CREATE TABLE IF NOT EXISTS dti.rt_owning_entity
(
    owning_entity_id      VARCHAR(100) NOT NULL,
    owning_entity_name    VARCHAR(50) NOT NULL,
    resource_version      VARCHAR(25),
    updated_on            VARCHAR(20),
    PRIMARY KEY (owning_entity_id)
);
