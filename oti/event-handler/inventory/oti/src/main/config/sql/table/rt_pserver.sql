CREATE TABLE IF NOT EXISTS dti.rt_pserver
(
    HOSTNAME                   VARCHAR(50) NOT NULL,
    PTNII_EQUIP_NAME           VARCHAR(80),
    NUMBER_OF_CPUS             DOUBLE PRECISION,
    DISK_IN_GIGABYTES          DOUBLE PRECISION,
    RAM_IN_MEGABYTES           DOUBLE PRECISION,
    EQUIP_TYPE                 VARCHAR(40),
    EQUIP_VENDOR               VARCHAR(40),
    EQUIP_MODEL                VARCHAR(40),
    FQDN                       VARCHAR(100),
    PSERVER_SELFLINK           VARCHAR(4000),
    IPV4_OAM_ADDRESS           VARCHAR(20),
    SERIAL_NUMBER              VARCHAR(100),
    IPADDRESS_V4_LOOPBACK_0    VARCHAR(20),
    IPADDRESS_V6_LOOPBACK_0    VARCHAR(45),
    IPADDRESS_V4_AIM           VARCHAR(20),
    IPADDRESS_V6_AIM           VARCHAR(45),
    IPADDRESS_V6_OAM           VARCHAR(45),
    INV_STATUS                 VARCHAR(20),
    PSERVER_ID                 VARCHAR(150),
    IN_MAINT                   VARCHAR(1),
    INTERNET_TOPOLOGY          VARCHAR(100),
    RESOURCE_VERSION           VARCHAR(25),
    PSERVER_NAME2              VARCHAR(256),
    PURPOSE                    VARCHAR(150),
    PROV_STATUS                VARCHAR(20),
    MANAGEMENT_OPTION          VARCHAR(20),
    HOST_PROFILE               VARCHAR(100),
    UPDATED_ON                 VARCHAR(20),
    PRIMARY KEY (HOSTNAME)
);
