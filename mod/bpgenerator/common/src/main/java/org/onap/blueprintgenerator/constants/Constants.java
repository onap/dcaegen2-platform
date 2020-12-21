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

package org.onap.blueprintgenerator.constants;

/*
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAP and DCAE Blueprint Applications
 * Constants: Used in ONAP Blueprints
 */

public class Constants {

    public static final String _TOPIC = "_topic";
    public static final String _FEED = "_feed";
    public static final String DATAROUTER_VALUE = "data router";
    public static final String DATA_ROUTER = "data_router";
    public static final String MESSAGEROUTER_VALUE = "message router";
    public static final String MESSAGE_ROUTER = "message_router";
    public static final String TOSCA_DEF_VERSION = "cloudify_dsl_1_3";
    public static final String SERVICE_COMPONENT_NAME_OVERRIDE = "service_component_name_override";
    public static final String EMPTY = "''";
    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String STRING_TYPE = "string";
    public static final String EMPTY_VALUE = "";
    public static final String DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT_USING_DMAAP =
        "dcae.nodes.ContainerizedServiceComponentUsingDmaap";
    public static final String MEMORY_LIMIT_128Mi = "128Mi";
    public static final String CPU_LIMIT_250m = "250m";
    public static final String CPU_LIMIT = "cpu_limit";
    public static final String MEMORY_LIMIT = "memory_limit";
    public static final String CPU_REQUEST = "cpu_request";
    public static final String MEMORY_REQUEST = "memory_request";

    public static final String CLOUDIFY_INTERFACES_LEFECYCLE = "cloudify.interfaces.lifecycle";
    public static final String DCAE = "dcae";
    public static final String DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT =
        "dcae.nodes.ContainerizedServiceComponent";
    public static final String ONAP_NAME_DCAE = "DCAE";
    public static final String POLICIES_POLICYNAME_DCAECONFIG = "DCAE.Config_";

    public static final String DB_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";
    public static final String NAME_POSTFIX = "_database_name";
    public static final String PGAAS_NODE_NAME_POSTFIX = "_pgaasdb";
    public static final String PGAAS_NODE_TYPE = "dcae.nodes.pgaas.database";
    public static final String POLICY_NODE_TYPE = "clamp.nodes.policy";
    public static final String POLICY_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";
    public static final String WRITER_FQDN_POSTFIX = "_database_writerfqdn";
    public static final boolean USE_EXISTING = true;
    public static final String ONAP_INPUT_CPU_LIMIT = "dcae-ves-collector_cpu_limit";
    public static final String ONAP_NODETEMPLATES = "dcae-ves-collector";
    public static final String ONAP_NODETEMPLATES_TYPE = "dcae.nodes.ContainerizedServiceComponent";
    public static final String ONAP_DEFAULT250m = "\"250m\"";
    public static final String ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT = "\"\"";
    public static final String DMAAP_NODETEMPLATES_TYPE =
        "dcae.nodes.ContainerizedServiceComponentUsingDmaap";
    public static final String USE_EXTERNAL_TLS_FIELD = "use_external_tls";
    public static final String DEFAULT_CA = "RA";
    public static final Object DEFAULT_CERT_TYPE = "P12";
    public static final String INPUT_PREFIX = "external_cert_";
    public static final String EXTERNAL_CERT_DIRECTORY_FIELD = "external_cert_directory";
    public static final String CA_NAME_FIELD = "ca_name";
    public static final String CERT_TYPE_FIELD = "cert_type";
    public static final String EXTERNAL_CERTIFICATE_PARAMETERS_FIELD =
        "external_certificate_parameters";
    public static final String CERT_DIRECTORY_FIELD = "cert_directory";
    public static final String COMMON_NAME_FIELD = "common_name";
    public static final String SANS_FIELD = "sans";
    public static final String DEFAULT_COMMON_NAME = "sample.onap.org";
    public static final String DEFAULT_SANS = "sample.onap.org,component.sample.onap.org";
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
