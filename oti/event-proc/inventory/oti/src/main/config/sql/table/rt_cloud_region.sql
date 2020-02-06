CREATE TABLE IF NOT EXISTS dti.rt_cloud_region
(
    CLOUD_OWNER             VARCHAR(25)  NOT NULL,
    CLOUD_REGION_ID         VARCHAR(20)  NOT NULL,
    CLOUD_TYPE              VARCHAR(20)  NULL,
    OWNER_DEFINED_TYPE      VARCHAR(20)  NULL,
    CLOUD_REGION_VERSION    VARCHAR(10)  NULL,
    IDENTITY_URL            VARCHAR(200) NULL,
    CLOUD_ZONE              VARCHAR(50)  NULL,
    COMPLEX_NAME            VARCHAR(20)  NULL,
    SRIOV_AUTOMATION        VARCHAR(50)   NULL,
    RESOURCE_VERSION        VARCHAR(25)  NULL,
    UPGRADE_CYCLE           VARCHAR(25)  NULL,
    ORCHESTRATION_DISABLED  VARCHAR(1)   NULL,
    IN_MAINT                VARCHAR(1)   NULL,
    UPDATED_ON              VARCHAR(20)  NULL,
    PRIMARY KEY (CLOUD_OWNER,CLOUD_REGION_ID)
);

CREATE INDEX IF NOT EXISTS rt_cloud_region_idx 
        ON dti.rt_cloud_region ( CLOUD_REGION_ID )
;
