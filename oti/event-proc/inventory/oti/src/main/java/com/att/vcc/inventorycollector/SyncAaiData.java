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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.schema.*;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncAaiData {
	private static Properties dtiProps = new Properties();

	private static String aaiApiUrl;
	private static RESTClient restClient;
	private static DBAdapter dbadapter;
	private static AAIEventProcessor aaiEventProcessor;

	private static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	private boolean storeAaiEntities(String aaiAPIUrl, String aaiEntity) {
		boolean status = false;
		String dateTimeStampLastWeek = EventUtil.getTimestamp(Constants.DAYS_IN_A_WEEK);
		System.out.println(dateTimeStampLastWeek + " aai Entity: " + aaiEntity);
		try {
			String response = restClient.retrieveAAIObject(aaiAPIUrl);
			if (!response.equalsIgnoreCase("")) {
				Map<String, String> dbModifiedRecords = dbadapter.getModifiedRecordsUsingUpdatedOn(aaiEntity,
						dateTimeStampLastWeek);
				JSONObject responseJSONObject = null;
				responseJSONObject = new JSONObject(response);
				JSONArray entitiesArray = responseJSONObject.optJSONArray("results");
				int totalRecords = entitiesArray.length();
				status = true;
				for (int i = 0; i < totalRecords; i++) {
					String resourceVersion = entitiesArray.optJSONObject(i).getString("resource-version");
					String url = entitiesArray.optJSONObject(i).getString("resource-link");
					Map<String, String> urlValues = EventUtil.parseUrl(EventUtil.aaiEntities, url);
					List<String> primaryKey = new ArrayList<>();
					String hostname = Optional.ofNullable(urlValues.get(Constants.PSERVER_ENTITY)).orElse("");
					String pnfName = Optional.ofNullable(urlValues.get(Constants.PNF_ENTITY)).orElse("");
					String networkId = Optional.ofNullable(urlValues.get(Constants.L3_NETWORK_ENTITY)).orElse("");
					String lagInterfaceName = Optional.ofNullable(urlValues.get(Constants.LAG_INTERFACE_ENTITY))
							.orElse("");
					String lInterfaceName = Optional.ofNullable(urlValues.get(Constants.L_INTERFACE_ENTITY)).orElse("");
					String pInterfaceName = Optional.ofNullable(urlValues.get(Constants.P_INTERFACE_ENTITY)).orElse("");
					String vlanInterface = Optional.ofNullable(urlValues.get(Constants.VLAN_ENTITY)).orElse("");
					String cloudOwner = Optional.ofNullable(urlValues.get(Constants.CLOUD_OWNER)).orElse("");
					String cloudRegionId = Optional.ofNullable(urlValues.get(Constants.CLOUD_REGION_ENTITY)).orElse("");
					String tenantId = Optional.ofNullable(urlValues.get(Constants.TENANT_ENTITY)).orElse("");
					String vserverId = Optional.ofNullable(urlValues.get(Constants.VSERVER_ENTITY)).orElse("");
					String equipmentName = Optional.ofNullable(urlValues.get(Constants.VPLS_PE_ENTITY)).orElse("");
					String modelInvariantId = Optional.ofNullable(urlValues.get(Constants.MODEL_ENTITY)).orElse("");
					String vnfcName = Optional.ofNullable(urlValues.get(Constants.VNFC_ENTITY)).orElse("");
					String cpInstanceId = Optional.ofNullable(urlValues.get(Constants.CP_ENTITY)).orElse("");
					String forwardingPathId = Optional.ofNullable(urlValues.get(Constants.FORWARDING_PATH_ENTITY))
							.orElse("");
					String globalCustomerId = Optional.ofNullable(urlValues.get(Constants.CUSTOMER_ENTITY)).orElse("");
					String serviceType = Optional.ofNullable(urlValues.get(Constants.SERVICE_SUBSCRIPTION_ENTITY))
							.orElse("");
					String serviceInstanceId = Optional.ofNullable(urlValues.get(Constants.SERVICE_INSTANCE_ENTITY))
							.orElse("");
					String vnfId = Optional.ofNullable(urlValues.get(Constants.GENERIC_VNF_ENTITY)).orElse("");
					String vceVnfId = Optional.ofNullable(urlValues.get(Constants.VCE_ENTITY)).orElse("");
					String newvceVnfId = Optional.ofNullable(urlValues.get(Constants.NEWVCE_ENTITY)).orElse("");
					String parentEntityType = "";
					String parentEntityId = "";
					String grandParentEntityType = "";
					String grandParentEntityId = "";

					if (!newvceVnfId.equalsIgnoreCase("")) {
						ecompLogger.debug("skipping newvce related object");
						continue;
					}
					// Add proper primary key so that we would be able to
					// compare it with DB records.
					switch (aaiEntity) {
					case Constants.ALLOTTED_RESOURCE_ENTITY:
						primaryKey.add(globalCustomerId);
						primaryKey.add(serviceType);
						primaryKey.add(serviceInstanceId);
						primaryKey.add(urlValues.get(Constants.ALLOTTED_RESOURCE_ENTITY));
						break;
					case Constants.AVAILABILITY_ZONE_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.AVAILABILITY_ZONE_ENTITY));
						break;
					case Constants.CLOUD_REGION_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						break;
					case Constants.COMPLEX_ENTITY:
						primaryKey.add(urlValues.get(Constants.COMPLEX_ENTITY));
						break;
					case Constants.CP_ENTITY:
						primaryKey.add(vnfcName);
						primaryKey.add(cpInstanceId);
						break;
					case Constants.CUSTOMER_ENTITY:
						primaryKey.add(globalCustomerId);
						break;
					case Constants.FLAVOR_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.FLAVOR_ENTITY));
						break;
					case Constants.FORWARDER_ENTITY:
						primaryKey.add(forwardingPathId);
						primaryKey.add(urlValues.get(Constants.FORWARDER_ENTITY));
						break;
					case Constants.FORWARDING_PATH_ENTITY:
						primaryKey.add(forwardingPathId);
						break;
					case Constants.GENERIC_VNF_ENTITY:
						primaryKey.add(vnfId);
						break;
					case Constants.IMAGE_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.IMAGE_ENTITY));
						break;
					case Constants.L_INTERFACE_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							parentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							parentEntityId = pnfName;
						} else if (!vserverId.equalsIgnoreCase("")) {
							parentEntityId = vserverId;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							parentEntityId = equipmentName;
						} else if (!vnfId.equalsIgnoreCase("")) {
							parentEntityId = vnfId;
						}
						primaryKey.add(parentEntityId);
						primaryKey.add(lInterfaceName);
						break;
					case Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							grandParentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							grandParentEntityId = pnfName;
						} else if (!vserverId.equalsIgnoreCase("")) {
							grandParentEntityId = vserverId;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							grandParentEntityId = equipmentName;
						} else if (!vnfId.equalsIgnoreCase("")) {
							grandParentEntityId = vnfId;
						} else if (!vnfcName.equalsIgnoreCase("")) {
							grandParentEntityId = vnfcName;
						}
						primaryKey.add(grandParentEntityId);
						primaryKey.add(urlValues.get(Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY));
						break;
					case Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							grandParentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							grandParentEntityId = pnfName;
						} else if (!vserverId.equalsIgnoreCase("")) {
							grandParentEntityId = vserverId;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							grandParentEntityId = equipmentName;
						} else if (!vnfId.equalsIgnoreCase("")) {
							grandParentEntityId = vnfId;
						} else if (!vnfcName.equalsIgnoreCase("")) {
							grandParentEntityId = vnfcName;
						}
						primaryKey.add(grandParentEntityId);
						primaryKey.add(urlValues.get(Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY));
						break;
					case Constants.L3_NETWORK_ENTITY:
						primaryKey.add(networkId);
						break;
					case Constants.LAG_INTERFACE_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							parentEntityType = Constants.PSERVER_ENTITY;
							parentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							parentEntityType = Constants.PNF_ENTITY;
							parentEntityId = pnfName;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							parentEntityType = Constants.VPLS_PE_ENTITY;
							parentEntityId = equipmentName;
						} else if (!vnfId.equalsIgnoreCase("")) {
							parentEntityType = Constants.GENERIC_VNF_ENTITY;
							parentEntityId = vnfId;
						}
						primaryKey.add(parentEntityType);
						primaryKey.add(parentEntityId);
						primaryKey.add(lagInterfaceName);
						break;
					case Constants.LAG_LINK_ENTITY:
						primaryKey.add(urlValues.get(Constants.LAG_LINK_ENTITY));
						break;
					case Constants.LINE_OF_BUSINESS_ENTITY:
						primaryKey.add(urlValues.get(Constants.LINE_OF_BUSINESS_ENTITY));
						break;
					case Constants.LOGICAL_LINK_ENTITY:
						primaryKey.add(urlValues.get(Constants.LOGICAL_LINK_ENTITY));
						break;
					case Constants.MODEL_ENTITY:
						primaryKey.add(modelInvariantId);
						break;
					case Constants.MODEL_VER_ENTITY:
						primaryKey.add(modelInvariantId);
						primaryKey.add(urlValues.get(Constants.MODEL_VER_ENTITY));
						break;
					case Constants.NETWORK_PROFILE_ENTITY:
						primaryKey.add(urlValues.get(Constants.NETWORK_PROFILE_ENTITY));
						break;
					case Constants.NOS_SERVER_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(tenantId);
						primaryKey.add(urlValues.get(Constants.AVAILABILITY_ZONE_ENTITY));
						break;
					case Constants.OAM_NETWORK_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.OAM_NETWORK_ENTITY));
						break;
					case Constants.OPERATIONAL_ENVIRONMENT_ENTITY:
						primaryKey.add(urlValues.get(Constants.OPERATIONAL_ENVIRONMENT_ENTITY));
						break;
					case Constants.OWNING_ENTITY_ENTITY:
						primaryKey.add(urlValues.get(Constants.OWNING_ENTITY_ENTITY));
						break;
					case Constants.P_INTERFACE_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							parentEntityType = Constants.PSERVER_ENTITY;
							parentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							parentEntityType = Constants.PNF_ENTITY;
							parentEntityId = pnfName;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							parentEntityType = Constants.VPLS_PE_ENTITY;
							parentEntityId = equipmentName;
						}
						primaryKey.add(parentEntityType);
						primaryKey.add(parentEntityId);
						primaryKey.add(pInterfaceName);
						break;
					case Constants.PHYSICAL_LINK_ENTITY:
						primaryKey.add(urlValues.get(Constants.PHYSICAL_LINK_ENTITY));
						break;
					case Constants.PLATFORM_ENTITY:
						primaryKey.add(urlValues.get(Constants.PLATFORM_ENTITY));
						break;
					case Constants.PNF_ENTITY:
						primaryKey.add(pnfName);
						break;
					case Constants.PORT_GROUP_ENTITY:
						primaryKey.add(vceVnfId);
						primaryKey.add(urlValues.get(Constants.PORT_GROUP_ENTITY));
						break;
					case Constants.PROJECT_ENTITY:
						primaryKey.add(urlValues.get(Constants.PORT_GROUP_ENTITY));
						break;
					case Constants.PSERVER_ENTITY:
						primaryKey.add(hostname);
						break;
					case Constants.SERVICE_ENTITY:
						primaryKey.add(urlValues.get(Constants.SERVICE_ENTITY));
						break;
					case Constants.SERVICE_CAPABILITY_ENTITY:
						primaryKey.add(serviceType);
						break;
					case Constants.SERVICE_INSTANCE_ENTITY:
						primaryKey.add(globalCustomerId);
						primaryKey.add(serviceType);
						primaryKey.add(serviceInstanceId);
						break;
					case Constants.SERVICE_SUBSCRIPTION_ENTITY:
						primaryKey.add(globalCustomerId);
						primaryKey.add(serviceType);
						break;
					case Constants.SRIOV_PF_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							grandParentEntityType = Constants.PSERVER_ENTITY;
							grandParentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							grandParentEntityType = Constants.PNF_ENTITY;
							grandParentEntityId = pnfName;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							grandParentEntityType = Constants.VPLS_PE_ENTITY;
							grandParentEntityId = equipmentName;
						}
						primaryKey.add(grandParentEntityType);
						primaryKey.add(grandParentEntityId);
						primaryKey.add(lInterfaceName);
						primaryKey.add(urlValues.get(Constants.SRIOV_PF_ENTITY));
						break;
					case Constants.SRIOV_VF_ENTITY:
						if (!vnfId.equalsIgnoreCase("")) {
							grandParentEntityId = vnfId;
						} else if (!pnfName.equalsIgnoreCase("")) {
							grandParentEntityId = pnfName;
						} else if (!vserverId.equalsIgnoreCase("")) {
							grandParentEntityId = vserverId;
						}
						primaryKey.add(grandParentEntityId);
						primaryKey.add(urlValues.get(Constants.SRIOV_VF_ENTITY));
						break;
					case Constants.SUBNET_ENTITY:
						primaryKey.add(networkId);
						primaryKey.add(urlValues.get(Constants.SUBNET_ENTITY));
						break;
					case Constants.TENANT_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(tenantId);
						break;
					case Constants.VCE_ENTITY:
						primaryKey.add(vceVnfId);
						break;
					case Constants.VF_MODULE_ENTITY:
						primaryKey.add(urlValues.get(Constants.VF_MODULE_ENTITY));
						break;
					case Constants.VIP_IPV4_ADDRESS_LIST_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.VIP_IPV4_ADDRESS_LIST_ENTITY));
						break;
					case Constants.VIP_IPV6_ADDRESS_LIST_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(urlValues.get(Constants.VIP_IPV6_ADDRESS_LIST_ENTITY));
						break;
					case Constants.VIRTUAL_DATA_CENTER_ENTITY:
						primaryKey.add(urlValues.get(Constants.VIRTUAL_DATA_CENTER_ENTITY));
						break;
					case Constants.VLAN_ENTITY:
						if (!hostname.equalsIgnoreCase("")) {
							grandParentEntityId = hostname;
						} else if (!pnfName.equalsIgnoreCase("")) {
							grandParentEntityId = pnfName;
						} else if (!vserverId.equalsIgnoreCase("")) {
							grandParentEntityId = vserverId;
						} else if (!equipmentName.equalsIgnoreCase("")) {
							grandParentEntityId = equipmentName;
						} else if (!vnfId.equalsIgnoreCase("")) {
							grandParentEntityId = vnfId;
						}
						primaryKey.add(grandParentEntityId);
						primaryKey.add(lInterfaceName);
						primaryKey.add(vlanInterface);
						break;
					case Constants.VNF_IMAGE_ENTITY:
						primaryKey.add(urlValues.get(Constants.VNF_IMAGE_ENTITY));
						break;
					case Constants.VNFC_ENTITY:
						primaryKey.add(vnfcName);
						break;
					case Constants.VPLS_PE_ENTITY:
						primaryKey.add(equipmentName);
						break;
					case Constants.VSERVER_ENTITY:
						primaryKey.add(cloudOwner);
						primaryKey.add(cloudRegionId);
						primaryKey.add(tenantId);
						primaryKey.add(vserverId);
						break;
					case Constants.ZONE_ENTITY:
						primaryKey.add(urlValues.get(Constants.ZONE_ENTITY));
						break;

					default:
						ecompLogger.debug("Skipping for entity :" + aaiEntity);
					}

					String primaryKeyValues = StringUtils.join(primaryKey, "|");
					String entityValue = "";
					boolean populateData = false;
					if (dbModifiedRecords.containsKey(primaryKeyValues)) {
						if (Long.valueOf(dbModifiedRecords.get(primaryKeyValues)) < Long.valueOf(resourceVersion)) {
							populateData = true;
							ecompLogger.debug("1) update of entity: " + aaiEntity + " for " + primaryKeyValues
									+ " as the value in DB is not latest.");
						} else {
							ecompLogger.debug("Skip update of entity: " + aaiEntity + " for " + primaryKeyValues
									+ " as the value in DB has latest updates.");
							continue;
						}
					} else {
						ecompLogger.debug("2) update of entity: " + aaiEntity + " for " + primaryKeyValues
								+ " as the DB does not have the records.");
						populateData = true;
					}
					if (populateData) {
						String aaiApiUrlPart = aaiApiUrl.substring(0, aaiApiUrl.indexOf("/aai/"));
						String aaiEntityApiUrl = aaiApiUrlPart + url;
						System.out.println(aaiEntityApiUrl);
						entityValue = restClient.retrieveAAIObject(aaiEntityApiUrl);
					} else {
						ecompLogger.debug("Skip for primary key: " + primaryKeyValues + " : "
								+ dbModifiedRecords.get(primaryKeyValues) + " < " + resourceVersion);
						continue;
					}
					if (entityValue == null || entityValue.equalsIgnoreCase("")) {
						ecompLogger.debug("Skip update of entity: " + aaiEntity + " for " + primaryKeyValues
								+ " as the response from API is not proper");
						continue;
					}

					urlValues.put("entityValue", entityValue);
					urlValues.put("aaiEntity", aaiEntity);
					try {
						storeToDB(urlValues);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return status;
	}

	private boolean storeToDB(Map<String, String> urlValues)
			throws JsonParseException, JsonMappingException, IOException {
		boolean status = false;
		String entityType = urlValues.get("aaiEntity") != null ? urlValues.get("aaiEntity") : "";
		if (urlValues.get("entityValue") != null) {
			String entityValue = urlValues.get("entityValue");
			String datetimestamp = EventUtil.getCurrentTimestamp();
			ObjectMapper mapper = EventUtil.getObjectMapperObject();
			aaiEventProcessor.setEventType(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT);
			aaiEventProcessor.setHeaderEntityType(entityType);
			String action = "FULL";
			String hostname = Optional.ofNullable(urlValues.get(Constants.PSERVER_ENTITY)).orElse("");
			String pnfName = Optional.ofNullable(urlValues.get(Constants.PNF_ENTITY)).orElse("");
			String networkId = Optional.ofNullable(urlValues.get(Constants.L3_NETWORK_ENTITY)).orElse("");
			String laginterfacename = Optional.ofNullable(urlValues.get(Constants.LAG_INTERFACE_ENTITY)).orElse("");
			String linterfacename = Optional.ofNullable(urlValues.get(Constants.L_INTERFACE_ENTITY)).orElse("");
			String pinterfacename = Optional.ofNullable(urlValues.get(Constants.P_INTERFACE_ENTITY)).orElse("");
			String vlanInterface = Optional.ofNullable(urlValues.get(Constants.VLAN_ENTITY)).orElse("");
			String cloudOwner = Optional.ofNullable(urlValues.get(Constants.CLOUD_OWNER)).orElse("");
			String cloudRegionId = Optional.ofNullable(urlValues.get(Constants.CLOUD_REGION_ENTITY)).orElse("");
			String tenantId = Optional.ofNullable(urlValues.get(Constants.TENANT_ENTITY)).orElse("");
			String vserverId = Optional.ofNullable(urlValues.get(Constants.VSERVER_ENTITY)).orElse("");
			String equipmentName = Optional.ofNullable(urlValues.get(Constants.VPLS_PE_ENTITY)).orElse("");
			String modelInvariantId = Optional.ofNullable(urlValues.get(Constants.MODEL_ENTITY)).orElse("");
			String vnfcName = Optional.ofNullable(urlValues.get(Constants.VNFC_ENTITY)).orElse("");
			String cpInstanceId = Optional.ofNullable(urlValues.get(Constants.CP_ENTITY)).orElse("");
			String forwardingPathId = Optional.ofNullable(urlValues.get(Constants.FORWARDING_PATH_ENTITY)).orElse("");
			String globalCustomerId = Optional.ofNullable(urlValues.get(Constants.CUSTOMER_ENTITY)).orElse("");
			String serviceType = Optional.ofNullable(urlValues.get(Constants.SERVICE_SUBSCRIPTION_ENTITY)).orElse("");
			String serviceInstanceId = Optional.ofNullable(urlValues.get(Constants.SERVICE_INSTANCE_ENTITY)).orElse("");
			String vnfId = Optional.ofNullable(urlValues.get(Constants.GENERIC_VNF_ENTITY)).orElse("");
			String vceVnfId = Optional.ofNullable(urlValues.get(Constants.VCE_ENTITY)).orElse("");
			String cloudregiontenant = cloudOwner + "|" + cloudRegionId + "|" + tenantId;
			String parentEntityType = "";
			String parentEntityId = "";
			String grandParentEntityType = "";
			String grandParentEntityId = "";

			switch (entityType) {
			case Constants.ALLOTTED_RESOURCE_ENTITY:
				AllottedResource allottedResource = mapper.readValue(entityValue, AllottedResource.class);
				if (allottedResource != null) {
					status = aaiEventProcessor.processAllottedResource(allottedResource, globalCustomerId, serviceType,
							serviceInstanceId, action, datetimestamp);
				}
				break;
			case Constants.AVAILABILITY_ZONE_ENTITY:
				AvailabilityZone availabilityZone = mapper.readValue(entityValue, AvailabilityZone.class);
				if (availabilityZone != null) {
					status = aaiEventProcessor.processAvailabilityZone(availabilityZone, cloudOwner, cloudRegionId,
							action, datetimestamp);
				}
				break;
			case Constants.CLOUD_REGION_ENTITY:
				CloudRegion cloudRegion = mapper.readValue(entityValue, CloudRegion.class);
				if (cloudRegion != null) {
					status = aaiEventProcessor.processAaiCloudRegionAndChildNodes(cloudRegion, action, datetimestamp);
				}
				break;
			case Constants.COMPLEX_ENTITY:
				Complex complex = mapper.readValue(entityValue, Complex.class);
				if (complex != null) {
					status = aaiEventProcessor.processComplex(complex, action, datetimestamp);
				}
				break;
			case Constants.CP_ENTITY:
				Cp cp = mapper.readValue(entityValue, Cp.class);
				if (cp != null) {
					grandParentEntityType = Constants.VNFC_ENTITY;
					grandParentEntityId = vnfcName;
					status = aaiEventProcessor.processAaiCpAndChildNodes(cp, grandParentEntityType, grandParentEntityId,
							action, datetimestamp);
				}
				break;
			case Constants.CUSTOMER_ENTITY:
				Customer customer = mapper.readValue(entityValue, Customer.class);
				if (customer != null) {
					status = aaiEventProcessor.processAaiCustomerAndChildNodes(customer, action, datetimestamp);
				}
				break;
			case Constants.FLAVOR_ENTITY:
				Flavor flavor = mapper.readValue(entityValue, Flavor.class);
				if (flavor != null) {
					status = aaiEventProcessor.processFlavor(flavor, cloudOwner, cloudRegionId, action, datetimestamp);
				}
				break;
			case Constants.FORWARDER_ENTITY:
				Forwarder forwarder = mapper.readValue(entityValue, Forwarder.class);
				if (forwarder != null) {
					status = aaiEventProcessor.processForwarder(forwarder, forwardingPathId, action, datetimestamp);
				}
				break;
			case Constants.FORWARDING_PATH_ENTITY:
				ForwardingPath forwardingPath = mapper.readValue(entityValue, ForwardingPath.class);
				if (forwardingPath != null) {
					status = aaiEventProcessor.processAaiForwardingPathAndChildNodes(forwardingPath, action,
							datetimestamp);
				}
				break;
			case Constants.GENERIC_VNF_ENTITY:
				GenericVnf genericVnf = mapper.readValue(entityValue, GenericVnf.class);
				if (genericVnf != null) {
					status = aaiEventProcessor.processAaiGenericVnfAndChildNodes(genericVnf, action, datetimestamp);
				}
				break;
			case Constants.IMAGE_ENTITY:
				Image image = mapper.readValue(entityValue, Image.class);
				if (image != null) {
					status = aaiEventProcessor.processImage(image, cloudOwner, cloudRegionId, action, datetimestamp);
				}
				break;
			case Constants.L_INTERFACE_ENTITY:
				LInterface lInterface = mapper.readValue(entityValue, LInterface.class);
				if (lInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						parentEntityType = Constants.PSERVER_ENTITY;
						parentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						parentEntityType = Constants.PNF_ENTITY;
						parentEntityId = pnfName;
					} else if (!vserverId.equalsIgnoreCase("")) {
						parentEntityType = Constants.VSERVER_ENTITY;
						parentEntityId = vserverId;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						parentEntityType = Constants.VPLS_PE_ENTITY;
						parentEntityId = equipmentName;
					} else if (!vnfId.equalsIgnoreCase("")) {
						parentEntityType = Constants.GENERIC_VNF_ENTITY;
						parentEntityId = vnfId;
					} else {
						ecompLogger.debug("skipping newvce related " + Constants.L_INTERFACE_ENTITY + " object");
						status = false;
						break;
					}
					status = aaiEventProcessor.processAaiLinterfaceAndChildNodes(lInterface, parentEntityType,
							parentEntityId, cloudregiontenant, pinterfacename, laginterfacename, action, datetimestamp);
				}
				break;
			case Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY:
				L3InterfaceIpv4AddressList ipv4 = mapper.readValue(entityValue, L3InterfaceIpv4AddressList.class);
				if (ipv4 != null) {
					parentEntityType = Constants.L_INTERFACE_ENTITY;
					parentEntityId = linterfacename;
					if (!hostname.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PSERVER_ENTITY;
						grandParentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = pnfName;
					} else if (!vserverId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VSERVER_ENTITY;
						grandParentEntityId = vserverId;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VPLS_PE_ENTITY;
						grandParentEntityId = equipmentName;
					} else if (!vnfId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.GENERIC_VNF_ENTITY;
						grandParentEntityId = vnfId;
					} else if (!vnfcName.equalsIgnoreCase("")) {
						if (!cpInstanceId.equalsIgnoreCase("")) {
							parentEntityType = Constants.CP_ENTITY;
							parentEntityId = cpInstanceId;
						}
						grandParentEntityType = Constants.VNFC_ENTITY;
						grandParentEntityId = vnfcName;
					} else {
						ecompLogger.debug("skipping newvce related " + Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY
								+ " object");
						status = false;
						break;
					}
					status = aaiEventProcessor.processL3InterfaceIpv4AddressList(ipv4, parentEntityType, parentEntityId,
							grandParentEntityType, grandParentEntityId, cloudregiontenant, pinterfacename,
							laginterfacename, vlanInterface, action, datetimestamp);
				}
				break;
			case Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY:
				L3InterfaceIpv6AddressList ipv6 = mapper.readValue(entityValue, L3InterfaceIpv6AddressList.class);
				if (ipv6 != null) {
					parentEntityType = Constants.L_INTERFACE_ENTITY;
					parentEntityId = linterfacename;
					if (!hostname.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PSERVER_ENTITY;
						grandParentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = pnfName;
					} else if (!vserverId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VSERVER_ENTITY;
						grandParentEntityId = vserverId;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VPLS_PE_ENTITY;
						grandParentEntityId = equipmentName;
					} else if (!vnfId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.GENERIC_VNF_ENTITY;
						grandParentEntityId = vnfId;
					} else if (!vnfcName.equalsIgnoreCase("")) {
						if (!cpInstanceId.equalsIgnoreCase("")) {
							parentEntityType = Constants.CP_ENTITY;
							parentEntityId = cpInstanceId;
						}
						grandParentEntityType = Constants.VNFC_ENTITY;
						grandParentEntityId = vnfcName;
					} else {
						ecompLogger.debug("skipping newvce related " + Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY
								+ " object");
						status = false;
						break;
					}
					status = aaiEventProcessor.processL3InterfaceIpv6AddressList(ipv6, parentEntityType, parentEntityId,
							grandParentEntityType, grandParentEntityId, cloudregiontenant, pinterfacename,
							laginterfacename, vlanInterface, action, datetimestamp);
				}
				break;
			case Constants.L3_NETWORK_ENTITY:
				L3Network l3Network = mapper.readValue(entityValue, L3Network.class);
				if (l3Network != null) {
					status = aaiEventProcessor.processAaiL3NetworkAndChildNodes(l3Network, action, datetimestamp);
				}
				break;
			case Constants.LAG_INTERFACE_ENTITY:
				LagInterface lagInterface = mapper.readValue(entityValue, LagInterface.class);
				if (lagInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						parentEntityType = Constants.PSERVER_ENTITY;
						parentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						parentEntityType = Constants.PNF_ENTITY;
						parentEntityId = pnfName;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						parentEntityType = Constants.VPLS_PE_ENTITY;
						parentEntityId = equipmentName;
					} else if (!vnfId.equalsIgnoreCase("")) {
						parentEntityType = Constants.GENERIC_VNF_ENTITY;
						parentEntityId = vnfId;
					}
					status = aaiEventProcessor.processAaiLaginterfaceAndChildNodes(lagInterface, parentEntityType,
							parentEntityId, action, datetimestamp);
				}
				break;
			case Constants.LAG_LINK_ENTITY:
				LagLink lagLink = mapper.readValue(entityValue, LagLink.class);
				if (lagLink != null) {
					status = aaiEventProcessor.processLagLink(lagLink, action, datetimestamp);
				}
				break;
			case Constants.LINE_OF_BUSINESS_ENTITY:
				LineOfBusiness lineOfBusiness = mapper.readValue(entityValue, LineOfBusiness.class);
				if (lineOfBusiness != null) {
					status = aaiEventProcessor.processLineOfBusiness(lineOfBusiness, action, datetimestamp);
				}
				break;
			case Constants.LOGICAL_LINK_ENTITY:
				LogicalLink logicalLink = mapper.readValue(entityValue, LogicalLink.class);
				if (logicalLink != null) {
					status = aaiEventProcessor.processLogicalLink(logicalLink, action, datetimestamp);
				}
				break;
			case Constants.MODEL_ENTITY:
				Model model = mapper.readValue(entityValue, Model.class);
				if (model != null) {
					status = aaiEventProcessor.processAaiModelAndChildNodes(model, action, datetimestamp);
				}
				break;
			case Constants.MODEL_VER_ENTITY:
				ModelVer modelVer = mapper.readValue(entityValue, ModelVer.class);
				if (modelVer != null) {
					status = aaiEventProcessor.processModelVer(modelVer, modelInvariantId, action, datetimestamp);
				}
				break;
			case Constants.NETWORK_PROFILE_ENTITY:
				NetworkProfile networkProfile = mapper.readValue(entityValue, NetworkProfile.class);
				if (networkProfile != null) {
					status = aaiEventProcessor.processNetworkProfile(networkProfile, action, datetimestamp);
				}
				break;
			case Constants.NOS_SERVER_ENTITY:
				NosServer nosServer = mapper.readValue(entityValue, NosServer.class);
				if (nosServer != null) {
					status = aaiEventProcessor.processNosServer(nosServer, tenantId, cloudOwner, cloudRegionId, action,
							datetimestamp);
				}
				break;
			case Constants.OAM_NETWORK_ENTITY:
				OamNetwork oamNetwork = mapper.readValue(entityValue, OamNetwork.class);
				if (oamNetwork != null) {
					status = aaiEventProcessor.processOamNetwork(oamNetwork, cloudOwner, cloudRegionId, action,
							datetimestamp);
				}
				break;
			case Constants.OPERATIONAL_ENVIRONMENT_ENTITY:
				OperationalEnvironment operationalEnvironment = mapper.readValue(entityValue,
						OperationalEnvironment.class);
				if (operationalEnvironment != null) {
					status = aaiEventProcessor.processOperationalEnvironment(operationalEnvironment, action,
							datetimestamp);
				}
				break;
			case Constants.OWNING_ENTITY_ENTITY:
				OwningEntity owningEntity = mapper.readValue(entityValue, OwningEntity.class);
				if (owningEntity != null) {
					status = aaiEventProcessor.processOwningEntity(owningEntity, action, datetimestamp);
				}
				break;
			case Constants.P_INTERFACE_ENTITY:
				PInterface pInterface = mapper.readValue(entityValue, PInterface.class);
				if (pInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						parentEntityType = Constants.PSERVER_ENTITY;
						parentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						parentEntityType = Constants.PNF_ENTITY;
						parentEntityId = pnfName;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						parentEntityType = Constants.VPLS_PE_ENTITY;
						parentEntityId = equipmentName;
					}
					status = aaiEventProcessor.processAaiPinterfaceAndChildNodes(pInterface, parentEntityType,
							parentEntityId, action, datetimestamp);
				}
				break;
			case Constants.PHYSICAL_LINK_ENTITY:
				PhysicalLink physicalLink = mapper.readValue(entityValue, PhysicalLink.class);
				if (physicalLink != null) {
					status = aaiEventProcessor.processPhysicalLink(physicalLink, action, datetimestamp);
				}
				break;
			case Constants.PLATFORM_ENTITY:
				Platform platform = mapper.readValue(entityValue, Platform.class);
				if (platform != null) {
					status = aaiEventProcessor.processPlatform(platform, action, datetimestamp);
				}
				break;
			case Constants.PNF_ENTITY:
				Pnf pnf = mapper.readValue(entityValue, Pnf.class);
				if (pnf != null) {
					status = aaiEventProcessor.processAaiPnfAndChildNodes(pnf, action, datetimestamp);
				}
				break;
			case Constants.PORT_GROUP_ENTITY:
				PortGroup portGroup = mapper.readValue(entityValue, PortGroup.class);
				if (portGroup != null) {
					status = aaiEventProcessor.processPortGroup(portGroup, vceVnfId, action, datetimestamp);
				}
				break;
			case Constants.PROJECT_ENTITY:
				Project project = mapper.readValue(entityValue, Project.class);
				if (project != null) {
					status = aaiEventProcessor.processProject(project, action, datetimestamp);
				}
				break;
			case Constants.PSERVER_ENTITY:
				Pserver pserver = mapper.readValue(entityValue, Pserver.class);
				if (pserver != null) {
					status = aaiEventProcessor.processAaiPserverAndChildNodes(pserver, action, datetimestamp);
				}
				break;
			case Constants.SERVICE_ENTITY:
				Service service = mapper.readValue(entityValue, Service.class);
				if (service != null) {
					status = aaiEventProcessor.processService(service, action, datetimestamp);
				}
				break;
			case Constants.SERVICE_CAPABILITY_ENTITY:
				ServiceCapability serviceCapability = mapper.readValue(entityValue, ServiceCapability.class);
				if (serviceCapability != null) {
					status = aaiEventProcessor.processServiceCapability(serviceCapability, action, datetimestamp);
				}
				break;
			case Constants.SERVICE_INSTANCE_ENTITY:
				ServiceInstance serviceInstance = mapper.readValue(entityValue, ServiceInstance.class);
				if (serviceInstance != null) {
					status = aaiEventProcessor.processServiceInstance(serviceInstance, globalCustomerId, serviceType,
							action, datetimestamp);
				}
				break;
			case Constants.SERVICE_SUBSCRIPTION_ENTITY:
				ServiceSubscription serviceSubscription = mapper.readValue(entityValue, ServiceSubscription.class);
				if (serviceSubscription != null) {
					status = aaiEventProcessor.processServiceSubscription(serviceSubscription, globalCustomerId, action,
							datetimestamp);
				}
				break;
			case Constants.SRIOV_PF_ENTITY:
				SriovPf sriovPf = mapper.readValue(entityValue, SriovPf.class);
				if (sriovPf != null) {
					if (!hostname.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PSERVER_ENTITY;
						grandParentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = pnfName;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VPLS_PE_ENTITY;
						grandParentEntityId = equipmentName;
					}
					status = aaiEventProcessor.processSriovPf(sriovPf, grandParentEntityType, grandParentEntityId,
							pinterfacename, action, datetimestamp);
				}
				break;
			case Constants.SRIOV_VF_ENTITY:
				SriovVf sriovVf = mapper.readValue(entityValue, SriovVf.class);
				if (sriovVf != null) {
					parentEntityType = Constants.L_INTERFACE_ENTITY;
					parentEntityId = linterfacename;
					if (!vnfId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = vnfId;
					} else if (!pnfName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = pnfName;
					} else if (!vserverId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VSERVER_ENTITY;
						grandParentEntityId = vserverId;
					} else {
						ecompLogger.debug("skipping newvce related " + Constants.SRIOV_VF_ENTITY + " object");
						status = false;
						break;
					}
					status = aaiEventProcessor.processSriovVf(sriovVf, parentEntityType, parentEntityId, cloudOwner,
							cloudRegionId, tenantId, grandParentEntityType, grandParentEntityId, pinterfacename,
							laginterfacename, action, datetimestamp);
				}
				break;
			case Constants.SUBNET_ENTITY:
				Subnet subnet = mapper.readValue(entityValue, Subnet.class);
				if (subnet != null) {
					status = aaiEventProcessor.processSubnet(subnet, networkId, action, datetimestamp);
				}
				break;
			case Constants.TENANT_ENTITY:
				Tenant tenant = mapper.readValue(entityValue, Tenant.class);
				if (tenant != null) {
					status = aaiEventProcessor.processAaiTenantAndChildNodes(tenant, cloudOwner, cloudRegionId, action,
							datetimestamp);
				}
				break;
			case Constants.VCE_ENTITY:
				Vce vce = mapper.readValue(entityValue, Vce.class);
				if (vce != null) {
					status = aaiEventProcessor.processVceAndChildNodes(vce, action, datetimestamp);
				}
				break;
			case Constants.VF_MODULE_ENTITY:
				VfModule vfModule = mapper.readValue(entityValue, VfModule.class);
				if (vfModule != null) {
					status = aaiEventProcessor.processVfModule(vfModule, Constants.GENERIC_VNF_ENTITY, vnfId, action,
							datetimestamp);
				}
				break;
			case Constants.VIP_IPV4_ADDRESS_LIST_ENTITY:
				VipIpv4AddressList vipIpv4AddressList = mapper.readValue(entityValue, VipIpv4AddressList.class);
				if (vipIpv4AddressList != null) {
					status = aaiEventProcessor.processCloudVIPIPV4List(vipIpv4AddressList, cloudOwner, cloudRegionId,
							action, datetimestamp);
				}
				break;
			case Constants.VIP_IPV6_ADDRESS_LIST_ENTITY:
				VipIpv6AddressList vipIpv6AddressList = mapper.readValue(entityValue, VipIpv6AddressList.class);
				if (vipIpv6AddressList != null) {
					status = aaiEventProcessor.processCloudVIPIPV6List(vipIpv6AddressList, cloudOwner, cloudRegionId,
							action, datetimestamp);
				}
				break;
			case Constants.VIRTUAL_DATA_CENTER_ENTITY:
				VirtualDataCenter virtualDataCenter = mapper.readValue(entityValue, VirtualDataCenter.class);
				if (virtualDataCenter != null) {
					status = aaiEventProcessor.processVirtualDataCenter(virtualDataCenter, action, datetimestamp);
				}
				break;
			case Constants.VLAN_ENTITY:
				Vlan vlan = mapper.readValue(entityValue, Vlan.class);
				if (vlan != null) {
					if (!hostname.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PSERVER_ENTITY;
						grandParentEntityId = hostname;
					} else if (!pnfName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.PNF_ENTITY;
						grandParentEntityId = pnfName;
					} else if (!vserverId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VSERVER_ENTITY;
						grandParentEntityId = vserverId;
					} else if (!equipmentName.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.VPLS_PE_ENTITY;
						grandParentEntityId = equipmentName;
					} else if (!vnfId.equalsIgnoreCase("")) {
						grandParentEntityType = Constants.GENERIC_VNF_ENTITY;
						grandParentEntityId = vnfId;
					} else {
						ecompLogger.debug("skipping newvce related " + Constants.VLAN_ENTITY + " object");
						status = false;
						break;
					}
					status = aaiEventProcessor.processAaiVlanAndChildNodes(vlan, linterfacename, grandParentEntityType,
							grandParentEntityId, cloudregiontenant, pinterfacename, laginterfacename, action,
							datetimestamp);
				}
				break;
			case Constants.VNF_IMAGE_ENTITY:
				VnfImage vnfImage = mapper.readValue(entityValue, VnfImage.class);
				if (vnfImage != null) {
					status = aaiEventProcessor.processVnfImage(vnfImage, action, datetimestamp);
				}
				break;
			case Constants.VNFC_ENTITY:
				Vnfc vnfc = mapper.readValue(entityValue, Vnfc.class);
				if (vnfc != null) {
					status = aaiEventProcessor.processAaiVnfcAndChildNodes(vnfc, action, datetimestamp);
				}
				break;
			case Constants.VPLS_PE_ENTITY:
				VplsPe vplsPe = mapper.readValue(entityValue, VplsPe.class);
				if (vplsPe != null) {
					status = aaiEventProcessor.processAaiVplsPeAndChildNodes(vplsPe, action, datetimestamp);
				}
				break;
			case Constants.VSERVER_ENTITY:
				Vserver vserver = mapper.readValue(entityValue, Vserver.class);
				if (vserver != null) {
					status = aaiEventProcessor.processAaiVserverAndChildNodes(vserver, cloudOwner, cloudRegionId,
							tenantId, action, datetimestamp);
				}
				break;
			case Constants.ZONE_ENTITY:
				Zone zone = mapper.readValue(entityValue, Zone.class);
				if (zone != null) {
					status = aaiEventProcessor.processZone(zone, action, datetimestamp);
				}
				break;

			default:
				System.out.println("Doesn't match any A&AI Entity");

			}

		}

		return status;
	}

	public boolean populateStoreAaiData(String entity, Integer duration) {
		boolean status = true;

		String entityUrl = aaiApiUrl + entity + "?hours=" + duration;

		// convert /aai/v15 to /aai/recents/v15
		entityUrl = entityUrl.replaceFirst("/aai/", "/aai/recents/");
		System.out.println(entityUrl);

		status = this.storeAaiEntities(entityUrl, entity);

		return status;
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");
		SyncAaiData syncAaiData = new SyncAaiData();

		boolean result = EventUtil.loadConfigs();
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_LOAD_CONFIG_ERROR);
			System.exit(1);
		}

		// Initialize rest client after loading configurations
		restClient = new RESTClient();
		aaiEventProcessor = new AAIEventProcessor();
		dbadapter = aaiEventProcessor.getDBAdapter();

		String configDir = System.getenv("DTI_CONFIG");
		if (configDir == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
			return;
		}

		FileInputStream dtiStream = null;
		FileInputStream fis = null;
		List<String> loadAAIDailySync = null;
		List<String> loadAAIWeeklySync = null;
		try {
			dtiStream = new FileInputStream(Util.safeFileName(configDir) + "/dti.properties");
			dtiProps.load(dtiStream);
			loadAAIDailySync = Files.readAllLines(
					Paths.get(Util.safeFileName(configDir) + "/loadAAIDailySync.properties"), StandardCharsets.UTF_8);
			loadAAIWeeklySync = Files.readAllLines(
					Paths.get(Util.safeFileName(configDir) + "/loadAAIWeeklySync.properties"), StandardCharsets.UTF_8);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			EventUtil.closeFileInputStream(dtiStream);
			EventUtil.closeFileInputStream(fis);
		}

		if (EventUtil.API_URL != null) {
			aaiApiUrl = EventUtil.API_URL;
		} else if (System.getenv("aai_api_url") != null && !System.getenv("aai_api_url").equalsIgnoreCase("")) {
			aaiApiUrl = System.getenv("aai_api_url");
		}

		for (String aaiEntity : loadAAIDailySync) {
			if (EventUtil.aaiEntities.containsKey(aaiEntity.trim())) {
				syncAaiData.populateStoreAaiData(aaiEntity, Constants.HOURS_IN_A_DAY);
			}
		}

		LocalDate date = LocalDate.now();
		DayOfWeek dow = date.getDayOfWeek();
		String dayName = dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		//define the weeklySyncDay in dti.properties file to identify the day for weekly sync
		if (dayName.equalsIgnoreCase(EventUtil.weeklySyncDay)) {
			for (String aaiEntity : loadAAIWeeklySync) {
				if (EventUtil.aaiEntities.containsKey(aaiEntity.trim())) {
					syncAaiData.populateStoreAaiData(aaiEntity, Constants.HOURS_IN_A_WEEK);
				}
			}
		}

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXIT_DEBUG);

	}

}
