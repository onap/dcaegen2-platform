CREATE OR REPLACE VIEW dti.rt_vserver_image_v AS
SELECT
v.VSERVER_ID,
v.VSERVER_NAME,
v.VSERVER_NAME2,
v.PROV_STATUS,
v.VSERVER_SELFLINK,
v.IN_MAINT,
v.IS_CLOSED_LOOP_DISABLED,
v.TENANT_ID,
v.VALIDFROM AS validfrom_vserver,
i.IMAGE_ID,
i.IMAGE_NAME,
i.IMAGE_ARCHITECTURE,
i.IMAGE_OS_DISTRO,
i.IMAGE_OS_VERSION,
i.APPLICATION,
i.APPLICATION_VENDOR,
i.APPLICATION_VERSION,
i.IMAGE_SELFLINK,
i.VALIDFROM AS validfrom_image
FROM dti.rt_vserver v
INNER JOIN dti.rt_relationship_list r
ON concat(v.cloud_owner,'|',v.cloud_region_id,'|',v.tenant_id,'|',v.vserver_id) = r.from_node_id
AND r.related_FROM = 'vserver'
INNER JOIN dti.rt_image i
ON i.image_id = split_part(r.to_node_id,'|',3) 
AND r.related_to = 'image'
WHERE v.validto IS NULL
AND   r.validto IS NULL
AND   i.validto IS NULL;
