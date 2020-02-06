DROP TABLE IF EXISTS dti.rt_vpe CASCADE;

CREATE TABLE dti.rt_vpe
(
    VNF_ID                        VARCHAR(150) NOT NULL,
    VNF_NAME                      VARCHAR(100) NOT NULL,
    VNF_NAME2                     VARCHAR(256),
    VNF_TYPE                      VARCHAR(40) NOT NULL,
    SERVICE_ID                    VARCHAR(150),
    REGIONAL_RESOURCE_ZONE        VARCHAR(20),
    PROV_STATUS                   VARCHAR(10),
    OPERATIONAL_STATUS            VARCHAR(20),
    EQUIPMENT_ROLE                VARCHAR(20),
    ORCHESTRATION_STATUS          VARCHAR(25),
    HEAT_STACK_ID                 VARCHAR(150),
    MSO_CATALOG_KEY               VARCHAR(200),
    IPV4_OAM_ADDRESS              VARCHAR(20),
    IPV4_OAM_GTWY_ADDR_PRE_LEN    DECIMAL(38),
    IPV4_OAM_GTWY_ADDR            VARCHAR(20),
    V4_LOOPBACK0_IP_ADDRESS       VARCHAR(20),
    VLAN_ID_OUTER                 VARCHAR(64),
    RESOURCE_VERSION              VARCHAR(25),
    as_number                     VARCHAR(20),
    SUMMARY_STATUS                VARCHAR(250),
    ENCRYPTED_ACCESS_FLAG         VARCHAR(1),
    UPDATED_ON                VARCHAR(20)
);

CREATE UNIQUE INDEX rt_vpe_idx1
    ON dti.rt_vpe(VNF_ID)
;
