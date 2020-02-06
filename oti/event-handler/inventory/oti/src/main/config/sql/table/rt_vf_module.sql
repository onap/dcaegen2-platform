CREATE TABLE IF NOT EXISTS dti.rt_vf_module
(
    VF_MODULE_ID                      VARCHAR(150) NOT NULL,
    VF_MODULE_NAME                    VARCHAR(100),
    MODEL_INVARIANT_ID                VARCHAR(150),
    MODEL_VERSION_ID                  VARCHAR(100),
    MODEL_CUSTOMIZATION_ID            VARCHAR(150),
    WIDGET_MODEL_ID                   VARCHAR(150),
    WIDGET_MODEL_VERSION              VARCHAR(25),
    HEAT_STACK_ID                     VARCHAR(150),
    IS_BASE_VF_MODULE                 VARCHAR(1) NOT NULL,
    ORCHESTRATION_STATUS              VARCHAR(25),
    RESOURCE_VERSION                  VARCHAR(25) NOT NULL,
    CONTRAIL_SERVICE_INSTANCE_FQDN    VARCHAR(200),
    MODULE_INDEX                      BIGINT,
    SELFLINK                          VARCHAR(4000),
    UPDATED_ON                        VARCHAR(20),
    PRIMARY KEY (VF_MODULE_ID)
);
