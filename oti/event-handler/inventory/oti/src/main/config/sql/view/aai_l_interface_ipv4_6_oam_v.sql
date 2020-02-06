CREATE OR REPLACE VIEW dti.aai_l_interface_ipv4_6_oam_v AS
SELECT li.interface_name,
    li.interface_role,
    li.interface_id,
    li.network_name,
    li.parent_entity_type,
    li.parent_entity_id,
        CASE
            WHEN li.resource_version::text <> ' '::text AND li.resource_version::text <> NULL::text THEN '1970-01-01 00:00:00'::timestamp without time zone + to_number(li.resource_version::text, '9999999999'::text)::double precision * '00:00:01'::interval
            ELSE NULL::timestamp without time zone
        END AS intf_resource_version_ts,
    ip4.ipv4address,
    ip4.vlan_id_inner AS vlan_id_inner_ip4,
    ip4.vlan_id_outer AS vlan_id_outer_ip4,
    ip4.grandparent_entity_type AS grandparent_entity_type_ip4,
    ip4.grandparent_entity_id AS grandparent_entity_id_ip4,
        CASE
            WHEN ip4.resource_version::text <> ' '::text AND ip4.resource_version::text <> NULL::text THEN '1970-01-01 00:00:00'::timestamp without time zone + to_number(ip4.resource_version::text, '9999999999'::text)::double precision * '00:00:01'::interval
            ELSE NULL::timestamp without time zone
        END AS ipv4_resource_version_ts,
    ip6.ipv6address,
    ip6.vlan_id_inner AS vlan_id_inner_ip6,
    ip6.vlan_id_outer AS vlan_id_outer_ip6,
    ip6.grandparent_entity_type AS grandparent_entity_type_ip6,
    ip6.grandparent_entity_id AS grandparent_entity_id_ip6,
        CASE
            WHEN ip6.resource_version::text <> ' '::text AND ip6.resource_version::text <> NULL::text THEN '1970-01-01 00:00:00'::timestamp without time zone + to_number(ip6.resource_version::text, '9999999999'::text)::double precision * '00:00:01'::interval
            ELSE NULL::timestamp without time zone
        END AS ipv6_resource_version_ts
   FROM dti.rt_l_interface li
     LEFT JOIN dti.rt_l3interface_ipv4addresslist ip4 ON li.interface_name::text = ip4.parent_entity_id::text AND li.parent_entity_id::text = ip4.grandparent_entity_id::text AND li.parent_entity_type::text = ip4.grandparent_entity_type::text
     LEFT JOIN dti.rt_l3interface_ipv6addresslist ip6 ON li.interface_name::text = ip6.parent_entity_id::text AND li.parent_entity_id::text = ip6.grandparent_entity_id::text AND li.parent_entity_type::text = ip6.grandparent_entity_type::text
  WHERE lower(li.network_name::text) ~~ '%oam%'::text or lower(li.interface_name::text) ~~ '%oam%'::text;
