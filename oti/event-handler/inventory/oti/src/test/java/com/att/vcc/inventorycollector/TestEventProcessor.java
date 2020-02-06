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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.DBAdapter;
import com.att.vcc.inventorycollector.EventProcessor;
import com.att.vcc.inventorycollector.EventUtil;
import com.att.vcc.inventorycollector.schema.Chassis;
import com.att.vcc.inventorycollector.schema.Complex;
import com.att.vcc.inventorycollector.schema.InstanceGroup;
import com.att.vcc.inventorycollector.schema.LInterface;
import com.att.vcc.inventorycollector.schema.LInterfaces;
import com.att.vcc.inventorycollector.schema.LagInterface;
import com.att.vcc.inventorycollector.schema.LagInterfaces;
import com.att.vcc.inventorycollector.schema.PInterface;
import com.att.vcc.inventorycollector.schema.PInterfaces;
import com.att.vcc.inventorycollector.schema.Pluggable;
import com.att.vcc.inventorycollector.schema.PluggableSlot;
import com.att.vcc.inventorycollector.schema.PluggableSlots;
import com.att.vcc.inventorycollector.schema.Pluggables;
import com.att.vcc.inventorycollector.schema.Pnf;
import com.att.vcc.inventorycollector.schema.Port;
import com.att.vcc.inventorycollector.schema.Ports;
import com.att.vcc.inventorycollector.schema.Rack;
import com.att.vcc.inventorycollector.schema.Racks;
import com.att.vcc.inventorycollector.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ EventProcessor.class, EcompLogger.class })
public class TestEventProcessor {
	DBAdapter dbadapter;
	EventProcessor eventProcessor;
	ObjectMapper mapper;
	String datetimestamp;
	private JSONObject eventEntity;

	@Before
	public void setUp() throws Exception {
		SessionFactory mockedSessionFactory = Mockito.mock(SessionFactory.class);
		Session mockedSession = Mockito.mock(Session.class);
		Mockito.when(mockedSessionFactory.openSession()).thenReturn(mockedSession);
		dbadapter = mock(DBAdapter.class);
		PowerMockito.whenNew(DBAdapter.class).withNoArguments().thenReturn(dbadapter);
		when(dbadapter.getDBfactory()).thenReturn(mockedSessionFactory);
		Whitebox.setInternalState(EventUtil.class, "ENABLE_PG_LOAD", "true");
		Whitebox.setInternalState(EventUtil.class, "DCAEENV", "D2");
		mockStatic(EcompLogger.class);
		EcompLogger logger = mock(EcompLogger.class);
		when(EcompLogger.getEcompLogger()).thenReturn(logger);
		mapper = EventUtil.getObjectMapperObject();
		datetimestamp = EventUtil.getCurrentTimestamp();
		eventProcessor = new EventProcessor();
	}

	@Test
	public void testEventProcessor() {
		eventProcessor = new EventProcessor();
		assertEquals(eventProcessor.getClass().getName(), "com.att.vcc.inventorycollector.EventProcessor");
	}

	@Test
	public void testSetEventType() {
		String aaiEvent = eventProcessor.setEventType(Constants.AAI_EVENT);
		assertEquals(aaiEvent, Constants.AAI_EVENT);
	}

	@Test
	public void testgetEventType() {
		eventProcessor.setEventType(Constants.AAI_EVENT);
		String aaiEvent = eventProcessor.getEventType();
		assertEquals(aaiEvent, Constants.AAI_EVENT);
	}

	@Test
	public void testSetHeaderEntityType() {
		String vserverEntity = eventProcessor.setHeaderEntityType(Constants.VSERVER_ENTITY);
		assertEquals(vserverEntity, Constants.VSERVER_ENTITY);
	}

	@Test
	public void testgetHeaderEntityType() {
		eventProcessor.setHeaderEntityType(Constants.VSERVER_ENTITY);
		String vserverEntity = eventProcessor.getHeaderEntityType();
		assertEquals(vserverEntity, Constants.VSERVER_ENTITY);
	}

	@Test
	public void testProcessUEBMsgNaradChassis() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/chassis_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Chassis chassis = mapper.readValue(entity, Chassis.class);
			assertTrue(eventProcessor.processNaradChassis(chassis, action, datetimestamp));
		}
	}

	@Test
	public void testProcessUEBMsgNaradPluggableSlot() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/pluggable_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Chassis chassis = mapper.readValue(entity, Chassis.class);
			JSONObject eventJSONObject = null;
			eventJSONObject = new JSONObject(naradEvent);

			JSONObject header = eventJSONObject.optJSONObject("event-header");
			String entityLinkUrl = header.optString("entity-link");

			Map<String, String> urlValues = EventUtil.parseUrl(EventUtil.naradEntities, entityLinkUrl);
			String chassisname = urlValues.get("chassis") != null ? urlValues.get("chassis") : "";
			String cardslotname = urlValues.get("card-slot") != null ? urlValues.get("card-slot") : "";
			String cardtype = urlValues.get("card") != null ? urlValues.get("card") : "";

			PluggableSlots pluggableSlots = chassis.getPluggableSlots();
			if (pluggableSlots != null) {
				List<PluggableSlot> pluggableSlotList = pluggableSlots.getPluggableSlot();
				for (PluggableSlot pluggableSlot : pluggableSlotList) {
					assertTrue(eventProcessor.processNaradPluggableSlot(pluggableSlot, chassisname, cardslotname,
							cardtype, action, datetimestamp));
					break;
				}
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradPluggable() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/pluggable_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Chassis chassis = mapper.readValue(entity, Chassis.class);

			JSONObject eventJSONObject = null;
			eventJSONObject = new JSONObject(naradEvent);

			JSONObject header = eventJSONObject.optJSONObject("event-header");
			String entityLinkUrl = header.optString("entity-link");

			Map<String, String> urlValues = EventUtil.parseUrl(EventUtil.naradEntities, entityLinkUrl);
			String chassisname = urlValues.get("chassis") != null ? urlValues.get("chassis") : "";
			String cardslotname = urlValues.get("card-slot") != null ? urlValues.get("card-slot") : "";
			String cardtype = urlValues.get("card") != null ? urlValues.get("card") : "";
			String pluggableslotname = urlValues.get("pluggable-slot") != null ? urlValues.get("pluggable-slot") : "";

			PluggableSlots pluggableSlots = chassis.getPluggableSlots();
			if (pluggableSlots != null) {
				List<PluggableSlot> pluggableSlotList = pluggableSlots.getPluggableSlot();
				for (PluggableSlot pluggableSlot : pluggableSlotList) {
					Pluggables pluggables = pluggableSlot.getPluggables();
					if (pluggables != null) {
						List<Pluggable> pluggableList = pluggables.getPluggable();
						for (Pluggable pluggable : pluggableList) {
							assertTrue(eventProcessor.processNaradPluggable(pluggable, chassisname, cardslotname,
									cardtype, pluggableslotname, action, datetimestamp));
							if (pluggable != null)
								break;
						}
					}
					break;
				}
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradPort() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/port_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Chassis chassis = mapper.readValue(entity, Chassis.class);

			JSONObject eventJSONObject = null;
			eventJSONObject = new JSONObject(naradEvent);

			JSONObject header = eventJSONObject.optJSONObject("event-header");
			String entityLinkUrl = header.optString("entity-link");

			Map<String, String> urlValues = EventUtil.parseUrl(EventUtil.naradEntities, entityLinkUrl);
			String chassisname = urlValues.get("chassis") != null ? urlValues.get("chassis") : "";
			String cardslotname = urlValues.get("card-slot") != null ? urlValues.get("card-slot") : "";
			String cardtype = urlValues.get("card") != null ? urlValues.get("card") : "";
			String pluggableslotname = urlValues.get("pluggable-slot") != null ? urlValues.get("pluggable-slot") : "";

			PluggableSlots pluggableSlots = chassis.getPluggableSlots();
			if (pluggableSlots != null) {
				List<PluggableSlot> pluggableSlotList = pluggableSlots.getPluggableSlot();
				for (PluggableSlot pluggableSlot : pluggableSlotList) {
					Pluggables pluggables = pluggableSlot.getPluggables();
					if (pluggables != null) {
						List<Pluggable> pluggableList = pluggables.getPluggable();
						for (Pluggable pluggable : pluggableList) {
							Ports ports = pluggable.getPorts();
							if (ports != null) {
								List<Port> portList = ports.getPort();
								for (Port port : portList) {
									assertTrue(eventProcessor.processNaradPort(port, chassisname, cardslotname,
											cardtype, pluggableslotname, pluggable.getPluggableType(), action,
											datetimestamp));
									break;
								}
							}
							break;
						}
					}
					break;
				}
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradPnf() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/pnf_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			Pnf pnf = mapper.readValue(entity, Pnf.class);
			assertTrue(eventProcessor.storeNaradPnf(pnf, action, datetimestamp));
		}
	}

	@Test
	public void testProcessUEBMsgNaradPnfLinterface() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/linterface_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			Pnf pnf = mapper.readValue(entity, Pnf.class);
			LagInterfaces lagInterfaces = pnf.getLagInterfaces();
			List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
			for (LagInterface lagInterface : lagIntList) {
				LInterfaces lInterfaces = lagInterface.getLInterfaces();
				List<LInterface> lIntList = lInterfaces.getLInterface();
				for (LInterface lInterface : lIntList) {
					assertTrue(eventProcessor.processNaradLinterfaceAndChildNodes(lInterface, "pnf", pnf.getPnfName(),
							"na", "na", lagInterface.getInterfaceName(), action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradPnfLaginterface() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/laginterface_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			Pnf pnf = mapper.readValue(entity, Pnf.class);
			LagInterfaces lagInterfaces = pnf.getLagInterfaces();
			List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
			for (LagInterface lagInterface : lagIntList) {
				assertTrue(eventProcessor.storeNaradLagInterface(lagInterface, "pnf", pnf.getPnfName(), action,
						datetimestamp));
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradPnfPinterface() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/pinterface_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			Pnf pnf = mapper.readValue(entity, Pnf.class);
			PInterfaces pInterfaces = pnf.getPInterfaces();
			List<PInterface> pIntList = pInterfaces.getPInterface();
			for (PInterface pInterface : pIntList) {
				assertTrue(eventProcessor.processNaradPinterfaceAndChildNodes(pInterface, "pnf", pnf.getPnfName(),
						action, datetimestamp));
			}
		}
	}

	@Test
	public void testProcessUEBMsgNaradInstanceGroup() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/pinterface_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			InstanceGroup instanceGroup = mapper.readValue(entity, InstanceGroup.class);
			assertTrue(eventProcessor.processNaradInstanceGroup(instanceGroup, action, datetimestamp));
		}
	}

	@Test
	public void testProcessUEBMsgNaradRack() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("naradevent/rack_naradevent.json").getFile());
		String naradEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = eventProcessor.validateEvent(naradEvent);
		String action = eventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = eventProcessor.getEventEntity();
			String entity = eventEntity.toString();
			Complex complex = mapper.readValue(entity, Complex.class);
			Racks racks = complex.getRacks();
			if (racks != null) {
				List<Rack> rackList = racks.getRack();
				for (Rack rack : rackList) {
					assertTrue(eventProcessor.processNaradRackChildNodes(rack, complex.getPhysicalLocationId(), action,
							datetimestamp));
				}
			}

		}
	}

}
