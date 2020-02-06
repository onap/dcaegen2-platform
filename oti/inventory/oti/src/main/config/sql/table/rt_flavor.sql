CREATE TABLE IF NOT EXISTS dti.rt_flavor
(
    flavor_id           VARCHAR(100) NOT NULL,
    flavor_name         VARCHAR(100),
    flavor_vcpus        DOUBLE PRECISION,
    flavor_ram          DOUBLE PRECISION,
    flavor_disk         DOUBLE PRECISION,
    flavor_ephemeral    DOUBLE PRECISION,
    flavor_swap         VARCHAR(18),
    flavor_is_public    VARCHAR(1),
    flavor_selflink     VARCHAR(4000),
    flavor_disabled     VARCHAR(1),
    resource_version    VARCHAR(25),
    CLOUD_OWNER         VARCHAR(25)  NOT NULL,
    CLOUD_REGION_ID     VARCHAR(20)  NOT NULL,
    UPDATED_ON          VARCHAR(20),
    PRIMARY KEY (flavor_id, CLOUD_OWNER, CLOUD_REGION_ID)
);
