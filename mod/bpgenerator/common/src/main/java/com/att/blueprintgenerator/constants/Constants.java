/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package com.att.blueprintgenerator.constants;

/*
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAP and DCAE Blueprint Applications
 * Constants: Used in ONAP and DCAE Blueprints
 */

public class Constants {

    //Common
    public static final String _TOPIC = "_topic";
    public static final String _FEED = "_feed";
    public static final String DATAROUTER_VALUE = "data router";
    public static final String DATA_ROUTER = "data_router";
    public static final String MESSAGEROUTER_VALUE = "message router";
    public static final String MESSAGE_ROUTER = "message_router";
    public static final String TOSCA_DEF_VERSION = "cloudify_dsl_1_3";
    public static final String SERVICE_COMPONENT_NAME_OVERRIDE = "service_component_name_override";
    public static final int DEFAULT10K = 10000;
    public static final int DEFAULT256 = 256;
    public static final int DEFAULT30K = 30000;
    public static final String EMPTY = "''";
    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String STRING_TYPE = "string";
    public static final String EMPTY_VALUE = "";
    public static final String DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT_USING_DMAAP =  "dcae.nodes.ContainerizedServiceComponentUsingDmaap";
    public static final String MEMORY_LIMIT_128Mi = "128Mi";
    public static final String CPU_LIMIT_250m = "250m";
    public static final String MEMORY_LIMIT_256Mi = "256Mi";
    public static final String CPU_LIMIT_100m = "100m";
    public static final String CPU_LIMIT = "cpu_limit";
    public static final String MEMORY_LIMIT = "memory_limit";
    public static final String CPU_REQUEST = "cpu_request";
    public static final String MEMORY_REQUEST = "memory_request";

    // DCAE Constants
    public static final String KUBE_CLUSTER_FQDN = "KUBE_CLUSTER_FQDN";
    public static final String AAFTLS = "aafTls";
    public static final String AAFTLS_IMAGE = "dockercentral.it.att.com:5100/com.att.ecompcntr.public/ecompc-aaf-init-container:1.0.4";
    public static final String AAF_CERT_DIRECTORY = "aaf_cert_directory";
    public static final String AAF_PASSWORD = "_aaf_password";
    public static final String AAF_PWD = "aaf_password";
    public static final String AAF_USERNAME = "_aaf_username";
    public static final String AAF_USER_NM = "aaf_username";
    public static final String ADMIN_HOST = ", admin, host ] }";
    public static final String ADMIN_PASSWORD = ", admin, password ] }";
    public static final String ADMIN_USER = ", admin, user ] }";
    public static final String ALWAYS = "'always'";
    public static final String ALWAYS_TEST = "''always''";
    public static final String ANTIAFFINITY = "antiAffinity";
    public static final String APPCONFIG = "appconfigObj";  // remove Obj
    public static final String APP_NAME = "app_name";
    public static final String CLOUDIFY_INTERFACES_LEFECYCLE = "cloudify.interfaces.lifecycle";
    public static final String CLOUDIFY_RELATIONSHIPS_DEPENDS_ON =  "cloudify.relationships.depends_on";
    public static final String CONFIGURATION = "configurationObj";
    public static final String DCAE = "dcae";
    public static final String DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT =  "dcae.nodes.ContainerizedServiceComponent";
    public static final String DCAE_NODES_EXISTINGFEED = "dcae.nodes.ExistingFeed";
    public static final String DCAE_NODES_EXISTINGTOPIC = "dcae.nodes.ExistingTopic";
    public static final String DCAE_NODES_FEED = "dcae.nodes.Feed";
    public static final String DCAE_NODES_PGAAS_DATABASE = "dcae.nodes.pgaas.database";
    public static final String DCAE_NODES_POLICIES = "dcae.nodes.policies";
    public static final String DCAE_NODES_POLICY = "dcae.nodes.policy";
    public static final String DCAE_NODES_TOPIC = "dcae.nodes.Topic";
    public static final String DCAE_RELATIONSHIPS_PUBLISH_EVENTS =  "dcae.relationships.publish_events";
    public static final String DCAE_RELATIONSHIPS_PUBLISH_FILES =  "dcae.relationships.publish_files";
    public static final String DCAE_RELATIONSHIPS_SUBSCRIBE_EVENTS =  "dcae.relationships.subscribe_to_events";
    public static final String DCAE_RELATIONSHIPS_SUBSCRIBE_FILES =  "dcae.relationships.subscribe_to_files";
    public static final String DCAE_SERVICE_LOCATION = "dcae_service_location";
    public static final String DEFAULT250m = "'250m'";
    public static final String DEFAULT128mi = "'128mi'";
    public static final String DCAE_RELATIONSHIPS_COMP_CONTAINED_IN = "dcae.relationships.component_contained_in";
    public static final String DEFAULT500m = "'500m'";
    public static final String DEFAULT500m_TEST = "''500m''";
    public static final String DOCKER_HOST_HOST = "docker_host_host";
    public static final String DTIDATA_DIRECTORY = "/dtidata";
    public static final String DTI_DATA_DIR = "DTI_DATA_DIR";
    public static final String DTI_HEALTHCHECK_ENDPOINT = "/healthcheck";
    public static final String DTI_HEALTHCHECK_INTERVAL = "90s";
    public static final String DTI_HEALTHCHECK_TIMEOUT = "60s";
    public static final String DTI_HEALTHCHECK_TYPE = "https";
    public static final String DTI_SERVICE_MAP = "dockercentral.it.att.com:5100/com.att.dcae.controller/dcae-controller-sidecar:19.11-001";
    public static final String GET_ATTRIBUTE = "{ get_attribute: [ ";
    public static final String GET_SECRET = "get_secret";
    public static final String IDNS_FQDN = "idns_fqdn";
    public static final String INPUT_AAF_CERT_DEFAULT = "'/opt/app/aafcertman'";
    public static final String INPUT_APP_NAME_DEFAULT = "'dcae'";
    public static final String INPUT_CPU_LIMIT_DMAAP = "dcae-collectors-vcc-helloworld-pm_cpu_limit";
    public static final String INPUT_CPU_LIMIT_SAM_COLLECTOR = "DcaeSamCollector_cpu_limit";
    public static final String INPUT_CPU_PERIOD_DMAAP = "dcae-collectors-vcc-helloworld-pm_cpu_period";
    public static final String INPUT_CPU_PERIOD_DTI_NODMAAP = "hello-buzzword_cpu_period";
    public static final String INPUT_CPU_PERIOD_SAM_COLLECTOR = "DcaeSamCollector_cpu_period";
    public static final String INPUT_CPU_QUOTA_DTIEVENTPROC = "dcae-dti-event-proc-narad_cpu_quota";
    public static final String INPUT_DATABASE_SCHEMA = "databaseSchema";
    public static final String INPUT_DATABASE_SCHEMA_DEFAULT = "Database Schema";
    public static final String INPUT_EXPORT_GROUP = "export_group";
    public static final String INPUT_EXPORT_GROUP_DEFAULT_DTIEVENTPROC = "'DCAE-DTI-EVENT-PROC-NARAD'";
    public static final String INPUT_FEED_DMAAP = "DCAE-HELLO-WORLD-PUB-DR_feed2_feed_id";
    public static final String INPUT_FEED_NAME_DMAAP = "DCAE-HELLO-WORLD-PUB-DR_feed2_feed_name";
    public static final String INPUT_LOC_MAP_SAM_COLLECTOR = "clliLocationMappingClli1";
    public static final String INPUT_LOC_MAP_SAM_COLLECTOR_DEFAULT = "'akr1=AKRNOHAH'";
    public static final String INPUT_MEMORY_LIMIT_DTI_NODMAAP = "hello-buzzword_mem_limit";
    public static final String INPUT_POLICY_NAME_DTI_EVENTPROC = "dcae-dti-event-proc-narad_restart_policy.Name";
    public static final String INPUT_POLICY_SAM_COLLECTOR = "DcaeSamCollector_DbConnectionPolicy_policy_0_policy_id";
    public static final String INPUT_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT_DMAAP = "'dcae-collectors-vcc-helloworld-pm'";
    public static final String INPUT_TEST_IMAGE = "'test-image-uri'";
    public static final String INPUT_TOPIC3_DEFAULT_DMAAP = "'DCAE-HELLO-WORLD-PUB-MR'";
    public static final String INPUT_TOPIC3_DMAAP ="DCAE-HELLO-WORLD-PUB-MR_topic3_name";
    public static final String INPUT_TYPE_DTI_NODMAAP = "ConsulTest1";
    public static final String INPUT_TYPE_SAM_COLLECTOR = "DcaeSamCollector_DbConnectionPolicy_policy_0_policy_id";
    public static final String MESSAGE_FEED = "<<feed";
    public static final String MESSAGE_TOPIC = "<<topic";
    public static final String NAMESPACE = "NAMESPACE";
    public static final String NODETEMPLATES_DMAAP =  "dcae-collectors-vcc-helloworld-pm_dcae-collectors-vcc-helloworld-pm";
    public static final String NODETEMPLATES_DMAAP_DEFAULT =  "dcae.nodes.DockerContainerForComponentsUsingDmaap";
    public static final String NODETEMPLATES_DOCKER_CONTAINER_FOR_COMPONENTS = "dcae.nodes.DockerContainerForComponents";
    public static final String NODETEMPLATES_DOCKER_HOST_DEFAULT = "dcae.nodes.SelectedDockerHost";
    public static final String NODETEMPLATES_DTIEVENTPROC = "dcae-dti-event-proc-narad_dcae-dti-event-proc-narad";
    public static final String NODETEMPLATES_DTINODMAAP =  "hello-buzzword_hello-buzzword";
    public static final String NODETEMPLATES_FEED2 = "feed2";
    public static final String NODETEMPLATES_PGAASDB0_DTIEVENTPROC = "pgaasdb0";
    public static final String NODETEMPLATES_POLICIES0 = "policies_0";
    public static final String NODETEMPLATES_POLICY0 = "policy_0";
    public static final String NODETEMPLATES_SAMCOLLECTOR = "DcaeSamCollector_DcaeSamCollector";
    public static final String NODETEMPLATES_TOPIC0 = "topic0";
    public static final String NODE_NAME = "node_name";
    public static final String NODE_PORTS = "node_ports";
    public static final String NODE_PORTS_DEFAULT = "node ports";
    public static final String ONAP_NAME_DCAE = "DCAE";
    public static final String OPT_APP_AAFCERTMAN = "/opt/app/aafcertman";
    public static final String PGAASDB = "pgaasdb";
    public static final String POD_AFFINITY_TOPOLOGY_KEY = "kubernetes.io/hostname";
    public static final String POLICIES_ = "policies_";
    public static final String POLICIES_POLICYNAME_DCAECONFIG = "DCAE.Config_";
    public static final String POLICY_ = "policy_";
    public static final String PREFERREDAFFINITY = " preferredAffinity";
    public static final String REQUIREDAFFINITY = "requiredAffinity";
    public static final String SIDECARCHECK_HTTP_ENDPOINT = "/opt/app/DCAE/sdnlocal/bin/sidecarstartstop.sh status";
    public static final String SIDECARCHECK_INTERVAL = "300s";
    public static final String SIDECARCHECK_TIMEOUT = "120s";
    public static final String SIDECARCHECK_TYPE = "docker";
    public static final String SIMPLENODE = "simpleNode";
    public static final String STREAM_PUBLISH_ = "stream_publish_";
    public static final String STREAM_SUBSCRIBE_ = "stream_subscribe_";
    public static final String USER_HOST = ", user, host ] }";
    public static final String USER_PASSWORD = ", user, password ] }";
    public static final String USER_USER = ", user, user ] }";
    public static final String USE_AAF_TLS = "use_aaf_tls";
    public static final String VIEWER_HOST = ", viewer, host ] }";
    public static final String VIEWER_PASSWORD = ", viewer, password ] }";
    public static final String VIEWER_USER = ", viewer, user ] }";
    public static final String _POLICY_NAME = "_policyName";

    // ONAP Constants
    public static final String DB_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";
    public static final String NAME_POSTFIX = "_database_name";
    public static final String PGAAS_NODE_NAME_POSTFIX = "_pgaasdb";
    public static final String PGAAS_NODE_TYPE = "dcae.nodes.pgaas.database";
    public static final String POLICY_NODE_TYPE = "clamp.nodes.policy";
    public static final String POLICY_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";
    public static final String WRITER_FQDN_POSTFIX = "_database_writerfqdn";
    public static final boolean USE_EXISTING = true;
    public static final String ALWAYS_PULL_IMAGE = "always_pull_image";
    public static final String ALWAYS_PULL_IMAGE_DEFAULT = "true";
    public static final String ONAP_INPUT_CPU_LIMIT = "dcae-ves-collector_cpu_limit";
    public static final String ONAP_NODETEMPLATES =  "dcae-ves-collector";
    public static final String ONAP_NODETEMPLATES_TYPE =  "dcae.nodes.ContainerizedServiceComponent";
    public static final String ONAP_DEFAULT250m = "\"250m\"";
    public static final String ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT = "\"\"";
    public static final String DMAAP_NODETEMPLATES_TYPE =  "dcae.nodes.ContainerizedServiceComponentUsingDmaap";
    public static final String USE_EXTERNAL_TLS_FIELD = "use_external_tls";
    public static final String DEFAULT_CA = "RA";
    public static final Object DEFAULT_CERT_TYPE = "P12";
    public static final String INPUT_PREFIX = "external_cert_";
    public static final String EXTERNAL_CERT_DIRECTORY_FIELD = "external_cert_directory";
    public static final String CA_NAME_FIELD = "ca_name";
    public static final String CERT_TYPE_FIELD = "cert_type";
    public static final String EXTERNAL_CERTIFICATE_PARAMETERS_FIELD = "external_certificate_parameters";
    public static final String CERT_DIRECTORY_FIELD = "cert_directory";
    public static final String COMMON_NAME_FIELD = "common_name";
    public static final String SANS_FIELD = "sans";
    public static final String DEFAULT_COMMON_NAME = "sample.onap.org";
    public static final String DEFAULT_SANS = "sample.onap.org:component.sample.onap.org";
    public static final String FEED = "dcaegen2.nodes.Feed";
    public static final String TOPIC = "dcaegen2.nodes.Topic";
    public static final String PUBLISH_EVENTS = "dcaegen2.relationships.publish_events";
    public static final String PUBLISH_FILES = "dcaegen2.relationships.publish_files";
    public static final String SUBSCRIBE_TO_EVENTS = "dcaegen2.relationships.subscribe_to_events";
    public static final String SUBSCRIBE_TO_FILES = "dcaegen2.relationships.subscribe_to_files";
    public static final String TOSCA_DATATYPES_ROOT = "tosca.datatypes.Root";
    public static final String TOSCA_NODES_ROOT = "tosca.nodes.Root";
    public static final String TOSCA_SIMPLE_YAML = "tosca_simple_yaml_1_0_0";

}
