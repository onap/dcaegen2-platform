CREATE OR REPLACE VIEW dti.rt_vserver_generic_vnf_v AS
SELECT
v.VSERVER_ID,
v.VSERVER_NAME,
v.VSERVER_NAME2,
v.PROV_STATUS AS prov_status_vserver,
v.VSERVER_SELFLINK,
v.IN_MAINT AS in_maint_vserver,
v.IS_CLOSED_LOOP_DISABLED AS is_closed_loop_disabled_v,
v.TENANT_ID,
v.VALIDFROM  validfrom_vserver,
g.VNF_ID,
g.VNF_NAME,
g.VNF_NAME2,
g.VNF_TYPE,
g.SERVICE_ID,
g.REGIONAL_RESOURCE_ZONE,
g.PROV_STATUS AS prov_status_generic_vnf,
g.OPERATIONAL_STATUS,
g.EQUIPMENT_ROLE,
g.ORCHESTRATION_STATUS,
g.HEAT_STACK_ID,
g.MSO_CATALOG_KEY,
g.MANAGEMENT_OPTION,
g.IPV4_OAM_ADDRESS,
g.IPV4_LOOPBACK0_ADDRESS,
g.NM_LANV6_ADDRESS,
g.MANAGEMENT_V6_ADDRESS,
g.VCPU,
g.VCPU_UNITS,
g.VMEMORY,
g.VMEMORY_UNITS,
g.VDISK,
g.VDISK_UNITS,
g.IN_MAINT AS in_maint_generic_vnf,
g.IS_CLOSED_LOOP_DISABLED AS is_closed_loop_disabled_gv,
g.RESOURCE_VERSION,
g.VALIDFROM AS validfrom_generic_vnf
FROM dti.rt_vserver v
INNER JOIN dti.rt_relationship_list r
ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id
AND r.related_FROM = 'vserver'
INNER JOIN dti.rt_generic_vnf g
ON g.vnf_id = r.to_node_id
AND r.related_to = 'generic-vnf'
WHERE v.validto IS NULL
AND   r.validto IS NULL
AND   g.validto IS NULL;
