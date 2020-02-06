CREATE OR REPLACE VIEW dti.rt_process_devconf_v AS
SELECT p.process_queued_datetime
,p.entity
,p.entity_key
,p.action
,p.vserver_name
,p.timestamp
,p.rt_process_status
,nd.name_regexp
,ec.network
,ec.subnetwork
,ec.vendor
,ec.service
,ec.taskname
FROM dti.rt_process_queue p
INNER JOIN dti.rt_entity_config ec
ON p.entity = ec.entity
AND p.action = ec.action
INNER JOIN dti.rt_network_device nd
ON nd.network = ec.network
AND nd.subnetwork = ec.subnetwork
AND nd.vendor = ec.vendor
WHERE REGEXP_LIKE(p.vserver_name, nd.name_regexp, 'i')
;
