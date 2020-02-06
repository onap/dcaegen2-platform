CREATE OR REPLACE VIEW dti.aai_vpe_vserver_v AS 
 SELECT a.vnf_id,
    a.vnf_name,
    a.vnf_name2,
    a.vnf_type,
    a.service_id,
    a.regional_resource_zone,
    a.prov_status,
    a.operational_status,
    a.equipment_role,
    a.orchestration_status,
    a.heat_stack_id,
    a.mso_catalog_key,
    a.ipv4_oam_address,
    a.ipv4_oam_gateway_address_prefix_length as ipv4_oam_gtwy_addr_pre_len,
    a.ipv4_oam_gateway_address as ipv4_oam_gtwy_addr,
    a.ipv4_loopback0_address as v4_loopback0_ip_address,
    a.vlan_id_outer,
    a.resource_version,
        CASE
            WHEN a.resource_version <> ' ' AND a.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(a.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vpe_resource_version_ts,  
    c.vserver_id,
    c.vserver_name,
    c.vserver_name2,
    c.prov_status AS vserver_prov_status,
    c.vserver_selflink,
    c.in_maint,
    c.is_closed_loop_disabled,
    c.resource_version AS vserver_resource_version,
        CASE
            WHEN c.resource_version <> ' ' AND c.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(c.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vserver_resource_version_ts,  
    c.tenant_id
   FROM dti.aai_vpe_v a
     JOIN dti.rt_relationship_list b ON a.vnf_id::text = b.from_node_id::text AND b.related_from::text = 'generic_vnf'::text
     JOIN dti.rt_vserver c ON CONCAT(c.cloud_owner, '|', c.cloud_Region_id, '|', c.tenant_id, '|', c.vserver_id) = b.to_node_id::text AND b.related_to::text = 'vserver'::text;
