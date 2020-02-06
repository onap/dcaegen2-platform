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

package com.att.vcc.dtiProcConfigManager;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.att.vcc.configmanager.ConfigManager;
// import com.att.vcc.utils.VccUtils;
import com.att.vcc.logger.*;

public class DtiProcConfigManager extends ConfigManager {
	private static VccLogger logger = new VccLogger("dti");
	private static String className = "DtiProcConfigManager";
	
	private static String vcc_home = System.getenv("VCC_HOME");
	
	
	public DtiProcConfigManager(Map<String, String> envMap) {
		super(envMap);
	}

	public static void main(String[] args) throws Exception {	
		
		logger.debug(className, "main", "vcc_home="+vcc_home);
		
		DtiProcConfigManager cf = new DtiProcConfigManager(System.getenv());		
		cf.initComponentConfigs();
	}
		
	@Override
	protected
	void createComponentConfig() throws Exception {
		String methodName = "createComponentConfig";
		
		try
		{
			logger.debug(className, methodName, "vcc_home="+vcc_home);
			PropertiesConfiguration config = new PropertiesConfiguration(vcc_home+"/config/dtiproc/DtiProcProfile.cfg");
			
			if(null==this.appConfigJson) {
				logger.debug(className, methodName, "App config does not exist");
				throw new Exception("App config does not exist");
			}
			
			/*
			if(this.appConfigJson.has("freq_ms")) {
				config.setProperty("freq_ms", this.appConfigJson.getString("freq_ms"));
				logger.debug(className, methodName, "freq_ms="+this.appConfigJson.getString("freq_ms"));
			}
			*/
			
			config.save(); 
		} catch (ConfigurationException ex)
		{
			logger.error(className, methodName, "ConfigurationException::"+ex.toString());
		}
	}

}
