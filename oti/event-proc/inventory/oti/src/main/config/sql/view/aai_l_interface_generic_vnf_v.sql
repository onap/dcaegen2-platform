CREATE OR REPLACE VIEW dti.aai_l_interface_generic_vnf_v AS 
 SELECT l.interface_name,
    l.interface_role,
    l.v6_wan_link_ipaddress,
    l.selflink,
    l.interface_id,
    l.macaddress,
    l.network_name,
    l.parent_entity_type,
    l.parent_entity_id,
        CASE
            WHEN l.resource_version <> ' ' AND l.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(l.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS intf_resource_version_ts,   
    g.vnf_id,
    g.vnf_name,
    g.vnf_name2,
    g.vnf_type,
    g.service_id,
    g.regional_resource_zone,
    g.prov_status,
    g.operational_status,
    g.equipment_role,
    g.orchestration_status,
    g.heat_stack_id,
    g.mso_catalog_key,
    g.management_option,
    g.ipv4_oam_address,
    g.ipv4_loopback0_address,
    g.nm_lanv6_address,
    g.management_v6_address,
    g.vcpu,
    g.vcpu_units,
    g.vmemory,
    g.vmemory_units,
    g.vdisk,
    g.vdisk_units,
    g.in_maint,
    g.is_closed_loop_disabled,
        CASE
            WHEN g.resource_version <> ' ' AND g.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(g.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vnf_resource_version_ts
   FROM dti.rt_l_interface l
     JOIN dti.rt_generic_vnf g ON g.vnf_id = l.parent_entity_id
  WHERE l.parent_entity_type = 'generic-vnf';
