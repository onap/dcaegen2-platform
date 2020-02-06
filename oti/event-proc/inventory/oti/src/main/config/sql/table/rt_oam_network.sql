CREATE TABLE IF NOT EXISTS dti.rt_oam_network
(
    NETWORK_UUID                     VARCHAR(40) NOT NULL,
    NETWORK_NAME                     VARCHAR(25) NOT NULL,
    CVLAN_TAG                        DECIMAL(32) NOT NULL,
    IPV4_OAM_GTWY_ADDR               VARCHAR(20),
    IPV4_OAM_GTWY_ADDR_PREFIX_LEN    BIGINT,
    RESOURCE_VERSION                 VARCHAR(25),
    CLOUD_OWNER                      VARCHAR(25)  NOT NULL,
    CLOUD_REGION_ID                  VARCHAR(20)  NOT NULL,
    UPDATED_ON                       VARCHAR(20),
    PRIMARY KEY (NETWORK_UUID, CLOUD_OWNER, CLOUD_REGION_ID)
);
