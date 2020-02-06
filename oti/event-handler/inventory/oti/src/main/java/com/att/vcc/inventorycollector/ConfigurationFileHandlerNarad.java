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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.att.vcc.inventorycollector.data.ConfigurationData;
import com.att.vcc.inventorycollector.data.Events;
import com.att.vcc.inventorycollector.domain.DcaeEvent;
import com.att.vcc.inventorycollector.domain.CommunityString;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.messages.inventoryCollectorOperationEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.ecomp.logger.StatusCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import com.att.vcc.inventorycollector.schema.*;

/**
 * 
 * TODO: Refactor this class into separate util/helper methods.
 * 
 * @author ng346g
 * 
 */
public class ConfigurationFileHandlerNarad {

	private static Properties dtiProps = new Properties();
	private static PropertiesConfiguration dtiTaskRegister = new PropertiesConfiguration();
	private DBAdapter dbadapter;
	private static RESTClient restClient = null;
	private static String DCAE_LOCATION;
	private static String post_url;
	private static EcompLogger ecompLogger;
	private static String datetime;
	private static String NARAD_API_URL;
	private Map<String, String> topLevelData = new HashMap<>();
	private JSONObject entity = new JSONObject();

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public ConfigurationFileHandlerNarad() {
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

		restClient = new RESTClient();

		if (System.getenv("narad_api_url") != null && !System.getenv("narad_api_url").equalsIgnoreCase("")) {
			NARAD_API_URL = System.getenv("narad_api_url");
		} else {
			NARAD_API_URL = "";
		}
		String configDir = System.getenv("DTI_CONFIG");
		if (configDir == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
			return;
		}

		try (FileInputStream dtiStream = new FileInputStream(
				ValidationData.cleanPathString(Util.safeFileName(configDir) + "/dti.properties"));) {
			dtiProps.load(dtiStream);
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE, e.getMessage());
			return;
		}

		try (FileInputStream dtiTaskRegisterStream = new FileInputStream(
				ValidationData.cleanPathString(Util.safeFileName(configDir) + "/dti_task_register.properties"))) {
			try {
				dtiTaskRegister.setDelimiterParsingDisabled(true);
				dtiTaskRegister.load(dtiTaskRegisterStream);
			} catch (ConfigurationException e1) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE,
						e1.getMessage());
			}
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE, e.getMessage());
			return;
		}
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
				sendRequestToDTIHandler(events, "UPDATE", "Y");
			} catch (DTIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendRequestToDTIHandler(Events events, String dtiAction, String checkFile) throws DTIException {

		ObjectMapper mapper = new ObjectMapper();

		if (StringUtils.isBlank(post_url)) {
			if (dtiProps.containsKey("post_url"))
				post_url = dtiProps.getProperty("post_url");
			else
				post_url = System.getenv("ORCH_POST_URL");
		}

		DCAE_LOCATION = dtiProps.getProperty("DCAE_LOCATION");

		if (DCAE_LOCATION != null && !DCAE_LOCATION.equalsIgnoreCase("")) {
			events.setDcaeServiceLocation(DCAE_LOCATION);
		}

		String eventsJSON = null;
		JSONObject dcaeEventEvent = events.getEvent();
		int statusCode = 0;
		try {
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			events.setDcaeServiceAction(events.getDcaeServiceAction());
			// To address defect 715445
			// Set empty json for event field before sending to DTI2 Handler
			events.setEvent(new JSONObject());
			eventsJSON = mapper.writeValueAsString(events);
			// Reset event json to store in postgres DB
			events.setEvent(dcaeEventEvent);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new DTIException("Action: " + events.getDcaeServiceAction()
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
	}

	public String populateIpAddress(List<L3InterfaceIpv4AddressList> lIpv4, String subnetNamePattern) {

		String dcaeTargetCollectionIp = "";
		for (L3InterfaceIpv4AddressList ipv4 : lIpv4) {
			if (subnetNamePattern.equalsIgnoreCase("")) {
				dcaeTargetCollectionIp = ipv4.getL3InterfaceIpv4Address();
				break;
			}
			RelationshipList relList = ipv4.getRelationshipList();
			if (relList != null) {
				List<Relationship> lRelationship = relList.getRelationship();
				for (Relationship r : lRelationship) {
					if (r.getRelatedTo().equalsIgnoreCase("subnet")) {
						String subnetName = r.getRelatedToProperty().get(0).getPropertyValue();
						if (subnetName.toLowerCase().endsWith(subnetNamePattern)) {
							dcaeTargetCollectionIp = ipv4.getL3InterfaceIpv4Address();
							break;
						}
					}
				}
			}
			if (!dcaeTargetCollectionIp.equalsIgnoreCase(""))
				break;
		}
		return dcaeTargetCollectionIp;
	}

	public String getNaradTemplate(String entityType) {
		String template = "";
		String configDir = System.getenv("DTI_CONFIG");
		template = EventUtil.getTemplateContent(Util.safeFileName(configDir) + "/naradevent_pnf_template.json");
		return template;
	}

	public void generateNaradEvents(String entityType) {
		com.att.vcc.inventorycollector.domain.NaradPnf pnf = null;
		String pnfTemplate = null;
		pnfTemplate = getNaradTemplate(Constants.PNF_ENTITY);

		List<com.att.vcc.inventorycollector.domain.NaradPnf> outputListPnf = dbadapter.getAllNaradPnf();
		Iterator<com.att.vcc.inventorycollector.domain.NaradPnf> pnfIt = outputListPnf.iterator();
		while (pnfIt.hasNext()) {
			pnf = pnfIt.next();
			if (pnf != null && pnf.getPnfname() != null) {
				String naradeventTemplate = pnfTemplate;
				String entityUrl = "network/pnfs/pnf/" + pnf.getPnfname();
				String pnfUrl = EventUtil.API_URL + entityUrl;
				String entityLink = EventUtil.API_URL.substring(EventUtil.API_URL.indexOf("/narad/")) + entityUrl;
				naradeventTemplate = naradeventTemplate.replace(Constants.ENTITY_LINK, entityLink);
				replaceEntityValueAndProcessEvent(pnfUrl, naradeventTemplate);
			}
		}

	}

	public void replaceEntityValueAndProcessEvent(String naradUrl, String naradeventTemplate) {
		String response = null;
		try {
			response = restClient.retrieveAAIObject(naradUrl);
			if (!StringUtils.isBlank(response)) {
				naradeventTemplate = naradeventTemplate.replace("ENTITY_VALUE", response);
				getNARADUpdates(naradeventTemplate);
			}
		} catch (DTIException e) {
			e.printStackTrace();
		}
	}

	public void getNARADUpdates(String jsonMessage) throws DTIException {
		if (StringUtils.isBlank(jsonMessage) || jsonMessage.contains("Error") || jsonMessage.contains("Exception"))
			return;

		ObjectMapper mapper = new ObjectMapper();
		// mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());

		datetime = EventUtil.getCurrentTimestamp();
		JSONObject eventJSONObject;
		eventJSONObject = new JSONObject(jsonMessage);
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_RECEIVED_AAI_EVENT, jsonMessage);
		ConfigurationData configData = new ConfigurationData();

		if (eventJSONObject != null) {

			JSONObject header = eventJSONObject.optJSONObject("event-header");
			this.entity = eventJSONObject.optJSONObject("entity");
			String eventType = header.getString("event-type");
			String entityType = header == null ? null : header.getString("entity-type");

			// remove /narad/v3/ from NARAD_API_URL
			String appendUrl = NARAD_API_URL.substring(0, NARAD_API_URL.length() - 9);

			if (entity == null || header == null || entityType == null) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROCESS_EVENT_FAILURE);
				return;
			}

			String pnfPolicyPath = ValidationData
					.cleanPathString(System.getenv("DTI") + dtiProps.getProperty("pnf_policy_path"));

			final String pnfFilePattern = entityType;
			File dir = new File(pnfPolicyPath);
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(pnfFilePattern);
				}
			});

			String hasPolicy = "N";
			for (File pnfPropFile : files) {
				hasPolicy = "Y";

				// Properties pnfPolicy = new Properties();
				PropertiesConfiguration pnfPolicy = new PropertiesConfiguration();

				FileInputStream dtiPnfPolicy = null;

				try {
					dtiPnfPolicy = new FileInputStream(
							ValidationData.cleanPathString(Util.safeFileName(pnfPropFile.getAbsolutePath())));
					pnfPolicy.setDelimiterParsingDisabled(true);
					pnfPolicy.load(dtiPnfPolicy);
				} catch (IOException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE,
							e.getMessage());
					return;
				} catch (ConfigurationException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_PROPS_LOAD_FAILURE,
							e.getMessage());
				} finally {
					Util.closeFileInputStream(dtiPnfPolicy);
				}

				String policyEventType = pnfPolicy.getString("event-type");
				if (eventType.equalsIgnoreCase(policyEventType)) {
					JSONObject json = new JSONObject();

					String dcaeTargetName = entity.getString("pnf-name");
					topLevelData.put("dcaeTargetName", Optional.ofNullable(dcaeTargetName).orElse(""));
					String pattern = this.getPolicyValue("namePattern", pnfPolicy);
					String functionCode = dcaeTargetName.substring(dcaeTargetName.length() - 3);
					topLevelData.put("functionCode", Optional.ofNullable(functionCode).orElse(""));
					String policyFunctionCode = this.getPolicyValue("functionCode", pnfPolicy);
					if (!policyFunctionCode.contains(functionCode)) {
						ecompLogger.debug("Device Name (" + dcaeTargetName + ") does not match the function code ("
								+ policyFunctionCode + ")");
						continue;
					} else {
						ecompLogger.debug("Device Name (" + dcaeTargetName + ") contains the function code ("
								+ policyFunctionCode + ")");
					}

					if (!dcaeTargetName.matches(pattern)) {
						ecompLogger.debug(
								"Device Name (" + dcaeTargetName + ") does not match the pattern (" + pattern + ")");
						continue;
					} else {
						ecompLogger.debug(
								"Device Name (" + dcaeTargetName + ") matches the pattern (" + pattern + ")");
					}

					String dcaeTargetType = this.getPolicyValue("dcaeTargetType", pnfPolicy);
					topLevelData.put("dcaeTargetType", Optional.ofNullable(dcaeTargetType).orElse(""));

					String dcaeServiceLocation = this.getPolicyValue("dcaeServiceLocation", pnfPolicy);
					topLevelData.put("dcaeServiceLocation", Optional.ofNullable(dcaeServiceLocation).orElse(""));

					String dcaeServiceAction = header.getString("action");
					dcaeServiceAction = this.getPolicyValue("mapRuleAction_" + dcaeServiceAction, pnfPolicy);
					if (dcaeServiceAction == null || dcaeServiceAction.equalsIgnoreCase("")) {
						dcaeServiceAction = "deploy"; // default action
					}
					topLevelData.put("dcaeServiceAction", dcaeServiceAction);
					String dcaeGenericVnfModelInvariantId = this.getPolicyValue("dcaeGenericVnfModelInvariantId",
							pnfPolicy);
					String dcaeGenericVnfModelVersionId = this.getPolicyValue("dcaeGenericVnfModelVersionId",
							pnfPolicy);
					String dcaeServiceInstanceModelInvariantId = this
							.getPolicyValue("dcaeServiceInstanceModelInvariantId", pnfPolicy);
					String dcaeServiceInstanceModelVersionId = this.getPolicyValue("dcaeServiceInstanceModelVersionId",
							pnfPolicy);
					String dcaeServiceType = this.getPolicyValue("dcaeServiceType", pnfPolicy);
					String dcaeSnmpCommunityString = pnfPolicy.getString("dcaeSnmpCommunityString");
					String dcaeSnmpVersion = pnfPolicy.getString("dcaeSnmpVersion");
					if (dcaeSnmpCommunityString.startsWith("db.community_string")
							&& dcaeSnmpVersion.startsWith("db.community_string")) {
						CommunityString communityStringObj = null;
						dcaeSnmpCommunityString = "";
						dcaeSnmpVersion = "";
						List<CommunityString> outputList = dbadapter.getCommunityString(dcaeTargetType, dcaeTargetName);
						Iterator<CommunityString> it = outputList.iterator();
						if (it.hasNext()) {
							communityStringObj = it.next();
							dcaeSnmpCommunityString = communityStringObj.getCommunitystring();
							dcaeSnmpVersion = communityStringObj.getSnmpversion();
						}
					} else {
						ecompLogger.debug("Wrong format for dcaeSnmpCommunityString or dcaeSnmpVersion from policy");
						//return; //for cases that no community string is required
					}
					String dcaeTargetCloudRegionId = this.getPolicyValue("dcaeTargetCloudRegionId", pnfPolicy);
					String dcaeTargetCloudRegionVersion = this.getPolicyValue("dcaeTargetCloudRegionVersion",
							pnfPolicy);
					String dcaeTargetCollection = this.getPolicyValue("dcaeTargetCollection", pnfPolicy);
					String dcaeTargetInMaint = Constants.FALSE;
					if (entity.has("in-maint")) {
						if (entity.getBoolean("in-maint"))
							configData.setIsInMaint(Constants.TRUE);
					}
					String dcaeTargetIsClosedLoopDisabled = Constants.FALSE;

					String dcaeTargetProvStatus = null;
					if (entity.has("prov-status")) {
						dcaeTargetProvStatus = entity.getString("prov-status");
					}
					if (dcaeTargetProvStatus == null
							|| !dtiProps.getProperty("narad-prov-status").contains(dcaeTargetProvStatus)) {
						ecompLogger.debug(
								"prov-status is either null or not " + dtiProps.getProperty("narad-prov-status"));
						continue;
					}
					topLevelData.put("dcaeTargetInMaint", dcaeTargetInMaint);
					topLevelData.put("dcaeTargetProvStatus", dcaeTargetProvStatus);

					String dcaeTargetServiceDescription = this.getPolicyValue("dcaeTargetServiceDescription",
							pnfPolicy);
					String dcaeTargetCollectionIp = "";
					String pnfSelfLinkUrl = "network/pnfs/pnf/" + dcaeTargetName + "?depth=all";
					String pnfUrl = NARAD_API_URL + pnfSelfLinkUrl;
					System.out.println(pnfUrl);
					String response = restClient.retrieveAAIObject(pnfUrl);
					if (StringUtils.isBlank(response)) {
						ecompLogger.debug("Not able to get response from " + NARAD_API_URL + pnfSelfLinkUrl);
					} else {
						try {
							Pnf pnf = mapper.readValue(response, Pnf.class);
							LInterfaces lInterfaces = pnf.getLInterfaces();
							dcaeTargetCollectionIp = "";
							if (functionCode.equalsIgnoreCase("nm4")) {
								PInterfaces pInterfaces = pnf.getPInterfaces();
								if (pInterfaces != null) {
									List<PInterface> lPinterfaces = pInterfaces.getPInterface();
									for (PInterface pInterface : lPinterfaces) {
										String intName = pInterface.getInterfaceName();
										if (intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName3"))) {
											dcaeTargetCollectionIp = populateIpAddress(
													pInterface.getL3InterfaceIpv4AddressList(), "");
										}
										if (!dcaeTargetCollectionIp.equalsIgnoreCase(""))
											break;
									}
								}
							} else {
								if (lInterfaces != null) {
									List<LInterface> lLinterfaces = lInterfaces.getLInterface();

									for (LInterface lInterface : lLinterfaces) {
										String intName = lInterface.getInterfaceName();

										// This logic needs to be updated for
										// for future release with parse
										// template logic Which is more generic
										if (functionCode.equalsIgnoreCase("es2")
												&& intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName1"))) {
											dcaeTargetCollectionIp = populateIpAddress(
													lInterface.getL3InterfaceIpv4AddressList(), "loopback0-1");
										} else if (functionCode.equalsIgnoreCase("el1")
												&& intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName1"))) {
											dcaeTargetCollectionIp = populateIpAddress(
													lInterface.getL3InterfaceIpv4AddressList(), "loopback0");
										} else if (functionCode.equalsIgnoreCase("jl1")
												&& intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName2"))) {
											dcaeTargetCollectionIp = populateIpAddress(
													lInterface.getL3InterfaceIpv4AddressList(), "loopback0");
										} else if (functionCode.equalsIgnoreCase("sx2") && (intName
												.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName4"))
												|| intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName5")))) {
											dcaeTargetCollectionIp = populateIpAddress(
													lInterface.getL3InterfaceIpv4AddressList(), "");
										} else if ((functionCode.equalsIgnoreCase("cwp")
												|| functionCode.equalsIgnoreCase("swc") 
												|| functionCode.equalsIgnoreCase("swx") 
												|| functionCode.equalsIgnoreCase("swy")
												|| functionCode.equalsIgnoreCase("swz"))
												&& intName.equalsIgnoreCase(dtiProps.getProperty("lInterfaceName1"))) {
											dcaeTargetCollectionIp = populateIpAddress(
													lInterface.getL3InterfaceIpv4AddressList(), "");
										} // Added for P1

										if (!dcaeTargetCollectionIp.equalsIgnoreCase(""))
											break;
									}
								}
							}
						} catch (JsonParseException e) {
							ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION,
									e.toString());
						} catch (JsonMappingException e) {
							ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION,
									e.toString());
						} catch (IOException e) {
							ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION,
									e.toString());
						}
						if (dcaeTargetCollectionIp == null || dcaeTargetCollectionIp.equalsIgnoreCase("")) {
							ecompLogger.debug("Problem with populating IP address.");
						}

						topLevelData.put("dcaeTargetCollectionIp",
								Optional.ofNullable(dcaeTargetCollectionIp).orElse(""));
						if (pnfPolicy.getString("collectorType").equalsIgnoreCase("stap")) {
							// Skip populating aai_additinoal_info for status
							// poller
							ecompLogger.debug(
									"collectorType is stap. Skip populating aai_additinoal_info for status poller");
						} // else if
							// (pnfPolicy.getString("collectorType").equalsIgnoreCase("snmp"))
							// {
						else if (dtiProps.getProperty("PMCOLLECTORS").contains(pnfPolicy.getString("collectorType"))) {
							String taskIdsMapStr = this.getPolicyValue("aaiAdditionalInfo-taskIds", pnfPolicy);
							String[] taskIds = taskIdsMapStr.split(",");
							String nodeType = "";
							String nodeSubtype = "";
							String serviceType = "";
							String vnfType = "";
							String vnfcFuncId = "";
							String remoteServerGroup = "g1";
							String collectionType = "";
							String subType = "";
							String protocol = "";
							String collectionInterval = "";
							String priority = "";
							String description = "";
							String isPrimary = "True";
							JSONObject taskItemObj = new JSONObject();
							for (String taskId : taskIds) {
								String taskInfo = dtiTaskRegister.getString(taskId);
								if (taskInfo == null || taskInfo.equalsIgnoreCase("")) {
									ecompLogger.debug("No task definition available for task (" + taskId + ")");
								} else {
									String[] taskDetails = taskInfo.split("~");
									nodeType = this.getPolicyValue("aaiAdditionalInfo-nodeType", pnfPolicy);
									nodeSubtype = this.getPolicyValue("aaiAdditionalInfo-nodeSubtype", pnfPolicy);
									serviceType = this.getPolicyValue("aaiAdditionalInfo-serviceType", pnfPolicy);
									vnfType = this.getPolicyValue("aaiAdditionalInfo-vnfType", pnfPolicy);
									vnfcFuncId = this.getPolicyValue("aaiAdditionalInfo-functionCode", pnfPolicy);
									collectionType = taskDetails[0];
									subType = taskDetails[1];
									protocol = taskDetails[2];
									collectionInterval = this.getPolicyValue("aaiAdditionalInfo-collectionInterval", pnfPolicy);
									if ((collectionInterval == null) || (collectionInterval.length() == 0)) {
										collectionInterval = taskDetails[3];										
									}
									priority = taskDetails[4];
									description = taskDetails[5];
									String targetNameTaskId = dcaeTargetName + "_" + taskId;
									JSONObject serverGroupObj = new JSONObject();
									serverGroupObj.put("isPrimary", isPrimary);
									serverGroupObj.put("remoteServerIp",
											this.getPolicyValue("aaiAdditionalInfo-remoteServerIp", pnfPolicy));
									serverGroupObj.put("remoteServerName",
											this.getPolicyValue("aaiAdditionalInfo-remoteServerName", pnfPolicy));
									serverGroupObj.put("inMaintenance",
											this.getPolicyValue("aaiAdditionalInfo-inMaintenance", pnfPolicy));
									
									serverGroupObj.put("networkId",
											this.getPolicyValue("aaiAdditionalInfo-netId", pnfPolicy));
																		
									serverGroupObj.put("provStatus",
											this.getPolicyValue("aaiAdditionalInfo-provStatus", pnfPolicy));
									JSONArray serverGroupArr = new JSONArray();
									serverGroupArr.put(serverGroupObj);
									JSONObject remoteServerGroupG1 = new JSONObject();
									remoteServerGroupG1.put("serverGroupId", remoteServerGroup);
									remoteServerGroupG1.put("serverGroup", serverGroupArr);
									JSONObject remoteServerGroups = new JSONObject();
									remoteServerGroups.put(remoteServerGroup, remoteServerGroupG1);
									JSONObject targetNameTaskIdObj = new JSONObject();
									targetNameTaskIdObj.put("taskId", taskId);
									targetNameTaskIdObj.put("collectionType", collectionType);
									targetNameTaskIdObj.put("subType", subType);
									targetNameTaskIdObj.put("protocol", protocol);
									targetNameTaskIdObj.put("nodeType", nodeType);
									targetNameTaskIdObj.put("nodeSubtype", nodeSubtype);
									targetNameTaskIdObj.put("serviceType", serviceType);
									targetNameTaskIdObj.put("collectionInterval", collectionInterval);
									targetNameTaskIdObj.put("vnfType", vnfType);
									targetNameTaskIdObj.put("vnfcFuncId", vnfcFuncId);
									targetNameTaskIdObj.put("priority", priority);
									targetNameTaskIdObj.put("description", description);
									targetNameTaskIdObj.put("remoteServerGroups", remoteServerGroups);
									taskItemObj.put(targetNameTaskId, targetNameTaskIdObj);
								}
							}
							json.put("TasksItems", taskItemObj);

						} else {
							String changeType = pnfPolicy.getString("aaiAdditionalInfo-changeType");
							if (changeType.startsWith("<narad-event.event-header.")) {
								String fieldName = changeType.substring(changeType.indexOf("<"),
										changeType.indexOf(">"));
								fieldName = fieldName.replace("<narad-event.event-header.", "");
								if (header.has(fieldName)) {
									changeType = header.getString(fieldName);
								}
								changeType = pnfPolicy.getString("mapRule_" + changeType);
								json.put("changeType", changeType);
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-hostName")
									.equalsIgnoreCase("<dcaeTargetName>")) {
								json.put("hostName", dcaeTargetName);
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-functionCode")
									.equalsIgnoreCase("<functionCode>")) {
								json.put("functionCode", pnfPolicy.getString("functionCode"));
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-deviceType")
									.equalsIgnoreCase("<dcaeServiceType>")) {
								json.put("deviceType", dcaeServiceType);
							}
							json.put("entityType", pnfPolicy.getString("aaiAdditionalInfo-entityType"));
							if (pnfPolicy.getString("aaiAdditionalInfo-location")
									.equalsIgnoreCase("<dcaeServiceLocation>")) {
								json.put("location", dcaeServiceLocation);
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-snmpIP")
									.equalsIgnoreCase("<dcaeTargetCollectionIp>")) {
								json.put("snmpIP", dcaeTargetCollectionIp);
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-snmpVersion")
									.equalsIgnoreCase("<dcaeSnmpVersion>")) {
								json.put("snmpVersion", dcaeSnmpVersion);
							}
							json.put("outputObjects", pnfPolicy.getString("aaiAdditionalInfo-outputObjects"));
							if (pnfPolicy.getString("aaiAdditionalInfo-communityString")
									.equalsIgnoreCase("<dcaeSnmpCommunityString>")) {
								json.put("communityString", dcaeSnmpCommunityString);
							}
							if (pnfPolicy.getString("aaiAdditionalInfo-deviceChangeTimeStamp")
									.equalsIgnoreCase("<currentTimeStamp>")) {
								json.put("deviceChangeTimeStamp", EventUtil.getCurrentTimestamp());
							}
							if (pnfPolicy.getString("collectorType").equalsIgnoreCase("mib")) {
								json.put("FMMIBPollerIntervalClass",
										pnfPolicy.getString("aaiAdditionalInfo-FMMIBPollerIntervalClass"));
							}

							JSONObject relationListObject = entity.optJSONObject("relationship-list");
							String designType = pnfPolicy.getString("aaiAdditionalInfo-designType");
							if (designType.startsWith("related-to")) {
								String[] desTypArr = designType.split(",");
								String[] ZoneObj = desTypArr[0].split(":");
								String[] relationData = getRelationshipData(relationListObject, ZoneObj[1],
										desTypArr[1]);
								if (relationData != null && relationData.length > 0) {
									response = restClient.retrieveAAIObject(appendUrl + relationData[0]);
									if (StringUtils.isNotBlank(response)) {
										JSONObject jObject = new JSONObject(response);
										String[] zoneField = desTypArr[1].split(".");
										if (jObject.has(zoneField[1])) {
											designType = jObject.getString(zoneField[1]);
										}
									}
								} else
									return;
							}
						}
					}
					Events events = new Events(dcaeTargetName, dcaeTargetType, dcaeServiceAction, dcaeServiceLocation,
							dcaeServiceType, dcaeTargetProvStatus, dcaeTargetInMaint, dcaeTargetIsClosedLoopDisabled,
							dcaeServiceInstanceModelInvariantId, dcaeServiceInstanceModelVersionId,
							dcaeGenericVnfModelInvariantId, dcaeGenericVnfModelVersionId, dcaeTargetCollection,
							dcaeTargetCollectionIp, dcaeSnmpCommunityString, dcaeSnmpVersion, dcaeTargetCloudRegionId,
							dcaeTargetCloudRegionVersion, dcaeTargetServiceDescription, entity, json);
					sendRequestToDTIHandler(events, dcaeServiceAction, "Y");
				} else {
					ecompLogger.debug("Skipping the event, we are expecting event type (" + policyEventType
							+ ") but got event type (" + eventType + ")");
				}
			}
			if ("N".equalsIgnoreCase(hasPolicy)) {
				ecompLogger.debug("Skipping the event, as we don't have policy for event type: " + eventType);
			}
		}
	}

	private String getPolicyValue(String policyKey, PropertiesConfiguration pnfPolicy) {
		String policyValue = pnfPolicy.getString(policyKey);
		//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the initial policyValue is " + policyValue);
		boolean containsMapRule = false;
		if (policyValue == null || policyValue.equals("")) {
			ecompLogger.debug("No value available for the policy key (" + policyKey + ")");
			return "";
		}
		if (policyValue.contains("mapRule")) {
			//handle the logic for mapRule
			//aaiAdditionalInfo-netId=<related-to=cloud-region,cloud-region.cloud-owner>|mapRule
			String[] policyValueArr = null;
			if (policyValue.contains("|")) {
				policyValueArr = policyValue.split("\\|");
			//	ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the policy has |mapRule, for " + policyValue + " and the policyValueArr[0]="+policyValueArr[0]);
			} else if (policyValue.contains("!")) {
				policyValueArr = policyValue.split("!");
			//	ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the policy has !mapRule, for " + policyValue + " and the policyValueArr[0]="+policyValueArr[0]);
			}
			policyValue = policyValueArr[0];
			containsMapRule = true;
		}
		
		if (policyValue.startsWith("<"))
			policyValue = policyValue.substring(1);
		if (policyValue.endsWith(">"))
			policyValue = policyValue.substring(0, policyValue.length() - 1);
		String[] policyValueArr = policyValue.split(">-<");
		if (policyValueArr.length == 2 && policyValueArr[1] != null && !policyValueArr[1].equalsIgnoreCase("")) {
			policyValueArr[1] = this.transformValue(policyValueArr[1], pnfPolicy);
		}
		if (policyValueArr[0] != null && !policyValueArr[0].equalsIgnoreCase("")) {
			// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "policyValueArr[0] is " + policyValueArr[0]);
			policyValueArr[0] = this.transformValue(policyValueArr[0], pnfPolicy);
		}
		if (policyValueArr.length == 2) {
			policyValue = policyValueArr[0] + '-' + policyValueArr[1];
		} else if (policyValueArr.length == 1) {
			policyValue = policyValueArr[0];
		}
		if (containsMapRule) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the policy has mapRule, for " + policyValue);
			policyValue = pnfPolicy.getString("mapRule_" + policyValue);
			return policyValue;
		} else {
			return policyValue;
		}
			
	}

	private String transformValue(String policyValue, PropertiesConfiguration pnfPolicy) {
		policyValue = policyValue.trim();
		String value = policyValue;
		if (policyValue.startsWith("<"))
			policyValue = policyValue.substring(1);
		if (policyValue.endsWith(">"))
			policyValue = policyValue.substring(0, policyValue.length() - 1);
		JSONObject relationListObject = entity.optJSONObject("relationship-list");
		if (policyValue.startsWith("narad.pnf")) {
			policyValue = policyValue.replace("narad.pnf.", "");
			if (entity.has(policyValue)) {
				value = entity.getString(policyValue);
			}
		} else if (policyValue.startsWith("related-to")) {
			//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "the policy has related-to, for " + policyValue);
			String[] relArr = policyValue.split(",");
			String[] relObj = null;
			if (relArr[0].contains(":")) {
				relObj = relArr[0].split(":");
			} else if (relArr[0].contains("=")) {
				relObj = relArr[0].split("=");
			}			
			String[] relationData = getRelationshipData(relationListObject, relObj[1], relArr[1]);
			if (relationData != null && relationData.length > 0) {
				value = relationData[1];
				//ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "found the  related-to value " + value);
			} else {
				value = "";
			}
		} else if (policyValue.startsWith("vvv")) {
			value = this.topLevelData.containsKey("functionCode") ? this.topLevelData.get("functionCode") : "";
		} else if (policyValue.startsWith("dcae")) {
			value = this.topLevelData.containsKey(policyValue) ? this.topLevelData.get(policyValue) : "";
		} else {
			if (pnfPolicy.containsKey(policyValue)) {
				value = pnfPolicy.getString(policyValue);
			}
		}
		return value;
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
		 ecompLogger.debug(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_INSIDE_FUNCTION_DEBUG, "getRelationshipData, with relatedTo =" + relatedTo + " and relationshipKey ="+ relationshipKey);

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
		if (relationData[0] != null && !relationData[0].equalsIgnoreCase("")) {
			return relationData;
		}
		return null;
	}

}
