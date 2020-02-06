DROP TABLE IF EXISTS dti.rt_network_device CASCADE;
EXCEPTION WHEN OTHERS THEN NULL; END;

CREATE TABLE dti.rt_network_device
(
        network       VARCHAR(20) NOT NULL,
        subnetwork    VARCHAR(10) NOT NULL,
        vendor        VARCHAR(40) NOT NULL,
        name_regexp   VARCHAR(100) NOT NULL,
        UPDATED_ON                VARCHAR(20)
);

CREATE INDEX rt_network_device_idx1
        ON dti.rt_network_device(network, subnetwork, vendor)
;
