\echo executing "CREATE TABLE IF NOT EXISTS dti.narad_autonomous_system"
CREATE TABLE IF NOT EXISTS dti.narad_autonomous_system
(
		AUTONOMOUS_SYSTEM_ID		VARCHAR(150)	NOT NULL,
		AUTONOMOUS_SYSTEM_NUMBER	BIGINT			NOT NULL,
		AUTONOMOUS_SYSTEM_TYPE		VARCHAR(100)	NOT NULL,
		AUTONOMOUS_SYSTEM_REGION	VARCHAR(100)	NULL,
		AUTONOMOUS_SYSTEM_ROLE		VARCHAR(100)	NULL,
		RESOURCE_VERSION			VARCHAR(25)		NULL,
		UPDATED_ON					VARCHAR(20)		NULL,
		PRIMARY KEY (AUTONOMOUS_SYSTEM_ID)
);

\echo executing "CREATE TABLE IF NOT EXISTS dti.dcae_db_installation"
CREATE TABLE IF NOT EXISTS dti.dcae_db_installation
(
		dti_db_version		VARCHAR(20)		NOT NULL,		
		status				VARCHAR(20)		NULL,
		UPDATED_ON			VARCHAR(20)		NULL,
		PRIMARY KEY (dti_db_version)
);

\echo executing "CREATE VIEW V_NARAD_PNF_REGION"

CREATE OR REPLACE VIEW dti.v_narad_pnf_region AS 
SELECT p.pnf_name, p.nf_function, p.nf_naming_code, p.nf_role, a.autonomous_system_number asn, a.autonomous_system_region as_region, a.autonomous_system_role as_role,
CASE
	WHEN upper(a.autonomous_system_region)='USA' THEN 'CBB'
	WHEN upper(a.autonomous_system_region)='AP' THEN 'AP'
	WHEN upper(a.autonomous_system_region)='EMEA' THEN 'EME'
	WHEN upper(a.autonomous_system_region)='CANADA' THEN 'CAN'
	WHEN upper(a.autonomous_system_region)='CALA' THEN 'CAL'
END netid,
ig.instance_group_name region_id
FROM dti.narad_pnf p, dti.narad_autonomous_system a, dti.narad_instance_group ig, 
dti.narad_relationship_list rs1, dti.narad_relationship_list rs2, dti.narad_relationship_list rs3
WHERE rs1.related_from= 'pnf' AND rs1.related_to='configuration'
AND p.pnf_name=rs1.from_node_id AND rs1.to_node_id=rs2.from_node_id and rs2.related_to='instance-group' and rs2.to_node_id=ig.id
AND p.pnf_name=rs3.to_node_id and rs3.from_node_id=a.autonomous_system_id;
 