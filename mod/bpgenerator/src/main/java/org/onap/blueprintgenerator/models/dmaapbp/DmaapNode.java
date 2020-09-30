/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 Copyright (c) 2020 Nokia. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.onap.blueprintgenerator.core.PgaasNodeBuilder;
import org.onap.blueprintgenerator.core.PolicyNodeBuilder;
import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.blueprint.Interfaces;
import org.onap.blueprintgenerator.models.blueprint.Node;
import org.onap.blueprintgenerator.models.blueprint.Properties;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isDataRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isMessageRouterType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)

public class DmaapNode extends Node {

    public TreeMap<String, LinkedHashMap<String, Object>> createDmaapNode(ComponentSpec componentSpec,
        TreeMap<String, LinkedHashMap<String, Object>> inps, String override) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;

        //set the type
        this.setType("dcae.nodes.ContainerizedServiceComponentUsingDmaap");

        //create the interface
        Interfaces inter = new Interfaces();
        retInputs = inter.createInterface(retInputs, componentSpec);
        TreeMap<String, Interfaces> interfaces = new TreeMap<>();
        interfaces.put("cloudify.interfaces.lifecycle", inter);
        this.setInterfaces(interfaces);

        //create and set the relationships
        ArrayList<LinkedHashMap<String, String>> relationships = new ArrayList<>();

        //go through the streams publishes
        if (componentSpec.getStreams().getPublishes() != null) {
            for (Publishes publishes : componentSpec.getStreams().getPublishes()) {
                relationships.add(createTypeAndTargetPubRelations(publishes));
            }
        }
        //go through the stream subscribes
        if (componentSpec.getStreams().getSubscribes() != null) {
            for (Subscribes subscribes : componentSpec.getStreams().getSubscribes()) {
                relationships.add(createTypeAndTargetSubRelations(subscribes));
            }
        }

        //add relationship for policy if exist
        if (componentSpec.getPolicyInfo() != null) {
            ArrayList<LinkedHashMap<String, String>> policyRelationshipsList = PolicyNodeBuilder
                .getPolicyRelationships(componentSpec);
            relationships.addAll(policyRelationshipsList);
        }

        //add relationships and env_variables for pgaas dbs if exist
        if (componentSpec.getAuxilary().getDatabases() != null) {
            ArrayList<LinkedHashMap<String, String>> pgaasRelationshipsList = PgaasNodeBuilder
                .getPgaasNodeRelationships(componentSpec);
            relationships.addAll(pgaasRelationshipsList);
        }

        this.setRelationships(relationships);

        //create and set the properties
        Properties props = new Properties();
        retInputs = props.createDmaapProperties(retInputs, componentSpec, override);
        this.setProperties(props);

        return retInputs;
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createFeedNode(ComponentSpec cs,
        TreeMap<String, LinkedHashMap<String, Object>> inps, String name) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;
        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        //set the type
        this.setType("ccsdk.nodes.Feed");

        //create and set the properties
        Properties props = new Properties();
        GetInput topicInput = new GetInput();
        topicInput.setBpInputName(name + "_name");
        props.setFeed_name(topicInput);
        props.setUseExisting(true);
        retInputs.put(name + "_name", stringType);
        this.setProperties(props);

        return retInputs;
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createTopicNode(ComponentSpec cs,
        TreeMap<String, LinkedHashMap<String, Object>> inps, String name) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;
        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        //set the type
        this.setType("ccsdk.nodes.Topic");

        //create and set the properties
        Properties props = new Properties();
        GetInput topicInput = new GetInput();
        topicInput.setBpInputName(name + "_name");
        props.setTopic_name(topicInput);
        //props.setUseExisting(true);
        retInputs.put(name + "_name", stringType);
        this.setProperties(props);

        return retInputs;
    }

    private LinkedHashMap<String, String> createTypeAndTargetPubRelations(Publishes publishes) {
        LinkedHashMap<String, String> pubRelations = new LinkedHashMap<>();
        if (isMessageRouterType(publishes.getType())) {
            pubRelations.put("type", "ccsdk.relationships.publish_events");
            pubRelations.put("target", publishes.getConfig_key() + "_topic");
        } else if (isDataRouterType(publishes.getType())) {
            pubRelations.put("type", "ccsdk.relationships.publish_files");
            pubRelations.put("target", publishes.getConfig_key() + "_feed");
        }
        return pubRelations;
    }

    private LinkedHashMap<String, String> createTypeAndTargetSubRelations(Subscribes subscribes) {
        LinkedHashMap<String, String> subRelations = new LinkedHashMap<>();
        if (isMessageRouterType(subscribes.getType())) {
            subRelations.put("type", "ccsdk.relationships.subscribe_to_events");
            subRelations.put("target", subscribes.getConfig_key() + "_topic");
        } else if (isDataRouterType(subscribes.getType())) {
            subRelations.put("type", "ccsdk.relationships.subscribe_to_files");
            subRelations.put("target", subscribes.getConfig_key() + "_feed");
        }
        return subRelations;
    }
}
