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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.att.vcc.logger.VccLogger;
import com.att.vcc.utils.VccUtils;

public abstract class ConfigManager {
	
private static String className = "ConfigManager";
	
	protected static final String LOGGER_SUBCOMP_NAME= System.getProperty("subcomponent");
	
	protected static final String APP_CONFIG_FILE = "APP_CONFIG_FILE";
	protected static final String POLICY_CONFIG_FILE = "POLICY_CONFIG_FILE";
	protected static final String DTI_CONFIG_FILE = "DTI_CONFIG_FILE";
	protected static final String TASK_FILE = "TASK_FILE";
	protected static final String YAML_DIR = "YAML_RESOURCE_DIR";
	protected static final String DMAAP_CONF_FILE = "DMAAP_CONF_FILE";
	static final String CONSUL_HOST = "CONSUL_HOST";
	protected static final String CONFIG_BINDING_SERVICE = "CONFIG_BINDING_SERVICE";
	//protected static final String HOSTNAME = "HOSTNAME";
	// For k8s, MYHOSTNAME=COMPONENT_NAME
	// For docker, MYHOSTNAME=HOSTNAME, set in setup-env.sh
	protected static final String HOSTNAME = "MYHOSTNAME";
	
	protected static VccLogger logger = new VccLogger(LOGGER_SUBCOMP_NAME);
		
	protected JSONObject appConfigJson = null;
	protected  JSONObject dtiConfigJson = null ;
	protected  JSONObject policyConfigJson = null ;
	protected JSONObject allAppConfigJson = null;

	protected Map<String, String> envMap;
	
	protected String serviceAddress;
	protected int servicePort;

	private String consulHost;

	private String configBindingSvc;

	private String hostName;

	protected abstract void createComponentConfig() throws Exception ;

	
	
	/*public static  void main(String[] args) throws Exception;
	 {
		/*
		 * 
		 * Sample code in main
		 * ConfigManager cfm = new ConcreteSubclassOfConfigManager(System.getenv());
		 * cfm.initComponentConfigs();
		*/
		
	
	
	public ConfigManager(Map<String, String> envMap) {
		this.envMap = envMap;
		
	}
	public void initComponentConfigs() throws Exception {
		
		String fnName="initComponentConfigs";
		
		if (isNonEmptyEnvVar(CONSUL_HOST) && isNonEmptyEnvVar(CONFIG_BINDING_SERVICE)
				&& isNonEmptyEnvVar(HOSTNAME)) {

			String host = envMap.get(HOSTNAME);
			String uuid = UUID.randomUUID().toString();
			logger.auditstart(host, uuid, "VCC");

			try {
				try {
					resolveServiceDetails();
				}			
				catch(Exception e) {

					throw new Exception ("Error resolving Service Details for Config Binding. Error : " + e.toString());
				}


				try {
					//Complete KV config with app config plus dti plus policies plus streams (as applicable)
					//KV_Config.json.ALL
					this.allAppConfigJson = callAPIAndSaveToFile("service_component_all",envMap.get(APP_CONFIG_FILE) + ".ALL" );
				}
				catch(Exception e) {
					throw new Exception("Error fetching ALL Application Config. Error : " + e.toString() );
				}
				
				try {
					// Save application config part
					if( this.allAppConfigJson!=null && this.allAppConfigJson.has("config")) {
						
						
						this.appConfigJson = this.allAppConfigJson.getJSONObject("config");
						String fullPathToLocalFilename=envMap.get(APP_CONFIG_FILE);						
						writeJsonToFile(this.appConfigJson,fullPathToLocalFilename );
					}
				}
				catch(Exception e) {
					logger.warn(className, fnName, "Error fetching DTI Config" + e.toString() );
				}

				try {
					//This is optional for some components and only used if there is dti reconfig flow
					if(this.allAppConfigJson!=null && this.allAppConfigJson.has("dti")) {
						
						//this.dtiConfigJson = callAPIAndSaveToFile("dti", envMap.get(DTI_CONFIG_FILE));
						this.dtiConfigJson = this.allAppConfigJson.getJSONObject("dti");
						String fullPathToLocalFilename=envMap.get(DTI_CONFIG_FILE);						
						writeJsonToFile(this.dtiConfigJson,fullPathToLocalFilename );
					}
				}
				catch(Exception e) {
					logger.warn(className, fnName, "Error fetching DTI Config" + e.toString() );
				}

				try {
					//This is optional for some components and only used if there is policy reconfig flow
					if(this.allAppConfigJson!=null && this.allAppConfigJson.has("policies")) {						
						this.policyConfigJson = this.allAppConfigJson.getJSONObject("policies");
						String fullPathToLocalFilename=envMap.get(POLICY_CONFIG_FILE);						
						writeJsonToFile(this.policyConfigJson,fullPathToLocalFilename );
					}
				}
				catch(Exception e) {
					logger.warn(className, fnName, "Error fetching DTI Config" + e.toString() );
				}

				createComponentConfig();	
				logger.audit_success();
			}
			catch(Exception e) {
				logger.error(className, fnName, e.toString());
				logger.audit_fail();				
				throw e;
			}
		}


	}
	
	protected boolean needDtiConfig() {
				
		if(this.appConfigJson!=null) {
			if(this.appConfigJson.has("useDtiConfig")) {
				if(this.appConfigJson.getBoolean("useDtiConfig"))
					return true;
			}
		}
		
		return false;
	}

	private JSONObject callAPIAndSaveToFile(String apiName, String fullPathLocalFileName) throws Exception {
		final String fnName = "callAPIAndSaveToFile";
		logger.debug(className, fnName, "Started.");
		String ret_string = null;

		HttpConnect http = new HttpConnect();
	
		String url = "http://" + this.serviceAddress + ":" + this.servicePort + "/"  +  apiName + "/" + this.hostName;
		logger.debug(className, fnName, "HOSTNAME URL = " + url);

		ret_string = http.sendGet(url);

		JSONObject jsonObject = null;

		if (!StringUtils.isEmpty(ret_string)) {
			jsonObject = new JSONObject(new JSONTokener(ret_string));

			logger.debug(className, fnName, "write out the Configuration JSON to a file");

			writeJsonToFile(jsonObject,fullPathLocalFileName );

		}

		return jsonObject;
	}
	
	
	protected boolean isNonEmptyEnvVar(String envVar) {
		return (envMap.get(envVar) !=null && !envMap.get(envVar).isEmpty() ) ;
	}
	
	
	protected void resolveServiceDetails() throws Exception {
		final String fnName = "resolveServiceDetails";
		logger.debug(className, fnName, "Started.");
		
		
			

			for (String envName : envMap.keySet()) {
				logger.debug(className, fnName, "Env variables list : " + envName + " = " + envMap.get(envName));

			}
			
			String ret_string = null;
		
			this.consulHost=envMap.get(CONSUL_HOST);
			this.configBindingSvc= envMap.get(CONFIG_BINDING_SERVICE);
			this.hostName = envMap.get(HOSTNAME);
			
			HttpConnect http = new HttpConnect();
			logger.debug(className, fnName, ">>>Dynamic configuration to be fetched from ConfigBindingService");

			String url = "http://" + this.consulHost + ":8500/v1/catalog/service/" + this.configBindingSvc;
			logger.debug(className, fnName, "CONFIG_BINDING_SERVICE URL = " + url);

			ret_string = http.sendGet(url);

			// consul returns as array
			JSONTokener temp = new JSONTokener(ret_string);
			JSONObject cbsjobj = (JSONObject) new JSONArray(temp).get(0);

			String url_part1 = null;
			if (cbsjobj.has("ServiceAddress") && cbsjobj.has("ServicePort")) {
				this.serviceAddress = cbsjobj.getString("ServiceAddress") ;
				this.servicePort= cbsjobj.getInt("ServicePort");			
			}
			
		
		
		
		
	}
	
		
			
	public  JSONObject readJsonFromFile(String filename)  {
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
	
	
	public  void writeJsonToFile(JSONObject jsonObject, String fileName)  {
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

}