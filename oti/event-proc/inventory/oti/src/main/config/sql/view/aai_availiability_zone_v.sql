CREATE OR REPLACE VIEW dti.aai_availability_zone_v AS 
 SELECT 
    rt_availability_zone.availability_zone_name,
    rt_availability_zone.hypervisor_type,
    rt_availability_zone.operational_status,
    rt_availability_zone.resource_version
   FROM dti.rt_availability_zone;
