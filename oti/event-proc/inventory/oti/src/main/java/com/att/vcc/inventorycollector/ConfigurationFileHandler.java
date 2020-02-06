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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.att.vcc.inventorycollector.data.ConfigurationData;
import com.att.vcc.inventorycollector.data.Events;
import com.att.vcc.inventorycollector.domain.SeedingManager;
import com.att.vcc.inventorycollector.domain.DcaeEvent;
import com.att.vcc.inventorycollector.domain.CommunityString;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.messages.inventoryCollectorOperationEnum;
import com.att.vcc.inventorycollector.schema.*;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;
import com.att.vcc.vcctask.RemoteServerGroup;
import com.att.vcc.vcctask.ServerGroupItem;
import com.att.vcc.vcctask.Tasks;
import com.att.vcc.vcctask.TasksItem;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.ecomp.logger.StatusCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * 
 * TODO: Refactor this class into separate util/helper methods.
 * 
 * @author ng346g
 * 
 */
public class ConfigurationFileHandler {

	private static String TEMP_OUT_FILE_PATH;
	private static RESTClient restClient = null;
	private static String domain;
	private static String DCAE_LOCATION;
	private static PropertiesConfiguration dtiProps = new PropertiesConfiguration();
	private static PropertiesConfiguration dtiTaskRegister = new PropertiesConfiguration();
	private static PropertiesConfiguration cc2netid = new PropertiesConfiguration();
	private static PropertiesConfiguration vmPatternMap = new PropertiesConfiguration();
	private static PropertiesConfiguration pnfPatternMap = new PropertiesConfiguration();
	private static PropertiesConfiguration pserverPatternMap = new PropertiesConfiguration();
	private static PropertiesConfiguration vnfPatternMap = new PropertiesConfiguration();
	private static PropertiesConfiguration vnfcPatternMap = new PropertiesConfiguration();
	private static List<String> pserverPatternKeyList = new ArrayList<String>();
	private static List<String> pserverIpKeyList = new ArrayList<String>();
	private static List<String> pnfPatternKeyList = new ArrayList<String>();
	private static List<String> pnfIpKeyList = new ArrayList<String>();
	private static List<String> vnfcPatternKeyList = new ArrayList<String>();
	private static List<String> vnfPatternKeyList = new ArrayList<String>();
	private static List<String> vnfIpKeyList = new ArrayList<String>();
	private static List<String> vnfcIpKeyList = new ArrayList<String>();
	private static List<String> vnfcMobilityIpKeyList = new ArrayList<String>();
	private static List<String> vnfcInMaintKeyList = new ArrayList<>();
	private static String post_url;
	private static EcompLogger ecompLogger;
	private static String datetime;
	private static String serviceType = null;
	private static String pmServiceType = null;
	private static ArrayList<String> fmCollectors = new ArrayList<String>(Arrays.asList("dis", "mib"));

	private static Map<String, String> regionNetIdMappingToa = new HashMap<>();
	private Map<String, Map<String, List<String>>> activeTaskMap = new HashMap<String, Map<String, List<String>>>();
	private DBAdapter dbadapter;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
		regionNetIdMappingToa.put("AP", "MNB-AP");
		regionNetIdMappingToa.put("Asia/Pacific", "MNB-AP");
		regionNetIdMappingToa.put("CA", "MNB-CALA");
		regionNetIdMappingToa.put("CALA", "MNB-CALA");
		regionNetIdMappingToa.put("EMEA", "MNB-EMEA");
		regionNetIdMappingToa.put("MOW", "MNB-MOW");
		regionNetIdMappingToa.put("US", "MNB-US");
		regionNetIdMappingToa.put("USA", "MNB-US");
		regionNetIdMappingToa.put("Canada", "MNB-CAN");
		regionNetIdMappingToa.put("CAN", "MNB-CAN");
	}

	public ConfigurationFileHandler() {
		if (!dtiProps.isEmpty())
			return;

		dbadapter = new DBAdapter();
		if (dbadapter.getDBfactory() == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_DB_FAILURE);
			System.exit(1);
		}
		dbadapter.attachShutDownHook();

		if (Constants.DOMAIN2.equalsIgnoreCase(System.getenv("DCAE_ENV")))
			EcompLogger.setLogContext(inventoryCollectorOperationEnum.DTICreateUpdateDeleteTaskAndPostToOrchOperation,
					EcompLogger.SERVICE_INSTANCE_ID, null, "dti");
		else
			EcompLogger.setLogContext(
					inventoryCollectorOperationEnum.DTICreateUpdateDeleteTaskconfigAndRsyncToLDCAEOperation,
					EcompLogger.SERVICE_INSTANCE_ID, null, "netman");

		EcompLogger.setServiceName("dti");

		String configDir = System.getenv("DTI_CONFIG");
		if (configDir == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
			return;
		}

		FileInputStream dtiStream = null;
		FileInputStream cc2netidStream = null;
		FileInputStream pserverStream = null;
		FileInputStream pnfStream = null;
		FileInputStream vmStream = null;
		FileInputStream vnfcStream = null;
		FileInputStream vnfStream = null;

		try {
			dtiStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/dti.properties"));

			cc2netidStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/CC2Netid.properties"));
			pserverStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/pserver_pattern_map.properties"));
			pnfStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/pnf_pattern_map.properties"));
			vmStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/vm_pattern_map.properties"));
			vnfcStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/vnfc_pattern_map.properties"));
			vnfStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(configDir) + "/vnf_pattern_map.properties"));

			dtiProps.setDelimiterParsingDisabled(true);
			dtiProps.load(dtiStream);

			cc2netid.setDelimiterParsingDisabled(true);
			cc2netid.load(cc2netidStream);

			pserverPatternMap.setDelimiterParsingDisabled(true);
			pserverPatternMap.load(pserverStream);

			pnfPatternMap.setDelimiterParsingDisabled(true);
			pnfPatternMap.load(pnfStream);

			vmPatternMap.setDelimiterParsingDisabled(true);
			vmPatternMap.load(vmStream);

			vnfcPatternMap.setDelimiterParsingDisabled(true);
			vnfcPatternMap.load(vnfcStream);

			vnfPatternMap.setDelimiterParsingDisabled(true);
			vnfPatternMap.load(vnfStream);

		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE, e.getMessage());
			return;
		} catch (ConfigurationException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE, e.getMessage());
			return;
		} finally {
			Util.closeFileInputStream(dtiStream);
			Util.closeFileInputStream(cc2netidStream);
			Util.closeFileInputStream(pserverStream);
			Util.closeFileInputStream(pnfStream);
			Util.closeFileInputStream(vmStream);
			Util.closeFileInputStream(vnfcStream);
			Util.closeFileInputStream(vnfStream);
		}

		for (Iterator<String> iter = pserverPatternMap.getKeys(); iter.hasNext();) {
			String nextValue = iter.next().toString();
			if (nextValue != null && nextValue.startsWith("regex"))
				pserverPatternKeyList.add(nextValue);
		}

		for (Iterator<String> iter = pserverPatternMap.getKeys("ip"); iter.hasNext();)
			pserverIpKeyList.add(iter.next().toString());

		for (Iterator<String> iter = pnfPatternMap.getKeys(); iter.hasNext();) {
			String nextValue = iter.next().toString();
			if (nextValue != null && nextValue.startsWith("regex"))
				pnfPatternKeyList.add(nextValue);
		}

		for (Iterator<String> iter = pnfPatternMap.getKeys("ip"); iter.hasNext();)
			pnfIpKeyList.add(iter.next().toString());

		for (Iterator<String> iter = vnfcPatternMap.getKeys(); iter.hasNext();) {
			String nextValue = iter.next().toString();
			if (nextValue != null && nextValue.startsWith("regex"))
				vnfcPatternKeyList.add(nextValue);
		}

		for (Iterator<String> iter = vnfcPatternMap.getKeys("ip"); iter.hasNext();)
			vnfcIpKeyList.add(iter.next().toString());

		for (Iterator<String> iter = vnfcPatternMap.getKeys("ipMobility"); iter.hasNext();)
			vnfcMobilityIpKeyList.add(iter.next().toString());

		for (Iterator<String> iter = vnfcPatternMap.getKeys("in_maint"); iter.hasNext();)
			vnfcInMaintKeyList.add(iter.next());

		for (Iterator<String> iter = vnfPatternMap.getKeys(); iter.hasNext();) {
			String nextValue = iter.next().toString();
			if (nextValue != null && nextValue.startsWith("regex"))
				vnfPatternKeyList.add(nextValue);
		}

		for (Iterator<String> iter = vnfPatternMap.getKeys("ip"); iter.hasNext();)
			vnfIpKeyList.add(iter.next().toString());

		TEMP_OUT_FILE_PATH = ValidationData
				.cleanPathString(System.getenv("DTI") + dtiProps.getString("temp-file-path"));
		domain = dtiProps.getString("dcae-env");
		DCAE_LOCATION = dtiProps.getString("DCAE_LOCATION");

		restClient = new RESTClient();

	}

	public void generateStaticDcaeEventDB() {
		System.out.println("Entering into generateStaticDcaeEventDB function.");
		DcaeEvent dcaeEvent = null;
		List<DcaeEvent> outputList = dbadapter.getStaticDcaeEvents("NEW");
		Iterator<DcaeEvent> it = outputList.iterator();
		while (it.hasNext()) {
			dcaeEvent = it.next();
			Events events = new Events(dcaeEvent.getDcaetargetname(), dcaeEvent.getDcaetargettype(),
					dcaeEvent.getDcaeserviceaction(), dcaeEvent.getDcaeservicelocation(),
					dcaeEvent.getDcaeservicetype(), dcaeEvent.getDcaetargetprovstatus(),
					dcaeEvent.getDcaetargetinmaint(), dcaeEvent.getDcaetargetisclosedloopdisabled(),
					dcaeEvent.getDcaeserviceinstancemodelinvariantid(),
					dcaeEvent.getDcaeserviceinstancemodelversionid(), dcaeEvent.getDcaegenericvnfmodelinvariantid(),
					dcaeEvent.getDcaegenericvnfmodelversionid(), dcaeEvent.getDcaetargetcollection(),
					dcaeEvent.getDcaetargetcollectionip(), dcaeEvent.getDcaesnmpcommunitystring(),
					dcaeEvent.getDcaesnmpversion(), dcaeEvent.getDcaetargetcloudregionid(),
					dcaeEvent.getDcaetargetcloudregionversion(), dcaeEvent.getDcaetargetservicedescription(),
					new JSONObject(dcaeEvent.getEvent()), new JSONObject(dcaeEvent.getAaiadditionalinfo()));
			try {
				if ((dcaeEvent.getDcaeserviceaction() != null) && (dcaeEvent.getDcaeserviceaction().length()>0)) {
					sendRequestToDTIHandler(events, dcaeEvent.getDcaeserviceaction(), "Y");
				}
				else {
					sendRequestToDTIHandler(events, Constants.UPDATE, "Y");
				}					
			} catch (DTIException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_FAILURE,
						"sendRequestToDTIHandler failed");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void seedingManagerEvent(String hostname, String changetype, SeedingManager seedingmanagerpojo,
			ConfigurationData configurationDataOrg) {
		System.out.println("Enteringinto seedingmanagerevent ");
		ConfigurationData configData = null;
		if(configurationDataOrg != null) {
			configData = configurationDataOrg;
		} else {
			configData = new ConfigurationData();
		}
		SeedingManager seedingManager = null;
		String action = Constants.UPDATE;
		if (Constants.DELETE.equalsIgnoreCase(changetype)) {
			seedingManager = seedingmanagerpojo;
			action = Constants.DELETE;
		} else {
			List<SeedingManager> outputList = dbadapter.queryHostname(hostname);
			Iterator<SeedingManager> it = outputList.iterator();
			while (it.hasNext()) {
				seedingManager = it.next();
			}
		}

		if (seedingManager != null && seedingManager.getHostname() != null) {
			configData.setDeviceName(hostname);
			configData.setAction(action);
			configData.setTargetCollectionIp(seedingManager.getIcmpip());
			configData.setIpAddress(seedingManager.getIcmpip());
			configData.setCommunityString(seedingManager.getCommunitystring());
			configData.setSnmpVersion(seedingManager.getSnmpip());
			configData.setDesigntype(seedingManager.getDesigntype());
			configData.setLocationId(seedingManager.getLocation());
			configData.setVnfType(seedingManager.getDevicetype());
			configData.setFunctionId(seedingManager.getFunctioncode());
			configData.setOutputobject(seedingManager.getOutputobjects());
			configData.setDevicechangetimestamp(seedingManager.getDevicechangetimestamp());
			configData.setIcmpintervalclass(seedingManager.getIcmpintervalclass());
			configData.setFmmibpollerintervalclass(seedingManager.getFmmibpollerintervalclass());
			if (changetype != null && !changetype.equalsIgnoreCase("")) {
				configData.setChangetype(changetype);
			} else {
				configData.setChangetype(seedingManager.getChangetype());
			}
			JSONObject jsonobject = new JSONObject();
			try {
				ecompLogger.debug("Creating DCAE event for mib poller...");
				sendEventsForDisMib("mib", configData, jsonobject, Constants.GENERIC_VNF_ENTITY);
			} catch (DTIException e) {
				e.printStackTrace();
			}
			try {
				ecompLogger.debug("Creating DCAE event for discovery poller...");
				sendEventsForDisMib("dis", configData, jsonobject, Constants.GENERIC_VNF_ENTITY);
			} catch (DTIException e) {
				e.printStackTrace();
			}
		} else {
			ecompLogger.debug("Skipping for hostname: " + hostname);
		}
	}

	public String groupTargetType(String targetType) {
		String groupTargetType = null;
		Properties groupTargetTypes = new Properties();

		String dir = System.getenv("DTI_CONFIG");
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(
					ValidationData.cleanPathString(Util.safeFileName(dir) + "/group_target_type.properties"));

			groupTargetTypes.load(inStream);
			if (groupTargetTypes.containsKey(targetType)) {
				groupTargetType = groupTargetTypes.getProperty(targetType);
			}
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_REF_FILE_LOAD_FAILURE,
					"dti_active_task.properties");
		} finally {
			Util.closeFileInputStream(inStream);
		}
		return groupTargetType;
	}

	public void storeDcaeEvent(Events event, String dcaeEventStatus) {
		System.out.println("Entering into storeDcaeEvent");
		if (datetime == null) {
			datetime = EventUtil.getCurrentTimestamp();
		}
		String data = event.getDcaeTargetName() + "^" + event.getDcaeTargetType() + "^" + event.getDcaeServiceLocation()
				+ "^" + event.getDcaeServiceAction() + "^" + event.getDcaeTargetProvStatus() + "^"
				+ event.getDcaeServiceType() + "^" + event.getDcaeTargetInMaint() + "^"
				+ event.getDcaeTargetIsClosedLoopDisabled() + "^" + event.getDcaeServiceInstanceModelInvariantId() + "^"
				+ event.getDcaeServiceInstanceModelVersionId() + "^" + event.getDcaeGenericVnfModelInvariantId() + "^"
				+ event.getDcaeGenericVnfModelVersionId() + "^" + event.getDcaeTargetCollection() + "^"
				+ event.getDcaeTargetCollectionIp() + "^" + event.getDcaeSnmpCommunityString() + "^"
				+ event.getDcaeSnmpVersion() + "^" + event.getDcaeTargetCloudRegionId() + "^"
				+ event.getDcaeTargetCloudRegionVersion() + "^" + event.getDcaeTargetServiceDescription() + "^"
				+ event.getEvent() + "^" + event.getAaiAdditionalInfo() + "^Y^" + dcaeEventStatus + "^^^" + datetime;
		dbadapter.processDcaeEvent(data);
		System.out.println("Done");
	}

	protected String getLocationDetails(String locationLink) {
		String location = "";
		String[] tempArray;
		if (locationLink != null && !locationLink.equalsIgnoreCase("")) {
			tempArray = locationLink.split("/");
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
					"Length" + tempArray.length);
			// example url value :
			// /aai/v14/cloud-infrastructure/complexes/complex/AMSTERNL
			// We need to get AMSTERNL
			location = tempArray[tempArray.length - 1];
		}
		return location;
	}

	public ConfigurationData setCommunityStringVersion(ConfigurationData configData, String targetType,
			String deviceName) {
		CommunityString communityStringObj = null;
		List<CommunityString> outputList = dbadapter.getCommunityString(targetType, deviceName);
		Iterator<CommunityString> it = outputList.iterator();
		if (it.hasNext()) {
			communityStringObj = it.next();
			configData.setSnmpCommunityString(communityStringObj.getCommunitystring());
			configData.setSnmpVersion(communityStringObj.getSnmpversion());
		}

		return configData;
	}

	private boolean validateProvStatus(String provStatus) {
		boolean status = false;
		if (provStatus == null)
			provStatus = "";

		List<String> provStatusList = new ArrayList<String>(
				Arrays.asList(dtiProps.getString("prov-status").split(",")));
		if (!provStatus.equalsIgnoreCase("") && provStatusList.contains(provStatus)) {
			status = true;
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROVSTATUS_FAILURE, "AAI Event",
					provStatus);
		}
		return status;
	}
	
	private boolean isFlexreach( String nodeType){
		String[] flexreachList = (vmPatternMap.getString("isFlexreach")).split(",");	
		return Arrays.asList(flexreachList).contains(nodeType);				
	}
	
	

	public void getAAIUpdates(String jsonMessage) throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "getAAIUpdates");
		if (StringUtils.isBlank(jsonMessage) || jsonMessage.contains("Error") || jsonMessage.contains("Exception"))
			return;

		datetime = EventUtil.getCurrentTimestamp();
		JSONObject eventJSONObject = null;
		eventJSONObject = new JSONObject(jsonMessage);
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_RECEIVED_AAI_EVENT, jsonMessage);
		ObjectMapper mapper = new ObjectMapper();
		// mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		JSONArray canopiEventArray = eventJSONObject.optJSONArray("NT_LTE_ENODEB_INVENTORY");
		JSONObject D2MSNEvent = eventJSONObject.optJSONObject("topology.IManagedElementSnapshot");
		JSONArray PCRFEventArray = eventJSONObject.optJSONArray("D1_Inventory");
		if (canopiEventArray != null) {

			for (int j = 0; canopiEventArray != null && j < canopiEventArray.length(); j++) {
				JSONObject sumsungEvent = canopiEventArray.optJSONObject(j);
				ConfigurationData configData = new ConfigurationData();

				try {
					String emsFqdn = "";
					String emsName = "";
					if (sumsungEvent.has("EMS_NAME")) {
						emsFqdn = EventUtil.DataNullCheck("SAMSUNG EVENT EMS NAME", sumsungEvent.optString("EMS_NAME"));
						emsName = emsFqdn.split("\\.")[0];
					}

					if (emsName.matches(vmPatternMap.getString("samsung"))) {
						configData.setIpAddress(
								EventUtil.DataNullCheck("SAMSUNG EVENT EMS_IP", sumsungEvent.optString("EMS_IP")));
						configData.setTargetType("usm-enb");
						configData.setTargetCollectionIp(
								EventUtil.DataNullCheck("SAMSUNG EVENT EMS_IP", sumsungEvent.optString("EMS_IP")));
						configData.setProvStatus("PROV");
						configData.setIsInMaint("false");
						configData.setCloudRegionId(EventUtil.DataNullCheck("SAMSUNG EVENT LOCATION",
								sumsungEvent.optString("EMS_LOCATION")));
						configData.setDeviceName(emsName);
						String location = dbadapter.getServiceLocation(EventUtil.DataNullCheck("SAMSUNG EVENT LOCATION",
								sumsungEvent.optString("EMS_LOCATION")));
						configData.setLocationId(location);
						configData.setVnfType(
								EventUtil.DataNullCheck("SAMSUNG EVENT TYPE", sumsungEvent.optString("TYPE")));
						configData.setAction(Constants.UPDATE);
						configData.setNodeType("123");
						pmServiceType = "MOBILITY-NGFW";
						createDcaeEvent(configData, eventJSONObject);
					}
				} catch (Exception e) {
					continue;
				}
			}
		} else if (D2MSNEvent != null){
			JSONObject elements = D2MSNEvent.optJSONObject("Elements");
			JSONArray IManagedElements = elements.optJSONArray("IManagedElement");
			ConfigurationData configData = new ConfigurationData();
			
			for (int j = 0; IManagedElements != null && j < IManagedElements.length(); j++) {
							
				JSONObject IManagedElement = IManagedElements.optJSONObject(j);			
				JSONObject elementTypeJson = IManagedElement.optJSONObject("ElementType");
				JSONObject IPJson = IManagedElement.optJSONObject("IP");
				JSONObject sysNameJson = IManagedElement.optJSONObject("SysName");

				String elementType=elementTypeJson.optString("content");				
                if ((elementType.contains("ACCTON") && elementType.contains("ACCESS MSN")) || (elementType.contains("ACCTON") && elementType.contains("HUB MSN"))) {
                	
                	configData.setIpAddress(IPJson.optString("content"));
    				configData.setTargetType("d2msn");
    				configData.setTargetCollectionIp(IPJson.optString("content"));
    				configData.setProvStatus("PROV");
    				configData.setIsInMaint("false");
    				configData.setCloudRegionId(sysNameJson.optString("content"));
    				configData.setDeviceName(sysNameJson.optString("content"));    				
    				configData.setVnfType("SWITCH");
    				configData.setLocationId(sysNameJson.optString("content"));
    				configData.setAction(Constants.UPDATE);
    				configData.setNodeType("d2msn");
    				pmServiceType = "D2CSBH";
    				createDcaeEvent(configData, eventJSONObject);
				}               								
			}
						
		} else if (PCRFEventArray != null){
			for (int j = 0; PCRFEventArray != null && j < PCRFEventArray.length(); j++) {
				JSONObject PCRFEvent = PCRFEventArray.optJSONObject(j);
				ConfigurationData configData = new ConfigurationData();
				
				try {
				String inventoryType=PCRFEvent.optString("Type");
				String zone=PCRFEvent.optString("SERVICE_ZONE");
				String ip=PCRFEvent.optString("MANAGEMENT_ADDRESS");
				String deviceName=PCRFEvent.optString("DEVICE_NAME");
				if(inventoryType.equalsIgnoreCase("PCRF CONTROL CTR VM SHELF") && zone.equalsIgnoreCase("NEO") && ip!=null){					
					    configData.setDeviceName(deviceName);	
					    configData.setIpAddress(ip);
						configData.setTargetType("pcrf-pcc");
						configData.setTargetCollectionIp(ip);
						configData.setProvStatus("PROV");
						configData.setIsInMaint("false");						
//						configData.setLocationId(location);
						configData.setVnfType(inventoryType);
						configData.setAction(Constants.UPDATE);
						configData.setNodeType("0000");
						pmServiceType = "NeoConsumer";
						createDcaeEvent(configData, eventJSONObject);
				 }
				} catch (Exception e) {
					continue;
				}					
			}
			
		} else if (eventJSONObject != null) {
			JSONObject header = eventJSONObject.optJSONObject("event-header");
			JSONObject entity = eventJSONObject.optJSONObject("entity");
			String entityType = header == null ? null : header.getString("entity-type");
			String eventDomain = header == null ? null : header.getString("domain");
			String entityValue = entity.toString();

			// remove /aai/v13/ from API_URL
			String appendUrl = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9);

			if (entity == null || header == null || entityType == null) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROCESS_EVENT_FAILURE);
				return;
			}

			/*
			 * if (entity.has("prov-status") ||
			 * !dtiProps.getString("prov-status").contains(entity.optString(
			 * "prov-status"))) {
			 * ecompLogger.debug(inventoryCollectorMessageEnum.
			 * DTI_CONFIG_FILE_HANDLER_PROVSTATUS_FAILURE, "AAI Event",
			 * entity.optString("prov-status")); return; }
			 */

			String vName = null;
			vName = entity.optString("vnf-name");
			if (Constants.PNF_ENTITY.equalsIgnoreCase(entityType)) {
				vName = entity.optString("pnf-name");
			} else if (Constants.PSERVER_ENTITY.equalsIgnoreCase(entityType)) {
				vName = entity.optString("hostname");
			} else if (Constants.VNFC_ENTITY.equalsIgnoreCase(entityType)) {
				vName = entity.optString("vnfc-name");
			}
			if (!Constants.VSERVER_ENTITY.equalsIgnoreCase(entityType) && StringUtils.isBlank(vName)) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NAME_FAILURE, "AAI Event",
						entityType, vName);
				return;
			}

			JSONObject relationListObject = null;
			String[] relationData = new String[2];
//			String vnfCategory = dtiProps.getString("vnf-catg");
//			String vmCategory = dtiProps.getString("vm-catg");
//			String vnfcCategory = dtiProps.getString("vnfc-catg");
//			ecompLogger.debug(
//					"vnfCategory " + vnfCategory + " ; VM Category " + vmCategory + " ; vnfcCategory " + vnfcCategory);
//			List<String> vnfCategoryList = new ArrayList<String>(Arrays.asList(vnfCategory.split(",")));
//			List<String> vmCategoryList = new ArrayList<String>(Arrays.asList(vmCategory.split(",")));
//			List<String> vnfcCategoryList = new ArrayList<String>(Arrays.asList(vnfcCategory.split(",")));

			ConfigurationData configData = new ConfigurationData();
			configData.setDeviceName(vName);
			configData.setAction(header.getString(Constants.ACTION));
			configData.setProvStatus(entity.optString("prov-status"));
			configData.setNetworkId(dtiProps.getString(entity.optString("as-number")));

			// Validate prov-status for entity other than vserver
			// i.e., pserver, generic-vnf, vnfc, pnf & vce
			if (!Constants.VSERVER_ENTITY.equalsIgnoreCase(entityType)) {
				if (!validateProvStatus(configData.getProvStatus())) {
					return;
				}
			}

			// Set inmaint flag
			if (entity.has("in-maint")) {
				if (entity.getBoolean("in-maint"))
					configData.setIsInMaint(Constants.TRUE);
				else
					configData.setIsInMaint(Constants.FALSE);
			}

			if (Constants.PNF_ENTITY.equalsIgnoreCase(entityType)) {
				Pnf pnf = getPnfSchemaObject(entityValue);

				String vnfType = pnf.getEquipType();
				configData.setVnfType(vnfType);
				vName = pnf.getPnfName();
				Boolean foundMatch = false;

				for (String e : pnfPatternKeyList) {
					foundMatch = false;
					if (!e.startsWith("regex"))
						continue;

					int index = 6;
					if (e.startsWith("regex_")) {
						index = 10;
						if (vName.matches(e.substring(index))) {
							foundMatch = true;
							configData.setNodeType(pnfPatternMap.getString(e));
						}
					}

					String collectorType = e.substring(index - 4, index - 1);

					if (!foundMatch) {
						ecompLogger.debug("Doesnot match the pattern : " + e);
						continue;
					} else {
						ecompLogger.debug("Collector type: " + collectorType);
					}
					ecompLogger.debug("Pnf nodetype : " + configData.getNodeType());

					for (String e1 : pnfIpKeyList)
						if (e1.substring(3).contains(configData.getNodeType())) {
							configData.setIpAddress(entity.optString(pnfPatternMap.getString(e1)));
							break;
						}

					configData.setIsClosedLoopDisabled(Constants.FALSE);

					if (StringUtils.isBlank(configData.getIpAddress())) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
								"AAI Event IP Address missing", entityType, configData.getDeviceName(),
								configData.getIpAddress());
						continue;
					}
					configData.setTargetCollectionIp(configData.getIpAddress());
					String nodeType = configData.getNodeType();
					configData.setNodeType(nodeType);

					// Populate dcae_target_in_maint
					if (pnf.isInMaint()) {
						configData.setIsInMaint(Constants.TRUE);
					} else {
						configData.setIsInMaint(Constants.FALSE);
					}

					// Populate NetID for pm collector
					String netId = getNetidFromCC(vName);
					configData.setNetworkId(netId);

					// Populate dcae_service_location
					RelationshipList relList = pnf.getRelationshipList();
					configData.setLocationId(getLocationFromRelationshipList(relList));

					if (StringUtils.isNotBlank(pnf.getEquipType())) {
						configData.setVnfType(pnf.getEquipType());
					}

					configData.setIsClosedLoopDisabled(Constants.FALSE);
					configData.setIsDeviceforCollection(Constants.TRUE);

					if (!StringUtils.isNotBlank(configData.getLocationId())) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
								"AAI Event Location mapping missing", entityType, configData.getDeviceName(),
								configData.getIpAddress());
						continue;
					}

					String nodetypeTarget = pnfPatternMap.getString(configData.getNodeType());
					if (nodetypeTarget != null) {
						String[] targetTypeArr = nodetypeTarget.split("-", 2);
						if (targetTypeArr.length > 1) {
							configData.setTargetType(nodetypeTarget);
						} else {
							configData.setFunctionId(vName.substring(vName.length() - 3));
							configData.setTargetType(vName.substring(vName.length() - 3) + '-' + nodetypeTarget);
						}
					}

					if (ConfigurationFileHandler.fmCollectors.contains(collectorType)) {
						if (Constants.DELETE.equalsIgnoreCase(configData.getAction())) {
							configData.setChangetype(Constants.DELETE);
						} else {
							configData.setChangetype(Constants.ADD);
						}
						// create DCAE event for FM collectors.
						sendEventsForDisMib(collectorType, configData, eventJSONObject, Constants.PNF_ENTITY);
					} else {
						createDcaeEvent(configData, eventJSONObject);
					}
				}
			} else if (Constants.PSERVER_ENTITY.equalsIgnoreCase(entityType)) {
				Pserver pserver = getPserverSchemaObject(entityValue);

				String vnfType = pserver.getEquipType();
				configData.setVnfType(vnfType);
				vName = pserver.getHostname();
				String location = "";
				Boolean foundMatch = false;

				for (String e : pserverPatternKeyList) {
					foundMatch = false;
					if (!e.startsWith("regex"))
						continue;

					int index = 6;
					if (e.startsWith("regex_")) {
						index = 10;
						if (vName.matches(e.substring(index))) {
							foundMatch = true;
							configData.setNodeType(pserverPatternMap.getString(e));
						}
					}

					String regexColVal = e.substring(index - 4, index - 1);

					if (!foundMatch) {
						ecompLogger.debug("Doesnot match the pattern : " + e);
						continue;
					} else {
						ecompLogger.debug("Collector type: " + regexColVal);
					}
					ecompLogger.debug("Pserver nodetype : " + configData.getNodeType());

					for (String e1 : pserverIpKeyList)
						if (e1.substring(3).contains(configData.getNodeType())) {
							configData.setIpAddress(entity.optString(pserverPatternMap.getString(e1)));
							break;
						}

					configData.setIsClosedLoopDisabled(Constants.FALSE);

					if (StringUtils.isBlank(configData.getIpAddress())) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
								"AAI Event IP Address missing", entityType, configData.getDeviceName(),
								configData.getIpAddress());
						continue;
					}
					configData.setTargetCollectionIp(configData.getIpAddress());
					String nodeType = configData.getNodeType();
					configData.setNodeType(nodeType);

					RelationshipList relList = pserver.getRelationshipList();
					String genericVnfURL = "";
					String vserverURL = "";
					String cloudRegionURL = "";
					String complexURL = "";
					if (relList != null) {
						List<Relationship> lRelationship = relList.getRelationship();
						for (Relationship r : lRelationship) {
							if (r.getRelatedTo().equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)) {
								genericVnfURL = r.getRelatedLink();
							} else if (r.getRelatedTo().equalsIgnoreCase(Constants.VSERVER_ENTITY)) {
								vserverURL = r.getRelatedLink();
							} else if (r.getRelatedTo().equalsIgnoreCase(Constants.CLOUD_REGION_ENTITY)) {
								cloudRegionURL = r.getRelatedLink();
							} else if (r.getRelatedTo().equalsIgnoreCase(Constants.COMPLEX_ENTITY)) {
								complexURL = r.getRelatedLink();
							}
						}
					}
					location = getLocationFromRelationshipList(relList);
					configData.setLocationId(location);

					String complexRegion = "";
					if (complexURL != null) {
						complexRegion = getComplexRegion(appendUrl + complexURL);
						if (ConfigurationFileHandler.regionNetIdMappingToa.containsKey(complexRegion)) {
							configData.setNetworkId(ConfigurationFileHandler.regionNetIdMappingToa.get(complexRegion));
						} else {
							configData.setNetworkId("");
						}
					}

					if (!StringUtils.isNotBlank(configData.getLocationId())) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
								"AAI Event Location mapping missing", entityType, configData.getDeviceName(),
								configData.getIpAddress());
						continue;
					}
					if (!cloudRegionURL.equalsIgnoreCase("")) {
						configData.setCloudRegionVersion(getCloudRegionVersion(appendUrl + cloudRegionURL));
					} else if (!vserverURL.equalsIgnoreCase("")) {
						configData.setCloudRegionId(vserverURL);
						// populate cloud-region-version
						String[] cloudRegionInfo = vserverURL.split("tenants", 2);
						configData.setCloudRegionVersion(getCloudRegionVersion(appendUrl + cloudRegionInfo[0]));
					}

					if (!genericVnfURL.equalsIgnoreCase("")) {
						String responseVnf = restClient.retrieveAAIObject(genericVnfURL);
						if (StringUtils.isNotBlank(responseVnf)) {
							GenericVnf gvnfObj = getGenericVnfSchemaObject(responseVnf);
							if (gvnfObj != null) {

								configData.setVnfType(gvnfObj.getVnfType());
								configData.setGenerciVnfModelInvariantId(gvnfObj.getModelInvariantId());
								configData.setGenerciVnfModelVersionId(gvnfObj.getModelVersionId());
								RelationshipList vnfRelList = gvnfObj.getRelationshipList();
								String serviceInstanceURL = "";
								if (vnfRelList != null) {
									List<Relationship> lRelationship = vnfRelList.getRelationship();
									for (Relationship r : lRelationship) {
										if (r.getRelatedTo().equalsIgnoreCase(Constants.SERVICE_INSTANCE_ENTITY)) {
											serviceInstanceURL = appendUrl + r.getRelatedLink();
											List<RelationshipData> lRelShipData = r.getRelationshipData();
											for (RelationshipData relData : lRelShipData) {
												if (relData.getRelationshipKey()
														.equalsIgnoreCase("service-subscription.service-type")) {
													serviceType = relData.getRelationshipValue();
													pmServiceType = serviceType;
													configData.setServiceDescription(serviceType);
													break;
												}
											}
											break;
										}
									}
								}

								if (!serviceInstanceURL.equalsIgnoreCase("")) {
									String siResponse = restClient.retrieveAAIObject(serviceInstanceURL);
									if (StringUtils.isNotBlank(siResponse)) {
										ServiceInstance serviceInstance = getServiceInstanceSchemaObject(siResponse);
										configData.setServiceInstanceModelInvariantId(
												serviceInstance.getModelInvariantId());
										configData
												.setServiceInstanceModelVersionId(serviceInstance.getModelVersionId());
									}
								}
							}
						}
					}
					String nodetypeTarget = pserverPatternMap.getString(configData.getNodeType());
					if (nodetypeTarget != null) {
						String[] targetTypeArr = nodetypeTarget.split("-", 2);
						if (targetTypeArr.length > 1) {
							configData.setTargetType(nodetypeTarget);
						} else {
							configData.setTargetType(vName.substring(vName.length() - 3) + '-' + nodetypeTarget);
						}
					}

					createDcaeEvent(configData, eventJSONObject);
				}
			} else if (Constants.GENERIC_VNF_ENTITY.equalsIgnoreCase(entityType) 
					|| Constants.VCE_ENTITY.equalsIgnoreCase(entityType)) {				
			
				configData.setIpAddress(entity.optString("ipv4-oam-address"));
				configData.setNodeType(entityType.toUpperCase());
				vName = configData.getDeviceName();
				Boolean foundMatch = false;

				if (Constants.GENERIC_VNF_ENTITY.equalsIgnoreCase(entityType)) {
					GenericVnf genericVnfObj = getGenericVnfSchemaObject(entityValue);
					String vnfType = genericVnfObj.getVnfType();
					configData.setVnfType(vnfType);
					String vnfId = genericVnfObj.getVnfId();
					vName = genericVnfObj.getVnfName();

					for (String e : vnfPatternKeyList) {
						foundMatch = false;
						if (!e.startsWith("regex"))
							continue;

						int index = 6;
						if (e.startsWith("regex_")) {
							index = 10;
							if (vName.matches(e.substring(index))) {
								foundMatch = true;
								configData.setNodeType(vnfPatternMap.getString(e));
							}
						}

						if (e.startsWith("regex_vnftype")) {
							index = 18;
							if (vnfType.matches(e.substring(index))) {
								foundMatch = true;
								configData.setNodeType(vnfPatternMap.getString(e));
							}
						}

						String regexColVal = e.substring(index - 4, index - 1);

						if (!foundMatch) {
							ecompLogger.debug("Doesnot match the pattern : " + e);
							continue;
						} else {
							ecompLogger.debug("Collector type: " + regexColVal);
						}
						ecompLogger.debug("Generic VNF nodetype : " + configData.getNodeType());

						for (String e1 : vnfIpKeyList)
							if (e1.substring(3).contains(configData.getNodeType())) {
								configData.setIpAddress(entity.optString(vnfPatternMap.getString(e1)));
								break;
							}

						if (genericVnfObj.isIsClosedLoopDisabled())
							configData.setIsClosedLoopDisabled(Constants.TRUE);
						else
							configData.setIsClosedLoopDisabled(Constants.FALSE);

						if (StringUtils.isBlank(configData.getIpAddress()) && !vnfPatternMap.getString("noIPVNF").contains(configData.getNodeType())) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"AAI Event IP Address missing", entityType, configData.getDeviceName(),
									configData.getIpAddress());
							continue;
						}
						configData.setTargetCollectionIp(configData.getIpAddress());
						String nodeType = configData.getNodeType();
						configData.setNodeType(nodeType);

						RelationshipList relList = genericVnfObj.getRelationshipList();
						String pserverUrl = "";
						String vserverUrl = "";
						if (relList != null) {
							List<Relationship> lRelationship = relList.getRelationship();
							for (Relationship r : lRelationship) {
								if (r.getRelatedTo().equalsIgnoreCase(Constants.PSERVER_ENTITY)) {
									pserverUrl = r.getRelatedLink();
								} else if (r.getRelatedTo().equalsIgnoreCase(Constants.VSERVER_ENTITY)) {
									vserverUrl = r.getRelatedLink();
								}
							}
						}

						if (!pserverUrl.equalsIgnoreCase("")) {
							configData.setLocationId(getLocationDetailsFromPserver(appendUrl + pserverUrl));
						}

						configData.setGenerciVnfModelInvariantId(genericVnfObj.getModelInvariantId());
						configData.setGenerciVnfModelVersionId(genericVnfObj.getModelVersionId());

						if (!vserverUrl.equalsIgnoreCase("")) {
							configData.setCloudRegionId(getCloudRegionId(vserverUrl));
							// populate cloud-region-version
							String[] cloudRegionInfo = vserverUrl.split("tenants", 2);
							configData.setCloudRegionVersion(getCloudRegionVersion(appendUrl + cloudRegionInfo[0]));
							if (!StringUtils.isNotBlank(configData.getLocationId())) {
								configData.setLocationId(getLocationDetailsFromVserver(appendUrl + vserverUrl));
							}
						}

						if (!StringUtils.isNotBlank(configData.getLocationId())) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"AAI Event Location mapping missing", entityType, configData.getDeviceName(),
									configData.getIpAddress());
							continue;
						}

						relationData = getRelationshipData(entity.optJSONObject("relationship-list"),
								"service-instance", "service-subscription.service-type");
						if (relationData != null && relationData[1] != null) {
							serviceType = relationData[1];
							configData.setServiceDescription(relationData[1]);
						}

						relationData = getRelationshipData(entity.optJSONObject("relationship-list"),
								"service-instance", "service-instance.service-instance-id");
						if (relationData != null && relationData[0] != null) {
							String response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
							if (StringUtils.isNotBlank(response)) {
								ServiceInstance serviceInstance = getServiceInstanceSchemaObject(response);
								configData.setServiceInstanceModelInvariantId(serviceInstance.getModelInvariantId());
								configData.setServiceInstanceModelVersionId(serviceInstance.getModelVersionId());
							}
						}
						configData.setNetworkId(getNetidFromCC(vName));
						final String regex = "([Rr][vV][1-7])$";
						final Pattern pattern = Pattern.compile(regex);
						final Matcher matcher = pattern.matcher(vName);
						final String regexVmme = "([Mm][Mm][Ee][Xx][0-9]{2})$";
						final Pattern patternVmme = Pattern.compile(regexVmme);
						final Matcher matcherVmme = patternVmme.matcher(vName);

						String callMibDis = "N";
						String callStatusPoller = "N";
						String pmCollections = "N";
						if (vName.endsWith("vn2") || vName.endsWith("vn3")) {
							configData.setFunctionId(vName.substring(vName.length() - 3));
						} else if (vName.endsWith("me6")) {
							configData.setFunctionId("me6");
						} else if (vName.endsWith("vbc")) {
							configData.setFunctionId("vbc");
						} else if (configData.getVnfType().equalsIgnoreCase("HP")) {
							configData.setFunctionId("HP");
						} else if (configData.getVnfType().equalsIgnoreCase("HG")) {
							configData.setFunctionId("HG");
						} else if (matcher.find()) {
							configData.setFunctionId(matcher.group(1));
						} else if (matcherVmme.find()) {
							configData.setNeVersion(populateNeVersion(entity));
							configData.setTargetType(vnfPatternMap.getString(configData.getNodeType()));
						}

						if (dtiProps.getString("SEEDINGMANAGERCOLLECTORS").contains(regexColVal)) {
							callMibDis = "Y";
						} else if (dtiProps.getString("FMCOLLECTORS").contains(regexColVal)) {
							callStatusPoller = "Y";
						} else {
							pmCollections = "Y";
						}

						CommunityString communityStringObj = null;
						List<CommunityString> outputList = dbadapter.getCommunityString(vName);
						Iterator<CommunityString> it = outputList.iterator();
						if (it.hasNext()) {
							communityStringObj = it.next();
							configData.setSnmpCommunityString(communityStringObj.getCommunitystring());
							configData.setSnmpVersion(communityStringObj.getSnmpversion());
						}

						if (dtiProps.getString("SEEDINGMANAGERCOLLECTORSENABLED").equalsIgnoreCase("Y")
								&& callMibDis.equalsIgnoreCase("Y")) {
							SeedingManagerController seedingmanager = new SeedingManagerController();
							SeedingManager seedingmanagerpojo = seedingmanager.getVNF(dbadapter, entity.toString(),
									header.getString(Constants.ACTION));
							if (seedingmanagerpojo != null) {
								if (header.getString(Constants.ACTION).equalsIgnoreCase(Constants.DELETE)) {
									seedingManagerEvent(configData.getDeviceName(), Constants.DELETE,
											seedingmanagerpojo, configData);
								} else {
									seedingManagerEvent(configData.getDeviceName(), "", null, configData);
								}
							}
						}

						// if (callMibDis.equalsIgnoreCase("Y")) {
						// sendEventsForDisMib(regexColVal, configData,
						// eventJSONObject);
						// }
						if ("Y".equalsIgnoreCase(callStatusPoller)) {
							sendEventsForStatusPoller(configData, eventJSONObject);
						}

						if (pmCollections.equalsIgnoreCase("Y")) {
							if (!configData.getNodeType().equalsIgnoreCase("PRSF")) {
								configData.setModel(getModelFromGenericVNF(vnfId));
							}
							configData.setSSHFlag("Yes");

							// configData.getTargetType() will return value
							// for mmex
							if (!vName.matches(vnfPatternMap.getString("voip")) && configData.getTargetType() == null) {
								String nodetypeTarget = vnfPatternMap.getString(configData.getNodeType());
								if (nodetypeTarget != null) {
									String[] targetTypeArr = nodetypeTarget.split("-", 2);
									if (targetTypeArr.length > 1) {
										configData.setTargetType(nodetypeTarget);
									} else {
										configData.setTargetType(
												vName.substring(vName.length() - 3) + '-' + nodetypeTarget);
									}
								}
							} else if (serviceType != null && !serviceType.equalsIgnoreCase("")) {
								pmServiceType = serviceType;
							}

							// Override the serviceType value from
							// vnf_pattern_map.properties file, if it is set.
							if (vnfPatternMap.getString(configData.getNodeType() + "_service") != null && !vnfPatternMap
									.getString(configData.getNodeType() + "_service").equalsIgnoreCase("")) {
								pmServiceType = vnfPatternMap.getString(configData.getNodeType() + "_service");
							}

							createDcaeEvent(configData, eventJSONObject);
						}
					}
				} else if (Constants.VCE_ENTITY.equalsIgnoreCase(entityType) && vName.toLowerCase().endsWith("vbc")) {
					if (StringUtils.isBlank(configData.getIpAddress())) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE, "AAI Event",
								entityType, configData.getDeviceName(), configData.getIpAddress());
						return;
					}
					configData.setTargetCollectionIp(configData.getIpAddress());
					configData.setFunctionId("vbc");
					configData.setVnfType(entity.optString("vnf-type"));
					configData.setIsClosedLoopDisabled(Constants.FALSE);
					for (String e : vnfPatternKeyList) {
						foundMatch = false;
						if (!e.startsWith("regex"))
							continue;

						int index = 6;
						if (e.startsWith("regex_")) {
							index = 10;
							if (vName.matches(e.substring(index))) {
								foundMatch = true;
								configData.setNodeType(vnfPatternMap.getString(e));
							}
						}

						String regexColVal = e.substring(index - 4, index - 1);

						if (!foundMatch) {
							ecompLogger.debug("Doesnot match the pattern : " + e);
							continue;
						} else {
							ecompLogger.debug("Collector type: " + regexColVal);
						}
						ecompLogger.debug("VCE nodetype : " + configData.getNodeType());
						String pmCollections = null;

						if (dtiProps.getString("SEEDINGMANAGERCOLLECTORS").contains(regexColVal)) {
							pmCollections = "N";
						} else if (dtiProps.getString("FMCOLLECTORS").contains(regexColVal)) {
							pmCollections = "N";
						} else {
							pmCollections = "Y";
						}

						if ("N".equalsIgnoreCase(pmCollections)) {
							ecompLogger.debug("Skip for FM collector : " + regexColVal);
							continue;
						}

						relationListObject = entity.optJSONObject("relationship-list");
						relationData = getRelationshipData(relationListObject, Constants.COMPLEX_ENTITY,
								"complex.physical-location-id");
						if (relationData != null && relationData.length > 0) {
							configData.setLocationId(relationData[1]);
						} else {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"AAI Event - missing complex relationship", entityType, configData.getDeviceName(),
									configData.getIpAddress());
							return;
						}

						relationData = getRelationshipData(entity.optJSONObject("relationship-list"),
								"service-instance", "service-instance.service-instance-id");
						if (relationData != null && relationData[0] != null) {
							String response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
							if (StringUtils.isNotBlank(response)) {
								ServiceInstance serviceInstance = getServiceInstanceSchemaObject(response);
								configData.setServiceInstanceModelInvariantId(serviceInstance.getModelInvariantId());
								configData.setServiceInstanceModelVersionId(serviceInstance.getModelVersionId());
							}
						}

						String nodetypeTarget = vnfPatternMap.getString(configData.getNodeType());
						if (nodetypeTarget != null) {
							String[] targetTypeArr = nodetypeTarget.split("-", 2);
							if (targetTypeArr.length > 1) {
								configData.setTargetType(nodetypeTarget);
							} else {
								configData.setTargetType(vName.substring(vName.length() - 3) + '-' + nodetypeTarget);
							}
						}

						// Override the serviceType value from
						// vnf_pattern_map.properties file, if it is set.
						if (vnfPatternMap.getString(configData.getNodeType() + "_service") != null && !vnfPatternMap
								.getString(configData.getNodeType() + "_service").equalsIgnoreCase("")) {
							pmServiceType = vnfPatternMap.getString(configData.getNodeType() + "_service");
						}
						createDcaeEvent(configData, eventJSONObject);
					}
				}
			} else if (Constants.VNFC_ENTITY.equalsIgnoreCase(entityType)) {
				Vnfc vnfc = getVnfcSchemaObject(entityValue);
				vName = vnfc.getVnfcName();
				configData.setDeviceName(vName);
				Boolean foundMatch = false;
				String targetType = null;

				for (String e : vnfcPatternKeyList) {
					foundMatch = false;
					if (!e.startsWith("regex"))
						continue;

					int index = 6;
					if (e.startsWith("regex_")) {
						index = 10;
						if (vName.matches(e.substring(index))) {
							foundMatch = true;
							targetType = vnfcPatternMap.getString(e);
							configData.setTargetType(targetType);
						}
					}

					String regexColVal = e.substring(index - 4, index - 1);

					if (!foundMatch) {
						ecompLogger.debug("Doesnot match the pattern : " + e);
						continue;
					} else {
						ecompLogger.debug("Collector type: " + regexColVal);
					}
					if (targetType != null) {
						String[] targetTypeArr = targetType.split("-", 2);
						configData.setNodeType(targetTypeArr[0]);
						configData.setVnfType(targetTypeArr[0]);
						configData.setFunctionId(targetTypeArr[1]);
					} else {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"targetType is empty", entityType, configData.getDeviceName(),
									configData.getIpAddress());
					}

					for (String e1 : vnfcIpKeyList) {
							if (e1.substring(3).contains(configData.getTargetType())) {
								configData.setIpAddress(entity.optString(vnfcPatternMap.getString(e1)));
								if (configData.getIpAddress() == null || configData.getIpAddress().equalsIgnoreCase("")) {
									relationData = getRelationshipData(entity.optJSONObject("relationship-list"),
											"vip-ipv4-address-list", "vip-ipv4-address-list.vip-ipv4-address");
									if (relationData != null && relationData[1] != null) {
										configData.setIpAddress(relationData[1]);
									}
								}
							}
						}

						if (StringUtils.isBlank(configData.getIpAddress())) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"AAI Event - ipaddress is not populated for vnfc using voip ipaddress logic.",
									entityType, configData.getDeviceName(), configData.getIpAddress());
						}

						if (configData.getIpAddress() == null) {
							String vnfcIpAddress = populateVnfcIPAddress(vnfc, configData.getTargetType());
							if (vnfcIpAddress != null)
								configData.setIpAddress(vnfcIpAddress);
						}

						if (StringUtils.isBlank(configData.getIpAddress())) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
									"AAI Event - ipaddress is not populated for vnfc using mobility ipaddress logic as well.",
									entityType, configData.getDeviceName(), configData.getIpAddress());
							return;
						}
						configData.setTargetCollectionIp(configData.getIpAddress());
						String useInMaintFlag = Constants.FALSE;
						for (String e1 : vnfcInMaintKeyList) {
							if (e1.substring(9).contains(configData.getTargetType())) {
								useInMaintFlag = Constants.TRUE;
							}
						}
						if (configData.getIsInMaint().equalsIgnoreCase(Constants.TRUE)
								&& useInMaintFlag.equalsIgnoreCase(Constants.TRUE)) {
							configData.setAction(Constants.DELETE);
						}

						// Get the vserver name from the vnfc
						String vmName = vName.substring(0, vName.length() - 6);
						String vserverURL = null;

						RelationshipList relList = vnfc.getRelationshipList();
						if (relList != null) {
							List<Relationship> lRelationship = relList.getRelationship();
							for (Relationship r : lRelationship) {
								if (r.getRelatedTo().equalsIgnoreCase(Constants.VSERVER_ENTITY)) {
									vserverURL = appendUrl + r.getRelatedLink() + "?depth=all";
									break;
								}
							}
						}

						ecompLogger.debug("VNFC nodetype : " + configData.getNodeType());

						if (vnfc.isIsClosedLoopDisabled())
							configData.setIsClosedLoopDisabled(Constants.TRUE);
						else
							configData.setIsClosedLoopDisabled(Constants.FALSE);

						String nodeType = configData.getNodeType();
						configData.setNodeType(nodeType);

						String vserverLocation = "";
						if (vserverURL != null) {
							String response = restClient.retrieveAAIObject(vserverURL);
							// populate cloud-region-version
							String[] cloudRegionInfo = vserverURL.split("tenants", 2);
							configData.setCloudRegionVersion(getCloudRegionVersion(cloudRegionInfo[0]));
							if (StringUtils.isNotBlank(response)) {
								JSONObject vserverObject = new JSONObject(response);
								Vserver vserverObj = getVserverSchemaObject(response);

								if (StringUtils.isBlank(configData.getNodeType())) {
									ecompLogger.debug(
											inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NODETYPE_FAILURE,
											"AAI Event", entityType, configData.getDeviceName(), configData.getIpAddress());
									return;
								}
								vserverLocation = getLocationDetailsFromVserver(vserverURL);

								String genericVnfURL = "";
								RelationshipList vserverRelList = vserverObj.getRelationshipList();
								if (vserverRelList != null) {
									List<Relationship> lRelationship = vserverRelList.getRelationship();
									for (Relationship r : lRelationship) {
										if (r.getRelatedTo().equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)) {
											genericVnfURL = appendUrl + r.getRelatedLink() + "?depth=all";
											break;
										}
									}
								}

								if (!genericVnfURL.equalsIgnoreCase("")) {
									String responseVnf = restClient.retrieveAAIObject(genericVnfURL);
									if (StringUtils.isNotBlank(responseVnf)) {
										GenericVnf gvnfObj = getGenericVnfSchemaObject(responseVnf);
										JSONObject jObject = new JSONObject(responseVnf);
										if (jObject != null && gvnfObj != null) {

											configData.setVnfType(gvnfObj.getVnfType());
											configData.setGenerciVnfModelInvariantId(gvnfObj.getModelInvariantId());
											configData.setGenerciVnfModelVersionId(gvnfObj.getModelVersionId());
											RelationshipList vnfRelList = gvnfObj.getRelationshipList();
											String serviceInstanceURL = "";
											if (vnfRelList != null) {
												List<Relationship> lRelationship = vnfRelList.getRelationship();
												for (Relationship r : lRelationship) {
													if (r.getRelatedTo()
															.equalsIgnoreCase(Constants.SERVICE_INSTANCE_ENTITY)) {
														serviceInstanceURL = appendUrl + r.getRelatedLink();
														List<RelationshipData> lRelShipData = r.getRelationshipData();
														for (RelationshipData relData : lRelShipData) {
															if (relData.getRelationshipKey().equalsIgnoreCase(
																	"service-subscription.service-type")) {
																serviceType = relData.getRelationshipValue();
																pmServiceType = serviceType;
																configData.setServiceDescription(serviceType);
																break;
															}
														}
														break;
													}
												}
											}

											if (!serviceInstanceURL.equalsIgnoreCase("")) {
												response = restClient.retrieveAAIObject(serviceInstanceURL);
												if (StringUtils.isNotBlank(response)) {
													ServiceInstance serviceInstance = getServiceInstanceSchemaObject(
															response);
													configData.setServiceInstanceModelInvariantId(
															serviceInstance.getModelInvariantId());
													configData.setServiceInstanceModelVersionId(
															serviceInstance.getModelVersionId());
												}
											}
										}
									}
								}

								relationData = null; // Reset relationData
								relationData = getRelationshipData(vserverObject.optJSONObject("relationship-list"),
										Constants.PSERVER_ENTITY, "pserver.hostname");
								if (relationData != null && relationData[0] != null) {
									configData.setLocationId(getLocationDetailsFromPserver(appendUrl + relationData[0]));
								}
								if (configData.getLocationId() == null || configData.getLocationId().equalsIgnoreCase("")) {
									if (!vserverLocation.equalsIgnoreCase("")) {
										configData.setLocationId(vserverLocation);
									}
								}

								// Override the serviceType value from
								// vnfc_pattern_map.properties file, if it is set.
								if (vnfcPatternMap.getString(configData.getNodeType() + "_service") != null
										&& !vnfcPatternMap.getString(configData.getNodeType() + "_service")
												.equalsIgnoreCase("")) {
									pmServiceType = vnfcPatternMap.getString(configData.getNodeType() + "_service");
								}
								System.out.println(pmServiceType);

								if (configData != null && StringUtils.isNotBlank(configData.getDeviceName()))
									createDcaeEvent(configData, eventJSONObject);
							} else {
								ecompLogger.debug("Vserver details not available in A&AI (" + vserverURL + ") for " + vmName);
							}
						} else {
							ecompLogger.debug("Vserver details not available for vnfc : " + vName);
						}
					}
 			} else if (Constants.CLOUD_REGION_ENTITY.equalsIgnoreCase(entityType)
 					|| Constants.TENANT_ENTITY.equalsIgnoreCase(entityType)
 					|| Constants.VSERVER_ENTITY.equalsIgnoreCase(entityType)) {
				String cloudRegionId = entity.optString("cloud-region-id");
				configData.setCloudRegionId(cloudRegionId);
				configData.setCloudRegionVersion(
						getCloudRegionVersion(appendUrl + header.optString("entity-link").split("tenants", 2)[0]));
				JSONObject tenantsObj = entity.optJSONObject("tenants");
				if (tenantsObj == null) {
					ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NO_TENANTS_FAILURE,
							"AAI Event");
					return;
				}
				JSONArray tenantArray = tenantsObj.optJSONArray("tenant");
				for (int i = 0; tenantArray != null && i < tenantArray.length(); i++) {
					JSONObject tenantObject = tenantArray.optJSONObject(i);
					if (tenantObject == null) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NO_TENANTS_FAILURE,
								"AAI Event");
						return;
					}
					JSONObject vserversObject = tenantObject.optJSONObject("vservers");
					if (vserversObject == null) {
						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NO_VSERVERS_FAILURE,
								"AAI Event");
						return;
					}
					JSONArray vserverArray = vserversObject.optJSONArray("vserver");
					for (int j = 0; vserverArray != null && j < vserverArray.length(); j++) {
						JSONObject vserverObject = vserverArray.optJSONObject(j);
						if (vserverObject == null) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NO_VSERVERS_FAILURE,
									"AAI Event");
							return;
						}
						vName = vserverObject.optString("vserver-name");
						configData.setDeviceName(vName);
						String provStatus = vserverObject.optString("prov-status");
						if (entityType.equalsIgnoreCase("vserver")) {
							if (!validateProvStatus(provStatus)) {
								return;
							}
							configData.setProvStatus(provStatus);
						}
						if (StringUtils.isBlank(vName)) {
							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NAME_FAILURE,
									"AAI Event", entityType, vName);
							return;
						}
						if (vName.matches(vmPatternMap.getString("voiptece"))
								|| vName.matches(vmPatternMap.getString("voiptsdb"))
								|| vName.matches(vmPatternMap.getString("voipccdb"))) {
							String lastVMNo = vName.substring(vName.length() - 3);
							if (!lastVMNo.equalsIgnoreCase("001") && !lastVMNo.equalsIgnoreCase("002")) {
								ecompLogger.debug(
										inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_INSIDE_FUNCTION_DEBUG,
										"AAI Event skip VM # >2 ");
								continue;
							}
						}

						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
								"vName = " + vName);

						if (vName.matches(vmPatternMap.getString("mobility"))
								|| vName.matches(".*[zZ][A-Za-z]{6}\\d{1}[A-Za-z]{4}\\d{2}[A-Za-z]{3}.*")
								|| vName.matches(vmPatternMap.getString("voip"))
								|| cloudRegionId.equalsIgnoreCase("D15")) {

							ecompLogger.debug(
									inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
									"cloudRegionId = " + cloudRegionId);

							Boolean isVoip = false;
							String networkId = null;
							String value = null;
							String key = null;
							if (cloudRegionId.equalsIgnoreCase("D15")) {
								// cisvpas is for Firstnet
								key = "cisvpas" + vName.substring(7, 10);
								if (Character.isDigit(vName.charAt(6))) {
									// cisvpas1 is for D2Consumer
									key = "cisvpas1" + vName.substring(7, 10);
								}
								value = vmPatternMap.getString(key.toLowerCase());

								ecompLogger.debug(
										inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
										"vmPatternMap key = " + key + ", value = " + value);

							} else if (vName.matches(vmPatternMap.getString("voip"))) {
								isVoip = true;
								key = vName.substring(vName.length() - 13, vName.length() - 9);
								value = vmPatternMap.getString(key.toLowerCase());
							} else if (StringUtils.isBlank(value)) {
								key = vName.substring(vName.length() - 12, vName.length() - 8)
										+ vName.substring(vName.length() - 6, vName.length() - 3);
								value = vmPatternMap.getString(key);

								if (vmPatternMap.getString(key + "_netid") != null) {
									networkId = vmPatternMap.getString(key + "_netid");
								}

							} else if (StringUtils.isBlank(value) && vName.length() >= 17) {
								key = vName.substring(8, 12) + vName.substring(14, 17);
								value = vmPatternMap.getString(key);
							}

							if (StringUtils.isNotBlank(value)) {
								ecompLogger.debug("vm_pattern_map value for VM (" + vName + ") : " + value);

								if (value != null) {
									String[] tokens = value.split(",");

									ecompLogger.debug(
											inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
											"nodeType = " + tokens[0]);

									configData.setNodeType(tokens[0]);
									if (tokens.length > 1)
										configData.setFunctionId(tokens[1]);

									if (tokens.length == 3) {
										if ("CL".equalsIgnoreCase(tokens[2])) {
											configData.setIsDeviceforCollection("false");
										} else {
											configData.setIsDeviceforCollection("true");
											configData.setZoneName(tokens[2]);
										}
									}
								} else {
									ecompLogger.debug(
											inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
											"nodeType is empty!");
								}
							}

							configData.setDeviceName(vName);
							configData.setIpAddress(
									getVmIPAddress(configData.getNodeType(), cloudRegionId, vserverObject));

							ecompLogger.debug(
									inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
									"getVmIPAddress returning = " + getVmIPAddress(configData.getNodeType(),
											configData.getCloudRegionId(), vserverObject));

							if (StringUtils.isBlank(configData.getIpAddress())) {
								ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
										"AAI Event - ipaddress is not populated (may be network-name / interface-name)",
										entityType, configData.getDeviceName(), configData.getIpAddress());
								return;
							}
							if (StringUtils.isBlank(configData.getIpAddress())
									&& Constants.DOMAIN1.equalsIgnoreCase(domain)) {
								ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_IP_FAILURE,
										"AAI Event", entityType, configData.getDeviceName(), configData.getIpAddress());
								return;
							}
							configData.setTargetCollectionIp(configData.getIpAddress());

							if (StringUtils.isBlank(configData.getNodeType())) {
								ecompLogger.debug(
										inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_NODETYPE_FAILURE,
										"AAI Event", entityType, configData.getDeviceName(), configData.getIpAddress());
								return;
							}

							if (vserverObject.has("in-maint")) {
								if (vserverObject.getBoolean("in-maint"))
									configData.setIsInMaint(Constants.TRUE);
								else
									configData.setIsInMaint(Constants.FALSE);
							}

							CommunityString communityStringObj = null;
							List<CommunityString> outputList = dbadapter.getCommunityString(vName);
							Iterator<CommunityString> it = outputList.iterator();
							if (it.hasNext()) {
								communityStringObj = it.next();
								configData.setSnmpCommunityString(communityStringObj.getCommunitystring());
								configData.setSnmpVersion(communityStringObj.getSnmpversion());
							}

							relationData = getRelationshipData(vserverObject.optJSONObject("relationship-list"),
									Constants.GENERIC_VNF_ENTITY, "generic-vnf.vnf-id");

							if (!Constants.DOMAIN1.equalsIgnoreCase(domain)) {
								ecompLogger.debug(
										inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_SET_PERSONA_DETAILS);
								relationData = getRelationshipData(vserverObject.optJSONObject("relationship-list"),
										Constants.GENERIC_VNF_ENTITY, "generic-vnf.vnf-id");
								if (relationData != null && relationData[0] != null) {
									String response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
									if (StringUtils.isNotBlank(response)) {
										JSONObject jObject = new JSONObject(response);
										if (jObject != null) {

											configData.setVnfType(jObject.optString("vnf-type"));
											if (jObject.has("model-invariant-id")) {
												configData.setGenerciVnfModelInvariantId(
														jObject.optString("model-invariant-id"));
											}
											if (jObject.has("model-version-id")) {
												configData.setGenerciVnfModelVersionId(
														jObject.optString("model-version-id"));
											}
											relationData = getRelationshipData(
													jObject.optJSONObject("relationship-list"), "service-instance",
													"service-subscription.service-type");
											if (relationData != null && relationData[1] != null && !(isFlexreach(configData.getNodeType())) ) {
												serviceType = relationData[1];
												configData.setServiceDescription(serviceType);
											}

											if (isVoip && serviceType != null && !serviceType.equalsIgnoreCase("") &&  !(isFlexreach(configData.getNodeType())) ) {
												pmServiceType = serviceType;
											}
											
											//override the service description for Flexreach devices
											if ( isFlexreach(configData.getNodeType())) {
												configData.setServiceDescription("FLEXREACH");
												pmServiceType= "FLEXREACH";
											}

											relationData = getRelationshipData(
													jObject.optJSONObject("relationship-list"), "service-instance",
													"service-instance.service-instance-id");
											if (relationData != null && relationData[0] != null) {
												response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
												if (StringUtils.isNotBlank(response)) {
													ServiceInstance serviceInstance = getServiceInstanceSchemaObject(
															response);
													configData.setServiceInstanceModelInvariantId(
															serviceInstance.getModelInvariantId());
													configData.setServiceInstanceModelVersionId(
															serviceInstance.getModelVersionId());
												}
											}
										}
									}
								}
							}

							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_SET_NEVERSION);
							// Set neVersion
							relationData = null;
							relationData = getRelationshipData(vserverObject.optJSONObject("relationship-list"),
									"image", "image.image-id");
							if (relationData != null && relationData.length > 1 && relationData[0] != null) {
								String response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
								if (StringUtils.isNotBlank(response)) {
									JSONObject jObject = new JSONObject(response);
									if (jObject != null)
										configData.setNeVersion(
												jObject.optString("application-version").replace(' ', '_'));
								}
							}

							// Set isClosedLoopDisabled flag to be sent in
							// Events json to Orchestrator
							if (vserverObject.has("is-closed-loop-disabled")) {
								if (vserverObject.getBoolean("is-closed-loop-disabled"))
									configData.setIsClosedLoopDisabled(Constants.TRUE);
								else
									configData.setIsClosedLoopDisabled(Constants.FALSE);
							}

							relationData = null; // Reset relationData
							relationData = getRelationshipData(vserverObject.optJSONObject("relationship-list"),
									Constants.PSERVER_ENTITY, "pserver.hostname");
							if (relationData != null && relationData[0] != null) {
								configData.setLocationId(getLocationDetailsFromPserver(appendUrl + relationData[0]));
							}

							if (networkId != null) {
								configData.setNetworkId(networkId);
							}

							if (configData != null && StringUtils.isNotBlank(configData.getDeviceName()))
								createDcaeEvent(configData, eventJSONObject);

							return;
						}
					}
				}
			}
		}
	}

	public String getTemplateContent(String filename) {
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

	public String getAAITemplate(String entityType) {
		String template = "";
		String configDir = System.getenv("DTI_CONFIG");
		switch (entityType) {
		case Constants.PSERVER_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_pserver_template.json");
			break;
		case Constants.PNF_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_pnf_template.json");
			break;
		case Constants.GENERIC_VNF_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_generic_vnf_template.json");
			break;
		case Constants.VCE_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_vce_template.json");
			break;
		case Constants.VNFC_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_vnfc_template.json");
			break;
		case Constants.VSERVER_ENTITY:
			template = getTemplateContent(Util.safeFileName(configDir) + "/aaievent_vserver_template.json");
			break;
		default:
			break;
		}
		return template;
	}

	public void replaceEntityValueAndProcessEvent(String aaiUrl, String aaieventTemplate) {
		String response = null;
		try {
			response = restClient.retrieveAAIObject(aaiUrl);
			if (!StringUtils.isBlank(response)) {
				aaieventTemplate = aaieventTemplate.replace("ENTITY_VALUE", response);
				getAAIUpdates(aaieventTemplate);
			}
		} catch (DTIException e) {
			e.printStackTrace();
		}
	}

	public void generateAAIEvents(String entityType) {
		com.att.vcc.inventorycollector.domain.Pserver pserver = null;
		com.att.vcc.inventorycollector.domain.Vserver vserver = null;
		com.att.vcc.inventorycollector.domain.GenericVnf genericVnf = null;
		com.att.vcc.inventorycollector.domain.Pnf pnf = null;
		com.att.vcc.inventorycollector.domain.Vnfc vnfc = null;
		com.att.vcc.inventorycollector.domain.Vce vce = null;

		switch (entityType) {
		case Constants.PSERVER_ENTITY:
			String pserverTemplate = null;
			pserverTemplate = getAAITemplate(Constants.PSERVER_ENTITY);

			List<com.att.vcc.inventorycollector.domain.Pserver> outputListPserver = dbadapter.getAllPserver();
			Iterator<com.att.vcc.inventorycollector.domain.Pserver> pserverIt = outputListPserver.iterator();
			while (pserverIt.hasNext()) {
				pserver = pserverIt.next();
				String nodeType = "";
				if (pserver != null && pserver.getHostname() != null) {
					nodeType = getNodeType(Constants.PSERVER_ENTITY, pserver.getHostname(), "", "");
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = pserverTemplate;
						String entityUrl = "cloud-infrastructure/pservers/pserver/" + pserver.getHostname();
						String pserverUrl = EventUtil.API_URL + entityUrl;
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/"))	+ entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						replaceEntityValueAndProcessEvent(pserverUrl, aaieventTemplate);
					}
				}
			}
			break;
		case Constants.PNF_ENTITY:
			String pnfTemplate = null;
			pnfTemplate = getAAITemplate(Constants.PNF_ENTITY);

			List<com.att.vcc.inventorycollector.domain.Pnf> outputListPnf = dbadapter.getAllPnf();
			Iterator<com.att.vcc.inventorycollector.domain.Pnf> pnfIt = outputListPnf.iterator();
			while (pnfIt.hasNext()) {
				pnf = pnfIt.next();
				String nodeType = "";
				if (pnf != null && pnf.getPnfname() != null) {
					nodeType = getNodeType(Constants.PNF_ENTITY, pnf.getPnfname(), "", "");
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = pnfTemplate;
						String entityUrl = "network/pnfs/pnf/" + pnf.getPnfname();
						String pnfUrl = EventUtil.API_URL + entityUrl;
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/"))	+ entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						replaceEntityValueAndProcessEvent(pnfUrl, aaieventTemplate);
					}
				}
			}
			break;
		case Constants.GENERIC_VNF_ENTITY:
			String genericVnfTemplate = null;
			genericVnfTemplate = getAAITemplate(Constants.GENERIC_VNF_ENTITY);

			List<com.att.vcc.inventorycollector.domain.GenericVnf> outputListGenericVnc = dbadapter.getAllGenericVnf();
			Iterator<com.att.vcc.inventorycollector.domain.GenericVnf> genericVnfIt = outputListGenericVnc.iterator();
			while (genericVnfIt.hasNext()) {
				genericVnf = genericVnfIt.next();
				String nodeType = "";
				if (genericVnf != null && genericVnf.getVnfname() != null) {
					nodeType = getNodeType(Constants.GENERIC_VNF_ENTITY, genericVnf.getVnfname(),
							genericVnf.getVnftype(), "");
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = genericVnfTemplate;
						String entityUrl = "network/generic-vnfs/generic-vnf/" + genericVnf.getVnfid();
						String vnfUrl = EventUtil.API_URL + entityUrl;
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/"))	+ entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						replaceEntityValueAndProcessEvent(vnfUrl, aaieventTemplate);
					}
				}
			}
			break;
		case Constants.VCE_ENTITY:
			genericVnfTemplate = null;
			genericVnfTemplate = getAAITemplate(Constants.VCE_ENTITY);

			List<com.att.vcc.inventorycollector.domain.Vce> outputListVce = dbadapter.getAllVce();
			Iterator<com.att.vcc.inventorycollector.domain.Vce> vceIt = outputListVce.iterator();
			while (vceIt.hasNext()) {
				vce = vceIt.next();
				System.out.println(vce.getVnfname());
				String nodeType = "";
				if (vce != null && vce.getVnfid() != null) {
					nodeType = getNodeType(Constants.VCE_ENTITY, vce.getVnfname(), "", "");
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = genericVnfTemplate;
						String entityUrl = "network/vces/vce/" + vce.getVnfid();
						;
						String vceUrl = EventUtil.API_URL + entityUrl;
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/")) + entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						replaceEntityValueAndProcessEvent(vceUrl, aaieventTemplate);
					}
				}
			}
			break;
		case Constants.VNFC_ENTITY:
			String vnfcTemplate = null;
			vnfcTemplate = getAAITemplate(Constants.VNFC_ENTITY);

			List<com.att.vcc.inventorycollector.domain.Vnfc> outputListVnfc = dbadapter.getAllVnfc();
			Iterator<com.att.vcc.inventorycollector.domain.Vnfc> vnfcIt = outputListVnfc.iterator();
			while (vnfcIt.hasNext()) {
				vnfc = vnfcIt.next();
				String nodeType = "";
				if (vnfc != null && vnfc.getVnfcname() != null) {
					nodeType = getNodeType(Constants.VNFC_ENTITY, vnfc.getVnfcname(), "", "");
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = vnfcTemplate;
						String entityUrl = "network/vnfcs/vnfc/" + vnfc.getVnfcname() + "?depth=all";
						String vnfcUrl = EventUtil.API_URL + entityUrl;
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/")) + entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						replaceEntityValueAndProcessEvent(vnfcUrl, aaieventTemplate);
					}
				}
			}
			break;
		case Constants.VSERVER_ENTITY:
			String vserverTemplate = null;
			vserverTemplate = getAAITemplate(Constants.VSERVER_ENTITY);

			List<com.att.vcc.inventorycollector.domain.Vserver> outputListVserver = dbadapter.getAllVserver();
			Iterator<com.att.vcc.inventorycollector.domain.Vserver> vserverIt = outputListVserver.iterator();
			while (vserverIt.hasNext()) {
				vserver = vserverIt.next();
				String nodeType = "";
				if (vserver != null && vserver.getVservername() != null) {
					nodeType = getNodeType(Constants.VSERVER_ENTITY, vserver.getVservername(), "",
							vserver.getCloudregionid());
					if (!StringUtils.isBlank(nodeType)) {
						String aaieventTemplate = vserverTemplate;
						String entityUrl = "cloud-infrastructure/cloud-regions/cloud-region/" + vserver.getCloudowner()
								+ "/" + vserver.getCloudregionid() + "/tenants/tenant/" + vserver.getTenantid()
								+ "/vservers/vserver/" + vserver.getVserverid() + "?depth=all";
						String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/aai/")) + entityUrl;
						aaieventTemplate = aaieventTemplate.replace(Constants.ENTITY_LINK, entityLink);
						aaieventTemplate = aaieventTemplate.replace("CLOUD_OWNER", vserver.getCloudowner());
						aaieventTemplate = aaieventTemplate.replace("CLOUD_REGION_ID", vserver.getCloudregionid());
						aaieventTemplate = aaieventTemplate.replace("TENANT_ID", vserver.getTenantid());
						replaceEntityValueAndProcessEvent(EventUtil.API_URL + entityUrl, aaieventTemplate);
					}
				}
			}
			break;
		default:
			break;

		}
	}

	public String getNodeType(String entityType, String deviceName, String vnfType, String cloudRegionId) {
		String nodeType = "";
		switch (entityType) {
		case Constants.PSERVER_ENTITY:
			for (String e : pserverPatternKeyList) {
				if (!e.startsWith("regex"))
					continue;

				int index = 6;
				if (e.startsWith("regex_")) {
					index = 10;
					if (deviceName.matches(e.substring(index))) {
						nodeType = pserverPatternMap.getString(e);
					}
				}
			}
			break;
		case Constants.PNF_ENTITY:
			for (String s : pnfPatternKeyList) {
				if (deviceName.matches(s.substring(6))) {
					nodeType = pnfPatternMap.getString(s);
					break;
				}
			}
			break;
		case Constants.VCE_ENTITY:
		case Constants.GENERIC_VNF_ENTITY:
			for (String e : vnfPatternKeyList) {
				if (!e.startsWith("regex"))
					continue;

				int index = 6;
				if (e.startsWith("regex_")) {
					index = 10;
					if (deviceName.matches(e.substring(index))) {
						nodeType = vnfPatternMap.getString(e);
						break;
					}
				}

				if (e.startsWith("regex_vnftype")) {
					index = 18;
					if (vnfType.matches(e.substring(index))) {
						nodeType = vnfPatternMap.getString(e);
						break;
					}
				}
			}
			break;
		case Constants.VSERVER_ENTITY:
			if (deviceName.matches(vmPatternMap.getString("mobility"))
					|| deviceName.matches(".*[zZ][A-Za-z]{6}\\d{1}[A-Za-z]{4}\\d{2}[A-Za-z]{3}.*")
					|| deviceName.matches(vmPatternMap.getString("voip")) || cloudRegionId.equalsIgnoreCase("D15")) {

				String key = null;
				if (cloudRegionId.equalsIgnoreCase("D15")) {
					key = deviceName.substring(7, 10);
					nodeType = vmPatternMap.getString(key.toLowerCase());
				} else if (deviceName.matches(vmPatternMap.getString("voip"))) {
					key = deviceName.substring(deviceName.length() - 13, deviceName.length() - 9);
					nodeType = vmPatternMap.getString(key.toLowerCase());
				} else if (StringUtils.isBlank(nodeType)) {
					key = deviceName.substring(deviceName.length() - 12, deviceName.length() - 8)
							+ deviceName.substring(deviceName.length() - 6, deviceName.length() - 3);
					nodeType = vmPatternMap.getString(key);
				} else if (StringUtils.isBlank(nodeType) && deviceName.length() >= 17) {
					key = deviceName.substring(8, 12) + deviceName.substring(14, 17);
					nodeType = vmPatternMap.getString(key);
				}
			}
			break;
		case Constants.VNFC_ENTITY:
			for (String e : vnfcPatternKeyList) {
				if (!e.startsWith("regex"))
					continue;

				int index = 6;
				if (e.startsWith("regex_")) {
					index = 10;
					if (deviceName.matches(e.substring(index))) {
						nodeType = vnfcPatternMap.getString(e);
					}
				}
			}
			break;
		default:
			break;

		}
		return nodeType;
	}

	private String populateVnfcIPAddress(Vnfc vnfc, String targetType) {

		// Populate IP address for mobility vnfc
		// Step 0) initialize ip address as null
		// Step 1) get all Connection Point (CP) object for the vnfc object.
		// Step 2) For each CP, perform below steps.
		// Step 3) Get l3-interface-ipv4-address-list associated with the CP.
		// Step 4) Get the relationship-list associated with the
		// l3-interface-ipv4-address-list.
		// Step 5) for each relationship from the relationship-list perform
		// below steps.
		// Step 6) check if "related-to" is "l3-network" then go to step 7 else
		// go to step 5
		// Step 7) check if related-to-property->property-value contains
		// 'oam_mgmt' then go to step 10 else go to step 8
		// Step 8) check if related-to-property->property-value contains
		// 'oam_protected' then go to step 10 else go to step 9
		// Step 9) check if related-to-property->property-value contains
		// 'oam_direct' then go to step 10 else go to step 11
		// Step 10) populate ip address from l3-interface-ipv4-address
		// attribute.
		// Step 11) return ip address

		String ipAddress = null;
		for (String e1 : vnfcMobilityIpKeyList) {
			if (e1.substring(11).contains(targetType)) {
				Cps cps = vnfc.getCps();
				if (cps != null) {
					List<Cp> listCp = cps.getCp();
					if (listCp != null) {
						for (Cp cp : listCp) {
							List<L3InterfaceIpv4AddressList> listL3IntIpv4AddrList = cp.getL3InterfaceIpv4AddressList();
							if (listL3IntIpv4AddrList != null) {
								for (L3InterfaceIpv4AddressList l3IntIpv4AddrList : listL3IntIpv4AddrList) {
									RelationshipList relList = l3IntIpv4AddrList.getRelationshipList();
									if (relList != null) {
										List<Relationship> lRelationship = relList.getRelationship();
										for (Relationship r : lRelationship) {
											if (r.getRelatedTo().equalsIgnoreCase("l3-network")) {
												String networkName = r.getRelatedToProperty().get(0).getPropertyValue()
														.toLowerCase();
												boolean isOamMgmtAddr = networkName
														.contains(vnfcPatternMap.getString("mobility-pattern1"));
												boolean isOamProtectedAddr = networkName
														.contains(vnfcPatternMap.getString("mobility-pattern2"));
												boolean isOamDirectAddr = networkName
														.contains(vnfcPatternMap.getString("mobility-pattern3"));
												if (isOamMgmtAddr || isOamProtectedAddr || isOamDirectAddr) {
													ipAddress = l3IntIpv4AddrList.getL3InterfaceIpv4Address();
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ipAddress;
	}

	private ObjectMapper getObjectMapperObject() {
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

	private CloudRegion getCloudRegionSchemaObject(String cloudRegionDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		CloudRegion cloudRegion = null;
		try {
			cloudRegion = mapper.readValue(cloudRegionDetails, CloudRegion.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cloudRegion;
	}

	private Pnf getPnfSchemaObject(String pnfDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		Pnf pnf = null;
		try {
			pnf = mapper.readValue(pnfDetails, Pnf.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pnf;
	}

	private Vnfc getVnfcSchemaObject(String vnfcDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		Vnfc vnfc = null;
		try {
			vnfc = mapper.readValue(vnfcDetails, Vnfc.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vnfc;
	}

	private Vce getVceSchemaObject(String vceDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		Vce vce = null;
		try {
			vce = mapper.readValue(vceDetails, Vce.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vce;
	}

	private GenericVnf getGenericVnfSchemaObject(String genericVnfDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		GenericVnf genericVnf = null;
		try {
			genericVnf = mapper.readValue(genericVnfDetails, GenericVnf.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return genericVnf;
	}

	private ServiceInstance getServiceInstanceSchemaObject(String serviceInstanceDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		ServiceInstance serviceInstance = null;
		try {
			serviceInstance = mapper.readValue(serviceInstanceDetails, ServiceInstance.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serviceInstance;
	}

	private Vserver getVserverSchemaObject(String vserverDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		Vserver vserver = null;
		try {
			vserver = mapper.readValue(vserverDetails, Vserver.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vserver;
	}

	private Pserver getPserverSchemaObject(String pserverDetails) {
		ObjectMapper mapper = getObjectMapperObject();
		Pserver pserver = null;
		try {
			pserver = mapper.readValue(pserverDetails, Pserver.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pserver;
	}

	private String getLocationDetailsFromPserver(String pserverSelfLink) {
		String location = "";
		String response = null;
		try {
			response = restClient.retrieveAAIObject(pserverSelfLink);
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pserver pserver = null;

		if (response != null && !response.equalsIgnoreCase("")) {
			pserver = getPserverSchemaObject(response);
			// Populate dcae_service_location
			RelationshipList relList = pserver.getRelationshipList();
			location = getLocationFromRelationshipList(relList);
		}
		return location;
	}

	private String getLocationFromRelationshipList(RelationshipList relList) {
		String location = "";
		if (relList != null) {
			List<Relationship> lRelationship = relList.getRelationship();
			for (Relationship r : lRelationship) {
				if (r.getRelatedTo().equalsIgnoreCase(Constants.COMPLEX_ENTITY)) {
					location = getLocationDetails(r.getRelatedLink());
					break;
				}
			}
		}
		return location;
	}

	private String getLocationDetailsFromVserver(String vserverSelfLink) {
		String location = "";
		String appendUrl = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9);

		String response = null;
		try {
			response = restClient.retrieveAAIObject(vserverSelfLink);
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vserver vserver = null;

		if (response != null && !response.equalsIgnoreCase("")) {
			vserver = getVserverSchemaObject(response);
			// Populate dcae_service_location
			RelationshipList relList = vserver.getRelationshipList();
			if (relList != null) {
				List<Relationship> lRelationship = relList.getRelationship();
				for (Relationship r : lRelationship) {
					if (r.getRelatedTo().equalsIgnoreCase(Constants.COMPLEX_ENTITY)) {
						location = getLocationDetails(r.getRelatedLink());
					} else if (r.getRelatedTo().equalsIgnoreCase(Constants.PSERVER_ENTITY)) {
						location = getLocationDetailsFromPserver(appendUrl + r.getRelatedLink());
					}
					if (!location.equalsIgnoreCase("")) {
						break;
					}
				}
			}
		}
		return location;
	}

	private String[] getVnfcNodeTypeFunctionCode(String vnfcName) {
		Boolean foundMatch;
		String targetType = null;
		for (String e : vnfcPatternKeyList) {
			foundMatch = false;
			if (!e.startsWith("regex"))
				continue;

			int index = 6;
			if (e.startsWith("regex_")) {
				index = 10;
				if (vnfcName.matches(e.substring(index))) {
					foundMatch = true;
					targetType = vnfcPatternMap.getString(e);
				}
			}

			if (foundMatch) {
				break;
			}
		}
		String[] targetTypeArr = { "", "" };
		if (targetType != null) {
			targetTypeArr = targetType.split("-", 2);
		}
		return targetTypeArr;
	}

	private String populateNeVersion(JSONObject jsonObj) throws DTIException {

		String neVersion = "";
		// remove /aai/v13/ from API_URL
		String appendUrl = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9);
		// Set neVersion
		String[] relData = getRelationshipData(jsonObj.optJSONObject("relationship-list"), "vserver",
				"vserver.vserver-id");
		if (relData != null && relData.length > 1 && relData[0] != null) {
			String response = restClient.retrieveAAIObject(appendUrl + relData[0]);
			if (StringUtils.isNotBlank(response)) {
				JSONObject jObject = new JSONObject(response); // vserver
																// object
				if (jObject != null) {
					relData = getRelationshipData(jObject.optJSONObject("relationship-list"), "image",
							"image.image-id");
					if (relData != null && relData.length > 1 && relData[0] != null) {
						response = restClient.retrieveAAIObject(appendUrl + relData[0]);
						if (StringUtils.isNotBlank(response)) {
							jObject = new JSONObject(response);
							if (jObject != null)
								neVersion = jObject.optString("application-version").replace(' ', '_');
						}
					}
				}
			}
		}
		return neVersion;
	}

	private String getModelFromGenericVNF(String vnfId) throws DTIException {
		String model = "";
		// remove /aai/v13/ from API_URL
		String appendUrl = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9);
		String response = restClient.retrieveAAIObject(EventUtil.API_URL + EventUtil.GENERICVNFURL + vnfId);
		if (StringUtils.isNotBlank(response)) {
			JSONObject vnfObject = new JSONObject(response);
			if (vnfObject != null) {
				String[] relData = getRelationshipData(vnfObject.optJSONObject("relationship-list"), "vserver",
						"vserver.vserver-id");
				if (relData != null && relData.length > 1 && relData[0] != null) {
					String vserverResponse = restClient.retrieveAAIObject(appendUrl + relData[0]);
					if (StringUtils.isNotBlank(vserverResponse)) {
						JSONObject jObject = new JSONObject(vserverResponse); // vserver
						// object
						if (jObject != null) {
							relData = getRelationshipData(jObject.optJSONObject("relationship-list"), "image",
									"image.image-id");
							if (relData != null && relData.length > 1 && relData[0] != null) {
								response = restClient.retrieveAAIObject(appendUrl + relData[0]);
								if (StringUtils.isNotBlank(response)) {
									jObject = new JSONObject(response);
									if (jObject != null)
										model = jObject.optString("application");
								}
							}
						}
					}
				}
			}
		}
		return model;
	}

	private String getComplexRegion(String complexUrl) {
		String complexRegion = "";
		String response = "";
		try {
			complexUrl += "?nodes-only";
			response = restClient.retrieveAAIObject(complexUrl);
		} catch (DTIException e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(response)) {
			JSONObject jObject = new JSONObject(response); // cloud-region obj
			if (jObject != null && jObject.has("region")) {
				complexRegion = jObject.optString("region");
			}
		}
		return complexRegion;
	}

	private String getCloudRegionVersion(String cloudRegionUrl) {
		String cloudRegionVersion = "";
		String response = "";
		try {
			cloudRegionUrl += "?nodes-only";
			response = restClient.retrieveAAIObject(cloudRegionUrl);
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(response)) {
			JSONObject jObject = new JSONObject(response); // cloud-region obj
			if (jObject != null && jObject.has("cloud-region-version")) {
				cloudRegionVersion = jObject.optString("cloud-region-version");
			}
		}
		return cloudRegionVersion;
	}

	private String getServiceDesc(String genericVnfUrl) {
		String serviceDesc = "";
		String[] relationData = new String[2];
		String response = "";
		try {
			response = restClient.retrieveAAIObject(genericVnfUrl);
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(response)) {
			JSONObject jObject = new JSONObject(response); // generic-vnf object
			relationData = getRelationshipData(jObject.optJSONObject("relationship-list"), "service-instance",
					"service-subscription.service-type");
			if (relationData != null && relationData[1] != null) {
				serviceDesc = relationData[1];
			}
		}
		return serviceDesc;
	}

	private String getCloudRegionId(String vserverUrl) {
		String cloudRegionId = "";

		if (vserverUrl != null && !vserverUrl.equals("")) {
			String[] tempArray;
			tempArray = vserverUrl.split("/");
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
					"Length" + tempArray.length);
			
			cloudRegionId = tempArray[tempArray.length - 7];
		}
		return cloudRegionId;
	}

	private String getNetidFromCC(String Name) {
		String dns = "";
		if (Name.length() == 13) {
			dns = Name.substring(0, 7);
		} else if (Name.length() == 11) {
			dns = Name.substring(0, 5);
		}
		String netId = "";
		if (dns.length() == 5) {
			netId = "CBB";
		} else if (dns.length() == 7) {
			String cc = dns.substring(0, 2).toUpperCase();
			String ccNetid = cc2netid.getString(cc);
			if (ccNetid != null) {
				String[] regionNetid = ccNetid.split(";");
				netId = regionNetid[1];
			}
		}
		return netId;
	}
	
	private boolean isovlMibTargetGroup( String nodeType){
		String[] ovlMibList = (vmPatternMap.getString("isovlMib")).split(",");	
		return Arrays.asList(ovlMibList).contains(nodeType);				
	}

	private boolean isovlDisTargetGroup( String nodeType){
		String[] ovlDisList = (vmPatternMap.getString("isovlDis")).split(",");	
		return Arrays.asList(ovlDisList).contains(nodeType);				
	}
	
	public void sendEventsForDisMib(String fmCollector, ConfigurationData configData, JSONObject aaiEvent,
			String entityType) throws DTIException {

		if (configData == null || configData.getIpAddress() == null) {
			ecompLogger.debug("Either config data or IP Address is empty.");
			return;
		}

		entityType = Optional.ofNullable(entityType).orElse(Constants.GENERIC_VNF_ENTITY);
		Events events = null;
		String orchestratorAction = Constants.DCAE_SERVICE_ACTION_ADD;
		if (StringUtils.isBlank(configData.getAction()))
			configData.setAction(Constants.CREATE);

		String dtiAction = configData.getAction();
		if (dtiAction.equalsIgnoreCase(Constants.UPDATE)) {
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_UPDATE;
			configData.setAction(Constants.UPDATE);
		} else if (dtiAction.equalsIgnoreCase(Constants.DELETE)) {
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_DELETE;
		}
		
		String dcaeTargetType= "";
		
		if( isovlMibTargetGroup(configData.getFunctionId()) && fmCollector.equalsIgnoreCase("mib") ) {
			dcaeTargetType="ovl-mib";
		} else if( isovlDisTargetGroup(configData.getFunctionId()) && fmCollector.equalsIgnoreCase("dis") ) {	
			dcaeTargetType="ovl-dis";			
		}else {
			dcaeTargetType = configData.getFunctionId().toLowerCase() + '-' + fmCollector;
		}

		 
		configData.setTargetType(dcaeTargetType);

		ConfigurationData configDataOrig = configData;
		configData = setCommunityStringVersion(configDataOrig, dcaeTargetType, configDataOrig.getDeviceName());

		JSONObject json = new JSONObject();
		json.put("hostName", configData.getDeviceName());
		json.put("icmpIP", configData.getTargetCollectionIp());
		json.put("snmpIP", configData.getTargetCollectionIp());
		json.put("communityString", configData.getSnmpCommunityString());
		json.put("snmpVersion", configData.getSnmpVersion());
		json.put("designType", "");
		json.put("location", configData.getLocationId());
		json.put("deviceType", configData.getVnfType());
		json.put("entityType", entityType);
		json.put("functionCode", configData.getFunctionId());
		json.put("outputObjects", "!arpTable");
		json.put("deviceChangeTimeStamp", EventUtil.getCurrentTimestamp());
		json.put("ICMPIntervalClass", "B");
		json.put("FMMIBPollerIntervalClass", "A");
		json.put("changeType", configData.getChangetype());

		events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction, configData.getLocationId(),
				configData.getVnfType(), configData.getProvStatus(), configData.getIsInMaint(),
				configData.getIsClosedLoopDisabled(), configData.getServiceInstanceModelInvariantId(),
				configData.getServiceInstanceModelVersionId(), configData.getGenerciVnfModelInvariantId(),
				configData.getGenerciVnfModelVersionId(), configData.getIsDeviceforCollection(),
				configData.getTargetCollectionIp(), configData.getSnmpCommunityString(), configData.getSnmpVersion(),
				configData.getCloudRegionId(), configData.getCloudRegionVersion(), configData.getServiceDescription(),
				aaiEvent, json);
		sendRequestToDTIHandler(events, dtiAction, "N");
		return;
	}
	
	private boolean isovlStapTargetGroup( String nodeType){
		String[] ovlStapList = (vmPatternMap.getString("isovlStap")).split(",");	
		return Arrays.asList(ovlStapList).contains(nodeType);				
	}
	
	private boolean iscpeStapTargetGroup( String nodeType){
		String[] cpeStapList = (vmPatternMap.getString("iscpeStap")).split(",");	
		return Arrays.asList(cpeStapList).contains(nodeType);				
	}


	public void sendEventsForStatusPoller(ConfigurationData configData, JSONObject aaiEvent) throws DTIException {

		if (configData == null || configData.getIpAddress() == null)
			return;

		Events events = null;
		
		String orchestratorAction = Constants.DCAE_SERVICE_ACTION_ADD;

		if (StringUtils.isBlank(configData.getAction()))
			configData.setAction(Constants.CREATE);

		String dtiAction = configData.getAction();
		if (dtiAction.equalsIgnoreCase(Constants.UPDATE)) {
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_UPDATE;
			configData.setAction(Constants.UPDATE);
		} else if (dtiAction.equalsIgnoreCase(Constants.DELETE)) {
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_DELETE;
		}

		JSONObject json = new JSONObject();
		String dcaeTargetType = "";
		if( isovlStapTargetGroup(configData.getFunctionId()) ) {
			dcaeTargetType = "ovl-stap";
			
		}else if( iscpeStapTargetGroup(configData.getFunctionId()) ) {
			dcaeTargetType = "cpe-stap";
		}else {
			dcaeTargetType = configData.getFunctionId().toLowerCase() + "-stap";
		}
		configData.setTargetType(dcaeTargetType);

		ConfigurationData configDataOrig = configData;
		configData = setCommunityStringVersion(configDataOrig, dcaeTargetType, configDataOrig.getDeviceName());

		events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction, configData.getLocationId(),
				configData.getVnfType(), configData.getProvStatus(), configData.getIsInMaint(),
				configData.getIsClosedLoopDisabled(), configData.getServiceInstanceModelInvariantId(),
				configData.getServiceInstanceModelVersionId(), configData.getGenerciVnfModelInvariantId(),
				configData.getGenerciVnfModelVersionId(), configData.getIsDeviceforCollection(),
				configData.getTargetCollectionIp(), configData.getSnmpCommunityString(), configData.getSnmpVersion(),
				configData.getCloudRegionId(), configData.getCloudRegionVersion(), configData.getServiceDescription(),
				aaiEvent, json);
		sendRequestToDTIHandler(events, dtiAction, "Y");
		return;
	}

	public void createDcaeEvent(ConfigurationData configData, JSONObject aaiEvent) throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "createDcaeEvent");

		if (configData == null || configData.getIpAddress() == null)
			return;

		postEventsToOrchestrator(Arrays.asList(configData), aaiEvent);
	}

	/**
	 * Util method to return {'related-link', 'relationship-value'} in a JSON
	 * Object returned by A&AI 'relationship-list' json is structured as below
	 * {} relationship-list [] relationship {} 0 * related-to * related-link []
	 * relationship-data {} 0 * relationship-key * relationship-value {} n []
	 * related-to-property {} 0 * property-key * property-value {} n
	 * 
	 * @param relationshipList
	 * @param relatedTo
	 * @param relationshipKey
	 * @return String[]
	 */
	private String[] getRelationshipData(JSONObject relationshipList, String relatedTo, String relationshipKey) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "getRelationshipData");

		if (relationshipList == null || relatedTo == null || relationshipKey == null)
			return null;

		String[] relationData = new String[2];

		JSONArray relationshipArray = relationshipList.optJSONArray("relationship");
		JSONObject relationshipArrayObject = null;
		JSONArray relationshipDataArray = null;
		JSONObject relationshipDataArrayObject = null;
		for (int i = 0; relationshipArray != null && i < relationshipArray.length(); i++) {
			relationshipArrayObject = relationshipArray.optJSONObject(i);
			if (relationshipArrayObject != null
					&& relatedTo.equalsIgnoreCase(relationshipArrayObject.optString("related-to"))) {
				relationData[0] = relationshipArrayObject.optString("related-link");
				relationshipDataArray = relationshipArrayObject.optJSONArray("relationship-data");
				for (int j = 0; relationshipDataArray != null && j < relationshipDataArray.length(); j++) {
					relationshipDataArrayObject = relationshipDataArray.optJSONObject(j);
					if (relationshipDataArrayObject != null
							&& relationshipKey
									.equalsIgnoreCase(relationshipDataArrayObject.optString("relationship-key"))
							&& relationshipDataArrayObject.has("relationship-value")) {
						relationData[1] = relationshipDataArrayObject.getString("relationship-value");
						return relationData;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param linterfaces
	 * @param nwNamePattern
	 * @param resultField
	 * @return oam ipv4 address
	 */
	private String getLInterfaceData(JSONObject linterfaces, String oamFieldName, String nwNamePattern,
			String resultField) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "getLInterfaceData");

		if (linterfaces == null || StringUtils.isBlank(nwNamePattern) || StringUtils.isBlank(resultField))
			return null;

		JSONArray linterfaceArray = linterfaces.optJSONArray("l-interface");
		JSONObject linterfaceArrayObject = null;
		JSONArray linterfaceipAddrArray = null;
		JSONObject linterfaceipAddrArrayObject = null;
		for (int i = 0; linterfaceArray != null && i < linterfaceArray.length(); i++) {
			linterfaceArrayObject = linterfaceArray.optJSONObject(i);
			if (linterfaceArrayObject != null) {
				if (linterfaceArrayObject.has(oamFieldName)) {
					if (!StringUtils.containsIgnoreCase(linterfaceArrayObject.getString(oamFieldName), nwNamePattern))
						continue;
					linterfaceipAddrArray = linterfaceArrayObject.optJSONArray("l3-interface-ipv4-address-list");
					for (int j = 0; linterfaceipAddrArray != null && j < linterfaceipAddrArray.length(); j++) {
						linterfaceipAddrArrayObject = linterfaceipAddrArray.optJSONObject(j);
						if (linterfaceipAddrArrayObject != null)
							return linterfaceipAddrArrayObject.getString(resultField);
					}
				} else {
					linterfaceipAddrArray = linterfaceArrayObject.optJSONArray("l3-interface-ipv4-address-list");
					for (int j = 0; linterfaceipAddrArray != null && j < linterfaceipAddrArray.length(); j++) {
						linterfaceipAddrArrayObject = linterfaceipAddrArray.optJSONObject(j);
						if (linterfaceipAddrArrayObject != null) {
							String[] relData = getRelationshipData(
									linterfaceipAddrArrayObject.optJSONObject("relationship-list"),
									Constants.L3_NETWORK_ENTITY, "l3-network.network-name");
							if (relData == null || relData.length < 1 || relData[1] == null)
								continue;
							if (StringUtils.containsIgnoreCase(relData[1], nwNamePattern))
								return linterfaceipAddrArrayObject.getString(resultField);
						}
					}
				}
			}
		}
		return null;
	}

	private String getLInterfaceDatavDBE(JSONObject linterfaces, String oamFieldName, String nwNamePattern,
			String interfaceName, String ifPattern, String resultField) {
		// getLInterfaceDatavDBE(vserverObject.optJSONObject("l-interfaces"),
		// "network-name",
		// vmPatternMap.getString("vusp"), "interface-name",
		// "l3-interface-ipv4-address");
		if (linterfaces == null || StringUtils.isBlank(nwNamePattern) || StringUtils.isBlank(resultField))
			return null;

		JSONArray linterfaceArray = linterfaces.optJSONArray("l-interface");
		JSONObject linterfaceArrayObject = null;
		JSONArray linterfaceipAddrArray = null;
		JSONObject linterfaceipAddrArrayObject = null;
		for (int i = 0; linterfaceArray != null && i < linterfaceArray.length(); i++) {
			linterfaceArrayObject = linterfaceArray.optJSONObject(i);
			if (linterfaceArrayObject != null) {
				if (linterfaceArrayObject.has(oamFieldName)) {
					if (StringUtils.containsIgnoreCase(linterfaceArrayObject.getString(oamFieldName), nwNamePattern)
							&& StringUtils.endsWithIgnoreCase(linterfaceArrayObject.getString(interfaceName),
									ifPattern))
						linterfaceipAddrArray = linterfaceArrayObject.optJSONArray("l3-interface-ipv4-address-list");
					for (int j = 0; linterfaceipAddrArray != null && j < linterfaceipAddrArray.length(); j++) {
						linterfaceipAddrArrayObject = linterfaceipAddrArray.optJSONObject(j);
						if (linterfaceipAddrArrayObject != null)
							return linterfaceipAddrArrayObject.getString(resultField);
					}
				}
			}
		}
		return null;
	}

	private TasksItem createTaskItem(String taskId, String nodeType, String funcId,
			Map<String, RemoteServerGroup> rsgMap) throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "createTaskItem");

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
				"createTaskItem(): taskId = " + taskId);

		if (dtiTaskRegister.isEmpty()) {
			FileInputStream inStream = null;
			try {
				inStream = new FileInputStream(
						Util.safeFileName(System.getenv("DTI_CONFIG") + "/dti_task_register.properties"));
				dtiTaskRegister.setDelimiterParsingDisabled(true);
				dtiTaskRegister.load(inStream);

			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_REF_FILE_LOAD_FAILURE,
						"dti_task_register.properties" + "\n" + Util.errorStacktoString(e.getStackTrace()));
				throw new DTIException(
						"Could not load " + System.getenv("DTI_CONFIG") + "/dti_task_register.properties", e);
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_REF_FILE_LOAD_FAILURE,
						"dti_task_register.properties" + "\n" + Util.errorStacktoString(e.getStackTrace()));
				e.printStackTrace();
			} finally {
				Util.closeFileInputStream(inStream);
			}
		}

		// APP_LOG.debug("Creating Task Items ..");
		String value = dtiTaskRegister.getString(taskId);
		if (StringUtils.isBlank(value))
			return null;

		String[] taskAttributes = value.split("~");
		String serverName = rsgMap.get("g1").getServerGroup().get(0).getRemoteServerName();

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
				"createTaskItem(): nodeType = " + nodeType + ", fuctionId = " + funcId + ",serverName = " + serverName);

		String vmData = (vmPatternMap.getString(nodeType.toLowerCase() + funcId));
		String serviceType = "";
		String[] token = null;
		if (!StringUtils.isBlank(vmData))
			token = vmData.split(",");
		if (token != null && token.length > 2)
			serviceType = token[2];

        if(nodeType.equalsIgnoreCase("CISVPCRF"))
        	serviceType = "FIRSTNET";

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
				"createTaskItem(): 10. nodeType = " + nodeType);

		// Remove the number from the nodeType, except d2msn nodetype
		if(!nodeType.equalsIgnoreCase("d2msn"))
			nodeType = nodeType.replaceAll("[0-9]", "");	
				

		TasksItem taskItem = new TasksItem(taskId, taskAttributes[0], taskAttributes[1], taskAttributes[3], nodeType,
				"", serviceType, taskAttributes[2], nodeType, funcId, Integer.valueOf(taskAttributes[4]),
				taskAttributes[5], rsgMap);

		return taskItem;
	}

	private void createTaskFile(List<TasksItem> taskItems, String zone, Events events, String dtiAction)
			throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "createTaskFile");
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		Map<String, TasksItem> taskItemMap = new HashMap<String, TasksItem>();
		Map<String, com.att.vcc.vcctask.RemoteServerGroup> rsgMap1 = null;
		List<com.att.vcc.vcctask.ServerGroupItem> sgiList1 = null;
		com.att.vcc.vcctask.ServerGroupItem sgi1 = null;
		
		for (com.att.vcc.vcctask.TasksItem ti : taskItems) {			
			if (ti != null) {
				if (vnfPatternMap.getString("appendExtraVNF").contains(ti.getVnfType()))
				{
					rsgMap1 = ti.getRemoteServerGroups();
					com.att.vcc.vcctask.RemoteServerGroup rsg1 = rsgMap1.get("g1");
					sgiList1 = rsg1.getServerGroup();
					sgi1 = sgiList1.get(0);
					ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "createTaskFile for RemoteServerName=" + sgi1.getRemoteServerName() );
					taskItemMap.put(sgi1.getRemoteServerName() + "_" + ti.getTaskId(), ti);					
				} else {
					taskItemMap.put(events.getDcaeTargetName() + "_" + ti.getTaskId(), ti);
				}
			}
		}
		
		// if an event was already dispatched to orchestrator with this
		// action and server name, do not send the same action again.
		if ((Constants.CREATE.equalsIgnoreCase(dtiAction) || Constants.DELETE.equalsIgnoreCase(dtiAction))
				&& (Files.exists(Paths.get(ValidationData
						.cleanPathString(Util.safeFileName(TEMP_OUT_FILE_PATH + "Events_" + events.getDcaeTargetName()
								+ "_" + events.getDcaeTargetType() + "_" + events.getDcaeServiceAction())))))) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_DUPLICATE_EVENT,
					events.getDcaeTargetName() + " , action " + dtiAction);
			return;
		}

		if (StringUtils.isBlank(post_url)) {
			if (dtiProps.containsKey("post_url"))
				post_url = dtiProps.getString("post_url");
			else
				post_url = System.getenv("ORCH_POST_URL");
		}

		if (DCAE_LOCATION != null && !DCAE_LOCATION.equalsIgnoreCase("")) {
			events.setDcaeServiceLocation(DCAE_LOCATION);
		}

		String eventsJSON = null;
		JSONObject dcaeEventEvent = events.getEvent();
		int statusCode = 0;
		if (Constants.CREATE.equalsIgnoreCase(dtiAction) || Constants.UPDATE.equalsIgnoreCase(dtiAction)) {

			mapper = new ObjectMapper();
			try {
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				if (taskItemMap.isEmpty())
					events.setAaiAdditionalInfo(new JSONObject());
				else {
					json = mapper.writeValueAsString(new Tasks(taskItemMap));
					events.setAaiAdditionalInfo(new JSONObject(json));
				} if ( Constants.CREATE.equalsIgnoreCase(dtiAction) ) {  
				events.setDcaeServiceAction(Constants.DCAE_SERVICE_ACTION_ADD);
				} else if ( Constants.UPDATE.equalsIgnoreCase(dtiAction) ) {
					events.setDcaeServiceAction(Constants.DCAE_SERVICE_ACTION_UPDATE);
				}
				// To address defect 715445
				// Set empty json for event field before sending to DTI2 Handler
				events.setEvent(new JSONObject());
				eventsJSON = mapper.writeValueAsString(events);
				// Reset event json to store in postgres DB
				events.setEvent(dcaeEventEvent);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new DTIException(
						"Action: " + events.getDcaeServiceAction()
								+ ". JSON Processing exception while creating events JSONObject to send to orchestrator",
						e);
			}
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH, Constants.DEPLOY,
					events.getDcaeTargetName());
			ecompLogger.recordMetricEventStart(inventoryCollectorOperationEnum.DTIPostTaskToOrchOperation,
					post_url + ", " + dtiAction);

			storeDcaeEvent(events, "");
			statusCode = restClient.post(eventsJSON, post_url);
			if (statusCode == Constants.DELETE_SUCCESS || statusCode == Constants.EVENTS_SUCCESS
					|| statusCode == Constants.DEPLOY_SUCCESS) {
				ecompLogger.recordMetricEventEnd(StatusCodeEnum.COMPLETE,
						inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_SUCCESS,
						events.getDcaeTargetName(), dtiAction);
				storeDcaeEvent(events, "Success");
			} else {
				ecompLogger.recordMetricEventEnd(StatusCodeEnum.ERROR,
						inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_FAILURE,
						String.valueOf(statusCode), events.getDcaeTargetName(), dtiAction);
				storeDcaeEvent(events, "Failure");
			}
		} else if (Constants.DELETE.equalsIgnoreCase(dtiAction)) {
			try {
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				events.setDcaeServiceAction(Constants.DCAE_SERVICE_ACTION_DELETE);
				// To address defect 715445
				// Set empty json for event field before sending to DTI2 Handler
				events.setEvent(new JSONObject());
				eventsJSON = mapper.writeValueAsString(events);
				// Reset event json to store in postgres DB
				events.setEvent(dcaeEventEvent);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new DTIException(
						"Action: " + events.getDcaeServiceAction()
								+ ". JSON Processing exception while creating events JSONObject to send to orchestrator",
						e);
			}
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH, Constants.UNDEPLOY,
					events.getDcaeTargetName());
			ecompLogger.recordMetricEventStart(inventoryCollectorOperationEnum.DTIPostTaskToOrchOperation,
					post_url + ", " + dtiAction);

			if (DCAE_LOCATION != null && !DCAE_LOCATION.equalsIgnoreCase("")) {
				events.setDcaeServiceLocation(DCAE_LOCATION);
			}

			storeDcaeEvent(events, "");
			statusCode = restClient.post(eventsJSON, post_url);
			if (statusCode == Constants.DELETE_SUCCESS || statusCode == Constants.EVENTS_SUCCESS
					|| statusCode == Constants.DEPLOY_SUCCESS) {
				ecompLogger.recordMetricEventEnd(StatusCodeEnum.COMPLETE,
						inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_SUCCESS,
						events.getDcaeTargetName(), dtiAction);
				storeDcaeEvent(events, "Success");
			} else {
				ecompLogger.recordMetricEventEnd(StatusCodeEnum.ERROR,
						inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_FAILURE,
						String.valueOf(statusCode), events.getDcaeTargetName(), dtiAction);
				storeDcaeEvent(events, "Failure");
			}
		}
	}

	public void sendRequestToDTIHandler(Events events, String dtiAction, String checkFile) throws DTIException {

		ObjectMapper mapper = new ObjectMapper();

		if (checkFile != null && checkFile.equalsIgnoreCase("Y")) {
			if ((Constants.CREATE.equalsIgnoreCase(dtiAction) || Constants.DELETE.equalsIgnoreCase(dtiAction))
					&& (Files.exists(Paths.get(ValidationData.cleanPathString(
							Util.safeFileName(TEMP_OUT_FILE_PATH + "Events_" + events.getDcaeTargetName() + "_"
									+ events.getDcaeTargetType() + "_" + events.getDcaeServiceAction())))))) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_DUPLICATE_EVENT,
						events.getDcaeTargetName() + " , action " + dtiAction);
				return;
			}
		}

		if (StringUtils.isBlank(post_url)) {
			if (dtiProps.containsKey("post_url"))
				post_url = dtiProps.getString("post_url");
			else
				post_url = System.getenv("ORCH_POST_URL");
		}

		if (DCAE_LOCATION != null && !DCAE_LOCATION.equalsIgnoreCase("")) {
			events.setDcaeServiceLocation(DCAE_LOCATION);
		}

		String eventsJSON = null;
		JSONObject dcaeEventEvent = events.getEvent();
		int statusCode = 0;
		try {
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			events.setDcaeServiceAction(dtiAction);
			// To address defect 715445
			// Set empty json for event field before sending to DTI2 Handler
			events.setEvent(new JSONObject());
			eventsJSON = mapper.writeValueAsString(events);
			// Reset event json to store in postgres DB
			events.setEvent(dcaeEventEvent);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new DTIException("Action: " + dtiAction
					+ ". JSON Processing exception while creating events JSONObject to send to DTI Handler", e);
		}
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH,
				events.getDcaeServiceAction(), events.getDcaeTargetName());
		ecompLogger.recordMetricEventStart(inventoryCollectorOperationEnum.DTIPostTaskToOrchOperation,
				post_url + ", " + dtiAction);
		storeDcaeEvent(events, "");
		statusCode = restClient.post(eventsJSON, post_url);
		if (statusCode == Constants.DELETE_SUCCESS || statusCode == Constants.EVENTS_SUCCESS
				|| statusCode == Constants.DEPLOY_SUCCESS) {
			ecompLogger.recordMetricEventEnd(StatusCodeEnum.COMPLETE,
					inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_SUCCESS, events.getDcaeTargetName(),
					dtiAction);
			storeDcaeEvent(events, "Success");
		} else {
			ecompLogger.recordMetricEventEnd(StatusCodeEnum.ERROR,
					inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_POST_ORCH_FAILURE, String.valueOf(statusCode),
					events.getDcaeTargetName(), dtiAction);
			storeDcaeEvent(events, "Failure");
		}
	}

	/**
	 * Post Events json to Orchestrator. Method invoked only in D2 env
	 * 
	 * @param configDataList
	 * @param aaiEvent
	 * @throws DTIException
	 */
	public void postEventsToOrchestrator(List<ConfigurationData> configDataList, JSONObject aaiEvent)
			throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "postEventsToOrchestrator");
		Set<String> nodeTypeFuncCodeSet = new HashSet<String>();
		loadActiveTaskMap();
		// ecompLogg.info("Posting events to orchestrator..");
		for (Map.Entry<String, Map<String, List<String>>> e : activeTaskMap.entrySet())
			nodeTypeFuncCodeSet.addAll(e.getValue().keySet());

		ServerGroupItem sgi = null;
		List<ServerGroupItem> sgiList = null;
		com.att.vcc.vcctask.RemoteServerGroup rsg = null;
		Map<String, com.att.vcc.vcctask.RemoteServerGroup> rsgMap = null;
		List<TasksItem> tiList = new ArrayList<TasksItem>();
		Events events = null;
		String targetName = null;
		String orchestratorAction = Constants.DCAE_SERVICE_ACTION_ADD;
		String nodeType;
		String funcCode;
		String backupServer = null;

		if (Constants.DELETE.equalsIgnoreCase(configDataList.get(0).getAction()))
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_DELETE;
		else if (Constants.UPDATE.equalsIgnoreCase(configDataList.get(0).getAction()))
			orchestratorAction = Constants.DCAE_SERVICE_ACTION_UPDATE;

		for (ConfigurationData configDataOrig : configDataList) {
			nodeType = configDataOrig.getNodeType();
			funcCode = configDataOrig.getFunctionId();
			if (funcCode == null)
				funcCode = "";

			tiList = new ArrayList<TasksItem>();
			if (StringUtils.isBlank(configDataOrig.getAction()))
				configDataOrig.setAction(Constants.CREATE);

			String dcaeTargetType = configDataOrig.getTargetType();
			if (dcaeTargetType == null || dcaeTargetType.equalsIgnoreCase("")) {
				dcaeTargetType = getDcaeTargetType(configDataOrig.getDeviceName());

			}

			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
					"dcaeTargetType = " + dcaeTargetType);

			String groupTargetType = groupTargetType(dcaeTargetType);
			if (groupTargetType != null) {
				dcaeTargetType = groupTargetType;
			}
			ConfigurationData configData = setCommunityStringVersion(configDataOrig, dcaeTargetType,
					configDataOrig.getDeviceName());
			/*if (Constants.FALSE.equalsIgnoreCase(configData.getIsDeviceforCollection())) {
				if (aaiEvent == null)
					aaiEvent = new JSONObject();
				if (Constants.TRUE.equalsIgnoreCase(vmPatternMap.getString("do-not-send-persona-details"))
						&& StringUtils.isNotBlank(vmPatternMap.getString("send-persona-details-types"))
						&& vmPatternMap.getString("send-persona-details-types").contains(configData.getNodeType())) {

					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(),
							configData.getServiceInstanceModelInvariantId(),
							configData.getServiceInstanceModelVersionId(), configData.getGenerciVnfModelInvariantId(),
							configData.getGenerciVnfModelVersionId(), configData.getIsDeviceforCollection(),
							configData.getTargetCollectionIp(), configData.getSnmpCommunityString(),
							configData.getSnmpVersion(), configData.getCloudRegionId(),
							configData.getCloudRegionVersion(), configData.getServiceDescription(), aaiEvent,
							new JSONObject());

				} else {
					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(), "", "", "", "",
							configData.getIsDeviceforCollection(), configData.getTargetCollectionIp(),
							configData.getSnmpCommunityString(), configData.getSnmpVersion(),
							configData.getCloudRegionId(), configData.getCloudRegionVersion(),
							configData.getServiceDescription(), aaiEvent, new JSONObject());
				}

				createTaskFile(tiList, null, events, configData.getAction());
				continue;
			}

			// If the configData does not have ipaddress, then no need to create
			// task info
			if (StringUtils.isBlank(configData.getIpAddress())) {
				if (aaiEvent == null)
					aaiEvent = new JSONObject();
				if (Constants.TRUE.equalsIgnoreCase(vmPatternMap.getString("do-not-send-persona-details"))
						&& StringUtils.isNotBlank(vmPatternMap.getString("send-persona-details-types"))
						&& vmPatternMap.getString("send-persona-details-types").contains(configData.getNodeType())) {
					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(),
							configData.getServiceInstanceModelInvariantId(),
							configData.getServiceInstanceModelVersionId(), configData.getGenerciVnfModelInvariantId(),
							configData.getGenerciVnfModelVersionId(), configData.getIsDeviceforCollection(),
							configData.getTargetCollectionIp(), configData.getSnmpCommunityString(),
							configData.getSnmpVersion(), configData.getCloudRegionId(),
							configData.getCloudRegionVersion(), configData.getServiceDescription(), aaiEvent,
							new JSONObject());
				} else {
					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(), "", "", "", "",
							configData.getIsDeviceforCollection(), configData.getTargetCollectionIp(),
							configData.getSnmpCommunityString(), configData.getSnmpVersion(),
							configData.getCloudRegionId(), configData.getCloudRegionVersion(),
							configData.getServiceDescription(), aaiEvent, new JSONObject());
				}
				createTaskFile(tiList, null, events, configData.getAction());
				continue;
			} */

			if (!nodeTypeFuncCodeSet.contains(nodeType + funcCode)) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
						"Skiping for NodeType + FuncCode :" + nodeType + funcCode);
				continue;
			}

			if (!Constants.DELETE.equalsIgnoreCase(configData.getAction())) {
				for (Map.Entry<String, Map<String, List<String>>> atmEntry : activeTaskMap.entrySet()) {
					for (Map.Entry<String, List<String>> nodeTypeTaskIdEntry : atmEntry.getValue().entrySet()) {
						if (!(nodeType + funcCode).equalsIgnoreCase(nodeTypeTaskIdEntry.getKey()))
							continue;						
						for (String taskId : nodeTypeTaskIdEntry.getValue()) {
							targetName = configData.getDeviceName();
							// check for special case of MEC, MCC, e.g. nw2b19mcmm01v001, zrdm52amcmc01							
							if ("mcmm".equalsIgnoreCase(nodeType) || "mcmc".equalsIgnoreCase(nodeType) 
									|| "mcmr".equalsIgnoreCase(nodeType) || "mcmi".equalsIgnoreCase(nodeType)
									|| "mccp".equalsIgnoreCase(nodeType)) {								
								Map<String, String> emsMap = new HashMap<String, String>();
								emsMap = loadSpecialConfigMap("mec_mapping.conf");
								//emsMap has the key, e.g. v001, and whole line as value
								for (Map.Entry<String, String> mapvalue : emsMap.entrySet()) {
									String[] token = mapvalue.getValue().split(",");
									String orderStr = token[1];
									String emsNameStr = token[2];
									String ipv4Str = token[3];
									
								//JsonPropertyOrder({ "isPrimary", "remoteServerIp", "remoteServerName", "inMaintenance", "neVersion", "networkId", "provStatus" }
								sgi = new ServerGroupItem(Constants.TRUE, ipv4Str, targetName+orderStr,
										configData.getIsInMaint(), configData.getNeVersion(), configData.getNetworkId(),
										configData.getModel(), configData.getSSHFlag(), configData.getProvStatus(),
										configData.getLocationId(), emsNameStr, ""); 
								sgiList = Arrays.asList(sgi);
								rsg = new RemoteServerGroup("g1", sgiList);								
								rsgMap = new HashMap<String, RemoteServerGroup>();
								rsgMap.put(rsg.getServerGroupId(), rsg);

								TasksItem ti = new TasksItem();
								ti = createTaskItem(taskId, nodeType, funcCode, rsgMap);  // cancat v001

									if (pmServiceType != null) {
										ti.setServiceType(pmServiceType);
									}
									tiList.add(ti);
								}
							} else if ("vssf".equalsIgnoreCase(nodeType) && "ssf".equalsIgnoreCase(funcCode)) {
								//TEST,vssf-ssf,107.112.130.154,zrdm5avssf01aas
								Map<String, String> emsMap = new HashMap<String, String>();
								sgiList = new ArrayList<ServerGroupItem>();
								emsMap = loadSpecialConfigMap("vssf_mapping.conf");
								//emsMap has the key, e.g. vssf-ssf, and whole line as value
								int countSgi=0;
								ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
										"for VSSF-SSF: mapping has size" + emsMap.entrySet().size());
								for (Map.Entry<String, String> mapvalue : emsMap.entrySet()) {
									String[] token = mapvalue.getValue().split(",");
									String emsNameStr = token[3];
									String ipv4Str = token[2];																
									
									if (countSgi==0) {
										sgi = new ServerGroupItem(Constants.TRUE, ipv4Str, targetName,
											configData.getIsInMaint(), configData.getNeVersion(), configData.getNetworkId(),
											configData.getModel(), configData.getSSHFlag(), configData.getProvStatus(),
											configData.getLocationId(), emsNameStr, "");
										countSgi++;
									} else {
										sgi = new ServerGroupItem(Constants.FALSE, ipv4Str, targetName,
												configData.getIsInMaint(), configData.getNeVersion(), configData.getNetworkId(),
												configData.getModel(), configData.getSSHFlag(), configData.getProvStatus(),
												configData.getLocationId(), emsNameStr, "");
										countSgi++;										
									}
									//sgiList = Arrays.asList(sgi);								
									sgiList.add(sgi);
								}
									rsg = new RemoteServerGroup("g1", sgiList);								
									rsgMap = new HashMap<String, RemoteServerGroup>();
									rsgMap.put(rsg.getServerGroupId(), rsg);

									TasksItem ti = new TasksItem();
									ti = createTaskItem(taskId, nodeType, funcCode, rsgMap);  // cancat v001

										if (pmServiceType != null) {
											ti.setServiceType(pmServiceType);
										}
										tiList.add(ti);
							} else {
								backupServer = vmPatternMap.getString(nodeType + funcCode);
								if (backupServer != null && !"0".equalsIgnoreCase(backupServer)
									&& configData.getDeviceName().endsWith("002"))
									sgi = new ServerGroupItem(Constants.FALSE, configData.getIpAddress(), targetName,
										configData.getIsInMaint(), configData.getNeVersion(), configData.getNetworkId(),
										configData.getModel(), configData.getSSHFlag(), configData.getProvStatus(),
										configData.getLocationId(),"","");
								else
									sgi = new ServerGroupItem(Constants.TRUE, configData.getIpAddress(), targetName,
										configData.getIsInMaint(), configData.getNeVersion(), configData.getNetworkId(),
										configData.getModel(), configData.getSSHFlag(), configData.getProvStatus(),
										configData.getLocationId(),"","");
								sgiList = Arrays.asList(sgi);
								rsg = new RemoteServerGroup("g1", sgiList);
								rsgMap = new HashMap<String, RemoteServerGroup>();
								rsgMap.put(rsg.getServerGroupId(), rsg);

								TasksItem ti = new TasksItem();
								ti = createTaskItem(taskId, nodeType, funcCode, rsgMap);

								if (pmServiceType != null) {
									ti.setServiceType(pmServiceType);
								}
								tiList.add(ti);
							}
						}
					}
				}
			}

			if (tiList.size() > 0 || Constants.DELETE.equalsIgnoreCase(configData.getAction())) {
				System.out.println("Task Item list > 0 OR action = delete");
				if (aaiEvent == null)
					aaiEvent = new JSONObject();
				if (Constants.TRUE.equalsIgnoreCase(vmPatternMap.getString("do-not-send-persona-details"))
						&& StringUtils.isNotBlank(vmPatternMap.getString("send-persona-details-types"))
						&& vmPatternMap.getString("send-persona-details-types").contains(configData.getNodeType())) {
					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(),
							configData.getServiceInstanceModelInvariantId(),
							configData.getServiceInstanceModelVersionId(), configData.getGenerciVnfModelInvariantId(),
							configData.getGenerciVnfModelVersionId(), configData.getIsDeviceforCollection(),
							configData.getTargetCollectionIp(), configData.getSnmpCommunityString(),
							configData.getSnmpVersion(), configData.getCloudRegionId(),
							configData.getCloudRegionVersion(), configData.getServiceDescription(), aaiEvent,
							new JSONObject());
				} else
					events = new Events(configData.getDeviceName(), dcaeTargetType, orchestratorAction,
							configData.getLocationId(), configData.getVnfType(), configData.getProvStatus(),
							configData.getIsInMaint(), configData.getIsClosedLoopDisabled(), "", "", "", "",
							configData.getIsDeviceforCollection(), configData.getTargetCollectionIp(),
							configData.getSnmpCommunityString(), configData.getSnmpVersion(),
							configData.getCloudRegionId(), configData.getCloudRegionVersion(),
							configData.getServiceDescription(), aaiEvent, new JSONObject());
				createTaskFile(tiList, null, events, configData.getAction());
			}
		}
	}

	public String getDcaeTargetType(String serverName) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "getDcaeTargetType");
		if (serverName == null || serverName.isEmpty()) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
					"getDcaeTargetType(): serverName is null");
			return "srv_name_is_null";
		}

		if (serverName.matches(vmPatternMap.getString("mobility"))) {
			return serverName.substring(serverName.length() - 12, serverName.length() - 8) + "-"
					+ serverName.substring(serverName.length() - 6, serverName.length() - 3);
		} else if (serverName.matches(".*[zZ][A-Za-z]{6}\\d{1}[A-Za-z]{4}\\d{2}[A-Za-z]{3}.*")) {
			if (serverName.length() >= 17)
				return serverName.substring(8, 12) + "-" + serverName.substring(14, 17);
		} else if (serverName.matches(vmPatternMap.getString("voip"))) {
			return serverName.substring(serverName.length() - 13, serverName.length() - 9);
		} else if (serverName.matches(".*[rR][vV][1-7]$")) {
			return serverName.substring(serverName.length() - 3) + "-yyy";
		} else if (serverName.matches(".*[vV][nN][2-3]$")) {
			return serverName.substring(serverName.length() - 3) + "-xxx";
		} else if (serverName.endsWith("me6")) {
			return "me6-xxx";
		} else if (serverName.matches(vnfPatternMap.getString("voip"))) {
			return serverName.substring(serverName.length() - 9, serverName.length() - 5);
		} else if (serverName.matches(vnfPatternMap.getString("voip-comx"))) {
			return serverName.substring(0, 5) + "-xxx";
		} else if (serverName.toLowerCase().contains("prmppmu")) {
			return "prmppmu";
		} else if (serverName.toLowerCase().contains("vper")) {
			return "snmp-mvm2";
		} else if (serverName.toUpperCase().contains("PNGA")) {
			return "pnga";
		} else if (serverName.matches(".*?[mM][cC][mM][cC]\\d{2}") || serverName.matches(".*?[mM][cC][mM][rR]\\d{2}") || serverName.matches(".*?[mM][cC][mM][iI]\\d{2}") || 
				serverName.matches(".*?[mM][cC][cC][pP]\\d{2}")){
			return "foic-mcc";
		}else if (serverName.matches(".*?[mM][cC][mM][mM]\\d{2}")) {
			return "foic-mec";
		}else if (serverName.length() == 13) {
			String str = serverName.substring(7, 10);

			if ("pcc".equalsIgnoreCase(str)) {
				return "pcrf-" + str;
			} else if ("dbc".equalsIgnoreCase(str) || "drc".equalsIgnoreCase(str) || "drm".equalsIgnoreCase(str)
					|| "dbm".equalsIgnoreCase(str)) {
				return "vpas-" + str;
			}
		}

		return null;
	}

	private boolean isVUSP( String nodeType){
		String[] VUSPList = (vmPatternMap.getString("isVUSP")).split(",");
		return Arrays.asList(VUSPList).contains(nodeType);
	}
	
	private boolean isTrinity( String nodeType){
		String[] TrinityList = (vmPatternMap.getString("isTrinity")).split(",");	
		return Arrays.asList(TrinityList).contains(nodeType);				
	}
	
	private boolean isVUSPVDBE ( String nodeType ){
		String[] VUSPVDBEList = (vmPatternMap.getString("isVUSPVDBE")).split(",");
		return Arrays.asList(VUSPVDBEList).contains(nodeType);
	}
	
	private boolean isTrinityVDBE (String nodeType){
		String[] TrinityVDBEList = (vmPatternMap.getString("isTrinityVDBE")).split(",");
		return Arrays.asList(TrinityVDBEList).contains(nodeType);
		
	}
	
	private String getVmIPAddress(String nodeType, String cloudRegionId, JSONObject vserverObject) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "getVmIPAddress");
		String ipAddress = null;
		if (StringUtils.isBlank(nodeType) || vserverObject == null)
			return ipAddress;

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
				"entering getVmIPAddress ");

		Util util = new Util();
		if (cloudRegionId.equalsIgnoreCase("D15")) {
			ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "interface-name",
					vmPatternMap.getString("d15firstnet"), "l3-interface-ipv4-address");
		} else if (isVUSP(nodeType)) {
			ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "network-name",
					vmPatternMap.getString("vusp"), "l3-interface-ipv4-address");			
		} else if (isVUSPVDBE(nodeType)){
				ipAddress = getLInterfaceDatavDBE(vserverObject.optJSONObject("l-interfaces"), "network-name",
						vmPatternMap.getString("vusp"), "interface-name", vmPatternMap.getString("vDBEifpattern"),
						"l3-interface-ipv4-address");
		} else if (isTrinity(nodeType)) {
			   ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "network-name",
					vmPatternMap.getString("trinity"), "l3-interface-ipv4-address");
			
		} else if (isTrinityVDBE(nodeType)) {
				ipAddress = getLInterfaceDatavDBE(vserverObject.optJSONObject("l-interfaces"), "network-name",
						vmPatternMap.getString("trinity"), "interface-name", vmPatternMap.getString("vDBEifpattern"),
						"l3-interface-ipv4-address");
		} else {
			// Populate IP address for mobility
			ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "network-name",
					vmPatternMap.getString("mobility-pattern1"), "l3-interface-ipv4-address");
			if (ipAddress == null) {
				ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "network-name",
						vmPatternMap.getString("mobility-pattern2"), "l3-interface-ipv4-address");
				if (ipAddress == null) {
					ipAddress = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "network-name",
							vmPatternMap.getString("mobility-pattern3"), "l3-interface-ipv4-address");
				}
			}
		}
		return ipAddress;
	}

	private void loadActiveTaskMap() throws DTIException {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
		// "loadActiveTaskMap");
		// Properties dtiActiveTaskProps = new Properties();
		String dir = System.getenv("DTI_CONFIG");
		// try (FileInputStream inStream = new
		// FileInputStream(Util.safeFileName(dir) +
		// "/dti_active_task.properties")) {
		// dtiActiveTaskProps.load(inStream);
		String fileName = ValidationData.cleanPathString(Util.safeFileName(dir) + "/dti_active_task.properties");
		List<String> tasks = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				tasks.add(line);
			}
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_REF_FILE_LOAD_FAILURE,
					"dti_active_task.properties");
			throw new DTIException("Could not load dti_active_task.properties", e);
		}

		// format of activeTaskMap :
		// <CollectorType,Map<'NodeType+funcCode',List<TaskIds>>>
		if (activeTaskMap == null || activeTaskMap.isEmpty()) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG,
					"activeTaskMap is empty");

			String[] token = null;
			Map<String, List<String>> nodeTaskMap = null;
			List<String> Ids = null;
			for (String str : tasks) {
				if (StringUtils.isBlank(str) || StringUtils.startsWith(str, "#"))
					continue;

				// format of str - taskid,nodetype,collection
				token = str.split(",");
				if (token == null || token.length < 4)
					continue;

				if (activeTaskMap.containsKey(token[0]))
					nodeTaskMap = activeTaskMap.get(token[0]);
				else
					nodeTaskMap = new HashMap<String, List<String>>();

				if (nodeTaskMap.containsKey(token[1] + token[2]))
					Ids = nodeTaskMap.get(token[1] + token[2]);
				else
					Ids = new ArrayList<String>();

				Ids.add(token[3]);
				nodeTaskMap.put(token[1] + token[2], Ids);
				activeTaskMap.put(token[0], nodeTaskMap);
			}
		}
	}

	private Map<String, String> loadSpecialConfigMap(String configFileName) throws DTIException {
		// this is used to load the special mapping for EMS to vnfName, e.g. MEC for mcmm or MCC for mcmc, mcmr, mcmi, mccp
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "loadSpecialConfigMap for " + configFileName);
		String dir = System.getenv("DTI_CONFIG");
		String env = System.getenv("Environment");
		// try (FileInputStream inStream = new
		// FileInputStream(Util.safeFileName(dir) +
		// "/dti_active_task.properties")) {
		// dtiActiveTaskProps.load(inStream);
		String fileName = ValidationData.cleanPathString(Util.safeFileName(dir) + "/" + configFileName );
		List<String> mapList = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				mapList.add(line);
			}
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_REF_FILE_LOAD_FAILURE, configFileName);
			throw new DTIException("Could not load " + configFileName, e);
		}
			String[] token = null;
			Map<String, String> emsVipMap = new HashMap<String, String>();
			//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the config has size" + mapList.size());
			for (String str : mapList) {
				if (StringUtils.isBlank(str) || StringUtils.startsWith(str, "#"))
					continue;

				// format of str - ENV, Order, EMSName, ipv4, role  
				token = str.split(",");
				//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "str is " + str);
				if ("PROD".equalsIgnoreCase(env.substring(0, 4))) {
					if ("PROD".equalsIgnoreCase(token[0])) {
						emsVipMap.put(token[1], str);
					}
				} else {
					if (!"PROD".equalsIgnoreCase(token[0])) {
						//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "NONPROD str is " + str);
						emsVipMap.put(token[1], str);
					}
				}
			}
		return emsVipMap;
	}
}
