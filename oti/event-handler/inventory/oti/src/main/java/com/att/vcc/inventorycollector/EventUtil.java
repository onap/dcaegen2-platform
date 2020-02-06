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

package com.att.vcc.inventorycollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.att.aft.dme2.internal.apache.commons.lang.time.DateUtils;
//import com.att.dcaetd.common.utilities.logging.EcompLogger;
//import com.att.dcaetd.common.utilities.logging.LogType;
//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;

public class EventUtil {

	public static String AAICLIENT;
	public static String UEBFILTER;
	public static String UEBURL;
	public static String UEBGROUP;
	public static String UEBID;
	public static String UEBEVENTSDIR;
	public static String UEBAPIKEY;
	public static String UEBAPISECRET;
	public static String UEBTIMEOUT_MS;
	public static String UEBLIMIT;
	public static String DBFILESDIR;
	public static String DMAAPCONSUMERFILEPATH;
	public static String DMAAPOUTPUTTMPDIR;
	public static String DMAAPCONSUMERFILEPATHDB;
	public static String DMAAPCONSUMERFILEPATHNARAD;
	public static String DMAAPCONSUMERFILEPATHNARADDB;
	public static String DMAAPCONSUMERFILEPATHSM;
	public static String DOMAIN;
	public static String COLLECTOREVENTS;
	public static String NARADCOLLECTOREVENTS;
	public static String DBEXCLUDEOBJECTS;
	public static String EVENTSDIR;
	public static Map<String, String> fullSyncMap;
	public static Map<String, String> fullSyncMapWeekly;
	public static String CLOUD_REGION_PATH;
	public static String API_URL;
	public static String TRUSTSTORE_PATH;
	public static String TRUSTSTORE_PASSWORD;
	public static String KEYSTORE_PATH;
	public static String KEYSTORE_LOCATION;
	public static String KEYSTORE_PASSWORD;
	public static String COLLECTORSYNCOBJECTS;
	public static String DMAAPDRPUBPATH;
	public static String DMAAPCONFPATH;
	public static String DMAAPOUTPUTDIR;
	public static String DCAEENV;
	public static String ENABLE_PG_LOAD;
	public static String ENABLE_DMAAP_PUBLISH;
	public static String ENABLE_COLLECTOR_EVENTS;
	public static String ENABLE_COLLECTOR_EVENTS_NARAD;
	public static String SET_TRUSTSTORE;
	public static String READ_TIMEOUT;
	public static String CONNECTION_TIMEOUT;
	public static String PROD_TARGET_NODE;
	public static String DMAAPCONSUMERCONFFILE;
	public static String DMAAPCONSUMERPREFERREDROUTE;
	public static Map<String, String> CollectorSyncMap;
	public static String SEEDINGMANAGERDATETIMESTAMP;
	public static String GENERICVNFURL;
	public static String fileSeperator = "/";
	public static String CANOPI_FILE_PROCESSED_FOLDER;
	public static String SAMSUNG_FILE_FOLDER;
	public static String D2MSN_FILE_FOLDER;
	public static String SAMSUNG_FILENAME = "NT_LTE_ENODEB_INVENTORY";
	public static String D2MSN_FILENAME = "D2_Device_list";
	public static String PCRF_FILENAME = "D1_Inventory";
	public static String weeklySyncDay;
	public static int pageSize;
	public static int modelPageSize;

	private static EcompLogger ecompLogger;
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public static final Map<String, Integer> naradEntities;
	public static final Map<String, String> naradEntitiesNodesUrl;
	public static final Map<String, Integer> aaiEntities;
	public static final Map<String, String> aaiEntitiesNodesUrl;
	static {
		aaiEntities = new HashMap<>();
		aaiEntities.put(Constants.ALLOTTED_RESOURCE_ENTITY, 1);
		aaiEntities.put(Constants.AVAILABILITY_ZONE_ENTITY, 1);
		aaiEntities.put(Constants.CLOUD_REGION_ENTITY, 1);
		aaiEntities.put(Constants.COMPLEX_ENTITY, 1);
		aaiEntities.put(Constants.CP_ENTITY, 1);
		aaiEntities.put(Constants.CUSTOMER_ENTITY, 1);
		aaiEntities.put(Constants.FLAVOR_ENTITY, 1);
		aaiEntities.put(Constants.FORWARDER_ENTITY, 1);
		aaiEntities.put(Constants.FORWARDING_PATH_ENTITY, 1);
		aaiEntities.put(Constants.GENERIC_VNF_ENTITY, 1);
		aaiEntities.put(Constants.IMAGE_ENTITY, 1);
		aaiEntities.put(Constants.L_INTERFACE_ENTITY, 1);
		aaiEntities.put(Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY, 1);
		aaiEntities.put(Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY, 1);
		aaiEntities.put(Constants.L3_NETWORK_ENTITY, 1);
		aaiEntities.put(Constants.LAG_INTERFACE_ENTITY, 1);
		aaiEntities.put(Constants.LAG_LINK_ENTITY, 1);
		aaiEntities.put(Constants.LINE_OF_BUSINESS_ENTITY, 1);
		aaiEntities.put(Constants.LOGICAL_LINK_ENTITY, 1);
		aaiEntities.put(Constants.MODEL_ENTITY, 1);
		aaiEntities.put(Constants.MODEL_VER_ENTITY, 1);
		aaiEntities.put(Constants.NETWORK_PROFILE_ENTITY, 1);
		aaiEntities.put(Constants.NOS_SERVER_ENTITY, 1);
		aaiEntities.put(Constants.OAM_NETWORK_ENTITY, 1);
		aaiEntities.put(Constants.OPERATIONAL_ENVIRONMENT_ENTITY, 1);
		aaiEntities.put(Constants.OWNING_ENTITY_ENTITY, 1);
		aaiEntities.put(Constants.P_INTERFACE_ENTITY, 1);
		aaiEntities.put(Constants.PHYSICAL_LINK_ENTITY, 1);
		aaiEntities.put(Constants.PLATFORM_ENTITY, 1);
		aaiEntities.put(Constants.PNF_ENTITY, 1);
		aaiEntities.put(Constants.PORT_GROUP_ENTITY, 1);
		aaiEntities.put(Constants.PROJECT_ENTITY, 1);
		aaiEntities.put(Constants.PSERVER_ENTITY, 1);
		aaiEntities.put(Constants.SERVICE_ENTITY, 1);
		aaiEntities.put(Constants.SERVICE_CAPABILITY_ENTITY, 1);
		aaiEntities.put(Constants.SERVICE_INSTANCE_ENTITY, 1);
		aaiEntities.put(Constants.SERVICE_SUBSCRIPTION_ENTITY, 1);
		aaiEntities.put(Constants.SRIOV_PF_ENTITY, 1);
		aaiEntities.put(Constants.SRIOV_VF_ENTITY, 1);
		aaiEntities.put(Constants.SUBNET_ENTITY, 1);
		aaiEntities.put(Constants.TENANT_ENTITY, 1);
		aaiEntities.put(Constants.VCE_ENTITY, 1);
		aaiEntities.put(Constants.VF_MODULE_ENTITY, 1);
		aaiEntities.put(Constants.VIP_IPV4_ADDRESS_LIST_ENTITY, 1);
		aaiEntities.put(Constants.VIP_IPV6_ADDRESS_LIST_ENTITY, 1);
		aaiEntities.put(Constants.VIRTUAL_DATA_CENTER_ENTITY, 1);
		aaiEntities.put(Constants.VLAN_ENTITY, 1);
		aaiEntities.put(Constants.VNF_IMAGE_ENTITY, 1);
		aaiEntities.put(Constants.VNFC_ENTITY, 1);
		aaiEntities.put(Constants.VPLS_PE_ENTITY, 1);
		aaiEntities.put(Constants.VSERVER_ENTITY, 1);
		aaiEntities.put(Constants.ZONE_ENTITY, 1);
		aaiEntities.put(Constants.NEWVCE_ENTITY, 1);

		aaiEntitiesNodesUrl = new HashMap<>();
		aaiEntitiesNodesUrl.put(Constants.COMPLEX_ENTITY, "complexes");
		aaiEntitiesNodesUrl.put(Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY,
				Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY);
		aaiEntitiesNodesUrl.put(Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY,
				Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY);
		aaiEntitiesNodesUrl.put(Constants.LINE_OF_BUSINESS_ENTITY, "lines-of-business");
		aaiEntitiesNodesUrl.put(Constants.OWNING_ENTITY_ENTITY, "owning-entities");
		aaiEntitiesNodesUrl.put(Constants.VIP_IPV4_ADDRESS_LIST_ENTITY, Constants.VIP_IPV4_ADDRESS_LIST_ENTITY);
		aaiEntitiesNodesUrl.put(Constants.VIP_IPV6_ADDRESS_LIST_ENTITY, Constants.VIP_IPV6_ADDRESS_LIST_ENTITY);
		aaiEntitiesNodesUrl.put(Constants.SERVICE_CAPABILITY_ENTITY, "service-capabilities");
		naradEntities = new HashMap<>();
		naradEntities.put("card", 1);
		naradEntities.put("card-slot", 1);
		naradEntities.put("cable", 1);
		naradEntities.put("chassis", 1);
		naradEntities.put("cloud-region", 1);
		naradEntities.put("complex", 1);
		naradEntities.put("instance-group", 1);
		naradEntities.put("l3-interface-ipv4-address-list", 1);
		naradEntities.put("l3-interface-ipv6-address-list", 1);
		naradEntities.put("l3-network", 1);
		naradEntities.put("lag-interface", 1);
		naradEntities.put("l-interface", 1);
		naradEntities.put("logical-link", 1);
		naradEntities.put("physical-link", 1);
		naradEntities.put("p-interface", 1);
		naradEntities.put("pluggable", 1);
		naradEntities.put("pluggable-slot", 1);
		naradEntities.put("pnf", 1);
		naradEntities.put("port", 1);
		naradEntities.put("pserver", 1);
		naradEntities.put("rack", 1);
		naradEntities.put("subnet", 1);
		naradEntities.put("vlan", 1);
		naradEntities.put("zone", 1);
		naradEntitiesNodesUrl = new HashMap<>();
		naradEntitiesNodesUrl.put("chassis", "chassies");
		naradEntitiesNodesUrl.put("complex", "complexes");
		naradEntitiesNodesUrl.put("l3-interface-ipv4-address-list", "l3-interface-ipv4-address-list");
		naradEntitiesNodesUrl.put("l3-interface-ipv6-address-list", "l3-interface-ipv6-address-list");
	}

	public static Map<String, Map<String, String>> relationshipConfigMap;
	public static Map<String, String> entitytypeTableMap;
	public static Map<String, String> tablePrimaryKeyProperties;
	private static final String subcomponent = "DTI";
	private static Map<String, String> llc = new HashMap<String, String>();
	private static Properties headerInfo = new Properties();
	// private static Logger errorLog=new
	// EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_ERROR).forClass(EventUtil.class).build().getLog();
	// private static Logger debugLog=new
	// EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_DEBUG).forClass(EventUtil.class).build().getLog();
	// private static DmaapDRPub dmaapDRPub;

	public EventUtil() {
		// dmaapDRPub=new DmaapDRPub();
	}

	public static boolean loadConfigs() {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "loadConfigs");
		fullSyncMap = new HashMap<String, String>();
		fullSyncMapWeekly = new HashMap<String, String>();
		tablePrimaryKeyProperties = new HashMap<String, String>();
		entitytypeTableMap = new HashMap<String, String>();
		relationshipConfigMap = new HashMap<String, Map<String, String>>();
		CollectorSyncMap = new HashMap<String, String>();

		// Properties config = new Properties();
		PropertiesConfiguration config = new PropertiesConfiguration();

		InputStream input = null;
		boolean result = true;
		try {

			input = EventUtil.class.getClassLoader().getResourceAsStream("dti.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_FILE_MISSING_ERROR);
				System.exit(1);
			}
			try {
				config.setDelimiterParsingDisabled(true);
				config.load(input);
			} catch (ConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, e.getMessage());
					result = false;
				}
			}
			input = EventUtil.class.getClassLoader().getResourceAsStream("table_header_info.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_FILE_MISSING_ERROR);
				System.exit(1);
			}
			headerInfo.load(input);

			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, e.getMessage());
					result = false;
				}
			}

			String prodConfigPrefix = "";
/*			if (System.getenv("PROD_TARGET_NODE") != null) {
				if (System.getenv("PROD_TARGET_NODE").equalsIgnoreCase("Y")) {
					prodConfigPrefix = "PROD_";
				}
			}
*/
			AAICLIENT = config.getString("AAICLIENT");
			if (AAICLIENT == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "AAICLIENT");
				System.exit(1);
			}
			if (AAICLIENT.equalsIgnoreCase("ueb")) {
				UEBFILTER = config.getString("UEBFILTER");
				if (UEBFILTER == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_DEFAULT_ERROR);
					UEBFILTER = "";
				}
				UEBURL = config.getString(prodConfigPrefix + "UEBURL");
				if (UEBURL == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBURL");
					System.exit(1);
				}
				UEBGROUP = UUID.randomUUID().toString();
				UEBID = "0";

				UEBAPIKEY = config.getString(prodConfigPrefix + "UEBAPIKEY");
				if (UEBAPIKEY == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBAPIKEY");
					System.exit(1);
				}
				UEBAPISECRET = config.getString(prodConfigPrefix + "UEBAPISECRET");
				if (UEBAPISECRET == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBAPISECRET");
					System.exit(1);
				}
				UEBTIMEOUT_MS = config.getString("UEBTIMEOUT_MS");
				if (UEBTIMEOUT_MS == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBTIMEOUT_MS");
					UEBTIMEOUT_MS = "15000";
				}
				UEBLIMIT = config.getString("UEBLIMIT");
				if (UEBLIMIT == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBLIMIT");
					UEBLIMIT = "1000";
				}
			} else if (AAICLIENT.equalsIgnoreCase("dmaap")) {
				UEBURL = config.getString(prodConfigPrefix + "UEBURL");
				if (UEBURL == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"UEBURL");
					System.exit(1);
				}
				DMAAPOUTPUTTMPDIR = config.getString("DMAAPOUTPUTTMPDIR");

				if (DMAAPOUTPUTTMPDIR == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPOUTPUTTMPDIR");
					System.exit(1);
				}
				DMAAPCONSUMERPREFERREDROUTE = config.getString("DMAAPCONSUMERPREFERREDROUTE");
				if (DMAAPCONSUMERPREFERREDROUTE == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPCONSUMERPREFERREDROUTE");
					System.exit(1);
				}
				DMAAPCONSUMERPREFERREDROUTE = System.getenv("DTI") + Constants.FILE_SEPARATOR
						+ DMAAPCONSUMERPREFERREDROUTE;
				ecompLogger.debug("DTI value: " + System.getenv("DTI"));

				DMAAPCONSUMERFILEPATH = config.getString("DMAAPCONSUMERFILEPATH");
				if (DMAAPCONSUMERFILEPATH == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPCONSUMERFILEPATH");
					System.exit(1);
				}
				DMAAPCONSUMERFILEPATHDB = config.getString("DMAAPCONSUMERFILEPATHDB");
				if (DMAAPCONSUMERFILEPATHDB == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPCONSUMERFILEPATHDB");
					System.exit(1);
				}

				DMAAPCONSUMERFILEPATHNARAD = config.getString("DMAAPCONSUMERFILEPATHNARAD");
				if (DMAAPCONSUMERFILEPATHNARAD == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPCONSUMERFILEPATHNARAD");
					System.exit(1);
				}
				DMAAPCONSUMERFILEPATHNARADDB = config.getString("DMAAPCONSUMERFILEPATHNARADDB");
				if (DMAAPCONSUMERFILEPATHNARADDB == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
							"DMAAPCONSUMERFILEPATHNARADDB");
					System.exit(1);
				}

			} else {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_AAI_UEB_DMAAP_MATCH_ERROR);
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_EXCEPTION_ERROR);
						result = false;
					}
				}
				System.exit(1);
			}

			DMAAPCONSUMERFILEPATHSM = config.getString("DMAAPCONSUMERFILEPATHSM");
			if (DMAAPCONSUMERFILEPATHSM == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"DMAAPCONSUMERFILEPATHSM");
				System.exit(1);
			}

			DOMAIN = config.getString("DOMAIN");
			if (DOMAIN == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "DOMAIN");
				System.exit(1);
			}
			DCAEENV = config.getString("dcae-env");
			if (DCAEENV == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "DCAEENV");
				System.exit(1);
			}

			ENABLE_PG_LOAD = config.getString("enable-pg-load");
			if (ENABLE_PG_LOAD == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"ENABLE_PG_LOAD");
				System.exit(1);
			}

			ENABLE_DMAAP_PUBLISH = config.getString("enable-dmaap-publish");
			if (ENABLE_DMAAP_PUBLISH == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"ENABLE_DMAAP_PUBLISH");
				System.exit(1);
			}

			ENABLE_COLLECTOR_EVENTS = config.getString("enable-collector-events");
			if (ENABLE_COLLECTOR_EVENTS == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"ENABLE_COLLECTOR_EVENTS");
				System.exit(1);
			}

			ENABLE_COLLECTOR_EVENTS_NARAD = config.getString("enable-collector-events-narad");
			if (ENABLE_COLLECTOR_EVENTS_NARAD == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"ENABLE_COLLECTOR_EVENTS_NARAD");
				System.exit(1);
			}

			GENERICVNFURL = config.getString("generic-vnf-url");
			if (GENERICVNFURL == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"GENERICVNFURL");
				System.exit(1);
			}

			DCAEENV = config.getString("dcae-env");
			if (DCAEENV == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "DCAEENV");
				System.exit(1);
			}

			DMAAPDRPUBPATH = config.getString("DMAAPDRPUBPATH");
			if (DMAAPDRPUBPATH == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"DMAAPDRPUBPATH");
				System.exit(1);
			}

			DMAAPCONFPATH = config.getString("DMAAPCONFPATH");
			if (DMAAPCONFPATH == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"DMAAPCONFPATH");
				System.exit(1);
			}

			DMAAPOUTPUTDIR = config.getString("DMAAPOUTPUTDIR");
			if (DMAAPOUTPUTDIR == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"DMAAPOUTPUTDIR");
				System.exit(1);
			}

			DBEXCLUDEOBJECTS = config.getString("DBEXCLUDEOBJECTS");
			if (DBEXCLUDEOBJECTS == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"DBEXCLUDEOBJECTS");
				System.exit(1);
			}

			NARADCOLLECTOREVENTS = config.getString("NARADCOLLECTOREVENTS");
			if (NARADCOLLECTOREVENTS == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"NARADCOLLECTOREVENTS");
				System.exit(1);
			}

			COLLECTOREVENTS = config.getString("COLLECTOREVENTS");
			if (COLLECTOREVENTS == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"COLLECTOREVENTS");
				System.exit(1);
			}

			COLLECTORSYNCOBJECTS = config.getString("COLLECTORSYNCOBJECTS");
			if (COLLECTORSYNCOBJECTS == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"COLLECTORSYNCOBJECTS");
				System.exit(1);
			}
			// Get inventory id mapping for this aai object in dti.properties
			String[] arrObjects = COLLECTORSYNCOBJECTS.split(",");
			List<String> lCollObjects = Arrays.asList(arrObjects);
			for (String colObject : lCollObjects) {
				String mapValue = config.getString(colObject);
				if (mapValue != null && !mapValue.isEmpty()) {
					CollectorSyncMap.put(colObject, mapValue);
				}
			}

			CLOUD_REGION_PATH = config.getString("cloud-region-path");
			if (CLOUD_REGION_PATH == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"cloud-region-path");
				return false;
			}

			if (StringUtils.isNotBlank(System.getenv("aai_api_url"))) {
				API_URL = System.getenv("aai_api_url");
			} else if (StringUtils.isNotBlank(System.getenv("narad_api_url"))) {
				API_URL = System.getenv("narad_api_url");
			}
			if (API_URL == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INVALID_ENV_VARIABLE);
				return false;
			}

			SET_TRUSTSTORE = config.getString("enable-set-truststore");
			if (SET_TRUSTSTORE == null) {
				System.out.println("Cannot find SET_TRUSTSTORE.");
			}

			if (Constants.TRUE.equalsIgnoreCase(SET_TRUSTSTORE)) {
				TRUSTSTORE_PATH = config.getString("TRUSTSTORE_PATH");
				if (TRUSTSTORE_PATH == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TRUSTSTORE_PATH_ERROR);
				}
				//TRUSTSTORE_PATH = System.getenv("DTI") + Constants.FILE_SEPARATOR + TRUSTSTORE_PATH;

				TRUSTSTORE_PASSWORD = System.getenv("TRUSTSTORE_PASSWORD");
				if (TRUSTSTORE_PASSWORD == null) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TRUSTSTORE_PASSWORD_ERROR);
				}
			}

			if (StringUtils.isNotBlank(TRUSTSTORE_PASSWORD))
				TRUSTSTORE_PASSWORD = TRUSTSTORE_PASSWORD.trim();

			KEYSTORE_PATH = config.getString("KEYSTORE_PATH");
			if (KEYSTORE_PATH == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"KEYSTORE_PATH");
				return false;
			}
			//KEYSTORE_PATH = System.getenv("DTI") + Constants.FILE_SEPARATOR + KEYSTORE_PATH;
			if (KEYSTORE_PATH.contains(Constants.PATH_BLACKLIST)) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_KEYSTORE_PATH_ERROR);
				return false;
			}

			KEYSTORE_LOCATION = config.getString("KEYSTORE_LOCATION");
			if (KEYSTORE_LOCATION == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"KEYSTORE_LOCATION");
				return false;
			}
			if (KEYSTORE_LOCATION.contains(Constants.PATH_BLACKLIST)) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_KEYSTORE_LOCATION_ERROR);
				return false;
			}

			KEYSTORE_PASSWORD = System.getenv("KEYSTORE_PASSWORD");
			if (KEYSTORE_PASSWORD == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"KEYSTORE_PASSWORD");
				return false;
			}
			KEYSTORE_PASSWORD = KEYSTORE_PASSWORD.trim();

			READ_TIMEOUT = config.getString("read-timeout");
			if (READ_TIMEOUT == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"read-timeout");
				return false;
			}

			CONNECTION_TIMEOUT = config.getString("connection-timeout");
			if (CONNECTION_TIMEOUT == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"connection-timeout");
				return false;
			}

			DOMAIN = config.getString("DOMAIN");
			if (DOMAIN == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "DOMAIN");
				DOMAIN = "";
			}

			weeklySyncDay = config.getString("weeklySyncDay");
			if (weeklySyncDay == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"weeklySyncDay");
				return false;
			}

			if (config.getString("pageSize") != null) {
				pageSize = Integer.parseInt(config.getString("pageSize"));
			} else {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR, "pageSize");
				pageSize = 1000;
			}

			if (config.getString("modelPageSize") != null) {
				modelPageSize = Integer.parseInt(config.getString("modelPageSize"));
			} else {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"modelPageSize");
				modelPageSize = 100;
			}

			CANOPI_FILE_PROCESSED_FOLDER = config.getString("CANOPI_FILE_PROCESSED_FOLDER");
			if (CANOPI_FILE_PROCESSED_FOLDER == null) {
				System.out.println("Cannot find CANOPI_FILE_PROCESSED_FOLDER.");
				return false;
			}

			SAMSUNG_FILE_FOLDER = config.getString("SAMSUNG_FILE_FOLDER");
			if (SAMSUNG_FILE_FOLDER == null) {
				System.out.println("Cannot find SAMSUNG_FILE_FOLDER.");
				return false;
			}

			D2MSN_FILE_FOLDER = config.getString("D2MSN_FILE_FOLDER");
			if (D2MSN_FILE_FOLDER == null) {
				System.out.println("Cannot find D2MSN_FILE_FOLDER.");
				return false;
			}

			config.clear();
			config = new PropertiesConfiguration();
			input = EventUtil.class.getClassLoader().getResourceAsStream("FullSync.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"FullSync.properties");
				return false;
			}
			try {
				config.setDelimiterParsingDisabled(true);
				config.load(input);
			} catch (ConfigurationException e1) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"FullSync.properties");
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, ex.getMessage());
					result = false;
				}
			}

			Iterator<String> it = config.getKeys();
			while (it.hasNext()) {
				String key = (String) it.next();
				fullSyncMap.put(key, config.getString(key));
			}

			config.clear();
			config = new PropertiesConfiguration();
			input = EventUtil.class.getClassLoader().getResourceAsStream("entitytypeTableMap.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"entitytypeTableMap.properties");
				return false;
			}
			try {
				config.setDelimiterParsingDisabled(true);
				config.load(input);
			} catch (ConfigurationException e1) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"entitytypeTableMap.properties");
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, ex.getMessage());
					result = false;
				}
			}

			Iterator<String> it2 = config.getKeys();
			while (it2.hasNext()) {
				String key = (String) it2.next();
				entitytypeTableMap.put(key, config.getString(key));
			}

			config.clear();
			config = new PropertiesConfiguration();
			input = EventUtil.class.getClassLoader().getResourceAsStream("TablePrimaryKeys.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"InvalidateNotReceived.properties");
				return false;
			}
			try {
				config.setDelimiterParsingDisabled(true);
				config.load(input);
			} catch (ConfigurationException e1) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"InvalidateNotReceived.properties");
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, ex.getMessage());
					result = false;
				}
			}

			Iterator<String> it3 = config.getKeys();
			while (it3.hasNext()) {
				String key = (String) it3.next();
				tablePrimaryKeyProperties.put(key, config.getString(key));
			}

			config.clear();
			config = new PropertiesConfiguration();
			input = EventUtil.class.getClassLoader().getResourceAsStream("RelationshipList.properties");
			if (input == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"RelationshipList.properties");
				return false;
			}

			try {
				config.setDelimiterParsingDisabled(true);
				config.load(input);
			} catch (ConfigurationException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_CONFIG_PARAM_MISSING_ERROR,
						"RelationshipList.properties");
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, ex.getMessage());
					result = false;
				}
			}

			Iterator<String> it4 = config.getKeys();
			while (it4.hasNext()) {
				String key = (String) it4.next();

				String value = config.getString(key);
				Map<String, String> keymap = new HashMap<String, String>();
				if (value.equalsIgnoreCase("ALL")) {
					keymap.put("ALL", "ALL");
				} else {
					String[] arr = value.split(",");
					if (arr.length == 0)
						continue;

					for (int i = 0; i < arr.length; i++) {
						String item = arr[i];
						String[] itemArr = item.split(":");
						if (itemArr.length < 2)
							continue;
						keymap.put(itemArr[0], itemArr[1]);
					}
				}
				relationshipConfigMap.put(key, keymap);
			}

		} catch (IOException ex) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, ex.getMessage());
			result = false;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_LOAD_CONFIG_ERROR, e.getMessage());
					result = false;
				}
			}
		}
		return result;

	}

	public boolean sendFullSyncFeedsToVETL(String datetimestamp) {
		if (!Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_DMAAP_PUBLISH)) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DMAAP_DISABLED_DEBUG);
			return true;
		}
		String action = "full";

		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "sendFullSyncFeedsToVETL");
		for (String table : entitytypeTableMap.values()) {
			List<String> lRes = null;
			lRes = tableDump(table, action);
			datetimestamp = "full";
			if (lRes != null && lRes.size() > 0) {
				storeListToFileAndSend(lRes, datetimestamp, table);
			}

		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "sendFullSyncFeedsToVETL");
		return true;
	}

	public boolean sendDatatoVETL(String entityType, String datetimestamp, String action, List<String> data) {
		if (!Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_DMAAP_PUBLISH)) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DMAAP_DISABLED_DEBUG);
			return true;
		}

		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "sendDatatoVETL");
		String table = EventUtil.entitytypeTableMap.get(entityType);
		ArrayList<String> resultList = new ArrayList<String>();
		for (String rec : data) {
			// Logic to remove the timestamp from the data passed to vETL
			String[] arrData = rec.split("#");
			String[] arrData_new = Arrays.copyOfRange(arrData, 0, arrData.length - 1);
			String new_data = StringUtils.join(arrData_new, "#");
			String new_rec = new_data.replace("#", ";");
			// vETL / Vertica needs the action type in lower case
			new_rec += ";" + action.toLowerCase() + ";";
			// add validfrom & validto
			resultList.add(datetimestamp + ";;" + new_rec);
		}
		// List<String> lRes = tableDump(table, datetimestamp, action);
		if (resultList.size() > 0) {
			storeListToFileAndSend(resultList, datetimestamp, table);
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "sendDatatoVETL");
		return true;
	}

	public static ObjectMapper getObjectMapperObject() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		return mapper;
	}

	public static Map<String, String> parseUrl(Map<String, Integer> entities, String url) {
		Map<String, String> urlValues = new HashMap<>();

		Integer getNextValues = 0;
		String[] tempArray;
		tempArray = url.split("/");
		String entityType = "";
		String cloudOwner = "";
		// example subnetUrl =
		// /narad/v1/network/l3-networks/l3-network/718cc7b7-bffc-4a62-88a9-78a2d82c1b2e/subnets/subnet/2a865f9b-48fe-4ec4-b3b7-433e21fa5094
		// map will contain { "l3-network",
		// "718cc7b7-bffc-4a62-88a9-78a2d82c1b2e", "subnet",
		// "2a865f9b-48fe-4ec4-b3b7-433e21fa5094" }
		for (String temp : tempArray) {
			if (entities.containsKey(temp)) {
				entityType = temp;
				if (temp.equalsIgnoreCase("cloud-region")) {
					cloudOwner = "cloud-owner";
					getNextValues = 2;
				} else {
					getNextValues = 1;
				}
				continue;
			}
			try {
				temp = URLDecoder.decode(temp, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (getNextValues > 0) {
				if (cloudOwner.equalsIgnoreCase("cloud-owner")) {
					urlValues.put(cloudOwner, temp);
					cloudOwner = "";
				} else {
					urlValues.put(entityType, temp);
				}
				getNextValues--;
			}
		}

		return urlValues;
	}

	public String getSeedingManager(String hostname) {
		String url = System.getenv("PGJDBC_URL");
		// String url = "jdbc:postgresql://localhost:5432/odcit";
		String user = System.getenv("PGUSERNAME");
		// String user ="postgres";
		String password = System.getenv("PGPASSWORD");
		// String password ="ctiadmin";
		String QUERY_PREFIX = "select * from dti.rt_seeding_manager";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// System.out.println(user+":"+password+":"+url);

			con = DriverManager.getConnection(url, user, password);
			DatabaseMetaData databaseMetaData = con.getMetaData();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DB_USERNAME_DEBUG,
					databaseMetaData.getDriverName(), databaseMetaData.getUserName());
			st = con.createStatement();

			String query = QUERY_PREFIX + " where hostname=?";
			stmt = con.prepareStatement(query);
			stmt.setString(1, hostname);
			rs = stmt.executeQuery();

			ResultSetMetaData rsm = rs.getMetaData();
			int cc = rsm.getColumnCount();

			String row = "";
			while (rs.next()) {

				for (int i = 1; i <= cc; i++) {
					String column = rs.getString(i);
					if (column == null) {
						row += "" + ";";
					} else {
						row += column + ";";
					}
				}
				break;
			}
			return row;
			// System.out.println("resultList:"+ resultList.size());

		} catch (SQLException ex) {
			System.out.println("tableDump:" + ex.getMessage());
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, ex.getMessage());
		} finally {

			closeDBResources_ResultSet(rs);
			closeDBResources_Stat(st);
			closeDBResources_Stmt(stmt);
			closeDBResources_Conn(con);
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "tableDump");
		return "";
	}

	public boolean deleteRealtimeFeeds(String datetimestamp, String entityType) {
		if (!Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_DMAAP_PUBLISH)) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DMAAP_DISABLED_DEBUG);
			return true;
		}
		Boolean status = true;
		ArrayList<String> resultList = new ArrayList<String>();
		String url = System.getenv("PGJDBC_URL");
		// String url = "jdbc:postgresql://localhost:5432/odcit";
		String user = System.getenv("PGUSERNAME");
		// String user ="postgres";
		String password = System.getenv("PGPASSWORD");
		// String password ="ctiadmin";
		String query = "delete from dti.vertica_feed where updated_on < ?";
		if (entityType != null && !entityType.equalsIgnoreCase("")) {
			query = query + "and entity_type = ?";
		}
		Connection con = null;
		// ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// System.out.println(user+":"+password+":"+url);

			con = DriverManager.getConnection(url, user, password);
			DatabaseMetaData databaseMetaData = con.getMetaData();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DB_USERNAME_DEBUG,
					databaseMetaData.getDriverName(), databaseMetaData.getUserName());
			stmt = con.prepareStatement(query);
			stmt.setString(1, datetimestamp);
			if (entityType != null && !entityType.equalsIgnoreCase("")) {
				stmt.setString(2, entityType);
			}
			stmt.executeUpdate();
			/*
			 * st = con.createStatement();
			 * 
			 * rs = stmt.executeQuery();
			 * 
			 * while (rs.next()) {
			 * 
			 * String row = rs.getString(1); resultList.add(row); }
			 */
		} catch (SQLException ex) {
			status = false;
			System.out.println("tableDump:" + ex.getMessage());
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, ex.getMessage());
		} finally {

			closeDBResources_Stmt(stmt);
			closeDBResources_Conn(con);
		}

		return status;
	}

	public boolean sendRealtimeFeedsToVETL(String datetimestamp) {
		if (!Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_DMAAP_PUBLISH)) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DMAAP_DISABLED_DEBUG);
			return false;
		}

		List<String> tableFeeds = null;
		tableFeeds = getRealtimeFeedsToVETL(datetimestamp, "Yes", "");
		if (tableFeeds.size() > 0) {
			for (String rec : tableFeeds) {
				// for (Map.Entry<String, String> entry :
				// entitytypeTableMap.entrySet()) {
				if (entitytypeTableMap.containsKey(rec)) {
					String table = entitytypeTableMap.get(rec);
					List<String> lRes = null;
					lRes = getRealtimeFeedsToVETL(datetimestamp, "", rec);
					if (lRes.size() > 0) {
						if (storeListToFileAndSend(lRes, datetimestamp, table)) {
							ecompLogger.debug("Sent realtime data for entity-type: " + rec);
							if (!deleteRealtimeFeeds(datetimestamp, rec)) {
								ecompLogger.debug("Problem in deleting the records for entity-type : " + rec);
							}
						} else {
							ecompLogger.debug("Sent (" + lRes.size() + ") records to vertica for entity-type: " + rec);
						}
					} else {
						ecompLogger.debug("Skip sending feed file for entity-type: " + rec + " because Size is <= 0");
						return false;
					}
				} else {
					ecompLogger.debug("Skip sending feed file for entity-type: " + rec);
					return false;
				}
			}
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "sendFullSyncFeedsToVETL");
		return true;
	}

	public List<String> getRealtimeFeedsToVETL(String datetimestamp, String needDistinct, String entityType) {
		ArrayList<String> resultList = new ArrayList<String>();
		String url = System.getenv("PGJDBC_URL");
		// String url = "jdbc:postgresql://localhost:5432/odcit";
		String user = System.getenv("PGUSERNAME");
		// String user ="postgres";
		String password = System.getenv("PGPASSWORD");
		// String password ="ctiadmin";
		String query;
		if (needDistinct != null && needDistinct.equalsIgnoreCase("Yes")) {
			query = "select distinct entity_type from dti.vertica_feed where updated_on < ?";
		} else {
			query = "select entity_value from dti.vertica_feed where updated_on < ? and entity_type = ? order by updated_on";
		}
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// System.out.println(user+":"+password+":"+url);

			con = DriverManager.getConnection(url, user, password);
			DatabaseMetaData databaseMetaData = con.getMetaData();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DB_USERNAME_DEBUG,
					databaseMetaData.getDriverName(), databaseMetaData.getUserName());
			stmt = con.prepareStatement(query);
			stmt.setString(1, datetimestamp);
			if (entityType != null && !entityType.equalsIgnoreCase("")) {
				stmt.setString(2, entityType);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {

				String row = rs.getString(1);
				resultList.add(row);
			}
		} catch (SQLException ex) {
			System.out.println("tableDump:" + ex.getMessage());
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, ex.getMessage());
		} finally {

			closeDBResources_ResultSet(rs);
			closeDBResources_Stmt(stmt);
			closeDBResources_Conn(con);
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "tableDump");
		return resultList;

	}

	public List<String> tableDump(String table, String datetimestamp, String action) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "tableDump");
		ArrayList<String> resultList = new ArrayList<String>();
		String url = System.getenv("PGJDBC_URL");
		// String url = "jdbc:postgresql://localhost:5432/odcit";
		String user = System.getenv("PGUSERNAME");
		// String user ="postgres";
		String password = System.getenv("PGPASSWORD");
		// String password ="ctiadmin";
		String QUERY_PREFIX = "select * from dti." + table;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// System.out.println(user+":"+password+":"+url);

			con = DriverManager.getConnection(url, user, password);
			DatabaseMetaData databaseMetaData = con.getMetaData();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DB_USERNAME_DEBUG,
					databaseMetaData.getDriverName(), databaseMetaData.getUserName());
			st = con.createStatement();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_QUERY_DEBUG, table, datetimestamp,
					datetimestamp);
			// rs = st.executeQuery("select * from dti."+table+" where
			// validto='"+datetimestamp+"' or validfrom='"+datetimestamp+"'");

			rs = st.executeQuery(
					"select table_name from information_schema.tables where table_schema='dti' and (table_name like 'rt_%' or table_name like 'narad_%')");
			List<String> tableList = new ArrayList<String>();
			while (rs.next())
				tableList.add(rs.getString(1));

			if (!tableList.contains(table)) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_INVALID_TABLE_NAME_ERROR, table);
				return null;
			}

			if (!datetimestamp.equalsIgnoreCase("full")) {

				String query = QUERY_PREFIX + " where validfrom=?";
				stmt = con.prepareStatement(query);
				stmt.setString(1, datetimestamp);
				// stmt.setString(2, datetimestamp);
			} else {
				String query = QUERY_PREFIX + " where validto is null";
				stmt = con.prepareStatement(query);
			}
			closeDBResources_ResultSet(rs);

			rs = stmt.executeQuery();

			ResultSetMetaData rsm = rs.getMetaData();
			int cc = rsm.getColumnCount();

			while (rs.next()) {

				String row = "";
				for (int i = 1; i <= cc; i++) {
					String column = rs.getString(i);
					if (column == null) {
						row += "" + ";";
					} else {
						row += column + ";";
					}
				}
				row += action + ";";
				resultList.add(row);
			}
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_RESULT_SIZE_DEBUG,
					String.valueOf(resultList.size()));
			// System.out.println("resultList:"+ resultList.size());

		} catch (SQLException ex) {
			System.out.println("tableDump:" + ex.getMessage());
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, ex.getMessage());
		} finally {

			closeDBResources_ResultSet(rs);
			closeDBResources_Stat(st);
			closeDBResources_Stmt(stmt);
			closeDBResources_Conn(con);
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "tableDump");
		return resultList;
	}

	public void closeDBResources_Conn(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, e.getMessage());
			}
		}
	}

	public void closeDBResources_Stat(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, e.getMessage());
			}
		}
	}

	public void closeDBResources_Stmt(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, e.getMessage());
			}
		}
	}

	public void closeDBResources_ResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, e.getMessage());
			}
		}
	}

	public List<String> tableDump(String table, String action) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "tableDump");
		String currentTimeStamp = getCurrentTimestamp();
		ArrayList<String> resultList = new ArrayList<String>();
		String url = System.getenv("PGJDBC_URL");
		// String url = "jdbc:postgresql://localhost:5432/odcit";
		String user = System.getenv("PGUSERNAME");
		// String user ="postgres";
		String password = System.getenv("PGPASSWORD");
		// String password ="ctiadmin";
		String QUERY_PREFIX = "select * from dti." + table;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// System.out.println(user+":"+password+":"+url);

			con = DriverManager.getConnection(url, user, password);
			DatabaseMetaData databaseMetaData = con.getMetaData();
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_DB_USERNAME_DEBUG,
					databaseMetaData.getDriverName(), databaseMetaData.getUserName());
			st = con.createStatement();
			// rs = st.executeQuery("select * from dti."+table+" where
			// validto='"+datetimestamp+"' or validfrom='"+datetimestamp+"'");

			rs = st.executeQuery(
					"select table_name from information_schema.tables where table_schema='dti' and (table_name like 'rt_%' or table_name like 'narad_%')");
			List<String> tableList = new ArrayList<String>();
			while (rs.next())
				tableList.add(rs.getString(1));

			if (!tableList.contains(table)) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_INVALID_TABLE_NAME_ERROR, table);
				return null;
			}

			String query = QUERY_PREFIX;
			stmt = con.prepareStatement(query);
			closeDBResources_ResultSet(rs);

			rs = stmt.executeQuery();

			ResultSetMetaData rsm = rs.getMetaData();
			int cc = rsm.getColumnCount();
			int updatedOnColumnIndex = -1;

			while (rs.next()) {

				String row = "";
				for (int i = 1; i <= cc; i++) {
					// Logic to skip updated_on field from Database.
					if (updatedOnColumnIndex == -1) {
						String columnName = rsm.getColumnName(i);
						if (columnName.equalsIgnoreCase("updated_on")) {
							updatedOnColumnIndex = i;
						}
					}

					if (updatedOnColumnIndex == i)
						continue;

					String column = rs.getString(i);
					if (column == null) {
						row += "" + ";";
					} else {
						row += column + ";";
					}
				}
				// Add action.
				row += action + ";";
				// Add validFrom, ValidTo (which is null)
				resultList.add(currentTimeStamp + ";" + ";" + row);
			}
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_RESULT_SIZE_DEBUG,
					String.valueOf(resultList.size()));
			// System.out.println("resultList:"+ resultList.size());

		} catch (SQLException ex) {
			System.out.println("tableDump:" + ex.getMessage());
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, ex.getMessage());
		} finally {

			closeDBResources_ResultSet(rs);
			closeDBResources_Stat(st);
			closeDBResources_Stmt(stmt);
			closeDBResources_Conn(con);
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "tableDump");
		return resultList;
	}

	public static void safeClose(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_TABLEDUMP_ERROR, e.getMessage());
			}

		}
	}

	public static String getTimestamp(int duration) {
		Date date = DateUtils.addDays(new Date(), -duration);
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		return sdf.format(date);
	}

	public static String getCurrentTimestamp() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public static String getCurrentTimestampMilliSeconds() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public static String DataNullCheck(String title, String value) {

		if (value == null || value.isEmpty()) {
			ecompLogger.debug(title + " value is null or blank");
		}
		return value;
	}

	public static boolean moveFile(String filename, String filepath) {
		boolean fileCopied = true;
		try {
			Path movefrom = FileSystems.getDefault().getPath(Util.safeFileName(filepath));
			Path target = FileSystems.getDefault().getPath(Util.safeFileName(CANOPI_FILE_PROCESSED_FOLDER
					+ fileSeperator + filename + "." + EventUtil.getCurrentTimestampMilliSeconds()));
			Files.copy(movefrom, target);
			Files.delete(movefrom);
		} catch (IOException e) {
			fileCopied = false;
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(), filename);
		}

		return fileCopied;
	}

	public static String unZipIt(String zipFile) {

		String content = "";
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(zipFile))));

			String line;
			while ((line = in.readLine()) != null) {
				content += line;
			}

		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
					"Samsung input zip file is causing problem to read the content");
		}
		return content;
	}

	public static String xmlToJson(String xmlPath) {

		String content = EventUtil.xmlToString(xmlPath);
		int PRETTY_PRINT_INDENT_FACTOR = 4;
		String jsonPrettyPrintString = null;
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(content);
			jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
		} catch (JSONException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
					"D2MSN input xml file is causing problem to read the content");
		}
		return jsonPrettyPrintString;
	}

	public static String xmlToString(String xmlPath) {
		InputStream is = null;
		try {
			is = new FileInputStream(xmlPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			line = buf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			try {
				line = buf.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String fileAsString = sb.toString();
		return fileAsString;
	}

	public static boolean isSamsungFile(String fileName) {
		if (StringUtils.containsIgnoreCase(fileName, SAMSUNG_FILENAME)) {
			return true;
		}
		return false;
	}

	public static boolean isPCRFFile(String fileName) {
		if (StringUtils.containsIgnoreCase(fileName, PCRF_FILENAME)) {
			return true;
		}
		return false;
	}
	
	public static boolean isD2MSNFile(String fileName) {
		if (StringUtils.containsIgnoreCase(fileName, D2MSN_FILENAME)) {
			return true;
		}
		return false;
	}

	public static Map<String, String> listFilesForFolder(final File folder) {

		Map<String, String> fileNameList = new HashMap<String, String>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				continue;
			} else {
				String name = fileEntry.getName();
				String fullFileName = folder + fileSeperator + name;
				fileNameList.put(name, fullFileName);
			}
		}
		return fileNameList;
	}

	public boolean storeListToFileAndSend(List<String> data, String datetimestamp, String table) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "storeListToFileAndSend");
		PrintWriter writer = null;
		String fileName = table;
		if (!datetimestamp.equalsIgnoreCase("full")) {
			datetimestamp = getCurrentTimestamp();
			fileName = fileName + ".tbl." + datetimestamp;
		} else {
			datetimestamp = getCurrentTimestamp();
			fileName = fileName + ".full" + ".tbl." + datetimestamp;
		}
		try {
			String headerDetails = headerInfo.getProperty(table);
			writer = new PrintWriter(System.getenv("DTI") + Constants.FILE_SEPARATOR + EventUtil.DMAAPOUTPUTTMPDIR
					+ Constants.FILE_SEPARATOR + fileName, "UTF-8");
			if (headerDetails != null && !headerDetails.equalsIgnoreCase("")) {
				writer.println(headerDetails);
			}
			for (String rec : data) {
				String new_rec = rec.replace("\\n", "\n");
				writer.println(new_rec);
			}

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG, e.getMessage());
			return false;

		} catch (UnsupportedEncodingException e) {

			// e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG, e.getMessage());
			return false;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		String inputFileName = System.getenv("DTI") + Constants.FILE_SEPARATOR + EventUtil.DMAAPOUTPUTTMPDIR
				+ Constants.FILE_SEPARATOR + fileName;
		String gzFileName = System.getenv("DTI") + Constants.FILE_SEPARATOR + EventUtil.DMAAPOUTPUTTMPDIR
				+ Constants.FILE_SEPARATOR + fileName + Constants.FILE_EXTENSION_GZ;

		boolean retVal = compressGzipFile(inputFileName, gzFileName);
		if (retVal) {
			Path movefrom = FileSystems.getDefault().getPath(Util.safeFileName(gzFileName));
			try {
				Path target = FileSystems.getDefault().getPath(
						Util.safeFileName(System.getenv("DTI") + Constants.FILE_SEPARATOR + EventUtil.DMAAPOUTPUTDIR
								+ Constants.FILE_SEPARATOR + fileName + Constants.FILE_EXTENSION_GZ));
				Files.copy(movefrom, target);

				target = FileSystems.getDefault()
						.getPath(Util.safeFileName(System.getenv("DTI") + Constants.FILE_SEPARATOR
								+ EventUtil.DMAAPOUTPUTDIR + Constants.FILE_SEPARATOR + "archive"
								+ Constants.FILE_SEPARATOR + fileName + Constants.FILE_EXTENSION_GZ));

				Path deletePath = FileSystems.getDefault().getPath(Util.safeFileName(inputFileName));
				Files.delete(deletePath);
				Files.copy(movefrom, target);
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
						fileName);
			}
			/*
			 * if (Constants.TRUE.equalsIgnoreCase(ENABLE_DMAAP_PUBLISH)) {
			 * DmaapDRPub dmaapDRPub = new DmaapDRPub();
			 * dmaapDRPub.publishFile(gzFileName, "");
			 * dmaapDRPub.closeDmaapClient(); } else
			 * ecompLogger.debug(inventoryCollectorMessageEnum.
			 * DTI_EVENT_UTIL_DMAAP_DISABLED_DEBUG);
			 */
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "storeListToFileAndSend");
		return true;
	}

	public static void closeFileInputStream(FileInputStream fis) {
		if (fis != null) {
			try {
				fis.close();
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
			}
		}
	}

	public static String getTemplateContent(String filename) {
		List<String> templateContent = null;
		String template = "";
		try {
			templateContent = Files.readAllLines(Paths.get(Util.safeFileName(filename)), StandardCharsets.UTF_8);
			template = templateContent.get(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return template;
	}

	/*
	 * public static String readFileContent(String filePath) { StringBuilder
	 * contentBuilder = new StringBuilder(); try (Stream<String> stream =
	 * Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) {
	 * stream.forEach(s -> contentBuilder.append(s).append("\n")); } catch
	 * (IOException e) { e.printStackTrace(); } return
	 * contentBuilder.toString(); }
	 */

	private boolean compressGzipFile(String file, String gzipFile) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IN_FUNCTION_DEBUG,
		// "compressGzipFile");
		FileInputStream fis = null;
		FileOutputStream fos = null;
		GZIPOutputStream gzipOS = null;
		boolean result = true;
		try {
			fis = new FileInputStream(ValidationData.cleanPathString(file));
			fos = new FileOutputStream(ValidationData.cleanPathString(Util.safeFileName(gzipFile)));
			gzipOS = new GZIPOutputStream(fos);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				gzipOS.write(buffer, 0, len);
			}

		} catch (IOException e) {

			// e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_COMPRESS_GZIP_FILE_ERROR, e.getMessage());
			result = false;
		} finally {
			// close resources
			if (gzipOS != null) {
				try {
					gzipOS.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_COMPRESS_GZIP_FILE_ERROR,
							e.getMessage());
					// return false;
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_COMPRESS_GZIP_FILE_ERROR,
							e.getMessage());
					// return false;
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_COMPRESS_GZIP_FILE_ERROR,
							e.getMessage());
					// return false;
				}
			}
		}
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_FINISHED_FUNCTION_DEBUG,
		// "compressGzipFile");
		return result;
	}
}
