CREATE TABLE IF NOT EXISTS dti.rt_vserver
(
    VSERVER_ID                 VARCHAR(150 ) NOT NULL,
    VSERVER_NAME               VARCHAR(100) NOT NULL,
    VSERVER_NAME2              VARCHAR(100),
    PROV_STATUS                VARCHAR(10),
    VSERVER_SELFLINK           VARCHAR(4000),
    IN_MAINT                   VARCHAR(1),
    IS_CLOSED_LOOP_DISABLED    VARCHAR(1),
    RESOURCE_VERSION           VARCHAR(25),
    TENANT_ID                  VARCHAR(150) NOT NULL,
    CLOUD_OWNER                VARCHAR(25)  NOT NULL,
    CLOUD_REGION_ID            VARCHAR(20)  NOT NULL,
    UPDATED_ON                VARCHAR(20),
    PRIMARY KEY (VSERVER_ID,TENANT_ID,CLOUD_OWNER,CLOUD_REGION_ID)
);

CREATE INDEX IF NOT EXISTS rt_vserver_idx 
    ON dti.rt_vserver(vserver_id)
;