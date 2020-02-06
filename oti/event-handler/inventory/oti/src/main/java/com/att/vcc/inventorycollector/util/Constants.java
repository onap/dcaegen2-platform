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

package com.att.vcc.inventorycollector.util;

import java.util.Calendar;

import com.att.vcc.inventorycollector.EventUtil;

public final class Constants {
	
	// config file related constants
	public static final String FILE_EXTENSION_TEXT = ".txt";
	public static final String FILE_EXTENSION_JSON = ".json";
	public static final String FILE_EXTENSION_GZ = ".gz";
	public static final String FILE_SEPARATOR = "/";
	public static final String ENCODING = "UTF8";
	public static final int WEEKLY_CRONJOB_DAY = Calendar.SUNDAY;
	public static final String AAI_EVENT = "AAI-EVENT";
	public static final String NARAD_EVENT = "NARAD-EVENT";

	// AAI & NARAD Entities
	public static final String AVAILABILITY_ZONE_ENTITY = "availability-zone";
	public static final String ALLOTTED_RESOURCE_ENTITY = "allotted-resource";
	public static final String CABLE_ENTITY = "cable";
	public static final String CARD_ENTITY = "card";
	public static final String CARD_SLOT_ENTITY = "card-slot";
	public static final String CHASSIS_ENTITY = "chassis";
	public static final String CLOUD_REGION_ENTITY = "cloud-region";
	public static final String COMPLEX_ENTITY = "complex";
	public static final String CP_ENTITY = "cp";
	public static final String CUSTOMER_ENTITY = "customer";
	public static final String DVS_SWITCH_ENTITY = "dvs-switch";
	public static final String FLAVOR_ENTITY = "flavor";
	public static final String FORWARDER_ENTITY = "forwarder";
	public static final String FORWARDING_PATH_ENTITY = "forwarding-path";
	public static final String GENERIC_VNF_ENTITY = "generic-vnf";
	public static final String IMAGE_ENTITY = "image";
	public static final String INSTANCE_GROUP_ENTITY = "instance-group";
	public static final String L_INTERFACE_ENTITY = "l-interface";
	public static final String L3_INTERFACE_IPV4_ADDRESS_LIST_ENTITY = "l3-interface-ipv4-address-list";
	public static final String L3_INTERFACE_IPV6_ADDRESS_LIST_ENTITY = "l3-interface-ipv6-address-list";
	public static final String L3_NETWORK_ENTITY = "l3-network";
	public static final String LAG_INTERFACE_ENTITY = "lag-interface";
	public static final String LAG_LINK_ENTITY = "lag-link";
	public static final String LINE_OF_BUSINESS_ENTITY = "line-of-business";
	public static final String LOGICAL_LINK_ENTITY = "logical-link";
	public static final String METADATUM_ENTITY = "metadatum";
	public static final String MODEL_ENTITY = "model";
	public static final String MODEL_VER_ENTITY = "model-ver";
	public static final String NETWORK_PROFILE_ENTITY = "network-profile";
	public static final String NOS_SERVER_ENTITY = "nos-server";
	public static final String OAM_NETWORK_ENTITY = "oam-network";
	public static final String OPERATIONAL_ENVIRONMENT_ENTITY = "operational-environment";
	public static final String OWNING_ENTITY_ENTITY = "owning-entity";
	public static final String P_INTERFACE_ENTITY = "p-interface";
	public static final String PHYSICAL_LINK_ENTITY = "physical-link";
	public static final String PLATFORM_ENTITY = "platform";
	public static final String PLUGGABLE_ENTITY = "pluggable";
	public static final String PLUGGABLE_SLOT_ENTITY = "pluggable-slot";
	public static final String PNF_ENTITY = "pnf";
	public static final String PORT_ENTITY = "port";
	public static final String PORT_GROUP_ENTITY = "port-group";
	public static final String PROJECT_ENTITY = "project";
	public static final String PSERVER_ENTITY = "pserver";
	public static final String RACK_ENTITY = "rack";
	public static final String SERVICE_ENTITY = "service";
	public static final String SERVICE_CAPABILITY_ENTITY = "service-capability";
	public static final String SERVICE_INSTANCE_ENTITY = "service-instance";
	public static final String SERVICE_SUBSCRIPTION_ENTITY = "service-subscription";
	public static final String SRIOV_PF_ENTITY = "sriov-pf";
	public static final String SRIOV_VF_ENTITY = "sriov-vf";
	public static final String SUBNET_ENTITY = "subnet";
	public static final String TENANT_ENTITY = "tenant";
	public static final String VCE_ENTITY = "vce";
	public static final String VF_MODULE_ENTITY = "vf-module";
	public static final String VIP_IPV4_ADDRESS_LIST_ENTITY = "vip-ipv4-address-list";
	public static final String CLOUD_VIP_IPV4_ADDRESS_LIST_ENTITY = "cloud-vip-ipv4-address-list";
	public static final String VIP_IPV6_ADDRESS_LIST_ENTITY = "vip-ipv6-address-list";
	public static final String CLOUD_VIP_IPV6_ADDRESS_LIST_ENTITY = "cloud-vip-ipv6-address-list";
	public static final String VIRTUAL_DATA_CENTER_ENTITY = "virtual-data-center";
	public static final String VLAN_ENTITY = "vlan";
	public static final String VNF_IMAGE_ENTITY = "vnf-image";
	public static final String VNFC_ENTITY = "vnfc";
	public static final String VOLUME_ENTITY = "volume";
	public static final String VPLS_PE_ENTITY = "vpls-pe";
	public static final String VSERVER_ENTITY = "vserver";
	public static final String ZONE_ENTITY = "zone";
	
	public static final String CTAG_POOL_ENTITY = "ctag-pool";
	public static final String CLOUD_OWNER = "cloud-owner";

	// A&AI & NARAD event fields
	public static final String EVENT_ENTITY_FIELD = "entity";
	public static final String EVENT_HEADER_FIELD = "event-header";
	public static final String EVENT_HEADER_ACTION_FIELD = "action";
	public static final String EVENT_HEADER_EVENT_TYPE_FIELD = "event-type";
	public static final String EVENT_HEADER_ENTITY_TYPE_FIELD = "entity-type";
	public static final String EVENT_HEADER_TOP_ENTITY_TYPE_FIELD = "top-entity-type";
	public static final String EVENT_HEADER_DOMAIN_FIELD = "domain";
	public static final String EVENT_HEADER_ID_FIELD = "id";

	public static final String VNAT = "vnat";
	public static final String VRR = "vrr";
	public static final String VIPE = "vipe";
	public static final String VMME = "vmme";
	
	public static final String CLOUD_REGIONS = "cloud-regions";
	public static final String CLOUD_REGION = "cloud-region";
	public static final String COMPLEXES = "complexes";
	public static final String PSERVERS = "pservers";
	public static final String GENERIC_VNFS = "generic-vnfs";
	public static final String VCES = "vces";
	public static final String VPES = "vpes";
	
	public static final String GAMMA_SERVICE = "gamma";
	public static final String MOBILITY_SERVICE = "mobility";
	public static final String NEWVCE_ENTITY = "newvce";
	
	public static final String DOMAIN1 = "d1";
	public static final String DOMAIN2 = "d2";
	public static final String DOMAIN3 = "d3";
	
	public static final String NODE_TYPE_VMME = "mmex";
	public static final String NODE_TYPE_AMDVSCP = "AMDvSCP";
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final String UPDATE = "UPDATE";
	public static final String CREATE = "CREATE";
	public static final String FULL = "FULL";
	public static final String DELETE = "DELETE";
	public static final String ADD = "ADD";

	public static final int HOURS_IN_A_DAY = 24;
	public static final int HOURS_IN_A_WEEK = 168;
	public static final int DAYS_IN_A_WEEK = 7;

	public static final String RESULTS = "results";
	public static final String INVALID_ACTION = "Invalid action : ";
	// Deploy action to Orchestrator - Possible HTTP Status codes
	public static final int DEPLOY_SUCCESS = 200;
	public static final int DEPLOY_BAD_REQUEST = 400;
	public static final int DEPLOY_BAD_REQUEST_CONTENT_TYPE = 415;
	
	// Delete action to Orchestrator - Possible HTTP Status codes
	public static final int DELETE_SUCCESS = 204;
	public static final int DELETE_NOT_FOUND = 404;

	// Deploy or Undeploy action to Orchestrator for UEB events - Possible HTTP Status codes
	public static final int EVENTS_SUCCESS = 202;
	public static final int EVENTS_BAD_REQUEST = 400;
	public static final int EVENTS_BAD_REQUEST_CONTENT_TYPE = 415;
	
	// Common status code for any action
	public static final int INTERNAL_SERVER_ERROR = 500;	

	//Path manipulation white lists
	public static final String KEYSTORE_PATH_WHITELIST = EventUtil.KEYSTORE_LOCATION;
	public static final String PATH_BLACKLIST = "[../]";

	public static final String DEPLOY = "deploy";
	public static final String UNDEPLOY = "undeploy";
	public static final String ACTION = "action";
	
	public static final String DCAE_SERVICE_ACTION_ADD = "add";
	public static final String DCAE_SERVICE_ACTION_UPDATE = "update";
	public static final String DCAE_SERVICE_ACTION_DELETE = "delete";

	public static final String ENTITY_LINK = "ENTITY_LINK";

	private Constants() {
		// Do nothing
		// This constructor was suggested by SonarLint.
	}
}
