CREATE OR REPLACE VIEW dti.aai_network_profile_v AS 
 SELECT 
    rt_network_profile.nm_profile_name,
    rt_network_profile.community_string,
    rt_network_profile.resource_version
   FROM dti.rt_network_profile;
