
CREATE OR REPLACE VIEW dti.aai_image_v AS 
 SELECT 
    rt_image.image_id,
    rt_image.image_name,
    rt_image.image_architecture,
    rt_image.image_os_distro,
    rt_image.image_os_version,
    rt_image.application,
    rt_image.application_vendor,
    rt_image.application_version,
    rt_image.image_selflink,
    rt_image.resource_version
   FROM dti.rt_image;
