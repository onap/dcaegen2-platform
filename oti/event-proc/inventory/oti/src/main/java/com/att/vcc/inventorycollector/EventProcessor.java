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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.att.vcc.inventorycollector.data.*;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.schema.*;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.messages.*;

public class EventProcessor {
	private static boolean isSync = false;
	private static final int NO_UPDATE = 0;
	private static final int SUCCESS_UPDATE = 1;
	protected static RESTClient restClient = null;
	protected static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	private DBAdapter dbadapter;
	private String domain;
	private String eventType;
	private String headerEntityType;

	private String eventHeaderDomain;
	private String eventHeaderAction;
	private String topEntityType;
	private String eventHeaderId;
	private JSONObject eventHeader;
	private JSONObject eventEntity;

	public EventProcessor() {

		if (!EventUtil.DCAEENV.equalsIgnoreCase("D1") && Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_PG_LOAD)) {
			dbadapter = new DBAdapter();
			if (dbadapter.getDBfactory() == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_DB_FAILURE);
				System.exit(1);
			}
			dbadapter.attachShutDownHook();
		}
		domain = EventUtil.DOMAIN;

	}

	public DBAdapter getDBAdapter() {
		return this.dbadapter;
	}

	public String setEventType(String newEventType) {
		this.eventType = newEventType;
		return this.eventType;
	}

	public String getEventType() {
		return this.eventType;
	}

	public String setHeaderEntityType(String entityType) {
		this.headerEntityType = entityType;
		return this.headerEntityType;
	}

	public String getHeaderEntityType() {
		return this.headerEntityType;
	}

	protected boolean isValidEvent(String event) {
		boolean status = true;
		if (event == null || event.length() == 0) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_UEB_EVENT_EMPTY);
			status = false;
		}
		return status;
	}

	protected JSONObject getEventHeader() {
		return this.eventHeader;
	}

	private JSONObject setEventHeader(JSONObject eventObj) {
		this.eventHeader = null;
		if (!eventObj.isNull(Constants.EVENT_HEADER_FIELD)) {
			this.eventHeader = eventObj.getJSONObject(Constants.EVENT_HEADER_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_HEADER,
					eventObj.toString());
		}
		return this.eventHeader;
	}

	protected JSONObject getEventEntity() {
		return this.eventEntity;
	}

	private JSONObject setEventEntity(JSONObject eventObj) {
		this.eventEntity = null;
		if (!eventObj.isNull(Constants.EVENT_ENTITY_FIELD)) {
			this.eventEntity = eventObj.getJSONObject(Constants.EVENT_ENTITY_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_HEADER,
					eventObj.toString());
		}
		return this.eventEntity;
	}

	protected String getEventHeaderAction() {
		return this.eventHeaderAction;
	}

	private String setEventHeaderAction(JSONObject eventHeaderObj) {
		this.eventHeaderAction = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_ACTION_FIELD)) {
			this.eventHeaderAction = eventHeaderObj.getString(Constants.EVENT_HEADER_ACTION_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_ACTION,
					eventHeaderObj.toString());
		}
		return this.eventHeaderAction;
	}

	private String setEventHeaderEventType(JSONObject eventHeaderObj) {
		eventType = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_EVENT_TYPE_FIELD)) {
			eventType = eventHeaderObj.getString(Constants.EVENT_HEADER_EVENT_TYPE_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_TYPE,
					eventHeaderObj.toString());
		}
		return eventType;
	}

	private String setEventHeaderEntityType(JSONObject eventHeaderObj) {
		headerEntityType = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_ENTITY_TYPE_FIELD)) {
			headerEntityType = eventHeaderObj.getString(Constants.EVENT_HEADER_ENTITY_TYPE_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_ENTITY_TYPE,
					eventHeaderObj.toString());
		}
		return headerEntityType;
	}

	protected String getEventHeaderTopEntityType() {
		return this.topEntityType;
	}

	private String setEventHeaderTopEntityType(JSONObject eventHeaderObj) {
		topEntityType = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_TOP_ENTITY_TYPE_FIELD)) {
			topEntityType = eventHeaderObj.getString(Constants.EVENT_HEADER_TOP_ENTITY_TYPE_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_ENTITY_TYPE,
					eventHeaderObj.toString());
		}
		return topEntityType;
	}

	protected String getEventHeaderId() {
		return this.eventHeaderId;
	}

	private String setEventHeaderId(JSONObject eventHeaderObj) {
		eventHeaderId = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_ID_FIELD)) {
			eventHeaderId = eventHeaderObj.getString(Constants.EVENT_HEADER_ID_FIELD);
		} else {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_NO_UEB_EVENT_ENTITY_TYPE,
					eventHeaderObj.toString());
		}
		return eventHeaderId;
	}

	protected String getEventHeaderDomain() {
		return this.eventHeaderDomain;
	}

	private String setEventHeaderDomain(JSONObject eventHeaderObj) {
		String headerDomain = "";
		if (!eventHeaderObj.isNull(Constants.EVENT_HEADER_DOMAIN_FIELD)) {
			headerDomain = eventHeaderObj.getString(Constants.EVENT_HEADER_DOMAIN_FIELD);
		}
		return headerDomain;
	}

	public boolean processUEBMsg(String uebMsg) throws IOException {
		boolean status = validateEvent(uebMsg);
		if (status) {
			eventEntity = getEventEntity();
			String entity = eventEntity.toString();
			ObjectMapper mapper = EventUtil.getObjectMapperObject();
			String action = getEventHeaderAction();
			String datetimestamp = getCurrentTimestamp();
			String id = this.getEventHeaderId();

			Complex complex = null;
			Chassis chassis = null;
			Pserver pserver = null;
			Pnf pnf = null;
			L3Network l3Network = null;
			PhysicalLink physicalLink = null;
			LogicalLink logicalLink = null;
			InstanceGroup instancegroup = null;
			CloudRegion cloudRegion = null;
			Zone zone = null;

			// Need to remove this method!
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				switch (headerEntityType) {
				case Constants.CABLE_ENTITY:
				case Constants.COMPLEX_ENTITY:
				case Constants.RACK_ENTITY:
					complex = mapper.readValue(entity, Complex.class);
					if (complex != null) {
						status = processNaradComplexChildNodes(complex, action, datetimestamp);
					}
					break;
				case "card":
				case "card-slot":
				case "pluggable-slot":
				case "pluggable":
				case "port":
				case "chassis":
					chassis = mapper.readValue(entity, Chassis.class);
					if (chassis != null) {
						status = processNaradChassisChildNodes(chassis, action, datetimestamp);
					}
					break;
				case "instance-group":
					instancegroup = mapper.readValue(entity, InstanceGroup.class);
					if (instancegroup != null) {
						status = processNaradInstanceGroup(instancegroup, action, datetimestamp);
					}
					break;
				case "cloud-region":
					cloudRegion = mapper.readValue(entity, CloudRegion.class);
					if (cloudRegion != null) {
						status = syncCloudRegionAndChildNodes(cloudRegion, action, datetimestamp);
					}
					break;
				case "l3-network":
					l3Network = mapper.readValue(entity, L3Network.class);
					if (l3Network != null) {
						status = syncL3NetworkAndChildNodes(l3Network, action, datetimestamp);
					}
					break;
				case "logical-link":
					logicalLink = mapper.readValue(entity, LogicalLink.class);
					if (logicalLink != null) {
						status = processLogicalLink(logicalLink, action, datetimestamp);
					}
					break;
				case "physical-link":
					physicalLink = mapper.readValue(entity, PhysicalLink.class);
					if (physicalLink != null) {
						status = processPhysicalLink(physicalLink, action, datetimestamp);
					}
					break;
				case "subnet":
					if (topEntityType.equalsIgnoreCase(Constants.L3_NETWORK_ENTITY)) {
						l3Network = mapper.readValue(entity, L3Network.class);
						if (l3Network != null) {
							status = processL3Subnet(l3Network, action, datetimestamp);
						}
					} else if (topEntityType.equalsIgnoreCase(Constants.ZONE_ENTITY)) {
						zone = mapper.readValue(entity, Zone.class);
						if (zone != null) {
							status = processZoneSubnets(zone, action, datetimestamp);
						}
					}
					break;
				case "zone":
					zone = mapper.readValue(entity, Zone.class);
					if (zone != null) {
						status = processNaradZoneAndChildNodes(zone, action, datetimestamp);
					}
					break;
				default:
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_EVENT_ENTITY_TYPE_NOT_PROCESSED,
							headerEntityType, id);
				}

				// topEntity "pserver" and "pnf" covers the following entities:
				// l-interface, lag-interface, p-interface, vlan,
				// l3-interface-ipv4-address-list &
				// l3-interface-ipv6-address-list
				if (topEntityType.equalsIgnoreCase("pserver")) {
					pserver = mapper.readValue(entity, Pserver.class);
					if (pserver != null) {
						status = processNaradPserverAndChildNodes(pserver, action, datetimestamp);
					}
				} else if (topEntityType.equalsIgnoreCase("pnf")) {
					pnf = mapper.readValue(entity, Pnf.class);
					if (pnf != null) {
						status = processNaradPnfAndChildNodes(pnf, action, datetimestamp);
					}
				}
			}
		}
		return status;
	}

	public boolean validateEvent(String event) {

		boolean status = this.isValidEvent(event);
		if (!status)
			return status;

		JSONObject objMsg = new JSONObject(event);

		this.setEventHeader(objMsg);
		this.setEventEntity(objMsg);
		String uebDomain = "";
		try {
			this.setEventHeaderTopEntityType(eventHeader);
			this.setEventHeaderAction(eventHeader);
			this.setEventHeaderEventType(eventHeader);
			this.setEventHeaderEntityType(eventHeader);
			this.setEventHeaderDomain(eventHeader);
			uebDomain = getEventHeaderDomain();
			this.setEventHeaderId(eventHeader);
			if (domain.length() > 0 && !uebDomain.matches(domain)) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_DIFFERENT_DOMAIN_ERROR, domain,
						uebDomain);
				status = false;
			}
		} catch (NullPointerException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_EXCEPTION, e.toString());
		}
		if (eventHeader == null || eventEntity == null || topEntityType == null || topEntityType.isEmpty()
				|| eventHeaderAction == null || eventHeaderAction.isEmpty() || eventType == null || eventType.isEmpty()
				|| headerEntityType == null || headerEntityType.isEmpty() || eventHeaderId == null
				|| eventHeaderId.isEmpty())
			status = false;

		return status;
	}

	public boolean processNaradComplexChildNodes(Complex complex, String action, String datetimestamp) {
		boolean result = false;

		if (complex != null) {
			if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.COMPLEX_ENTITY)) {
				result = processComplex(complex, action, datetimestamp);
			}
			String physicallocationid = complex.getPhysicalLocationId();
			if (physicallocationid == null) {
				ecompLogger.debug("physicallocationid is null");
				return result;
			}
			Cables cables = complex.getCables();
			if (cables != null) {
				List<Cable> cableList = cables.getCable();
				for (Cable cable : cableList) {
					result = processNaradCable(cable, physicallocationid, action, datetimestamp);
				}
			}
			Racks racks = complex.getRacks();
			if (racks != null) {
				List<Rack> rackList = racks.getRack();
				for (Rack rack : rackList) {
					result = processNaradRackChildNodes(rack, physicallocationid, action, datetimestamp);
				}
			}
		}
		return result;
	}

	public boolean processNaradRackChildNodes(Rack rack, String physicallocationid, String action,
			String datetimestamp) {
		boolean result = false;

		if (rack != null) {
			if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.RACK_ENTITY)
					|| headerEntityType
							.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.COMPLEX_ENTITY)) {
				result = processNaradRack(rack, physicallocationid, action, datetimestamp);
			}
		}
		return result;
	}

	public boolean processNaradChassisChildNodes(Chassis chassis, String action, String datetimestamp) {
		String cardtype = "";
		String cardslotname = "";
		boolean result = false;
		if (chassis != null) {
			if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.CHASSIS_ENTITY)
					|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.RACK_ENTITY)
					|| headerEntityType
							.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.COMPLEX_ENTITY)) {
				result = processNaradChassis(chassis, action, datetimestamp);
			}
			PluggableSlots pluggableSlots = chassis.getPluggableSlots();
			if (pluggableSlots != null) {
				List<PluggableSlot> pluggableSlotList = pluggableSlots.getPluggableSlot();
				for (PluggableSlot pluggableSlot : pluggableSlotList) {
					result = processNaradPluggableSlotChildNodes(pluggableSlot, chassis.getChassisName(), cardslotname,
							cardtype, action, datetimestamp);
				}
			}
			Ports ports = chassis.getPorts();
			if (ports != null) {
				List<Port> portList = ports.getPort();
				for (Port port : portList) {
					String pluggableslotname = "";
					String pluggabletype = "";
					result = processNaradPort(port, chassis.getChassisName(), cardslotname, cardtype, pluggableslotname,
							pluggabletype, action, datetimestamp);
				}
			}
			CardSlots cardSlots = chassis.getCardSlots();
			if (cardSlots != null) {
				List<CardSlot> cardSlotList = cardSlots.getCardSlot();
				for (CardSlot cardslot : cardSlotList) {
					result = processNaradCardSlotChildNodes(cardslot, chassis.getChassisName(), action, datetimestamp);
				}
			}

		}
		return result;
	}

	public boolean processNaradPluggableSlotChildNodes(PluggableSlot pluggableSlot, String chassisname,
			String cardslotname, String cardtype, String action, String datetimestamp) {
		boolean result = false;
		if (pluggableSlot != null) {
			result = processNaradPluggableSlot(pluggableSlot, chassisname, cardslotname, cardtype, action,
					datetimestamp);
			Pluggables pluggables = pluggableSlot.getPluggables();
			if (pluggables != null) {
				List<Pluggable> pluggableList = pluggables.getPluggable();
				for (Pluggable pluggable : pluggableList) {
					result = processNaradPluggableChildNodes(pluggable, chassisname, cardslotname, cardtype,
							pluggableSlot.getSlotName(), action, datetimestamp);
				}
			}
		}
		return result;
	}

	public boolean processNaradCardSlotChildNodes(CardSlot cardslot, String chassisname, String action,
			String datetimestamp) {
		boolean result = false;
		if (cardslot != null) {
			result = processNaradCardSlot(cardslot, chassisname, action, datetimestamp);
			String cardslotname = cardslot.getSlotName();
			Cards cards = cardslot.getCards();
			if (cards != null) {
				List<Card> cardList = cards.getCard();
				for (Card card : cardList) {
					result = processNaradCardChildNodes(card, chassisname, cardslotname, action, datetimestamp);
				}
			}
		}
		return result;
	}

	public boolean processNaradCardChildNodes(Card card, String chassisname, String cardslotname, String action,
			String datetimestamp) {
		boolean result = false;
		if (card != null) {
			result = processNaradCard(card, chassisname, cardslotname, action, datetimestamp);
			Ports ports = card.getPorts();
			if (ports != null) {
				List<Port> portList = ports.getPort();
				for (Port port : portList) {
					String pluggableslotname = "";
					String pluggabletype = "";
					result = processNaradPort(port, chassisname, cardslotname, card.getCardType(), pluggableslotname,
							pluggabletype, action, datetimestamp);
				}
			}
			PluggableSlots pluggableSlots = card.getPluggableSlots();
			if (pluggableSlots != null) {
				List<PluggableSlot> pluggableSlotList = pluggableSlots.getPluggableSlot();
				for (PluggableSlot pluggableSlot : pluggableSlotList) {
					result = processNaradPluggableSlotChildNodes(pluggableSlot, chassisname, cardslotname,
							card.getCardType(), action, datetimestamp);
				}
			}
		}
		return result;
	}

	public boolean processNaradPluggableChildNodes(Pluggable pluggable, String chassisname, String cardslotname,
			String cardtype, String pluggableslotname, String action, String datetimestamp) {
		boolean result = false;
		if (pluggable != null) {
			result = processNaradPluggable(pluggable, chassisname, cardslotname, cardtype, pluggableslotname, action,
					datetimestamp);
			Ports ports = pluggable.getPorts();
			if (ports != null) {
				List<Port> portList = ports.getPort();
				for (Port port : portList) {
					result = processNaradPort(port, chassisname, cardslotname, cardtype, pluggableslotname,
							pluggable.getPluggableType(), action, datetimestamp);
				}
			}
		}
		return result;
	}

	public boolean processNaradRack(Rack rack, String physicallocationid, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "rack";

		if (rack != null) {
			String rackname = rack.getRackName() != null ? rack.getRackName() : "";
			String racktype = rack.getRackType() != null ? rack.getRackType() : "";
			String racklocation = rack.getRackLocation() != null ? rack.getRackLocation() : "";
			String rackposition = rack.getRackPosition() != null ? rack.getRackPosition() : "";
			String rackpowerdiversity = rack.isRackPowerDiversity() ? "Y" : "N";
			String resourceversion = rack.getResourceVersion() != null ? rack.getResourceVersion() : "";

			String fromNodeId = physicallocationid + "|" + rackname;
			RelationshipList relList = rack.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					physicallocationid + "^" + rackname + "^" + racktype + "^" + racklocation + "^" + rackposition + "^"
							+ rackpowerdiversity + "^" + resourceversion + "^" + datetimestamp,
					lRelationship, entityType, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradCable(Cable cable, String physicallocationid, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "cable";

		if (cable != null) {
			String cablename = cable.getCableName() != null ? cable.getCableName() : "";
			String cabletype = cable.getCableType() != null ? cable.getCableType() : "";
			String resourceversion = cable.getResourceVersion() != null ? cable.getResourceVersion() : "";

			String fromNodeId = physicallocationid + "|" + cablename;
			RelationshipList relList = cable.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(physicallocationid + "^" + cablename + "^" + cabletype + "^" + resourceversion + "^"
					+ datetimestamp, lRelationship, entityType, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradChassis(Chassis chassis, String action, String datetimestamp) {
		boolean result = false;

		if (chassis != null) {
			String chassisname = chassis.getChassisName() != null ? chassis.getChassisName() : "";
			String chassistype = chassis.getChassisType() != null ? chassis.getChassisType() : "";
			String chassisrole = chassis.getChassisRole() != null ? chassis.getChassisRole() : "";
			String serialnumber = chassis.getSerialNumber() != null ? chassis.getSerialNumber() : "";
			String assettag = chassis.getAssetTag() != null ? chassis.getAssetTag() : "";
			String resourceversion = chassis.getResourceVersion() != null ? chassis.getResourceVersion() : "";

			String relatedFrom = "chassis";
			String fromNodeId = chassisname;
			RelationshipList relList = chassis.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(chassisname + "^" + chassistype + "^" + chassisrole + "^" + serialnumber + "^"
					+ assettag + "^" + resourceversion + "^" + datetimestamp, lRelationship, relatedFrom, action,
					datetimestamp);
		}
		return result;
	}

	public boolean processNaradCardSlot(CardSlot cardslot, String chassisname, String action, String datetimestamp) {
		boolean result = false;

		if (cardslot != null) {
			String entityType = "card-slot";

			String slotname = cardslot.getSlotName() != null ? cardslot.getSlotName() : "";
			String resourceversion = cardslot.getResourceVersion() != null ? cardslot.getResourceVersion() : "";

			String fromNodeId = chassisname + "|" + slotname;
			RelationshipList relList = cardslot.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(chassisname + "^" + slotname + "^" + resourceversion + "^" + datetimestamp,
					lRelationship, entityType, action, datetimestamp);
		}

		return result;
	}

	public boolean processNaradCard(Card card, String chassisname, String slotname, String action,
			String datetimestamp) {
		boolean result = false;

		if (card != null) {
			String entityType = "card";

			String cardtype = card.getCardType() != null ? card.getCardType() : "";
			String resourceversion = card.getResourceVersion() != null ? card.getResourceVersion() : "";

			String fromNodeId = chassisname + "|" + slotname + "|" + cardtype;
			RelationshipList relList = card.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					chassisname + "^" + slotname + "^" + cardtype + "^" + resourceversion + "^" + datetimestamp,
					lRelationship, entityType, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradPluggableSlot(PluggableSlot pslot, String chassisname, String cardslotname,
			String cardtype, String action, String datetimestamp) {
		boolean result = false;

		if (pslot != null) {
			String slotname = pslot.getSlotName() != null ? pslot.getSlotName() : "";
			String resourceversion = pslot.getResourceVersion() != null ? pslot.getResourceVersion() : "";

			String relatedFrom = "pluggable-slot";
			String fromNodeId = chassisname + "|" + cardslotname + "|" + cardtype + "|" + slotname;
			RelationshipList relList = pslot.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(chassisname + "^" + cardslotname + "^" + cardtype + "^" + slotname + "^"
					+ resourceversion + "^" + datetimestamp, lRelationship, relatedFrom, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradPluggable(Pluggable pluggable, String chassisname, String cardslotname, String cardtype,
			String pluggableslotname, String action, String datetimestamp) {
		boolean result = false;

		if (pluggable != null) {
			String pluggabletype = pluggable.getPluggableType() != null ? pluggable.getPluggableType() : "";
			String resourceversion = pluggable.getResourceVersion() != null ? pluggable.getResourceVersion() : "";

			String relatedFrom = "pluggable";
			String fromNodeId = chassisname + "|" + cardslotname + "|" + cardtype + "|" + pluggableslotname + "|"
					+ pluggabletype;
			RelationshipList relList = pluggable.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					chassisname + "^" + cardslotname + "^" + cardtype + "^" + pluggableslotname + "^" + pluggabletype
							+ "^" + resourceversion + "^" + datetimestamp,
					lRelationship, relatedFrom, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradPort(Port port, String chassisname, String cardslotname, String cardtype,
			String pluggableslotname, String pluggabletype, String action, String datetimestamp) {
		boolean result = false;

		if (port != null) {
			String portname = port.getPortName() != null ? port.getPortName() : "";
			String porttype = port.getPortType() != null ? port.getPortType() : "";
			String portrole = port.getPortRole() != null ? port.getPortRole() : "";
			String speedvalue = port.getSpeedValue() != null ? port.getSpeedValue() : "";
			String speedunits = port.getSpeedUnits() != null ? port.getSpeedUnits() : "";
			String resourceversion = port.getResourceVersion() != null ? port.getResourceVersion() : "";

			String relatedFrom = "port";
			String fromNodeId = chassisname + "|" + cardslotname + "|" + cardtype + "|" + pluggableslotname + "|"
					+ pluggabletype + "|" + portname;
			RelationshipList relList = port.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					chassisname + "^" + cardslotname + "^" + cardtype + "^" + pluggableslotname + "^" + pluggabletype
							+ "^" + portname + "^" + porttype + "^" + portrole + "^" + speedvalue + "^" + speedunits
							+ "^" + resourceversion + "^" + datetimestamp,
					lRelationship, relatedFrom, action, datetimestamp);
		}
		return result;
	}

	public boolean processNaradInstanceGroup(InstanceGroup instancegroup, String action, String datetimestamp) {
		boolean result = false;

		if (instancegroup != null) {
			String id = Optional.ofNullable(instancegroup.getId()).orElse("");
			String instancegrouprole = Optional.ofNullable(instancegroup.getInstanceGroupRole()).orElse("");
			String modelinvariantid = Optional.ofNullable(instancegroup.getModelInvariantId()).orElse("");
			String modelversionid = Optional.ofNullable(instancegroup.getModelVersionId()).orElse("");
			String description = Optional.ofNullable(instancegroup.getDescription()).orElse("");
			String instancegrouptype = Optional.ofNullable(instancegroup.getInstanceGroupType()).orElse("");
			String resourceversion = Optional.ofNullable(instancegroup.getResourceVersion()).orElse("");
			String instancegroupname = Optional.ofNullable(instancegroup.getInstanceGroupName()).orElse("");
			String instancegroupfunction = Optional.ofNullable(instancegroup.getInstanceGroupFunction()).orElse("");

			String relatedFrom = "instance-group";
			String fromNodeId = id;
			RelationshipList relList = instancegroup.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					id + "^" + instancegrouprole + "^" + modelinvariantid + "^" + modelversionid + "^" + description
							+ "^" + instancegrouptype + "^" + resourceversion + "^" + instancegroupname + "^"
							+ instancegroupfunction + "^" + datetimestamp,
					lRelationship, relatedFrom, action, datetimestamp);
		}
		return result;
	}

	public boolean storeNaradPnf(Pnf pnf, String action, String datetimestamp) {
		boolean result = false;

		if (pnf.getPnfName() == null)
			return false;

		String pnfname = Optional.ofNullable(pnf.getPnfName()).orElse("");
		String pnfname2 = Optional.ofNullable(pnf.getPnfName()).orElse("");
		String pnfname2source = Optional.ofNullable(pnf.getPnfName2Source()).orElse("");
		String pnfid = Optional.ofNullable(pnf.getPnfId()).orElse("");
		String nfnamingcode =  Optional.ofNullable(pnf.getNfNamingCode()).orElse("");
		String networkoperatingsystem = Optional.ofNullable(pnf.getNetworkOperatingSystem()).orElse("");
		String equiptype = Optional.ofNullable(pnf.getEquipType()).orElse("");
		String equipvendor = Optional.ofNullable(pnf.getEquipVendor()).orElse("");
		String equipmodel = Optional.ofNullable(pnf.getEquipModel()).orElse("");
		String managementoption = Optional.ofNullable(pnf.getManagementOption()).orElse("");
		String ipaddressv4oam = Optional.ofNullable(pnf.getIpaddressV4Oam()).orElse("");
		String swversion = Optional.ofNullable(pnf.getSwVersion()).orElse("");
		String inmaint = pnf.isInMaint() ? "Y" : "N";
		String frameid = Optional.ofNullable(pnf.getFrameId()).orElse("");
		String serialnumber = Optional.ofNullable(pnf.getSerialNumber()).orElse("");
		String ipaddressv4loopback0 = Optional.ofNullable(pnf.getIpaddressV4Loopback0()).orElse("");
		String ipaddressv6loopback0 = Optional.ofNullable(pnf.getIpaddressV6Loopback0()).orElse("");
		String ipaddressv4aim = Optional.ofNullable(pnf.getIpaddressV4Aim()).orElse("");
		String ipaddressv6aim = Optional.ofNullable(pnf.getIpaddressV6Aim()).orElse("");
		String ipaddressv6oam = Optional.ofNullable(pnf.getIpaddressV6Oam()).orElse("");
		String invstatus = Optional.ofNullable(pnf.getInvStatus()).orElse("");
		String resourceversion = Optional.ofNullable(pnf.getResourceVersion()).orElse("");
		String provstatus = Optional.ofNullable(pnf.getProvStatus()).orElse("");
		String opsnote = Optional.ofNullable(pnf.getOpsNote()).orElse("");
		String configvalidationrequestid = Optional.ofNullable(pnf.getConfigValidationRequestId()).orElse("");
		String configvalidationstatus = Optional.ofNullable(pnf.getConfigValidationStatus()).orElse("");
		String nfrole = Optional.ofNullable(pnf.getNfRole()).orElse("");
		String selflink = Optional.ofNullable(pnf.getSelflink()).orElse("");
		String nftype = Optional.ofNullable(pnf.getNfType()).orElse("");
		String nffunction = Optional.ofNullable(pnf.getNfFunction()).orElse("");

		String relatedFrom = "pnf";
		String fromNodeId = pnf.getPnfName();
		RelationshipList relList = pnf.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				pnfname + "^" + pnfname2 + "^" + pnfname2source + "^" + pnfid + "^" + nfnamingcode + "^" + networkoperatingsystem + "^" + equiptype + "^" + equipvendor
						+ "^" + equipmodel + "^" + managementoption + "^" + ipaddressv4oam + "^" + swversion + "^"
						+ inmaint + "^" + frameid + "^" + serialnumber + "^" + ipaddressv4loopback0 + "^"
						+ ipaddressv6loopback0 + "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam
						+ "^" + invstatus + "^" + resourceversion + "^" + provstatus + "^" + opsnote+ "^" + configvalidationrequestid+ "^" + configvalidationstatus+ "^" + nfrole 
						+ "^" + selflink + "^" + nftype + "^" + nffunction + "^" + datetimestamp,
				lRelationship, relatedFrom, action, datetimestamp);

		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_PSERVER_ERROR,
					pnf.getPnfName());
			return result;
		}

		return result;
	}

	public boolean processNaradZoneAndChildNodes(Zone zone, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		status = processZone(zone, action, datetimestamp);
		if (!status)
			allStatus = status;
		status = processZoneSubnets(zone, action, datetimestamp);
		if (!status)
			allStatus = status;
		return allStatus;
	}

	public boolean processNaradPnfAndChildNodes(Pnf pnf, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String parentEntityType = "pnf";
		String parentEntityId = pnf.getPnfName();

		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PNF_ENTITY)) {
			status = storeNaradPnf(pnf, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LInterfaces lInterfaces = pnf.getLInterfaces();
		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInterface : lIntList) {
				status = processNaradLinterfaceAndChildNodes(lInterface, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		LagInterfaces lagInterfaces = pnf.getLagInterfaces();
		if (lagInterfaces != null) {
			List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
			for (LagInterface lagInterface : lagIntList) {
				status = processNaradLaginterfaceAndChildNodes(lagInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		PInterfaces pInterfaces = pnf.getPInterfaces();
		if (pInterfaces != null) {
			List<PInterface> pIntList = pInterfaces.getPInterface();
			for (PInterface pInterface : pIntList) {
				status = processNaradPinterfaceAndChildNodes(pInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean storeNaradPserver(Pserver pserver, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "pserver";
		String hostname = pserver.getHostname() != null ? pserver.getHostname() : "";
		String ptniiEquipName = pserver.getPtniiEquipName() != null ? pserver.getPtniiEquipName() : "";
		String numberOfCpus = pserver.getNumberOfCpus() != null ? pserver.getNumberOfCpus().toString() : "";
		String diskInGigabytes = pserver.getDiskInGigabytes() != null ? pserver.getDiskInGigabytes().toString() : "";
		String ramInMegabytes = pserver.getRamInMegabytes() != null ? pserver.getRamInMegabytes().toString() : "";
		String equipType = pserver.getEquipType() != null ? pserver.getEquipType() : "";
		String equipVendor = pserver.getEquipVendor() != null ? pserver.getEquipVendor() : "";
		String equipModel = pserver.getEquipModel() != null ? pserver.getEquipModel() : "";
		String fqdn = pserver.getFqdn() != null ? pserver.getFqdn() : "";
		String pserverSelflink = pserver.getPserverSelflink() != null ? pserver.getPserverSelflink() : "";
		String ipv4OamAddress = pserver.getIpv4OamAddress() != null ? pserver.getIpv4OamAddress() : "";
		String serialNumber = pserver.getSerialNumber() != null ? pserver.getSerialNumber() : "";
		String pserverId = pserver.getPserverId() != null ? pserver.getPserverId() : "";
		String ipaddressv4loopback0 = pserver.getIpaddressV4Loopback0() != null ? pserver.getIpaddressV4Loopback0()
				: "";
		String ipaddressv6loopback0 = pserver.getIpaddressV6Loopback0() != null ? pserver.getIpaddressV6Loopback0()
				: "";
		String ipaddressv4aim = pserver.getIpaddressV4Aim() != null ? pserver.getIpaddressV4Aim() : "";
		String ipaddressv6aim = pserver.getIpaddressV6Aim() != null ? pserver.getIpaddressV6Aim() : "";
		String ipaddressv6oam = pserver.getIpaddressV6Oam() != null ? pserver.getIpaddressV6Oam() : "";
		String invstatus = pserver.getInvStatus() != null ? pserver.getInvStatus() : "";
		String internetTopology = pserver.getInternetTopology() != null ? pserver.getInternetTopology() : "";
		String inMaint = pserver.isInMaint() ? "Y" : "N";
		String resourceVersion = pserver.getResourceVersion() != null ? pserver.getResourceVersion() : "";
		String pservername2 = pserver.getPserverName2() != null ? pserver.getPserverName2() : "";
		String purpose = pserver.getPurpose() != null ? pserver.getPurpose() : "";
		String provstatus = pserver.getProvStatus() != null ? pserver.getProvStatus() : "";
		String managementoption = pserver.getManagementOption() != null ? pserver.getManagementOption() : "";
		String hostProfile = pserver.getHostProfile() != null ? pserver.getHostProfile() : "";
		String role = pserver.getRole() != null ? pserver.getRole() : "";
		String function = pserver.getFunction() != null ? pserver.getFunction() : "";
		String opsnote = pserver.getOpsNote() != null ? pserver.getOpsNote() : "";

		String relatedFrom = "pserver";
		String fromNodeId = pserver.getHostname();
		RelationshipList relList = pserver.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				hostname + "^" + ptniiEquipName + "^" + numberOfCpus + "^" + diskInGigabytes + "^" + ramInMegabytes
						+ "^" + equipType + "^" + equipVendor + "^" + equipModel + "^" + fqdn + "^" + pserverSelflink
						+ "^" + ipv4OamAddress + "^" + serialNumber + "^" + ipaddressv4loopback0 + "^"
						+ ipaddressv6loopback0 + "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam
						+ "^" + invstatus + "^" + pserverId + "^" + inMaint + "^" + internetTopology + "^"
						+ resourceVersion + "^" + pservername2 + "^" + purpose + "^" + provstatus + "^"
						+ managementoption + "^" + hostProfile + "^" + role + "^" + function + "^" + opsnote + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processNaradPserverAndChildNodes(Pserver pserver, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String parentEntityType = "pserver";
		String parentEntityId = pserver.getHostname();

		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PSERVER_ENTITY)) {
			status = storeNaradPserver(pserver, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LInterfaces lInterfaces = pserver.getLInterfaces();
		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInterface : lIntList) {
				status = processNaradLinterfaceAndChildNodes(lInterface, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		LagInterfaces lagInterfaces = pserver.getLagInterfaces();
		if (lagInterfaces != null) {
			List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
			for (LagInterface lagInterface : lagIntList) {
				status = processNaradLaginterfaceAndChildNodes(lagInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		PInterfaces Pinterfaces = pserver.getPInterfaces();
		if (Pinterfaces != null) {
			List<PInterface> PIntList = Pinterfaces.getPInterface();
			for (PInterface pInterface : PIntList) {
				status = processNaradPinterfaceAndChildNodes(pInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processNaradPinterfaceAndChildNodes(PInterface pInterface, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String lagInterfaceName = "na";
		String pInterfaceName = pInterface.getInterfaceName();
		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PSERVER_ENTITY)
				|| headerEntityType
						.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.P_INTERFACE_ENTITY)) {
			storeNaradPInterface(pInterface, parentEntityType, parentEntityId, action, datetimestamp);
		}

		String entityType = "na";
		String intName = "na";
		String vlanInterface = "na";
		List<L3InterfaceIpv4AddressList> lIpv4 = pInterface.getL3InterfaceIpv4AddressList();
		status = processStoreNaradL3InterfaceIpv4AddressList(lIpv4, entityType, intName, parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
				datetimestamp);
		allStatus = status ? allStatus : status;

		List<L3InterfaceIpv6AddressList> lIpv6 = pInterface.getL3InterfaceIpv6AddressList();
		status = processStoreNaradL3InterfaceIpv6AddressList(lIpv6, entityType, intName, parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
				datetimestamp);
		allStatus = status ? allStatus : status;

		LInterfaces lInterfaces = pInterface.getLInterfaces();
		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInterface : lIntList) {
				status = processNaradLinterfaceAndChildNodes(lInterface, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;

	}

	public boolean storeNaradPInterface(PInterface pInterface, String parentEntityType, String parentEntityId,
			String action, String datetimestamp) {
		boolean result = false;
		String entityType = "p-interface";
		String interfacename = pInterface.getInterfaceName() != null ? pInterface.getInterfaceName() : "";
		String interfacename2 = pInterface.getInterfaceName2() != null ? pInterface.getInterfaceName2() : "";
		String speedvalue = pInterface.getSpeedValue() != null ? pInterface.getSpeedValue() : "";
		String speedunits = pInterface.getSpeedUnits() != null ? pInterface.getSpeedUnits().toString() : "";
		String portdescription = pInterface.getPortDescription() != null ? pInterface.getPortDescription() : "";
		String resourceversion = pInterface.getResourceVersion() != null ? pInterface.getResourceVersion() : "";
		String equipmentidentifier = pInterface.getEquipmentIdentifier() != null ? pInterface.getEquipmentIdentifier()
				: "";
		String interfacerole = pInterface.getInterfaceRole() != null ? pInterface.getInterfaceRole() : "";
		String interfacetype = pInterface.getInterfaceType() != null ? pInterface.getInterfaceType() : "";
		String provstatus = pInterface.getProvStatus() != null ? pInterface.getProvStatus() : "";
		String inmaint = pInterface.isInMaint() ? "Y" : "N";
		String invstatus = pInterface.getInvStatus() != null ? pInterface.getInvStatus() : "";
		String opsnote = pInterface.getOpsNote() != null ? pInterface.getOpsNote() : "";
		String interfacefunction = pInterface.getInterfaceFunction() != null ? pInterface.getInterfaceFunction() : "";
		String macaddr = pInterface.getMacaddr() != null ? pInterface.getMacaddr() : "";
		String selflink = pInterface.getSelflink() != null ? pInterface.getSelflink() : "";

		String relatedFrom = "p-interface";
		String fromNodeId = parentEntityId + "|" + pInterface.getInterfaceName();
		RelationshipList relList = pInterface.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(interfacename + "^" + interfacename2 + "^" + speedvalue + "^" + speedunits + "^" + portdescription + "^"
				+ equipmentidentifier + "^" + interfacerole + "^" + interfacetype + "^" + provstatus + "^"
				+ resourceversion + "^" + inmaint + "^" + invstatus + "^" + opsnote + "^"+ interfacefunction + "^" + macaddr + "^" + parentEntityId + "^" + parentEntityType
				+ "^" + selflink + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean storeNaradLagInterface(LagInterface lagInterface, String parentEntityType, String parentEntityId,
			String action, String datetimestamp) {
		boolean result = false;
		if (lagInterface != null) {
			if (lagInterface.getInterfaceName() == null)
				return result;

			String interfacename = lagInterface.getInterfaceName() != null ? lagInterface.getInterfaceName() : "";
			String interfacedescription = lagInterface.getInterfaceDescription() != null
					? lagInterface.getInterfaceDescription()
					: "";
			String resourceversion = lagInterface.getResourceVersion() != null ? lagInterface.getResourceVersion() : "";
			String speedvalue = lagInterface.getSpeedValue();
			String speedunits = lagInterface.getSpeedUnits() != null ? lagInterface.getSpeedUnits() : "";
			String interfaceid = lagInterface.getInterfaceId() != null ? lagInterface.getInterfaceId() : "";
			String interfacerole = lagInterface.getInterfaceRole() != null ? lagInterface.getInterfaceRole() : "";
			String provstatus = lagInterface.getProvStatus() != null ? lagInterface.getProvStatus() : "";
			String inmaint = lagInterface.isInMaint() ? "Y" : "N";
			String interfaceFunction = lagInterface.getInterfaceFunction() != null ? lagInterface.getInterfaceFunction() : "";
			String opsNote = lagInterface.getOpsNote() != null ? lagInterface.getOpsNote() : "";
			String lacpSystemId = lagInterface.getLacpSystemId() != null ? lagInterface.getLacpSystemId() : "";
		
			String relatedFrom = "lag-interface";
			String fromNodeId = parentEntityId + "|" + lagInterface.getInterfaceName();
			RelationshipList relList = lagInterface.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					interfacename + "^" + interfacedescription + "^" + resourceversion + "^" + speedvalue + "^"
							+ speedunits + "^" + interfaceid + "^" + interfacerole + "^" + provstatus + "^" + inmaint
							+ "^" + lacpSystemId + "^" + opsNote + "^" + interfaceFunction
							+ "^" + parentEntityType + "^" + parentEntityId + "^" + datetimestamp,
					lRelationship, relatedFrom, action, datetimestamp);
			if (!result) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_LAG_INTERFACE_TOFILE_ERROR,
						lagInterface.getInterfaceName());
				return result;
			}

		}
		return result;
	}

	public boolean processNaradLaginterfaceAndChildNodes(LagInterface lagInterface, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = lagInterface.getInterfaceName();
		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PSERVER_ENTITY)
				|| headerEntityType
						.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.LAG_INTERFACE_ENTITY)) {
			storeNaradLagInterface(lagInterface, parentEntityType, parentEntityId, action, datetimestamp);
		}

		LInterfaces lInterfaces = lagInterface.getLInterfaces();
		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInterface : lIntList) {
				status = processNaradLinterfaceAndChildNodes(lInterface, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		List<L3InterfaceIpv4AddressList> lIpv4 = lagInterface.getL3InterfaceIpv4AddressList();
		status = processStoreNaradL3InterfaceIpv4AddressList(lIpv4, "na", "na", parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, "na", action, datetimestamp);
		allStatus = status ? allStatus : status;

		List<L3InterfaceIpv6AddressList> lIpv6 = lagInterface.getL3InterfaceIpv6AddressList();
		status = processStoreNaradL3InterfaceIpv6AddressList(lIpv6, "na", "na", parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, "na", action, datetimestamp);
		allStatus = status ? allStatus : status;

		return allStatus;

	}

	public boolean processNaradLinterfaceAndChildNodes(LInterface lInterface, String parentEntityType,
			String parentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String intName = lInterface.getInterfaceName();
		String vlanInterface = "na";
		String entityType = "l-interface";
		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PSERVER_ENTITY)
				|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.LAG_INTERFACE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.P_INTERFACE_ENTITY)
				|| headerEntityType
						.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.L_INTERFACE_ENTITY)) {
			storeNaradLinterface(lInterface, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
					lagInterfaceName, action, datetimestamp);
		}

		List<L3InterfaceIpv4AddressList> lIpv4 = lInterface.getL3InterfaceIpv4AddressList();
		status = processStoreNaradL3InterfaceIpv4AddressList(lIpv4, entityType, intName, parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
				datetimestamp);
		allStatus = status ? allStatus : status;

		List<L3InterfaceIpv6AddressList> lIpv6 = lInterface.getL3InterfaceIpv6AddressList();
		status = processStoreNaradL3InterfaceIpv6AddressList(lIpv6, entityType, intName, parentEntityType,
				parentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
				datetimestamp);
		allStatus = status ? allStatus : status;

		Vlans vlans = lInterface.getVlans();
		if (vlans != null) {
			List<Vlan> lVlan = vlans.getVlan();
			for (Vlan vlan : lVlan) {
				status = processNaradVlanAndChildNodes(vlan, intName, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				allStatus = status ? allStatus : status;
			}
		}
		return allStatus;
	}

	private boolean processStoreNaradL3InterfaceIpv4AddressList(List<L3InterfaceIpv4AddressList> lIpv4,
			String parentEntityType, String parentInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String vlanInterface, String action, String datetimestamp) {
		boolean status = true;
		boolean allStatus = true;
		if (lIpv4 != null) {
			for (L3InterfaceIpv4AddressList ipv4 : lIpv4) {
				status = storeNaradL3InterfaceIpv4AddressList(ipv4, parentEntityType, parentInterfaceName,
						grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
						vlanInterface, action, datetimestamp);
				allStatus = status ? allStatus : status;
			}
		}
		return allStatus;
	}

	private boolean processStoreNaradL3InterfaceIpv6AddressList(List<L3InterfaceIpv6AddressList> lIpv6,
			String parentEntityType, String parentInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String vlanInterface, String action, String datetimestamp) {
		boolean status = true;
		boolean allStatus = true;
		if (lIpv6 != null) {
			for (L3InterfaceIpv6AddressList ipv6 : lIpv6) {
				status = storeNaradL3InterfaceIpv6AddressList(ipv6, parentEntityType, parentInterfaceName,
						grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
						vlanInterface, action, datetimestamp);
				allStatus = status ? allStatus : status;
			}
		}
		return allStatus;
	}

	public boolean storeNaradLinterface(LInterface lInterface, String parentEntityType, String parentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String action,
			String datetimestamp) {
		boolean result = false;
		String entityType = "l-interface";
		String interfaceName = lInterface.getInterfaceName() != null ? lInterface.getInterfaceName() : "";
		String interfaceRole = lInterface.getInterfaceRole() != null ? lInterface.getInterfaceRole() : "";
		String v6WanLinkIp = lInterface.getV6WanLinkIp() != null ? lInterface.getV6WanLinkIp() : "";
		String selflink = lInterface.getSelflink() != null ? lInterface.getSelflink() : "";
		String interfaceId = lInterface.getInterfaceId() != null ? lInterface.getInterfaceId() : "";
		String macaddr = lInterface.getMacaddr() != null ? lInterface.getMacaddr() : "";
		String networkName = lInterface.getNetworkName() != null ? lInterface.getNetworkName() : "";
		String resourceVersion = lInterface.getResourceVersion() != null ? lInterface.getResourceVersion() : "";
		String managementOption = lInterface.getManagementOption() != null ? lInterface.getManagementOption() : "";
		String interfaceDescription = lInterface.getInterfaceDescription() != null
				? lInterface.getInterfaceDescription()
				: "";
		String isPortMirrored = lInterface.isIsPortMirrored() ? "Y" : "N";
		String isInMaint = lInterface.isInMaint() ? "Y" : "N";
		String provStatus = lInterface.getProvStatus() != null ? lInterface.getProvStatus() : "";
		String isipunnumbered = lInterface.isIsIpUnnumbered() ? "Y" : "N";
		String allowedAddressPairs = lInterface.getAllowedAddressPairs() != null ? lInterface.getAllowedAddressPairs()
				: "";

		String relatedFrom = "l-interface";
		String anchestorInfo = (parentEntityId != null || parentEntityId != "na") ? parentEntityId + "|" : "";
		anchestorInfo += (cloudRegionTenant != "na") ? cloudRegionTenant + "|" : "";
		anchestorInfo += (pInterfaceName != "na") ? pInterfaceName + "|" : "";
		anchestorInfo += (lagInterfaceName != "na") ? lagInterfaceName + "|" : "";
		String fromNodeId = anchestorInfo + interfaceName;
		RelationshipList relList = lInterface.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
		
		String interfacetype = lInterface.getInterfaceType() != null ? lInterface.getInterfaceType() : "";
		
		String adminstatus = lInterface.getAdminStatus() != null ? lInterface.getAdminStatus() : "";
		String opsnote = lInterface.getOpsNote() != null ? lInterface.getOpsNote() : "";
		String interfacefunction = lInterface.getInterfaceFunction()!= null ? lInterface.getInterfaceFunction() : "";
		
		result = storeEventToDB(
				interfaceName + "^" + interfaceRole + "^" + v6WanLinkIp + "^" + selflink + "^" + interfaceId + "^"
						+ macaddr + "^" + networkName + "^" + resourceVersion + "^" + managementOption + "^"
						+ interfaceDescription + "^" + interfacetype  + "^"+ isPortMirrored + "^" + isInMaint + "^" + provStatus + "^"
						+ isipunnumbered + "^" + parentEntityType + "^" + parentEntityId + "^" + allowedAddressPairs
						+ "^" + adminstatus + "^" + opsnote + "^" + interfacefunction
						+ "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean storeNaradL3InterfaceIpv4AddressList(L3InterfaceIpv4AddressList ipv4, String parentEntityType,
			String parentInterfaceName, String grandParentEntityType, String grandParentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String vlanInterface,
			String action, String datetimestamp) {
		boolean result = true;
		String entityType = "l3-interface-ipv4-address-list";
		String l3InterfaceIpv4Address = ipv4.getL3InterfaceIpv4Address() != null ? ipv4.getL3InterfaceIpv4Address()
				: "";
		String l3InterfaceIpv4PrefixLength = ipv4.getL3InterfaceIpv4PrefixLength() != null
				? ipv4.getL3InterfaceIpv4PrefixLength().toString()
				: "";
		String vlanIdInner = ipv4.getVlanIdInner() != null ? ipv4.getVlanIdInner().toString() : "";
		String vlanIdOuter = ipv4.getVlanIdOuter() != null ? ipv4.getVlanIdOuter().toString() : "";
		String isFloating = ipv4.isIsFloating() != null ? ipv4.isIsFloating() ? "Y" : "N" : "N";
		String resourceVersion = ipv4.getResourceVersion() != null ? ipv4.getResourceVersion() : "";
		String neutronNetworkId = ipv4.getNeutronNetworkId() != null ? ipv4.getNeutronNetworkId() : "";
		String neutronNetworkSubId = ipv4.getNeutronSubnetId() != null ? ipv4.getNeutronSubnetId() : "";

		String relatedFrom = "l3-interface-ipv4-address-list";
		/*String anchestorInfo = (grandParentEntityId != null || grandParentEntityId != "na") ? grandParentEntityId + "|" : "";
		anchestorInfo += (parentInterfaceName != "na") ? parentInterfaceName + "|" : "";
		anchestorInfo += (cloudRegionTenant != "na") ? cloudRegionTenant + "|" : "";
		anchestorInfo += (pInterfaceName != "na") ? pInterfaceName + "|" : "";
		anchestorInfo += (lagInterfaceName != "na") ? lagInterfaceName + "|" : "";
		anchestorInfo += (vlanInterface != "na") ? vlanInterface + "|" : "";*/
		String fromNodeId = ipv4.getL3InterfaceIpv4Address();
		RelationshipList relList = ipv4.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(l3InterfaceIpv4Address + "^" + l3InterfaceIpv4PrefixLength + "^" + vlanIdInner + "^"
				+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion + "^" + neutronNetworkId + "^"
				+ neutronNetworkSubId + "^" + parentEntityType + "^" + parentInterfaceName + "^" + grandParentEntityType
				+ "^" + grandParentEntityId + "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName
				+ "^" + vlanInterface + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean storeNaradL3InterfaceIpv6AddressList(L3InterfaceIpv6AddressList ipv6, String parentEntityType,
			String parentInterfaceName, String grandParentEntityType, String grandParentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String vlanInterface,
			String action, String datetimestamp) {
		boolean result = true;
		String entityType = "l3-interface-ipv6-address-list";
		String l3InterfaceIpv6Address = ipv6.getL3InterfaceIpv6Address() != null ? ipv6.getL3InterfaceIpv6Address()
				: "";
		String l3InterfaceIpv6PrefixLength = ipv6.getL3InterfaceIpv6PrefixLength() != null
				? ipv6.getL3InterfaceIpv6PrefixLength().toString()
				: "";
		String vlanIdInner = ipv6.getVlanIdInner() != null ? ipv6.getVlanIdInner().toString() : "";
		String vlanIdOuter = ipv6.getVlanIdOuter() != null ? ipv6.getVlanIdOuter().toString() : "";
		String isFloating = ipv6.isIsFloating() != null ? ipv6.isIsFloating() ? "Y" : "N" : "N";
		String resourceVersion = ipv6.getResourceVersion() != null ? ipv6.getResourceVersion() : "";
		String neutronNetworkId = ipv6.getNeutronNetworkId() != null ? ipv6.getNeutronNetworkId() : "";
		String neutronNetworkSubId = ipv6.getNeutronSubnetId() != null ? ipv6.getNeutronSubnetId() : "";

		String relatedFrom = "l3-interface-ipv6-address-list";
		String fromNodeId = ipv6.getL3InterfaceIpv6Address();
		RelationshipList relList = ipv6.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(l3InterfaceIpv6Address + "^" + l3InterfaceIpv6PrefixLength + "^" + vlanIdInner + "^"
				+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion + "^" + neutronNetworkId + "^"
				+ neutronNetworkSubId + "^" + parentEntityType + "^" + parentInterfaceName + "^" + grandParentEntityType
				+ "^" + grandParentEntityId + "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName
				+ "^" + vlanInterface + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processNaradVlanAndChildNodes(Vlan vlan, String lInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (!(headerEntityType
				.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY)
				|| headerEntityType.equalsIgnoreCase(
						com.att.vcc.inventorycollector.util.Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY))) {
			status = storeNaradVlan(vlan, lInterfaceName, grandParentEntityType, grandParentEntityId, cloudRegionTenant,
					pInterfaceName, lagInterfaceName, action, datetimestamp);
			if (!status)
				allStatus = status;
		}
		String vlanInterface = vlan.getVlanInterface();
		List<L3InterfaceIpv4AddressList> lIpv4 = vlan.getL3InterfaceIpv4AddressList();
		if (lIpv4 != null) {
			status = processStoreNaradL3InterfaceIpv4AddressList(lIpv4, Constants.L_INTERFACE_ENTITY, lInterfaceName,
					grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
					vlanInterface, action, datetimestamp);
			if (!status)
				allStatus = status;
		}
		List<L3InterfaceIpv6AddressList> lIpv6 = vlan.getL3InterfaceIpv6AddressList();
		if (lIpv6 != null) {
			status = processStoreNaradL3InterfaceIpv6AddressList(lIpv6, Constants.L_INTERFACE_ENTITY, lInterfaceName,
					grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
					vlanInterface, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		return allStatus;
	}

	public boolean storeNaradVlan(Vlan vlan, String lInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean result = false;

		String entityType = "vlan";
		String vlaninterface = vlan.getVlanInterface() != null ? vlan.getVlanInterface() : "";
		String vlanidinner = vlan.getVlanIdInner() != null ? vlan.getVlanIdInner().toString() : "";
		String vlanidouter = vlan.getVlanIdOuter() != null ? vlan.getVlanIdOuter().toString() : "";
		String resourceversion = vlan.getResourceVersion() != null ? vlan.getResourceVersion() : "";
		String speedvalue = vlan.getSpeedValue() != null ? vlan.getSpeedValue() : "";
		String speedunits = vlan.getSpeedUnits() != null ? vlan.getSpeedUnits() : "";
		String vlanDesc = vlan.getVlanDescription() != null ? vlan.getVlanDescription() : "";
		String backdoorConnection = vlan.getBackdoorConnection() != null ? vlan.getBackdoorConnection() : "";
		String vpnKey = vlan.getVpnKey() != null ? vlan.getVpnKey() : "";
		String orchestrationStatus = vlan.getOrchestrationStatus() != null ? vlan.getOrchestrationStatus() : "";
		String inMaint = vlan.isInMaint() ? "Y" : "N";
		String provStatus = vlan.getProvStatus() != null ? vlan.getProvStatus() : "";
		String isIpUnnumbered = vlan.isIsIpUnnumbered() ? "Y" : "N";

		result = storeEventToDB(
				vlaninterface + "^" + vlanidinner + "^" + vlanidouter + "^" + resourceversion + "^" + speedvalue + "^"
						+ speedunits + "^" + vlanDesc + "^" + backdoorConnection + "^" + vpnKey + "^"
						+ orchestrationStatus + "^" + inMaint + "^" + provStatus + "^" + isIpUnnumbered + "^"
						+ lInterfaceName + "^" + grandParentEntityType + "^" + grandParentEntityId + "^"
						+ cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName + "^" + datetimestamp,
				null, entityType, action, datetimestamp);
		return result;
	}

	public boolean processPnf(Pnf pnf, String action, String datetimestamp) {
		boolean result = false;
		if (pnf.getPnfName() == null)
			return false;

		String pnfname = pnf.getPnfName() != null ? pnf.getPnfName() : "";
		String pnfname2 = pnf.getPnfName() != null ? pnf.getPnfName() : "";
		String pnfname2source = pnf.getPnfName2Source() != null ? pnf.getPnfName2Source() : "";
		String pnfid = pnf.getPnfId() != null ? pnf.getPnfId() : "";
		String nfnamingcode = pnf.getNfNamingCode() != null ? pnf.getNfNamingCode() : "";
		String networkoperatingsystem = Optional.ofNullable(pnf.getNetworkOperatingSystem()).orElse("");
		String equiptype = pnf.getEquipType() != null ? pnf.getEquipType() : "";
		String equipvendor = pnf.getEquipVendor() != null ? pnf.getEquipVendor() : "";
		String equipmodel = pnf.getEquipModel() != null ? pnf.getEquipModel() : "";
		String managementoption = pnf.getManagementOption() != null ? pnf.getManagementOption() : "";
		String ipaddressv4oam = pnf.getIpaddressV4Oam() != null ? pnf.getIpaddressV4Oam() : "";
		String swversion = pnf.getSwVersion() != null ? pnf.getSwVersion() : "";
		String inmaint = pnf.isInMaint() ? "Y" : "N";
		String frameid = pnf.getFrameId() != null ? pnf.getFrameId() : "";
		String serialnumber = pnf.getSerialNumber() != null ? pnf.getSerialNumber() : "";
		String ipaddressv4loopback0 = pnf.getIpaddressV4Loopback0() != null ? pnf.getIpaddressV4Loopback0() : "";
		String ipaddressv6loopback0 = pnf.getIpaddressV6Loopback0() != null ? pnf.getIpaddressV6Loopback0() : "";
		String ipaddressv4aim = pnf.getIpaddressV4Aim() != null ? pnf.getIpaddressV4Aim() : "";
		String ipaddressv6aim = pnf.getIpaddressV6Aim() != null ? pnf.getIpaddressV6Aim() : "";
		String ipaddressv6oam = pnf.getIpaddressV6Oam() != null ? pnf.getIpaddressV6Oam() : "";
		String invstatus = pnf.getInvStatus() != null ? pnf.getInvStatus() : "";
		String resourceversion = pnf.getResourceVersion() != null ? pnf.getResourceVersion() : "";
		String provstatus = pnf.getProvStatus() != null ? pnf.getProvStatus() : "";
		String opsnote = Optional.ofNullable(pnf.getOpsNote()).orElse("");
		String configvalidationrequestid = Optional.ofNullable(pnf.getConfigValidationRequestId()).orElse("");
		String configvalidationstatus = Optional.ofNullable(pnf.getConfigValidationStatus()).orElse("");
		String nfrole = pnf.getNfRole() != null ? pnf.getNfRole() : "";
		String selflink = pnf.getSelflink() != null ? pnf.getSelflink() : "";
		String relatedFrom = "pnf";
		String fromNodeId = pnf.getPnfName();
		RelationshipList relList = pnf.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(pnfname + "^" + pnfname2 + "^" + pnfname2source + "^" + pnfid + "^" + nfnamingcode + "^" + networkoperatingsystem + "^" + equiptype + "^"
				+ equipvendor + "^" + equipmodel + "^" + managementoption + "^" + ipaddressv4oam + "^" + swversion + "^"
				+ inmaint + "^" + frameid + "^" + serialnumber + "^" + ipaddressv4loopback0 + "^" + ipaddressv6loopback0
				+ "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam + "^" + invstatus + "^"
				+ resourceversion + "^" + provstatus + "^" + opsnote + "^" + configvalidationrequestid + "^" + configvalidationstatus + "^" + nfrole + "^" + selflink + "^" + datetimestamp,
				lRelationship, relatedFrom, action, datetimestamp);
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_PSERVER_ERROR,
					pnf.getPnfName());
			return result;
		}

		return result;
	}

	public boolean processVplsPe(VplsPe vplspe, String action, String datetimestamp) {
		boolean result = false;
		if (vplspe.getEquipmentName() == null)
			return false;

		String equipmentname = vplspe.getEquipmentName() != null ? vplspe.getEquipmentName() : "";
		String provstatus = vplspe.getProvStatus() != null ? vplspe.getProvStatus() : "";
		String ipv4oamaddress = vplspe.getIpv4OamAddress() != null ? vplspe.getIpv4OamAddress() : "";
		String equipmentrole = vplspe.getEquipmentName() != null ? vplspe.getEquipmentName() : "";
		String vlanidouter = vplspe.getVlanIdOuter() != null ? vplspe.getVlanIdOuter().toString() : "";
		String resourceversion = vplspe.getResourceVersion() != null ? vplspe.getResourceVersion() : "";

		String relatedFrom = "vpls-pe";
		String fromNodeId = vplspe.getEquipmentName();
		RelationshipList relList = vplspe.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(equipmentname + "^" + provstatus + "^" + ipv4oamaddress + "^" + equipmentrole + "^"
				+ vlanidouter + "^" + resourceversion + "^" + datetimestamp, lRelationship, relatedFrom, action,
				datetimestamp);
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_VPLSPE_ERROR,
					vplspe.getEquipmentName());
			return result;
		}
		return result;
	}

	List<EntityData> arrangeLstCollectData(List<EntityData> lstColldata) {
		List<EntityData> arrangedData = new ArrayList<>();

		// populate collector data
		String[] collectorSyncObj = EventUtil.COLLECTORSYNCOBJECTS.split(",");
		List<String> lCollObjects = Arrays.asList(collectorSyncObj);
		for (String colObj : lCollObjects) {
			for (EntityData entityData : lstColldata) {
				// if
				// (entityData.getEntityType().equalsIgnoreCase("cloud-regions"))
				// continue;
				if (colObj.equalsIgnoreCase(entityData.getEntityType())) {
					arrangedData.add(entityData);
				}
			}
		}

		return arrangedData;
	}

	public boolean processPinterfaces(VplsPe vplspe, PInterface pInterafce, String action, String datetimestamp,
			String pEntityType) {
		boolean result = false;
		PInterfaces pInterfaces = vplspe.getPInterfaces();
		if (pInterfaces != null) {
			List<PInterface> lPinterfaces = pInterfaces.getPInterface();
			if (!lPinterfaces.isEmpty()) {
				for (PInterface pInt : lPinterfaces) {
					if (pInt.equals(pInterafce)) {
						if (pInt.getInterfaceName() == null)
							continue;
						result = storePinterfacesToFile(pInt, action, datetimestamp, pEntityType,
								vplspe.getEquipmentName());
					}
				}
			}
		}

		return result;
	}

	public boolean storePinterfacesToFile(PInterface pInt, String action, String datetimestamp, String pEntityType,
			String pEntityId) {
		boolean result = false;

		String interfacename = pInt.getInterfaceName() != null ? pInt.getInterfaceName() : "";
		String speedvalue = pInt.getSpeedValue() != null ? pInt.getSpeedValue() : "";
		String speedunits = pInt.getSpeedUnits() != null ? pInt.getSpeedUnits().toString() : "";
		String portrescription = pInt.getPortDescription() != null ? pInt.getPortDescription() : "";
		String equipmentidentifier = pInt.getEquipmentIdentifier() != null ? pInt.getEquipmentIdentifier() : "";
		String interfacerole = pInt.getInterfaceRole() != null ? pInt.getInterfaceRole() : "";
		String interfacetype = pInt.getInterfaceType() != null ? pInt.getInterfaceType() : "";
		String provstatus = pInt.getProvStatus() != null ? pInt.getProvStatus() : "";
		String resourceversion = pInt.getResourceVersion() != null ? pInt.getResourceVersion() : "";
		String inmaint = pInt.isInMaint() ? "Y" : "N";
		String invstatus = pInt.getInvStatus() != null ? pInt.getInvStatus() : "";
		String selflink = pInt.getSelflink() != null ? pInt.getSelflink() : "";

		String relatedFrom = "p-interface";
		String fromNodeId = pEntityId + "|" + pInt.getInterfaceName();
		RelationshipList relList = pInt.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
		result = storeEventToDB(interfacename + "^" + speedvalue + "^" + speedunits + "^" + portrescription + "^"
				+ equipmentidentifier + "^" + interfacerole + "^" + interfacetype + "^" + provstatus + "^"
				+ resourceversion + "^" + inmaint + "^" + invstatus + "^" + pEntityId + "^" + pEntityType + "^"
				+ selflink + "^" + datetimestamp, lRelationship, relatedFrom, action, datetimestamp);
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_PINTERFACES_ERROR,
					pInt.getInterfaceName());
			return result;
		}
		return result;
	}

	public boolean syncPInterfaceAndChildNodes(VplsPe vplspe, String action, String datetimestamp, String pEntityType) {

		if (vplspe != null) {
			PInterfaces pInterfaces = vplspe.getPInterfaces();
			if (pInterfaces != null) {
				List<PInterface> lPinterfaces = pInterfaces.getPInterface();
				if (lPinterfaces != null) {
					for (PInterface pInt : lPinterfaces) {
						processPinterfaces(vplspe, pInt, action, datetimestamp, "vpls-pe");

						LInterfaces lInterfaces = pInt.getLInterfaces();
						syncLinterfaceAndChildNodes(lInterfaces, pEntityType, pInt.getInterfaceName(), action, null,
								null, null, datetimestamp);

					}
				}
			}
		}
		return true;
	}

	public boolean syncPInterfaceAndChildNodes(Pserver pserver, String action, String datetimestamp,
			String pEntityType) {

		if (pserver != null) {
			PInterfaces pInterfaces = pserver.getPInterfaces();
			List<PInterface> lPinterfaces = pInterfaces.getPInterface();
			for (PInterface pInt : lPinterfaces) {
				processPinterfaces(pserver, pInt, action, datetimestamp, "pserver");

				LInterfaces lInterfaces = pInt.getLInterfaces();
				syncLinterfaceAndChildNodes(lInterfaces, pEntityType, pInt.getInterfaceName(), action, null, null, null,
						datetimestamp);

			}
		}
		return true;
	}

	public boolean syncPInterfaceAndChildNodes(Pnf pnf, String action, String datetimestamp, String pEntityType) {

		if (pnf != null) {
			PInterfaces pInterfaces = pnf.getPInterfaces();
			if (pInterfaces != null) {
				List<PInterface> lPinterfaces = pInterfaces.getPInterface();
				if (lPinterfaces != null) {
					for (PInterface pInt : lPinterfaces) {
						processPinterfaces(pnf, pInt, action, datetimestamp, "pnf");

						LInterfaces lInterfaces = pInt.getLInterfaces();
						syncLinterfaceAndChildNodes(lInterfaces, "p-interface", pInt.getInterfaceName(), action, null,
								null, null, datetimestamp);

					}
				}
			}
		}
		return true;
	}

	public boolean syncL3NetworkAndChildNodes(L3Network l3network, String action, String datetimestamp) {
		processL3Network(l3network, action, datetimestamp);
		processL3Subnet(l3network, action, datetimestamp);
		return true;
	}

	public boolean syncPInterfaceAndChildNodes(PInterface pInterface, String parentEntityType, String parentEntityValue,
			String action, String datetimestamp) {

		storePinterfacesToFile(pInterface, action, datetimestamp, parentEntityType, parentEntityValue);

		LInterfaces lInterfaces = pInterface.getLInterfaces();
		syncLinterfaceAndChildNodes(lInterfaces, "p-interface", pInterface.getInterfaceName(), action, null, null, null,
				datetimestamp);
		return true;
	}

	public boolean syncPInterfaceAndChildNodes(Pserver pserver, String action, String datetimestamp) {

		if (pserver != null) {
			PInterfaces pInterfaces = pserver.getPInterfaces();
			if (pInterfaces != null) {
				List<PInterface> lPinterfaces = pInterfaces.getPInterface();
				for (PInterface pInt : lPinterfaces) {
					processPinterfaces(pserver, pInt, action, datetimestamp, "pserver");

					LInterfaces lInterfaces = pInt.getLInterfaces();
					syncLinterfaceAndChildNodes(lInterfaces, "p-interface", pInt.getInterfaceName(), action, null, null,
							null, datetimestamp);
				}
			}
		}
		return true;
	}

	public boolean processPinterfaces(Pnf pnf, PInterface pInterafce, String action, String datetimestamp,
			String pEntityType) {
		boolean result = false;
		String pEntityId = pnf.getPnfName();
		PInterfaces pInterfaces = pnf.getPInterfaces();
		if (pInterfaces != null) {
			List<PInterface> lPinterfaces = pInterfaces.getPInterface();
			if (!lPinterfaces.isEmpty()) {
				for (PInterface pInt : lPinterfaces) {
					if (pInt.equals(pInterafce)) {
						if (pInt.getInterfaceName() == null)
							continue;
						result = storePinterfacesToFile(pInt, action, datetimestamp, pEntityType, pEntityId);

					}
				}
			}
		}

		return result;
	}

	public boolean syncPserverAndChildNodes(Pserver pserver, String action, String datetimestamp) {
		boolean result = true;
		if (pserver != null) {
			processPserver(pserver, action, datetimestamp);
			syncPInterfaceAndChildNodes(pserver, action, datetimestamp);

			LagInterfaces lagInts = pserver.getLagInterfaces();
			processLagInterfacesToFile(lagInts, action, datetimestamp, "p-server", pserver.getHostname());
		}
		return result;
	}

	public boolean syncLinterfaceAndChildNodes(LInterfaces lInterfaces, String pEntityType, String pEntityId,
			String action, String cloudOwner, String cloudRegionId, String tenantId, String datetimestamp) {
		if (lInterfaces != null) {
			List<LInterface> lLinterfaces = lInterfaces.getLInterface();
			processLinterfaceToFile(lInterfaces, action, pEntityType, pEntityId, datetimestamp);

			for (LInterface lInterface : lLinterfaces) {
				String intName = lInterface.getInterfaceName();
				List<L3InterfaceIpv4AddressList> lIpv4 = lInterface.getL3InterfaceIpv4AddressList();
				if (lIpv4 != null) {
					storeL3InterfaceIpv4AddressListToFile(lIpv4, action, "l-interface", intName, pEntityType, pEntityId,
							datetimestamp);
				}
				List<L3InterfaceIpv6AddressList> lIpv6 = lInterface.getL3InterfaceIpv6AddressList();
				if (lIpv6 != null) {
					storeL3InterfaceIpv6AddressListToFile(lIpv6, action, "l-interface", intName, pEntityType, pEntityId,
							datetimestamp);
				}

				Vlans vlans = lInterface.getVlans();

				syncVlanAndChildNodes(vlans, "l-interface", intName, pEntityType, pEntityId, action, datetimestamp);
			}
		}
		return true;
	}

	public boolean syncVlanAndChildNodes(Vlans vlans, String pEntityType, String pIntName, String gpEntityType,
			String gpEntityId, String action, String datetimestamp) {

		if (vlans != null) {
			processVlanToFile(vlans, action, pIntName, gpEntityId, datetimestamp);
			List<Vlan> lVlan = vlans.getVlan();
			for (Vlan vlan : lVlan) {
				List<L3InterfaceIpv4AddressList> lvIpv4 = vlan.getL3InterfaceIpv4AddressList();
				storeL3InterfaceIpv4AddressListToFile(lvIpv4, action, "vlan", vlan.getVlanInterface(), gpEntityType,
						gpEntityId, datetimestamp);

				List<L3InterfaceIpv6AddressList> lvIpv6 = vlan.getL3InterfaceIpv6AddressList();
				storeL3InterfaceIpv6AddressListToFile(lvIpv6, action, "vlan", vlan.getVlanInterface(), gpEntityType,
						gpEntityId, datetimestamp);
			}
		}
		return true;
	}

	public boolean syncCloudRegionAndChildNodes(CloudRegion cloudRegion, String action, String datetimestamp) {

		if (cloudRegion != null) {
			processCloudRegion(cloudRegion, action, datetimestamp);
		}
		return true;
	}

	public boolean storeL3InterfaceIpv4AddressListToFile(List<L3InterfaceIpv4AddressList> list, String action,
			String pEntity, String pIntName, String gEntity, String gIntName, String datetimestamp) {
		boolean result = true;
		for (L3InterfaceIpv4AddressList ipv4 : list) {
			String entityType = "l3-interface-ipv4-address-list";
			String l3InterfaceIpv4Address = ipv4.getL3InterfaceIpv4Address() != null ? ipv4.getL3InterfaceIpv4Address()
					: "";
			String l3InterfaceIpv4PrefixLength = ipv4.getL3InterfaceIpv4PrefixLength() != null
					? ipv4.getL3InterfaceIpv4PrefixLength().toString()
					: "";
			String vlanIdInner = ipv4.getVlanIdInner() != null ? ipv4.getVlanIdInner().toString() : "";
			String vlanIdOuter = ipv4.getVlanIdOuter() != null ? ipv4.getVlanIdOuter().toString() : "";
			String isFloating = ipv4.isIsFloating() != null ? ipv4.isIsFloating() ? "Y" : "N" : "N";
			String resourceVersion = ipv4.getResourceVersion() != null ? ipv4.getResourceVersion() : "";
			String neutronNetworkId = ipv4.getNeutronNetworkId() != null ? ipv4.getNeutronNetworkId() : "";
			String neutronNetworkSubId = ipv4.getNeutronSubnetId() != null ? ipv4.getNeutronSubnetId() : "";

			String relatedFrom = "l3-interface-ipv4-address-list";
			String fromNodeId = ipv4.getL3InterfaceIpv4Address();
			RelationshipList relList = ipv4.getRelationshipList();
			ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
			result = storeEventToDB(l3InterfaceIpv4Address + "^" + l3InterfaceIpv4PrefixLength + "^" + vlanIdInner + "^"
					+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion + "^" + neutronNetworkId + "^"
					+ neutronNetworkSubId + "^" + pEntity + "^" + pIntName + "^" + gEntity + "^" + gIntName + "^"
					+ datetimestamp, lRelationship, entityType, action, datetimestamp);

		}
		return result;
	}

	public boolean processL3InterfaceIpv4AddressList(String entity, String action, String topEntityType,
			String datetimestamp) {
		boolean result = true;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		switch (topEntityType) {
		case "pserver":
			Pserver pserver;
			try {
				pserver = mapper.readValue(entity, Pserver.class);

				PInterfaces pInts = pserver.getPInterfaces();
				if (pInts != null) {
					List<PInterface> pIntList = pInts.getPInterface();
					for (PInterface pInt : pIntList) {
						String pIntName = pInt.getInterfaceName();
						LInterfaces lInterfaces = pInt.getLInterfaces();
						if (lInterfaces != null) {
							List<LInterface> lIntList = lInterfaces.getLInterface();
							for (LInterface lInt : lIntList) {
								String lIntName = pInt.getInterfaceName();
								List<L3InterfaceIpv4AddressList> ipv4List = lInt.getL3InterfaceIpv4AddressList();
								if (!ipv4List.isEmpty()) {
									result = storeL3InterfaceIpv4AddressListToFile(ipv4List, action, "l-interface",
											lIntName, "p-interface", pIntName, datetimestamp);
								} else {
									Vlans vlans = lInt.getVlans();
									if (vlans != null) {
										List<Vlan> vlanList = vlans.getVlan();
										for (Vlan vlan : vlanList) {
											List<L3InterfaceIpv4AddressList> ipv4VlanList = vlan
													.getL3InterfaceIpv4AddressList();
											result = storeL3InterfaceIpv4AddressListToFile(ipv4VlanList, action, "vlan",
													vlan.getVlanInterface(), "l-interface", lIntName, datetimestamp);
										}
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "cloud-region":
			CloudRegion cloudRegion;

			try {
				cloudRegion = mapper.readValue(entity, CloudRegion.class);
				if (cloudRegion != null) {
					Tenants tenants = cloudRegion.getTenants();
					if (tenants != null) {
						List<Tenant> lTenants = tenants.getTenant();
						for (Tenant tenant : lTenants) {

							if (tenant != null) {
								Vservers vservers = tenant.getVservers();
								if (vservers != null) {
									List<Vserver> vserverList = vservers.getVserver();
									for (Vserver vserver : vserverList) {
										LInterfaces lInterfaces = vserver.getLInterfaces();
										if (lInterfaces != null) {
											List<LInterface> lIntList = lInterfaces.getLInterface();
											for (LInterface lInt : lIntList) {
												String intName = lInt.getInterfaceName();
												List<L3InterfaceIpv4AddressList> ipv4List = lInt
														.getL3InterfaceIpv4AddressList();
												if (!ipv4List.isEmpty()) {
													result = storeL3InterfaceIpv4AddressListToFile(ipv4List, action,
															"l-interface", intName, "vserver", vserver.getVserverId(),
															datetimestamp);
												} else {
													Vlans vlans = lInt.getVlans();
													if (vlans != null) {
														List<Vlan> vlanList = vlans.getVlan();
														for (Vlan vlan : vlanList) {
															List<L3InterfaceIpv4AddressList> ipv4VlanList = vlan
																	.getL3InterfaceIpv4AddressList();
															result = storeL3InterfaceIpv4AddressListToFile(ipv4VlanList,
																	action, "vlan", vlan.getVlanInterface(),
																	"l-interface", intName, datetimestamp);
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
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "generic-vnf":
			GenericVnf vnf;
			try {
				vnf = mapper.readValue(entity, GenericVnf.class);
				if (vnf != null) {
					LInterfaces lInterfaces = vnf.getLInterfaces();
					if (lInterfaces != null) {
						List<LInterface> lIntList = lInterfaces.getLInterface();
						for (LInterface lInt : lIntList) {
							String intName = lInt.getInterfaceName();
							List<L3InterfaceIpv4AddressList> ipv4List = lInt.getL3InterfaceIpv4AddressList();
							if (!ipv4List.isEmpty()) {
								result = storeL3InterfaceIpv4AddressListToFile(ipv4List, action, "linterface", intName,
										"generic-vnf", vnf.getVnfId(), datetimestamp);
							} else {
								Vlans vlans = lInt.getVlans();
								if (vlans != null) {
									List<Vlan> vlanList = vlans.getVlan();
									for (Vlan vlan : vlanList) {
										List<L3InterfaceIpv4AddressList> ipv4VlanList = vlan
												.getL3InterfaceIpv4AddressList();
										result = storeL3InterfaceIpv4AddressListToFile(ipv4VlanList, action, "vlan",
												vlan.getVlanInterface(), "linterface", intName, datetimestamp);
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "pnf":
			Pnf pnf;
			try {
				pnf = mapper.readValue(entity, Pnf.class);
				if (pnf != null) {
					LagInterfaces lagInterfaces = pnf.getLagInterfaces();
					if (lagInterfaces != null) {
						List<LagInterface> lagInterfaceList = lagInterfaces.getLagInterface();
						for (LagInterface lagInt : lagInterfaceList) {
							LInterfaces lInterfaces = lagInt.getLInterfaces();
							if (lInterfaces != null) {
								List<LInterface> lIntList = lInterfaces.getLInterface();
								for (LInterface lInt : lIntList) {
									String intName = lInt.getInterfaceName();
									List<L3InterfaceIpv4AddressList> ipv4List = lInt.getL3InterfaceIpv4AddressList();
									if (!ipv4List.isEmpty()) {
										result = storeL3InterfaceIpv4AddressListToFile(ipv4List, action, "linterface",
												intName, "pnf", pnf.getPnfName(), datetimestamp);
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "vpls-pe":
			break;
		default:
			break;
		}

		return result;
	}

	public boolean storeL3InterfaceIpv6AddressListToFile(List<L3InterfaceIpv6AddressList> list, String action,
			String pEntity, String pIntName, String gEntity, String gIntName, String datetimestamp) {
		boolean result = true;
		for (L3InterfaceIpv6AddressList ipv6 : list) {
			String entityType = "l3-interface-ipv6-address-list";
			String l3InterfaceIpv6Address = ipv6.getL3InterfaceIpv6Address() != null ? ipv6.getL3InterfaceIpv6Address()
					: "";
			String l3InterfaceIpv6PrefixLength = ipv6.getL3InterfaceIpv6PrefixLength() != null
					? ipv6.getL3InterfaceIpv6PrefixLength().toString()
					: "";
			String vlanIdInner = ipv6.getVlanIdInner() != null ? ipv6.getVlanIdInner().toString() : "";
			String vlanIdOuter = ipv6.getVlanIdOuter() != null ? ipv6.getVlanIdOuter().toString() : "";
			String isFloating = ipv6.isIsFloating() != null ? ipv6.isIsFloating() ? "Y" : "N" : "N";
			String resourceVersion = ipv6.getResourceVersion() != null ? ipv6.getResourceVersion() : "";
			String neutronNetworkId = ipv6.getNeutronNetworkId() != null ? ipv6.getNeutronNetworkId() : "";
			String neutronNetworkSubId = ipv6.getNeutronSubnetId() != null ? ipv6.getNeutronSubnetId() : "";

			String relatedFrom = "l3-interface-ipv6-address-list";
			String fromNodeId = ipv6.getL3InterfaceIpv6Address();
			RelationshipList relList = ipv6.getRelationshipList();

			ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
			result = storeEventToDB(l3InterfaceIpv6Address + "^" + l3InterfaceIpv6PrefixLength + "^" + vlanIdInner + "^"
					+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion.trim() + "^" + neutronNetworkId + "^"
					+ neutronNetworkSubId + "^" + pEntity + "^" + pIntName + "^" + gEntity + "^" + gIntName + "^"
					+ datetimestamp, lRelationship, entityType, action, datetimestamp);

		}
		return result;
	}

	public boolean processL3InterfaceIpv6AddressList(String entity, String action, String topEntityType,
			String datetimestamp) {
		boolean result = true;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		switch (topEntityType) {
		case "pserver":
			Pserver pserver;
			try {
				pserver = mapper.readValue(entity, Pserver.class);

				PInterfaces pInts = pserver.getPInterfaces();
				if (pInts != null) {
					List<PInterface> pIntList = pInts.getPInterface();
					for (PInterface pInt : pIntList) {
						String intName = pInt.getInterfaceName();
						LInterfaces lInterfaces = pInt.getLInterfaces();
						if (lInterfaces != null) {
							List<LInterface> lIntList = lInterfaces.getLInterface();
							for (LInterface lInt : lIntList) {
								List<L3InterfaceIpv6AddressList> ipv6List = lInt.getL3InterfaceIpv6AddressList();
								if (!ipv6List.isEmpty()) {
									result = storeL3InterfaceIpv6AddressListToFile(ipv6List, action, "l-interface",
											intName, "p-interface", pInt.getInterfaceName(), datetimestamp);
								} else {
									Vlans vlans = lInt.getVlans();
									if (vlans != null) {
										List<Vlan> vlanList = vlans.getVlan();
										for (Vlan vlan : vlanList) {
											List<L3InterfaceIpv6AddressList> ipv6vlanList = vlan
													.getL3InterfaceIpv6AddressList();
											result = storeL3InterfaceIpv6AddressListToFile(ipv6vlanList, action, "vlan",
													vlan.getVlanInterface(), "l-interface", intName, datetimestamp);
										}
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "cloud-region":
			CloudRegion cloudRegion;

			try {
				cloudRegion = mapper.readValue(entity, CloudRegion.class);
				if (cloudRegion != null) {
					Tenants tenants = cloudRegion.getTenants();
					if (tenants != null) {
						List<Tenant> lTenants = tenants.getTenant();
						for (Tenant tenant : lTenants) {

							if (tenant != null) {
								Vservers vservers = tenant.getVservers();
								if (vservers != null) {
									List<Vserver> vserverList = vservers.getVserver();
									for (Vserver vserver : vserverList) {
										LInterfaces lInterfaces = vserver.getLInterfaces();
										if (lInterfaces != null) {
											List<LInterface> lIntList = lInterfaces.getLInterface();
											for (LInterface lInt : lIntList) {
												String intName = lInt.getInterfaceName();
												List<L3InterfaceIpv6AddressList> ipv6List = lInt
														.getL3InterfaceIpv6AddressList();
												if (!ipv6List.isEmpty()) {
													result = storeL3InterfaceIpv6AddressListToFile(ipv6List, action,
															"l-interface", intName, "vserver", vserver.getVserverId(),
															datetimestamp);
												} else {
													Vlans vlans = lInt.getVlans();
													if (vlans != null) {
														List<Vlan> vlanList = vlans.getVlan();
														for (Vlan vlan : vlanList) {
															List<L3InterfaceIpv6AddressList> ipv6vlanList = vlan
																	.getL3InterfaceIpv6AddressList();
															result = storeL3InterfaceIpv6AddressListToFile(ipv6vlanList,
																	action, "vlan", vlan.getVlanInterface(),
																	"l-interface", intName, datetimestamp);
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
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}

			break;
		case "generic-vnf":
			GenericVnf vnf;
			try {
				vnf = mapper.readValue(entity, GenericVnf.class);
				if (vnf != null) {
					LInterfaces lInterfaces = vnf.getLInterfaces();
					if (lInterfaces != null) {
						List<LInterface> lIntList = lInterfaces.getLInterface();
						for (LInterface lInt : lIntList) {
							String intName = lInt.getInterfaceName();
							List<L3InterfaceIpv6AddressList> ipv6List = lInt.getL3InterfaceIpv6AddressList();
							if (!ipv6List.isEmpty()) {
								result = storeL3InterfaceIpv6AddressListToFile(ipv6List, action, "l-interface", intName,
										"generic-vnf", vnf.getVnfId(), datetimestamp);
							} else {
								Vlans vlans = lInt.getVlans();
								if (vlans != null) {
									List<Vlan> vlanList = vlans.getVlan();
									for (Vlan vlan : vlanList) {
										List<L3InterfaceIpv6AddressList> ipv6vlanList = vlan
												.getL3InterfaceIpv6AddressList();
										result = storeL3InterfaceIpv6AddressListToFile(ipv6vlanList, action, "vlan",
												vlan.getVlanInterface(), "l-interface", intName, datetimestamp);
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "pnf":
			Pnf pnf;
			try {
				pnf = mapper.readValue(entity, Pnf.class);
				if (pnf != null) {
					LagInterfaces lagInterfaces = pnf.getLagInterfaces();
					if (lagInterfaces != null) {
						List<LagInterface> lagInterfaceList = lagInterfaces.getLagInterface();
						for (LagInterface lagInt : lagInterfaceList) {
							LInterfaces lInterfaces = lagInt.getLInterfaces();
							if (lInterfaces != null) {
								List<LInterface> lIntList = lInterfaces.getLInterface();
								for (LInterface lInt : lIntList) {
									String intName = lInt.getInterfaceName();
									List<L3InterfaceIpv6AddressList> ipv6List = lInt.getL3InterfaceIpv6AddressList();
									if (!ipv6List.isEmpty()) {
										result = storeL3InterfaceIpv6AddressListToFile(ipv6List, action, "linterface",
												intName, "pnf", pnf.getPnfName(), datetimestamp);
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "vpls-pe":
			break;
		default:
			break;
		}

		return result;
	}

	public boolean processVce(Vce vce, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "vce";

		String vnfId = vce.getVnfId() != null ? vce.getVnfId() : "";
		String vnfName = vce.getVnfName() != null ? vce.getVnfName() : "";
		String vnfName2 = vce.getVnfName2() != null ? vce.getVnfName2() : "";
		String vnfType = vce.getVnfType() != null ? vce.getVnfType() : "";
		String serviceId = vce.getServiceId() != null ? vce.getServiceId() : "";
		String regionalResourceZone = vce.getRegionalResourceZone() != null ? vce.getRegionalResourceZone() : "";
		String provStatus = vce.getProvStatus() != null ? vce.getProvStatus() : "";
		String operationalStatus = vce.getOperationalStatus() != null ? vce.getOperationalStatus() : "";
		String equipmentRole = vce.getEquipmentRole() != null ? vce.getEquipmentRole() : "";
		String orchestrationStatus = vce.getOrchestrationStatus() != null ? vce.getOrchestrationStatus() : "";
		String heatStackId = vce.getHeatStackId() != null ? vce.getHeatStackId() : "";
		String msoCatalogKey = vce.getMsoCatalogKey() != null ? vce.getMsoCatalogKey() : "";
		String vpeId = vce.getVpeId() != null ? vce.getVpeId() : "";
		String v6VceWanAddress = vce.getV6VceWanAddress() != null ? vce.getV6VceWanAddress() : "";
		String ipv4OamAddress = vce.getIpv4OamAddress() != null ? vce.getIpv4OamAddress() : "";
		String resourceVersion = vce.getResourceVersion() != null ? vce.getResourceVersion() : "";
		String ipv4Loopback0Address = vce.getIpv4Loopback0Address() != null ? vce.getIpv4Loopback0Address() : "";

		String relatedFrom = "vce";
		String fromNodeId = vce.getVnfId();
		RelationshipList relList = vce.getRelationshipList();

		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
		result = storeEventToDB(
				vnfId + "^" + vnfName + "^" + vnfName2 + "^" + vnfType + "^" + serviceId + "^" + regionalResourceZone
						+ "^" + provStatus + "^" + operationalStatus + "^" + equipmentRole + "^" + orchestrationStatus
						+ "^" + heatStackId + "^" + msoCatalogKey + "^" + vpeId + "^" + v6VceWanAddress + "^"
						+ ipv4OamAddress + "^" + resourceVersion + "^" + ipv4Loopback0Address + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public String getCurrentTimestamp() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public boolean processGenericVnf(GenericVnf gVnf, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "generic-vnf";

		String vnfId = gVnf.getVnfId() != null ? gVnf.getVnfId() : "";
		String vnfName = gVnf.getVnfName() != null ? gVnf.getVnfName() : "";
		String vnfName2 = gVnf.getVnfName2() != null ? gVnf.getVnfName2() : "";
		String vnfType = gVnf.getVnfType() != null ? gVnf.getVnfType() : "";
		String serviceId = gVnf.getServiceId() != null ? gVnf.getServiceId() : "";
		String regionalResourceZone = gVnf.getRegionalResourceZone() != null ? gVnf.getRegionalResourceZone() : "";
		String provStatus = gVnf.getProvStatus() != null ? gVnf.getProvStatus() : "";
		String operationalStatus = gVnf.getOperationalStatus() != null ? gVnf.getOperationalStatus() : "";
		String equipmentRole = gVnf.getEquipmentRole() != null ? gVnf.getEquipmentRole() : "";
		String orchestrationStatus = gVnf.getOrchestrationStatus() != null ? gVnf.getOrchestrationStatus() : "";
		String heatStackId = gVnf.getHeatStackId() != null ? gVnf.getHeatStackId() : "";
		String msoCatalogKey = gVnf.getMsoCatalogKey() != null ? gVnf.getMsoCatalogKey() : "";
		String managementOption = gVnf.getManagementOption() != null ? gVnf.getManagementOption() : "";
		String ipv4OamAddress = gVnf.getIpv4OamAddress() != null ? gVnf.getIpv4OamAddress() : "";
		String ipv4Loopback0Address = gVnf.getIpv4Loopback0Address() != null ? gVnf.getIpv4Loopback0Address() : "";
		String nmLanV6Address = gVnf.getNmLanV6Address() != null ? gVnf.getNmLanV6Address() : "";
		String managementV6Address = gVnf.getManagementV6Address() != null ? gVnf.getManagementV6Address() : "";
		String vcpu = gVnf.getVcpu() != null ? gVnf.getVcpu().toString() : "";
		String vcpuUnits = gVnf.getVcpuUnits() != null ? gVnf.getVcpuUnits() : "";
		String vmemory = gVnf.getVmemory() != null ? gVnf.getVmemory().toString() : "";
		String vmemoryUnits = gVnf.getVmemoryUnits() != null ? gVnf.getVmemoryUnits() : "";
		String vdisk = gVnf.getVdisk() != null ? gVnf.getVdisk().toString() : "";
		String vdiskUnits = gVnf.getVdiskUnits() != null ? gVnf.getVdiskUnits() : "";
		String inMaint = gVnf.isInMaint() ? "Y" : "N";
		String isClosedLoopDisabled = gVnf.isIsClosedLoopDisabled() ? "Y" : "N";
		String resourceVersion = gVnf.getResourceVersion() != null ? gVnf.getResourceVersion() : "";
		String summarystatus = gVnf.getSummaryStatus() != null ? gVnf.getSummaryStatus() : "";
		String encryptedaccessflag = gVnf.isEncryptedAccessFlag() != null ? gVnf.isEncryptedAccessFlag() ? "Y" : "N"
				: "N";
		String modelInvariantId = gVnf.getModelInvariantId() != null ? gVnf.getModelInvariantId() : "";
		String modelVersionId = gVnf.getModelVersionId() != null ? gVnf.getModelVersionId() : "";
		String modelCustomizationId = gVnf.getModelCustomizationId() != null ? gVnf.getModelCustomizationId() : "";
		String widgetModelId = gVnf.getWidgetModelId() != null ? gVnf.getWidgetModelId() : "";
		String widgetModelVersion = gVnf.getWidgetModelVersion() != null ? gVnf.getWidgetModelVersion() : "";
		String asNumber = gVnf.getAsNumber() != null ? gVnf.getAsNumber() : "";
		String regionalResourceSubzone = gVnf.getRegionalResourceSubzone() != null ? gVnf.getRegionalResourceSubzone()
				: "";
		String nfType = gVnf.getNfType() != null ? gVnf.getNfType() : "";
		String nfRole = gVnf.getNfRole() != null ? gVnf.getNfRole() : "";
		String nfFunction = gVnf.getNfFunction() != null ? gVnf.getNfFunction() : "";
		String nfNamingCode = gVnf.getNfNamingCode() != null ? gVnf.getNfNamingCode() : "";
		String selflink = gVnf.getSelflink() != null ? gVnf.getSelflink() : "";
		String ipv4OamGatewayAddress = gVnf.getIpv4OamGatewayAddress() != null ? gVnf.getIpv4OamGatewayAddress() : "";
		String ipv4OamGatewayAddressPrefixLength = gVnf.getIpv4OamGatewayAddressPrefixLength() != null
				? gVnf.getIpv4OamGatewayAddressPrefixLength().toString()
				: "";
		String vlanIdOuter = gVnf.getVlanIdOuter() != null ? gVnf.getVlanIdOuter().toString() : "";
		String nmProfileName = gVnf.getNmProfileName() != null ? gVnf.getNmProfileName() : "";

		String relatedFrom = "generic-vnf";
		String fromNodeId = gVnf.getVnfId();
		RelationshipList relList = gVnf.getRelationshipList();

		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
		result = storeEventToDB(
				vnfId + "^" + vnfName + "^" + vnfName2 + "^" + vnfType + "^" + serviceId + "^" + regionalResourceZone
						+ "^" + provStatus + "^" + operationalStatus + "^" + equipmentRole + "^" + orchestrationStatus
						+ "^" + heatStackId + "^" + msoCatalogKey + "^" + managementOption + "^" + ipv4OamAddress + "^"
						+ ipv4Loopback0Address + "^" + nmLanV6Address + "^" + managementV6Address + "^" + vcpu + "^"
						+ vcpuUnits + "^" + vmemory + "^" + vmemoryUnits + "^" + vdisk + "^" + vdiskUnits + "^"
						+ inMaint + "^" + isClosedLoopDisabled + "^" + resourceVersion + "^" + summarystatus + "^"
						+ encryptedaccessflag + "^" + modelInvariantId + "^" + modelVersionId + "^"
						+ modelCustomizationId + "^" + widgetModelId + "^" + widgetModelVersion + "^" + asNumber + "^"
						+ regionalResourceSubzone + "^" + nfType + "^" + nfRole + "^" + nfFunction + "^" + nfNamingCode
						+ "^" + selflink + "^" + ipv4OamGatewayAddress + "^" + ipv4OamGatewayAddressPrefixLength + "^"
						+ vlanIdOuter + "^" + nmProfileName + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		// SeedingManagerController seedingmanagercontroller = new
		// SeedingManagerController();
		// String seedingmanagervnf = seedingmanagercontroller.getVNF(dbadapter,
		// gVnf, datetimestamp, relList);

		return result;
	}

	public boolean processPserver(Pserver pserver, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "pserver";
		String hostname = pserver.getHostname() != null ? pserver.getHostname() : "";
		String ptniiEquipName = pserver.getPtniiEquipName() != null ? pserver.getPtniiEquipName() : "";
		String numberOfCpus = pserver.getNumberOfCpus() != null ? pserver.getNumberOfCpus().toString() : "";
		String diskInGigabytes = pserver.getDiskInGigabytes() != null ? pserver.getDiskInGigabytes().toString() : "";
		String ramInMegabytes = pserver.getRamInMegabytes() != null ? pserver.getRamInMegabytes().toString() : "";
		String equipType = pserver.getEquipType() != null ? pserver.getEquipType() : "";
		String equipVendor = pserver.getEquipVendor() != null ? pserver.getEquipVendor() : "";
		String equipModel = pserver.getEquipModel() != null ? pserver.getEquipModel() : "";
		String fqdn = pserver.getFqdn() != null ? pserver.getFqdn() : "";
		String pserverSelflink = pserver.getPserverSelflink() != null ? pserver.getPserverSelflink() : "";
		String ipv4OamAddress = pserver.getIpv4OamAddress() != null ? pserver.getIpv4OamAddress() : "";
		String serialNumber = pserver.getSerialNumber() != null ? pserver.getSerialNumber() : "";
		String pserverId = pserver.getPserverId() != null ? pserver.getPserverId() : "";
		String ipaddressv4loopback0 = pserver.getIpaddressV4Loopback0() != null ? pserver.getIpaddressV4Loopback0()
				: "";
		String ipaddressv6loopback0 = pserver.getIpaddressV6Loopback0() != null ? pserver.getIpaddressV6Loopback0()
				: "";
		String ipaddressv4aim = pserver.getIpaddressV4Aim() != null ? pserver.getIpaddressV4Aim() : "";
		String ipaddressv6aim = pserver.getIpaddressV6Aim() != null ? pserver.getIpaddressV6Aim() : "";
		String ipaddressv6oam = pserver.getIpaddressV6Oam() != null ? pserver.getIpaddressV6Oam() : "";
		String invstatus = pserver.getInvStatus() != null ? pserver.getInvStatus() : "";
		String internetTopology = pserver.getInternetTopology() != null ? pserver.getInternetTopology() : "";
		String inMaint = pserver.isInMaint() ? "Y" : "N";
		String resourceVersion = pserver.getResourceVersion() != null ? pserver.getResourceVersion() : "";
		String pservername2 = pserver.getPserverName2() != null ? pserver.getPserverName2() : "";
		String purpose = pserver.getPurpose() != null ? pserver.getPurpose() : "";
		String provstatus = pserver.getProvStatus() != null ? pserver.getProvStatus() : "";
		String managementoption = pserver.getManagementOption() != null ? pserver.getManagementOption() : "";
		String hostProfile = pserver.getHostProfile() != null ? pserver.getHostProfile() : "";
		String role = pserver.getRole() != null ? pserver.getRole() : "";
		String function = pserver.getFunction() != null ? pserver.getFunction() : "";
		String opsnote = pserver.getOpsNote() != null ? pserver.getOpsNote() : "";
		
		String relatedFrom = "pserver";
		String fromNodeId = pserver.getHostname();
		RelationshipList relList = pserver.getRelationshipList();

		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
		result = storeEventToDB(hostname + "^" + ptniiEquipName + "^" + numberOfCpus + "^" + diskInGigabytes + "^"
				+ ramInMegabytes + "^" + equipType + "^" + equipVendor + "^" + equipModel + "^" + fqdn + "^"
				+ pserverSelflink + "^" + ipv4OamAddress + "^" + serialNumber + ipaddressv4loopback0 + "^"
				+ ipaddressv6loopback0 + "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam + "^"
				+ invstatus + "^" + "^" + pserverId + "^" + inMaint + "^" + internetTopology + "^" + resourceVersion
				+ "^" + pservername2 + "^" + purpose + "^" + provstatus + "^" + managementoption + "^" + hostProfile
				+ "^" + role + "^" + function + "^" + opsnote + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	// Mady: not use the  following method anymore
	public boolean processPinterfaces(Pserver pserver, String action, String datetimestamp) {
		boolean result = false;
		String hostname = pserver.getHostname();
		PInterfaces pInterfaces = pserver.getPInterfaces();
		if (pInterfaces != null) {
			List<PInterface> lPinterfaces = pInterfaces.getPInterface();
			if (!lPinterfaces.isEmpty()) {
				for (PInterface pInt : lPinterfaces) {

					String entityType = "p-interface";
					String interfacename = pInt.getInterfaceName() != null ? pInt.getInterfaceName() : "";
					String speedvalue = pInt.getSpeedValue() != null ? pInt.getSpeedValue() : "";
					String speedunits = pInt.getSpeedUnits() != null ? pInt.getSpeedUnits().toString() : "";
					String portrescription = pInt.getPortDescription() != null ? pInt.getPortDescription() : "";
					String resourceversion = pInt.getResourceVersion() != null ? pInt.getResourceVersion() : "";
					String equipmentidentifier = pInt.getEquipmentIdentifier() != null ? pInt.getEquipmentIdentifier()
							: "";
					String interfacerole = pInt.getInterfaceRole() != null ? pInt.getInterfaceRole() : "";
					String interfacetype = pInt.getInterfaceType() != null ? pInt.getInterfaceType() : "";
					String provstatus = pInt.getProvStatus() != null ? pInt.getProvStatus() : "";
					String inmaint = pInt.isInMaint() ? "Y" : "N";
					String invstatus = pInt.getInvStatus() != null ? pInt.getInvStatus() : "";
					String selflink = pInt.getSelflink() != null ? pInt.getSelflink() : "";

					String relatedFrom = "p-interface";
					String fromNodeId = pInt.getInterfaceName();
					RelationshipList relList = pInt.getRelationshipList();

					ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList,
							datetimestamp);
					result = storeEventToDB(
							interfacename + "^" + speedvalue + "^" + speedunits + "^" + portrescription + "^"
									+ equipmentidentifier + "^" + interfacerole + "^" + interfacetype + "^" + provstatus
									+ "^" + resourceversion + "^" + inmaint + "^" + invstatus + "^pserver^" + hostname
									+ "^" + selflink + "^" + datetimestamp,
							lRelationship, entityType, action, datetimestamp);

				}
			}
		}

		return result;
	}

	public boolean processPinterfaces(Pserver pserver, PInterface pInterafce, String action, String datetimestamp,
			String pEntityType) {
		boolean result = false;
		String hostname = pserver.getHostname();
		PInterfaces pInterfaces = pserver.getPInterfaces();
		if (pInterfaces != null) {
			List<PInterface> lPinterfaces = pInterfaces.getPInterface();
			if (!lPinterfaces.isEmpty()) {
				for (PInterface pInt : lPinterfaces) {
					if (pInt.equals(pInterafce)) {
						if (pInt.getInterfaceName() == null)
							continue;
						result = storePinterfacesToFile(pInt, action, datetimestamp, pEntityType, hostname);
					}
				}
			}
		}

		return result;
	}

	public boolean processAvailabilityZone(CloudRegion cloudRegion, String action, String datetimestamp) {
		boolean result = false;
		if (cloudRegion != null) {
			// result =
			// syncAvailabilityZones(cloudRegion.getAvailabilityZones(),
			// datetimestamp, action);
		}

		return result;
	}

	public boolean processL3Network(L3Network l3network, String action, String datetimestamp) {
		boolean result = false;

		String networkId = l3network.getNetworkId() != null ? l3network.getNetworkId() : "";
		String networkName = l3network.getNetworkName() != null ? l3network.getNetworkName() : "";
		String networkType = l3network.getNetworkType() != null ? l3network.getNetworkType() : "";
		String networkRole = l3network.getNetworkRole() != null ? l3network.getNetworkRole() : "";
		String networkTechnology = l3network.getNetworkTechnology() != null ? l3network.getNetworkTechnology() : "";
		String neutronNetworkId = l3network.getNeutronNetworkId() != null ? l3network.getNeutronNetworkId() : "";
		String isBoundToVpn = l3network.isIsBoundToVpn() ? "Y" : "N";
		String serviceId = l3network.getServiceId() != null ? l3network.getServiceId() : "";
		String networkRoleInstance = l3network.getNetworkRoleInstance() != null
				? l3network.getNetworkRoleInstance().toString()
				: "";
		String resourceVersion = l3network.getResourceVersion() != null ? l3network.getResourceVersion() : "";
		String orchestrationStatus = l3network.getOrchestrationStatus() != null ? l3network.getOrchestrationStatus()
				: "";
		String heatStackId = l3network.getHeatStackId() != null ? l3network.getHeatStackId() : "";
		String msoCatalogKey = l3network.getMsoCatalogKey() != null ? l3network.getMsoCatalogKey() : "";
		String contrailNetworkFqdn = l3network.getContrailNetworkFqdn() != null ? l3network.getContrailNetworkFqdn()
				: "";
		String modelInvariantId = l3network.getModelInvariantId() != null ? l3network.getModelInvariantId() : "";
		String modelVersionId = l3network.getModelVersionId() != null ? l3network.getModelVersionId() : "";
		String modelCustomizationId = l3network.getModelCustomizationId() != null ? l3network.getModelCustomizationId()
				: "";
		String widgetModelId = l3network.getWidgetModelId() != null ? l3network.getWidgetModelId() : "";
		String widgetModelVersion = l3network.getWidgetModelVersion() != null ? l3network.getWidgetModelVersion() : "";
		String physicalnetworkname = l3network.getPhysicalNetworkName() != null ? l3network.getPhysicalNetworkName()
				: "";
		String isprovidernetwork = l3network.isIsProviderNetwork() ? "Y" : "N";
		String issharednetwork = l3network.isIsSharedNetwork() ? "Y" : "N";
		String isexternalnetwork = l3network.isIsExternalNetwork() ? "Y" : "N";
		String operationalStatus = l3network.getOperationalStatus() != null ? l3network.getOperationalStatus() : "";
		String selflink = l3network.getSelflink() != null ? l3network.getSelflink() : "";

		String entityType = "l3-network";
		result = storeEventToDB(
				networkId + "^" + networkName + "^" + networkType + "^" + networkRole + "^" + networkTechnology + "^"
						+ neutronNetworkId + "^" + isBoundToVpn + "^" + serviceId + "^" + networkRoleInstance + "^"
						+ resourceVersion + "^" + orchestrationStatus + "^" + heatStackId + "^" + msoCatalogKey + "^"
						+ contrailNetworkFqdn + "^" + modelInvariantId + "^" + modelVersionId + "^"
						+ modelCustomizationId + "^" + widgetModelId + "^" + widgetModelVersion + "^"
						+ physicalnetworkname + "^" + isprovidernetwork + "^" + issharednetwork + "^"
						+ isexternalnetwork + "^" + operationalStatus + "^" + selflink + "^" + datetimestamp,
				null, entityType, action, datetimestamp);
		return result;
	}

	public boolean processZone(Zone zone, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "zone";

		String zoneId = zone.getZoneId() != null ? zone.getZoneId() : "";
		String zoneName = zone.getZoneName() != null ? zone.getZoneName() : "";
		String designType = zone.getDesignType() != null ? zone.getDesignType() : "";
		String zoneContext = zone.getZoneContext() != null ? zone.getZoneContext() : "";
		String status = zone.getStatus() != null ? zone.getStatus() : "";
		String inMaint = zone.isInMaint() ? "Y" : "N";
		String resourceVersion = zone.getResourceVersion() != null ? zone.getResourceVersion() : "";

		String relatedFrom = entityType;
		String fromNodeId = zoneName;
		RelationshipList relList = zone.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(zoneId + "^" + zoneName + "^" + designType + "^" + zoneContext + "^" + status + "^"
				+ resourceVersion + "^" + inMaint + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	public boolean processVlanToFile(Vlans vlans, String action, String intName, String intId, String datetimestamp) {
		boolean result = false;

		if (vlans != null) {
			List<Vlan> vlanList = vlans.getVlan();
			for (Vlan vlan : vlanList) {
				String entityType = "vlan";
				String vlaninterface = vlan.getVlanInterface() != null ? vlan.getVlanInterface() : "";
				String vlanidinner = vlan.getVlanIdInner() != null ? vlan.getVlanIdInner().toString() : "";
				String vlanidouter = vlan.getVlanIdOuter() != null ? vlan.getVlanIdOuter().toString() : "";
				String resourceversion = vlan.getResourceVersion() != null ? vlan.getResourceVersion() : "";
				String speedvalue = vlan.getSpeedValue() != null ? vlan.getSpeedValue() : "";
				String speedunits = vlan.getSpeedUnits() != null ? vlan.getSpeedUnits() : "";
				String vlantype = vlan.getVlanType() !=null ? vlan.getVlanType() : "";
				String vlanDesc = vlan.getVlanDescription() != null ? vlan.getVlanDescription() : "";
				String backdoorConnection = vlan.getBackdoorConnection() != null ? vlan.getBackdoorConnection() : "";
				String vpnKey = vlan.getVpnKey() != null ? vlan.getVpnKey() : "";
				String orchestrationStatus = vlan.getOrchestrationStatus() != null ? vlan.getOrchestrationStatus() : "";
				String inMaint = vlan.isInMaint() ? "Y" : "N";
				String provStatus = vlan.getProvStatus() != null ? vlan.getProvStatus() : "";
				String isIpUnnumbered = vlan.isIsIpUnnumbered() ? "Y" : "N";

				result = storeEventToDB(vlaninterface + "^" + vlanidinner + "^" + vlanidouter + "^" + resourceversion
						+ "^" + speedvalue + "^" + speedunits + "^" + vlantype + "^" + vlanDesc + "^" + backdoorConnection + "^" + vpnKey
						+ "^" + orchestrationStatus + "^" + inMaint + "^" + provStatus + "^" + isIpUnnumbered + "^"
						+ intName + "^" + intId + "^" + datetimestamp, null, entityType, action, datetimestamp);
			}
		}
		return result;
	}

	public boolean processVlan(String entity, String action, String topEntityType, String datetimestamp) {
		boolean result = false;
		ObjectMapper mapper = new ObjectMapper();

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		switch (topEntityType) {
		case "pserver":
			Pserver pserver;
			try {
				pserver = mapper.readValue(entity, Pserver.class);
				PInterfaces pInts = pserver.getPInterfaces();
				if (pInts != null) {
					List<PInterface> pIntList = pInts.getPInterface();
					for (PInterface pInt : pIntList) {

						LInterfaces lInterfaces = pInt.getLInterfaces();
						if (lInterfaces != null) {
							List<LInterface> lIntList = lInterfaces.getLInterface();
							for (LInterface lInt : lIntList) {
								String intName = lInt.getInterfaceName();
								// String intId=lInt.getInterfaceId();
								Vlans vlans = lInt.getVlans();
								result = syncVlanAndChildNodes(vlans, "l-interface", intName, "p-interface",
										pInt.getInterfaceName(), action, datetimestamp);
								if (!result) {
									return result;
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "cloud-region":
			CloudRegion cloudRegion;

			try {
				cloudRegion = mapper.readValue(entity, CloudRegion.class);
				if (cloudRegion != null) {
					Tenants tenants = cloudRegion.getTenants();
					if (tenants != null) {
						List<Tenant> lTenants = tenants.getTenant();
						for (Tenant tenant : lTenants) {

							if (tenant != null) {
								Vservers vservers = tenant.getVservers();
								if (vservers != null) {
									List<Vserver> vserverList = vservers.getVserver();
									for (Vserver vserver : vserverList) {
										LInterfaces lInterfaces = vserver.getLInterfaces();
										if (lInterfaces != null) {
											List<LInterface> lIntList = lInterfaces.getLInterface();
											for (LInterface lInt : lIntList) {
												String intName = lInt.getInterfaceName();
												Vlans vlans = lInt.getVlans();
												result = syncVlanAndChildNodes(vlans, "l-interface", intName, "vserver",
														vserver.getVserverId(), action, datetimestamp);
												if (!result) {
													return result;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "generic-vnf":
			GenericVnf vnf;
			try {
				vnf = mapper.readValue(entity, GenericVnf.class);
				if (vnf != null) {
					LInterfaces lInterfaces = vnf.getLInterfaces();
					if (lInterfaces != null) {
						List<LInterface> lIntList = lInterfaces.getLInterface();
						for (LInterface lInt : lIntList) {
							String intName = lInt.getInterfaceName();
							Vlans vlans = lInt.getVlans();
							result = syncVlanAndChildNodes(vlans, "l-interface", intName, "generic-vnf", vnf.getVnfId(),
									action, datetimestamp);
							if (!result) {
								return result;
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}

			break;
		case "vpls-pe":
			break;
		default:
			break;
		}
		return result;
	}

	public boolean processLagInterfaceToFile(LagInterface lagInterface, String action, String datetimestamp,
			String pEntityType, String pEntityId) {
		boolean result = false;
		if (lagInterface != null) {
			if (lagInterface.getInterfaceName() == null)
				return result;

			String interfacename = lagInterface.getInterfaceName() != null ? lagInterface.getInterfaceName() : "";
			String interfacedescription = lagInterface.getInterfaceDescription() != null
					? lagInterface.getInterfaceDescription()
					: "";
			String resourceversion = lagInterface.getResourceVersion() != null ? lagInterface.getResourceVersion() : "";
			String speedvalue = lagInterface.getSpeedValue();
			String speedunits = lagInterface.getSpeedUnits() != null ? lagInterface.getSpeedUnits() : "";
			String interfaceid = lagInterface.getInterfaceId() != null ? lagInterface.getInterfaceId() : "";
			String interfacerole = lagInterface.getInterfaceRole() != null ? lagInterface.getInterfaceRole() : "";
			String provstatus = lagInterface.getProvStatus() != null ? lagInterface.getProvStatus() : "";
			String inmaint = lagInterface.isInMaint() ? "Y" : "N";
			String lacpsystemid = lagInterface.getLacpSystemId() != null ? lagInterface.getLacpSystemId() : "";
			String opsnote = lagInterface.getOpsNote() != null ? lagInterface.getOpsNote() : "";
			String interfacefunction = lagInterface.getInterfaceFunction() != null ? lagInterface.getInterfaceFunction() : "";
			
			String relatedFrom = "lag-interface";
			String fromNodeId = pEntityId + "|" + lagInterface.getInterfaceName();
			RelationshipList relList = lagInterface.getRelationshipList();
			ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					interfacename + "^" + interfacedescription + "^" + resourceversion + "^" + speedvalue + "^"
							+ speedunits + "^" + interfaceid + "^" + interfacerole + "^" + provstatus + "^" + inmaint
							+ "^" + lacpsystemid + "^" + opsnote + "^" + interfacefunction
							+ "^" + pEntityType + "^" + pEntityId + "^" + datetimestamp,
					lRelationship, relatedFrom, action, datetimestamp); 		
			if (!result) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_LAG_INTERFACE_TOFILE_ERROR,
						lagInterface.getInterfaceName());
				return result;
			}

		}
		return result;
	}

	public boolean processLagInterfacesToFile(LagInterfaces lagIinterfaces, String action, String datetimestamp,
			String pEntityType, String pEntityId) {
		boolean result = false;
		if (lagIinterfaces != null) {
			List<LagInterface> lagList = lagIinterfaces.getLagInterface();
			for (LagInterface lag : lagList) {
				result = processLagInterfaceToFile(lag, action, datetimestamp, pEntityType, pEntityId);
			}
		}
		return result;
	}

	public boolean processLagInterfaces(String entity, String action, String topEntityType, String datetimestamp) {
		boolean result = false;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		switch (topEntityType) {
		case "pserver":
			Pserver pserver;
			try {
				pserver = mapper.readValue(entity, Pserver.class);
				LagInterfaces lagInterfaces = pserver.getLagInterfaces();
				result = processLagInterfacesToFile(lagInterfaces, action, datetimestamp, "pserver",
						pserver.getHostname());
				if (!result) {
					return result;
				}

			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;

		case "generic-vnf":
			GenericVnf vnf;
			try {
				vnf = mapper.readValue(entity, GenericVnf.class);
				if (vnf != null) {
					LagInterfaces lagInterfaces = vnf.getLagInterfaces();
					result = processLagInterfacesToFile(lagInterfaces, action, datetimestamp, "generic-vnf",
							vnf.getVnfId());
					if (!result) {
						return result;
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}

			break;
		case "pnf":
			Pnf pnf;
			try {
				pnf = mapper.readValue(entity, Pnf.class);
				LagInterfaces lagInterfaces = pnf.getLagInterfaces();
				result = processLagInterfacesToFile(lagInterfaces, action, datetimestamp, "pnf", pnf.getPnfName());
				if (!result) {
					return result;
				}

			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "vpls-pe":
			break;
		default:
			break;
		}
		return result;
	}

	public boolean processLInterface(String entity, String action, String topEntityType, String datetimestamp) {
		boolean result = false;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		switch (topEntityType) {
		case "pserver":
			Pserver pserver;
			try {
				pserver = mapper.readValue(entity, Pserver.class);
				PInterfaces pInts = pserver.getPInterfaces();
				if (pInts != null) {
					List<PInterface> pIntList = pInts.getPInterface();
					for (PInterface pInt : pIntList) {
						LInterfaces lInterfaces = pInt.getLInterfaces();
						if (lInterfaces != null) {
							result = syncLinterfaceAndChildNodes(lInterfaces, "pserver", pserver.getHostname(), action,
									null, null, null, datetimestamp);
							if (!result) {
								return result;
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "cloud-region":
			CloudRegion cloudRegion;

			try {
				cloudRegion = mapper.readValue(entity, CloudRegion.class);
				if (cloudRegion != null) {
					Tenants tenants = cloudRegion.getTenants();
					if (tenants != null) {
						List<Tenant> lTenants = tenants.getTenant();
						for (Tenant tenant : lTenants) {

							if (tenant != null) {
								Vservers vservers = tenant.getVservers();
								if (vservers != null) {
									List<Vserver> vserverList = vservers.getVserver();
									for (Vserver vserver : vserverList) {
										LInterfaces lInterfaces = vserver.getLInterfaces();
										if (lInterfaces != null) {
											result = syncLinterfaceAndChildNodes(lInterfaces, "vserver",
													vserver.getVserverId(), action, null, null, null, datetimestamp);
											if (!result) {
												return result;
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "generic-vnf":
			GenericVnf vnf;
			try {
				vnf = mapper.readValue(entity, GenericVnf.class);
				if (vnf != null) {
					LInterfaces lInterfaces = vnf.getLInterfaces();
					if (lInterfaces != null) {
						result = syncLinterfaceAndChildNodes(lInterfaces, "generic-vnf", vnf.getVnfId(), action, null,
								null, null, datetimestamp);
						if (!result) {
							return result;
						}
					}
				}
			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}

			break;
		case "pnf":
			Pnf pnf;
			try {
				pnf = mapper.readValue(entity, Pnf.class);
				LInterfaces lInterfaces = pnf.getLInterfaces();
				result = syncLinterfaceAndChildNodes(lInterfaces, "pnf", pnf.getPnfName(), action, null, null, null,
						datetimestamp);
				if (!result) {
					return result;
				}

			} catch (JsonParseException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSON_PARSE_EXCEPTION, e.toString());
			} catch (JsonMappingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_JSONMAPPINGEXCEPTION, e.toString());
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_IOEXCEPTION, e.toString());
			}
			break;
		case "vpls-pe":
			break;
		default:
			break;
		}
		return result;
	}

	// Mady: Not used anymore.
	public boolean processLinterfaceToFile(LInterfaces lInterfaces, String action, String pEntityType, String pEntityId,
			String datetimestamp) {
		boolean result = false;
		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInt : lIntList) {
				String entityType = "l-interface";
				String interfaceName = lInt.getInterfaceName() != null ? lInt.getInterfaceName() : "";
				String interfaceRole = lInt.getInterfaceRole() != null ? lInt.getInterfaceRole() : "";
				String v6WanLinkIp = lInt.getV6WanLinkIp() != null ? lInt.getV6WanLinkIp() : "";
				String selflink = lInt.getSelflink() != null ? lInt.getSelflink() : "";
				String interfaceId = lInt.getInterfaceId() != null ? lInt.getInterfaceId() : "";
				String macaddr = lInt.getMacaddr() != null ? lInt.getMacaddr() : "";
				String networkName = lInt.getNetworkName() != null ? lInt.getNetworkName() : "";
				String resourceVersion = lInt.getResourceVersion() != null ? lInt.getResourceVersion() : "";
				String managementOption = lInt.getManagementOption() != null ? lInt.getManagementOption() : "";
				String interfaceDescription = lInt.getInterfaceDescription() != null ? lInt.getInterfaceDescription()
						: "";
				String isPortMirrored = lInt.isIsPortMirrored() ? "Y" : "N";
				String isInMaint = lInt.isInMaint() ? "Y" : "N";
				String provStatus = lInt.getProvStatus() != null ? lInt.getProvStatus() : "";
				String isipunnumbered = lInt.isIsIpUnnumbered() ? "Y" : "N";
				String allowedAddressPairs = lInt.getAllowedAddressPairs() != null ? lInt.getAllowedAddressPairs() : "";
				String interfaceType = lInt.getInterfaceType() !=null ? lInt.getInterfaceType() : "";
				String adminStatus = lInt.getAdminStatus() !=null ? lInt.getAdminStatus() : "";
				String opsNote = lInt.getOpsNote() !=null ? lInt.getOpsNote() : "";
				String interfaceFunction = lInt.getInterfaceFunction() !=null ? lInt.getInterfaceFunction() : "";
				
				String relatedFrom = "l-interface";
				String fromNodeId = interfaceName;
				RelationshipList relList = lInt.getRelationshipList();
				ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

				result = storeEventToDB(interfaceName + "^" + interfaceRole + "^" + v6WanLinkIp + "^" + selflink + "^"
						+ interfaceId + "^" + macaddr + "^" + networkName + "^" + resourceVersion + "^"
						+ managementOption + "^" + interfaceDescription + "^" + interfaceType + "^" + isPortMirrored + "^" + isInMaint + "^"
						+ provStatus + "^" + isipunnumbered + "^" + pEntityType + "^" + pEntityId + "^"
						+ allowedAddressPairs + "^ " + adminStatus + "^ " + opsNote + "^ " + interfaceFunction + "^ "
						+ datetimestamp, lRelationship, entityType, action, datetimestamp);
			}
		}
		return result;
	}

	public boolean processPhysicalLink(PhysicalLink physicalLink, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "physical-link";
		String linkname = physicalLink.getLinkName() != null ? physicalLink.getLinkName() : "";
		String speedvalue = physicalLink.getSpeedValue() != null ? physicalLink.getSpeedValue() : "";
		String speedunits = physicalLink.getSpeedUnits() != null ? physicalLink.getSpeedUnits() : "";
		String circuitid = physicalLink.getCircuitId() != null ? physicalLink.getCircuitId() : "";
		String dualmode = physicalLink.getDualMode() != null ? physicalLink.getDualMode() : "";
		String resourceversion = physicalLink.getResourceVersion() != null ? physicalLink.getResourceVersion() : "";
		String managementOption = physicalLink.getManagementOption() != null ? physicalLink.getManagementOption() : "";
		String serviceprovidername = physicalLink.getServiceProviderName() != null
				? physicalLink.getServiceProviderName()
				: "";
		String serviceProviderBWUpvalue = physicalLink.getServiceProviderBandwidthUpValue() != null
				? physicalLink.getServiceProviderBandwidthUpValue().toString()
				: "";
		String serviceProviderBWUpUnits = physicalLink.getServiceProviderBandwidthUpUnits() != null
				? physicalLink.getServiceProviderBandwidthUpUnits()
				: "";
		String serviceProviderBWDownvalue = physicalLink.getServiceProviderBandwidthDownValue() != null
				? physicalLink.getServiceProviderBandwidthDownValue().toString()
				: "";
		String serviceProviderBWDownUnits = physicalLink.getServiceProviderBandwidthDownUnits() != null
				? physicalLink.getServiceProviderBandwidthDownUnits()
				: "";

		RelationshipList relList = physicalLink.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, linkname, relList, datetimestamp);

		result = storeEventToDB(
				linkname + "^" + speedvalue + "^" + speedunits + "^" + circuitid + "^" + dualmode + "^"
						+ resourceversion + "^" + managementOption + "^" + serviceprovidername + "^"
						+ serviceProviderBWUpvalue + "^" + serviceProviderBWUpUnits + "^" + serviceProviderBWDownvalue
						+ "^" + serviceProviderBWDownUnits + "^" + datetimestamp,
						lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processZoneSubnets(Zone zone, String action, String datetimestamp) {
		boolean result = false;
		String zoneId = zone.getZoneId();
		Subnets subs = zone.getSubnets();
		if (subs != null) {
			List<Subnet> lsub = subs.getSubnet();
			for (Subnet subnet : lsub) {
				processSubnet(subnet, Constants.ZONE_ENTITY, zoneId, action, datetimestamp);
			}
		}
		return result;
	}

	public boolean processL3Subnet(L3Network l3network, String action, String datetimestamp) {
		boolean result = false;
		String networkId = l3network.getNetworkId();
		Subnets subs = l3network.getSubnets();
		if (subs != null) {
			List<Subnet> lsub = subs.getSubnet();
			for (Subnet subnet : lsub) {
				processSubnet(subnet, Constants.L3_NETWORK_ENTITY, networkId, action, datetimestamp);
			}
		}
		return result;
	}

	public boolean processSubnet(Subnet subnet, String parentEntityType, String parentEntityId, String action,
			String datetimestamp) {
		boolean result = false;

		String entityType = "subnet";

		String subnetId = subnet.getSubnetId() != null ? subnet.getSubnetId() : "";
		String neutronSubnetId = subnet.getNeutronSubnetId() != null ? subnet.getNeutronSubnetId() : "";
		String gatewayAddress = subnet.getGatewayAddress() != null ? subnet.getGatewayAddress() : "";
		String networkStartAddress = subnet.getNetworkStartAddress() != null ? subnet.getNetworkStartAddress() : "";
		String cidrMask = subnet.getCidrMask() != null ? subnet.getCidrMask() : "";
		String ipVersion = subnet.getIpVersion() != null ? subnet.getIpVersion() : "";
		String orchestrationStatus = subnet.getOrchestrationStatus() != null ? subnet.getOrchestrationStatus() : "";
		String dhcpEnabled = subnet.isDhcpEnabled() ? "Y" : "N";
		String dhcpStart = subnet.getDhcpStart() != null ? subnet.getDhcpStart() : "";
		String dhcpEnd = subnet.getDhcpEnd() != null ? subnet.getDhcpEnd() : "";
		String resourceVersion = subnet.getResourceVersion() != null ? subnet.getResourceVersion() : "";
		String subnetRole = subnet.getSubnetRole() != null ? subnet.getSubnetRole() : "";
		String ipAssignmentDirection = subnet.getIpAssignmentDirection() != null ? subnet.getIpAssignmentDirection()
				: "";
		String subnetSequence = subnet.getSubnetSequence() != null ? subnet.getSubnetSequence().toString() : "";
		String subnetName = subnet.getSubnetName() != null ? subnet.getSubnetName() : "";
		String description = subnet.getDescription() != null ? subnet.getDescription() : "";
		String subnetModel = subnet.getSubnetModel() != null ? subnet.getSubnetModel() : "";
		String opsNote = subnet.getOpsNote() != null ? subnet.getOpsNote() : "";

		String relatedFrom = entityType;
		String fromNodeId = parentEntityId + "|" + subnetId;
		RelationshipList relList = subnet.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			result = storeEventToDB(subnetId + "^" + subnetName + "^" + neutronSubnetId + "^" + gatewayAddress + "^"
					+ networkStartAddress + "^" + cidrMask + "^" + ipVersion + "^" + orchestrationStatus + "^"
					+ description + "^" + dhcpEnabled + "^" + dhcpStart + "^" + dhcpEnd + "^" + resourceVersion + "^"
					+ parentEntityType + "^" + parentEntityId + "^" + subnetRole + "^" + ipAssignmentDirection + "^"
					+ subnetSequence + "^" + subnetModel + "^" + opsNote + "^" + datetimestamp, lRelationship,
					entityType, action, datetimestamp);
		} else {
			result = storeEventToDB(
					subnetId + "^" + neutronSubnetId + "^" + gatewayAddress + "^" + networkStartAddress + "^" + cidrMask
							+ "^" + ipVersion + "^" + orchestrationStatus + "^" + dhcpEnabled + "^" + dhcpStart + "^"
							+ dhcpEnd + "^" + resourceVersion + "^" + parentEntityId + "^" + subnetRole + "^"
							+ ipAssignmentDirection + "^" + subnetSequence + "^" + datetimestamp,
					lRelationship, entityType, action, datetimestamp);
		}

		return result;
	}

	public boolean processLogicalLink(LogicalLink logicalLink, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "logical-link";
		String linkname = logicalLink.getLinkName() != null ? logicalLink.getLinkName() : "";
		String linktype = logicalLink.getLinkType() != null ? logicalLink.getLinkType() : "";
		String speedvalue = logicalLink.getSpeedValue() != null ? logicalLink.getSpeedValue() : "";
		String speedunits = logicalLink.getSpeedUnits() != null ? logicalLink.getSpeedUnits() : "";
		String ipversion = logicalLink.getIpVersion() != null ? logicalLink.getIpVersion() : "";
		String routingprotocol = logicalLink.getRoutingProtocol() != null ? logicalLink.getRoutingProtocol() : "";
		String resourceversion = logicalLink.getResourceVersion() != null ? logicalLink.getResourceVersion() : "";
		String modelInvariantId = logicalLink.getModelInvariantId() != null ? logicalLink.getModelInvariantId() : "";
		String modelVersionId = logicalLink.getModelVersionId() != null ? logicalLink.getModelVersionId() : "";
		String widgetModelId = logicalLink.getWidgetModelId() != null ? logicalLink.getWidgetModelId() : "";
		String widgetModelVersion = logicalLink.getWidgetModelVersion() != null ? logicalLink.getWidgetModelVersion()
				: "";
		String operationalStatus = logicalLink.getOperationalStatus() != null ? logicalLink.getOperationalStatus() : "";
		String provStatus = logicalLink.getProvStatus() != null ? logicalLink.getProvStatus() : "";
		String linkRole = logicalLink.getLinkRole() != null ? logicalLink.getLinkRole() : "";
		String linkName2 = logicalLink.getLinkName2() != null ? logicalLink.getLinkRole() : "";
		String linkId = logicalLink.getLinkId() != null ? logicalLink.getLinkId() : "";
		String circuitId = logicalLink.getCircuitId() != null ? logicalLink.getCircuitId() : "";
		String purpose = logicalLink.getPurpose() != null ? logicalLink.getPurpose() : "";
		String inmaint = logicalLink.isInMaint() ? "Y" : "N";

		String relatedFrom = "logical-link";
		String fromNodeId = logicalLink.getLinkName();
		RelationshipList relList = logicalLink.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(linkname + "^" + inmaint + "^" + linktype + "^" + speedvalue + "^" + speedunits + "^"
				+ ipversion + "^" + routingprotocol + "^" + resourceversion + "^" + modelInvariantId + "^"
				+ modelVersionId + "^" + widgetModelId + "^" + widgetModelVersion + "^" + operationalStatus + "^"
				+ provStatus + "^" + linkRole + "^" + linkName2 + "^" + linkId + "^" + circuitId + "^" + purpose + "^"
				+ datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processImage(Image image, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "image";

		String imageid = image.getImageId() != null ? image.getImageId() : "";
		String imagename = image.getImageName() != null ? image.getImageName() : "";
		String imagearchitecture = image.getImageArchitecture() != null ? image.getImageArchitecture() : "";
		String imageosdistro = image.getImageOsDistro() != null ? image.getImageOsDistro() : "";
		String imageosversion = image.getImageOsVersion() != null ? image.getImageOsVersion() : "";
		String application = image.getApplication() != null ? image.getApplication() : "";
		String applicationvendor = image.getApplicationVendor() != null ? image.getApplicationVendor() : "";
		String applicationversion = image.getApplicationVersion() != null ? image.getApplicationVersion() : "";
		String imageselflink = image.getImageSelflink() != null ? image.getImageSelflink() : "";
		String resourceversion = image.getResourceVersion() != null ? image.getResourceVersion() : "";

		result = storeEventToDB(imageid + "^" + imagename + "^" + imagearchitecture + "^" + imageosdistro + "^"
				+ imageosversion + "^" + application + "^" + applicationvendor + "^" + applicationversion + "^"
				+ imageselflink + "^" + resourceversion + "^" + datetimestamp, null, entityType, action, datetimestamp);

		return result;
	}

	public boolean processNetworkProfile(NetworkProfile networkProfile, String action, String datetimestamp) {
		boolean result = false;
		String entityType = "network-profile";

		String nmprofilename = networkProfile.getNmProfileName() != null ? networkProfile.getNmProfileName() : "";
		String communitystring = networkProfile.getCommunityString() != null ? networkProfile.getCommunityString() : "";
		String resourceversion = networkProfile.getResourceVersion() != null ? networkProfile.getResourceVersion() : "";
		result = storeEventToDB(nmprofilename + "^" + communitystring + "^" + resourceversion + "^" + datetimestamp,
				null, entityType, action, datetimestamp);

		return result;
	}

	public ArrayList<String> processrelationships(String relatedFrom, String fromNodeId, RelationshipList relationship,
			String datetimestamp) {
		ArrayList<String> relationshipList = new ArrayList<String>();
		if (relationship == null)
			return relationshipList;
		List<Relationship> relList = relationship.getRelationship();
		for (Relationship rel : relList) {
			String relatedTo = rel.getRelatedTo();
			String relatedLink = rel.getRelatedLink() != null ? rel.getRelatedLink() : "";
			String relationshipLabel = rel.getRelationshipLabel() != null ? rel.getRelationshipLabel() : "";
			String toNodeId = "";
			String toNodeIdCombined = "";
			List<RelationshipData> relData = rel.getRelationshipData();
			for (RelationshipData data : relData) {
				toNodeIdCombined += data.getRelationshipValue() + "|";
			}
			if (toNodeIdCombined.endsWith("|")) {
				toNodeId = toNodeIdCombined.substring(0, toNodeIdCombined.length() - 1);
			}
			if (fromNodeId != null && fromNodeId.length() != 0 && toNodeId != null && toNodeId.length() != 0
					&& relatedFrom != null && relatedFrom.length() != 0 && relatedTo != null
					&& relatedTo.length() != 0) {
				relationshipList.add(fromNodeId + "^" + toNodeId + "^" + relatedFrom + "^" + relatedTo + "^"
						+ relatedLink + "^" + relationshipLabel + "^" + datetimestamp);
			}
		}
		return relationshipList;
	}

	public boolean processComplex(Complex complexObj, String action, String datetimestamp) {
		boolean result = true;

		String entityType = Constants.COMPLEX_ENTITY;

		String physicalLocationId = Optional.ofNullable(complexObj.getPhysicalLocationId()).orElse("");
		String dataCenterCode = Optional.ofNullable(complexObj.getDataCenterCode()).orElse("");
		String complexName = Optional.ofNullable(complexObj.getComplexName()).orElse("");
		String identityurl = Optional.ofNullable(complexObj.getIdentityUrl()).orElse("");
		String resourceVersion = Optional.ofNullable(complexObj.getResourceVersion()).orElse("");
		String physicalLocationType = Optional.ofNullable(complexObj.getPhysicalLocationType()).orElse("");
		String street1 = Optional.ofNullable(complexObj.getStreet1()).orElse("");
		String street2 = Optional.ofNullable(complexObj.getStreet2()).orElse("");
		String city = Optional.ofNullable(complexObj.getCity()).orElse("");
		String state = Optional.ofNullable(complexObj.getState()).orElse("");
		String postalCode = Optional.ofNullable(complexObj.getPostalCode()).orElse("");
		String country = Optional.ofNullable(complexObj.getCountry()).orElse("");
		String region = Optional.ofNullable(complexObj.getRegion()).orElse("");
		String latitude = Optional.ofNullable(complexObj.getLatitude()).orElse("");
		String longitude = Optional.ofNullable(complexObj.getLongitude()).orElse("");
		String elevation = Optional.ofNullable(complexObj.getElevation()).orElse("");
		String lata = Optional.ofNullable(complexObj.getLata()).orElse("");

		String fromNodeId = physicalLocationId;
		RelationshipList relList = complexObj.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				physicalLocationId + "^" + dataCenterCode + "^" + complexName + "^" + identityurl + "^"
						+ resourceVersion + "^" + physicalLocationType + "^" + street1 + "^" + street2 + "^" + city
						+ "^" + state + "^" + postalCode + "^" + country + "^" + region + "^" + latitude + "^"
						+ longitude + "^" + elevation + "^" + lata + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processCloudRegion(CloudRegion cloudregion, String action, String datetimestamp) {
		boolean result = true;

		String entityType = Constants.CLOUD_REGION_ENTITY;
		String cloudowner = Optional.ofNullable(cloudregion.getCloudOwner()).orElse("");
		String cloudregionid = Optional.ofNullable(cloudregion.getCloudRegionId()).orElse("");
		String cloudtype = Optional.ofNullable(cloudregion.getCloudType()).orElse("");
		String ownerdefinedtype = Optional.ofNullable(cloudregion.getOwnerDefinedType()).orElse("");
		String cloudregionversion = Optional.ofNullable(cloudregion.getCloudRegionVersion()).orElse("");
		String identityurl = Optional.ofNullable(cloudregion.getIdentityUrl()).orElse("");
		String cloudzone = Optional.ofNullable(cloudregion.getCloudZone()).orElse("");
		String complexname = Optional.ofNullable(cloudregion.getComplexName()).orElse("");
		String sriovautomation = Optional.ofNullable(cloudregion.getSriovAutomation()).orElse("");
		String resourceversion = Optional.ofNullable(cloudregion.getResourceVersion()).orElse("");
		String upgradecycle = Optional.ofNullable(cloudregion.getUpgradeCycle()).orElse("");
		String orchestrationdisabled = cloudregion.isOrchestrationDisabled() ? "Y" : "N";
		String inmaint = cloudregion.isInMaint() ? "Y" : "N";
		String cloudrole = Optional.ofNullable(cloudregion.getCloudRole()).orElse("");
		String cloudfunction = Optional.ofNullable(cloudregion.getCloudFunction()).orElse("");
		String status = Optional.ofNullable(cloudregion.getStatus()).orElse("");
		
		String fromNodeId = cloudowner + "|" + cloudregion.getCloudRegionId();
		RelationshipList relList = cloudregion.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(cloudowner + "^" + cloudregionid + "^" + cloudtype + "^" + ownerdefinedtype + "^"
				+ cloudregionversion + "^" + identityurl + "^" + cloudzone + "^" + complexname + "^" + sriovautomation
				+ "^" + resourceversion + "^" + upgradecycle + "^" + orchestrationdisabled + "^" + inmaint + "^"
				+ cloudrole + "^" + cloudfunction + "^" + status + "^"
				+ datetimestamp, lRelationship, entityType, action, datetimestamp); 
		return result;
	}

	public String storeVserverEventToFile(Vserver vserver, String action, String tenantId, String cloudOwner,
			String cloudRegionId, String datetimestamp) {

		String vserverId = vserver.getVserverId() != null ? vserver.getVserverId() : "";
		String vserverName = vserver.getVserverName() != null ? vserver.getVserverName() : "";
		String vserverName2 = vserver.getVserverName2() != null ? vserver.getVserverName2() : "";
		String provStatus = vserver.getProvStatus() != null ? vserver.getProvStatus() : "";
		String selfLink = vserver.getVserverSelflink() != null ? vserver.getVserverSelflink() : "";
		String isInMaint = vserver.isInMaint() ? "Y" : "N";
		String isClosedLoopDisabled = vserver.isIsClosedLoopDisabled() ? "Y" : "N";
		String resourceversion = vserver.getResourceVersion();

		String data = vserverId + "^" + vserverName + "^" + vserverName2 + "^" + provStatus + "^" + selfLink + "^"
				+ isInMaint + "^" + isClosedLoopDisabled + "^" + resourceversion + "^" + tenantId + "^" + cloudOwner
				+ "^" + cloudRegionId + "^" + datetimestamp;
		return data;
	}

	public boolean processVserver(Tenant tenant, Vserver vser, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean result = true;

		Vservers vservers = tenant.getVservers();
		List<Vserver> vserverList = vservers.getVserver();
		for (Vserver vserver : vserverList) {
			if (vser.equals(vserver)) {
				String entityType = "vserver";
				String data = storeVserverEventToFile(vserver, action, tenant.getTenantId(), cloudOwner, cloudRegionId,
						datetimestamp);

				String relatedFrom = "vserver";
				String fromNodeId = vserver.getVserverId();
				RelationshipList relList = vserver.getRelationshipList();
				ArrayList<String> lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);
				result = storeEventToDB(data, lRelationship, entityType, action, datetimestamp);

			}
		}

		return result;
	}

	public String storeNosServerEventToFile(NosServer nosserver, String action, String tenantId, String cloudOwner,
			String cloudRegionId, String datetimestamp) {

		String nosServerId = nosserver.getNosServerId() != null ? nosserver.getNosServerId() : "";
		String nosserverName = nosserver.getNosServerName() != null ? nosserver.getNosServerName() : "";
		String vendor = nosserver.getVendor() != null ? nosserver.getVendor() : "";
		String provStatus = nosserver.getProvStatus() != null ? nosserver.getProvStatus() : "";
		String nosserverselfLink = nosserver.getNosServerSelflink() != null ? nosserver.getNosServerSelflink() : "";
		String isInMaint = nosserver.isInMaint() ? "Y" : "N";
		String resourceversion = nosserver.getResourceVersion();

		String data = cloudOwner + "^" + cloudRegionId + "^" + tenantId + "^" + nosServerId + "^" + nosserverName + "^"
				+ vendor + "^" + provStatus + "^" + nosserverselfLink + "^" + isInMaint + "^" + resourceversion + "^"
				+ datetimestamp;
		return data;
	}

	public boolean processLineOfBusiness(LineOfBusiness lineofbusiness, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "line-of-business";
		String lineofbusinessname = lineofbusiness.getLineOfBusinessName() != null
				? lineofbusiness.getLineOfBusinessName()
				: "";
		String resourceversion = lineofbusiness.getResourceVersion() != null ? lineofbusiness.getResourceVersion() : "";

		String fromNodeId = lineofbusinessname;
		RelationshipList relList = lineofbusiness.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(lineofbusinessname + "^" + resourceversion + "^" + datetimestamp, lRelationship,
				entityType, action, datetimestamp);
		return result;
	}

	public boolean processOwningEntity(OwningEntity owningentity, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "owning-entity";
		String owningentityid = owningentity.getOwningEntityId() != null ? owningentity.getOwningEntityId() : "";
		String owningentityname = owningentity.getOwningEntityName() != null ? owningentity.getOwningEntityName() : "";
		String resourceversion = owningentity.getResourceVersion() != null ? owningentity.getResourceVersion() : "";

		String fromNodeId = owningentityid;
		RelationshipList relList = owningentity.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(owningentityid + "^" + owningentityname + "^" + resourceversion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processPlatform(Platform Platform, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "platform";
		String platformname = Platform.getPlatformName() != null ? Platform.getPlatformName() : "";
		String resourceversion = Platform.getResourceVersion() != null ? Platform.getResourceVersion() : "";

		String fromNodeId = platformname;
		RelationshipList relList = Platform.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(platformname + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType,
				action, datetimestamp);
		return result;
	}

	public boolean processProject(Project project, String action, String datetimestamp) {
		boolean result = false;

		String entityType = "project";
		String projectname = project.getProjectName() != null ? project.getProjectName() : "";
		String resourceversion = project.getResourceVersion() != null ? project.getResourceVersion() : "";

		String fromNodeId = projectname;
		RelationshipList relList = project.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(projectname + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType,
				action, datetimestamp);
		return result;
	}

	public boolean processPnfAndChildNodes(String pnfData, String action, String datetimestamp) {
		boolean result = false;

		Pnf pnf;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
		try {
			pnf = mapper.readValue(pnfData, Pnf.class);

			String entityType = "pnf";
			if (pnf != null) {
				String pnfName = pnf.getPnfName();
				result = processPnf(pnf, action, datetimestamp);
				if (result == false) {
					ecompLogger.debug("Loading pnf data and child objects for pnf: " + pnfName + " Failed");
					return result;
				}
				if (pnf.getPInterfaces() != null) {
					result = syncPInterfaceAndChildNodes(pnf, action, datetimestamp, entityType);
					if (result == false) {
						ecompLogger.debug("Loading p-interfaces and child for pnf: " + pnfName + " Failed");
					}
				}
				if (pnf.getLInterfaces() != null) {
					result = processLInterface(pnfData, action, entityType, datetimestamp);
					if (result == false) {
						ecompLogger.debug("Loading l-interfaces and child for pnf: " + pnfName + " Failed");
					}
				}
				if (pnf.getLagInterfaces() != null) {
					result = processLagInterfaces(pnfData, action, entityType, datetimestamp);
					if (result == false) {
						ecompLogger.debug("Loading lag-interfaces and child for pnf: " + pnfName + " Failed");
					}
				}
			}
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

		return result;
	}

	public boolean initNaradPnfs(String url, String datetimestamp)
			throws JsonParseException, JsonMappingException, IOException {
		boolean result = false;

		eventType = com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT;
		String action = "UPDATE";

		try {
			String pnfUrl = url + "/nodes/pnfs?nodes-only=true";
			restClient = new RESTClient();
			String responsePnfs = restClient.retrieveAAIObject(pnfUrl);
			JSONObject pnfJSONObject = null;
			pnfJSONObject = new JSONObject(responsePnfs);
			JSONArray pnfsArray = pnfJSONObject.optJSONArray("pnf");
			for (int i = 0; pnfsArray != null && i < pnfsArray.length(); i++) {
				JSONObject pnfObj = pnfsArray.optJSONObject(i);
				String pnfName = pnfObj.getString("pnf-name");
				String pnfSelfUrl = url + "/network/pnfs/pnf/" + pnfName + "?depth=all";
				String pnfData = restClient.retrieveAAIObject(pnfSelfUrl);
				if (pnfData != null) {
					result = processPnfAndChildNodes(pnfData, action, datetimestamp);
				}
			}
		} catch (DTIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public boolean storeEventToDB(String data, ArrayList<String> lRelationship, String entityType, String action,
			String datetimestamp) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_INSIDE_BLOCK_DEBUG, "storeEventToDB");

		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_ENTER_STORE_EVENT_DB_DEBUG, data,
				entityType + eventType);
		if (eventType == null) {
			System.out.println("event-type is not set.");
			ecompLogger.debug("Event Type is missing.");
			return false;
		}
		int retVal = SUCCESS_UPDATE;
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			switch (entityType) {
			case Constants.ALLOTTED_RESOURCE_ENTITY:
				retVal = dbadapter.processAllottedResource(data, action, entityType, datetimestamp);
				break;
			case Constants.AVAILABILITY_ZONE_ENTITY:
				retVal = dbadapter.processAvailabilityZone(data, action, entityType, eventType);
				break;
			case Constants.CLOUD_REGION_ENTITY:
				retVal = dbadapter.processCloudRegion(data, action, entityType, eventType);
				break;
			case Constants.COMPLEX_ENTITY:
				retVal = dbadapter.processComplex(data, action, entityType, eventType);
				break;
			case Constants.CP_ENTITY:
				retVal = dbadapter.processCp(data, action, entityType, eventType);
				break;
			case Constants.CUSTOMER_ENTITY:
				retVal = dbadapter.processCustomer(data, action, entityType, eventType);
				break;
			case Constants.CLOUD_VIP_IPV4_ADDRESS_LIST_ENTITY:
				retVal = dbadapter.processCloudVIPIPV4AddressList(data, action, entityType, eventType);
				break;
			case Constants.CLOUD_VIP_IPV6_ADDRESS_LIST_ENTITY:
				retVal = dbadapter.processCloudVIPIPV6AddressList(data, action, entityType, eventType);
				break;
			case Constants.DVS_SWITCH_ENTITY:
				retVal = dbadapter.processDvsSwitch(data, action, entityType, eventType);
				break;
			case Constants.FLAVOR_ENTITY:
				retVal = dbadapter.processFlavor(data, action, entityType, eventType);
				break;
			case Constants.FORWARDER_ENTITY:
				retVal = dbadapter.processForwarder(data, action, entityType, eventType);
				break;
			case Constants.FORWARDING_PATH_ENTITY:
				retVal = dbadapter.processForwardingPath(data, action, entityType, eventType);
				break;
			case Constants.GENERIC_VNF_ENTITY:
				retVal = dbadapter.processGenericVnf(data, action, entityType, eventType);
				break;
			case Constants.IMAGE_ENTITY:
				retVal = dbadapter.processImage(data, action, entityType, eventType);
				break;
			case Constants.L_INTERFACE_ENTITY:
				retVal = dbadapter.processLinterafce(data, action, entityType, eventType);
				break;
			case Constants.L3_NETWORK_ENTITY:
				retVal = dbadapter.processL3Network(data, action, entityType, eventType);
				break;
			case Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY:
				retVal = dbadapter.processL3InterfaceIpv4AddressList(data, action, entityType, eventType);
				break;
			case Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY:
				retVal = dbadapter.processL3InterfaceIpv6AddressList(data, action, entityType, eventType);
				break;
			case Constants.LAG_INTERFACE_ENTITY:
				retVal = dbadapter.processLagInterface(data, action, entityType, eventType);
				break;
			case Constants.LAG_LINK_ENTITY:
				retVal = dbadapter.processLagLink(data, action, entityType, eventType);
				break;
			case Constants.LINE_OF_BUSINESS_ENTITY:
				retVal = dbadapter.processLineofBusiness(data, action, entityType, eventType);
				break;
			case Constants.LOGICAL_LINK_ENTITY:
				retVal = dbadapter.processLogicalLink(data, action, entityType, eventType);
				break;
			case Constants.MODEL_ENTITY:
				retVal = dbadapter.processModel(data, action, entityType, eventType);
				break;
			case Constants.MODEL_VER_ENTITY:
				retVal = dbadapter.processModelVer(data, action, entityType, eventType);
				break;
			case Constants.NETWORK_PROFILE_ENTITY:
				retVal = dbadapter.processNetworkProfile(data, action, entityType, eventType);
				break;
			case Constants.NOS_SERVER_ENTITY:
				retVal = dbadapter.processNosSserver(data, action, entityType, eventType);
				break;
			case Constants.OAM_NETWORK_ENTITY:
				retVal = dbadapter.processOamNetwork(data, action, entityType, eventType);
				break;
			case Constants.OPERATIONAL_ENVIRONMENT_ENTITY:
				retVal = dbadapter.processOperationalEnvironment(data, action, entityType, eventType);
				break;
			case Constants.OWNING_ENTITY_ENTITY:
				retVal = dbadapter.processOwningEntity(data, action, entityType, eventType);
				break;
			case Constants.P_INTERFACE_ENTITY:
				retVal = dbadapter.processPinterface(data, action, entityType, eventType);
				break;
			case Constants.PHYSICAL_LINK_ENTITY:
				retVal = dbadapter.processPhysicalLink(data, action, entityType, eventType);
				break;
			case Constants.PLATFORM_ENTITY:
				retVal = dbadapter.processPlatform(data, action, entityType, eventType);
				break;
			case Constants.PNF_ENTITY:
				retVal = dbadapter.processPNF(data, action, entityType, eventType);
				break;
			case Constants.PORT_GROUP_ENTITY:
				retVal = dbadapter.processPortGroup(data, action, entityType, eventType);
				break;
			case Constants.PROJECT_ENTITY:
				retVal = dbadapter.processProject(data, action, entityType, eventType);
				break;
			case Constants.PSERVER_ENTITY:
				retVal = dbadapter.processPserver(data, action, entityType, eventType);
				break;
			case Constants.SERVICE_ENTITY:
				retVal = dbadapter.processService(data, action, entityType, eventType);
				break;
			case Constants.SERVICE_CAPABILITY_ENTITY:
				retVal = dbadapter.processServiceCapability(data, action, entityType, eventType);
				break;
			case Constants.SERVICE_INSTANCE_ENTITY:
				retVal = dbadapter.processServiceInstance(data, action, entityType, eventType);
				break;
			case Constants.SERVICE_SUBSCRIPTION_ENTITY:
				retVal = dbadapter.processServiceSubscription(data, action, entityType, eventType);
				break;
			case Constants.SRIOV_PF_ENTITY:
				retVal = dbadapter.processSriovPf(data, action, entityType, eventType);
				break;
			case Constants.SRIOV_VF_ENTITY:
				retVal = dbadapter.processSriovVf(data, action, entityType, eventType);
				break;
			case Constants.SUBNET_ENTITY:
				retVal = dbadapter.processSubnet(data, action, entityType, eventType);
				break;
			case Constants.TENANT_ENTITY:
				retVal = dbadapter.processTenant(data, action, entityType, eventType);
				break;
			case Constants.VCE_ENTITY:
				retVal = dbadapter.processVce(data, action, entityType, eventType);
				break;
			case Constants.VF_MODULE_ENTITY:
				retVal = dbadapter.processVfModule(data, action, entityType, eventType);
				break;
			case Constants.VIRTUAL_DATA_CENTER_ENTITY:
				retVal = dbadapter.processVirtualDataCenter(data, action, entityType, eventType);
				break;
			case Constants.VLAN_ENTITY:
				retVal = dbadapter.processVlan(data, action, entityType, eventType);
				break;
			case Constants.VNF_IMAGE_ENTITY:
				retVal = dbadapter.processVnfImage(data, action, entityType, eventType);
				break;
			case Constants.VNFC_ENTITY:
				retVal = dbadapter.processVNFC(data, action, entityType, eventType);
				break;
			case Constants.VPLS_PE_ENTITY:
				retVal = dbadapter.processVPLSPE(data, action, entityType, eventType);
				break;
			case Constants.VSERVER_ENTITY:
				retVal = dbadapter.processVserver(data, action, entityType, eventType);
				break;
			case Constants.ZONE_ENTITY:
				retVal = dbadapter.processZone(data, action, entityType, eventType);
				break;
			case "volume":
				retVal = dbadapter.processVolume(data, action, entityType, eventType);
				break;
			case "vpe":
				retVal = dbadapter.processVpe(data, action, entityType, eventType);
				break;
			case "newvce":
				retVal = dbadapter.processNewVce(data, action, entityType, eventType);
				break;
			case "metadatum":
				retVal = dbadapter.processMetadatum(data, action, entityType, eventType);
				break;
			default:
				break;
			}

			if (retVal == NO_UPDATE || retVal == SUCCESS_UPDATE) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_STATUS_UPDATE_DEBUG, data,
						entityType);
				if (lRelationship != null && lRelationship.size() > 0) {
					retVal = dbadapter.processRelationshipList(lRelationship, action, eventType);
				}
			} else {
				System.out.println("ERROR while DB Update");
			}
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			switch (entityType) {
			case "card":
				retVal = dbadapter.processNaradCard(data, action, entityType, eventType);
				break;
			case "card-slot":
				retVal = dbadapter.processNaradCardSlot(data, action, entityType, eventType);
				break;
			case "cable":
				retVal = dbadapter.processNaradCable(data, action, entityType, eventType);
				break;
			case "chassis":
				retVal = dbadapter.processNaradChassis(data, action, entityType, eventType);
				break;
			case "cloud-region":
				retVal = dbadapter.processCloudRegion(data, action, entityType, eventType);
				break;
			case "complex":
				retVal = dbadapter.processComplex(data, action, entityType, eventType);
				break;
			case "instance-group":
				retVal = dbadapter.processNaradInstanceGroup(data, action, entityType, eventType);
				break;
			case "l3-interface-ipv4-address-list":
				retVal = dbadapter.processL3InterfaceIpv4AddressList(data, action, entityType, eventType);
				break;
			case "l3-interface-ipv6-address-list":
				retVal = dbadapter.processL3InterfaceIpv6AddressList(data, action, entityType, eventType);
				break;
			case "l3-network":
				retVal = dbadapter.processL3Network(data, action, entityType, eventType);
				break;
			case "lag-interface":
				retVal = dbadapter.processLagInterface(data, action, entityType, eventType);
				break;
			case "l-interface":
				retVal = dbadapter.processLinterafce(data, action, entityType, eventType);
				break;
			case "logical-link":
				retVal = dbadapter.processLogicalLink(data, action, entityType, eventType);
				break;
			case "physical-link":
				retVal = dbadapter.processPhysicalLink(data, action, entityType, eventType);
				break;
			case "p-interface":
				retVal = dbadapter.processPinterface(data, action, entityType, eventType);
				break;
			case "pluggable":
				retVal = dbadapter.processNaradPluggable(data, action, entityType, eventType);
				break;
			case "pluggable-slot":
				retVal = dbadapter.processNaradPluggableSlot(data, action, entityType, eventType);
				break;
			case "pnf":
				retVal = dbadapter.processPNF(data, action, entityType, eventType);
				break;
			case "port":
				retVal = dbadapter.processNaradPort(data, action, entityType, eventType);
				break;
			case "pserver":
				retVal = dbadapter.processPserver(data, action, entityType, eventType);
				break;
			case "rack":
				retVal = dbadapter.processNaradRack(data, action, entityType, eventType);
				break;
			case "subnet":
				retVal = dbadapter.processSubnet(data, action, entityType, eventType);
				break;
			case "vlan":
				retVal = dbadapter.processVlan(data, action, entityType, eventType);
				break;
			case "zone":
				retVal = dbadapter.processZone(data, action, entityType, eventType);
				break;
			default:
				break;
			}
			if (retVal == NO_UPDATE || retVal == SUCCESS_UPDATE) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_STATUS_UPDATE_DEBUG, data,
						entityType);
				if (lRelationship != null && lRelationship.size() > 0) {
					retVal = dbadapter.processRelationshipList(lRelationship, action, eventType);
				}
			} else {
				System.out.println("ERROR while DB Update");
			}

		}

		return true;
	}

}
