CREATE OR REPLACE VIEW dti.v_vpe_vm_pserver AS 
 SELECT substr(v1.vserver_name, 1, 11) AS vnf_name,
    v1.ipv4_oam_address AS vnf_ipv4_oam_address,
    substr(v1.vnf_name, 14, 3) AS vnfc,
    v1.v4_loopback0_ip_address AS vnf_ipv4_loopback_address,
    v1.in_maint AS vnf_in_maint,
    v1.prov_status AS vnf_prov_status,
    v1.vnf_type,
    v1.vserver_name AS vm_name,
    v2p_server.hostname AS pserver_hostname,
    v2p_server.fqdn AS pserver_fqdn,
    v2p_server.ipv4_oam_address AS pserver_oam_ipv4,
    v2p_server.equip_type AS pserver_equip_type,
    v2p_server.equip_vendor AS pserver_equip_vendor,
    p2complex.physical_location_id AS pserver_physical_location_id,
    p2complex.complex_name,
    p2complex.city,
    p2complex.state,
    p2complex.country
   FROM dti.aai_vpe_vserver_v v1
     JOIN dti.aai_vserver_pserver_v v2p_server 
     ON v1.vserver_id = v2p_server.vserver_id
     LEFT JOIN dti.aai_pserver_complex_v p2complex 
     ON v2p_server.pserver_id = p2complex.pserver_id
  WHERE v1.vnf_name like '%me6%';
