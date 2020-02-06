DROP TABLE IF EXISTS dti.rt_event_queue CASCADE;
EXCEPTION WHEN OTHERS THEN NULL; END;

CREATE TABLE dti.rt_event_queue
(
        event_queued_datetime    VARCHAR(17) NOT NULL,
        entity                   VARCHAR(50) NOT NULL,
        entity_key               VARCHAR(200) NOT NULL,
        action                   VARCHAR(10) NOT NULL,
        timestamp                VARCHAR(14) NOT NULL,
        has_relationship         CHAR(1) NOT NULL,
        parm                     VARCHAR(200),
        rt_event_status          VARCHAR(10) NOT NULL,
        UPDATED_ON                VARCHAR(20)
);

CREATE INDEX rt_event_queue_idx1
        ON dti.rt_event_queue(entity)
;
