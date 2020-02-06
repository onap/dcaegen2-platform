CREATE OR REPLACE VIEW dti.rt_l_interface_ipv4_6_v AS
SELECT
li.INTERFACE_NAME,
li.INTERFACE_ROLE,
li.INTERFACE_ID,
li.NETWORK_NAME,
li.PARENT_ENTITY_TYPE,
li.PARENT_ENTITY_ID,
ip4.IPV4ADDRESS,
ip4.VLAN_ID_INNER AS vlan_id_inner_ip4,
ip4.VLAN_ID_OUTER AS vlan_id_outer_ip4,
ip4.GRANDPARENT_ENTITY_TYPE AS grandparent_entity_type_ip4,
ip4.GRANDPARENT_ENTITY_ID AS grandparent_entity_id_ip4,
ip4.VALIDFROM AS validfrom_ip4,
ip6.IPV6ADDRESS,
ip6.VLAN_ID_INNER AS vlan_id_inner_ip6,
ip6.VLAN_ID_OUTER AS vlan_id_outer_ip6,
ip6.GRANDPARENT_ENTITY_TYPE AS grandparent_entity_type_ip6,
ip6.GRANDPARENT_ENTITY_ID AS grandparent_entity_id_ip6
FROM dti.rt_l_interface li
LEFT OUTER JOIN dti.rt_l3interface_ipv4addresslist ip4
ON li.interface_name = ip4.parent_interface_name
AND li.parent_entity_id = ip4.grandparent_entity_id
AND li.parent_entity_type = ip4.grandparent_entity_type
LEFT OUTER JOIN dti.rt_l3interface_ipv6addresslist ip6
ON li.interface_name = ip6.parent_interface_name
AND li.parent_entity_id = ip6.grandparent_entity_id
AND li.parent_entity_type = ip6.grandparent_entity_type
WHERE li.validto IS NULL
AND   ip4.validto IS NULL
AND   ip6.validto IS NULL;
