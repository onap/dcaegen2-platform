drop view IF EXISTS dti.aai_vm_cdap_v ;
-- added comments
CREATE OR REPLACE VIEW dti.aai_vm_cdap_v AS 
SELECT DISTINCT v1.vserver_name AS "vserver$vserver_name",
    v1.cloud_owner AS "cloud_region$cloud_owner",
    v1.cloud_region_id AS "cloud_region$cloud_region_id",
    v1.vserver_id AS "vserver$vserver_id",
    v1.tenant_id AS "tenant$tenant_id",
    v1.vnf_id AS "generic_vnf$vnf_id",
    v1.vnf_name AS "generic_vnf$vnf_name",
    v1.vnf_type AS "generic_vnf$vnf_type",
    v1.service_id AS "generic_vnf$service_id",
    v1.in_maint_vserver AS "vserver$in_maint",
    v1.is_closed_loop_disabled_v AS "vserver$is_closed_loop_disabled",
    v4.physical_location_id AS "complex$physical_location_id",
    v5.identity_url AS "cloud_region$identity_url",
    v4.city AS "complex$city",
    v4.state AS "complex$state",
    v1.vserver_selflink AS "vserver$selflink",
    v1.prov_status_vserver AS "vserver$prov_status",
    v2.interface_name AS "vserver$l_interface$interface_name",
    v2.network_name AS "vserver$l_interface$network_name",
    v2.ipv4address AS "l3_interface_ipv4_address_list$l3_inteface_ipv4_address",
    v2.ipv6address AS "l3_interface_ipv6_address_list$l3_inteface_ipv6_address"
   FROM dti.aai_vserver_generic_vnf_cdap_v v1
     LEFT JOIN dti.aai_l_interface_ipv4_6_oam_v v2 ON v1.vserver_id::text = v2.parent_entity_id::text
     JOIN dti.aai_vserver_pserver_v v3 ON v1.vserver_id::text = v3.vserver_id::text
     JOIN dti.aai_pserver_complex_v v4 ON v3.hostname::text = v4.hostname::text
     JOIN dti.rt_cloud_region v5 ON v5.cloud_region_id::text = v1.cloud_region_id::text AND v5.cloud_owner::text = v1.cloud_owner::text
  ORDER BY v1.vserver_name, v1.vnf_name, v2.network_name;
