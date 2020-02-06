CREATE OR REPLACE VIEW dti.aai_l_interface_vserver_v AS 
 SELECT l.interface_name,
    l.interface_role,
    l.v6_wan_link_ipaddress,
    l.selflink,
    l.interface_id,
    l.macaddress,
    l.network_name,
    l.resource_version,
    l.parent_entity_type,
    l.parent_entity_id,
        CASE
            WHEN l.resource_version <> ' ' AND l.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(l.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS intf_resource_version_ts,   
    v.vserver_id,
    v.vserver_name,
    v.vserver_name2,
    v.prov_status,
    v.vserver_selflink,
    v.in_maint,
    v.is_closed_loop_disabled,
    v.tenant_id,
         CASE
                WHEN v.resource_version <> ' ' AND v.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(v.resource_version, '9999999999') * INTERVAL '1 second'
                ELSE NULL
        END AS vserver_resource_version_ts
   FROM dti.rt_l_interface l
     JOIN dti.rt_vserver v ON v.vserver_id::text = l.parent_entity_id::text
  WHERE l.parent_entity_type::text = 'vserver'::text;
