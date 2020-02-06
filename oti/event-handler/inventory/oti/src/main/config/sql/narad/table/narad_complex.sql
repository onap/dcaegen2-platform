CREATE TABLE IF NOT EXISTS dti.narad_complex
(
	physical_location_id	VARCHAR(250)	 NOT NULL,
	data_center_code	VARCHAR(20),
	complex_name	VARCHAR(20),
	identity_url	VARCHAR(200),
	resource_version	VARCHAR(25),
	physical_location_type	VARCHAR(250) NOT NULL,
	street1	VARCHAR(100)	 NOT NULL,
	street2	VARCHAR(100),
	city	VARCHAR(25)	 NOT NULL,
	state	VARCHAR(10),
	postal_code	VARCHAR(10)	 NOT NULL,
	country	VARCHAR(50)	 NOT NULL,
	region	VARCHAR(20)	 NOT NULL,
	latitude	VARCHAR(25),
	longitude	VARCHAR(25),
	elevation	VARCHAR(25),
	lata	VARCHAR(10),
	UPDATED_ON   VARCHAR(20),
	PRIMARY KEY (PHYSICAL_LOCATION_ID)
);
