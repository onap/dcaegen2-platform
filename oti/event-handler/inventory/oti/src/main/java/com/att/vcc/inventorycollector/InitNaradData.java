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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.schema.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;

public class InitNaradData {
	private static Properties dtiProps = new Properties();

	private static final int PAGE_SIZE = 1000;
	private static String NARAD_API_URL;
	private static RESTClient restClient;
	private static EventProcessor eventProcessor;

	private static Map<String, Integer> naradEntities;
	private static Map<String, String> naradEntitiesNodesUrl;
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

	private static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	private static Integer getNaradEntitiesCount(String naradAPIUrl, String naradEntity) {
		Integer totalEntities = 0;
		try {
			String response = restClient.retrieveAAIObject(naradAPIUrl);
			if (!response.equalsIgnoreCase("")) {
				JSONObject responseJSONObject = null;
				responseJSONObject = new JSONObject(response);
				JSONArray countsArray;
				if (responseJSONObject.has("results")) {
					countsArray = responseJSONObject.optJSONArray("results");
					totalEntities = countsArray.optJSONObject(0).getInt(naradEntity);

					System.out.println(
							"Total number of entity: " + naradEntity + " available in NARAD is : " + totalEntities);
				} else {
					System.out.println("Error returned from Narad API.");
				}
			}
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return totalEntities;
	}

	private static boolean storeNaradEntities(String naradAPIUrl, String naradEntity) {
		boolean status = false;
		try {
			String response = restClient.retrieveAAIObject(naradAPIUrl);
			if (!response.equalsIgnoreCase("")) {
				JSONObject responseJSONObject = null;
				responseJSONObject = new JSONObject(response);
				JSONArray entitiesArray = responseJSONObject.optJSONArray("results");
				int totalRecords = entitiesArray.length();
				for (int i = 0; i < totalRecords; i++) {
					String entityValue = entitiesArray.optJSONObject(i).getString(naradEntity);
					String url = entitiesArray.optJSONObject(i).getString("url");
					Map<String, String> urlValues = EventUtil.parseUrl(naradEntities, url);
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

	public static boolean populateStoreNaradData(String entity) {
		boolean status = true;
		String urlEntity = naradEntitiesNodesUrl.containsKey(entity) ? naradEntitiesNodesUrl.get(entity) : entity + "s";
		String entityCountUrl = NARAD_API_URL + "nodes/" + urlEntity + "?format=count";
		Integer totalEntity = getNaradEntitiesCount(entityCountUrl, entity);
		int totalPages = 0;
		if (totalEntity > 0) {
			totalPages = (int) Math.ceil((double) totalEntity / PAGE_SIZE);
			System.out.println("Total Pages: " + totalPages);
			String detailedEntityUrl;
			boolean dbStatus;
			for (int i = 1; i <= totalPages; i++) {
				detailedEntityUrl = NARAD_API_URL + "nodes/" + urlEntity + "?format=resource_and_url" + "&resultIndex="
						+ i + "&resultSize=" + PAGE_SIZE;
				dbStatus = storeNaradEntities(detailedEntityUrl, entity);
				if (dbStatus == false)
					status = dbStatus;
			}
		} else {
			System.out.println("No records available for entity: " + entity + " in NARAD");
		}
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

		String type = args.length > 0 ? args[0] : "UEB";
		if (type.equalsIgnoreCase("INITDATA")) {
			System.out.println("Inside Data initialization block.");
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

			if ("ALL".equalsIgnoreCase(entity)) {
				for (String naradEntity : naradEntities.keySet()) {
					populateStoreNaradData(naradEntity);
				}
			} else if (naradEntities.containsKey(entity)) {
				populateStoreNaradData(entity);
			} else {
				System.out.println("ERROR: Invalid option");
			}
		}

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXIT_DEBUG);
		// System.exit(0);

	}

}
