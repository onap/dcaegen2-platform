CREATE OR REPLACE VIEW dti.rt_event_devconf_v AS
SELECT e.entity
,e.entity_key
,e.action
,e.parm
,e.timestamp
,e.rt_event_status
,nd.name_regexp
,ec.network
,ec.subnetwork
,ec.vendor
,ec.service
,ec.taskname
FROM dti.rt_event_queue e
INNER JOIN dti.rt_entity_config ec
ON e.entity = ec.entity
AND e.action = ec.action
INNER JOIN dti.rt_network_device nd
ON nd.network = ec.network
AND nd.subnetwork = ec.subnetwork
AND nd.vendor = ec.vendor
;
