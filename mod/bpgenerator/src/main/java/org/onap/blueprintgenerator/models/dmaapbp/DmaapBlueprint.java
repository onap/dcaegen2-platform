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

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.joinUnderscore;
import static org.onap.blueprintgenerator.models.blueprint.Imports.createDmaapImports;
import static org.onap.blueprintgenerator.models.blueprint.Imports.createImportsFromFile;

public class DmaapBlueprint extends Blueprint {

    private static final String TOPIC = "topic";
    private static final String FEED = "feed";

    public Blueprint createDmaapBlueprint(ComponentSpec cs, String importPath, String override) {
        Blueprint blueprint = new Blueprint();

        //set tosca definition
        blueprint.setTosca_definitions_version("cloudify_dsl_1_3");

        //set the description
        blueprint.setDescription(cs.getSelf().getDescription());

        //create the inpus object that will be added to over the creation of the blueprint
        TreeMap<String, LinkedHashMap<String, Object>> inps = new TreeMap<>();

        //set the imports
        if (importPath.equals("")) {
            blueprint.setImports(createDmaapImports());
        } else {
            blueprint.setImports(createImportsFromFile(importPath));
        }

        //blueprint.setImports(imps.getImports());

        //set and create the node templates
        TreeMap<String, Node> nodeTemplate = new TreeMap();

        //create and add the main dmaap node
        DmaapNode dmaap = new DmaapNode();
        inps = dmaap.createDmaapNode(cs, inps, override);
        nodeTemplate.put(cs.getSelf().getName(), dmaap);

        //create and add the topic/feed nodes

        //go through the streams publishes
        if (cs.getStreams().getPublishes() != null) {
            for (Publishes publisher : cs.getStreams().getPublishes()) {
                if (isMessageRouter(publisher.getType())) {
                    String topic = joinUnderscore(publisher.getConfig_key(), TOPIC);
                    DmaapNode topicNode = new DmaapNode();
                    inps = topicNode.createTopicNode(cs, inps, topic);
                    nodeTemplate.put(topic, topicNode);
                } else if (isDataRouter(publisher.getType())) {
                    String feed = joinUnderscore(publisher.getConfig_key(), FEED);
                    DmaapNode feedNode = new DmaapNode();
                    inps = feedNode.createFeedNode(cs, inps, feed);
                    nodeTemplate.put(feed, feedNode);
                }
            }
        }
        //go through the stream subscribes
        if (cs.getStreams().getSubscribes() != null) {
            for (Subscribes subscriber : cs.getStreams().getSubscribes()) {
                if (isMessageRouter(subscriber.getType())) {
                    String topic = joinUnderscore(subscriber.getConfig_key(), TOPIC);
                    DmaapNode topicNode = new DmaapNode();
                    inps = topicNode.createTopicNode(cs, inps, topic);
                    nodeTemplate.put(topic, topicNode);
                } else if (isDataRouter(subscriber.getType())) {
                    String feed = joinUnderscore(subscriber.getConfig_key(), FEED);
                    DmaapNode feedNode = new DmaapNode();
                    inps = feedNode.createFeedNode(cs, inps, feed);
                    nodeTemplate.put(feed, feedNode);
                }
            }
        }

        //if present in component spec, populate policyNodes information in the blueprint
        if (cs.getPolicyInfo() != null) {
            PolicyNodeBuilder.addPolicyNodesAndInputs(cs, nodeTemplate, inps);
        }

        //if present in component spec, populate pgaasNodes information in the blueprint
        if (cs.getAuxilary().getDatabases() != null) {
            PgaasNodeBuilder.addPgaasNodesAndInputs(cs, nodeTemplate, inps);
        }

        blueprint.setNode_templates(nodeTemplate);

        blueprint.setInputs(inps);
        return blueprint;
    }

    private boolean isDataRouter(String type) {
        return type.equals("data_router") || type.equals("data router");
    }

    private boolean isMessageRouter(String type) {
        return type.equals("message_router") || type.equals("message router");
    }
}
