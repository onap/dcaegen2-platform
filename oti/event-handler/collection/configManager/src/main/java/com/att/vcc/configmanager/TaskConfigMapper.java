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

//Maps the Application configuration to Task json 

package com.att.vcc.configmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import com.att.vcc.utils.VccUtils;


public class TaskConfigMapper {

	
	
	String yamlResourceDir;
	String yamlTaskFile;
	String yamlRemoteSvrGroupFile;
	
	protected final String DEFAULT_TASK="DefaultTask";
	
	
	public TaskConfigMapper(String yamlResourceDir) {
	
		this.yamlResourceDir = yamlResourceDir;
		
		yamlTaskFile = yamlResourceDir + "/AppConfigToTask.yaml";
		yamlRemoteSvrGroupFile = yamlResourceDir + "/DtiConfigToRemoteServer.yaml";
		
		
	}
	
	@SuppressWarnings("unchecked")
	Map<String, Object> parseYaml(String yamlFile) throws Exception {

		Map<String, Object> result = null;
		Yaml yaml = new Yaml();
		InputStream ios = new FileInputStream(new File(VccUtils.safeFileName(yamlFile)));
		// Parse the YAML file and return the output as a series of Maps
		result = (Map<String, Object>) yaml.load(ios);
		return result;
	}

	
	public JSONObject makeTask(JSONObject appConfig,JSONObject targetItem) throws Exception {
			
		JSONObject task = makeJsonObj(appConfig,yamlTaskFile);								
		
		JSONObject remoteSvrGrp = makeJsonObj(targetItem,yamlRemoteSvrGroupFile);					
				
		JSONObject remoteSvrGrpWrapper = new JSONObject();
		JSONObject remoteServerGrpItem =  new JSONObject();
		remoteServerGrpItem.put("serverGroupId", "g1");
		
		JSONObject [] serverGroupArray = new JSONObject[1] ;
		serverGroupArray[0] = remoteSvrGrp;
		
		remoteServerGrpItem.put("serverGroup", serverGroupArray);		
		remoteSvrGrpWrapper.put("g1", remoteServerGrpItem);
		task.put("remoteServerGroups", remoteSvrGrpWrapper);				
		return(task);
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected JSONObject makeJsonObj(JSONObject inputJsonObj,String yamlFile ) throws Exception {
		
		Map<String,Object> yamlMap = parseYaml(yamlFile);
		
		Map<String, Object> contentMap= (Map<String, Object>) yamlMap.get("contents"); 
		Map<String, Object> contentDefaults=(Map<String, Object>) yamlMap.get("defaults");
		Map<String, Object> constantsMap=(Map<String, Object>) yamlMap.get("otherconstants");
		
		JSONObject outputObj = new JSONObject();
				
		
		for (Iterator<Entry<String, Object>> iterator = contentMap.entrySet().iterator(); iterator.hasNext();) {
			
			Entry<String, Object> itm =  iterator.next();
			
			String mappedName = null;			
			if(itm.getValue() instanceof String){
					mappedName = (String)itm.getValue() ;
				
					if(mappedName!=null){
						if(inputJsonObj.has(mappedName) 
								&& !StringUtils.isEmpty( inputJsonObj.getString(mappedName))
										&& !"NA".equalsIgnoreCase(inputJsonObj.getString(mappedName)))
							outputObj.put(itm.getKey(),  inputJsonObj.getString(mappedName) );
						else{
							if(contentDefaults!=null ) {
								Object defaultVal = contentDefaults.get(itm.getKey());
								if(defaultVal!=null)
									outputObj.put(itm.getKey(),defaultVal);
							}
						}
					}
			}
			
		}
		
		if(constantsMap!=null)
			fillStaticValues(outputObj,constantsMap);
		
		return outputObj;
	}
	
	void fillStaticValues(JSONObject outputObj,Map<String, Object> constantsMap ) {
		
	
		for (Iterator<Entry<String, Object>> iterator = constantsMap.entrySet().iterator(); iterator.hasNext();) {
			
			Entry<String, Object> itm =  iterator.next();
			
			outputObj.put(itm.getKey(), itm.getValue());
			
		}
	
	}
	
	
}
