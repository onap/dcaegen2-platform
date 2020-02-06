CREATE TABLE IF NOT EXISTS dti.vertica_feed
(
    ENTITY_TYPE     VARCHAR(50) NOT NULL,
    ACTION          VARCHAR(20) NOT NULL,
    ENTITY_VALUE    TEXT,
    UPDATED_ON      VARCHAR(20),
    PRIMARY KEY (ENTITY_TYPE, ACTION, ENTITY_VALUE, UPDATED_ON)
);
