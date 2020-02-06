DROP TABLE IF EXISTS dti.rt_entity_config CASCADE;
EXCEPTION WHEN OTHERS THEN NULL; END;

CREATE TABLE dti.rt_entity_config
(
        entity        VARCHAR(50) NOT NULL,
        action        VARCHAR(30) NOT NULL,
        network       VARCHAR(20) NOT NULL,
        subnetwork    VARCHAR(10) NOT NULL,
        vendor        VARCHAR(40) NOT NULL,
        service       VARCHAR(150),
        taskname      VARCHAR(200),
        UPDATED_ON                VARCHAR(20)
);

CREATE INDEX rt_entity_config_idx1
        ON dti.rt_entity_config(entity)
;

CREATE INDEX rt_entity_config_idx2
        ON dti.rt_entity_config(network, subnetwork, vendor)
;
