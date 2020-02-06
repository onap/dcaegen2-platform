CREATE OR REPLACE VIEW dti.rt_l_interface_vserver_v AS
SELECT
l.INTERFACE_NAME,
l.INTERFACE_ROLE,
l.V6_WAN_LINK_IPADDRESS,
l.SELFLINK,
l.INTERFACE_ID,
l.MACADDRESS,
l.NETWORK_NAME,
l.RESOURCE_VERSION,
l.PARENT_ENTITY_TYPE,
l.PARENT_ENTITY_ID,
l.VALIDFROM AS validfrom_l_interface,
v.VSERVER_ID,
v.VSERVER_NAME,
v.VSERVER_NAME2,
v.PROV_STATUS,
v.VSERVER_SELFLINK,
v.IN_MAINT,
v.IS_CLOSED_LOOP_DISABLED,
v.TENANT_ID,
v.VALIDFROM AS validfrom_vserver
FROM dti.rt_l_interface l
INNER JOIN dti.rt_vserver v
ON v.vserver_id = l.parent_entity_id
WHERE l.parent_entity_type = 'vserver'
AND   l.validto IS NULL
AND   v.validto IS NULL;
