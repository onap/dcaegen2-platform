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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.domain.RelationshipList;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.schema.Cable;
import com.att.vcc.inventorycollector.schema.Card;
import com.att.vcc.inventorycollector.schema.CardSlot;
import com.att.vcc.inventorycollector.schema.Chassis;
import com.att.vcc.inventorycollector.schema.CloudRegion;
import com.att.vcc.inventorycollector.schema.Complex;
import com.att.vcc.inventorycollector.schema.InstanceGroup;
import com.att.vcc.inventorycollector.schema.L3InterfaceIpv4AddressList;
import com.att.vcc.inventorycollector.schema.L3InterfaceIpv6AddressList;
import com.att.vcc.inventorycollector.schema.L3Network;
import com.att.vcc.inventorycollector.schema.LInterface;
import com.att.vcc.inventorycollector.schema.LagInterface;
import com.att.vcc.inventorycollector.schema.LogicalLink;
import com.att.vcc.inventorycollector.schema.PInterface;
import com.att.vcc.inventorycollector.schema.PhysicalLink;
import com.att.vcc.inventorycollector.schema.Pluggable;
import com.att.vcc.inventorycollector.schema.PluggableSlot;
import com.att.vcc.inventorycollector.schema.Pnf;
import com.att.vcc.inventorycollector.schema.Port;
import com.att.vcc.inventorycollector.schema.Pserver;
import com.att.vcc.inventorycollector.schema.Rack;
import com.att.vcc.inventorycollector.schema.Subnet;
import com.att.vcc.inventorycollector.schema.Vlan;
import com.att.vcc.inventorycollector.schema.Zone;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncNaradData {
	private static Properties dtiProps = new Properties();

	private static final int PAGE_SIZE = 1000;
	private static final int HOURS_FOR_A_DAY = 24;
	private static final int DAYS_IN_A_WEEK = 7;
	private static String NARAD_API_URL;
	private static RESTClient restClient;
	private static DBAdapter dbadapter;
	private static EventProcessor eventProcessor;

	private static Map<String, Integer> naradEntities;
	private static Map<String, String> naradEntitiesNodesUrl;

	private static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	static {
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

	private static boolean storeNaradEntities(String naradAPIUrl, String naradEntity) {
		boolean status = false;
		String dateTimeStampLastWeek = EventUtil.getTimestamp(DAYS_IN_A_WEEK);
		System.out.println(dateTimeStampLastWeek + " narad Entity: " + naradEntity);
		try {
			String response = restClient.retrieveAAIObject(naradAPIUrl);
			if (!response.equalsIgnoreCase("")) {
				Map<String, String> dbModifiedRecords = dbadapter
						.getModifiedRecordsUsingUpdatedOn("narad-" + naradEntity, dateTimeStampLastWeek);
				JSONObject responseJSONObject = null;
				responseJSONObject = new JSONObject(response);
				JSONArray entitiesArray = responseJSONObject.optJSONArray("results");
				int totalRecords = entitiesArray.length();
				for (int i = 0; i < totalRecords; i++) {
					String resourceVersion = entitiesArray.optJSONObject(i).getString("resource-version");
					String url = entitiesArray.optJSONObject(i).getString("resource-link");
					Map<String, String> urlValues = EventUtil.parseUrl(naradEntities, url);
					List<String> primaryKey = new ArrayList<String>();
					String physicallocationid = urlValues.get("complex") != null ? urlValues.get("complex") : "";
					String chassisname = urlValues.get("chassis") != null ? urlValues.get("chassis") : "";
					String cardslotname = urlValues.get("card-slot") != null ? urlValues.get("card-slot") : "";
					String cardtype = urlValues.get("card") != null ? urlValues.get("card") : "";
					String pluggableslotname = urlValues.get("pluggable-slot") != null ? urlValues.get("pluggable-slot")
							: "";
					String hostname = urlValues.get("pserver") != null ? urlValues.get("pserver") : "";
					String pnfName = urlValues.get("pnf") != null ? urlValues.get("pnf") : "";
					String networkId = urlValues.get("l3-network") != null ? urlValues.get("l3-network") : "";
					String pluggabletype = urlValues.get("pluggable") != null ? urlValues.get("pluggable") : "";
					String laginterfacename = urlValues.get("lag-interface") != null ? urlValues.get("lag-interface")
							: "na";
					String linterfacename = urlValues.get("l-interface") != null ? urlValues.get("l-interface") : "na";
					String pinterfacename = urlValues.get("p-interface") != null ? urlValues.get("p-interface") : "na";
					String vlanInterface = urlValues.get("vlan") != null ? urlValues.get("vlan") : "na";
					String parentEntityType = "";
					String cloudregiontenant = "na";

					// Add top level entity info / ancestor information to
					// primary key ArrayList
					if (!hostname.equalsIgnoreCase("")) {
						primaryKey.add(hostname);
						parentEntityType = "pserver";
					} else if (!pnfName.equalsIgnoreCase("")) {
						primaryKey.add(pnfName);
						parentEntityType = "pnf";
					} else if (!chassisname.equalsIgnoreCase("")) {
						primaryKey.add(chassisname);
					} else if (!physicallocationid.equalsIgnoreCase("")) {
						primaryKey.add(physicallocationid);
					} else if (!networkId.equalsIgnoreCase("")) {
						primaryKey.add(networkId);
					}

					// Add proper primary key so that we would be able to
					// compare it with DB records.
					if (!cardslotname.equalsIgnoreCase("")
							&& (naradEntity.equalsIgnoreCase("card") || naradEntity.equalsIgnoreCase("card-slot")))
						primaryKey.add(cardslotname);
					if (!cardtype.equalsIgnoreCase("") && naradEntity.equalsIgnoreCase("card"))
						primaryKey.add(cardtype);
					if (!pluggableslotname.equalsIgnoreCase("") && (naradEntity.equalsIgnoreCase("pluggable")
							|| naradEntity.equalsIgnoreCase("pluggable-slot")))
						primaryKey.add(pluggableslotname);
					if (!pluggabletype.equalsIgnoreCase("") && naradEntity.equalsIgnoreCase("pluggable"))
						primaryKey.add(pluggabletype);

					if (naradEntity.equalsIgnoreCase("cable")) {
						primaryKey.add(urlValues.get("cable"));
					} else if (naradEntity.equalsIgnoreCase("cloud-region")) {
						primaryKey.add(urlValues.get("cloud-owner"));
						primaryKey.add(urlValues.get("cloud-region"));
					} else if (naradEntity.equalsIgnoreCase("instance-group")) {
						primaryKey.add(urlValues.get("instance-group"));
					} else if (naradEntity.equalsIgnoreCase("l3-interface-ipv4-address-list")) {
						primaryKey.add(linterfacename);
						primaryKey.add(cloudregiontenant);
						primaryKey.add(pinterfacename);
						primaryKey.add(laginterfacename);
						primaryKey.add(vlanInterface);
						primaryKey.add(urlValues.get("l3-interface-ipv4-address-list"));
					} else if (naradEntity.equalsIgnoreCase("l3-interface-ipv6-address-list")) {
						primaryKey.add(linterfacename);
						primaryKey.add(cloudregiontenant);
						primaryKey.add(pinterfacename);
						primaryKey.add(laginterfacename);
						primaryKey.add(vlanInterface);
						primaryKey.add(urlValues.get("l3-interface-ipv6-address-list"));
					} else if (naradEntity.equalsIgnoreCase("lag-interface")) {
						primaryKey.add(parentEntityType);
						primaryKey.add(laginterfacename);
					} else if (naradEntity.equalsIgnoreCase("p-interface")) {
						primaryKey.add(parentEntityType);
						primaryKey.add(pinterfacename);
					} else if (naradEntity.equalsIgnoreCase("l-interface")) {
						primaryKey.add(cloudregiontenant);
						primaryKey.add(pinterfacename);
						primaryKey.add(laginterfacename);
						primaryKey.add(linterfacename);
					} else if (naradEntity.equalsIgnoreCase("logical-link")) {
						primaryKey.add(urlValues.get("logical-link"));
					} else if (naradEntity.equalsIgnoreCase("physical-link")) {
						primaryKey.add(urlValues.get("physical-link"));
					} else if (naradEntity.equalsIgnoreCase("port")) {
						primaryKey.add(urlValues.get("port"));
					} else if (naradEntity.equalsIgnoreCase("rack")) {
						primaryKey.add(urlValues.get("rack"));
					} else if (naradEntity.equalsIgnoreCase("subnet")) {
						primaryKey.add(urlValues.get("subnet"));
					} else if (naradEntity.equalsIgnoreCase("vlan")) {
						primaryKey.add(linterfacename);
						primaryKey.add(cloudregiontenant);
						primaryKey.add(pinterfacename);
						primaryKey.add(laginterfacename);
						primaryKey.add(vlanInterface);
					} else if (naradEntity.equalsIgnoreCase("zone")) {
						primaryKey.add(urlValues.get("zone"));
					}

					String primaryKeyValues = StringUtils.join(primaryKey, "|");
					String entityValue = "";
					boolean populateData = false;
					if (dbModifiedRecords.containsKey(primaryKeyValues)) {
						if (Long.valueOf(dbModifiedRecords.get(primaryKeyValues)) < Long.valueOf(resourceVersion)) {
							populateData = true;
							ecompLogger.debug("1) update of entity: " + naradEntity + " for " + primaryKeyValues
									+ " as the value in DB is not latest.");
						} else {
							ecompLogger.debug("Skip update of entity: " + naradEntity + " for " + primaryKeyValues
									+ " as the value in DB has latest updates.");
							continue;
						}
					} else {
						ecompLogger.debug("2) update of entity: " + naradEntity + " for " + primaryKeyValues
								+ " as the DB does not have the records.");
						populateData = true;
					}
					if (populateData) {
						String naradApiUrl = NARAD_API_URL.substring(0, NARAD_API_URL.indexOf("/narad/"));
						String naradEntityApiUrl = naradApiUrl + url;
						System.out.println(naradEntityApiUrl);
						entityValue = restClient.retrieveAAIObject(naradEntityApiUrl);
					} else {
						ecompLogger.debug("Skip for primary key: " + primaryKeyValues + " : "
								+ dbModifiedRecords.get(primaryKeyValues) + " < " + resourceVersion);
						continue;
					}
					if (entityValue == null || entityValue.equalsIgnoreCase("")) {
						ecompLogger.debug("Skip update of entity: " + naradEntity + " for " + primaryKeyValues
								+ " as the response from API is not proper");
						continue;
					}

					urlValues.put("entityValue", entityValue);
					urlValues.put("naradEntity", naradEntity);
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
				status = true;
			}

		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return status;
	}

	private static boolean storeToDB(Map<String, String> urlValues)
			throws JsonParseException, JsonMappingException, IOException {
		boolean status = false;
		String entityType = urlValues.get("naradEntity") != null ? urlValues.get("naradEntity") : "";
		if (urlValues.get("entityValue") != null) {
			String entityValue = urlValues.get("entityValue");
			String datetimestamp = EventUtil.getCurrentTimestamp();
			ObjectMapper mapper = EventUtil.getObjectMapperObject();
			eventProcessor.setEventType(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT);
			eventProcessor.setHeaderEntityType(entityType);
			String action = "FULL";
			String physicallocationid = urlValues.get("complex") != null ? urlValues.get("complex") : "";
			String chassisname = urlValues.get("chassis") != null ? urlValues.get("chassis") : "";
			String cardslotname = urlValues.get("card-slot") != null ? urlValues.get("card-slot") : "";
			String cardtype = urlValues.get("card") != null ? urlValues.get("card") : "";
			String pluggableslotname = urlValues.get("pluggable-slot") != null ? urlValues.get("pluggable-slot") : "";
			String hostname = urlValues.get("pserver") != null ? urlValues.get("pserver") : "";
			String pnfName = urlValues.get("pnf") != null ? urlValues.get("pnf") : "";
			String networkId = urlValues.get("l3-network") != null ? urlValues.get("l3-network") : "";
			String pluggabletype = urlValues.get("pluggable") != null ? urlValues.get("pluggable") : "";
			String laginterfacename = urlValues.get("lag-interface") != null ? urlValues.get("lag-interface") : "na";
			String linterfacename = urlValues.get("l-interface") != null ? urlValues.get("l-interface") : "na";
			String pinterfacename = urlValues.get("p-interface") != null ? urlValues.get("p-interface") : "na";
			String vlanInterface = urlValues.get("vlan") != null ? urlValues.get("vlan") : "na";
			String zoneId = urlValues.get("zone") != null ? urlValues.get("zone") : "";
			String cloudregiontenant = "na";

			switch (entityType) {
			case "card":
				Card card = mapper.readValue(entityValue, Card.class);
				if (card != null) {
					status = eventProcessor.processNaradCardChildNodes(card, chassisname, cardslotname, action,
							datetimestamp);
				}
				break;
			case "card-slot":
				CardSlot cardSlot = mapper.readValue(entityValue, CardSlot.class);
				if (cardSlot != null) {
					status = eventProcessor.processNaradCardSlotChildNodes(cardSlot, chassisname, action,
							datetimestamp);
				}
				break;
			case "cable":
				Cable cable = mapper.readValue(entityValue, Cable.class);
				if (cable != null) {
					status = eventProcessor.processNaradCable(cable, physicallocationid, action, datetimestamp);
				}
				break;
			case "chassis":
				Chassis chassis = mapper.readValue(entityValue, Chassis.class);
				if (chassis != null) {
					status = eventProcessor.processNaradChassisChildNodes(chassis, action, datetimestamp);
				}
				break;
			case "cloud-region":
				CloudRegion cloudRegion = mapper.readValue(entityValue, CloudRegion.class);
				if (cloudRegion != null) {
					status = eventProcessor.syncCloudRegionAndChildNodes(cloudRegion, action, datetimestamp);
				}
				break;
			case "complex":
				Complex complex = mapper.readValue(entityValue, Complex.class);
				if (complex != null) {
					status = eventProcessor.processNaradComplexChildNodes(complex, action, datetimestamp);
				}
				break;
			case "instance-group":
				InstanceGroup instancegroup = mapper.readValue(entityValue, InstanceGroup.class);
				if (instancegroup != null) {
					status = eventProcessor.processNaradInstanceGroup(instancegroup, action, datetimestamp);
				}
				break;
			case "l3-network":
				L3Network l3Network = mapper.readValue(entityValue, L3Network.class);
				if (l3Network != null) {
					status = eventProcessor.syncL3NetworkAndChildNodes(l3Network, action, datetimestamp);
				}
				break;
			case "l3-interface-ipv4-address-list":
				L3InterfaceIpv4AddressList ipv4 = mapper.readValue(entityValue, L3InterfaceIpv4AddressList.class);
				if (ipv4 != null) {
					String parentEntityType = (!linterfacename.equalsIgnoreCase("na")) ? "l-interface" : "na";
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.storeNaradL3InterfaceIpv4AddressList(ipv4, parentEntityType,
								linterfacename, "pserver", hostname, cloudregiontenant, pinterfacename,
								laginterfacename, vlanInterface, action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.storeNaradL3InterfaceIpv4AddressList(ipv4, parentEntityType,
								linterfacename, "pnf", pnfName, cloudregiontenant, pinterfacename, laginterfacename,
								vlanInterface, action, datetimestamp);
					}
				}
				break;
			case "l3-interface-ipv6-address-list":
				L3InterfaceIpv6AddressList ipv6 = mapper.readValue(entityValue, L3InterfaceIpv6AddressList.class);
				if (ipv6 != null) {
					String parentEntityType = (!linterfacename.equalsIgnoreCase("na")) ? "l-interface" : "na";
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.storeNaradL3InterfaceIpv6AddressList(ipv6, parentEntityType,
								linterfacename, "pserver", hostname, cloudregiontenant, pinterfacename,
								laginterfacename, vlanInterface, action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.storeNaradL3InterfaceIpv6AddressList(ipv6, parentEntityType,
								linterfacename, "pnf", pnfName, cloudregiontenant, pinterfacename, laginterfacename,
								vlanInterface, action, datetimestamp);
					}
				}
				break;
			case "lag-interface":
				LagInterface lagInterface = mapper.readValue(entityValue, LagInterface.class);
				if (lagInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradLaginterfaceAndChildNodes(lagInterface, "pserver", hostname,
								action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradLaginterfaceAndChildNodes(lagInterface, "pnf", pnfName,
								action, datetimestamp);
					}
				}
				break;
			case "l-interface":
				LInterface lInterface = mapper.readValue(entityValue, LInterface.class);
				if (lInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradLinterfaceAndChildNodes(lInterface, "pserver", hostname,
								cloudregiontenant, pinterfacename, laginterfacename, action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradLinterfaceAndChildNodes(lInterface, "pnf", pnfName,
								cloudregiontenant, pinterfacename, laginterfacename, action, datetimestamp);
					}
				}
				break;
			case "logical-link":
				LogicalLink logicalLink = mapper.readValue(entityValue, LogicalLink.class);
				if (logicalLink != null) {
					status = eventProcessor.processLogicalLink(logicalLink, action, datetimestamp);
				}
				break;
			case "physical-link":
				PhysicalLink physicalLink = mapper.readValue(entityValue, PhysicalLink.class);
				if (physicalLink != null) {
					status = eventProcessor.processPhysicalLink(physicalLink, action, datetimestamp);
				}
				break;
			case "p-interface":
				PInterface pInterface = mapper.readValue(entityValue, PInterface.class);
				if (pInterface != null) {
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradPinterfaceAndChildNodes(pInterface, "pserver", hostname,
								action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradPinterfaceAndChildNodes(pInterface, "pnf", pnfName, action,
								datetimestamp);
					}
				}
				break;
			case "pluggable":
				Pluggable pluggable = mapper.readValue(entityValue, Pluggable.class);
				if (pluggable != null) {
					status = eventProcessor.processNaradPluggableChildNodes(pluggable, chassisname, cardslotname,
							cardtype, pluggableslotname, action, datetimestamp);
				}
				break;
			case "pluggable-slot":
				PluggableSlot pluggableSlot = mapper.readValue(entityValue, PluggableSlot.class);
				if (pluggableSlot != null) {
					status = eventProcessor.processNaradPluggableSlotChildNodes(pluggableSlot, chassisname,
							cardslotname, cardtype, action, datetimestamp);
				}
				break;
			case "pnf":
				Pnf pnf = mapper.readValue(entityValue, Pnf.class);
				if (pnf != null) {
					status = eventProcessor.processNaradPnfAndChildNodes(pnf, action, datetimestamp);
				}
				break;
			case "port":
				Port port = mapper.readValue(entityValue, Port.class);
				if (port != null) {
					status = eventProcessor.processNaradPort(port, chassisname, cardslotname, cardtype,
							pluggableslotname, pluggabletype, action, datetimestamp);
				}
				break;
			case "pserver":
				Pserver pserver = mapper.readValue(entityValue, Pserver.class);
				if (pserver != null) {
					status = eventProcessor.processNaradPserverAndChildNodes(pserver, action, datetimestamp);
				}
				break;
			case "rack":
				Rack rack = mapper.readValue(entityValue, Rack.class);
				if (rack != null) {
					status = eventProcessor.processNaradRackChildNodes(rack, physicallocationid, action, datetimestamp);
				}
				break;
			case "subnet":
				Subnet subnet = mapper.readValue(entityValue, Subnet.class);
				if (StringUtils.isNotBlank(networkId)) {
					if (subnet != null) {
						status = eventProcessor.processSubnet(subnet, Constants.L3_NETWORK_ENTITY, networkId, action,
								datetimestamp);
					}
				} else if (StringUtils.isNotBlank(zoneId)) {
					if (subnet != null) {
						status = eventProcessor.processSubnet(subnet, Constants.ZONE_ENTITY, zoneId, action,
								datetimestamp);
					}
				}
				break;
			case "vlan":
				Vlan vlan = mapper.readValue(entityValue, Vlan.class);
				if (vlan != null) {
					if (!hostname.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradVlanAndChildNodes(vlan, linterfacename, "pserver", hostname,
								cloudregiontenant, pinterfacename, laginterfacename, action, datetimestamp);
					} else if (!pnfName.equalsIgnoreCase("")) {
						status = eventProcessor.processNaradVlanAndChildNodes(vlan, linterfacename, "pnf", pnfName,
								cloudregiontenant, pinterfacename, laginterfacename, action, datetimestamp);
					}
				}
				break;
			case "zone":
				Zone zone = mapper.readValue(entityValue, Zone.class);
				if (zone != null) {
					status = eventProcessor.processZone(zone, action, datetimestamp);
				}
				break;

			}
		}

		return status;
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

	public static boolean populateStoreNaradData(String entity, Integer duration) {
		boolean status = true;

		String entityUrl = NARAD_API_URL + entity + "?hours=" + duration;

		// convert /narad/v3 to /narad/recents/v3
		entityUrl = entityUrl.replaceFirst("/narad/", "/narad/recents/");
		System.out.println(entityUrl);

		status = storeNaradEntities(entityUrl, entity);

		return status;
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");

		boolean result = EventUtil.loadConfigs();
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_LOAD_CONFIG_ERROR);
			System.exit(1);
		}

		// Initialize rest client after loading configurations
		restClient = new RESTClient();
		eventProcessor = new EventProcessor();
		dbadapter = eventProcessor.getDBAdapter();

		String type = args.length > 0 ? args[0] : "SYNCDATA";
		if (type.equalsIgnoreCase("SYNCDATA")) {
			System.out.println("Inside Sync Data block.");
			String entity = args.length > 1 ? args[1] : "ALL";
			String configDir = System.getenv("DTI_CONFIG");
			if (configDir == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
				return;
			}

			FileInputStream dtiStream = null;
			FileInputStream fis = null;
			try {
				dtiStream = new FileInputStream(Util.safeFileName(configDir) + "/dti.properties");
				dtiProps.load(dtiStream);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				closeFileInputStream(dtiStream);
				closeFileInputStream(fis);
			}

			if (System.getenv("narad_api_url") != null && !System.getenv("narad_api_url").equalsIgnoreCase("")) {
				NARAD_API_URL = System.getenv("narad_api_url");
			}

			int hoursInAWeek = SyncNaradData.HOURS_FOR_A_DAY * SyncNaradData.DAYS_IN_A_WEEK;
			if ("ALL".equalsIgnoreCase(entity)) {
				for (String naradEntity : naradEntities.keySet()) {
					populateStoreNaradData(naradEntity, hoursInAWeek);
				}
			} else if (naradEntities.containsKey(entity)) {
				populateStoreNaradData(entity, hoursInAWeek);
			} else {
				System.out.println("ERROR: Invalid option");
			}
		}

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXIT_DEBUG);
		// System.exit(0);

	}

}
