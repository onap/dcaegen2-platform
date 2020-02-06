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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.schema.*;
import com.att.vcc.inventorycollector.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIEventProcessor extends EventProcessor {
	private String datetimestamp = null;

	public AAIEventProcessor() {
		super();
	}

	private String setDataTimeStamp() {
		this.datetimestamp = getCurrentTimestamp();
		return this.datetimestamp;
	}

	private String getDataTimeStamp() {
		if (this.datetimestamp == null)
			this.datetimestamp = getCurrentTimestamp();
		return this.datetimestamp;
	}

	public boolean processAAIEvent(String event) throws IOException {
		boolean status = super.validateEvent(event);
		if (status) {
			JSONObject eventEntity = getEventEntity();
			String entity = eventEntity.toString();
			ObjectMapper mapper = EventUtil.getObjectMapperObject();
			String entityType = getHeaderEntityType();
			String action = getEventHeaderAction();
			this.setDataTimeStamp();
			String topEntityType = this.getEventHeaderTopEntityType();
			String id = this.getEventHeaderId();

			switch (topEntityType) {
			case Constants.CLOUD_REGION_ENTITY:
				CloudRegion cloudRegion = mapper.readValue(entity, CloudRegion.class);
				if (cloudRegion != null) {
					status = processAaiCloudRegionAndChildNodes(cloudRegion, action, datetimestamp);
				}
				break;
			case Constants.CUSTOMER_ENTITY:
				Customer customer = mapper.readValue(entity, Customer.class);
				if (customer != null) {
					status = processAaiCustomerAndChildNodes(customer, action, datetimestamp);
				}
				break;
			case Constants.PSERVER_ENTITY:
				Pserver pserver = mapper.readValue(entity, Pserver.class);
				if (pserver != null) {
					status = processAaiPserverAndChildNodes(pserver, action, datetimestamp);
				}
				break;
			case Constants.PNF_ENTITY:
				Pnf pnf = mapper.readValue(entity, Pnf.class);
				if (pnf != null) {
					status = processAaiPnfAndChildNodes(pnf, action, datetimestamp);
				}
				break;
			case Constants.GENERIC_VNF_ENTITY:
				GenericVnf genericVnf = mapper.readValue(entity, GenericVnf.class);
				if (genericVnf != null) {
					status = processAaiGenericVnfAndChildNodes(genericVnf, action, datetimestamp);
				}
				break;
			case Constants.VNFC_ENTITY:
				Vnfc vnfc = mapper.readValue(entity, Vnfc.class);
				if (vnfc != null) {
					status = processAaiVnfcAndChildNodes(vnfc, action, datetimestamp);
				}
				break;
			case Constants.VPLS_PE_ENTITY:
				VplsPe vplsPe = mapper.readValue(entity, VplsPe.class);
				if (vplsPe != null) {
					status = processAaiVplsPeAndChildNodes(vplsPe, action, datetimestamp);
				}
				break;
			default:
				status = false;
			}

			switch (entityType) {
			case Constants.ALLOTTED_RESOURCE_ENTITY:
			case Constants.AVAILABILITY_ZONE_ENTITY:
			case Constants.CLOUD_REGION_ENTITY:
			case Constants.CP_ENTITY:
			case Constants.CUSTOMER_ENTITY:
			case Constants.FLAVOR_ENTITY:
			case Constants.GENERIC_VNF_ENTITY:
			case Constants.IMAGE_ENTITY:
			case Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY:
			case Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY:
			case Constants.LAG_INTERFACE_ENTITY:
			case Constants.L_INTERFACE_ENTITY:
			case Constants.NOS_SERVER_ENTITY:
			case Constants.OAM_NETWORK_ENTITY:
			case Constants.PNF_ENTITY:
			case Constants.PSERVER_ENTITY:
			case Constants.P_INTERFACE_ENTITY:
			case Constants.SERVICE_INSTANCE_ENTITY:
			case Constants.SERVICE_SUBSCRIPTION_ENTITY:
			case Constants.SRIOV_PF_ENTITY:
			case Constants.SRIOV_VF_ENTITY:
			case Constants.TENANT_ENTITY:
			case Constants.VF_MODULE_ENTITY:
			case Constants.VIP_IPV4_ADDRESS_LIST_ENTITY:
			case Constants.VIP_IPV6_ADDRESS_LIST_ENTITY:
			case Constants.VLAN_ENTITY:
			case Constants.VNFC_ENTITY:
			case Constants.VPLS_PE_ENTITY:
			case Constants.VSERVER_ENTITY:
				// Do nothing as as the above entities are taken care by Switch
				// statement of topEntityType.
				break;
			case Constants.COMPLEX_ENTITY:
				Complex complex = mapper.readValue(entity, Complex.class);
				if (complex != null) {
					status = processComplex(complex, action, datetimestamp);
				}
				break;
			case Constants.FORWARDER_ENTITY:
			case Constants.FORWARDING_PATH_ENTITY:
				ForwardingPath forwardingPath = mapper.readValue(entity, ForwardingPath.class);
				if (forwardingPath != null) {
					status = this.processAaiForwardingPathAndChildNodes(forwardingPath, action, datetimestamp);
				}
				break;
			case Constants.MODEL_ENTITY:
			case Constants.MODEL_VER_ENTITY:
				Model model = mapper.readValue(entity, Model.class);
				if (model != null) {
					status = processAaiModelAndChildNodes(model, action, datetimestamp);
				}
				break;
			case Constants.SERVICE_CAPABILITY_ENTITY:
				ServiceCapability serviceCapability = mapper.readValue(entity, ServiceCapability.class);
				if (serviceCapability != null) {
					status = processServiceCapability(serviceCapability, action, datetimestamp);
				}
				break;
			case Constants.SUBNET_ENTITY:
			case Constants.L3_NETWORK_ENTITY:
				L3Network l3Network = mapper.readValue(entity, L3Network.class);
				if (l3Network != null) {
					status = this.processAaiL3NetworkAndChildNodes(l3Network, action, datetimestamp);
				}
				break;
			case Constants.LAG_LINK_ENTITY:
				LagLink lagLink = mapper.readValue(entity, LagLink.class);
				if (lagLink != null) {
					status = this.processLagLink(lagLink, action, datetimestamp);
				}
				break;
			case Constants.LINE_OF_BUSINESS_ENTITY:
				LineOfBusiness lineOfBusiness = mapper.readValue(entity, LineOfBusiness.class);
				if (lineOfBusiness != null) {
					status = this.processLineOfBusiness(lineOfBusiness, action, datetimestamp);
				}
				break;
			case Constants.LOGICAL_LINK_ENTITY:
				LogicalLink logicalLink = mapper.readValue(entity, LogicalLink.class);
				if (logicalLink != null) {
					status = this.processLogicalLink(logicalLink, action, datetimestamp);
				}
				break;
			case Constants.NETWORK_PROFILE_ENTITY:
				NetworkProfile networkProfile = mapper.readValue(entity, NetworkProfile.class);
				if (networkProfile != null) {
					status = this.processNetworkProfile(networkProfile, action, datetimestamp);
				}
				break;
			case Constants.OPERATIONAL_ENVIRONMENT_ENTITY:
				OperationalEnvironment operationalEnvironment = mapper.readValue(entity, OperationalEnvironment.class);
				if (operationalEnvironment != null) {
					status = this.processOperationalEnvironment(operationalEnvironment, action, datetimestamp);
				}
				break;
			case Constants.OWNING_ENTITY_ENTITY:
				OwningEntity owningEntity = mapper.readValue(entity, OwningEntity.class);
				if (owningEntity != null) {
					status = this.processOwningEntity(owningEntity, action, datetimestamp);
				}
				break;
			case Constants.PHYSICAL_LINK_ENTITY:
				PhysicalLink physicalLink = mapper.readValue(entity, PhysicalLink.class);
				if (physicalLink != null) {
					status = this.processPhysicalLink(physicalLink, action, datetimestamp);
				}
				break;
			case Constants.PLATFORM_ENTITY:
				Platform platform = mapper.readValue(entity, Platform.class);
				if (platform != null) {
					status = this.processPlatform(platform, action, datetimestamp);
				}
				break;
			case Constants.PROJECT_ENTITY:
				Project project = mapper.readValue(entity, Project.class);
				if (project != null) {
					status = this.processProject(project, action, datetimestamp);
				}
				break;
			case Constants.SERVICE_ENTITY:
				Service service = mapper.readValue(entity, Service.class);
				if (service != null) {
					status = processService(service, action, datetimestamp);
				}
				break;
			case Constants.PORT_GROUP_ENTITY:
			case Constants.VCE_ENTITY:
				Vce vce = mapper.readValue(entity, Vce.class);
				if (vce != null) {
					status = processVceAndChildNodes(vce, action, datetimestamp);
				}
				break;
			case Constants.VIRTUAL_DATA_CENTER_ENTITY:
				VirtualDataCenter virtualDataCenter = mapper.readValue(entity, VirtualDataCenter.class);
				if (virtualDataCenter != null) {
					status = processVirtualDataCenter(virtualDataCenter, action, datetimestamp);
				}
				break;
			case Constants.VNF_IMAGE_ENTITY:
				VnfImage vnfImage = mapper.readValue(entity, VnfImage.class);
				if (vnfImage != null) {
					status = processVnfImage(vnfImage, action, datetimestamp);
				}
				break;
			case Constants.ZONE_ENTITY:
				Zone zone = mapper.readValue(entity, Zone.class);
				if (zone != null) {
					status = processZone(zone, action, datetimestamp);
				}
				break;

			default:
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_EVENT_ENTITY_TYPE_NOT_PROCESSED,
						entityType, id);

			}

		}
		return status;
	}

	public boolean processAaiCloudRegionAndChildNodes(CloudRegion cloudRegion, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (cloudRegion != null) {
			if (headerEntityType.equalsIgnoreCase(Constants.CLOUD_REGION_ENTITY)) {
				status = processCloudRegion(cloudRegion, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String cloudOwner = cloudRegion.getCloudOwner();
			String cloudRegionId = cloudRegion.getCloudRegionId();

			AvailabilityZones availabilityZones = cloudRegion.getAvailabilityZones();
			status = processAaiAvailabilityZones(availabilityZones, cloudOwner, cloudRegionId, action, datetimestamp);
			allStatus = status ? allStatus : status;

			Flavors flavors = cloudRegion.getFlavors();
			status = processAaiFlavors(flavors, cloudOwner, cloudRegionId, action, datetimestamp);
			allStatus = status ? allStatus : status;

			Images images = cloudRegion.getImages();
			status = processAaiImages(images, cloudOwner, cloudRegionId, action, datetimestamp);
			allStatus = status ? allStatus : status;

			OamNetworks oamNetworks = cloudRegion.getOamNetworks();
			status = processAaiOamNetworks(oamNetworks, cloudOwner, cloudRegionId, action, datetimestamp);
			allStatus = status ? allStatus : status;

			List<VipIpv4AddressList> vipIpv4AddressList = cloudRegion.getVipIpv4AddressList();
			status = processAaiCloudVipIpv4AddressList(vipIpv4AddressList, cloudOwner, cloudRegionId, action,
					datetimestamp);
			allStatus = status ? allStatus : status;

			List<VipIpv6AddressList> vipIpv6AddressList = cloudRegion.getVipIpv6AddressList();
			status = processAaiCloudVipIpv6AddressList(vipIpv6AddressList, cloudOwner, cloudRegionId, action,
					datetimestamp);
			allStatus = status ? allStatus : status;

			Tenants tenants = cloudRegion.getTenants();
			status = processAaiTenants(tenants, cloudOwner, cloudRegionId, action, datetimestamp);
			allStatus = status ? allStatus : status;

		}
		return allStatus;
	}

	public boolean processAaiTenants(Tenants tenants, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (tenants != null) {
			List<Tenant> lTenant = tenants.getTenant();
			for (Tenant tenant : lTenant) {
				status = processAaiTenantAndChildNodes(tenant, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiTenantAndChildNodes(Tenant tenant, String cloudOwner, String cloudRegionid, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (tenant != null) {
			if (headerEntityType.equalsIgnoreCase(Constants.CLOUD_REGION_ENTITY)
					|| headerEntityType.equalsIgnoreCase(Constants.TENANT_ENTITY)) {
				status = processTenant(tenant, cloudOwner, cloudRegionid, action, datetimestamp);
				if (!status)
					allStatus = status;
			}

			String tenantId = tenant.getTenantId();
			Vservers vservers = tenant.getVservers();
			status = processAaiVservers(vservers, cloudOwner, cloudRegionid, tenantId, action, datetimestamp);
			if (!status)
				allStatus = status;
			NosServers nosServers = tenant.getNosServers();
			status = processAaiNosServers(nosServers, cloudOwner, cloudRegionid, tenantId, action, datetimestamp);
			if (!status)
				allStatus = status;
		}
		return allStatus;
	}

	public boolean processAaiNosServers(NosServers nosServers, String cloudOwner, String cloudRegionId, String tenantId,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (nosServers != null) {
			List<NosServer> lNosServer = nosServers.getNosServer();
			for (NosServer nosServer : lNosServer) {
				if (headerEntityType.equalsIgnoreCase(Constants.CLOUD_REGION_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.TENANT_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.NOS_SERVER_ENTITY)) {
					status = this.processNosServer(nosServer, tenantId, cloudOwner, cloudRegionId, action,
							datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}
		return allStatus;
	}

	public boolean processAaiVservers(Vservers vservers, String cloudOwner, String cloudRegionId, String tenantId,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (vservers != null) {
			List<Vserver> lVservers = vservers.getVserver();
			for (Vserver vserver : lVservers) {
				status = processAaiVserverAndChildNodes(vserver, cloudOwner, cloudRegionId, tenantId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processAaiVserverAndChildNodes(Vserver vserver, String cloudOwner, String cloudRegionId,
			String tenantId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (headerEntityType.equalsIgnoreCase(Constants.CLOUD_REGION_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.TENANT_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.VSERVER_ENTITY)) {
			status = processVserver(vserver, cloudOwner, cloudRegionId, tenantId, action, datetimestamp);
			if (!status)
				allStatus = status;
		}
		String vserverId = vserver.getVserverId();
		String cloudRegionTenant = cloudOwner + "|" + cloudRegionId + "|" + tenantId;
		String pInterfaceName = "na";
		String lagInterfaceName = "na";

		LInterfaces lInterfaces = vserver.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, Constants.VSERVER_ENTITY, vserverId, cloudRegionTenant,
				pInterfaceName, lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;
		return allStatus;

	}

	public boolean processAaiImages(Images images, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (images != null) {
			List<Image> lImage = images.getImage();
			for (Image image : lImage) {
				status = processImage(image, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiOamNetworks(OamNetworks oamNetworks, String cloudOwner, String cloudRegionId,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (oamNetworks != null) {
			List<OamNetwork> lOamNetwork = oamNetworks.getOamNetwork();
			for (OamNetwork oamNetwork : lOamNetwork) {
				status = processOamNetwork(oamNetwork, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiAvailabilityZones(AvailabilityZones availabilityZones, String cloudOwner,
			String cloudRegionId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (availabilityZones != null) {
			List<AvailabilityZone> lAvailabilityZone = availabilityZones.getAvailabilityZone();
			for (AvailabilityZone availabilityZone : lAvailabilityZone) {
				status = this.processAvailabilityZone(availabilityZone, cloudOwner, cloudRegionId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiFlavors(Flavors flavors, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (flavors != null) {
			List<Flavor> lFlavor = flavors.getFlavor();
			for (Flavor flavor : lFlavor) {
				status = processFlavor(flavor, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiCloudVipIpv4AddressList(List<VipIpv4AddressList> vipIpv4AddressList, String cloudOwner,
			String cloudRegionId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (vipIpv4AddressList != null) {
			for (VipIpv4AddressList vipipv4 : vipIpv4AddressList) {
				status = processCloudVIPIPV4List(vipipv4, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processAaiCloudVipIpv6AddressList(List<VipIpv6AddressList> vipIpv6AddressList, String cloudOwner,
			String cloudRegionId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (vipIpv6AddressList != null) {
			for (VipIpv6AddressList vipipv6 : vipIpv6AddressList) {
				status = processCloudVIPIPV6List(vipipv6, cloudOwner, cloudRegionId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processAaiCustomerAndChildNodes(Customer customer, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (customer != null) {
			String headerEntityType = this.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.CUSTOMER_ENTITY)) {
				status = processCustomer(customer, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String globalCustomerId = customer.getGlobalCustomerId();
			ServiceSubscriptions serviceSubscriptions = customer.getServiceSubscriptions();
			status = processAaiServiceSubscriptionsAndChildNodes(serviceSubscriptions, globalCustomerId, action,
					datetimestamp);
			if (!status)
				allStatus = status;

		}
		return allStatus;
	}

	public boolean processAaiServiceSubscriptionsAndChildNodes(ServiceSubscriptions serviceSubscriptions,
			String globalCustomerId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (serviceSubscriptions != null) {
			String headerEntityType = this.getHeaderEntityType();
			List<ServiceSubscription> lServiceSubscription = serviceSubscriptions.getServiceSubscription();
			for (ServiceSubscription serviceSubscription : lServiceSubscription) {
				if (headerEntityType.equalsIgnoreCase(Constants.CUSTOMER_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.SERVICE_SUBSCRIPTION_ENTITY)) {
					status = processServiceSubscription(serviceSubscription, globalCustomerId, action, datetimestamp);
					if (!status)
						allStatus = status;
				}
				String serviceType = serviceSubscription.getServiceType();
				ServiceInstances serviceInstances = serviceSubscription.getServiceInstances();
				status = processAaiServiceInstancesAndChildNodes(serviceInstances, globalCustomerId, serviceType,
						action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;

	}

	public boolean processAaiServiceInstancesAndChildNodes(ServiceInstances serviceInstances, String globalCustomerId,
			String serviceType, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (serviceInstances != null) {
			List<ServiceInstance> lserviceInstances = serviceInstances.getServiceInstance();
			for (ServiceInstance serviceInstance : lserviceInstances) {
				if (headerEntityType.equalsIgnoreCase(Constants.CUSTOMER_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.SERVICE_SUBSCRIPTION_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.SERVICE_INSTANCE_ENTITY)) {
					status = processServiceInstance(serviceInstance, globalCustomerId, serviceType, action,
							datetimestamp);
					if (!status)
						allStatus = status;
				}
				AllottedResources allottedResources = serviceInstance.getAllottedResources();
				status = processAaiAllottedResourcesAndChildNodes(allottedResources, globalCustomerId, serviceType,
						serviceInstance.getServiceInstanceId(), action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiAllottedResourcesAndChildNodes(AllottedResources allottedResources,
			String globalCustomerId, String serviceType, String serviceInstanceId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (allottedResources != null) {
			List<AllottedResource> lallottedResources = allottedResources.getAllottedResource();
			for (AllottedResource allottedResource : lallottedResources) {
				if (headerEntityType.equalsIgnoreCase(Constants.CUSTOMER_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.SERVICE_SUBSCRIPTION_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.SERVICE_INSTANCE_ENTITY)
						|| headerEntityType.equalsIgnoreCase(Constants.ALLOTTED_RESOURCE_ENTITY)) {
					status = processAllottedResource(allottedResource, globalCustomerId, serviceType, serviceInstanceId,
							action, datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}
		return allStatus;
	}

	public boolean processAaiForwardingPathAndChildNodes(ForwardingPath forwardingPath, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (forwardingPath != null) {
			String headerEntityType = this.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.FORWARDING_PATH_ENTITY)) {
				status = this.processForwardingPath(forwardingPath, action, datetimestamp);
				if (!status)
					allStatus = status;
			}

			String forwardingPathId = forwardingPath.getForwardingPathId() != null
					? forwardingPath.getForwardingPathId()
					: "";
			Forwarders forwarders = forwardingPath.getForwarders();
			status = this.processAaiForwardersAndChildNodes(forwarders, forwardingPathId, action, datetimestamp);
			if (!status)
				allStatus = status;

		}
		return allStatus;
	}

	public boolean processAaiForwardersAndChildNodes(Forwarders forwarders, String forwardingPathId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (forwarders != null) {
			List<Forwarder> listForwarder = forwarders.getForwarder();
			if (listForwarder != null) {
				for (Forwarder forwarder : listForwarder) {
					status = this.processForwarder(forwarder, forwardingPathId, action, datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}
		return allStatus;
	}

	public boolean processAaiL3NetworkAndChildNodes(L3Network l3Network, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (l3Network != null) {
			String headerEntityType = this.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.L3_NETWORK_ENTITY)) {
				status = this.processL3Network(l3Network, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String networkId = l3Network.getNetworkId();
			Subnets subnets = l3Network.getSubnets();
			if (subnets != null) {
				List<Subnet> lSubnet = subnets.getSubnet();
				for (Subnet subnet : lSubnet) {
					status = processSubnet(subnet, networkId, action, datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}

		return allStatus;
	}

	public boolean processAaiVnfcAndChildNodes(Vnfc vnfc, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String grandParententityType = Constants.VNFC_ENTITY;

		if (vnfc != null) {
			String headerEntityType = super.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.VNFC_ENTITY)) {
				status = processVnfc(vnfc, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String parentEntityType = "";
			String parentEntityId = "na";
			String cloudRegionTenant = "na";
			String pInterfaceName = "na";
			String lagInterfaceName = "na";
			String vlanInterface = "na";
			String grandParentEntityId = vnfc.getVnfcName();

			List<L3InterfaceIpv4AddressList> lIpv4 = vnfc.getL3InterfaceIpv4AddressList();
			status = syncL3InterfaceIpv4AddressList(lIpv4, parentEntityType, parentEntityId, grandParententityType,
					grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
					datetimestamp);
			allStatus = status ? allStatus : status;
			List<L3InterfaceIpv6AddressList> lIpv6 = vnfc.getL3InterfaceIpv6AddressList();
			status = syncL3InterfaceIpv6AddressList(lIpv6, parentEntityType, parentEntityId, grandParententityType,
					grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
					datetimestamp);
			allStatus = status ? allStatus : status;

			Cps cps = vnfc.getCps();
			status = processAaiCps(cps, grandParententityType, grandParentEntityId, action, datetimestamp);
			allStatus = status ? allStatus : status;

		}
		return allStatus;
	}

	public boolean processAaiCps(Cps cps, String grandParentEntityType, String grandParentEntityId, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (cps != null) {
			List<Cp> listCp = cps.getCp();
			if (listCp != null) {
				for (Cp cp : listCp) {
					status = processAaiCpAndChildNodes(cp, grandParentEntityType, grandParentEntityId, action,
							datetimestamp);
					allStatus = status ? allStatus : status;
				}
			}
		}

		return allStatus;
	}

	public boolean processAaiCpAndChildNodes(Cp cp, String grandParentEntityType, String grandParentEntityId,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String vlanInterface = "na";
		String headerEntityType = super.getHeaderEntityType();

		if (cp != null) {
			String parentEntityType = Constants.CP_ENTITY;

			if (headerEntityType.equalsIgnoreCase(Constants.VNFC_ENTITY)
					|| headerEntityType.equalsIgnoreCase(Constants.CP_ENTITY)) {
				status = processCp(cp, grandParentEntityId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}

			String parentEntityId = cp.getCpInstanceId();
			List<L3InterfaceIpv4AddressList> lIpv4 = cp.getL3InterfaceIpv4AddressList();
			status = syncL3InterfaceIpv4AddressList(lIpv4, parentEntityType, parentEntityId, grandParentEntityType,
					grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
					datetimestamp);
			allStatus = status ? allStatus : status;

			List<L3InterfaceIpv6AddressList> lIpv6 = cp.getL3InterfaceIpv6AddressList();
			status = syncL3InterfaceIpv6AddressList(lIpv6, parentEntityType, parentEntityId, grandParentEntityType,
					grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
					datetimestamp);
			allStatus = status ? allStatus : status;

		}
		return allStatus;
	}

	public boolean processAaiGenericVnfAndChildNodes(GenericVnf genericVnf, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String parentEntityType = Constants.GENERIC_VNF_ENTITY;
		String parentEntityId = genericVnf.getVnfId();

		if (super.getHeaderEntityType().equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)) {
			status = processGenericVnf(genericVnf, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LInterfaces lInterfaces = genericVnf.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
				lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;

		LagInterfaces lagInterfaces = genericVnf.getLagInterfaces();
		status = syncAaiLaginterfaceAndChildNodes(lagInterfaces, parentEntityType, parentEntityId, action,
				datetimestamp);
		if (!status)
			allStatus = status;

		VfModules vfModules = genericVnf.getVfModules();
		if (vfModules != null) {
			List<VfModule> vfModulesList = vfModules.getVfModule();
			for (VfModule vfModule : vfModulesList) {
				status = this.processVfModule(vfModule, parentEntityType, parentEntityId, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processAaiPserverAndChildNodes(Pserver pserver, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String parentEntityType = Constants.PSERVER_ENTITY;
		String parentEntityId = pserver.getHostname();
		String headerEntityType = this.getHeaderEntityType();

		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PSERVER_ENTITY)) {
			status = this.processPserver(pserver, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LInterfaces lInterfaces = pserver.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
				lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;

		LagInterfaces lagInterfaces = pserver.getLagInterfaces();
		status = syncAaiLaginterfaceAndChildNodes(lagInterfaces, parentEntityType, parentEntityId, action,
				datetimestamp);
		if (!status)
			allStatus = status;

		PInterfaces pInterfaces = pserver.getPInterfaces();
		status = syncAaiPinterfaceAndChildNodes(pInterfaces, parentEntityType, parentEntityId, action, datetimestamp);
		if (!status)
			allStatus = status;

		return allStatus;
	}

	public boolean processAaiPnfAndChildNodes(Pnf pnf, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = "na";
		String parentEntityType = Constants.PNF_ENTITY;
		String parentEntityId = pnf.getPnfName();

		if (super.getHeaderEntityType().equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.PNF_ENTITY)) {
			status = processPnf(pnf, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LInterfaces lInterfaces = pnf.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
				lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;

		LagInterfaces lagInterfaces = pnf.getLagInterfaces();
		status = syncAaiLaginterfaceAndChildNodes(lagInterfaces, parentEntityType, parentEntityId, action,
				datetimestamp);
		if (!status)
			allStatus = status;

		PInterfaces pInterfaces = pnf.getPInterfaces();
		status = syncAaiPinterfaceAndChildNodes(pInterfaces, parentEntityType, parentEntityId, action, datetimestamp);
		if (!status)
			allStatus = status;

		return allStatus;
	}

	public boolean processAaiVplsPeAndChildNodes(VplsPe vplsPe, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String parentEntityType = Constants.VPLS_PE_ENTITY;
		String parentEntityId = vplsPe.getEquipmentName();
		String headerEntityType = this.getHeaderEntityType();

		if (headerEntityType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.VPLS_PE_ENTITY)) {
			status = this.processVplsPe(vplsPe, action, datetimestamp);
			if (!status)
				allStatus = status;
		}

		LagInterfaces lagInterfaces = vplsPe.getLagInterfaces();
		status = syncAaiLaginterfaceAndChildNodes(lagInterfaces, parentEntityType, parentEntityId, action,
				datetimestamp);
		if (!status)
			allStatus = status;

		PInterfaces pInterfaces = vplsPe.getPInterfaces();
		status = syncAaiPinterfaceAndChildNodes(pInterfaces, parentEntityType, parentEntityId, action, datetimestamp);
		if (!status)
			allStatus = status;

		return allStatus;
	}

	public boolean syncAaiPinterfaceAndChildNodes(PInterfaces pInterfaces, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (pInterfaces != null) {
			List<PInterface> pIntList = pInterfaces.getPInterface();
			for (PInterface pInterface : pIntList) {
				status = processAaiPinterfaceAndChildNodes(pInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;
	}

	public boolean processAaiPinterfaceAndChildNodes(PInterface pInterface, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String lagInterfaceName = "na";
		String pInterfaceName = pInterface.getInterfaceName();
		String headerEntityType = this.getHeaderEntityType();
		if (headerEntityType.equalsIgnoreCase(Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.PSERVER_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.VPLS_PE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.P_INTERFACE_ENTITY)) {
			processPInterface(pInterface, parentEntityType, parentEntityId, action, datetimestamp);
		}

		LInterfaces lInterfaces = pInterface.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
				lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;

		SriovPfs sriovPfs = pInterface.getSriovPfs();
		if (sriovPfs != null) {
			List<SriovPf> lSriovPf = sriovPfs.getSriovPf();
			for (SriovPf sriovPf : lSriovPf) {
				status = this.processSriovPf(sriovPf, parentEntityType, parentEntityId, pInterfaceName, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}

		return allStatus;

	}

	public boolean syncAaiLaginterfaceAndChildNodes(LagInterfaces lagInterfaces, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (lagInterfaces != null) {
			List<LagInterface> lagIntList = lagInterfaces.getLagInterface();
			for (LagInterface lagInterface : lagIntList) {
				status = processAaiLaginterfaceAndChildNodes(lagInterface, parentEntityType, parentEntityId, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiLaginterfaceAndChildNodes(LagInterface lagInterface, String parentEntityType,
			String parentEntityId, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String cloudRegionTenant = "na";
		String pInterfaceName = "na";
		String lagInterfaceName = lagInterface.getInterfaceName();
		String headerEntityType = this.getHeaderEntityType();
		if (headerEntityType.equalsIgnoreCase(Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.PSERVER_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.VPLS_PE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.LAG_INTERFACE_ENTITY)) {
			this.processLagInterface(lagInterface, parentEntityType, parentEntityId, action, datetimestamp);
		}

		LInterfaces lInterfaces = lagInterface.getLInterfaces();
		status = processAaiLinterfaces(lInterfaces, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
				lagInterfaceName, action, datetimestamp);
		if (!status)
			allStatus = status;

		return allStatus;

	}

	public boolean processAaiLinterfaces(LInterfaces lInterfaces, String parentEntityType, String parentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String action,
			String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (lInterfaces != null) {
			List<LInterface> lIntList = lInterfaces.getLInterface();
			for (LInterface lInterface : lIntList) {
				status = processAaiLinterfaceAndChildNodes(lInterface, parentEntityType, parentEntityId,
						cloudRegionTenant, pInterfaceName, lagInterfaceName, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiLinterfaceAndChildNodes(LInterface lInterface, String parentEntityType,
			String parentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String intName = lInterface.getInterfaceName();
		String cloudOwner = "";
		String cloudRegionId = "";
		String tenantId = "";
		String vlanInterface = "na";
		String entityType = Constants.L_INTERFACE_ENTITY;
		String headerEntityType = this.getHeaderEntityType();
		if (headerEntityType.equalsIgnoreCase(Constants.PNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.PSERVER_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.VPLS_PE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.VSERVER_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.TENANT_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.CLOUD_REGION)
				|| headerEntityType.equalsIgnoreCase(Constants.LAG_INTERFACE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.P_INTERFACE_ENTITY)
				|| headerEntityType.equalsIgnoreCase(Constants.L_INTERFACE_ENTITY)) {
			processLinterface(lInterface, parentEntityType, parentEntityId, cloudRegionTenant, pInterfaceName,
					lagInterfaceName, action, datetimestamp);
		}

		SriovVfs sriovVfs = lInterface.getSriovVfs();
		if (sriovVfs != null) {
			if (cloudRegionTenant != null) {
				String[] tempArr = cloudRegionTenant.split("\\|");
				cloudOwner = tempArr[0];
				cloudRegionId = tempArr.length >= 2 ? tempArr[1] : "";
				tenantId = tempArr.length >= 3 ? tempArr[2] : "";
			}
			List<SriovVf> lSriovVf = sriovVfs.getSriovVf();
			if (lSriovVf != null) {
				for (SriovVf sriovVf : lSriovVf) {
					status = processSriovVf(sriovVf, entityType, intName, cloudOwner, cloudRegionId, tenantId,
							parentEntityType, parentEntityId, pInterfaceName, lagInterfaceName, action, datetimestamp);
					allStatus = status ? allStatus : status;
				}
			}
		}

		List<L3InterfaceIpv4AddressList> lIpv4 = lInterface.getL3InterfaceIpv4AddressList();
		status = syncL3InterfaceIpv4AddressList(lIpv4, entityType, intName, parentEntityType, parentEntityId,
				cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action, datetimestamp);
		allStatus = status ? allStatus : status;
		List<L3InterfaceIpv6AddressList> lIpv6 = lInterface.getL3InterfaceIpv6AddressList();
		status = syncL3InterfaceIpv6AddressList(lIpv6, entityType, intName, parentEntityType, parentEntityId,
				cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action, datetimestamp);
		allStatus = status ? allStatus : status;

		Vlans vlans = lInterface.getVlans();
		if (vlans != null) {
			List<Vlan> lVlan = vlans.getVlan();
			for (Vlan vlan : lVlan) {
				status = processAaiVlanAndChildNodes(vlan, intName, parentEntityType, parentEntityId, cloudRegionTenant,
						pInterfaceName, lagInterfaceName, action, datetimestamp);
				allStatus = status ? allStatus : status;
			}
		}
		return allStatus;
	}

	public boolean processAaiVlanAndChildNodes(Vlan vlan, String lInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		String headerEntityType = this.getHeaderEntityType();

		if (!(headerEntityType
				.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY)
				|| headerEntityType.equalsIgnoreCase(
						com.att.vcc.inventorycollector.util.Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY))) {
			status = processVlan(vlan, lInterfaceName, grandParentEntityType, grandParentEntityId, cloudRegionTenant,
					pInterfaceName, lagInterfaceName, action, datetimestamp);
			if (!status)
				allStatus = status;
		}
		String vlanInterface = vlan.getVlanInterface();
		List<L3InterfaceIpv4AddressList> lIpv4 = vlan.getL3InterfaceIpv4AddressList();
		status = syncL3InterfaceIpv4AddressList(lIpv4, Constants.L_INTERFACE_ENTITY, lInterfaceName,
				grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
				vlanInterface, action, datetimestamp);
		if (!status)
			allStatus = status;

		List<L3InterfaceIpv6AddressList> lIpv6 = vlan.getL3InterfaceIpv6AddressList();
		status = syncL3InterfaceIpv6AddressList(lIpv6, Constants.L_INTERFACE_ENTITY, lInterfaceName,
				grandParentEntityType, grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName,
				vlanInterface, action, datetimestamp);
		if (!status)
			allStatus = status;

		return allStatus;
	}

	public boolean syncL3InterfaceIpv4AddressList(List<L3InterfaceIpv4AddressList> lIpv4, String parentEntity,
			String lInterfaceName, String grandParentEntityType, String grandParentEntityId, String cloudRegionTenant,
			String pInterfaceName, String lagInterfaceName, String vlanInterface, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (lIpv4 != null) {
			for (L3InterfaceIpv4AddressList ipv4 : lIpv4) {
				status = processL3InterfaceIpv4AddressList(ipv4, parentEntity, lInterfaceName, grandParentEntityType,
						grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean syncL3InterfaceIpv6AddressList(List<L3InterfaceIpv6AddressList> lIpv6, String parentEntity,
			String lInterfaceName, String grandParentEntityType, String grandParentEntityId, String cloudRegionTenant,
			String pInterfaceName, String lagInterfaceName, String vlanInterface, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (lIpv6 != null) {
			for (L3InterfaceIpv6AddressList ipv6 : lIpv6) {
				status = processL3InterfaceIpv6AddressList(ipv6, parentEntity, lInterfaceName, grandParentEntityType,
						grandParentEntityId, cloudRegionTenant, pInterfaceName, lagInterfaceName, vlanInterface, action,
						datetimestamp);
				if (!status)
					allStatus = status;
			}
		}
		return allStatus;
	}

	public boolean processAaiModelAndChildNodes(Model model, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;

		if (model != null) {
			String headerEntityType = this.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.MODEL_ENTITY)) {
				status = processModel(model, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String modelInvariantId = model.getModelInvariantId();

			ModelVers modelVers = model.getModelVers();
			if (modelVers != null) {
				List<ModelVer> mvs = modelVers.getModelVer();
				for (ModelVer mv : mvs) {
					status = processModelVer(mv, modelInvariantId, action, datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}
		return allStatus;
	}

	public boolean processVceAndChildNodes(Vce vce, String action, String datetimestamp) {
		boolean allStatus = true;
		boolean status = true;
		if (vce != null) {
			String headerEntityType = this.getHeaderEntityType();
			if (headerEntityType.equalsIgnoreCase(Constants.VCE_ENTITY)) {
				status = processVce(vce, action, datetimestamp);
				if (!status)
					allStatus = status;
			}
			String vnfId = vce.getVnfId();
			PortGroups postGroups = vce.getPortGroups();
			if (postGroups != null) {
				List<PortGroup> lPostGroups = postGroups.getPortGroup();
				for (PortGroup portGroup : lPostGroups) {
					status = processPortGroup(portGroup, vnfId, action, datetimestamp);
					if (!status)
						allStatus = status;
				}
			}
		}
		return allStatus;
	}

	public boolean processAvailabilityZone(AvailabilityZone availabilityZone, String cloudOwner, String cloudRegionId,
			String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.AVAILABILITY_ZONE_ENTITY;
		String availabilityZoneName = Optional.ofNullable(availabilityZone.getAvailabilityZoneName()).orElse("");
		String hypervisorType = Optional.ofNullable(availabilityZone.getHypervisorType()).orElse("");
		String operationalStatus = Optional.ofNullable(availabilityZone.getOperationalStatus()).orElse("");
		String resourceversion = Optional.ofNullable(availabilityZone.getResourceVersion()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + availabilityZoneName;
		RelationshipList relList = availabilityZone.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				availabilityZoneName + "^" + hypervisorType + "^" + operationalStatus + "^" + resourceversion + "^"
						+ cloudOwner + "^" + cloudRegionId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	@Override
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
		String orchestrationdisabled = cloudregion.isOrchestrationDisabled() != null
				&& cloudregion.isOrchestrationDisabled() ? "Y" : "N";
		String inmaint = cloudregion.isInMaint() != null && cloudregion.isInMaint() ? "Y" : "N";

		String fromNodeId = cloudregion.getCloudRegionId();
		RelationshipList relList = cloudregion.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(cloudowner + "^" + cloudregionid + "^" + cloudtype + "^" + ownerdefinedtype + "^"
				+ cloudregionversion + "^" + identityurl + "^" + cloudzone + "^" + complexname + "^" + sriovautomation
				+ "^" + resourceversion + "^" + upgradecycle + "^" + orchestrationdisabled + "^" + inmaint + "^"
				+ datetimestamp, lRelationship, entityType, action, datetimestamp);
		return result;
	}

	@Override
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
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				physicalLocationId + "^" + dataCenterCode + "^" + complexName + "^" + identityurl + "^"
						+ resourceVersion + "^" + physicalLocationType + "^" + street1 + "^" + street2 + "^" + city
						+ "^" + state + "^" + postalCode + "^" + country + "^" + region + "^" + latitude + "^"
						+ longitude + "^" + elevation + "^" + lata + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processCp(Cp cp, String vnfcname, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.CP_ENTITY;
		String cpinstanceid = Optional.ofNullable(cp.getCpInstanceId()).orElse("");
		String portid = (cp.getPortId() == null) ? "" : cp.getPortId().toString();
		String resourceversion = Optional.ofNullable(cp.getResourceVersion()).orElse("");

		String fromNodeId = vnfcname + "|" + cp.getCpInstanceId();
		RelationshipList relList = cp.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				cpinstanceid + "^" + portid + "^" + resourceversion + "^" + vnfcname + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processCustomer(Customer customer, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.CUSTOMER_ENTITY;

		String globalcustomerid = Optional.ofNullable(customer.getGlobalCustomerId()).orElse("");
		String subscribername = Optional.ofNullable(customer.getSubscriberName()).orElse("");
		String resourceversion = Optional.ofNullable(customer.getResourceVersion()).orElse("");

		String fromNodeId = globalcustomerid;
		RelationshipList relList = customer.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(globalcustomerid + "^" + subscribername + "^" + resourceversion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processFlavor(Flavor flavor, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean result = false;

		String entityType = Constants.FLAVOR_ENTITY;
		String flavorid = Optional.ofNullable(flavor.getFlavorId()).orElse("");
		String flavorname = Optional.ofNullable(flavor.getFlavorName()).orElse("");
		String flavorvcpus = (flavor.getFlavorVcpus() == null) ? "" : flavor.getFlavorVcpus().toString();
		String flavorram = (flavor.getFlavorRam() == null) ? "" : flavor.getFlavorRam().toString();
		String flavordisk = (flavor.getFlavorDisk() == null) ? "" : flavor.getFlavorDisk().toString();
		String flavorephemeral = (flavor.getFlavorEphemeral() == null) ? "" : flavor.getFlavorEphemeral().toString();
		String flavorswap = Optional.ofNullable(flavor.getFlavorSwap()).orElse("");
		String flavorispublic = flavor.isFlavorIsPublic() != null && flavor.isFlavorIsPublic() ? "Y" : "N";
		String flavorselflink = Optional.ofNullable(flavor.getFlavorSelflink()).orElse("");
		String flavordisabled = flavor.isFlavorDisabled() != null && flavor.isFlavorDisabled() ? "Y" : "N";
		String resourceversion = Optional.ofNullable(flavor.getResourceVersion()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + flavorid;
		RelationshipList relList = flavor.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(flavorid + "^" + flavorname + "^" + flavorvcpus + "^" + flavorram + "^" + flavordisk
				+ "^" + flavorephemeral + "^" + flavorswap + "^" + flavorispublic + "^" + flavorselflink + "^"
				+ flavordisabled + "^" + resourceversion + "^" + cloudOwner + "^" + cloudRegionId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processForwarder(Forwarder forwarder, String forwardingPathId, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.FORWARDER_ENTITY;
		Integer sequence = forwarder.getSequence();
		String forwarderRole = Optional.ofNullable(forwarder.getForwarderRole()).orElse("");
		String resourceVersion = Optional.ofNullable(forwarder.getResourceVersion()).orElse("");

		String fromNodeId = forwardingPathId + "|" + sequence;
		RelationshipList relList = forwarder.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				sequence + "^" + forwarderRole + "^" + resourceVersion + "^" + forwardingPathId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processForwardingPath(ForwardingPath forwardingPath, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.FORWARDING_PATH_ENTITY;
		String forwardingPathId = Optional.ofNullable(forwardingPath.getForwardingPathId()).orElse("");
		String forwardingPathName = Optional.ofNullable(forwardingPath.getForwardingPathName()).orElse("");
		String resourceVersion = Optional.ofNullable(forwardingPath.getResourceVersion()).orElse("");
		String selflink = Optional.ofNullable(forwardingPath.getSelflink()).orElse("");

		RelationshipList relList = forwardingPath.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, forwardingPathId, relList, datetimestamp);

		result = storeEventToDB(forwardingPathId + "^" + forwardingPathName + "^" + selflink + "^" + resourceVersion
				+ "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	@Override
	public boolean processGenericVnf(GenericVnf gVnf, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.GENERIC_VNF_ENTITY;

		String vnfId = Optional.ofNullable(gVnf.getVnfId()).orElse("");
		String vnfName = Optional.ofNullable(gVnf.getVnfName()).orElse("");
		String vnfName2 = Optional.ofNullable(gVnf.getVnfName2()).orElse("");
		String vnfType = Optional.ofNullable(gVnf.getVnfType()).orElse("");
		String serviceId = Optional.ofNullable(gVnf.getServiceId()).orElse("");
		String regionalResourceZone = Optional.ofNullable(gVnf.getRegionalResourceZone()).orElse("");
		String provStatus = Optional.ofNullable(gVnf.getProvStatus()).orElse("");
		String operationalStatus = Optional.ofNullable(gVnf.getOperationalStatus()).orElse("");
		String equipmentRole = Optional.ofNullable(gVnf.getEquipmentRole()).orElse("");
		String orchestrationStatus = Optional.ofNullable(gVnf.getOrchestrationStatus()).orElse("");
		String heatStackId = Optional.ofNullable(gVnf.getHeatStackId()).orElse("");
		String msoCatalogKey = Optional.ofNullable(gVnf.getMsoCatalogKey()).orElse("");
		String managementOption = Optional.ofNullable(gVnf.getManagementOption()).orElse("");
		String ipv4OamAddress = Optional.ofNullable(gVnf.getIpv4OamAddress()).orElse("");
		String ipv4Loopback0Address = Optional.ofNullable(gVnf.getIpv4Loopback0Address()).orElse("");
		String nmLanV6Address = Optional.ofNullable(gVnf.getNmLanV6Address()).orElse("");
		String managementV6Address = Optional.ofNullable(gVnf.getManagementV6Address()).orElse("");
		String vcpu = (gVnf.getVcpu() == null) ? "" : gVnf.getVcpu().toString();
		String vcpuUnits = Optional.ofNullable(gVnf.getVcpuUnits()).orElse("");
		String vmemory = (gVnf.getVmemory() == null) ? "" : gVnf.getVmemory().toString();
		String vmemoryUnits = Optional.ofNullable(gVnf.getVmemoryUnits()).orElse("");
		String vdisk = (gVnf.getVdisk() == null) ? "" : gVnf.getVdisk().toString();
		String vdiskUnits = Optional.ofNullable(gVnf.getVdiskUnits()).orElse("");
		String inMaint = gVnf.isInMaint() ? "Y" : "N";
		String isClosedLoopDisabled = gVnf.isIsClosedLoopDisabled() ? "Y" : "N";
		String resourceVersion = Optional.ofNullable(gVnf.getResourceVersion()).orElse("");
		String summarystatus = Optional.ofNullable(gVnf.getSummaryStatus()).orElse("");
		String encryptedaccessflag = gVnf.isEncryptedAccessFlag() != null && gVnf.isEncryptedAccessFlag() ? "Y" : "N";
		String modelInvariantId = Optional.ofNullable(gVnf.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(gVnf.getModelVersionId()).orElse("");
		String modelCustomizationId = Optional.ofNullable(gVnf.getModelCustomizationId()).orElse("");
		String widgetModelId = Optional.ofNullable(gVnf.getWidgetModelId()).orElse("");
		String widgetModelVersion = Optional.ofNullable(gVnf.getWidgetModelVersion()).orElse("");
		String asNumber = Optional.ofNullable(gVnf.getAsNumber()).orElse("");
		String regionalResourceSubzone = Optional.ofNullable(gVnf.getRegionalResourceSubzone()).orElse("");
		String nfType = Optional.ofNullable(gVnf.getNfType()).orElse("");
		String nfRole = Optional.ofNullable(gVnf.getNfRole()).orElse("");
		String nfFunction = Optional.ofNullable(gVnf.getNfFunction()).orElse("");
		String nfNamingCode = Optional.ofNullable(gVnf.getNfNamingCode()).orElse("");
		String selflink = Optional.ofNullable(gVnf.getSelflink()).orElse("");
		String ipv4OamGatewayAddress = Optional.ofNullable(gVnf.getIpv4OamGatewayAddress()).orElse("");
		String ipv4OamGatewayAddressPrefixLength = (gVnf.getIpv4OamGatewayAddressPrefixLength() == null) ? ""
				: gVnf.getIpv4OamGatewayAddressPrefixLength().toString();
		String vlanIdOuter = (gVnf.getVlanIdOuter() == null) ? "" : gVnf.getVlanIdOuter().toString();
		String nmProfileName = Optional.ofNullable(gVnf.getNmProfileName()).orElse("");

		RelationshipList relList = gVnf.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, vnfId, relList, datetimestamp);

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

		return result;
	}

	public boolean processImage(Image image, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean result = false;

		String entityType = Constants.IMAGE_ENTITY;
		String imageid = Optional.ofNullable(image.getImageId()).orElse("");
		String imagename = Optional.ofNullable(image.getImageName()).orElse("");
		String imagearchitecture = Optional.ofNullable(image.getImageArchitecture()).orElse("");
		String imageosdistro = Optional.ofNullable(image.getImageOsDistro()).orElse("");
		String imageosversion = Optional.ofNullable(image.getImageOsVersion()).orElse("");
		String application = Optional.ofNullable(image.getApplication()).orElse("");
		String applicationvendor = Optional.ofNullable(image.getApplicationVendor()).orElse("");
		String applicationversion = Optional.ofNullable(image.getApplicationVersion()).orElse("");
		String imageselflink = Optional.ofNullable(image.getImageSelflink()).orElse("");
		String resourceversion = Optional.ofNullable(image.getResourceVersion()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + imageid;
		RelationshipList relList = image.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				imageid + "^" + imagename + "^" + imagearchitecture + "^" + imageosdistro + "^" + imageosversion + "^"
						+ application + "^" + applicationvendor + "^" + applicationversion + "^" + imageselflink + "^"
						+ resourceversion + "^" + cloudOwner + "^" + cloudRegionId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processL3InterfaceIpv4AddressList(L3InterfaceIpv4AddressList ipv4, String parentEntityType,
			String parentInterfaceName, String grandParentEntityType, String grandParentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String vlanInterface,
			String action, String datetimestamp) {
		boolean result = true;
		String entityType = Constants.L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY;
		String l3InterfaceIpv4Address = Optional.ofNullable(ipv4.getL3InterfaceIpv4Address()).orElse("");
		String l3InterfaceIpv4PrefixLength = (ipv4.getL3InterfaceIpv4PrefixLength() == null) ? ""
				: ipv4.getL3InterfaceIpv4PrefixLength().toString();
		String vlanIdInner = (ipv4.getVlanIdInner() == null) ? "" : ipv4.getVlanIdInner().toString();
		String vlanIdOuter = (ipv4.getVlanIdOuter() == null) ? "" : ipv4.getVlanIdOuter().toString();
		String isFloating = ipv4.isIsFloating() != null && ipv4.isIsFloating() ? "Y" : "N";
		String resourceVersion = Optional.ofNullable(ipv4.getResourceVersion()).orElse("");
		String neutronNetworkId = Optional.ofNullable(ipv4.getNeutronNetworkId()).orElse("");
		String neutronNetworkSubId = Optional.ofNullable(ipv4.getNeutronSubnetId()).orElse("");

		String fromNodeId = ipv4.getL3InterfaceIpv4Address();
		RelationshipList relList = ipv4.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(l3InterfaceIpv4Address + "^" + l3InterfaceIpv4PrefixLength + "^" + vlanIdInner + "^"
				+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion + "^" + neutronNetworkId + "^"
				+ neutronNetworkSubId + "^" + parentEntityType + "^" + parentInterfaceName + "^" + grandParentEntityType
				+ "^" + grandParentEntityId + "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName
				+ "^" + vlanInterface + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processL3InterfaceIpv6AddressList(L3InterfaceIpv6AddressList ipv6, String parentEntityType,
			String parentInterfaceName, String grandParentEntityType, String grandParentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String vlanInterface,
			String action, String datetimestamp) {
		boolean result = true;
		String entityType = Constants.L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY;
		String l3InterfaceIpv6Address = Optional.ofNullable(ipv6.getL3InterfaceIpv6Address()).orElse("");
		String l3InterfaceIpv6PrefixLength = (ipv6.getL3InterfaceIpv6PrefixLength() == null) ? ""
				: ipv6.getL3InterfaceIpv6PrefixLength().toString();
		String vlanIdInner = (ipv6.getVlanIdInner() == null) ? "" : ipv6.getVlanIdInner().toString();
		String vlanIdOuter = (ipv6.getVlanIdOuter() == null) ? "" : ipv6.getVlanIdOuter().toString();
		String isFloating = ipv6.isIsFloating() != null && ipv6.isIsFloating() ? "Y" : "N";
		String resourceVersion = Optional.ofNullable(ipv6.getResourceVersion()).orElse("");
		String neutronNetworkId = Optional.ofNullable(ipv6.getNeutronNetworkId()).orElse("");
		String neutronNetworkSubId = Optional.ofNullable(ipv6.getNeutronSubnetId()).orElse("");

		String fromNodeId = ipv6.getL3InterfaceIpv6Address();
		RelationshipList relList = ipv6.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(l3InterfaceIpv6Address + "^" + l3InterfaceIpv6PrefixLength + "^" + vlanIdInner + "^"
				+ vlanIdOuter + "^" + isFloating + "^" + resourceVersion + "^" + neutronNetworkId + "^"
				+ neutronNetworkSubId + "^" + parentEntityType + "^" + parentInterfaceName + "^" + grandParentEntityType
				+ "^" + grandParentEntityId + "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName
				+ "^" + vlanInterface + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	@Override
	public boolean processL3Network(L3Network l3network, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.L3_NETWORK_ENTITY;
		String networkId = Optional.ofNullable(l3network.getNetworkId()).orElse("");
		String networkName = Optional.ofNullable(l3network.getNetworkName()).orElse("");
		String networkType = Optional.ofNullable(l3network.getNetworkType()).orElse("");
		String networkRole = Optional.ofNullable(l3network.getNetworkRole()).orElse("");
		String networkTechnology = Optional.ofNullable(l3network.getNetworkTechnology()).orElse("");
		String neutronNetworkId = Optional.ofNullable(l3network.getNeutronNetworkId()).orElse("");
		String isBoundToVpn = l3network.isIsBoundToVpn() ? "Y" : "N";
		String serviceId = Optional.ofNullable(l3network.getServiceId()).orElse("");
		String networkRoleInstance = (l3network.getNetworkRoleInstance() == null) ? ""
				: l3network.getNetworkRoleInstance().toString();
		String resourceVersion = Optional.ofNullable(l3network.getResourceVersion()).orElse("");
		String orchestrationStatus = Optional.ofNullable(l3network.getOrchestrationStatus()).orElse("");
		String heatStackId = Optional.ofNullable(l3network.getHeatStackId()).orElse("");
		String msoCatalogKey = Optional.ofNullable(l3network.getMsoCatalogKey()).orElse("");
		String contrailNetworkFqdn = Optional.ofNullable(l3network.getContrailNetworkFqdn()).orElse("");
		String modelInvariantId = Optional.ofNullable(l3network.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(l3network.getModelVersionId()).orElse("");
		String modelCustomizationId = Optional.ofNullable(l3network.getModelCustomizationId()).orElse("");
		String widgetModelId = Optional.ofNullable(l3network.getWidgetModelId()).orElse("");
		String widgetModelVersion = Optional.ofNullable(l3network.getWidgetModelVersion()).orElse("");
		String physicalnetworkname = Optional.ofNullable(l3network.getPhysicalNetworkName()).orElse("");
		String isprovidernetwork = l3network.isIsProviderNetwork() ? "Y" : "N";
		String issharednetwork = l3network.isIsSharedNetwork() ? "Y" : "N";
		String isexternalnetwork = l3network.isIsExternalNetwork() ? "Y" : "N";
		String operationalStatus = Optional.ofNullable(l3network.getOperationalStatus()).orElse("");
		String selflink = Optional.ofNullable(l3network.getSelflink()).orElse("");

		RelationshipList relList = l3network.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, networkId, relList, datetimestamp);

		result = storeEventToDB(
				networkId + "^" + networkName + "^" + networkType + "^" + networkRole + "^" + networkTechnology + "^"
						+ neutronNetworkId + "^" + isBoundToVpn + "^" + serviceId + "^" + networkRoleInstance + "^"
						+ resourceVersion + "^" + orchestrationStatus + "^" + heatStackId + "^" + msoCatalogKey + "^"
						+ contrailNetworkFqdn + "^" + modelInvariantId + "^" + modelVersionId + "^"
						+ modelCustomizationId + "^" + widgetModelId + "^" + widgetModelVersion + "^"
						+ physicalnetworkname + "^" + isprovidernetwork + "^" + issharednetwork + "^"
						+ isexternalnetwork + "^" + operationalStatus + "^" + selflink + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processLagInterface(LagInterface lagInterface, String parentEntityType, String parentEntityId,
			String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.LAG_INTERFACE_ENTITY;

		if (lagInterface != null) {

			String interfacename = Optional.ofNullable(lagInterface.getInterfaceName()).orElse("");
			String interfacedescription = Optional.ofNullable(lagInterface.getInterfaceDescription()).orElse("");
			String resourceversion = Optional.ofNullable(lagInterface.getResourceVersion()).orElse("");
			String speedvalue = Optional.ofNullable(lagInterface.getSpeedValue()).orElse("");
			String speedunits = Optional.ofNullable(lagInterface.getSpeedUnits()).orElse("");
			String interfaceid = Optional.ofNullable(lagInterface.getInterfaceId()).orElse("");
			String interfacerole = Optional.ofNullable(lagInterface.getInterfaceRole()).orElse("");
			String provstatus = Optional.ofNullable(lagInterface.getProvStatus()).orElse("");
			String inmaint = lagInterface.isInMaint() ? "Y" : "N";

			String fromNodeId = parentEntityId + "|" + interfacename;
			RelationshipList relList = lagInterface.getRelationshipList();
			ArrayList<String> lRelationship = null;
			if (relList != null)
				lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

			result = storeEventToDB(
					interfacename + "^" + interfacedescription + "^" + resourceversion + "^" + speedvalue + "^"
							+ speedunits + "^" + interfaceid + "^" + interfacerole + "^" + provstatus + "^" + inmaint
							+ "^" + parentEntityType + "^" + parentEntityId + "^" + datetimestamp,
					lRelationship, entityType, action, datetimestamp);
			if (!result) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_PROCESS_LAG_INTERFACE_TOFILE_ERROR,
						lagInterface.getInterfaceName());
				return result;
			}

		}
		return result;
	}

	public boolean processLagLink(LagLink lagLink, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.LAG_LINK_ENTITY;

		String linkname = Optional.ofNullable(lagLink.getLinkName()).orElse("");
		String resourceversion = Optional.ofNullable(lagLink.getResourceVersion()).orElse("");

		RelationshipList relList = lagLink.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, linkname, relList, datetimestamp);

		result = storeEventToDB(linkname + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType,
				action, datetimestamp);

		return result;
	}

	@Override
	public boolean processLineOfBusiness(LineOfBusiness lineofbusiness, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.LINE_OF_BUSINESS_ENTITY;
		String lineofbusinessname = lineofbusiness.getLineOfBusinessName() != null
				? lineofbusiness.getLineOfBusinessName()
				: "";
		String resourceversion = lineofbusiness.getResourceVersion() != null ? lineofbusiness.getResourceVersion() : "";

		RelationshipList relList = lineofbusiness.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, lineofbusinessname, relList, datetimestamp);

		result = storeEventToDB(lineofbusinessname + "^" + resourceversion + "^" + datetimestamp, lRelationship,
				entityType, action, datetimestamp);
		return result;
	}

	public boolean processLinterface(LInterface lInterface, String parentEntityType, String parentEntityId,
			String cloudRegionTenant, String pInterfaceName, String lagInterfaceName, String action,
			String datetimestamp) {
		boolean result = false;
		String entityType = Constants.L_INTERFACE_ENTITY;
		String interfaceName = Optional.ofNullable(lInterface.getInterfaceName()).orElse("");
		String interfaceRole = Optional.ofNullable(lInterface.getInterfaceRole()).orElse("");
		String v6WanLinkIp = Optional.ofNullable(lInterface.getV6WanLinkIp()).orElse("");
		String selflink = Optional.ofNullable(lInterface.getSelflink()).orElse("");
		String interfaceId = Optional.ofNullable(lInterface.getInterfaceId()).orElse("");
		String macaddr = Optional.ofNullable(lInterface.getMacaddr()).orElse("");
		String networkName = Optional.ofNullable(lInterface.getNetworkName()).orElse("");
		String resourceVersion = Optional.ofNullable(lInterface.getResourceVersion()).orElse("");
		String managementOption = Optional.ofNullable(lInterface.getManagementOption()).orElse("");
		String interfaceDescription = Optional.ofNullable(lInterface.getInterfaceDescription()).orElse("");
		String isPortMirrored = lInterface.isIsPortMirrored() ? "Y" : "N";
		String isInMaint = lInterface.isInMaint() ? "Y" : "N";
		String provStatus = Optional.ofNullable(lInterface.getProvStatus()).orElse("");
		String isipunnumbered = lInterface.isIsIpUnnumbered() ? "Y" : "N";
		String allowedAddressPairs = Optional.ofNullable(lInterface.getAllowedAddressPairs()).orElse("");

		String fromNodeId = "";
		fromNodeId = interfaceName + "|" + parentEntityId + "|" + cloudRegionTenant + "|" + pInterfaceName + "|"
				+ lagInterfaceName;

		RelationshipList relList = lInterface.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				interfaceName + "^" + interfaceRole + "^" + v6WanLinkIp + "^" + selflink + "^" + interfaceId + "^"
						+ macaddr + "^" + networkName + "^" + resourceVersion + "^" + managementOption + "^"
						+ interfaceDescription + "^" + isPortMirrored + "^" + isInMaint + "^" + provStatus + "^"
						+ isipunnumbered + "^" + parentEntityType + "^" + parentEntityId + "^" + allowedAddressPairs
						+ "^" + cloudRegionTenant + "^" + pInterfaceName + "^" + lagInterfaceName + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	@Override
	public boolean processLogicalLink(LogicalLink logicalLink, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.LOGICAL_LINK_ENTITY;
		String linkname = Optional.ofNullable(logicalLink.getLinkName()).orElse("");
		String linktype = Optional.ofNullable(logicalLink.getLinkType()).orElse("");
		String speedvalue = Optional.ofNullable(logicalLink.getSpeedValue()).orElse("");
		String speedunits = Optional.ofNullable(logicalLink.getSpeedUnits()).orElse("");
		String ipversion = Optional.ofNullable(logicalLink.getIpVersion()).orElse("");
		String routingprotocol = Optional.ofNullable(logicalLink.getRoutingProtocol()).orElse("");
		String resourceversion = Optional.ofNullable(logicalLink.getResourceVersion()).orElse("");
		String modelInvariantId = Optional.ofNullable(logicalLink.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(logicalLink.getModelVersionId()).orElse("");
		String widgetModelId = Optional.ofNullable(logicalLink.getWidgetModelId()).orElse("");
		String widgetModelVersion = Optional.ofNullable(logicalLink.getWidgetModelVersion()).orElse("");
		String operationalStatus = Optional.ofNullable(logicalLink.getOperationalStatus()).orElse("");
		String provStatus = Optional.ofNullable(logicalLink.getProvStatus()).orElse("");
		String linkRole = Optional.ofNullable(logicalLink.getLinkRole()).orElse("");
		String linkName2 = Optional.ofNullable(logicalLink.getLinkName2()).orElse("");
		String linkId = Optional.ofNullable(logicalLink.getLinkId()).orElse("");
		String circuitId = Optional.ofNullable(logicalLink.getCircuitId()).orElse("");
		String purpose = Optional.ofNullable(logicalLink.getPurpose()).orElse("");
		String inmaint = logicalLink.isInMaint() ? "Y" : "N";

		RelationshipList relList = logicalLink.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, linkname, relList, datetimestamp);

		result = storeEventToDB(linkname + "^" + inmaint + "^" + linktype + "^" + speedvalue + "^" + speedunits + "^"
				+ ipversion + "^" + routingprotocol + "^" + resourceversion + "^" + modelInvariantId + "^"
				+ modelVersionId + "^" + widgetModelId + "^" + widgetModelVersion + "^" + operationalStatus + "^"
				+ provStatus + "^" + linkRole + "^" + linkName2 + "^" + linkId + "^" + circuitId + "^" + purpose + "^"
				+ datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processModel(Model model, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.MODEL_ENTITY;

		String modelInvariantId = Optional.ofNullable(model.getModelInvariantId()).orElse("");
		String modelType = Optional.ofNullable(model.getModelType()).orElse("");
		String resourceVersion = Optional.ofNullable(model.getResourceVersion()).orElse("");

		RelationshipList relList = model.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, modelInvariantId, relList, datetimestamp);

		result = storeEventToDB(modelInvariantId + "^" + modelType + "^" + resourceVersion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processModelVer(ModelVer modelVer, String modelInvariantId, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.MODEL_VER_ENTITY;

		String modelVersionId = Optional.ofNullable(modelVer.getModelVersionId()).orElse("");
		String modelName = Optional.ofNullable(modelVer.getModelName()).orElse("");
		String modelVersion = Optional.ofNullable(modelVer.getModelVersion()).orElse("");
		String modelDescription = Optional.ofNullable(modelVer.getModelDescription()).orElse("");
		String distributionStatus = Optional.ofNullable(modelVer.getDistributionStatus()).orElse("");
		String resourceVersion = Optional.ofNullable(modelVer.getResourceVersion()).orElse("");

		String fromNodeId = modelInvariantId + "|" + modelVersionId;
		RelationshipList relList = modelVer.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				modelVersionId + "^" + modelName + "^" + modelVersion + "^" + modelDescription + "^"
						+ distributionStatus + "^" + resourceVersion + "^" + modelInvariantId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processNosServer(NosServer nosServer, String tenantId, String cloudOwner, String cloudRegionId,
			String action, String datetimestamp) {
		boolean result = true;

		String entityType = Constants.NOS_SERVER_ENTITY;
		String nosServerId = Optional.ofNullable(nosServer.getNosServerId()).orElse("");
		String nosserverName = Optional.ofNullable(nosServer.getNosServerName()).orElse("");
		String vendor = Optional.ofNullable(nosServer.getVendor()).orElse("");
		String provStatus = Optional.ofNullable(nosServer.getProvStatus()).orElse("");
		String nosserverselfLink = Optional.ofNullable(nosServer.getNosServerSelflink()).orElse("");
		String isInMaint = nosServer.isInMaint() ? "Y" : "N";
		String resourceversion = Optional.ofNullable(nosServer.getResourceVersion()).orElse("");

		String entityData = cloudOwner + "^" + cloudRegionId + "^" + tenantId + "^" + nosServerId + "^" + nosserverName
				+ "^" + vendor + "^" + provStatus + "^" + nosserverselfLink + "^" + isInMaint + "^" + resourceversion
				+ "^" + datetimestamp;

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + tenantId + "|" + nosServer.getNosServerId();
		RelationshipList relList = nosServer.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(entityData, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processOamNetwork(OamNetwork oamNetwork, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean result = false;
		String entityType = Constants.OAM_NETWORK_ENTITY;
		String networkuuid = Optional.ofNullable(oamNetwork.getNetworkUuid()).orElse("");
		String networkname = Optional.ofNullable(oamNetwork.getNetworkName()).orElse("");
		long cvlantag = oamNetwork.getCvlanTag();
		String ipv4oamgtwyaddr = Optional.ofNullable(oamNetwork.getIpv4OamGatewayAddress()).orElse("");
		String ipv4oamgtwyaddrprefixlen = (oamNetwork.getIpv4OamGatewayAddressPrefixLength() == null) ? ""
				: oamNetwork.getIpv4OamGatewayAddressPrefixLength().toString();
		String resourceversion = Optional.ofNullable(oamNetwork.getResourceVersion()).orElse("");

		RelationshipList relList = oamNetwork.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, networkuuid, relList, datetimestamp);

		result = storeEventToDB(networkuuid + "^" + networkname + "^" + cvlantag + "^" + ipv4oamgtwyaddr + "^"
				+ ipv4oamgtwyaddrprefixlen + "^" + resourceversion + "^" + cloudOwner + "^" + cloudRegionId + "^"
				+ datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processOperationalEnvironment(OperationalEnvironment operationalEnvironment, String action,
			String datetimestamp) {
		boolean result = false;
		String entityType = Constants.OPERATIONAL_ENVIRONMENT_ENTITY;

		String opEnvId = Optional.ofNullable(operationalEnvironment.getOperationalEnvironmentId()).orElse("");
		String opEnvName = Optional.ofNullable(operationalEnvironment.getOperationalEnvironmentName()).orElse("");
		String opEnvType = Optional.ofNullable(operationalEnvironment.getOperationalEnvironmentType()).orElse("");
		String opEnvStatus = Optional.ofNullable(operationalEnvironment.getOperationalEnvironmentStatus()).orElse("");
		String tenantContext = Optional.ofNullable(operationalEnvironment.getTenantContext()).orElse("");
		String workloadContext = Optional.ofNullable(operationalEnvironment.getWorkloadContext()).orElse("");
		String resourceVersion = Optional.ofNullable(operationalEnvironment.getResourceVersion()).orElse("");

		RelationshipList relList = operationalEnvironment.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, opEnvId, relList, datetimestamp);

		result = storeEventToDB(
				opEnvId + "^" + opEnvName + "^" + opEnvType + "^" + opEnvStatus + "^" + tenantContext + "^"
						+ workloadContext + "^" + resourceVersion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	@Override
	public boolean processOwningEntity(OwningEntity owningentity, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.OWNING_ENTITY_ENTITY;
		String owningentityid = Optional.ofNullable(owningentity.getOwningEntityId()).orElse("");
		String owningentityname = Optional.ofNullable(owningentity.getOwningEntityName()).orElse("");
		String resourceversion = Optional.ofNullable(owningentity.getResourceVersion()).orElse("");

		RelationshipList relList = owningentity.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, owningentityid, relList, datetimestamp);

		result = storeEventToDB(owningentityid + "^" + owningentityname + "^" + resourceversion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	@Override
	public boolean processPhysicalLink(PhysicalLink physicalLink, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PHYSICAL_LINK_ENTITY;
		String linkname = Optional.ofNullable(physicalLink.getLinkName()).orElse("");
		String speedvalue = Optional.ofNullable(physicalLink.getSpeedValue()).orElse("");
		String speedunits = Optional.ofNullable(physicalLink.getSpeedUnits()).orElse("");
		String circuitid = Optional.ofNullable(physicalLink.getCircuitId()).orElse("");
		String dualmode = Optional.ofNullable(physicalLink.getDualMode()).orElse("");
		String resourceversion = Optional.ofNullable(physicalLink.getResourceVersion()).orElse("");
		String managementOption = Optional.ofNullable(physicalLink.getManagementOption()).orElse("");
		String serviceprovidername = Optional.ofNullable(physicalLink.getServiceProviderName()).orElse("");
		String serviceProviderBWUpvalue = (physicalLink.getServiceProviderBandwidthUpValue() == null) ? ""
				: physicalLink.getServiceProviderBandwidthUpValue().toString();
		String serviceProviderBWUpUnits = Optional.ofNullable(physicalLink.getServiceProviderBandwidthUpUnits())
				.orElse("");
		String serviceProviderBWDownvalue = (physicalLink.getServiceProviderBandwidthDownValue() == null) ? ""
				: physicalLink.getServiceProviderBandwidthDownValue().toString();
		String serviceProviderBWDownUnits = Optional.ofNullable(physicalLink.getServiceProviderBandwidthDownUnits())
				.orElse("");

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

	public boolean processPInterface(PInterface pInterface, String parentEntityType, String parentEntityId,
			String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.P_INTERFACE_ENTITY;
		String interfacename = Optional.ofNullable(pInterface.getInterfaceName()).orElse("");
		String interfacename2 = Optional.ofNullable(pInterface.getInterfaceName2()).orElse("");
		String speedvalue = Optional.ofNullable(pInterface.getSpeedValue()).orElse("");
		String speedunits = Optional.ofNullable(pInterface.getSpeedUnits()).orElse("");
		String portdescription = Optional.ofNullable(pInterface.getPortDescription()).orElse("");
		String resourceversion = Optional.ofNullable(pInterface.getResourceVersion()).orElse("");
		String equipmentidentifier = Optional.ofNullable(pInterface.getEquipmentIdentifier()).orElse("");
		String interfacerole = Optional.ofNullable(pInterface.getInterfaceRole()).orElse("");
		String interfacetype = Optional.ofNullable(pInterface.getInterfaceType()).orElse("");
		String provstatus = Optional.ofNullable(pInterface.getProvStatus()).orElse("");
		String inmaint = pInterface.isInMaint() ? "Y" : "N";
		String invstatus = Optional.ofNullable(pInterface.getInvStatus()).orElse("");
		String selflink = Optional.ofNullable(pInterface.getSelflink()).orElse("");

		String fromNodeId = parentEntityId + "|" + interfacename;
		RelationshipList relList = pInterface.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(interfacename + "^" +interfacename2 + "^" + speedvalue + "^" + speedunits + "^" + portdescription + "^"
				+ equipmentidentifier + "^" + interfacerole + "^" + interfacetype + "^" + provstatus + "^"
				+ resourceversion + "^" + inmaint + "^" + invstatus + "^" + parentEntityId + "^" + parentEntityType
				+ "^" + selflink + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);

		return result;
	}

	@Override
	public boolean processPlatform(Platform platform, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PLATFORM_ENTITY;
		String platformname = Optional.ofNullable(platform.getPlatformName()).orElse("");
		String resourceversion = Optional.ofNullable(platform.getResourceVersion()).orElse("");

		RelationshipList relList = platform.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, platformname, relList, datetimestamp);

		result = storeEventToDB(platformname + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType,
				action, datetimestamp);
		return result;
	}

	@Override
	public boolean processProject(Project project, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PROJECT_ENTITY;
		String projectname = Optional.ofNullable(project.getProjectName()).orElse("");
		String resourceversion = Optional.ofNullable(project.getResourceVersion()).orElse("");

		RelationshipList relList = project.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, projectname, relList, datetimestamp);

		result = storeEventToDB(projectname + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType,
				action, datetimestamp);
		return result;
	}

	@Override
	public boolean processPnf(Pnf pnf, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PNF_ENTITY;
		String pnfname = Optional.ofNullable(pnf.getPnfName()).orElse("");
		String pnfname2 = Optional.ofNullable(pnf.getPnfName()).orElse("");
		String pnfname2source = Optional.ofNullable(pnf.getPnfName2Source()).orElse("");
		String pnfid = Optional.ofNullable(pnf.getPnfId()).orElse("");
		String nfnamingcode = pnf.getNfNamingCode() != null ? pnf.getNfNamingCode() : "";
		String equiptype = Optional.ofNullable(pnf.getEquipType()).orElse("");
		String equipvendor = Optional.ofNullable(pnf.getEquipVendor()).orElse("");
		String equipmodel = Optional.ofNullable(pnf.getEquipModel()).orElse("");
		String managementoption = Optional.ofNullable(pnf.getManagementOption()).orElse("");
		String orchestrationstatus = Optional.ofNullable(pnf.getOrchestrationStatus()).orElse("");
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
		String nfrole = Optional.ofNullable(pnf.getNfRole()).orElse("");
		String selflink = Optional.ofNullable(pnf.getSelflink()).orElse("");
		
		RelationshipList relList = pnf.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, pnfname, relList, datetimestamp);

		result = storeEventToDB(pnfname + "^" + pnfname2 + "^" + pnfname2source + "^" + pnfid + "^" + nfnamingcode + "^" + equiptype + "^"
				+ equipvendor + "^" + equipmodel + "^" + managementoption + "^" + orchestrationstatus + "^" + ipaddressv4oam + "^" + swversion + "^"
				+ inmaint + "^" + frameid + "^" + serialnumber + "^" + ipaddressv4loopback0 + "^" + ipaddressv6loopback0
				+ "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam + "^" + invstatus + "^"
				+ resourceversion + "^" + provstatus + "^" + nfrole + "^" + selflink + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processPortGroup(PortGroup portGroup, String vnfId, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PORT_GROUP_ENTITY;
		String interfaceid = Optional.ofNullable(portGroup.getInterfaceId()).orElse("");
		String neutronnetworkid = Optional.ofNullable(portGroup.getNeutronNetworkId()).orElse("");
		String neutronnetworkname = Optional.ofNullable(portGroup.getNeutronNetworkName()).orElse("");
		String interfacerole = Optional.ofNullable(portGroup.getInterfaceRole()).orElse("");
		String resourceversion = Optional.ofNullable(portGroup.getResourceVersion()).orElse("");
		String portgroupid = Optional.ofNullable(portGroup.getPortGroupId()).orElse("");
		String portgroupname = Optional.ofNullable(portGroup.getPortGroupName()).orElse("");
		String switchname = Optional.ofNullable(portGroup.getSwitchName()).orElse("");
		String orchestrationstatus = Optional.ofNullable(portGroup.getOrchestrationStatus()).orElse("");
		String heatstackid = Optional.ofNullable(portGroup.getHeatStackId()).orElse("");
		String msocatalogkey = Optional.ofNullable(portGroup.getMsoCatalogKey()).orElse("");

		String fromNodeId = vnfId + "|" + interfaceid;
		RelationshipList relList = portGroup.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(interfaceid + "^" + neutronnetworkid + "^" + neutronnetworkname + "^" + interfacerole
				+ "^" + resourceversion + "^" + portgroupid + "^" + portgroupname + "^" + switchname + "^"
				+ orchestrationstatus + "^" + heatstackid + "^" + msocatalogkey + "^" + vnfId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;

	}

	@Override
	public boolean processPserver(Pserver pserver, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.PSERVER_ENTITY;
		String hostname = Optional.ofNullable(pserver.getHostname()).orElse("");
		String ptniiEquipName = Optional.ofNullable(pserver.getPtniiEquipName()).orElse("");
		String numberOfCpus = (pserver.getNumberOfCpus() == null) ? "" : pserver.getNumberOfCpus().toString();
		String diskInGigabytes = (pserver.getDiskInGigabytes() == null) ? "" : pserver.getDiskInGigabytes().toString();
		String ramInMegabytes = (pserver.getRamInMegabytes() == null) ? "" : pserver.getRamInMegabytes().toString();
		String equipType = Optional.ofNullable(pserver.getEquipType()).orElse("");
		String equipVendor = Optional.ofNullable(pserver.getEquipVendor()).orElse("");
		String equipModel = Optional.ofNullable(pserver.getEquipModel()).orElse("");
		String fqdn = Optional.ofNullable(pserver.getFqdn()).orElse("");
		String pserverSelflink = Optional.ofNullable(pserver.getPserverSelflink()).orElse("");
		String ipv4OamAddress = Optional.ofNullable(pserver.getIpv4OamAddress()).orElse("");
		String serialNumber = Optional.ofNullable(pserver.getSerialNumber()).orElse("");
		String pserverId = Optional.ofNullable(pserver.getPserverId()).orElse("");
		String ipaddressv4loopback0 = Optional.ofNullable(pserver.getIpaddressV4Loopback0()).orElse("");
		String ipaddressv6loopback0 = Optional.ofNullable(pserver.getIpaddressV6Loopback0()).orElse("");
		String ipaddressv4aim = Optional.ofNullable(pserver.getIpaddressV4Aim()).orElse("");
		String ipaddressv6aim = Optional.ofNullable(pserver.getIpaddressV6Aim()).orElse("");
		String ipaddressv6oam = Optional.ofNullable(pserver.getIpaddressV6Oam()).orElse("");
		String invstatus = Optional.ofNullable(pserver.getInvStatus()).orElse("");
		String internetTopology = Optional.ofNullable(pserver.getInternetTopology()).orElse("");
		String inMaint = pserver.isInMaint() ? "Y" : "N";
		String resourceVersion = Optional.ofNullable(pserver.getResourceVersion()).orElse("");
		String pservername2 = Optional.ofNullable(pserver.getPserverName2()).orElse("");
		String purpose = Optional.ofNullable(pserver.getPurpose()).orElse("");
		String provstatus = Optional.ofNullable(pserver.getProvStatus()).orElse("");
		String managementoption = Optional.ofNullable(pserver.getManagementOption()).orElse("");
		String hostProfile = Optional.ofNullable(pserver.getHostProfile()).orElse("");

		RelationshipList relList = pserver.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, hostname, relList, datetimestamp);

		result = storeEventToDB(
				hostname + "^" + ptniiEquipName + "^" + numberOfCpus + "^" + diskInGigabytes + "^" + ramInMegabytes
						+ "^" + equipType + "^" + equipVendor + "^" + equipModel + "^" + fqdn + "^" + pserverSelflink
						+ "^" + ipv4OamAddress + "^" + serialNumber + "^" + ipaddressv4loopback0 + "^"
						+ ipaddressv6loopback0 + "^" + ipaddressv4aim + "^" + ipaddressv6aim + "^" + ipaddressv6oam
						+ "^" + invstatus + "^" + pserverId + "^" + inMaint + "^" + internetTopology + "^"
						+ resourceVersion + "^" + pservername2 + "^" + purpose + "^" + provstatus + "^"
						+ managementoption + "^" + hostProfile + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processService(Service service, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.SERVICE_ENTITY;
		String serviceId = Optional.ofNullable(service.getServiceId()).orElse("");
		String serviceDescription = Optional.ofNullable(service.getServiceDescription()).orElse("");
		String serviceSelflink = Optional.ofNullable(service.getServiceSelflink()).orElse("");
		String resourceVersion = Optional.ofNullable(service.getResourceVersion()).orElse("");

		RelationshipList relList = service.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, serviceId, relList, datetimestamp);

		result = storeEventToDB(serviceId + "^" + serviceDescription + "^" + serviceSelflink + "^" + resourceVersion
				+ "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processServiceCapability(ServiceCapability serviceCapability, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.SERVICE_CAPABILITY_ENTITY;
		String serviceType = Optional.ofNullable(serviceCapability.getServiceType()).orElse("");
		String vnfType = Optional.ofNullable(serviceCapability.getVnfType()).orElse("");
		String resourceVersion = Optional.ofNullable(serviceCapability.getResourceVersion()).orElse("");

		RelationshipList relList = serviceCapability.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, serviceType, relList, datetimestamp);

		result = storeEventToDB(serviceType + "^" + vnfType + "^" + resourceVersion + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processAllottedResource(AllottedResource allottedResource, String globalCustomerId,
			String serviceType, String serviceInstanceId, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.ALLOTTED_RESOURCE_ENTITY;
		String id = Optional.ofNullable(allottedResource.getId()).orElse("");
		String description = Optional.ofNullable(allottedResource.getDescription()).orElse("");
		String selflink = Optional.ofNullable(allottedResource.getSelflink()).orElse("");
		String modelInvariantId = Optional.ofNullable(allottedResource.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(allottedResource.getModelVersionId()).orElse("");
		String resourceVersion = Optional.ofNullable(allottedResource.getResourceVersion()).orElse("");
		String orchestrationStatus = Optional.ofNullable(allottedResource.getOrchestrationStatus()).orElse("");
		String operationalStatus = Optional.ofNullable(allottedResource.getOperationalStatus()).orElse("");
		String type = Optional.ofNullable(allottedResource.getType()).orElse("");
		String role = Optional.ofNullable(allottedResource.getRole()).orElse("");

		String fromNodeId = globalCustomerId + "|" + serviceType + "|" + serviceInstanceId + "|" + id;
		RelationshipList relList = allottedResource.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				globalCustomerId + "^" + serviceType + "^" + serviceInstanceId + "^" + id + "^" + description + "^"
						+ selflink + "^" + modelInvariantId + "^" + modelVersionId + "^" + resourceVersion + "^"
						+ orchestrationStatus + "^" + operationalStatus + "^" + type + "^" + role + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processServiceInstance(ServiceInstance serviceInstance, String globalcustomerid, String serviceType,
			String action, String datetimestamp) {
		boolean result = false;

		String entityType = "service-instance";
		String serviceinstanceid = Optional.ofNullable(serviceInstance.getServiceInstanceId()).orElse("");
		String serviceinstancename = Optional.ofNullable(serviceInstance.getServiceInstanceName()).orElse("");
		String modelInvariantId = Optional.ofNullable(serviceInstance.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(serviceInstance.getModelVersionId()).orElse("");
		String widgetModelId = Optional.ofNullable(serviceInstance.getWidgetModelId()).orElse("");
		String widgetModelVersion = Optional.ofNullable(serviceInstance.getWidgetModelVersion()).orElse("");
		String bandwidthtotal = Optional.ofNullable(serviceInstance.getBandwidthTotal()).orElse("");
		String bandwidthupwan1 = Optional.ofNullable(serviceInstance.getBandwidthUpWan1()).orElse("");
		String bandwidthdownwan1 = Optional.ofNullable(serviceInstance.getBandwidthDownWan1()).orElse("");
		String bandwidthupwan2 = Optional.ofNullable(serviceInstance.getBandwidthUpWan2()).orElse("");
		String bandwidthdownwan2 = Optional.ofNullable(serviceInstance.getBandwidthDownWan2()).orElse("");
		String vhnportalurl = Optional.ofNullable(serviceInstance.getVhnPortalUrl()).orElse("");
		String serviceinstancelocationid = Optional.ofNullable(serviceInstance.getServiceInstanceLocationId())
				.orElse("");
		String resourceversion = Optional.ofNullable(serviceInstance.getResourceVersion()).orElse("");
		String selflink = Optional.ofNullable(serviceInstance.getSelflink()).orElse("");
		String orchestrationstatus = Optional.ofNullable(serviceInstance.getOrchestrationStatus()).orElse("");
		String servicerole = Optional.ofNullable(serviceInstance.getServiceRole()).orElse("");
		String environmentcontext = Optional.ofNullable(serviceInstance.getEnvironmentContext()).orElse("");
		String workloadcontext = Optional.ofNullable(serviceInstance.getWorkloadContext()).orElse("");

		String fromNodeId = globalcustomerid + "|" + serviceType + "|" + serviceinstanceid;
		RelationshipList relList = serviceInstance.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(serviceinstanceid + "^" + serviceinstancename + "^" + modelInvariantId + "^"
				+ modelVersionId + "^" + widgetModelId + "^" + widgetModelVersion + "^" + bandwidthtotal + "^"
				+ bandwidthupwan1 + "^" + bandwidthdownwan1 + "^" + bandwidthupwan2 + "^" + bandwidthdownwan2 + "^"
				+ vhnportalurl + "^" + serviceinstancelocationid + "^" + resourceversion + "^" + selflink + "^"
				+ orchestrationstatus + "^" + serviceType + "^" + servicerole + "^" + environmentcontext + "^"
				+ workloadcontext + "^" + globalcustomerid + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	public boolean processServiceSubscription(ServiceSubscription serviceSubscription, String globalCustomerId,
			String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.SERVICE_SUBSCRIPTION_ENTITY;
		String servicetype = Optional.ofNullable(serviceSubscription.getServiceType()).orElse("");
		String resourceversion = Optional.ofNullable(serviceSubscription.getResourceVersion()).orElse("");

		String fromNodeId = globalCustomerId + "|" + servicetype;
		RelationshipList relList = serviceSubscription.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);
		result = storeEventToDB(servicetype + "^" + resourceversion + "^" + globalCustomerId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processSriovPf(SriovPf sriovpf, String grandParentEntityType, String grantParentEntityId,
			String interfaceName, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.SRIOV_PF_ENTITY;
		String pfpciid = Optional.ofNullable(sriovpf.getPfPciId()).orElse("");
		String resourceversion = Optional.ofNullable(sriovpf.getResourceVersion()).orElse("");

		String fromNodeId = grantParentEntityId + "|" + interfaceName + "|" + pfpciid;
		RelationshipList relList = sriovpf.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(pfpciid + "^" + resourceversion + "^" + grandParentEntityType + "^"
				+ grantParentEntityId + "^" + interfaceName + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	public boolean processSriovVf(SriovVf sriovvf, String parentEntityType, String parentEntityId, String cloudOwner,
			String cloudRegionId, String tenantId, String grandParentEntityType, String grandParentEntityId,
			String pInterfaceName, String lagInterfaceName, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.SRIOV_VF_ENTITY;
		String pciid = Optional.ofNullable(sriovvf.getPciId()).orElse("");
		String vfvlanfilter = Optional.ofNullable(sriovvf.getVfVlanFilter()).orElse("");
		String vfmacfilter = Optional.ofNullable(sriovvf.getVfMacFilter()).orElse("");
		String vfvlanstrip = sriovvf.isVfVlanStrip() != null && sriovvf.isVfVlanStrip() ? "Y" : "N";
		String vfvlanantispoofcheck = sriovvf.isVfVlanAntiSpoofCheck() != null && sriovvf.isVfVlanAntiSpoofCheck() ? "Y"
				: "N";
		String vfmacantispoofcheck = sriovvf.isVfMacAntiSpoofCheck() != null && sriovvf.isVfMacAntiSpoofCheck() ? "Y"
				: "N";
		String vfmirrors = Optional.ofNullable(sriovvf.getVfMirrors()).orElse("");
		String vfbroadcastallow = sriovvf.isVfBroadcastAllow() != null && sriovvf.isVfBroadcastAllow() ? "Y" : "N";
		String vfunknownmulticastallow = sriovvf.isVfUnknownMulticastAllow() != null
				&& sriovvf.isVfUnknownMulticastAllow() ? "Y" : "N";
		String vfunknownunicastallow = sriovvf.isVfUnknownUnicastAllow() != null && sriovvf.isVfUnknownUnicastAllow()
				? "Y"
				: "N";
		String vfinsertstag = sriovvf.isVfInsertStag() != null && sriovvf.isVfInsertStag() ? "Y" : "N";
		String vflinkstatus = Optional.ofNullable(sriovvf.getVfLinkStatus()).orElse("");
		String resourceversion = Optional.ofNullable(sriovvf.getResourceVersion()).orElse("");
		String neutronnetworkid = Optional.ofNullable(sriovvf.getNeutronNetworkId()).orElse("");

		String fromNodeId = sriovvf.getPciId();
		RelationshipList relList = sriovvf.getRelationshipList();
		ArrayList<String> lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				pciid + "^" + vfvlanfilter + "^" + vfmacfilter + "^" + vfvlanstrip + "^" + vfvlanantispoofcheck + "^"
						+ vfmacantispoofcheck + "^" + vfmirrors + "^" + vfbroadcastallow + "^" + vfunknownmulticastallow
						+ "^" + vfunknownunicastallow + "^" + vfinsertstag + "^" + vflinkstatus + "^" + resourceversion
						+ "^" + neutronnetworkid + "^" + parentEntityType + "^" + parentEntityId + "^" + cloudOwner
						+ "^" + cloudRegionId + "^" + tenantId + "^" + grandParentEntityType + "^" + grandParentEntityId
						+ "^" + pInterfaceName + "^" + lagInterfaceName + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processSubnet(Subnet subnet, String networkId, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.SUBNET_ENTITY;

		String subnetId = Optional.ofNullable(subnet.getSubnetId()).orElse("");
		String neutronSubnetId = Optional.ofNullable(subnet.getNeutronSubnetId()).orElse("");
		String gatewayAddress = Optional.ofNullable(subnet.getGatewayAddress()).orElse("");
		String networkStartAddress = Optional.ofNullable(subnet.getNetworkStartAddress()).orElse("");
		String cidrMask = Optional.ofNullable(subnet.getCidrMask()).orElse("");
		String ipVersion = Optional.ofNullable(subnet.getIpVersion()).orElse("");
		String orchestrationStatus = Optional.ofNullable(subnet.getOrchestrationStatus()).orElse("");
		String dhcpEnabled = subnet.isDhcpEnabled() ? "Y" : "N";
		String dhcpStart = Optional.ofNullable(subnet.getDhcpStart()).orElse("");
		String dhcpEnd = Optional.ofNullable(subnet.getDhcpEnd()).orElse("");
		String resourceVersion = Optional.ofNullable(subnet.getResourceVersion()).orElse("");
		String subnetRole = Optional.ofNullable(subnet.getSubnetRole()).orElse("");
		String ipAssignmentDirection = Optional.ofNullable(subnet.getIpAssignmentDirection()).orElse("");
		String subnetSequence = (subnet.getSubnetSequence() == null) ? "" : subnet.getSubnetSequence().toString();
		String subnetName = Optional.ofNullable(subnet.getSubnetName()).orElse("");

		String relatedFrom = entityType;
		String fromNodeId = networkId + "|" + subnetId;
		RelationshipList relList = subnet.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				subnetId + "^" + subnetName + "^" + neutronSubnetId + "^" + gatewayAddress + "^" + networkStartAddress
						+ "^" + cidrMask + "^" + ipVersion + "^" + orchestrationStatus + "^" + dhcpEnabled + "^"
						+ dhcpStart + "^" + dhcpEnd + "^" + resourceVersion + "^" + networkId + "^" + subnetRole + "^"
						+ ipAssignmentDirection + "^" + subnetSequence + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processTenant(Tenant tenant, String cloudOwner, String cloudRegionId, String action,
			String datetimestamp) {
		boolean result = false;
		String entityType = Constants.TENANT_ENTITY;
		String tenantId = Optional.ofNullable(tenant.getTenantId()).orElse("");
		String tenantName = Optional.ofNullable(tenant.getTenantName()).orElse("");
		String resourceVersion = Optional.ofNullable(tenant.getResourceVersion()).orElse("");
		String tenantContext = Optional.ofNullable(tenant.getTenantContext()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + tenantId;
		RelationshipList relList = tenant.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(tenantId + "^" + tenantName + "^" + tenantContext + "^" + resourceVersion + "^"
				+ cloudOwner + "^" + cloudRegionId + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	@Override
	public boolean processVce(Vce vce, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.VCE_ENTITY;
		String vnfId = Optional.ofNullable(vce.getVnfId()).orElse("");
		String vnfName = Optional.ofNullable(vce.getVnfName()).orElse("");
		String vnfName2 = Optional.ofNullable(vce.getVnfName2()).orElse("");
		String vnfType = Optional.ofNullable(vce.getVnfType()).orElse("");
		String serviceId = Optional.ofNullable(vce.getServiceId()).orElse("");
		String regionalResourceZone = Optional.ofNullable(vce.getRegionalResourceZone()).orElse("");
		String provStatus = Optional.ofNullable(vce.getProvStatus()).orElse("");
		String operationalStatus = Optional.ofNullable(vce.getOperationalStatus()).orElse("");
		String equipmentRole = Optional.ofNullable(vce.getEquipmentRole()).orElse("");
		String orchestrationStatus = Optional.ofNullable(vce.getOrchestrationStatus()).orElse("");
		String heatStackId = Optional.ofNullable(vce.getHeatStackId()).orElse("");
		String msoCatalogKey = Optional.ofNullable(vce.getMsoCatalogKey()).orElse("");
		String vpeId = Optional.ofNullable(vce.getVpeId()).orElse("");
		String v6VceWanAddress = Optional.ofNullable(vce.getV6VceWanAddress()).orElse("");
		String ipv4OamAddress = Optional.ofNullable(vce.getIpv4OamAddress()).orElse("");
		String resourceVersion = Optional.ofNullable(vce.getResourceVersion()).orElse("");
		String ipv4Loopback0Address = Optional.ofNullable(vce.getIpv4Loopback0Address()).orElse("");

		RelationshipList relList = vce.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, vnfId, relList, datetimestamp);

		result = storeEventToDB(
				vnfId + "^" + vnfName + "^" + vnfName2 + "^" + vnfType + "^" + serviceId + "^" + regionalResourceZone
						+ "^" + provStatus + "^" + operationalStatus + "^" + equipmentRole + "^" + orchestrationStatus
						+ "^" + heatStackId + "^" + msoCatalogKey + "^" + vpeId + "^" + v6VceWanAddress + "^"
						+ ipv4OamAddress + "^" + resourceVersion + "^" + ipv4Loopback0Address + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	public boolean processVfModule(VfModule vfModule, String parentEntityType, String parentEntityId, String action,
			String datetimestamp) {
		boolean result = false;

		String entityType = Constants.VF_MODULE_ENTITY;
		String vfModuleId = Optional.ofNullable(vfModule.getVfModuleId()).orElse("");
		String vfModuleName = Optional.ofNullable(vfModule.getVfModuleName()).orElse("");
		String modelInvariantId = Optional.ofNullable(vfModule.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(vfModule.getModelVersionId()).orElse("");
		String modelCustomizationId = Optional.ofNullable(vfModule.getModelCustomizationId()).orElse("");
		String widgetModelId = Optional.ofNullable(vfModule.getWidgetModelId()).orElse("");
		String widgetModelVersion = Optional.ofNullable(vfModule.getWidgetModelVersion()).orElse("");
		String heatStackId = Optional.ofNullable(vfModule.getHeatStackId()).orElse("");
		String isBaseVfModule = vfModule.isIsBaseVfModule() ? "Y" : "N";
		String orchestrationStatus = Optional.ofNullable(vfModule.getOrchestrationStatus()).orElse("");
		String resourceVersion = Optional.ofNullable(vfModule.getResourceVersion()).orElse("");
		String contrailfqdn = Optional.ofNullable(vfModule.getContrailServiceInstanceFqdn()).orElse("");
		String moduleIndex = (vfModule.getModuleIndex() == null) ? "" : vfModule.getModuleIndex().toString();
		String selflink = Optional.ofNullable(vfModule.getSelflink()).orElse("");

		RelationshipList relList = vfModule.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, vfModuleId, relList, datetimestamp);

		result = storeEventToDB(vfModuleId + "^" + vfModuleName + "^" + modelInvariantId + "^" + modelVersionId + "^"
				+ modelCustomizationId + "^" + widgetModelId + "^" + widgetModelVersion + "^" + heatStackId + "^"
				+ isBaseVfModule + "^" + orchestrationStatus + "^" + resourceVersion + "^" + contrailfqdn + "^"
				+ moduleIndex + "^" + selflink + "^" + parentEntityType + "^" + parentEntityId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processCloudVIPIPV4List(VipIpv4AddressList vipipv4addresslist, String cloudowner,
			String cloudregionid, String action, String datetimestamp) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_INSIDE_BLOCK_DEBUG,
				"processCloudVIPIPV4List");

		boolean result = false;
		String entityType = Constants.CLOUD_VIP_IPV4_ADDRESS_LIST_ENTITY;
		String vipipv4address = Optional.ofNullable(vipipv4addresslist.getVipIpv4Address()).orElse("");
		String vipipv4prefixlength = (vipipv4addresslist.getVipIpv4PrefixLength() == null) ? ""
				: vipipv4addresslist.getVipIpv4PrefixLength().toString();
		String vlanidinner = (vipipv4addresslist.getVlanIdInner() == null) ? ""
				: vipipv4addresslist.getVlanIdInner().toString();
		String vlanidouter = (vipipv4addresslist.getVlanIdOuter() == null) ? ""
				: vipipv4addresslist.getVlanIdOuter().toString();
		String isfloating = vipipv4addresslist.isIsFloating() != null && vipipv4addresslist.isIsFloating() ? "Y" : "N";
		String resourceversion = Optional.ofNullable(vipipv4addresslist.getResourceVersion()).orElse("");
		String neutronnetworkid = Optional.ofNullable(vipipv4addresslist.getNeutronNetworkId()).orElse("");
		String neutronsubnetid = Optional.ofNullable(vipipv4addresslist.getNeutronSubnetId()).orElse("");

		String relatedFrom = Constants.VIP_IPV4_ADDRESS_LIST_ENTITY;
		String fromNodeId = cloudowner + "|" + cloudregionid + "|" + vipipv4address;
		RelationshipList relList = vipipv4addresslist.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(vipipv4address + "^" + vipipv4prefixlength + "^" + vlanidinner + "^" + vlanidouter + "^"
				+ isfloating + "^" + resourceversion + "^" + neutronnetworkid + "^" + neutronsubnetid + "^" + cloudowner
				+ "^" + cloudregionid + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processCloudVIPIPV6List(VipIpv6AddressList vipipv6addresslist, String cloudowner,
			String cloudregionid, String action, String datetimestamp) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_INSIDE_BLOCK_DEBUG,
				"processCloudVIPIPV6List");

		boolean result = false;
		String entityType = Constants.CLOUD_VIP_IPV6_ADDRESS_LIST_ENTITY;
		String vipipv6address = Optional.ofNullable(vipipv6addresslist.getVipIpv6Address()).orElse("");
		String vipipv6prefixlength = (vipipv6addresslist.getVipIpv6PrefixLength() == null) ? ""
				: vipipv6addresslist.getVipIpv6PrefixLength().toString();
		String vlanidinner = (vipipv6addresslist.getVlanIdInner() == null) ? ""
				: vipipv6addresslist.getVlanIdInner().toString();
		String vlanidouter = (vipipv6addresslist.getVlanIdOuter() == null) ? ""
				: vipipv6addresslist.getVlanIdOuter().toString();
		String isfloating = vipipv6addresslist.isIsFloating() != null && vipipv6addresslist.isIsFloating() ? "Y" : "N";
		String resourceversion = Optional.ofNullable(vipipv6addresslist.getResourceVersion()).orElse("");
		String neutronnetworkid = Optional.ofNullable(vipipv6addresslist.getNeutronNetworkId()).orElse("");
		String neutronsubnetid = Optional.ofNullable(vipipv6addresslist.getNeutronSubnetId()).orElse("");

		String relatedFrom = Constants.VIP_IPV6_ADDRESS_LIST_ENTITY;
		String fromNodeId = cloudowner + "|" + cloudregionid + "|" + vipipv6address;
		RelationshipList relList = vipipv6addresslist.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(relatedFrom, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(vipipv6address + "^" + vipipv6prefixlength + "^" + vlanidinner + "^" + vlanidouter + "^"
				+ isfloating + "^" + resourceversion + "^" + neutronnetworkid + "^" + neutronsubnetid + "^" + cloudowner
				+ "^" + cloudregionid + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processVirtualDataCenter(VirtualDataCenter virtualDataCenter, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.VIRTUAL_DATA_CENTER_ENTITY;

		String vdcid = Optional.ofNullable(virtualDataCenter.getVdcId()).orElse("");
		String vdcname = Optional.ofNullable(virtualDataCenter.getVdcName()).orElse("");
		String resourceversion = Optional.ofNullable(virtualDataCenter.getResourceVersion()).orElse("");

		RelationshipList relList = virtualDataCenter.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, vdcid, relList, datetimestamp);

		result = storeEventToDB(vdcid + "^" + vdcname + "^" + resourceversion + "^" + datetimestamp, lRelationship,
				entityType, action, datetimestamp);
		return result;
	}

	public boolean processVlan(Vlan vlan, String lInterfaceName, String grandParentEntityType,
			String grandParentEntityId, String cloudRegionTenant, String pInterfaceName, String lagInterfaceName,
			String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.VLAN_ENTITY;
		String vlaninterface = Optional.ofNullable(vlan.getVlanInterface()).orElse("");
		String vlanidinner = (vlan.getVlanIdInner() == null) ? "" : vlan.getVlanIdInner().toString();
		String vlanidouter = (vlan.getVlanIdOuter() == null) ? "" : vlan.getVlanIdOuter().toString();
		String resourceversion = Optional.ofNullable(vlan.getResourceVersion()).orElse("");
		String speedvalue = Optional.ofNullable(vlan.getSpeedValue()).orElse("");
		String speedunits = Optional.ofNullable(vlan.getSpeedUnits()).orElse("");
		String vlanDesc = Optional.ofNullable(vlan.getVlanDescription()).orElse("");
		String backdoorConnection = Optional.ofNullable(vlan.getBackdoorConnection()).orElse("");
		String vpnKey = Optional.ofNullable(vlan.getVpnKey()).orElse("");
		String orchestrationStatus = Optional.ofNullable(vlan.getOrchestrationStatus()).orElse("");
		String inMaint = vlan.isInMaint() ? "Y" : "N";
		String provStatus = Optional.ofNullable(vlan.getProvStatus()).orElse("");
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

	public boolean processVnfc(Vnfc vnfc, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.VNFC_ENTITY;
		String vnfcName = Optional.ofNullable(vnfc.getVnfcName()).orElse("");
		String nfcNamingCode = Optional.ofNullable(vnfc.getNfcNamingCode()).orElse("");
		String nfcFunction = Optional.ofNullable(vnfc.getNfcFunction()).orElse("");
		String provStatus = Optional.ofNullable(vnfc.getProvStatus()).orElse("");
		String orchestrationStatus = Optional.ofNullable(vnfc.getOrchestrationStatus()).orElse("");
		String resourceVersion = Optional.ofNullable(vnfc.getResourceVersion()).orElse("");
		String inMaint = vnfc.isInMaint() ? "Y" : "N";
		String ipaddressv4OamVip = Optional.ofNullable(vnfc.getIpaddressV4OamVip()).orElse("");
		String isClosedLoopDisabled = vnfc.isIsClosedLoopDisabled() ? "Y" : "N";
		String groupnotation = Optional.ofNullable(vnfc.getGroupNotation()).orElse("");
		String modelInvariantId = Optional.ofNullable(vnfc.getModelInvariantId()).orElse("");
		String modelVersionId = Optional.ofNullable(vnfc.getModelVersionId()).orElse("");
		String modelCustomizationId = Optional.ofNullable(vnfc.getModelCustomizationId()).orElse("");

		RelationshipList relList = vnfc.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, vnfcName, relList, datetimestamp);

		result = storeEventToDB(vnfcName + "^" + nfcNamingCode + "^" + nfcFunction + "^" + provStatus + "^"
				+ orchestrationStatus + "^" + resourceVersion + "^" + inMaint + "^" + ipaddressv4OamVip + "^"
				+ isClosedLoopDisabled + "^" + groupnotation + "^" + modelInvariantId + "^" + modelVersionId + "^"
				+ modelCustomizationId + "^" + datetimestamp, lRelationship, entityType, action, datetimestamp);
		return result;
	}

	public boolean processVnfImage(VnfImage vnfImage, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.VNF_IMAGE_ENTITY;

		String attuuid = Optional.ofNullable(vnfImage.getVnfImageUuid()).orElse("");
		String application = Optional.ofNullable(vnfImage.getApplication()).orElse("");
		String applicationvendor = Optional.ofNullable(vnfImage.getApplicationVendor()).orElse("");
		String applicationversion = Optional.ofNullable(vnfImage.getApplicationVersion()).orElse("");
		String selflink = Optional.ofNullable(vnfImage.getSelflink()).orElse("");
		String resourceversion = Optional.ofNullable(vnfImage.getResourceVersion()).orElse("");

		RelationshipList relList = vnfImage.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, attuuid, relList, datetimestamp);

		result = storeEventToDB(attuuid + "^" + application + "^" + applicationvendor + "^" + applicationversion + "^"
				+ selflink + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	public boolean processVolume(Volume volume, String cloudOwner, String cloudRegionId, String tenantId,
			String vserverId, String action, String datetimestamp) {
		boolean result = false;
		String entityType = Constants.VOLUME_ENTITY;
		String volumeid = Optional.of(volume.getVolumeId()).orElse("");
		String volumeselflink = Optional.ofNullable(volume.getVolumeSelflink()).orElse("");
		String resourceversion = Optional.ofNullable(volume.getResourceVersion()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + tenantId + "|" + vserverId + "|" + volumeid;
		RelationshipList relList = volume.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(
				volumeid + "^" + volumeselflink + "^" + resourceversion + "^" + cloudOwner + "^" + cloudRegionId + "^"
						+ tenantId + "^" + vserverId + "^" + datetimestamp,
				lRelationship, entityType, action, datetimestamp);

		return result;
	}

	@Override
	public boolean processVplsPe(VplsPe vplspe, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.VPLS_PE_ENTITY;
		String equipmentname = Optional.ofNullable(vplspe.getEquipmentName()).orElse("");
		String provstatus = Optional.ofNullable(vplspe.getProvStatus()).orElse("");
		String ipv4oamaddress = Optional.ofNullable(vplspe.getIpv4OamAddress()).orElse("");
		String equipmentrole = Optional.ofNullable(vplspe.getEquipmentName()).orElse("");
		String vlanidouter = (vplspe.getVlanIdOuter() == null) ? "" : vplspe.getVlanIdOuter().toString();
		String resourceversion = Optional.ofNullable(vplspe.getResourceVersion()).orElse("");

		RelationshipList relList = vplspe.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, equipmentname, relList, datetimestamp);

		result = storeEventToDB(equipmentname + "^" + provstatus + "^" + ipv4oamaddress + "^" + equipmentrole + "^"
				+ vlanidouter + "^" + resourceversion + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}

	public boolean processVserver(Vserver vserver, String cloudOwner, String cloudRegionId, String tenantId,
			String action, String datetimestamp) {

		boolean result = false;
		String entityType = Constants.VSERVER_ENTITY;
		String vserverId = Optional.ofNullable(vserver.getVserverId()).orElse("");
		String vserverName = Optional.ofNullable(vserver.getVserverName()).orElse("");
		String vserverName2 = Optional.ofNullable(vserver.getVserverName2()).orElse("");
		String provStatus = Optional.ofNullable(vserver.getProvStatus()).orElse("");
		String selfLink = Optional.ofNullable(vserver.getVserverSelflink()).orElse("");
		String isInMaint = vserver.isInMaint() ? "Y" : "N";
		String isClosedLoopDisabled = vserver.isIsClosedLoopDisabled() ? "Y" : "N";
		String resourceversion = Optional.ofNullable(vserver.getResourceVersion()).orElse("");

		String fromNodeId = cloudOwner + "|" + cloudRegionId + "|" + tenantId + "|" + vserverId;
		RelationshipList relList = vserver.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, fromNodeId, relList, datetimestamp);

		result = storeEventToDB(vserverId + "^" + vserverName + "^" + vserverName2 + "^" + provStatus + "^" + selfLink
				+ "^" + isInMaint + "^" + isClosedLoopDisabled + "^" + resourceversion + "^" + tenantId + "^"
				+ cloudOwner + "^" + cloudRegionId + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);

		return result;
	}

	@Override
	public boolean processZone(Zone zone, String action, String datetimestamp) {
		boolean result = false;

		String entityType = Constants.ZONE_ENTITY;
		String zoneId = Optional.ofNullable(zone.getZoneId()).orElse("");
		String zoneName = Optional.ofNullable(zone.getZoneName()).orElse("");
		String designType = Optional.ofNullable(zone.getDesignType()).orElse("");
		String zoneContext = Optional.ofNullable(zone.getZoneContext()).orElse("");
		String status = Optional.ofNullable(zone.getStatus()).orElse("");
		String inMaint = zone.isInMaint() != null && zone.isInMaint() ? "Y" : "N";
		String resourceVersion = Optional.ofNullable(zone.getResourceVersion()).orElse("");

		RelationshipList relList = zone.getRelationshipList();
		ArrayList<String> lRelationship = null;
		if (relList != null)
			lRelationship = processrelationships(entityType, zoneId, relList, datetimestamp);

		result = storeEventToDB(zoneId + "^" + zoneName + "^" + designType + "^" + zoneContext + "^" + status + "^"
				+ resourceVersion + "^" + inMaint + "^" + datetimestamp, lRelationship, entityType, action,
				datetimestamp);
		return result;
	}
}