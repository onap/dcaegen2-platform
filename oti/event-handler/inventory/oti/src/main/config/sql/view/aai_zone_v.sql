CREATE OR REPLACE VIEW dti.aai_zone_v AS 
 SELECT 
  zone_id,
 zone_name,
 design_type,
 zone_context,
 status,
 resource_version
 FROM dti.rt_zone;
