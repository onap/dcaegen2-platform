CREATE OR REPLACE VIEW dti.aai_vpe_v AS 
 SELECT 
    vnf_id,
    vnf_name,
    vnf_name2,
    vnf_type,
    service_id,
    regional_resource_zone,
    prov_status,
    operational_status,
    equipment_role,
    orchestration_status,
    heat_stack_id,
    mso_catalog_key,
    ipv4_oam_address,
    ipv4_oam_gateway_address_prefix_length,
    ipv4_oam_gateway_address,
    ipv4_loopback0_address,
    vlan_id_outer,
    resource_version,
    as_number,
    summary_status,
    encrypted_access_flag
   FROM dti.rt_generic_vnf where lower(vnf_type) = 'vpe' and lower(vnf_name) like '%me6';
