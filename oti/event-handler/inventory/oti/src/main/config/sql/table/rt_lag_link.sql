CREATE TABLE IF NOT EXISTS dti.rt_lag_link
(
    LINK_NAME           VARCHAR(64) NOT NULL,
    RESOURCE_VERSION    VARCHAR(25),
    UPDATED_ON          VARCHAR(20),
    PRIMARY KEY (LINK_NAME)
);
