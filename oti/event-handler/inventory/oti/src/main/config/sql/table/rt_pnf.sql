CREATE TABLE IF NOT EXISTS dti.rt_pnf
(
    PNF_NAME                   VARCHAR(40)  NOT NULL,
    PNF_NAME2                  VARCHAR(40)  NULL,
    PNF_NAME2_SOURCE           VARCHAR(50)  NULL,
    PNF_ID                     VARCHAR(150) NULL,
    NF_NAMING_CODE			   VARCHAR(50)  NULL,
    EQUIP_TYPE                 VARCHAR(40)  NULL,
    EQUIP_VENDOR               VARCHAR(40)  NULL,
    EQUIP_MODEL                VARCHAR(250) NULL,
    MANAGEMENT_OPTION          VARCHAR(20)  NULL,
    ORCHESTRATION_STATUS	   VARCHAR(50)  NULL,
    IPADDRESS_V4_OAM           VARCHAR(20)  NULL,
    SW_VERSION                 VARCHAR(25)  NULL,
    IN_MAINT                   VARCHAR(1)   NOT NULL,
    FRAME_ID                   VARCHAR(100) NULL,
    SERIAL_NUMBER              VARCHAR(100) NULL,
    IPADDRESS_V4_LOOPBACK_0    VARCHAR(20)  NULL,
    IPADDRESS_V6_LOOPBACK_0    VARCHAR(45)  NULL,
    IPADDRESS_V4_AIM           VARCHAR(20)  NULL,
    IPADDRESS_V6_AIM           VARCHAR(45)  NULL,
    IPADDRESS_V6_OAM           VARCHAR(45)  NULL,
    INV_STATUS                 VARCHAR(20)  NULL,
    RESOURCE_VERSION           VARCHAR(25)  NULL,
    PROV_STATUS                VARCHAR(10)  NULL,
    NF_ROLE                    VARCHAR(50)  NULL,
    SELFLINK                   VARCHAR(4000)  NULL,
    UPDATED_ON                 VARCHAR(20)  NULL,
    PRIMARY KEY (PNF_NAME)
);
