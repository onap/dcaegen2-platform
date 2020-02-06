CREATE OR REPLACE VIEW dti.rt_vserver_pserver_v AS
SELECT
v.VSERVER_ID,
v.VSERVER_NAME,
v.VSERVER_NAME2,
v.PROV_STATUS,
v.VSERVER_SELFLINK,
v.IN_MAINT AS in_maint_vserver,
v.IS_CLOSED_LOOP_DISABLED,
v.TENANT_ID,
v.VALIDFROM AS validfrom_vserver,
p.HOSTNAME,
p.PTNII_EQUIP_NAME,
p.NUMBER_OF_CPUS,
p.DISK_IN_GIGABYTES,
p.RAM_IN_MEGABYTES,
p.EQUIP_TYPE,
p.EQUIP_VENDOR,
p.EQUIP_MODEL,
p.FQDN,
p.PSERVER_SELFLINK,
p.IPV4_OAM_ADDRESS,
p.SERIAL_NUMBER,
p.PSERVER_ID,
p.IN_MAINT AS in_maint_pserver,
p.INTERNET_TOPOLOGY,
p.RESOURCE_VERSION,
p.VALIDFROM AS validfrom_pserver
FROM dti.rt_vserver v
INNER JOIN dti.rt_relationship_list r
ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id
AND r.related_FROM = 'vserver'
INNER JOIN dti.rt_pserver p
ON p.hostname = r.to_node_id
AND r.related_to = 'pserver'
WHERE v.validto IS NULL
AND   r.validto IS NULL
AND   p.validto IS NULL;
