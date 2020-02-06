BEGIN;
\i rt_forwarding_path.sql
\i rt_forwarder.sql
\i community_string.sql
COMMIT;

alter table dti.rt_pserver add column host_profile VARCHAR(100);

alter table dti.rt_vnf_image rename att_uuid to vnf_image_uuid;

alter table dti.rt_physical_link add column service_provider_bandwidth_up_value VARCHAR(20);

alter table dti.rt_physical_link add column service_provider_bandwidth_up_units VARCHAR(10);

alter table dti.rt_physical_link add column service_provider_bandwidth_down_value VARCHAR(20);

alter table dti.rt_physical_link add column service_provider_bandwidth_down_units VARCHAR(10);

alter table dti.rt_subnet add column subnet_role VARCHAR(50);

alter table dti.rt_subnet add column ip_assignment_direction VARCHAR(50);

alter table dti.rt_subnet add column subnet_sequence VARCHAR(20);

alter table dti.rt_l_interface add column allowed_address_pairs VARCHAR(500);

alter table dti.rt_p_interface add column selflink VARCHAR(4000);

DROP TABLE IF EXISTS dti.dcae_event CASCADE;

CREATE TABLE dti.dcae_event
(
dcae_target_name                           VARCHAR(150) NOT NULL,
dcae_target_type                           VARCHAR(15) NOT NULL,
dcae_service_location                      VARCHAR(50) NOT NULL,
dcae_service_action                        VARCHAR(15) NOT NULL,
dcae_target_prov_status                    VARCHAR(15) NOT NULL,
dcae_service_type                          VARCHAR(150),
dcae_target_in_maint                       VARCHAR(5),
dcae_target_is_closed_loop_disabled        VARCHAR(5),
dcae_service_instance_model_invariant_id   VARCHAR(150),
dcae_service_instance_model_version_id     VARCHAR(100),
dcae_generic_vnf_model_invariant_id        VARCHAR(150),
dcae_generic_vnf_model_version_id          VARCHAR(100),
dcae_target_service_description            VARCHAR(150),
dcae_target_collection                     VARCHAR(5),
dcae_target_collection_ip                  VARCHAR(20),
dcae_snmp_community_string                 VARCHAR(100),
dcae_snmp_version                          VARCHAR(20),
event                                      TEXT,
aai_additional_info                        TEXT,
dcae_event_sent_flag                       VARCHAR(5),
dcae_event_status                          VARCHAR(10),
dcae_event_retry_interval                  VARCHAR(10),
dcae_event_retry_number                    VARCHAR(10),
updated_on                                 VARCHAR(20)
);

CREATE UNIQUE INDEX dcae_event_idx1
    ON dti.dcae_event(dcae_target_name, dcae_target_type)
;

alter table dti.rt_relationship_list add column relationship_label varchar(100);

