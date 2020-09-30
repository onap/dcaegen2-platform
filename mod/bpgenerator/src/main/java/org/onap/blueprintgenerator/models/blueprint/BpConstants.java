/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia Intellectual Property. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.models.blueprint;

public final class BpConstants {

    private BpConstants() {}

    public static final String CLOUDIFY_DSL_1_3 = "cloudify_dsl_1_3";

    public static final String CONTENERIZED_SERVICE_COMPONENT_USING_DMAAP = "dcae.nodes.ContainerizedServiceComponentUsingDmaap";
    public static final String CONTENERIZED_SERVICE_COMPONENT = "dcae.nodes.ContainerizedServiceComponent";
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
