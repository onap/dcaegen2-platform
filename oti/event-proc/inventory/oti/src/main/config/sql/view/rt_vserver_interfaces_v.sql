CREATE OR REPLACE VIEW dti.rt_vserver_interfaces_v AS
SELECT
iv.vserver_id,
iv.vserver_name,
iv.vserver_name2,
iv.prov_status,
iv.vserver_selflink,
iv.in_maint,
iv.is_closed_loop_disabled,
iv.tenant_id,
iv.validfrom_vserver,
iv.interface_name,
iv.interface_role,
iv.v6_wan_link_ipaddress,
iv.selflink,
iv.interface_id,
iv.macaddress,
iv.network_name,
iv.resource_version,
iv.parent_entity_type,
iv.parent_entity_id,
iv.validfrom_l_interface,
ip.ipv4address,
ip.ipv6address
FROM dti.rt_l_interface_vserver_v iv
INNER JOIN dti.rt_l_interface_ipv4_6_v ip
ON iv.vserver_id = ip.grandparent_entity_id_ip4
AND iv.interface_name = ip.interface_name
;
