-- ================================================================================
-- Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ============LICENSE_END=========================================================

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

CREATE OR REPLACE FUNCTION dti.trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_modified_ts = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON dti.dtih_event_ack
FOR EACH ROW
EXECUTE PROCEDURE dti.trigger_set_timestamp();

CREATE TRIGGER set_timestamp_evt
BEFORE UPDATE ON dti.dtih_event
FOR EACH ROW
EXECUTE PROCEDURE dti.trigger_set_timestamp();
