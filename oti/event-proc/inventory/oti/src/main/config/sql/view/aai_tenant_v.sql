CREATE OR REPLACE VIEW dti.aai_tenant_v AS 
 SELECT 
    rt_tenant.tenant_id,
    rt_tenant.tenant_name,
    rt_tenant.resource_version,
    rt_tenant.cloud_owner,
    rt_tenant.cloud_region_id,
    rt_tenant.tenant_context
   FROM dti.rt_tenant;
