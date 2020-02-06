delete from dti.rt_availability_zone where validTo is not null;
drop index dti.rt_availability_zone_idx1;
CREATE UNIQUE INDEX rt_availability_zone_idx1
    ON dti.rt_availability_zone(availability_zone_name);
alter table dti.rt_availability_zone drop column validfrom CASCADE;
alter table dti.rt_availability_zone drop column validto CASCADE;

delete from dti.rt_cloud_region where validTo is not null;
drop index dti.rt_cloud_region_idx1;
CREATE UNIQUE INDEX rt_cloud_region_idx1
        ON dti.rt_cloud_region(CLOUD_OWNER,CLOUD_REGION_ID);
alter table dti.rt_cloud_region drop column validfrom CASCADE;
alter table dti.rt_cloud_region drop column validto CASCADE;

delete from dti.rt_complex where validTo is not null;
drop index dti.rt_complex_idx1;
CREATE UNIQUE INDEX rt_complex_idx1
       ON dti.rt_complex(physical_location_id);
alter table dti.rt_complex drop column validfrom CASCADE;
alter table dti.rt_complex drop column validto CASCADE;

delete from dti.rt_customer where validTo is not null;
drop index dti.rt_customer_idx1;
CREATE UNIQUE INDEX rt_customer_idx1
        ON dti.rt_customer(GLOBAL_CUSTOMER_ID);
alter table dti.rt_customer drop column validfrom CASCADE;
alter table dti.rt_customer drop column validto CASCADE;

delete from dti.rt_dvs_switch where validTo is not null;
drop index dti.rt_dvs_switch_idx1;
CREATE UNIQUE INDEX rt_dvs_switch_idx1
		 ON dti.rt_dvs_switch(SWITCH_NAME);
alter table dti.rt_dvs_switch drop column validfrom CASCADE;
alter table dti.rt_dvs_switch drop column validto CASCADE;

delete from dti.rt_flavor where validTo is not null;
drop index dti.rt_flavor_idx1;
CREATE UNIQUE INDEX rt_flavor_idx1
        ON dti.rt_flavor(flavor_id);
alter table dti.rt_flavor drop column validfrom CASCADE;
alter table dti.rt_flavor drop column validto CASCADE;

delete from dti.rt_generic_vnf where validTo is not null;
drop index dti.rt_generic_vnf_idx1;
CREATE UNIQUE INDEX rt_generic_vnf_idx1
    ON dti.rt_generic_vnf(VNF_ID);
alter table dti.rt_generic_vnf drop column validfrom CASCADE;
alter table dti.rt_generic_vnf drop column validto CASCADE;

delete from dti.rt_image where validTo is not null;
drop index dti.rt_image_idx1;
CREATE UNIQUE INDEX rt_image_idx1
        ON dti.rt_image(IMAGE_ID);
alter table dti.rt_image drop column validfrom CASCADE;
alter table dti.rt_image drop column validto CASCADE;

delete from dti.rt_l_interface where validTo is not null;
drop index dti.rt_l_interface_idx1;
CREATE UNIQUE INDEX rt_l_interface_idx1
    ON dti.rt_l_interface(INTERFACE_NAME,PARENT_ENTITY_ID);
alter table dti.rt_l_interface drop column validfrom CASCADE;
alter table dti.rt_l_interface drop column validto CASCADE;

delete from dti.rt_l3_network where validTo is not null;
drop index dti.rt_l3_network_idx1;
CREATE UNIQUE INDEX rt_l3_network_idx1
    ON dti.rt_l3_network(NETWORK_ID);
alter table dti.rt_l3_network drop column validfrom CASCADE;
alter table dti.rt_l3_network drop column validto CASCADE;

delete from dti.rt_l3interface_ipv4addresslist where validTo is not null;
drop index dti.rt_l3intf_ipv4addrlist_idx1;
CREATE UNIQUE INDEX rt_l3intf_ipv4addrlist_idx1
        ON dti.rt_l3interface_ipv4addresslist(IPV4ADDRESS,PARENT_INTERFACE_NAME,GRANDPARENT_ENTITY_ID);
alter table dti.rt_l3interface_ipv4addresslist drop column validfrom CASCADE;
alter table dti.rt_l3interface_ipv4addresslist drop column validto CASCADE;

delete from dti.rt_l3interface_ipv6addresslist where validTo is not null;
drop index dti.rt_l3intf_ipv6addrlist_idx1;
CREATE UNIQUE INDEX rt_l3intf_ipv6addrlist_idx1
        ON dti.rt_l3interface_ipv6addresslist(IPV6ADDRESS,PARENT_INTERFACE_NAME,GRANDPARENT_ENTITY_ID);

delete from dti.rt_l_interface where validTo is not null;
drop index dti.rt_availability_zone_idx1;
delete from dti.rt_l3interface_ipv6addresslist;
CREATE UNIQUE INDEX rt_l3intf_ipv6addrlist_idx1
        ON dti.rt_l3interface_ipv6addresslist(IPV6ADDRESS,PARENT_INTERFACE_NAME,GRANDPARENT_ENTITY_ID);
alter table dti.rt_l3interface_ipv6addresslist drop column validfrom CASCADE;
alter table dti.rt_l3interface_ipv6addresslist drop column validto CASCADE;

delete from dti.rt_lag_interface where validTo is not null;
drop index dti.rt_lag_interface_idx1;
CREATE UNIQUE INDEX rt_lag_interface_idx1
    ON dti.rt_lag_interface(INTERFACE_NAME,PARENT_ENTITY_TYPE,PARENT_ENTITY_ID);
alter table dti.rt_lag_interface drop column validfrom CASCADE;
alter table dti.rt_lag_interface drop column validto CASCADE;
	
delete from dti.rt_lag_link where validTo is not null;
drop index dti.rt_lag_link_idx1;
CREATE UNIQUE INDEX rt_lag_link_idx1
        ON dti.rt_lag_link(LINK_NAME);
alter table dti.rt_lag_link drop column validfrom CASCADE;
alter table dti.rt_lag_link drop column validto CASCADE;

delete from dti.rt_logical_link where validTo is not null;
drop index dti.rt_logical_link_idx1;
CREATE UNIQUE INDEX rt_logical_link_idx1
    ON dti.rt_logical_link(LINK_NAME);
alter table dti.rt_logical_link drop column validfrom CASCADE;
alter table dti.rt_logical_link drop column validto CASCADE;

delete from dti.rt_metadatum where validTo is not null;
drop index dti.rt_metadatum_idx1;
CREATE UNIQUE INDEX rt_metadatum_idx1
        ON dti.rt_metadatum(METANAME,PARENT_ENTITY_ID);
alter table dti.rt_metadatum drop column validfrom CASCADE;
alter table dti.rt_metadatum drop column validto CASCADE;

delete from dti.rt_model where validTo is not null;
drop index dti.rt_model_idx1;
CREATE UNIQUE INDEX rt_model_idx1
    ON dti.rt_model(MODEL_INVARIANT_ID);
alter table dti.rt_model drop column validfrom CASCADE;
alter table dti.rt_model drop column validto CASCADE;

delete from dti.rt_model_ver where validTo is not null;
drop index dti.rt_model_ver_idx1;
CREATE UNIQUE INDEX rt_model_ver_idx1
    ON dti.rt_model_ver(MODEL_VERSION_ID,MODEL_INVARIANT_ID);
alter table dti.rt_model_ver drop column validfrom CASCADE;
alter table dti.rt_model_ver drop column validto CASCADE;

delete from dti.rt_network_profile where validTo is not null;
drop index dti.rt_network_profile_idx1;
CREATE UNIQUE INDEX rt_network_profile_idx1
        ON dti.rt_network_profile(NM_PROFILE_NAME);
alter table dti.rt_network_profile drop column validfrom CASCADE;
alter table dti.rt_network_profile drop column validto CASCADE;

delete from dti.rt_newvce where validTo is not null;
drop index dti.rt_newvce_idx1;
CREATE UNIQUE INDEX rt_newvce_idx1
    ON dti.rt_newvce(VNF_ID2);
alter table dti.rt_newvce drop column validfrom CASCADE;
alter table dti.rt_newvce drop column validto CASCADE;

delete from dti.rt_oam_network where validTo is not null;
drop index dti.rt_oam_network_idx1;
CREATE UNIQUE INDEX rt_oam_network_idx1
        ON dti.rt_oam_network(NETWORK_UUID);
alter table dti.rt_oam_network drop column validfrom CASCADE;
alter table dti.rt_oam_network drop column validto CASCADE;
		
delete from dti.rt_p_interface where validTo is not null;
drop index dti.rt_p_interface_idx1;
CREATE UNIQUE INDEX rt_p_interface_idx1
    ON dti.rt_p_interface(INTERFACE_NAME,PARENT_ENTITY_TYPE,PARENT_ENTITY_ID);
alter table dti.rt_p_interface drop column validfrom CASCADE;
alter table dti.rt_p_interface drop column validto CASCADE;

delete from dti.rt_physical_link;
drop index dti.rt_physical_link_idx1;
CREATE UNIQUE INDEX rt_physical_link_idx1
        ON dti.rt_physical_link(link_name);
alter table dti.rt_physical_link drop column validfrom CASCADE;
alter table dti.rt_physical_link drop column validto CASCADE;

delete from dti.rt_pnf where validTo is not null;
drop index dti.rt_pnf_idx1;
CREATE UNIQUE INDEX rt_pnf_idx1
    ON dti.rt_pnf(PNF_NAME);
alter table dti.rt_pnf drop column validfrom CASCADE;
alter table dti.rt_pnf drop column validto CASCADE;

delete from dti.rt_port_group where validTo is not null;
drop index dti.rt_port_group_idx1;
CREATE UNIQUE INDEX rt_port_group_idx1
        ON dti.rt_port_group(INTERFACE_ID,VNF_ID);
alter table dti.rt_port_group drop column validfrom CASCADE;
alter table dti.rt_port_group drop column validto CASCADE;

delete from dti.rt_pserver where validTo is not null;
drop index dti.rt_pserver_idx1;
CREATE UNIQUE INDEX rt_pserver_idx1
    ON dti.rt_pserver(HOSTNAME);
alter table dti.rt_pserver drop column validfrom CASCADE;
alter table dti.rt_pserver drop column validto CASCADE;
	
delete from dti.rt_relationship_list where validTo is not null;
drop index dti.rt_relationship_list_idx1;
alter table dti.rt_relationship_list drop column validfrom CASCADE;
alter table dti.rt_relationship_list drop column validto CASCADE;

delete from dti.rt_service where validTo is not null;
drop index dti.rt_service_idx1;
CREATE UNIQUE INDEX rt_service_idx1
        ON dti.rt_service(SERVICE_ID);
alter table dti.rt_service drop column validfrom CASCADE;
alter table dti.rt_service drop column validto CASCADE;

delete from dti.rt_service_capability where validTo is not null;
drop index dti.rt_service_capability_idx1;
CREATE UNIQUE INDEX rt_service_capability_idx1
        ON dti.rt_service_capability(SERVICE_TYPE);
alter table dti.rt_service_capability drop column validfrom CASCADE;
alter table dti.rt_service_capability drop column validto CASCADE;

delete from dti.rt_service_instance where validTo is not null;
drop index dti.rt_service_instance_idx1;
CREATE UNIQUE INDEX rt_service_instance_idx1
    ON dti.rt_service_instance(SERVICE_INSTANCE_ID,SERVICE_TYPE,GLOBAL_CUSTOMER_ID);
alter table dti.rt_service_instance drop column validfrom CASCADE;
alter table dti.rt_service_instance drop column validto CASCADE;

delete from dti.rt_service_subscription where validTo is not null;
drop index dti.rt_service_subscription_idx1;
CREATE UNIQUE INDEX rt_service_subscription_idx1
        ON dti.rt_service_subscription(SERVICE_TYPE,GLOBAL_CUSTOMER_ID);
alter table dti.rt_service_subscription drop column validfrom CASCADE;
alter table dti.rt_service_subscription drop column validto CASCADE;

delete from dti.rt_sriov_vf;
drop index dti.rt_sriov_vf_idx1;
CREATE UNIQUE INDEX rt_sriov_vf_idx1
        ON dti.rt_sriov_vf(PCI_ID,PARENT_ENTITY_ID);
alter table dti.rt_sriov_vf drop column validfrom CASCADE;
alter table dti.rt_sriov_vf drop column validto CASCADE;

delete from dti.rt_subnet where validTo is not null;
drop index dti.rt_subnet_idx1;
CREATE UNIQUE INDEX rt_subnet_idx1
        ON dti.rt_subnet(SUBNET_ID,NETWORK_ID);
alter table dti.rt_subnet drop column validfrom CASCADE;
alter table dti.rt_subnet drop column validto CASCADE;
		
delete from dti.rt_tenant;
drop index dti.rt_tenant_idx1;
CREATE UNIQUE INDEX rt_tenant_idx1
        ON dti.rt_tenant(TENANT_ID,CLOUD_OWNER,CLOUD_REGION_ID);
alter table dti.rt_tenant drop column validfrom CASCADE;
alter table dti.rt_tenant drop column validto CASCADE;

delete from dti.rt_vce where validTo is not null;
drop index dti.rt_vce_idx1;
CREATE UNIQUE INDEX rt_vce_idx1
    ON dti.rt_vce(VNF_ID);
alter table dti.rt_vce drop column validfrom CASCADE;
alter table dti.rt_vce drop column validto CASCADE;

delete from dti.rt_vf_module where validTo is not null;
drop index dti.rt_vf_module_idx1;
CREATE UNIQUE INDEX rt_vf_module_idx1
    ON dti.rt_vf_module(VF_MODULE_ID);
alter table dti.rt_vf_module drop column validfrom CASCADE;
alter table dti.rt_vf_module drop column validto CASCADE;

delete from dti.rt_virtual_data_center where validTo is not null;
drop index dti.rt_virtual_data_center_idx1;
CREATE UNIQUE INDEX rt_virtual_data_center_idx1
        ON dti.rt_virtual_data_center(VDC_ID);
alter table dti.rt_virtual_data_center drop column validfrom CASCADE;
alter table dti.rt_virtual_data_center drop column validto CASCADE;

delete from dti.rt_vlan where validTo is not null;
drop index dti.rt_vlan_idx1;
CREATE UNIQUE INDEX rt_vlan_idx1
    ON dti.rt_vlan(VLAN_INTERFACE,INTERFACE_NAME,INTERFACE_PARENT_ID);
alter table dti.rt_vlan drop column validfrom CASCADE;
alter table dti.rt_vlan drop column validto CASCADE;

delete from dti.rt_vnf_image where validTo is not null;
drop index dti.rt_vnf_image_idx1;
CREATE UNIQUE INDEX rt_vnf_image_idx1
        ON dti.rt_vnf_image(ATT_UUID);
alter table dti.rt_vnf_image drop column validfrom CASCADE;
alter table dti.rt_vnf_image drop column validto CASCADE;

delete from dti.rt_vnfc where validTo is not null;
drop index dti.rt_vnfc_idx1;
CREATE UNIQUE INDEX rt_vnfc_idx1
 ON dti.rt_vnfc(VNFC_NAME);
alter table dti.rt_vnfc drop column validfrom CASCADE;
alter table dti.rt_vnfc drop column validto CASCADE;
 
delete from dti.rt_volume where validTo is not null;
drop index dti.rt_volume_idx1;
CREATE UNIQUE INDEX rt_volume_idx1
        ON dti.rt_volume(VOLUME_ID,VSERVER_ID);
alter table dti.rt_volume drop column validfrom CASCADE;
alter table dti.rt_volume drop column validto CASCADE;

delete from dti.rt_vpe where validTo is not null;
drop index dti.rt_vpe_idx1;
CREATE UNIQUE INDEX rt_vpe_idx1
    ON dti.rt_vpe(VNF_ID);
alter table dti.rt_vpe drop column validfrom CASCADE;
alter table dti.rt_vpe drop column validto CASCADE;

delete from dti.rt_vpls_pe where validTo is not null;
drop index dti.rt_vpls_pe_idx1;
CREATE UNIQUE INDEX rt_vpls_pe_idx1
        ON dti.rt_vpls_pe(EQUIPMENT_NAME);
alter table dti.rt_vpls_pe drop column validfrom CASCADE;
alter table dti.rt_vpls_pe drop column validto CASCADE;

delete from dti.rt_vserver where validTo is not null;
drop index dti.rt_vserver_idx1;
CREATE UNIQUE INDEX rt_vserver_idx1
    ON dti.rt_vserver(VSERVER_ID,TENANT_ID,CLOUD_OWNER,CLOUD_REGION_ID);
alter table dti.rt_vserver drop column validfrom CASCADE;
alter table dti.rt_vserver drop column validto CASCADE;

delete from dti.rt_zone where validTo is not null;
drop index dti.rt_zone_idx1;
CREATE UNIQUE INDEX rt_zone_idx1
    ON dti.rt_zone(ZONE_ID);
alter table dti.rt_zone drop column validfrom CASCADE;
alter table dti.rt_zone drop column validto CASCADE;

alter table dti.rt_cloud_region add column SRIOV_AUTOMATION VARCHAR(1);

alter table dti.rt_generic_vnf add column ipv4_oam_gateway_address VARCHAR(20);
alter table dti.rt_generic_vnf add column ipv4_oam_gateway_address_prefix_length VARCHAR(20);
alter table dti.rt_generic_vnf add column vlan_id_outer VARCHAR(100);
alter table dti.rt_generic_vnf add column NM_PROFILE_NAME VARCHAR(100);

alter table dti.rt_lag_interface add column IN_MAINT VARCHAR(1) NOT NULL DEFAULT 'N';

alter table dti.rt_l_interface add column PROV_STATUS VARCHAR(10);
alter table dti.rt_l_interface add column IN_MAINT VARCHAR(1) NOT NULL DEFAULT 'N';
alter table dti.rt_l_interface add column IS_IP_UNNUMBERED VARCHAR(1) NOT NULL DEFAULT 'N';

alter table dti.rt_logical_link add column IN_MAINT VARCHAR(1) NOT NULL DEFAULT 'N';

alter table dti.rt_p_interface add column IN_MAINT VARCHAR(1) NOT NULL DEFAULT 'N';
alter table dti.rt_p_interface add column INV_STATUS VARCHAR(20);

alter table dti.rt_physical_link add column SERVICE_PROVIDER_NAME VARCHAR(50);

alter table dti.rt_pserver add column MANAGEMENT_OPTION VARCHAR(20);

alter table dti.rt_service_instance add column SERVICE_ROLE VARCHAR(100);
alter table dti.rt_service_instance add column ENVIRONMENT_CONTEXT VARCHAR(40);
alter table dti.rt_service_instance add column WORKLOAD_CONTEXT VARCHAR(40);

alter table dti.rt_tenant add column TENANT_CONTEXT VARCHAR(150);

alter table dti.rt_vlan add column IN_MAINT VARCHAR(1) NOT NULL DEFAULT 'N';
alter table dti.rt_vlan add column PROV_STATUS VARCHAR(10);
alter table dti.rt_vlan rename VPN_ID to VPN_KEY;
alter table dti.rt_vlan add column IS_IP_UNNUMBERED VARCHAR(1) NOT NULL DEFAULT 'N';

alter table dti.rt_vnfc rename vnfc_function_code to nfc_naming_code;
alter table dti.rt_vnfc rename vnfc_type to nfc_function;
alter table dti.rt_vnfc add column MODEL_INVARIANT_ID VARCHAR(150);
alter table dti.rt_vnfc add column MODEL_VERSION_ID VARCHAR(100);

 drop view dti.aai_generic_vnf;
alter table dti.rt_generic_vnf alter column model_version_id TYPE varchar(100);
alter table dti.rt_l3_network alter column model_version_id TYPE varchar(100);

alter table dti.rt_logical_link alter column model_version_id TYPE varchar(100);
alter table dti.rt_model_ver alter column model_version_id TYPE varchar(100);
alter table dti.rt_service_instance alter column model_version_id TYPE varchar(100);
alter table dti.rt_vf_module alter column model_version_id TYPE varchar(100);
alter table dti.rt_vnfc alter column model_version_id TYPE varchar(100);
