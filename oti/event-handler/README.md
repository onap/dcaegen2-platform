### OTI Handler

OTI Handler is a python 3.6 web application designed to run as a microservice in the DCAE kubernetes platform.
This application serves as an event routing service for DCAE OTI events. It is packaged in a docker image and run as a docker container in the Kubernetes platform cluster. It’s main business logic is to store incoming events in its database, determine the destination(s) for each event and then perform event routing to the target service (poller/collector). 
Information present in the event [VNF type, location CLLI of a VNF instance] determines which services will be notified with this event. Event routing logic uses information available in the platform services 
–
cloudify (service orchestrator), consul (service discovery).
A postgreSQL database will be used by the application for storage and retrieval of current OTI events.
  
Installation pre-requisites:

1. OTI postgreSQL database 
1. Cloudify service
1. Consul service

External interfaces/services for application processing:

1. Cloudify service
1. Consul service

OTI handler provides a REST API that is provided by an external node port service with a nodeport ID: 30134 (or as specified in the blueprint). External clients can access OTI handler service with an idns name and kubernetes nodeport,

e.g. https://{OTI Handler IDNS}:{node port}/

OTI Handler:
1. receives OTI Events from the DCAE Topology Interface (OTI) and uses them to
assign or unassign VNF instances to DCAE monitoring services (data collector component instances).
1. accepts requests to run a reconfig script in a Cloudify deployment's service component instances.
1. sends event notification for service component instances that will be deployed in docker containers as a Kubernetes StatefulSet
1. stores the received events and the active event distribution data in an application database
1. using historical OTI event records from consul KV store or application database, answers queries for current (latest, not undeployed) OTI Events
1. answers queries for policy or policies of a service_component, indexed by policy_id.
1. answers queries for service_component (fully-bound config) --
the same results as available from Config Binding Service (CBS).
1. answers queries for service_component_all with historical OTI events and policies indexed by policy_id
(not available from the prior Config Binding Service (CBS)).

OTI Handler's HTTPS interface is documented [here](./OTI-API.md).  

Post-installation verification:

Application health check - 
From any external client or a web browser, access the oti handler health check URL: https://OTI Handler IDNS:nodeport/healthcheck

If the application responds properly to a health check query, it is ready to process requests from clients.
