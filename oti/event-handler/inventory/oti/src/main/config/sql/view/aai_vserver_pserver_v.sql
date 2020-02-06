CREATE OR REPLACE VIEW dti.aai_vserver_pserver_v AS 
 SELECT v.vserver_id,
    v.vserver_name,
    v.vserver_name2,
    v.prov_status,
    v.vserver_selflink,
    v.in_maint AS in_maint_vserver,
    v.is_closed_loop_disabled,
    v.tenant_id,
        CASE
            WHEN v.resource_version <> ' ' AND v.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(v.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS vserver_resource_version_ts,  
    p.hostname,
    p.ptnii_equip_name,
    p.number_of_cpus,
    p.disk_in_gigabytes,
    p.ram_in_megabytes,
    p.equip_type,
    p.equip_vendor,
    p.equip_model,
    p.fqdn,
    p.pserver_selflink,
    p.ipv4_oam_address,
    p.serial_number,
    p.pserver_id,
    p.in_maint AS in_maint_pserver,
    p.internet_topology,
        CASE
            WHEN p.resource_version <> ' ' AND p.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(p.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS pserver_resource_version_ts
   FROM dti.rt_vserver v
     JOIN dti.rt_relationship_list r ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id AND r.related_from = 'vserver'
     JOIN dti.rt_pserver p ON p.hostname = r.to_node_id AND r.related_to = 'pserver';
