CREATE OR REPLACE VIEW dti.aai_vserver_v AS 
 SELECT 
    rt_vserver.vserver_id,
    rt_vserver.vserver_name,
    rt_vserver.vserver_name2,
    rt_vserver.prov_status,
    rt_vserver.vserver_selflink,
    rt_vserver.in_maint,
    rt_vserver.is_closed_loop_disabled,
    rt_vserver.resource_version,
    rt_vserver.tenant_id,
    rt_vserver.cloud_owner,
    rt_vserver.cloud_region_id
   FROM dti.rt_vserver;
