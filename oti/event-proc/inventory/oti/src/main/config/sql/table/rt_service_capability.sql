CREATE TABLE IF NOT EXISTS dti.rt_service_capability
(
    SERVICE_TYPE        VARCHAR(100) NOT NULL,
    VNF_TYPE            VARCHAR(20) NOT NULL,
    RESOURCE_VERSION    VARCHAR(25),
    UPDATED_ON          VARCHAR(20),
    PRIMARY KEY (SERVICE_TYPE)
);
