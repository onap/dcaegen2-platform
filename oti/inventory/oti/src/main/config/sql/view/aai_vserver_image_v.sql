
CREATE OR REPLACE VIEW dti.aai_vserver_image_v AS 
 SELECT v.vserver_id,
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
        END AS vserver_resource_version_ts,  
    i.image_id,
    i.image_name,
    i.image_architecture,
    i.image_os_distro,
    i.image_os_version,
    i.application,
    i.application_vendor,
    i.application_version,
    i.image_selflink,
        CASE
            WHEN i.resource_version <> ' ' AND i.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(i.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS img_resource_version_ts
   FROM dti.rt_vserver v
     JOIN dti.rt_relationship_list r ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id::text AND r.related_from::text = 'vserver'::text
     JOIN dti.rt_image i ON i.image_id::text = split_part(r.to_node_id,'|',3) AND r.related_to::text = 'image'::text;
