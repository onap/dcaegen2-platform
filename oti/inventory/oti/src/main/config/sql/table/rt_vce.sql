CREATE TABLE IF NOT EXISTS dti.rt_vce
(
    VNF_ID                       VARCHAR(150) NOT NULL,
    VNF_NAME                     VARCHAR(40) NOT NULL,
    VNF_NAME2                    VARCHAR(256),
    VNF_TYPE                     VARCHAR(40) NOT NULL,
    SERVICE_ID                   VARCHAR(150),
    REGIONAL_RESOURCE_ZONE       VARCHAR(20),
    PROV_STATUS                  VARCHAR(20),
    OPERATIONAL_STATUS           VARCHAR(20),
    EQUIPMENT_ROLE               VARCHAR(20),
    ORCHESTRATION_STATUS         VARCHAR(25),
    HEAT_STACK_ID                VARCHAR(150),
    MSO_CATALOG_KEY              VARCHAR(200),
    VPE_ID                       VARCHAR(150),
    V6_VCE_WAN_ADDRESS           VARCHAR(45),
    IPV4_OAM_ADDRESS             VARCHAR(20),
    RESOURCE_VERSION             VARCHAR(25),
    IPV4_LOOPBACK0_ADDRESS       VARCHAR(20),
    ENTITLEMENT_RESOURCE_UUID    VARCHAR(150),
    UPDATED_ON                   VARCHAR(20),
    PRIMARY KEY (VNF_ID)
);
