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

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import com.att.vcc.inventorycollector.schema.AllottedResource;
import com.att.vcc.inventorycollector.schema.AllottedResources;
import com.att.vcc.inventorycollector.schema.CloudRegion;
import com.att.vcc.inventorycollector.schema.Complex;
import com.att.vcc.inventorycollector.schema.Cp;
import com.att.vcc.inventorycollector.schema.Cps;
import com.att.vcc.inventorycollector.schema.Customer;
import com.att.vcc.inventorycollector.schema.Flavor;
import com.att.vcc.inventorycollector.schema.Flavors;
import com.att.vcc.inventorycollector.schema.Forwarder;
import com.att.vcc.inventorycollector.schema.Forwarders;
import com.att.vcc.inventorycollector.schema.ForwardingPath;
import com.att.vcc.inventorycollector.schema.GenericVnf;
import com.att.vcc.inventorycollector.schema.Image;
import com.att.vcc.inventorycollector.schema.Images;
import com.att.vcc.inventorycollector.schema.L3InterfaceIpv4AddressList;
import com.att.vcc.inventorycollector.schema.L3InterfaceIpv6AddressList;
import com.att.vcc.inventorycollector.schema.L3Network;
import com.att.vcc.inventorycollector.schema.LInterface;
import com.att.vcc.inventorycollector.schema.LInterfaces;
import com.att.vcc.inventorycollector.schema.LagInterface;
import com.att.vcc.inventorycollector.schema.LagInterfaces;
import com.att.vcc.inventorycollector.schema.LogicalLink;
import com.att.vcc.inventorycollector.schema.Model;
import com.att.vcc.inventorycollector.schema.ModelVer;
import com.att.vcc.inventorycollector.schema.ModelVers;
import com.att.vcc.inventorycollector.schema.NetworkProfile;
import com.att.vcc.inventorycollector.schema.OamNetwork;
import com.att.vcc.inventorycollector.schema.OamNetworks;
import com.att.vcc.inventorycollector.schema.OperationalEnvironment;
import com.att.vcc.inventorycollector.schema.OwningEntity;
import com.att.vcc.inventorycollector.schema.PInterface;
import com.att.vcc.inventorycollector.schema.PInterfaces;
import com.att.vcc.inventorycollector.schema.PhysicalLink;
import com.att.vcc.inventorycollector.schema.Platform;
import com.att.vcc.inventorycollector.schema.Pnf;
import com.att.vcc.inventorycollector.schema.PortGroup;
import com.att.vcc.inventorycollector.schema.PortGroups;
import com.att.vcc.inventorycollector.schema.Project;
import com.att.vcc.inventorycollector.schema.Pserver;
import com.att.vcc.inventorycollector.schema.Service;
import com.att.vcc.inventorycollector.schema.ServiceCapability;
import com.att.vcc.inventorycollector.schema.ServiceInstance;
import com.att.vcc.inventorycollector.schema.ServiceInstances;
import com.att.vcc.inventorycollector.schema.ServiceSubscription;
import com.att.vcc.inventorycollector.schema.ServiceSubscriptions;
import com.att.vcc.inventorycollector.schema.SriovPf;
import com.att.vcc.inventorycollector.schema.SriovPfs;
import com.att.vcc.inventorycollector.schema.SriovVf;
import com.att.vcc.inventorycollector.schema.SriovVfs;
import com.att.vcc.inventorycollector.schema.Subnet;
import com.att.vcc.inventorycollector.schema.Subnets;
import com.att.vcc.inventorycollector.schema.Tenant;
import com.att.vcc.inventorycollector.schema.Tenants;
import com.att.vcc.inventorycollector.schema.Vce;
import com.att.vcc.inventorycollector.schema.VfModule;
import com.att.vcc.inventorycollector.schema.VfModules;
import com.att.vcc.inventorycollector.schema.VipIpv4AddressList;
import com.att.vcc.inventorycollector.schema.VipIpv6AddressList;
import com.att.vcc.inventorycollector.schema.VirtualDataCenter;
import com.att.vcc.inventorycollector.schema.Vlan;
import com.att.vcc.inventorycollector.schema.Vlans;
import com.att.vcc.inventorycollector.schema.VnfImage;
import com.att.vcc.inventorycollector.schema.Vnfc;
import com.att.vcc.inventorycollector.schema.Vserver;
import com.att.vcc.inventorycollector.schema.Vservers;
import com.att.vcc.inventorycollector.schema.Zone;
import com.att.vcc.inventorycollector.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ EventProcessor.class, EcompLogger.class })
public class TestAaiEventProcessor {
	DBAdapter dbadapter;
	AAIEventProcessor aaiEventProcessor;
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
		aaiEventProcessor = new AAIEventProcessor();
	}

	@Test
	public void testProcessAllotedResourceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/allotted_resource_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Customer customer = mapper.readValue(entity, Customer.class);
			String globalCustomerId = customer.getGlobalCustomerId();
			ServiceSubscriptions serviceSubscriptions = customer.getServiceSubscriptions();
			if (serviceSubscriptions != null) {
				List<ServiceSubscription> lServiceSubscription = serviceSubscriptions.getServiceSubscription();
				for (ServiceSubscription serviceSubscription : lServiceSubscription) {
					String serviceType = serviceSubscription.getServiceType();
					ServiceInstances serviceInstances = serviceSubscription.getServiceInstances();
					if (serviceInstances != null) {
						List<ServiceInstance> lserviceInstances = serviceInstances.getServiceInstance();
						for (ServiceInstance serviceInstance : lserviceInstances) {
							String serviceInstanceId = serviceInstance.getServiceInstanceId();
							AllottedResources allottedResources = serviceInstance.getAllottedResources();
							if (allottedResources != null) {
								List<AllottedResource> lallottedResources = allottedResources.getAllottedResource();
								for (AllottedResource allottedResource : lallottedResources) {
									assertTrue(aaiEventProcessor.processAllottedResource(allottedResource, globalCustomerId, serviceType, serviceInstanceId,
											action, datetimestamp));
								}
							}
						}
					}
				}
			}
		}
	}
	@Test
	public void testProcessCloudRegionAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/cloud_region_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			assertTrue(aaiEventProcessor.processCloudRegion(cloudRegion, action, datetimestamp));
		}
	}

	@Test
	public void testProcessCustomerAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/customer_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Customer customer = mapper.readValue(entity, Customer.class);
			assertTrue(aaiEventProcessor.processCustomer(customer, action, datetimestamp));
		}
	}
	@Test
	public void testProcessComplexAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/complex_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Complex complex = mapper.readValue(entity, Complex.class);
			assertTrue(aaiEventProcessor.processComplex(complex, action, datetimestamp));
		}
	}

	@Test
	public void testProcessCpAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/cp_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vnfc vnfc = mapper.readValue(entity, Vnfc.class);
			Cps cps = vnfc.getCps();
			String grandParentEntityId = vnfc.getVnfcName();
			if (cps != null) {
				List<Cp> listCp = cps.getCp();
				if (listCp != null) {
					for (Cp cp : listCp) {
						assertTrue(aaiEventProcessor.processCp(cp, grandParentEntityId, action, datetimestamp));
					}
				}
			}
		}
	}

	@Test
	public void testProcessFlavorAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/flavor_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			Flavors flavors = cloudRegion.getFlavors();
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			if (flavors != null) {
				List<Flavor> lFlavor = flavors.getFlavor();
				for (Flavor flavor : lFlavor) {
					assertTrue(aaiEventProcessor.processFlavor(flavor, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessForwarderAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/forwarder_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			ForwardingPath forwardingPath = mapper.readValue(entity, ForwardingPath.class);
			String forwardingPathId = forwardingPath.getForwardingPathId();
			Forwarders forwarders = forwardingPath.getForwarders();
			if (forwarders != null) {
				List<Forwarder> listForwarder = forwarders.getForwarder();
				if (listForwarder != null) {
					for (Forwarder forwarder : listForwarder) {
						assertTrue(aaiEventProcessor.processForwarder(forwarder, forwardingPathId, action, datetimestamp));
					}
				}
			}
		}
	}

	@Test
	public void testProcessForwardingPathAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/forwarding_path_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			ForwardingPath forwardingPath = mapper.readValue(entity, ForwardingPath.class);
			assertTrue(aaiEventProcessor.processForwardingPath(forwardingPath, action, datetimestamp));
		}
	}	

	@Test
	public void testProcessGenericVnfAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/generic_vnf_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			GenericVnf genericVnf = mapper.readValue(entity, GenericVnf.class);
			assertTrue(aaiEventProcessor.processGenericVnf(genericVnf, action, datetimestamp));
		}
	}

	@Test
	public void testProcessImageAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/image_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			Images images = cloudRegion.getImages();
			if (images != null) {
				List<Image> lImage = images.getImage();
				for (Image image : lImage) {
					assertTrue(aaiEventProcessor.processImage(image, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}
		}
	}


	@Test
	public void testProcessLagInterfaceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/lag_interface_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			GenericVnf genericVnf = mapper.readValue(entity, GenericVnf.class);
			String parentEntityType = Constants.GENERIC_VNF_ENTITY;
			String parentEntityId = genericVnf.getVnfId();
			LagInterfaces lagInterfaces = genericVnf.getLagInterfaces();
			if (lagInterfaces != null) {
				List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
				for (LagInterface lagInterface : lagIntList) {
					assertTrue(aaiEventProcessor.processLagInterface(lagInterface, parentEntityType, parentEntityId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessL3InterfaceIpv4AddressListAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(
				classLoader.getResource("aaievent/l3_interface_ipv6_address_list_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vnfc vnfc = mapper.readValue(entity, Vnfc.class);
			String grandParentEntityType = Constants.VNFC_ENTITY;
			String parentEntityType = Constants.CP_ENTITY;
			String parentEntityId = "na";
			String cloudRegionTenant = "na";
			String pInterfaceName = "na";
			String lagInterfaceName = "na";
			String vlanInterface = "na";
			String grandParentEntityId = vnfc.getVnfcName();

			Cps cps = vnfc.getCps();
			if (cps != null) {
				List<Cp> listCp = cps.getCp();
				if (listCp != null) {
					for (Cp cp : listCp) {
						parentEntityId = cp.getCpInstanceId();
						List<L3InterfaceIpv4AddressList> lIpv4 = cp.getL3InterfaceIpv4AddressList();
						if (lIpv4 != null) {
							for (L3InterfaceIpv4AddressList ipv4 : lIpv4) {
								assertTrue(aaiEventProcessor.processL3InterfaceIpv4AddressList(ipv4, parentEntityType, parentEntityId, grandParentEntityType,
										grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
										datetimestamp));
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void testProcessL3InterfaceIpv6AddressListAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(
				classLoader.getResource("aaievent/l3_interface_ipv6_address_list_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vnfc vnfc = mapper.readValue(entity, Vnfc.class);
			String grandParentEntityType = Constants.VNFC_ENTITY;
			String parentEntityType = Constants.CP_ENTITY;
			String parentEntityId = "na";
			String cloudRegionTenant = "na";
			String pInterfaceName = "na";
			String lagInterfaceName = "na";
			String vlanInterface = "na";
			String grandParentEntityId = vnfc.getVnfcName();
			Cps cps = vnfc.getCps();
			if (cps != null) {
				List<Cp> listCp = cps.getCp();
				if (listCp != null) {
					for (Cp cp : listCp) {
						parentEntityId = cp.getCpInstanceId();
						List<L3InterfaceIpv6AddressList> lIpv6 = vnfc.getL3InterfaceIpv6AddressList();
						if(lIpv6 != null) {
							for (L3InterfaceIpv6AddressList ipv6 : lIpv6) {
								assertTrue(aaiEventProcessor.processL3InterfaceIpv6AddressList(ipv6, parentEntityType, parentEntityId, grandParentEntityType,
										grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
										datetimestamp));
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void testProcessL3NetworkAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/l3_network_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			L3Network l3Network = mapper.readValue(entity, L3Network.class);
			assertTrue(aaiEventProcessor.processL3Network(l3Network, action, datetimestamp));
		}
	}

	@Test
	public void testProcessLInterfaceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/l_interface_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			GenericVnf genericVnf = mapper.readValue(entity, GenericVnf.class);
			String cloudRegionTenant = "na";
			String pInterfaceName = "na";
			String lagInterfaceName = "na";
			String parentEntityType = Constants.GENERIC_VNF_ENTITY;
			String parentEntityId = genericVnf.getVnfId();
			LInterfaces lInterfaces = genericVnf.getLInterfaces();
			if (lInterfaces != null) {
				List<LInterface> lIntList = lInterfaces.getLInterface();
				for (LInterface lInterface : lIntList) {
					assertTrue(aaiEventProcessor.processLinterface(lInterface, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
							lagInterfaceName, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessLogicalLinkAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/logical_link_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			LogicalLink logicalLink = mapper.readValue(entity, LogicalLink.class);
			assertTrue(aaiEventProcessor.processLogicalLink(logicalLink, action, datetimestamp));
		}
	}

	@Test
	public void testProcessModelAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/model_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Model model = mapper.readValue(entity, Model.class);
			assertTrue(aaiEventProcessor.processModel(model, action, datetimestamp));
		}
	}

	@Test
	public void testProcessModelVerAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/model_ver_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Model model = mapper.readValue(entity, Model.class);
			String modelInvariantId = model.getModelInvariantId();

			ModelVers modelVers = model.getModelVers();
			if (modelVers != null) {
				List<ModelVer> mvs = modelVers.getModelVer();
				for (ModelVer mv : mvs) {
					assertTrue(aaiEventProcessor.processModelVer(mv, modelInvariantId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessNetworkProfileAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/network_profile_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			NetworkProfile networkProfile = mapper.readValue(entity, NetworkProfile.class);
			assertTrue(aaiEventProcessor.processNetworkProfile(networkProfile, action, datetimestamp));
		}
	}
	
	@Test
	public void testProcessOamNetworkAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/oam_network_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			OamNetworks oamNetworks = cloudRegion.getOamNetworks();
			if (oamNetworks != null) {
				List<OamNetwork> lOamNetwork = oamNetworks.getOamNetwork();
				for (OamNetwork oamNetwork : lOamNetwork) {
					assertTrue(aaiEventProcessor.processOamNetwork(oamNetwork, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessOperationalEnvironmentAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/operational_environment_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			OperationalEnvironment operationalEnvironment = mapper.readValue(entity, OperationalEnvironment.class);
			assertTrue(aaiEventProcessor.processOperationalEnvironment(operationalEnvironment, action, datetimestamp));
		}
	}

	@Test
	public void testProcessOwningEntityAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/owning_entity_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			OwningEntity owningEntity = mapper.readValue(entity, OwningEntity.class);
			assertTrue(aaiEventProcessor.processOwningEntity(owningEntity, action, datetimestamp));
		}
	}

	@Test
	public void testProcessPlatformAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/platform_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Platform platform = mapper.readValue(entity, Platform.class);
			assertTrue(aaiEventProcessor.processPlatform(platform, action, datetimestamp));
		}
	}

	@Test
	public void testProcessPnfAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/pnf_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Pnf pnf = mapper.readValue(entity, Pnf.class);
			assertTrue(aaiEventProcessor.processPnf(pnf, action, datetimestamp));
		}
	}


	@Test
	public void testProcessPortGroupAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/port_group_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vce vce = mapper.readValue(entity, Vce.class);
			String vnfId = vce.getVnfId();
			PortGroups postGroups = vce.getPortGroups();
			if (postGroups != null) {
				List<PortGroup> lPostGroups = postGroups.getPortGroup();
				for (PortGroup portGroup : lPostGroups) {
					assertTrue(aaiEventProcessor.processPortGroup(portGroup, vnfId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessProjectAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/project_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Project project = mapper.readValue(entity, Project.class);
			assertTrue(aaiEventProcessor.processProject(project, action, datetimestamp));
		}
	}

	@Test
	public void testProcessPhysicalLinkAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/physical_link_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			PhysicalLink physicalLink = mapper.readValue(entity, PhysicalLink.class);
			assertTrue(aaiEventProcessor.processPhysicalLink(physicalLink, action, datetimestamp));
		}
	}
	/***
	@Test
	public void testProcessPInterfaceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/p_interface_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();

		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Pserver pserver = mapper.readValue(entity, Pserver.class);
			String parentEntityType = Constants.PSERVER_ENTITY;
			String parentEntityId = pserver.getHostname();
			PInterfaces pInterfaces = pserver.getPInterfaces();
			if (pInterfaces != null) {
				List<PInterface> pIntList = pInterfaces.getPInterface();
				for (PInterface pInterface : pIntList) {
					assertTrue(aaiEventProcessor.processPInterface(pInterface, parentEntityType, parentEntityId, action, datetimestamp));
				}
			}
		}

	}
		***/
	
	@Test
	public void testProcessPserverAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/pserver_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Pserver pserver = mapper.readValue(entity, Pserver.class);
			assertTrue(aaiEventProcessor.processPserver(pserver, action, datetimestamp));
		}
	}

	@Test
	public void testProcessServiceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/service_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Service service = mapper.readValue(entity, Service.class);
			assertTrue(aaiEventProcessor.processService(service, action, datetimestamp));
		}
	}

	@Test
	public void testProcessServiceCapabilityAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/service_capability_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			ServiceCapability serviceCapability = mapper.readValue(entity, ServiceCapability.class);
			assertTrue(aaiEventProcessor.processServiceCapability(serviceCapability, action, datetimestamp));
		}
	}

	@Test
	public void testProcessServiceSubscriptionAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/service_subscription_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Customer customer = mapper.readValue(entity, Customer.class);
			String globalCustomerId = customer.getGlobalCustomerId();
			ServiceSubscriptions serviceSubscriptions = customer.getServiceSubscriptions();
			if (serviceSubscriptions != null) {
				List<ServiceSubscription> lServiceSubscription = serviceSubscriptions.getServiceSubscription();
				for (ServiceSubscription serviceSubscription : lServiceSubscription) {
					assertTrue(aaiEventProcessor.processServiceSubscription(serviceSubscription, globalCustomerId, action, datetimestamp));
				}
			}
		}
	}


	@Test
	public void testProcessServiceInstanceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/service_instance_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Customer customer = mapper.readValue(entity, Customer.class);
			String globalCustomerId = customer.getGlobalCustomerId();
			ServiceSubscriptions serviceSubscriptions = customer.getServiceSubscriptions();
			if (serviceSubscriptions != null) {
				List<ServiceSubscription> lServiceSubscription = serviceSubscriptions.getServiceSubscription();
				for (ServiceSubscription serviceSubscription : lServiceSubscription) {
					String serviceType = serviceSubscription.getServiceType();
					ServiceInstances serviceInstances = serviceSubscription.getServiceInstances();
					if (serviceInstances != null) {
						List<ServiceInstance> lserviceInstances = serviceInstances.getServiceInstance();
						for (ServiceInstance serviceInstance : lserviceInstances) {
							assertTrue(aaiEventProcessor.processServiceInstance(serviceInstance, globalCustomerId, serviceType, action,
									datetimestamp));
						}
					}
				}
			}
		}
	}

	@Test
	public void testProcessSriovPfAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/sriov_pf_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Pserver pserver = mapper.readValue(entity, Pserver.class);
			String parentEntityType = Constants.PSERVER_ENTITY;
			String parentEntityId = pserver.getHostname();
			PInterfaces pInterfaces = pserver.getPInterfaces();
			if (pInterfaces != null) {
				List<PInterface> pIntList = pInterfaces.getPInterface();
				for (PInterface pInterface : pIntList) {
					String pInterfaceName = pInterface.getInterfaceName();
					SriovPfs sriovPfs = pInterface.getSriovPfs();
					if (sriovPfs != null) {
						List<SriovPf> lSriovPf = sriovPfs.getSriovPf();
						for (SriovPf sriovPf : lSriovPf) {
							assertTrue(aaiEventProcessor.processSriovPf(sriovPf, parentEntityType, parentEntityId, pInterfaceName, action,
									datetimestamp));
						}
					}
				}
			}
		}
	}

	@Test
	public void testProcessSriovVfAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/sriov_vf_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			Tenants tenants = cloudRegion.getTenants();
			if (tenants != null) {
				List<Tenant> lTenant = tenants.getTenant();
				for (Tenant tenant : lTenant) {
					String tenantId = tenant.getTenantId();
					Vservers vservers = tenant.getVservers();
					if (vservers != null) {
						List<Vserver> lVservers = vservers.getVserver();
						for (Vserver vserver : lVservers) {
							String vserverId = vserver.getVserverId();
							String cloudRegionTenant = cloudOwner + "|" + cloudRegionId + "|" + tenantId;
							String pInterfaceName = "na";
							String lagInterfaceName = "na";
							LInterfaces lInterfaces = vserver.getLInterfaces();
							if (lInterfaces != null) {
								List<LInterface> lIntList = lInterfaces.getLInterface();
								for (LInterface lInterface : lIntList) {
									SriovVfs sriovVfs = lInterface.getSriovVfs();
									if (sriovVfs != null) {
										String intName = lInterface.getInterfaceName();
										String entityType = Constants.L_INTERFACE_ENTITY;
										if (cloudRegionTenant != null) {
											String[] tempArr = cloudRegionTenant.split("\\|");
											cloudOwner = tempArr[0];
											cloudRegionId = tempArr.length >= 2 ? tempArr[1] : "";
											tenantId = tempArr.length >= 3 ? tempArr[2] : "";
										}
										List<SriovVf> lSriovVf = sriovVfs.getSriovVf();
										if (lSriovVf != null) {
											for (SriovVf sriovVf : lSriovVf) {
												assertTrue(aaiEventProcessor.processSriovVf(sriovVf, entityType, intName, cloudOwner, cloudRegionId, tenantId,
														Constants.VSERVER_ENTITY, vserverId, pInterfaceName, lagInterfaceName, action, datetimestamp));
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

	@Test
	public void testProcessSubnetAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/subnet_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			L3Network l3Network = mapper.readValue(entity, L3Network.class);
			String networkId = l3Network.getNetworkId();
			Subnets subnets = l3Network.getSubnets();
			if (subnets != null) {
				List<Subnet> lSubnet = subnets.getSubnet();
				for (Subnet subnet : lSubnet) {
					assertTrue(aaiEventProcessor.processSubnet(subnet, networkId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessTenantAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/tenant_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			Tenants tenants = cloudRegion.getTenants();
			if (tenants != null) {
				List<Tenant> lTenant = tenants.getTenant();
				for (Tenant tenant : lTenant) {
					assertTrue(aaiEventProcessor.processTenant(tenant, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessVceAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vce_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vce vce = mapper.readValue(entity, Vce.class);
			assertTrue(aaiEventProcessor.processVce(vce, action, datetimestamp));
		}
	}

	@Test
	public void testProcessVfModuleAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vf_module_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			GenericVnf genericVnf = mapper.readValue(entity, GenericVnf.class);
			String parentEntityType = Constants.GENERIC_VNF_ENTITY;
			String parentEntityId = genericVnf.getVnfId();
			VfModules vfModules = genericVnf.getVfModules();
			if (vfModules != null) {
				List<VfModule> vfModulesList = vfModules.getVfModule();
				for (VfModule vfModule : vfModulesList) {
					assertTrue(aaiEventProcessor.processVfModule(vfModule, parentEntityType, parentEntityId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessVipIpv4AddressListAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vip_ipv4_address_list_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			List<VipIpv4AddressList> vipIpv4AddressList = cloudRegion.getVipIpv4AddressList();
			if (vipIpv4AddressList != null) {
				for (VipIpv4AddressList vipipv4 : vipIpv4AddressList) {
					assertTrue(aaiEventProcessor.processCloudVIPIPV4List(vipipv4, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}			
		}
	}

	@Test
	public void testProcessVipIpv6AddressListAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vip_ipv6_address_list_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			List<VipIpv6AddressList> vipIpv6AddressList = cloudRegion.getVipIpv6AddressList();
			if (vipIpv6AddressList != null) {
				for (VipIpv6AddressList vipipv6 : vipIpv6AddressList) {
					assertTrue(aaiEventProcessor.processCloudVIPIPV6List(vipipv6, cloudOwner, cloudRegionId, action, datetimestamp));
				}
			}
		}
	}

	@Test
	public void testProcessVirtualDataCenterAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/virtual_data_center_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			VirtualDataCenter virtualDataCenter = mapper.readValue(entity, VirtualDataCenter.class);
			assertTrue(aaiEventProcessor.processVirtualDataCenter(virtualDataCenter, action, datetimestamp));
		}
	}

	@Test
	public void testProcessVlanAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vlan_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			Tenants tenants = cloudRegion.getTenants();
			if (tenants != null) {
				List<Tenant> lTenant = tenants.getTenant();
				for (Tenant tenant : lTenant) {
					String tenantId = tenant.getTenantId();
					Vservers vservers = tenant.getVservers();
					if (vservers != null) {
						List<Vserver> lVservers = vservers.getVserver();
						for (Vserver vserver : lVservers) {
							String vserverId = vserver.getVserverId();
							String cloudRegionTenant = cloudOwner + "|" + cloudRegionId + "|" + tenantId;
							String pInterfaceName = "na";
							String lagInterfaceName = "na";
							LInterfaces lInterfaces = vserver.getLInterfaces();
							if (lInterfaces != null) {
								List<LInterface> lIntList = lInterfaces.getLInterface();
								for (LInterface lInterface : lIntList) {
									String intName = lInterface.getInterfaceName();
									Vlans vlans = lInterface.getVlans();
									if (vlans != null) {
										List<Vlan> lVlan = vlans.getVlan();
										for (Vlan vlan : lVlan) {
											assertTrue(aaiEventProcessor.processVlan(vlan, intName,Constants.VSERVER_ENTITY, vserverId, cloudRegionTenant,
													pInterfaceName, lagInterfaceName, action, datetimestamp));
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

	@Test
	public void testProcessVnfcAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vnfc_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Vnfc vnfc = mapper.readValue(entity, Vnfc.class);
			assertTrue(aaiEventProcessor.processVnfc(vnfc, action, datetimestamp));
		}
	}

	@Test
	public void testProcessVnfImageAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vnf_image_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			VnfImage vnfImage = mapper.readValue(entity, VnfImage.class);
			assertTrue(aaiEventProcessor.processVnfImage(vnfImage, action, datetimestamp));
		}
	}

	@Test
	public void testProcessVserverAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/vserver_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();
			Tenants tenants = cloudRegion.getTenants();
			if (tenants != null) {
				List<Tenant> lTenant = tenants.getTenant();
				for (Tenant tenant : lTenant) {
					String tenantId = tenant.getTenantId();
					Vservers vservers = tenant.getVservers();
					if (vservers != null) {
						List<Vserver> lVservers = vservers.getVserver();
						for (Vserver vserver : lVservers) {
							assertTrue(aaiEventProcessor.processVserver(vserver, cloudOwner, cloudRegionId, tenantId, action, datetimestamp));
						}
					}
				}
			}
		}
	}


	@Test
	public void testProcessZoneAaiEvent() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("aaievent/zone_aaievent.json").getFile());
		String aaiEvent = EventUtil.getTemplateContent(file.getAbsolutePath());
		boolean status = aaiEventProcessor.validateEvent(aaiEvent);
		String action = aaiEventProcessor.getEventHeaderAction();
		if (status) {
			eventEntity = aaiEventProcessor.getEventEntity();
			String entity = eventEntity.toString();

			Zone zone = mapper.readValue(entity, Zone.class);
			assertTrue(aaiEventProcessor.processZone(zone, action, datetimestamp));
		}
	}

}
