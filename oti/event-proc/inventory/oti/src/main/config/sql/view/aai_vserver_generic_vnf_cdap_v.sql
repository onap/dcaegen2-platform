CREATE OR REPLACE VIEW dti.aai_vserver_generic_vnf_cdap_v AS 
 SELECT v.vserver_id,
    v.vserver_name,
    v.vserver_name2,
    v.prov_status AS prov_status_vserver,
    v.vserver_selflink,
    v.in_maint AS in_maint_vserver,
    v.is_closed_loop_disabled AS is_closed_loop_disabled_v,
    v.tenant_id,
	v.cloud_owner,
	v.cloud_region_id,
        CASE
            WHEN v.resource_version <> ' ' AND v.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(v.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vserver_resource_version_ts,
    g.vnf_id,
    g.vnf_name,
    g.vnf_name2,
    g.vnf_type,
    g.service_id,
    g.regional_resource_zone,
    g.prov_status AS prov_status_generic_vnf,
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
    g.in_maint AS in_maint_generic_vnf,
    CASE
            WHEN g.is_closed_loop_disabled = 'Y' THEN 'true'
            WHEN g.is_closed_loop_disabled = 'N' THEN 'false'
            ELSE g.is_closed_loop_disabled
        END AS is_closed_loop_disabled_gv,    
    g.resource_version,
        CASE
            WHEN g.resource_version <> ' ' AND g.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(g.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vnf_resource_version_ts
   FROM dti.rt_vserver v
     LEFT JOIN dti.rt_relationship_list r ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id AND r.related_from = 'vserver' AND r.related_to = 'generic-vnf'
     LEFT JOIN dti.rt_generic_vnf g ON g.vnf_id = r.to_node_id AND r.related_to = 'generic-vnf';
