CREATE SCHEMA IF NOT EXISTS dti AUTHORIZATION dti_admin;

CREATE SEQUENCE IF NOT EXISTS dti.dtih_event_event_id_seq;

CREATE TABLE IF NOT EXISTS dti.dtih_event
(
    dtih_event_id integer NOT NULL DEFAULT nextval('dti.dtih_event_event_id_seq'::regclass),
    create_ts timestamp with time zone DEFAULT now(),
    event jsonb,
    target_name character varying(80),
    target_type character varying(50),
    last_modified_ts timestamp with time zone,
    location_clli character varying(50),
    CONSTRAINT dtih_event_pkey PRIMARY KEY (dtih_event_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS "dtih_event_UK"
    ON dti.dtih_event USING btree
    (target_name, target_type)
    TABLESPACE pg_default;

CREATE SEQUENCE IF NOT EXISTS dti.dtih_event_ack_event_ack_id_seq;

CREATE TABLE IF NOT EXISTS dti.dtih_event_ack
(
    dtih_event_ack_id integer NOT NULL DEFAULT nextval('dti.dtih_event_ack_event_ack_id_seq'::regclass),
    last_modified_ts timestamp with time zone DEFAULT now(),
    k8s_cluster_fqdn character varying(80),
    k8s_proxy_fqdn character varying(80),
    k8s_pod_id character varying(80),
    dtih_event_id integer,
    k8s_namespace character varying(100),
    k8s_service_name character varying(120),
    k8s_service_port character varying(6),
    create_ts timestamp with time zone,
    action character varying(10),
    service_component character varying(120),
    deployment_id character varying(120),
    container_type character varying(10),
    docker_host character varying(120),
    container_id character varying(120),
    reconfig_script character varying(100),
    CONSTRAINT dtih_event_ack_pkey PRIMARY KEY (dtih_event_ack_id),
    CONSTRAINT event_ack_fk FOREIGN KEY (dtih_event_id)
        REFERENCES dti.dtih_event (dtih_event_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS fki_dtih_event_ack_fk
    ON dti.dtih_event_ack USING btree
    (dtih_event_id)
    TABLESPACE pg_default;
