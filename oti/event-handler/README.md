### DTI Handler


DTI Handler:
1. receives DTI Events from the DCAE Topology Interface (DTI) and uses them to
assign or unassign VNF instances to DCAE monitoring services (data collector component instances).
1. accepts requests to run a reconfig script in a Cloudify deployment's service component instances.
1. sends event notification for service component instances that will be deployed in docker containers as a Kubernetes StatefulSet
1. stores the received events and the active event distribution data in an application database
1. using historical DTI event records from consul KV store or application database, answers queries for current (latest, not undeployed) DTI Events
1. answers queries for policy or policies of a service_component, indexed by policy_id.
1. answers queries for service_component (fully-bound config) --
the same results as available from Config Binding Service (CBS).
1. answers queries for service_component_all with historical DTI events and policies indexed by policy_id
(not available from the prior Config Binding Service (CBS)).

DTI Handler's HTTPS interface is documented [here](./DTI-API.md).  
  
