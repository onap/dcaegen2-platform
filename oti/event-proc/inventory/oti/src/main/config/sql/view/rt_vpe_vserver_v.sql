CREATE OR REPLACE VIEW dti.RT_VPE_VSERVER_V (VNF_ID, VNF_NAME, VNF_NAME2, VNF_TYPE, SERVICE_ID, REGIONAL_RESOURCE_ZONE, PROV_STATUS, OPERATIONAL_STATUS, EQUIPMENT_ROLE, ORCHESTRATION_STATUS, HEAT_STACK_ID, MSO_CATALOG_KEY, IPV4_OAM_ADDRESS, IPV4_OAM_GTWY_ADDR_PRE_LEN, IPV4_OAM_GTWY_ADDR, V4_LOOPBACK0_IP_ADDRESS, VLAN_ID_OUTER, RESOURCE_VERSION, VSERVER_ID, VSERVER_NAME, VSERVER_NAME2, VSERVER_PROV_STATUS, VSERVER_SELFLINK, IN_MAINT, IS_CLOSED_LOOP_DISABLED, VSERVER_RESOURCE_VERSION, TENANT_ID) AS 
  SELECT a.VNF_ID,                     
	a.VNF_NAME,                   
	a.VNF_NAME2,                  
	a.VNF_TYPE,                   
	a.SERVICE_ID,                 
	a.REGIONAL_RESOURCE_ZONE,     
	a.PROV_STATUS,                
	a.OPERATIONAL_STATUS,          
	a.EQUIPMENT_ROLE,             
	a.ORCHESTRATION_STATUS,       
	a.HEAT_STACK_ID,              
	a.MSO_CATALOG_KEY,            
	a.IPV4_OAM_ADDRESS,           
	a.IPV4_OAM_GTWY_ADDR_PRE_LEN, 
	a.IPV4_OAM_GTWY_ADDR,         
	a.V4_LOOPBACK0_IP_ADDRESS,    
	a.VLAN_ID_OUTER,              
	a.RESOURCE_VERSION,       
	c.VSERVER_ID,              
	c.VSERVER_NAME,            
	c.VSERVER_NAME2,           
	c.PROV_STATUS as vserver_prov_status,             
	c.VSERVER_SELFLINK,        
	c.IN_MAINT,                
	c.IS_CLOSED_LOOP_DISABLED, 
	c.RESOURCE_VERSION as Vserver_RESOURCE_VERSION,          
	c.TENANT_ID               
FROM dti.rt_vpe a
 INNER JOIN dti.rt_relationship_list b
   ON a.vnf_id       =b.from_node_id
   AND b.related_from='vpe'
 INNER JOIN dti.rt_vserver c
   ON CONCAT(c.cloud_owner, '|', c.cloud_Region_id, '|', c.tenant_id, '|', c.vserver_id) = b.to_node_id
   AND b.related_to='vserver'
WHERE a.validto is null
and b.validto is null
and c.validto is null;
