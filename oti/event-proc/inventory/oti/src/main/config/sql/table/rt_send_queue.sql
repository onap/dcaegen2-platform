DROP TABLE IF EXISTS dti.rt_send_queue CASCADE;
CREATE TABLE dti.rt_send_queue
(
        SEND_QUEUED_DATETIME	VARCHAR(17) NOT NULL,
        UPDATED_OBJECT		VARCHAR(50) NOT NULL,
        TIMESTAMP               VARCHAR(14) NOT NULL,
        RT_SEND_STATUS		VARCHAR(5) NOT NULL,
        UPDATED_ON                VARCHAR(20)
)
;

CREATE INDEX rt_send_queue_idx1
        ON dti.rt_send_queue(UPDATED_OBJECT)
;
