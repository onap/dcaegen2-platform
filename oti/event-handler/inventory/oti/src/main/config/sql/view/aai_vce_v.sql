CREATE OR REPLACE VIEW dti.aai_vce_v AS 
 SELECT 
    vnf_id,
    vnf_name,
    vnf_name2,
    vnf_type,
    '' as service_id,
    '' as regional_resource_zone,
    prov_status,
    operational_status,
    equipment_role,
    orchestration_status,
    heat_stack_id,
    mso_catalog_key,
    ipv4_oam_address,
    ipv4_loopback0_address,
    resource_version
   FROM dti.rt_vce where lower(vnf_name) like '%vbc'
 UNION
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
    ipv4_loopback0_address,
    resource_version
   FROM dti.rt_generic_vnf where lower(vnf_type) = 'vce' and lower(vnf_name) like '%vbc';
