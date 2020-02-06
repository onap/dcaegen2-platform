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

package com.att.vcc.configmanager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.att.vcc.logger.VccLogger;

public class SingleVnfTypeTaskConfigManager  extends ConfigManager {
	
	protected String dcaeTargetType; 
	
	private static String className = "SingleVnfTypeTaskConfigManager";
	

	public SingleVnfTypeTaskConfigManager(Map<String, String> envMap) {
		super(envMap);
		
	}
	
	@Override
	protected
	void createComponentConfig() throws Exception {
		// processing  parsing task from app config params
		// config will have single vnf type, one task and one vnf instance only

		if(null==this.appConfigJson) 
			throw new Exception("App config does not exist");
		

		if(this.appConfigJson.has("dcae_target_type"))
			this.dcaeTargetType = this.appConfigJson.getString("dcae_target_type");
		
		if(StringUtils.isEmpty(this.dcaeTargetType))
			throw new Exception("dcae_target_type value not found in application configuration");
		
		boolean isActive = true;
		if(this.appConfigJson.has("isActiveK8sCluster")) {
			isActive = this.appConfigJson.getBoolean("isActiveK8sCluster");
			logger.debug(className, "createComponentConfig", "Input isActiveK8sCluster="+isActive);
		}
		logger.debug(className, "createComponentConfig", "isActive="+isActive);
		
		updateK8sClusterStatus(isActive);
		
		createTaskJsonFile();
		createTaskPropertiesFile();
		//createDmaapConfFileFromStreams(jsonObject);
	}

	protected void createTaskJsonFile() throws Exception {

		final String fnName = "createTaskJsonFile";
		final String EMPTY_TASKS = "{\"TasksItems\": { }}";		
		
		String overrideTask = null;
		
		if(this.appConfigJson.has("overrideTaskJson"))
			overrideTask = this.appConfigJson.getString("overrideTaskJson");
		
		JSONObject outputTasksObject =null;		
				
		if(!StringUtils.isEmpty(overrideTask) && !"NA".equalsIgnoreCase(overrideTask)) {			
			logger.debug(className, fnName, "Creating Task Json from param overrideTaskJson"  );			
			outputTasksObject = new JSONObject(overrideTask);				
		}
		else {
			if(null==this.dtiConfigJson)
				throw new Exception("DTI config does not exist");
			
			outputTasksObject = createTaskListFromDTIConfig();			
		}	//end main else
		
		//if(outputTasksObject!=null) {			
				logger.debug(className, fnName, "create Task json file " + envMap.get(TASK_FILE) + " mapped from Application Configuration JSON");
				if(isNonEmptyEnvVar(TASK_FILE)) {						
					if(null==outputTasksObject) {
						outputTasksObject = new JSONObject(EMPTY_TASKS);
					}
					writeJsonToFile(outputTasksObject , envMap.get(TASK_FILE));
					
					logger.debug(className, fnName, "Done writing Task Json from param overrideTaskJson to file : " + this.envMap.get("TASK_FILE")  );
				}

		//}	
	}

	protected JSONObject createTaskListFromDTIConfig() throws Exception {
		
		final String fnName ="createTaskListFromDTIConfig"; 
		
		JSONObject outputTasksListObj = new JSONObject();		
				
		if(!this.dtiConfigJson.has(this.dcaeTargetType))
			return null;
		//throw new Exception("No object found in DTI config for DCAE target type : " + this.dcaeTargetType);
		
		JSONObject currentTargetTypeJson = this.dtiConfigJson.getJSONObject(this.dcaeTargetType);
		
		boolean isSelfServe = false;
		
		if(this.appConfigJson.has("isSelfServeComponent")) {
			isSelfServe = this.appConfigJson.getBoolean("isSelfServeComponent");
		}
		
		if(currentTargetTypeJson.keys()!=null) {
			outputTasksListObj = isSelfServe ?
									createTasks_SelfServe(currentTargetTypeJson) :
									createTasks_NonSelfServe(currentTargetTypeJson);
		}
		
		if(outputTasksListObj.length()>0) {
			JSONObject outputTasksObject =new JSONObject();		
			outputTasksObject.put("TasksItems", outputTasksListObj);
			return outputTasksObject;
		}
		else	
			return null;
	}

	JSONObject createTasks_NonSelfServe(JSONObject currentTargetTypeJson) throws Exception {
		
		JSONObject outputTasksListObj = new JSONObject();
		
		final String fnName = "createTasks_NonSelfServe";
		
		for(Iterator<String> dcaeTargetItr= currentTargetTypeJson.keys();dcaeTargetItr.hasNext(); ) {
			String dcaeTargetName = dcaeTargetItr.next();
			
			logger.debug(className, fnName, "Processing DCAE Target " +  dcaeTargetName );
			
			JSONObject targetItem = currentTargetTypeJson.getJSONObject(dcaeTargetName);
					
			if(targetItem.has("aai_additional_info") && targetItem.getJSONObject("aai_additional_info").has("TasksItems") ) {
				logger.debug(className, fnName, "Param aai_additional_info exists"  );


				JSONObject tasksItemsObj = targetItem.getJSONObject("aai_additional_info").getJSONObject("TasksItems");						
				if(tasksItemsObj.keys()!=null) {
					for(Iterator<String> tasksItemsIter = tasksItemsObj.keys(); tasksItemsIter.hasNext(); ) {
						String tasksItemName = tasksItemsIter.next();
						logger.debug(className, fnName, "Creating Task Json from param aai_additional_info for item " + tasksItemName  );
						outputTasksListObj.put(tasksItemName, tasksItemsObj.getJSONObject(tasksItemName));
					}
				}							
			}				
		}
		
		return outputTasksListObj;
	}

	JSONObject createTasks_SelfServe(JSONObject currentTargetTypeJson) throws Exception {

		JSONObject outputTasksListObj = new JSONObject();

		final String fnName = "createTasks_SelfServe";

		for(Iterator<String> dcaeTargetItr= currentTargetTypeJson.keys();dcaeTargetItr.hasNext(); ) {
			String dcaeTargetName = dcaeTargetItr.next();

			logger.debug(className, fnName, "Processing DCAE Target " +  dcaeTargetName );

			JSONObject targetItem = currentTargetTypeJson.getJSONObject(dcaeTargetName);

			logger.debug(className, fnName, "Creating Task Json from DCAE params "  );
			JSONObject tasksItem =createTaskJsonFromDcaeParams(targetItem);//self serve flow;
			String taskId = null;
			if(this.appConfigJson.has("collectionTaskId")) {
				taskId = this.appConfigJson.getString("collectionTaskId");
			}
			if(StringUtils.isEmpty(taskId)) {
				taskId = "DefaultTask";
			}

			String tasksItemName = dcaeTargetName + "_" + taskId ;
			if(tasksItem!=null)
				outputTasksListObj.put(tasksItemName, tasksItem);	
		}
		
		return outputTasksListObj;
	}
	
	protected  void createDmaapConfFileFromStreams(JSONObject jsonObject) {
		
		final String fnName = "createDmaapConfFileFromStreams" ;
		
		DmaapConfigMapper dm = new DmaapConfigMapper();
		JSONObject dmaapObj = dm.makeDmaapConf(jsonObject);
		
		if(dmaapObj!=null) {
			logger.debug(className, fnName, "create dmaap.conf file " + envMap.get(DMAAP_CONF_FILE) + " mapped from Application Configuration JSON");
			
			if(isNonEmptyEnvVar(DMAAP_CONF_FILE)) {
				writeJsonToFile(dmaapObj, envMap.get(DMAAP_CONF_FILE));
			}
		}
		else
			logger.error(className, fnName, " Could not create Dmaap json from App config ");
	}

	protected JSONObject createTaskJsonFromDcaeParams(JSONObject taskItem) throws Exception {

		final String fnName = "createTaskJsonFromDcaeParams" ;
		JSONObject taskObj = null;
		
		if(isNonEmptyEnvVar(YAML_DIR)) {
			TaskConfigMapper tm = new TaskConfigMapper(envMap.get(YAML_DIR));
			taskObj = tm.makeTask(this.appConfigJson,taskItem);
		}
		else
			logger.error(className, fnName, " Could not create Task json from App config ");

		return taskObj;
	}

	protected void createTaskPropertiesFile() throws Exception {
		//Task Properties json file
		String overrideTaskProp = null;		
			
		if(isNonEmptyEnvVar("TASK_PROP_FILE")) {
			if(this.appConfigJson.has("overrideTaskPropertiesJson"))
				overrideTaskProp = this.appConfigJson.getString("overrideTaskPropertiesJson");
		
			if(!StringUtils.isEmpty(overrideTaskProp) && !"NA".equalsIgnoreCase(overrideTaskProp)) {
				JSONObject taskPropJson = new JSONObject(overrideTaskProp);
				
				writeJsonToFile(taskPropJson, this.envMap.get("TASK_PROP_FILE"));
			}		
		}
	}

	protected void updateK8sClusterStatus (boolean isActive)
	{
		String vcc_home = envMap.get("VCC_HOME");
		String k8s_cluster_file = envMap.get("K8SCLUSTER_STATUS_FILE");
		String k8s_cluster_name = envMap.get("K8SCLUSTER_NAME");
		String status = (isActive? "ACTIVE" : "INACTIVE");
		logger.debug(className, "updateK8sClusterStatus", "Update k8s_cluster_name="+k8s_cluster_name+", status="+status);
		//ConfigManagerUtils.updateK8sClusterStatus(vcc_home, k8s_cluster_file, k8s_cluster_name, status);
		ConfigManagerUtils.updateK8sClusterStatus(vcc_home, k8s_cluster_name, status, "FALSE");
	}
	
	public static void main(String[] args) throws Exception {
		
		SingleVnfTypeTaskConfigManager cf = new SingleVnfTypeTaskConfigManager(System.getenv());
		cf.initComponentConfigs();
	}

}