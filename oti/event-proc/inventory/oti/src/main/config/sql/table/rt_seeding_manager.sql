CREATE TABLE IF NOT EXISTS dti.rt_seeding_manager
(
    HOSTNAME                    VARCHAR(150) NOT NULL,
    ICMP_IP                     VARCHAR(50),
    SNMP_IP                     VARCHAR(50),
    COMMUNITY_STRING            VARCHAR(50),
    SNMP_VERSION                VARCHAR(50),
    DESIGN_TYPE                 VARCHAR(50),
    LOCATION                    VARCHAR(50),
    DEVICE_TYPE                 VARCHAR(50),
    ENTITY_TYPE                 VARCHAR(50),
    FUNCTION_CODE               VARCHAR(50),
    OUTPUT_OBJECTS              VARCHAR(50),
    DEVICE_CHANGE_TIMESTAMP     VARCHAR(14) NOT NULL,
    ICMP_INTERVAL_CLASS         VARCHAR(50),
    FMMIB_POLLER_INTERVAL_CLASS VARCHAR(50),
    CHANGE_TYPE                 VARCHAR(50),
    UPDATED_ON                  VARCHAR(20),
    PRIMARY KEY (HOSTNAME)    
);
