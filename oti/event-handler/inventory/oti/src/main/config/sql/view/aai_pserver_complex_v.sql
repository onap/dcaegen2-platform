CREATE OR REPLACE VIEW dti.aai_pserver_complex_v AS 
 SELECT p.hostname,
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
    p.ipaddress_v4_loopback_0,
    p.ipaddress_v6_loopback_0,
    p.ipaddress_v4_aim,
    p.ipaddress_v6_aim,
    p.ipaddress_v6_oam,
    p.inv_status,
    p.pserver_id,
    p.in_maint,
    p.internet_topology,
    p.resource_version AS resource_version_pserver,
        CASE
            WHEN p.resource_version <> ' ' AND p.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(p.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS pserver_resource_version_ts,  
    p.pserver_name2,
    p.purpose,
    p.prov_status,
    c.physical_location_id,
    c.data_center_code,
    c.complex_name,
    c.identity_url,
    c.resource_version AS resource_version_complex,
    c.physical_location_type,
    c.street1,
    c.street2,
    c.city,
    c.state,
    c.postal_code,
    c.country,
    c.region,
    c.latitude,
    c.longitude,
    c.elevation,
    c.lata,
        CASE
            WHEN c.resource_version <> ' ' AND c.resource_version <> NULL THEN TIMESTAMP 'epoch' + to_number(c.resource_version, '9999999999') * INTERVAL '1 second'
            ELSE NULL
        END AS complex_resource_version_ts
   FROM dti.rt_pserver p
     JOIN dti.rt_relationship_list r ON p.hostname = r.from_node_id AND r.related_from = 'pserver'
     JOIN dti.rt_complex c ON c.physical_location_id = r.to_node_id AND r.related_to = 'complex';
