CREATE TABLE IF NOT EXISTS dti.rt_network_profile
(
    NM_PROFILE_NAME     VARCHAR(100) NOT NULL,
    COMMUNITY_STRING    VARCHAR(4000),
    RESOURCE_VERSION    VARCHAR(25),
    UPDATED_ON          VARCHAR(20),
    PRIMARY KEY  (NM_PROFILE_NAME)
);
