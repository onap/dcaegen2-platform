/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 ================================================================================
 Modifications Copyright (c) 2020 Nokia. All rights reserved.
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

package org.onap.blueprintgenerator.models.dmaapbp;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.onap.blueprintgenerator.core.PgaasNodeBuilder;
import org.onap.blueprintgenerator.core.PolicyNodeBuilder;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.blueprint.Node;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isDataRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isMessageRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.joinUnderscore;
import static org.onap.blueprintgenerator.models.blueprint.Imports.createDmaapImports;
import static org.onap.blueprintgenerator.models.blueprint.Imports.createImportsFromFile;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.CLOUDIFY_DSL_1_3;

public class DmaapBlueprint extends Blueprint {

    private static final String TOPIC = "topic";
    private static final String FEED = "feed";

    public Blueprint createDmaapBlueprint(ComponentSpec componentSpec, String importPath, String override) {
        Blueprint blueprint = new Blueprint();

        //set tosca definition
        blueprint.setTosca_definitions_version(CLOUDIFY_DSL_1_3);

        //set the description
        blueprint.setDescription(componentSpec.getSelf().getDescription());

        //create the inpus object that will be added to over the creation of the blueprint
        TreeMap<String, LinkedHashMap<String, Object>> inps = new TreeMap<>();

        //set the imports
        if (importPath.equals("")) {
            blueprint.setImports(createDmaapImports());
        } else {
            blueprint.setImports(createImportsFromFile(importPath));
        }

        //set and create the node templates
        TreeMap<String, Node> nodeTemplate = new TreeMap();

        //create and add the main dmaap node
        DmaapNode dmaap = new DmaapNode();
        inps = dmaap.createDmaapNode(componentSpec, inps, override);
        nodeTemplate.put(componentSpec.getSelf().getName(), dmaap);

        //create and add the topic/feed nodes

        //go through the streams publishes
        if (componentSpec.getStreams().getPublishes() != null) {
            for (Publishes publisher : componentSpec.getStreams().getPublishes()) {
                if (isMessageRouterType(publisher.getType())) {
                    String topic = joinUnderscore(publisher.getConfig_key(), TOPIC);
                    DmaapNode topicNode = new DmaapNode();
                    inps = topicNode.createTopicNode(componentSpec, inps, topic);
                    nodeTemplate.put(topic, topicNode);
                } else if (isDataRouterType(publisher.getType())) {
                    String feed = joinUnderscore(publisher.getConfig_key(), FEED);
                    DmaapNode feedNode = new DmaapNode();
                    inps = feedNode.createFeedNode(componentSpec, inps, feed);
                    nodeTemplate.put(feed, feedNode);
                }
            }
        }
        //go through the stream subscribes
        if (componentSpec.getStreams().getSubscribes() != null) {
            for (Subscribes subscriber : componentSpec.getStreams().getSubscribes()) {
                if (isMessageRouterType(subscriber.getType())) {
                    String topic = joinUnderscore(subscriber.getConfig_key(), TOPIC);
                    DmaapNode topicNode = new DmaapNode();
                    inps = topicNode.createTopicNode(componentSpec, inps, topic);
                    nodeTemplate.put(topic, topicNode);
                } else if (isDataRouterType(subscriber.getType())) {
                    String feed = joinUnderscore(subscriber.getConfig_key(), FEED);
                    DmaapNode feedNode = new DmaapNode();
                    inps = feedNode.createFeedNode(componentSpec, inps, feed);
                    nodeTemplate.put(feed, feedNode);
                }
            }
        }

        //if present in component spec, populate policyNodes information in the blueprint
        if (componentSpec.getPolicyInfo() != null) {
            PolicyNodeBuilder.addPolicyNodesAndInputs(componentSpec, nodeTemplate, inps);
        }

        //if present in component spec, populate pgaasNodes information in the blueprint
        if (componentSpec.getAuxilary().getDatabases() != null) {
            PgaasNodeBuilder.addPgaasNodesAndInputs(componentSpec, nodeTemplate, inps);
        }

        blueprint.setNode_templates(nodeTemplate);

        blueprint.setInputs(inps);
        return blueprint;
    }

}
