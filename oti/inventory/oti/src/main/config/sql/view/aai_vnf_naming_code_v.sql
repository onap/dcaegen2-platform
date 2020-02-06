CREATE VIEW dti.aai_vnf_naming_code_v AS
SELECT upper(a.vnf_name) as vnfName,
a.nf_naming_code as nfNamingCode,
c.nfc_naming_code as nfcNamingCode,
d.service_description as serviceDescription
FROM dti.rt_generic_vnf a,
dti.rt_relationship_list b,
dti.rt_vnfc c,
dti.rt_service d
WHERE b.related_from='generic-vnf'
and b.related_to = 'vnfc'
and b.from_node_id = a.vnf_id
and b.to_node_id = c.vnfc_name
and a.service_id = d.service_id;
