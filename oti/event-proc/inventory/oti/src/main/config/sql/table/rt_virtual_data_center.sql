CREATE TABLE IF NOT EXISTS dti.rt_virtual_data_center
(
    VDC_ID              VARCHAR(150) NOT NULL,
    VDC_NAME            VARCHAR(40),
    RESOURCE_VERSION    VARCHAR(25),
    UPDATED_ON          VARCHAR(20),
    PRIMARY KEY (VDC_ID)
);
