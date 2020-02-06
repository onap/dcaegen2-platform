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

package com.att.vcc.common.route;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.lang3.StringUtils;

import com.att.vcc.logger.VccLogger;

public class RouteLoaderXML {
	private static VccLogger log = new VccLogger("RouteLoaderXML", RouteLoaderXML.class);
	CamelContext context;
	Path path;
	String routeFileOverride;
	String propSource;
	String routeFile;
	
	private final static String C_ENV_PROPERTIES = "env.properties";
	private final static String C_COMMON_CONFIG_DIR = "VCC_CONFIG_COMMON_DIR";
	private final static String C_CDR_CONFIG_DIR = "CDR_CONFIG_DIR";
	private final static String C_RUNTIME_ENV = "RUNTIME_ENV";

	public RouteLoaderXML(CamelContext context, Path path, String routeFileOverride) {
		super();
		this.context = context;
		this.path = path;
		this.routeFileOverride = routeFileOverride;
	}
	public String getPropertiesLocation(String location) throws IOException,FileNotFoundException,Exception {
		String propSource = null;

		if (location.equalsIgnoreCase(C_ENV_PROPERTIES)) {
			String cdrConfigDir = System.getProperty(C_CDR_CONFIG_DIR);
			if (StringUtils.isNotEmpty(cdrConfigDir)) {
				propSource = cdrConfigDir + "/" + location;
			} else { // Log this error but continue and try to find it in the next section
				log.error(this.getClass().getName(), "getPropertiesLocation", C_COMMON_CONFIG_DIR + " is empty/null. Please set this environment variable");
			}
		} else {
			String commonDir = System.getenv(C_COMMON_CONFIG_DIR);
			if (StringUtils.isNotEmpty(commonDir)) {
				propSource = commonDir + "/" + location;
			} else {
				log.error(this.getClass().getName(), "getPropertiesLocation", C_CDR_CONFIG_DIR + " is empty/null. Please set this environment variable");
			}
		}
		
    	try {
    		log.debug(this.getClass().getName(), "","Trying Properties/config file: " + propSource);
			Path file = Paths.get(propSource);
			log.info(this.getClass().getName(), "","Attribute isFile: " + Files.getAttribute(file, "basic:isRegularFile"));
			propSource="file:"+propSource;
			log.info(this.getClass().getName(), "","Using Properties/config file: " + propSource);
    	} catch (FileNotFoundException e) {
            log.warn(this.getClass().getName(), "",propSource + ": " + e);
			propSource="classpath:"+location;
    	} catch (IOException e) {
            log.warn(this.getClass().getName(), "",propSource + ": " + e);
			propSource="classpath:"+location;
    	} catch (Exception e) {
            log.warn(this.getClass().getName(), "",propSource + ": " + e);
			propSource="classpath:"+location;
    	}
    	
        log.info(this.getClass().getName(), "","Property source: " + propSource);
        this.propSource = propSource;
		return propSource;
	}
	public String getRouteFileName() {
		String configRouteFile= (StringUtils.isNotEmpty(routeFileOverride) ? routeFileOverride : "");
		
		try {
			if (path != null && path.getFileName() != null && path.getFileName().toString().contains("-") ) {
				String jarBasename = path.getFileName().toString().substring(0, path.getFileName().toString().indexOf("-"));
				if(StringUtils.isNotEmpty(System.getenv("ROUTES"))) {
					configRouteFile = System.getenv("ROUTES");
				}
				else if(StringUtils.isEmpty(configRouteFile)) {
		        	configRouteFile=jarBasename+".xml";
				}
			}
        } catch (Exception e) {
               log.info(this.getClass().getName(), "","getRouteFile: " + e);
        }
		return configRouteFile;
	}

	public void loadRouteDefinitionXML() throws Exception {
		InputStream is = null;
		String cdrConfigDir = System.getProperty(C_CDR_CONFIG_DIR);
		if (StringUtils.isEmpty(cdrConfigDir)) {
			log.error(this.getClass().getName(), "loadRouteDefinitionXML", C_CDR_CONFIG_DIR + " is empty/null. Please set this environment variable");
			return;
		} 
		
		String runtimeEnv = System.getProperty(C_RUNTIME_ENV);
		String routeFileName = "";
		if (StringUtils.isNotEmpty(runtimeEnv) && runtimeEnv.equalsIgnoreCase("LOCAL")) {
	    	routeFileName = cdrConfigDir + "\\" + getRouteFileName();
		} else {
			routeFileName = cdrConfigDir + "/" + getRouteFileName();
		}
				
       	File f = new File(routeFileName);
        log.debug(this.getClass().getName(), "","Trying Route file: " + routeFileName);
        
		try {
			is = new FileInputStream(f);
			this.routeFile=routeFileName;
		} catch (FileNotFoundException e) {
			log.error(this.getClass().getName(), "","Route file not found ... "+f+"("+C_CDR_CONFIG_DIR+")");
			throw e;
		} finally {
			if(is != null) {
				log.debug(this.getClass().getName(), "","Route file is: " + routeFileName +"(" + C_CDR_CONFIG_DIR  +")");
				try {
					//External
					RoutesDefinition routes = context.loadRoutesDefinition(is);
					context.addRouteDefinitions(routes.getRoutes());
					is.close();
				} catch (Exception e) {
					log.error(this.getClass().getName(), "","Route file load error ("+f+")..."+e);
					throw e;
				}
			}
		}
	}
	public String getPropSource() {
		return this.propSource;
	}
	public String getRouteFile() {
		return this.routeFile;
	}
}
