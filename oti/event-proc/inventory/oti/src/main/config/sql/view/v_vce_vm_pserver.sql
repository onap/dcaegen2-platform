CREATE OR REPLACE VIEW dti.V_VCE_VM_PSERVER AS
select substr(v1.vserver_name, 1, 11) as vnf_name  ,
       v1.ipv4_oam_address as vnf_ipv4_oam_address,
       substr(vnf_name, 14, 3) as vnfc,
       v1.in_maint as vnf_in_maint,
      v1.prov_status as vnf_prov_status,
      v1.vnf_type,
      v1.vserver_name as vm_name,
      v2p_server.hostname as pserver_hostname,
      v2p_server.fqdn as pserver_fqdn, 
      v2p_server.ipv4_oam_address as pserver_oam_ipv4,
      v2p_server.equip_type as pserver_equip_type,
      v2p_server.equip_vendor as pserver_equip_vendor,
      p2complex.physical_location_id as pserver_physical_location_id,
      p2complex.complex_name as complex_name,
      p2complex.city as city,
      p2complex.state as state,
      p2complex.country as country
from
      dti.aai_vce_vserver_v v1
 JOIN dti.aai_vserver_pserver_v v2p_server
   ON v1.vserver_id= v2p_server.vserver_id 
  LEFT JOIN dti.aai_pserver_complex_v p2complex 
    ON v2p_server.pserver_id = p2complex.pserver_id 
where  v1.vnf_name SIMILAR TO '[a-zA-Z0-9]{5}4[0-9A-Za-z]{2}vbc[a-zA-Z0-9]*'
UNION
select substr(v1.vserver_name, 1, 11) as vnf_name  ,
       v1.ipv4_oam_address as vnf_ipv4_oam_address,
       substr(vnf_name, 14, 3) as vnfc,
      v1.in_maint_generic_vnf AS vnf_in_maint,
      v1.prov_status_generic_vnf AS vnf_prov_status,
      v1.vnf_type,
      v1.vserver_name as vm_name,
      v2p_server.hostname as pserver_hostname,
      v2p_server.fqdn as pserver_fqdn, 
      v2p_server.ipv4_oam_address as pserver_oam_ipv4,
      v2p_server.equip_type as pserver_equip_type,
      v2p_server.equip_vendor as pserver_equip_vendor,
      p2complex.physical_location_id as pserver_physical_location_id,
      p2complex.complex_name as complex_name,
      p2complex.city as city,
      p2complex.state as state,
      p2complex.country as country
from
      dti.aai_generic_vnf_vserver_v v1
 JOIN dti.aai_vserver_pserver_v v2p_server
   ON v1.vserver_id= v2p_server.vserver_id 
  LEFT JOIN dti.aai_pserver_complex_v p2complex 
    ON v2p_server.pserver_id = p2complex.pserver_id 
where  v1.vnf_name SIMILAR TO '[a-zA-Z0-9]{5}4[0-9A-Za-z]{2}vbc[a-zA-Z0-9]*';
