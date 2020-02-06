CREATE TABLE IF NOT EXISTS dti.community_string
(
	resource_type        VARCHAR(50) NOT NULL,
	resource_name        VARCHAR(100) NOT NULL,
	community_string     VARCHAR(150) NOT NULL,
	snmp_version         VARCHAR(20),
	reservation_id       VARCHAR(50),
	updated_on           VARCHAR(20),
	PRIMARY KEY ( resource_type, resource_name )
);
