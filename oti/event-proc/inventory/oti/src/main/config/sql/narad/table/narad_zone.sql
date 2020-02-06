CREATE TABLE IF NOT EXISTS dti.narad_zone
(
    ZONE_ID                   VARCHAR(150) NOT NULL PRIMARY KEY,
    ZONE_NAME                 VARCHAR(50) NOT NULL,
    DESIGN_TYPE               VARCHAR(20) NOT NULL,
    ZONE_CONTEXT              VARCHAR(20) NOT NULL,
    STATUS                    VARCHAR(20),
    RESOURCE_VERSION          VARCHAR(25),
	IN_MAINT                  VARCHAR(1) NOT NULL,
    UPDATED_ON                VARCHAR(20)
);
