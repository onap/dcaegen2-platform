// ============LICENSE_START=======================================================
// Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
// ================================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============LICENSE_END=========================================================

package com.att.vcc.configmanager.watcher;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.att.vcc.logger.VccLogger;
import com.att.vcc.utils.*;
import com.att.vcc.configmanager.ConfigManagerUtils;


public class DtiEventChangeProcessor implements IFileChangeProcessor {

	private static final String className="DtiEventChangeProcessor";	
	private static VccLogger logger = new VccLogger("DirWatcher");
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private static String targetEntity = "VCC_DTI_EVENT_PROCESSOR";

	private static final String EMPTY_TASKS = "{\"TasksItems\": { }}";
	
	private static JobUtilInterface jobUtil = new JobUtil();
	private Map<String, String> envMap = System.getenv();
	private String vcc_home = envMap.get("VCC_HOME");
	private String archive_dir = vcc_home + "/archive/dtievent/";
	
	private String dti_event_filename = "";
	
	public void processEvent(File file)
	{
		String methodName = "processEvent";
		
		logger.debug(className, methodName, "file="+file);
		logger.metric_start(targetEntity, file.toString());
		
		long startTime = new Date().getTime();
		
		String action = "";
		JSONObject additionInfoObj = null;
		String dcaeTargetType = null;
		boolean changed = false;
		
		try 
		{
			JSONObject dti_event = ConfigManagerUtils.readJsonFromFile(file.toString());
			dti_event_filename = file.getName();
			
			if (dti_event != null && dti_event.has("dcae_target_type")) {
				dcaeTargetType = dti_event.getString("dcae_target_type");
				logger.debug(className, methodName, "dcae_target_type="+dcaeTargetType);
			}
			
			if(StringUtils.isEmpty(dcaeTargetType))
				throw new Exception("dcae_target_type value not found in dti_event "+file.toString());
			
			if (dti_event.has("dcae_service_action")) {
				action = dti_event.getString("dcae_service_action");
				logger.debug(className, methodName, "dcae_service_action="+action);
			}
			
			if(StringUtils.isEmpty(action))
				throw new Exception("dcae_service_action value not found in dti_event "+file.toString());
			
			if (action.equalsIgnoreCase("notify"))
			{
				String status = "";
				String dcaeTargetName = "";
				/*
				 "dcae_target_name": "zldcdyh1bdce4kcma00.f7d151.dyh1b.tci.att.com ",
  				 "dcae_target_type": "K8sCluster",
  				 "dcae_service_action": "notify",
  				 "dcae_service_type": "",
  				 "dcae_service_location": "dyh1b",
  				 "dcae_target_prov-status":"active",
				 */
				if (dti_event.has("dcae_target_prov-status") && dti_event.has("dcae_target_name")) {
					status = dti_event.getString("dcae_target_prov-status");
					dcaeTargetName = dti_event.getString("dcae_target_name");
					logger.info(className, methodName, "Got notify event with dcae_target_name="+dcaeTargetName+", dcae_target_prov-status="+status);
					String k8s_cluster_file = envMap.get("K8SCLUSTER_STATUS_FILE");
					String k8s_cluster_fqdn = envMap.get("KUBE_CLUSTER_FQDN");
					logger.debug(className, methodName, "KUBE_CLUSTER_FQDN="+k8s_cluster_fqdn);
					if (dcaeTargetName.equalsIgnoreCase(k8s_cluster_fqdn)) {
						logger.info(className, methodName, "dcaeTargetName equals KUBE_CLUSTER_FQDN, updateK8sClusterStatus()");
						//ConfigManagerUtils.updateK8sClusterStatus(vcc_home, k8s_cluster_file, dcaeTargetName, status);
						ConfigManagerUtils.updateK8sClusterStatus(vcc_home, dcaeTargetName, status, "TRUE");
					}
					else
						logger.info(className, methodName, "dcaeTargetName does NOT equal KUBE_CLUSTER_FQDN, Do NOT updateK8sClusterStatus()");
				}
				else
				{
					logger.error(className, methodName, "Got notify event without dcae_target_name and dcae_target_prov-status");
				}
				
				archiveDtiEventFile(file);
				logger.metric_success();
				return;
			}
			else if (action.equalsIgnoreCase("delete"))
			{
				String dcaeTargetName = null;
				
				if (dti_event.has("dcae_target_name")) {
					dcaeTargetName = dti_event.getString("dcae_target_name");
					logger.debug(className, methodName, "dcae_target_name="+dcaeTargetName);
					
					handleDeleteAction(dcaeTargetName); 
				}
				
				// Update DTI_Config.json with new dti_event
				UpdateDTIConfigJsonFile(dcaeTargetType, action, dti_event);
				
				archiveDtiEventFile(file);
				logger.metric_success();
				return;
			}
			
				// Handle "add" and "update" action with aai_additional_info
			if (dti_event.has("aai_additional_info")) {
				additionInfoObj = dti_event.getJSONObject("aai_additional_info");
				logger.debug(className, methodName, "additionInfoObj="+additionInfoObj);
				JSONObject newTasks = createTasks(additionInfoObj);
				if (newTasks != null)
				{				
					String task_file = envMap.get("TASK_FILE");
					logger.debug(className, methodName, "Task json file=" + task_file);
					
					if (ConfigManagerUtils.isNonEmptyEnvVar(envMap, "TASK_FILE") && newTasks.keys() != null) 
					{
						JSONObject currentTasks = ConfigManagerUtils.readJsonFromFile(task_file);
						if (currentTasks == null)
							currentTasks = new JSONObject(EMPTY_TASKS);
					
						if(currentTasks.has("TasksItems") ) 
						{
							JSONObject tasksItemsObj = currentTasks.getJSONObject("TasksItems");						
	
							for (Iterator<String> newTasksIter = newTasks.keys(); newTasksIter.hasNext(); ) 
							{
								String tasksItemName = newTasksIter.next();
								logger.debug(className, methodName, "Process new tasksItemName=" + tasksItemName);	
		
								if (action.equalsIgnoreCase("add"))
								{
									/** Do not check, just add it
									if (tasksItemsObj.has(tasksItemName)) 
									{
										logger.info(className, methodName, "Action=add, Current Task.json already has "+tasksItemName);
									}
									else
									{
									**/
										tasksItemsObj.put(tasksItemName, newTasks.getJSONObject(tasksItemName));
										changed = true;
										logger.info(className, methodName, "Action=add, Add to Task.json for "+tasksItemName);
									//}
								}
								else if (action.equalsIgnoreCase("update"))
								{
									/** Do not check, if not exist, will add it
									if (tasksItemsObj.has(tasksItemName)) 
									{
									**/
										logger.info(className, methodName, "Action=update, Update Task.json for "+tasksItemName);
										tasksItemsObj.put(tasksItemName, newTasks.getJSONObject(tasksItemName));
										changed = true;
										
									/**
									}
									else
									{
										logger.info(className, methodName, "Action=update, Current Task.json does not have "+tasksItemName);
									}
									**/
								}
							}
							
							// If task changed, write back to Task.json
							if (changed)
								UpdateTaskJsonFile(currentTasks, task_file);
						}	
					}
				}
				else
				{
					logger.info(className, methodName, "dti_event does not have aai_additional_info.");
				}
			}
			
			// Update DTI_Config.json with new dti_event
			UpdateDTIConfigJsonFile(dcaeTargetType, action, dti_event);
			
			// Archive the file
			archiveDtiEventFile(file);
			
			logger.metric_success();
			
		} catch (Exception ex) {
			logger.error(className, methodName, "Catch Exception::", "ERROR001", ex);
		}
		
		long endTime = new Date().getTime();
		long diff = endTime - startTime;

		logger.metrics(className, methodName, diff, "End process::"+file+", Elapsed milliseconds: " + diff);
	}
	
	private JSONObject createTasks(JSONObject infoJson) throws Exception 
	{
		String methodName = "createTasks";

		JSONObject outputTasksListObj = null;
					
		if(infoJson.has("TasksItems") ) 
		{
			JSONObject tasksItemsObj = infoJson.getJSONObject("TasksItems");						
			if (tasksItemsObj.keys() != null) 
			{
				outputTasksListObj = new JSONObject();
				for (Iterator<String> tasksItemsIter = tasksItemsObj.keys(); tasksItemsIter.hasNext(); ) 
				{
					String tasksItemName = tasksItemsIter.next();
					logger.debug(className, methodName, "Process tasksItemName=" + tasksItemName);
					outputTasksListObj.put(tasksItemName, tasksItemsObj.getJSONObject(tasksItemName));
				}
			}							
		}

		return outputTasksListObj;
	}
	
	private void UpdateTaskJsonFile(JSONObject currentTasks, String task_file)
	{	
			// Write tasks back to Task.json file
		ConfigManagerUtils.writeJsonToFile(currentTasks , task_file);
	
			// Write tasks to tracking archive/dti_events/YYYY/MM/DD/HH/Task.json+dti_event_filename file
		String current_time = df.format(Calendar.getInstance().getTime());
		String destDirPath = archive_dir + 
				current_time.substring(0, 4) + File.separator + 
				current_time.substring(4, 6) + File.separator + 
				current_time.substring(6, 8) + File.separator + 
				current_time.substring(8, 10);
		String destFile = destDirPath + File.separator + "Task.json." + dti_event_filename;
		
		File destDir = new File (destDirPath);
		if (!destDir.exists()) 
			destDir.mkdirs();

			// Save updated Task.json to archive
		ConfigManagerUtils.writeJsonToFile(currentTasks, destFile);
	}
	
	private void archiveDtiEventFile(File file)
	{
		jobUtil.moveFileToDest(file, archive_dir, df.format(Calendar.getInstance().getTime()));
	}
	
	private void handleDeleteAction(String dcaeTargetName)
	{
		String methodName = "handleDeleteAction";
		logger.debug(className, methodName, "Delete dcae_target_name="+dcaeTargetName);

		boolean changed = false;
		
		try
		{
			// Handle delete action for Task.json file
			String task_file = envMap.get("TASK_FILE");
			
			if (ConfigManagerUtils.isNonEmptyEnvVar(envMap, "TASK_FILE")) 
			{
				JSONObject currentTasks = ConfigManagerUtils.readJsonFromFile(task_file);
				if (currentTasks != null && currentTasks.has("TasksItems") ) 
				{
					JSONObject tasksItemsObj = currentTasks.getJSONObject("TasksItems");						
					if (tasksItemsObj.keys() == null) 
					{
						logger.debug(className, methodName, "currentTasks TasksItems is empty.");	
						return;
					}
					
					JSONObject newTaskItemsObj = new JSONObject();
					String name = null;
					
					for (Iterator<String> currentTasksIter = tasksItemsObj.keys(); currentTasksIter.hasNext(); ) 
					{
						String tasksItemName = currentTasksIter.next();
						logger.debug(className, methodName, "Process tasksItemName=" + tasksItemName);	
		
						String[] arr = tasksItemName.split("_");
						name = arr[0];
								
						if (!dcaeTargetName.equals(name)) 
						{
								// Not the one to delete, copy to newTaskItemsObj
							newTaskItemsObj.put(tasksItemName, tasksItemsObj.getJSONObject(tasksItemName));
						}
						else
						{
							logger.info(className, methodName, "Action=delete, Delete from Task.json for "+tasksItemName);
							changed = true;
						}
					}
					
					// If task changed, write back to Task.json
					if (changed)
					{
						JSONObject outputTasksObject =new JSONObject();		
						outputTasksObject.put("TasksItems", newTaskItemsObj);
						UpdateTaskJsonFile(outputTasksObject, task_file);
					}
				}
			}
			
		} catch (Exception ex) {
			logger.error(className, methodName, "Catch Exception::", "ERROR001", ex);
		}
	}
	
	// Update DTI_Config.json with new dti_event
	private void UpdateDTIConfigJsonFile(String dcaeTargetType, String action, JSONObject dti_event)
	{
		String methodName = "UpdateDTIConfigJsonFile";
		boolean changed = false;
		
		logger.debug(className, methodName, "dcaeTargetType="+dcaeTargetType+", action="+action);
	
		String dti_config_file = envMap.get("DTI_CONFIG_FILE");
			
		if (ConfigManagerUtils.isNonEmptyEnvVar(envMap, "DTI_CONFIG_FILE")) 
		{
			JSONObject dti_config_json = ConfigManagerUtils.readJsonFromFile(dti_config_file);
			//logger.debug(className, methodName, "Read in dti_config_json="+dti_config_json.toString());

			JSONObject dtiConfigObj = null;
			if (dti_config_json != null && dti_config_json.has(dcaeTargetType)) 
				dtiConfigObj = dti_config_json.getJSONObject(dcaeTargetType);
			else
				dtiConfigObj = new JSONObject("{}");
				
			String dcaeTargetName = dti_event.getString("dcae_target_name");
			logger.debug(className, methodName, "dcaeTargetName="+dcaeTargetName);
			//logger.debug(className, methodName, "Before change dtiConfigObj="+dtiConfigObj.toString());

			if (action.equalsIgnoreCase("delete"))
			{
				if (dtiConfigObj.has(dcaeTargetName))
				{
					dtiConfigObj.remove(dcaeTargetName);
					//logger.debug(className, methodName, "After delete dtiConfigObj="+dtiConfigObj.toString());
					changed = true;
				}
			}	
			else if (action.equalsIgnoreCase("add"))
			{
				if (!dtiConfigObj.has(dcaeTargetName))
				{
					dtiConfigObj.put(dcaeTargetName, dti_event);
					//logger.debug(className, methodName, "After add dtiConfigObj="+dtiConfigObj.toString());
					changed = true;
				}
			}
			else if (action.equalsIgnoreCase("update"))
			{
				if (dtiConfigObj.has(dcaeTargetName))
				{
					dtiConfigObj.put(dcaeTargetName, dti_event);
					//logger.debug(className, methodName, "After update dtiConfigObj="+dtiConfigObj.toString());
					changed = true;
				}
			}
				
			// If dtiConfigObj changed, write back to DTI_Config.json
			if (changed)
			{
				JSONObject outputObject = new JSONObject();		
				outputObject.put(dcaeTargetType, dtiConfigObj);
				UpdateTaskJsonFile(outputObject, dti_config_file);
			}
		}
	}
}
	