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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.att.vcc.logger.VccLogger;
import com.att.vcc.utils.VccUtils;

public class ConfigManagerUtils
{
	private static final String className="ConfigManagerUtils";
	private static final String LOGGER_SUBCOMP_NAME= System.getProperty("subcomponent");
	private static VccLogger logger = new VccLogger(LOGGER_SUBCOMP_NAME);
	private static final String K8S_CLUSTER_STATUS_VAR = "K8S_CLUSTER_STATUS";
	
	public static boolean isNonEmptyEnvVar(Map<String, String> envMap, String envVar) {
		return (envMap.get(envVar) != null && !envMap.get(envVar).isEmpty());
	}

	public static JSONObject readJsonFromFile(String filename)  
	{
		String fnName="readJsonFromFile";
		try {
			String content = new String(Files.readAllBytes(Paths.get(filename))); 
	        return new JSONObject(content);
		}
		catch (Exception e) {
			logger.error(className, fnName, "Exception:"+e.toString());
		}
		
		return null;
	}			
	
	
	public static void writeJsonToFile(JSONObject jsonObject, String fileName)  
	{
		String fnName="writeJsonToFile";
		
		if(StringUtils.isEmpty(fileName))
			return;
		
		try {
			FileWriter fileWriter = new FileWriter(VccUtils.safeFileName(fileName));
			String tempJson=jsonObject.toString(4);
			tempJson.replaceAll("\\\"", "\"");
			logger.debug(className, fnName, "tempJson="+tempJson);
			fileWriter.write(tempJson);
			fileWriter.close();
			logger.debug(className, fnName, "Successfully Copied JSON Object to file " + fileName);
		}
		catch (Exception e) {
			logger.error(className, fnName, "Exception:"+e.toString());
		}		
	}

	// Only write K8S_CLUSTER_STATUS to vcc_env
	public static void updateK8sClusterStatus(String vcc_home, String k8s_cluster, String status, String isNotifyEvent)
	{
		String methodName = "updateK8sClusterStatus";
		
		logger.debug(className, methodName, "Set k8s_cluster [" + k8s_cluster + "] to status ["+ status + "]. isNotifyEvent="+isNotifyEvent);
		
		ProcessBuilder pb = null;
		Process p = null;
		try {
			// Cannot just pass cmdStr.toString() to ProcessBuilder
			pb = new ProcessBuilder(vcc_home+"/bin/configManager/updateK8sClusterStatus.sh", status.toUpperCase(), isNotifyEvent);
			pb.redirectErrorStream(true);
			p = pb.start();
            p.waitFor();
            
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;

			while ((line = reader.readLine()) != null) {
				logger.debug(className, methodName, "updateK8sClusterStatus, line=" + line);
			}
			
		} catch (Exception e) {
			logger.error(className, methodName, "Exception:"+e.toString(), "Error");
		} finally {
			if(p != null) { 
				try {
					OutputStream o = p.getOutputStream();
					if ( o!= null) o.close();
					InputStream inp = p.getInputStream();
					if ( inp != null) inp.close();
					InputStream err = p.getErrorStream();
					if (err != null)  err.close();
				} catch (Exception ee) {
				}
			}
		}
	}
	
	/**
	public static void updateK8sClusterStatus(String vcc_home, String k8s_cluster_file, String k8s_cluster, String status)
	{
		String methodName = "updateK8sClusterStatus";
		
		logger.debug(className, methodName, "Set k8s_cluster [" + k8s_cluster + "] to status ["+ status + "] in file " + k8s_cluster_file);
		
		BufferedWriter outWriter = null;
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		
		BufferedWriter outWriter2 = null;
		FileOutputStream fos2 = null;
		OutputStreamWriter osw2 = null;

		try
		{
				// Write to K8SCLUSTER_STATUS_FILE
			fos = new FileOutputStream(VccUtils.safeFileName(k8s_cluster_file), false);
			osw = new OutputStreamWriter(fos);			
			outWriter = new BufferedWriter(osw);
	
			StringBuffer sb = new StringBuffer();
			sb.append(k8s_cluster).append("~").append(status.toUpperCase()).append("\n");
			outWriter.write(sb.toString());
	
			VccUtils.safeClose(outWriter);
		
			VccUtils.safeClose(osw);
			VccUtils.safeClose(fos);
			
				// Update export to bin/common/vcc_env file
			fos2 = new FileOutputStream(VccUtils.safeFileName(vcc_home+"/bin/common/vcc_env"), true);
			osw2 = new OutputStreamWriter(fos2);			
			outWriter2 = new BufferedWriter(osw2);
	
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			sb = new StringBuffer();
			sb.append("# Updated at ").append(df2.format(Calendar.getInstance().getTime())).append("\n");
			sb.append("export ").append(K8S_CLUSTER_STATUS_VAR).append("=").append(status.toUpperCase()).append("\n");
			outWriter2.write(sb.toString());
	
			VccUtils.safeClose(outWriter2);
		
			VccUtils.safeClose(osw2);
			VccUtils.safeClose(fos2);
			
		} catch (IOException ioe) {
			logger.error(className, methodName, "IO Exception Caught :: " + ioe, "ERROR");
		} catch (Exception ee) {
			logger.error(className, methodName, "Exception Caught :: " + ee, "ERROR");
		} finally {
			VccUtils.safeClose(outWriter);
			VccUtils.safeClose(osw);
			VccUtils.safeClose(fos);
			
			VccUtils.safeClose(outWriter2);
			VccUtils.safeClose(osw2);
			VccUtils.safeClose(fos2);
		}
	}
	**/
}
