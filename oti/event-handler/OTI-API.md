# OTI Handler API
*Version 1.0.0*

---
<a name="toc"></a>
## Contents

* Overview
* Managing OTI Handler
  - [GET /healthcheck](#healthcheck)
  - [GET /shutdown](#shutdown)
* Triggering notifications to microservices
  - [POST /events](#events-post)
* Queries for information from Consul KVs
  - [GET /policies](#policies)
  - [GET /service_component](#service_component)
  - [GET /service_component_all](#service_component_all)
* Queries for OTI events from OTI database
  - [GET /dti_docker_events](#dti_docker_events)
  - [GET /dti_k8_events](#dti_k8_events)
  
---
<a name="overview"></a>
## Overview
This document describes OTI Handler's HTTPS API for:
1. accepting events from OTI to reconfigure microservices.
1. sending reconfig notifications to microservices.
1. retrieving service component information from Consul.

---
<a name="manage"></a>
## Managing OTI Handler

---
<a name="healthcheck">healthcheck</a>
### GET /healthcheck

#### Description
Get health status of OTI Handler service

#### Parameters
None

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[HealthCheckResponse](#healthcheckresponse)|

<a name="healthcheckresponse"></a>
##### HealthCheckResponse
OTI Handler returns a JSON object containing current health check information.

Example JSON response, formatted:
```
{
    "now": "2019-10-19 18:30:34.987514",
    "packages": "N/A",
    "python": "3.6.0 (default, Oct 10 2019, 02:49:49) [GCC 4.9.2]",
    "service_instance_UUID": "0cf593cd-83d4-4cdc-b8bb-a33f8edc28f4",
    "service_name": "oti_handler",
    "service_version": "3.6.0",
    "started": "2019-10-18 18:50:10.209063",
    "stats": {
        "oti_handler": {
            "ave_timer_millisecs": 207.0,
            "call_count": 5,
            "error_count": 0,
            "last_error": "None",
            "last_success": "2019-10-18 19:25:36.604613",
            "longest_timer_millisecs": 348
        }
    },
    "uptime": "23:40:24.778451"
}
```

---
<a name="shutdown"></a>
### GET /shutdown

#### Description
Shutdown and restart OTI Handler

#### Parameters
None

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|string|

OTI Handler returns a string acknowledging the shutdown request.

Example response:
```
goodbye! shutdown requested 2019-10-19 18:34:22.642045
```


---
<a name="notifications"></a>
## Triggering notifications to microservices
---
<a name="events-post"></a>
### POST /events?**notify**=n

#### Description
(For OTI to) Submit a OTI Event to OTI Handler

**/events** is
for OTI to signal add, update, delete of a VNF instance or to signal an entire site activation/deactivation.
OTI POSTs an Event when the VNF in question needs monitoring by DCAE monitoring services.
The OTI Event includes information that identifies the type of VNF, the specific VNF instance,
the location where the VNF is running, the type of operation, and
additional information that may be useful for setting up the DCAE monitoring services.

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|Body|**dcae_event**  <br>*required*|JSON event from OTI that must contain<br>at least the few fields identified below.|[DCAEEvent](#dcaeevent)||
|query component|**notify**  <br>*optional*|When set to "n", oti handler will **not** notify components of this OTI Event<br> and only persist the event into OTI postgreSQL database.|string|y|

<a name="dcaeevent"></a>
##### DCAEEvent
OTI Handler uses the following fields of the OTI Event JSON object to identify which monitoring services to reconfigure:

|Name|Description|Schema|
|---|---|---|
|**dcae_service_action**  <br>*required*|Indicates the action to be taken by a DCAE service [deploy, modify or remove a VNF instance monitoring].<br>Valid values are: "add", "update", "delete", "notify". <br>Docker hosted microservices will continue to be signaled with "deploy" or "undeploy action".<br> A "notify" action for kubernetes hosted collector services can signal all the services to be activated or deactivated.|string|
|**dcae_target_name**  <br>*required*|The name of the unique VNF Instance monitored by DCAE services.|string|
|**dcae_target_type**  <br>*required*|The VNF Type of the VNF Instance monitored by a DCAE service.|string|
|**dcae_service_location**  <br>*optional*|CLLI location.  Not provided or "" infers all locations.|string|

Any additional fields of the OTI Event are ignored by OTI Handler and are passed over to the collector services.
The entire OTI Event object is saved in OTI handler application database, for Config Binding Service to provide to the monitoring services.

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Accepted.  Any errors will be noted in the JSON object response.|[DCAEEventResponse](#dcaeeventresponse)|

<a name="dcaeeventresponse"></a>
##### DCAEEventResponse
OTI Handler returns a JSON object containing results of running each affected service component's dti reconfig script in case of docker containers and the HTTP response acknowledgement from kubernetes pods.
If the object is empty or only contains an ERROR, then no collector component's reconfig script was run.

|Name|Description|Schema|
|---|---|---|
|**\<service_component_name\>**  <br>*optional*,<br>*can be multiple*|Identification of a component's docker container<br>and results of running its reconfig script.<br>One for each service notified about this event.|string|
|**ERROR**  <br>*optional*|An error not specific to any one **\<service_component_name\>**.|string|

Examples of JSON responses, formatted:
```
{
    "0b231f372c6f42a89817a097549f4af5_green-eggs-and-ham": "ran /opt/app/reconfigure.py in container 9a9045657e097a4e41b077d10a0c8b2e860a9993e90e1c2a6997b03c2287d86f on zldcmtn23adcc1-platform-dockerhost-1 that was deployed by policy-test-terry node green_eggs_and_ham_docker_container, got: reconfigured\n",
    "add6bcffdf16488cb961ac88605243a6_green-eggs-and-ham": "ran /opt/app/reconfigure.py in container dab026db7c33081f89b0de54a5a8ed1eed4bcf6bac783a1f657c3018a24f522e on zldcmtn23adcc1-platform-dockerhost-1 that was deployed by app_reconfigurable_tes node green_eggs_and_ham_docker_container, got: rpc error: code = 13 desc = invalid header field value \"oci runtime error: exec failed: container_linux.go:247: starting container process caused \\\"exec: \\\\\\\"/opt/app/reconfigure.py\\\\\\\": stat /opt/app/reconfigure.py: no such file or directory\\\"\\n\"\r\n",
    "DTIHandler": {
        "deploymentIds": [
            "43b77ab2-7341-4929-ba27-ea91d00b253c"
        ],
        "requestId": "ab88d651-fa83-4342-9579-d383c1f29373"
    }
}

{
    "zldcdyh1bdce1d13-vcc-clicollector-cli-p1-v12": "ran add in k8s deployment sts-zldcdyh1bdce1d13-vcc-clicollector-cli-p1-v12 in namespace com-my-dcae-collgrp1-dev that was deployed by dcae_vcc-clicollector-cli
-p1-k8_2002_vp663p_1120v7 node vcc-clicollector-cli-p1_vcc-clicollector-cli-p1, got: {u'KubeServicePort': u'9999', u'KubeNamespace': u'com-my-dcae-collgrp1-dev', u'KubeServiceName': u'zldcdyh1bdce1d13-vcc-clicoll
ector-cli-p1-v12', u'KubeClusterFqdn': u'32.68.210.134', u'KubeProxyFqdn': u'dcae-kcdthp-site1-edge-d13.test.idns.cip.my.com', u'KubePod': u'sts-zldcdyh1bdce1d13-vcc-clicollector-cli-p1-v12-1'}"
}

{
    "zldcdyh1bdce1d13-ovl-mib": "ran UPDATE to k8s pod ID sts-zldcdyh1bdce1d13-ovl-mib-0 in namespace com-my-dcae-poller-dev that was deployed in cluster 32.68.210.134, got: {u'KubeServicePort': u'9999', u'KubeNa
mespace': u'com-my-dcae-poller-dev', u'KubeServiceName': u'zldcdyh1bdce1d13-ovl-mib', u'KubeClusterFqdn': u'32.68.210.134', u'KubeProxyFqdn': u'dcae-kcdthp-site1-dyh1b-edge-d13.test.idns.cip.my.com', u'KubePod'
: u'sts-zldcdyh1bdce1d13-ovl-mib-0'}"
}

```


---
<a name="info-queries"></a>
## Queries for information from Consul KVs

---
<a name="dti"></a>
### GET /dti/**\<service_name\>**?**vnf_type**=\<vnf_type\>;**vnf_id**=\<vnf_id\>;**service_location**=\<service_location\>

#### Description
Retrieve current (latest, not undeployed) OTI Events

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|path segment|**\<service_name\>**  <br>*optional*|The service component name assigned by dockerplugin to the component<br>that is unique to the cloudify node instance and used in its Consul key(s).|string||
|query component|**vnf_type**  <br>*optional*|Allows multiple values separated by commas.  <br>Gets OTI events for these **\<vnf_type\>(s)**.|string||
|query component|**vnf_id**  <br>*optional*|Requires **vnf_type** also.  Gets OTI event for this **\<vnf_id\>**.|string||
|query component|**service_location**  <br>*optional*|Allows multiple values separated by commas.<br>Filters OTI events with dcae_service_location in **\<service_location\>**.<br>Overrides locations defined in Consul for the **\<service_name\>**.|string|locations from Consul|

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[DTIResponse](#dtiresponse)|

<a name="dtiresponse"></a>
##### OTIResponse
OTI Handler returns a JSON object of OTI event(s).
- If one **vnf_type** and **vnf_id** are both specified, then object returned will be just the one OTI event.
- If one **vnf_type** is specified but not **vnf_id**, then OTI events will be keyed by **\<vnf_id\>**.
- Otherwise the OTI events will be keyed by **\<vnf_type\>**, sub-keyed by **\<vnf_id\>**.

Example JSON response, formatted:
```
{
    "anot-her": {
        "another01ems003": {
            "aai_additional_info": {},
            "dcae_generic-vnf_model-version-id": "1",
            "dcae_service-instance_model-version-id": "1",
            "dcae_service_action": "deploy",
            "dcae_service_location": "LSLEILAA",
            "dcae_snmp_community_string": "my_first_community",
            "dcae_snmp_version": "2c",
            "dcae_target_collection": "true",
            "dcae_target_collection_ip": "107.239.85.3",
            "dcae_target_in-maint": "false",
            "dcae_target_is-closed-loop-disabled": "false",
            "dcae_target_name": "another01ems003",
            "dcae_target_prov-status": "PROV",
            "dcae_target_type": "anot-her",
            "event": {}
        },
        "another01ems044": {
            "aai_additional_info": {},
            "dcae_generic-vnf_model-version-id": "1",
            "dcae_service-instance_model-version-id": "1",
            "dcae_service_action": "deploy",
            "dcae_service_location": "LSLEILAA",
            "dcae_snmp_community_string": "my_first_community",
            "dcae_snmp_version": "2c",
            "dcae_target_collection": "true",
            "dcae_target_collection_ip": "107.239.85.44",
            "dcae_target_in-maint": "false",
            "dcae_target_is-closed-loop-disabled": "false",
            "dcae_target_name": "another01ems044",
            "dcae_target_prov-status": "PROV",
            "dcae_target_type": "anot-her",
            "event": {}
        }
    }
}
```

---
<a name="policies"></a>
### GET /policies/**\<service_name\>**?**policy_id**=\<policy_id\>

#### Description
Retrieve policy or policies for a service component instance

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|path segment|**\<service_name\>**  <br>*required*|The service component name assigned by dockerplugin to the component<br>that is unique to the cloudify node instance and used in its Consul key(s).|string||
|query component|**policy_id**  <br>*optional*|Returns only the policy for this one **\<policy_id\>**.|string||

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[PoliciesResponse](#policiesresponse)|

<a name="policiesresponse"></a>
##### PoliciesResponse
OTI Handler returns a JSON object containing policy bodies for the **\<service_name\>** component.
- If **policy_id** is specified, then object returned will be just the one policy body.
- If **policy_id** is not specified, then object will contain all policy bodies for **\<service_name\>**, keyed by **\<policy_id\>**.

Example JSON response, formatted:
```
{
    "DCAE_FTL3B.Config_Green_Collectors": {
        "config": {
            "conflicting_key": "green_collectors_wins",
            "package_type": "plastic",
            "polling_frequency": "30m",
            "power_source": "lemmings"
        },
        "matchingConditions": {
            "ConfigName": "Green_Collectors",
            "testName": "dcae",
            "ONAPName": "dcae"
        },
        "policyConfigMessage": "Config Retrieved! ",
        "policyConfigStatus": "CONFIG_RETRIEVED",
        "policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml",
        "policyVersion": "1",
        "property": null,
        "responseAttributes": {},
        "type": "JSON"
    },
    "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific": {
        "config": {
            "bacon": "soft, not crispy",
            "bread": "pumpernickel",
            "conflicting_key": "green_eggs_and_ham_are_better",
            "dcae_target_type": [
                "pnga-xxx",
                "pcrf-oam",
                "vhss-ems",
                "anot-her",
                "new-type"
            ],
            "egg_color": "green",
            "preparation": "scrambled"
        },
        "matchingConditions": {
            "ConfigName": "Green_Eggs_and_Ham_specific",
            "testName": "dcae",
            "ONAPName": "dcae"
        },
        "policyConfigMessage": "Config Retrieved! ",
        "policyConfigStatus": "CONFIG_RETRIEVED",
        "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml",
        "policyVersion": "5",
        "property": null,
        "responseAttributes": {},
        "type": "JSON"
    },
    "DCAE_FTL3B.Config_In_Service": {
        "config": {
            "conflicting_key": "in_service_trumps!",
            "in_service": true
        },
        "matchingConditions": {
            "ConfigName": "In_Service",
            "testName": "dcae",
            "ONAPName": "dcae"
        },
        "policyConfigMessage": "Config Retrieved! ",
        "policyConfigStatus": "CONFIG_RETRIEVED",
        "policyName": "DCAE_FTL3B.Config_In_Service.1.xml",
        "policyVersion": "1",
        "property": null,
        "responseAttributes": {},
        "type": "JSON"
    }
}
```

---
<a name="service_component"></a>
### GET /service_component/**\<service_name\>**

#### Description
Retrieve fully-bound configuration for a service component instance.

*Note:  Response is the same as what Config Binding Service returns.*

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|path segment|**\<service_name\>**  <br>*required*|The service component name assigned by dockerplugin to the component<br>that is unique to the cloudify node instance and used in its Consul key(s).|string||

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[ServictestonentResponse](#servictestonentresponse)|

<a name="servictestonentresponse"></a>
##### ServictestonentResponse
OTI Handler returns a JSON object containing the install-time value of the service component node's
application_config property from the Cloudify deployment,
with any templating resolved from the current values of Consul dmaap and rel keys.

Example JSON response, formatted:
```
{
    "dcae_target_type": [
        "pnga-xxx"
    ]
}
```

---
<a name="dti_k8_events"></a>
### GET /dti_k8_events?pod=\<pod name\>&cluster=\<k8s cluster\>&namespace=\<k8s namespace\>

#### Description
Retrieve dti events list associated with a specific kubernetes pod.

*Note:Config Binding Service calls this API to fetch pod specific data*
1. *OTI events are queried from application database (not from consul)*
1. *events are associated with a specific k8s pod and the k8s location CLLI*

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|query component|**pod** <br>*required*|pod ID of the kubernetes StatefulSet for this collector service|String||
|query component|**cluster** <br>*required*|cluster FQDN of the kubernetes StatefulSet for this collector service|String||
|query component|**namespace** <br>*required*|namespace of the kubernetes StatefulSet for this collector service|String||

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[DtiEventsResponse](#dtieventsresponse)|

<a name="dtieventsresponse"></a>
##### DtiEventsResponse
OTI Handler Service returns a JSON object with VNF Types as first-level keys, and<br>VNF Instance IDs as second-level keys with value of its latest OTI Event.


Example JSON response

```
{
	"ctsf": {
		"dpa2actsf12345": {
			"dcae_service_location": "LSLEILAA",
			"dcae_service_type": "vUSP - vCTS",
			"dcae_target_type": "ctsf",
			"dcae_service_action": "add",
			"dcae_target_name": "dpa2actsf12345",
			"dcae_target_is-closed-loop-disabled": "false",
			"dcae_target_collection_ip": "32.67.11.99",
			"dcae_target_collection": "true",
			"dcae_target_prov-status": "PROV",
			"dcae_snmp_version": "",
			"dcae_target_service-description": "VIRTUAL USP",
			"dcae_snmp_community_string": "",
			"dcae_target_cloud-region-id": "dpa2a",
			"dcae_target_cloud-region-version": "aic3.0",
			"event": {}
		},
		"dpa2actsf4421": {
			"dcae_service_location": "LSLEILAA",
			"dcae_service_type": "vUSP - vCTS",
			"dcae_target_type": "ctsf",
			"dcae_service_action": "add",
			"dcae_target_name": "dpa2actsf4421",
			"dcae_target_is-closed-loop-disabled": "false",
			"dcae_target_collection_ip": "32.67.11.99",
			"dcae_target_collection": "true",
			"dcae_target_prov-status": "PROV",
			"dcae_snmp_version": "",
			"dcae_target_service-description": "VIRTUAL USP",
			"nodeType": "ctsf",
			"description": "CTS metrics",
			"nodeSubtype": "",
			"serviceType": "VIRTUAL USP",
			"priority": 1,
			"subType": "camel",
			"vnfType": "ctsf",
			"taskId": "PVUVUALUCTSCTS1080",
			"collectionType": "FOI",
			"protocol": "sftp",
			"collectionInterval": "300"
		}
	}
}
```

---
<a name="dti_docker_events"></a>
### GET /dti_docker_events?service=\<service name\>&location=\<location CLLI\>

#### Description
Retrieve dti events list associated with a docker hosted service or k8s service.

*Note:Config Binding Service calls this API to fetch OTI events associated with a docker container*
1. *OTI events are queried from application database (not from consul)*
1. *events are associated with a specific docker container or all k8s pods related to the service,<br> further filtered by the input location CLLI*

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|query component|**service** <br>*required*|service compnent name|String||
|query component|**location** <br>*optional*|location CLLI associated with the docker host or k8s cluster|String||

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[DtiEventsResponse](#dtieventsresponse)|

<a name="dtieventsresponse"></a>
##### DtiEventsResponse
OTI Handler Service returns a JSON object with VNF Types as first-level keys, and<br>VNF Instance IDs as second-level keys with value of its latest OTI Event.


Example JSON response

```
{
	"ctsf": {
		"dpa2actsf12345": {
			"dcae_service_location": "LSLEILAA",
			"dcae_service_type": "vUSP - vCTS",
			"dcae_target_type": "ctsf",
			"dcae_service_action": "add",
			"dcae_target_name": "dpa2actsf12345",
			"dcae_target_is-closed-loop-disabled": "false",
			"dcae_target_collection_ip": "32.67.11.99",
			"dcae_target_collection": "true",
			"dcae_target_prov-status": "PROV",
			"dcae_snmp_version": "",
			"dcae_target_service-description": "VIRTUAL USP",
			"dcae_snmp_community_string": "",
			"dcae_target_cloud-region-id": "dpa2a",
			"dcae_target_cloud-region-version": "aic3.0",
			"event": {}
		},
		"dpa2actsf4421": {
			"dcae_service_location": "LSLEILAA",
			"dcae_service_type": "vUSP - vCTS",
			"dcae_target_type": "ctsf",
			"dcae_service_action": "add",
			"dcae_target_name": "dpa2actsf4421",
			"dcae_target_is-closed-loop-disabled": "false",
			"dcae_target_collection_ip": "32.67.11.99",
			"dcae_target_collection": "true",
			"dcae_target_prov-status": "PROV",
			"dcae_snmp_version": "",
			"dcae_target_service-description": "VIRTUAL USP",
			"nodeType": "ctsf",
			"description": "CTS metrics",
			"nodeSubtype": "",
			"serviceType": "VIRTUAL USP",
			"priority": 1,
			"subType": "camel",
			"vnfType": "ctsf",
			"taskId": "PVUVUALUCTSCTS1080",
			"collectionType": "FOI",
			"protocol": "sftp",
			"collectionInterval": "300"
		}
	}
}
```

<a name="service_component_all"></a>
### GET /service_component_all/**\<service_name\>**?**service_location**=\<service_location\>;**policy_ids**=n

#### Description
Retrieve all available information for a service component instance (config, dti, and policies).

*Note:  Response differs from what prior Config Binding Service returned in that:*
1. *OTI events come from history (e.g., before a collector service component instance was deployed and are not lost if redeployed).*
1. *Can specify locations for OTI events to retrieve (e.g., for filtering, or for alternate sites).*
1. *Policies items is an object indexed by policy_id rather than a list (unless you specify **policy_ids**=n).*

#### Parameters
|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|path segment|**\<service_name\>**  <br>*required*|The service component name assigned by dockerplugin or k8splugin to the component<br>that is unique to the cloudify node instance and used in its Consul key(s).|string||
|query component|**service_location**  <br>*optional*|Allows multiple values separated by commas.<br>Filters OTI events with dcae_service_location in **\<service_location\>**.<br>Overrides locations defined in Consul for the **\<service_name\>**.|string|locations from Consul|
|query component|**policy_ids**  <br>*optional*|When "n", formats policies items as a list without policy_ids rather than as an object indexed by policy_id.|string|y|

#### Responses
|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Success|[ServictestonentAllResponse](#servictestonentallresponse)|

<a name="servictestonentallresponse"></a>
##### ServictestonentAllResponse
OTI Handler returns a JSON object containing all information for the component from Consul:

|Name|Description|Schema|
|---|---|---|
|**config**  <br>*required*|The install-time value of the service component node's application_config property<br>from the Cloudify deployment, with any templating resolved<br>from the current values of Consul dmaap and rel keys.|object|
|**dti**  <br>*optional*|A JSON object with VNF Types as first-level keys, and<br>VNF Instance IDs as second-level keys with value of its latest OTI Event.|object|
|**policies**  <br>*optional*|A JSON object with "event" and "items" first-level keys, and<br>policy_ids as second-level keys under "items"<br>(or if **policy_ids**=n then just a list without policy_ids)<br>with value of the complete policy body from Policy Manager.|object|

Example JSON response, formatted:
```
{
    "config": {
        "dcae_target_type": [
            "pnga-xxx"
        ]
    },
    "dti": {
        "anot-her": {
            "another01ems003": {
                "aai_additional_info": {},
                "dcae_generic-vnf_model-version-id": "1",
                "dcae_service-instance_model-version-id": "1",
                "dcae_service_action": "deploy",
                "dcae_service_location": "LSLEILAA",
                "dcae_snmp_community_string": "my_first_community",
                "dcae_snmp_version": "2c",
                "dcae_target_collection": "true",
                "dcae_target_collection_ip": "107.239.85.3",
                "dcae_target_in-maint": "false",
                "dcae_target_is-closed-loop-disabled": "false",
                "dcae_target_name": "another01ems003",
                "dcae_target_prov-status": "PROV",
                "dcae_target_type": "anot-her",
                "event": {}
            },
            "another01ems044": {
                "aai_additional_info": {},
                "dcae_generic-vnf_model-version-id": "1",
                "dcae_service-instance_model-version-id": "1",
                "dcae_service_action": "deploy",
                "dcae_service_location": "LSLEILAA",
                "dcae_snmp_community_string": "my_first_community",
                "dcae_snmp_version": "2c",
                "dcae_target_collection": "true",
                "dcae_target_collection_ip": "107.239.85.44",
                "dcae_target_in-maint": "false",
                "dcae_target_is-closed-loop-disabled": "false",
                "dcae_target_name": "another01ems044",
                "dcae_target_prov-status": "PROV",
                "dcae_target_type": "anot-her",
                "event": {}
            }
        }
    },
    "policies": {
        "event": {
            "action": "updated",
            "policies_count": 3,
            "timestamp": "2018-07-16T15:11:44.845Z",
            "update_id": "e6102aab-3079-435a-ae0d-0397a2cb3c4d"
        },
        "items": {
            "DCAE_FTL3B.Config_Green_Collectors": {
                "config": {
                    "conflicting_key": "green_collectors_wins",
                    "package_type": "plastic",
                    "polling_frequency": "30m",
                    "power_source": "lemmings"
                },
                "matchingConditions": {
                    "ConfigName": "Green_Collectors",
                    "testName": "dcae",
                    "ONAPName": "dcae"
                },
                "policyConfigMessage": "Config Retrieved! ",
                "policyConfigStatus": "CONFIG_RETRIEVED",
                "policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml",
                "policyVersion": "1",
                "property": null,
                "responseAttributes": {},
                "type": "JSON"
            },
            "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific": {
                "config": {
                    "bacon": "soft, not crispy",
                    "bread": "pumpernickel",
                    "conflicting_key": "green_eggs_and_ham_are_better",
                    "dcae_target_type": [
                        "pnga-xxx",
                        "pcrf-oam",
                        "vhss-ems",
                        "anot-her",
                        "new-type"
                    ],
                    "egg_color": "green",
                    "preparation": "scrambled"
                },
                "matchingConditions": {
                    "ConfigName": "Green_Eggs_and_Ham_specific",
                    "testName": "dcae",
                    "ONAPName": "dcae"
                },
                "policyConfigMessage": "Config Retrieved! ",
                "policyConfigStatus": "CONFIG_RETRIEVED",
                "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml",
                "policyVersion": "5",
                "property": null,
                "responseAttributes": {},
                "type": "JSON"
            },
            "DCAE_FTL3B.Config_In_Service": {
                "config": {
                    "conflicting_key": "in_service_trumps!",
                    "in_service": true
                },
                "matchingConditions": {
                    "ConfigName": "In_Service",
                    "testName": "dcae",
                    "ONAPName": "dcae"
                },
                "policyConfigMessage": "Config Retrieved! ",
                "policyConfigStatus": "CONFIG_RETRIEVED",
                "policyName": "DCAE_FTL3B.Config_In_Service.1.xml",
                "policyVersion": "1",
                "property": null,
                "responseAttributes": {},
                "type": "JSON"
            }
        }
    }
}
```
