CREATE OR REPLACE VIEW dti.aai_flavor_v AS 
 SELECT 
    rt_flavor.flavor_id,
    rt_flavor.flavor_name,
    rt_flavor.flavor_vcpus,
    rt_flavor.flavor_ram,
    rt_flavor.flavor_disk,
    rt_flavor.flavor_ephemeral,
    rt_flavor.flavor_swap,
    rt_flavor.flavor_is_public,
    rt_flavor.flavor_selflink,
    rt_flavor.flavor_disabled,
    rt_flavor.resource_version
   FROM dti.rt_flavor;
