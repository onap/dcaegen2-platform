CREATE OR REPLACE VIEW dti.aai_vnf_app_vendor_v AS
SELECT gv.vnf_name AS vnfName, vi.application_vendor AS applicationVendor
FROM dti.rt_generic_vnf gv
JOIN dti.rt_relationship_list rl ON gv.vnf_id = rl.from_node_id AND rl.related_from = 'generic-vnf'
JOIN dti.rt_vnf_image vi ON vi.vnf_image_uuid = rl.to_node_id AND rl.related_to = 'vnf-image';
