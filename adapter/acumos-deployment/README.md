# Helm Chart for Acumos Adapter
This directory contains a Helm chart for deploying the Acumos adapter component.  The chart deploys an instance of the adapter into an existing ONAP deployment.  Because the adapter depends on some external components that will be different for each deployment, it does not make sense to deploy the adapter automatically as part of an automated ONAP deployment using OOM.  In addition, the Acumos adapter is still at the proof of concept stage for the ONAP R6 (Frankfurt) release, so it is not appropriate to include it in the OOM deployment.

## Deployment method
The chart creates the following Kubernetes entities:
- A Deployment that manages a pod with the Acumos adapter container.
- A ClusterIP Service that exposes the adapter's HTTP interface within the Kubernetes cluster.
- An Ingress that configures the cluster's ingress controller to route traffic to the adapter service.
- A ConfigMap with the adapter's configuration file, mounted as a volume on the adapter container.
- A secret containing the TLS certificate materials that the adapter uses to authenticate to the Acumos instance. The contents of the secret come from values provided in an "override" file at deployment time.  The secret is mounted as a volume on the adapter container at the location set in the adapter configuration in the ConfigMap.
- A secret containing the password that the adapter needs to push images to the Docker registry.  The content comes from a value provided in an "override" file at deployment time.  The secret is mounted as a volume on the adapter container at the location set in the adapter configuration in the ConfigMap.

## Helm dependencies
The chart depends on the ONAP "common" chart.  The chart uses elements from "common" to set various names.  It also uses the "common" template for secrets.   This keeps the chart consistent with ONAP practices.

## Instructions for using the chart
See [these instructions](https://wiki.onap.org/display/DW/Acumos+Adapter+Installation) for details on how to use the chart to deploy the adapter.

## Future work
Once the Acumos adapter is accepted as a standard component of ONAP, it might make sense to integrate this chart into the ONAP OOM structure, as an optional component.  Enabling the component would require both setting a flag and providing additional information in an "overrides" file for the Acumos instance and the Docker registry.