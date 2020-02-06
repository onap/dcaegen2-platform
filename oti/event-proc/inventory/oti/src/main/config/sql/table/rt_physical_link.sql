CREATE TABLE IF NOT EXISTS dti.rt_physical_link
(
    link_name                                VARCHAR(100) NOT NULL,
    speed_value                              DOUBLE PRECISION,
    speed_units                              VARCHAR(10),
    circuit_id                               VARCHAR(45),
    dual_mode                                VARCHAR(20),
    resource_version                         VARCHAR(25),
    management_option                        VARCHAR(20),
    SERVICE_PROVIDER_NAME                    VARCHAR(50),
    SERVICE_PROVIDER_BANDWIDTH_UP_VALUE      VARCHAR(20),
    SERVICE_PROVIDER_BANDWIDTH_UP_UNITS      VARCHAR(10),
    SERVICE_PROVIDER_BANDWIDTH_DOWN_VALUE    VARCHAR(20),
    SERVICE_PROVIDER_BANDWIDTH_DOWN_UNITS    VARCHAR(10), 
    UPDATED_ON                               VARCHAR(20),
    PRIMARY KEY (link_name)
);
