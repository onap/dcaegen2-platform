BEGIN;
\echo executing rt_cp.sql
\i rt_cp.sql

\echo executing rt_cloud_vip_ipv4_address_list.sql
\i rt_cloud_vip_ipv4_address_list.sql

\echo executing rt_cloud_vip_ipv6_address_list.sql
\i rt_cloud_vip_ipv6_address_list.sql


COMMIT;

\echo executing "alter table dti.rt_vnfc add column MODEL_CUSTOMIZATION_ID VARCHAR(150);"
alter table dti.rt_vnfc ADD COLUMN MODEL_CUSTOMIZATION_ID VARCHAR(150);

\echo executing "ALTER TABLE dti.community_string RENAME PATTERN TO RESERVATION_ID;"
ALTER TABLE dti.community_string RENAME PATTERN TO RESERVATION_ID;

\echo executing "ALTER TABLE dti.dcae_event ADD COLUMN dcae_target_cloud_region_id VARCHAR(20);"
ALTER TABLE dti.dcae_event ADD COLUMN dcae_target_cloud_region_id VARCHAR(20);

\echo executing "ALTER TABLE dti.dcae_event ADD COLUMN dcae_target_cloud_region_version VARCHAR(10);"
ALTER TABLE dti.dcae_event ADD COLUMN dcae_target_cloud_region_version VARCHAR(10);

\echo ececuting "CREATE OR REPLACE VIEW dti.aai_vnf_app_vendor_v AS SELECT gv.vnf_name AS vnfName, vi.application_vendor AS applicationVendor FROM dti.rt_generic_vnf gv JOIN dti.rt_relationship_list rl ON gv.vnf_id = rl.from_node_id AND rl.related_from = 'generic-vnf' JOIN dti.rt_vnf_image vi ON vi.vnf_image_uuid = rl.to_node_id AND rl.related_to = 'vnf-image';"
CREATE OR REPLACE VIEW dti.aai_vnf_app_vendor_v AS SELECT gv.vnf_name AS vnfName, vi.application_vendor AS applicationVendor FROM dti.rt_generic_vnf gv JOIN dti.rt_relationship_list rl ON gv.vnf_id = rl.from_node_id AND rl.related_from = 'generic-vnf' JOIN dti.rt_vnf_image vi ON vi.vnf_image_uuid = rl.to_node_id AND rl.related_to = 'vnf-image';
