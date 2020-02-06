CREATE TABLE IF NOT EXISTS dti.rt_forwarding_path
(
    FORWARDING_PATH_ID      VARCHAR(150) NOT NULL,
    FORWARDING_PATH_NAME    VARCHAR(100) NOT NULL,
    SELFLINK                VARCHAR(4000),
    RESOURCE_VERSION        VARCHAR(25),
    UPDATED_ON              VARCHAR(20),
    PRIMARY KEY (FORWARDING_PATH_ID)
);
