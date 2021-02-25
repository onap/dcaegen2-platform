/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2021 Nokia. All rights reserved.
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

package org.onap.blueprintgenerator.service.common;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.Interfaces;
import org.onap.blueprintgenerator.model.common.Node;
import org.onap.blueprintgenerator.model.common.Properties;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add
 * ONAP/DMAAP/Feed/Topic Nodes
 */
@Service
public class NodeService {

    @Autowired
    private InterfacesService interfacesService;

    @Autowired
    private PolicyNodeService policyNodeService;

    @Autowired
    private PgaasNodeService pgaasNodeService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    /**
     * Creates Onap Node to include interface
     *
     * @param inputs Inputs
     * @param onapComponentSpec OnapComponentSpec
     * @param override Service Name Override
     * @return
     */
    public Map<String, Object> createOnapNode(
        Map<String, Map<String, Object>> inputs,
        OnapComponentSpec onapComponentSpec,
        String override) {

        Map<String, Object> response = new HashMap<>();
        Node onapNode = new Node();

        Map<String, Object> onapResponse = interfacesService
            .createInterface(inputs, onapComponentSpec);
        inputs = (Map<String, Map<String, Object>>) onapResponse.get("inputs");

        Map<String, Interfaces> interfaces = new TreeMap<>();
        interfaces.put(
            Constants.CLOUDIFY_INTERFACES_LEFECYCLE, (Interfaces) onapResponse.get("interfaces"));
        onapNode.setInterfaces(interfaces);

        onapNode.setType(Constants.DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT);

        List<Map<String, String>> relationships = new ArrayList();

        if (onapComponentSpec.getPolicyInfo() != null) {
            List<Map<String, String>> policyRelationshipsList =
                policyNodeService.getPolicyRelationships(onapComponentSpec);
            relationships.addAll(policyRelationshipsList);
        }

        if (onapComponentSpec.getAuxilary().getDatabases() != null) {
            List<Map<String, String>> pgaasRelationshipsList =
                pgaasNodeService.getPgaasNodeRelationships(onapComponentSpec);
            relationships.addAll(pgaasRelationshipsList);
        }

        onapNode.setRelationships(relationships);

        Map<String, Object> propertiesResponse =
            propertiesService.createOnapProperties(inputs, onapComponentSpec, override);
        inputs = (Map<String, Map<String, Object>>) propertiesResponse.get("inputs");
        onapNode.setProperties(
            (org.onap.blueprintgenerator.model.common.Properties) propertiesResponse
                .get("properties"));

        response.put("onapNode", onapNode);
        response.put("inputs", inputs);
        return response;
    }

    /**
     * Creates Dmaap Node to include interface
     *
     * @param inputs Inputs
     * @param onapComponentSpec OnapComponentSpec
     * @param override Service Name Override
     * @return
     */
    public Map<String, Object> createDmaapNode(
        OnapComponentSpec onapComponentSpec,
        Map<String, Map<String, Object>> inputs,
        String override) {

        Map<String, Object> response = new HashMap<>();
        Node dmaapNode = new Node();

        dmaapNode.setType(Constants.DCAE_NODES_CONTAINERIZED_SERVICE_COMPONENT_USING_DMAAP);

        Map<String, Object> dmaapResponse =
            interfacesService.createInterface(inputs, onapComponentSpec);
        inputs = (Map<String, Map<String, Object>>) dmaapResponse.get("inputs");

        Map<String, Interfaces> interfaces = new TreeMap<>();
        interfaces.put(
            Constants.CLOUDIFY_INTERFACES_LEFECYCLE, (Interfaces) dmaapResponse.get("interfaces"));
        dmaapNode.setInterfaces(interfaces);

        List<Map<String, String>> relationships = new ArrayList();

        if (onapComponentSpec.getStreams().getPublishes() != null) {
            for (Publishes publishes : onapComponentSpec.getStreams().getPublishes()) {
                Map<String, String> pubRelations = new LinkedHashMap();
                if (blueprintHelperService.isMessageRouterType(publishes.getType())) {
                    pubRelations.put("type", Constants.PUBLISH_EVENTS);
                    pubRelations.put("target", publishes.getConfig_key() + Constants.A_TOPIC);
                } else if (blueprintHelperService.isDataRouterType(publishes.getType())) {
                    pubRelations.put("type", Constants.PUBLISH_FILES);
                    pubRelations.put("target", publishes.getConfig_key() + Constants.A_FEED);
                }
                relationships.add(pubRelations);
            }
        }

        if (onapComponentSpec.getStreams().getSubscribes() != null) {
            for (Subscribes subscribes : onapComponentSpec.getStreams().getSubscribes()) {
                Map<String, String> subRelations = new LinkedHashMap();
                if (blueprintHelperService.isMessageRouterType(subscribes.getType())) {
                    subRelations.put("type", Constants.SUBSCRIBE_TO_EVENTS);
                    subRelations.put("target", subscribes.getConfig_key() + Constants.A_TOPIC);
                } else if (blueprintHelperService.isDataRouterType(subscribes.getType())) {
                    subRelations.put("type", Constants.SUBSCRIBE_TO_FILES);
                    subRelations.put("target", subscribes.getConfig_key() + Constants.A_FEED);
                }
                relationships.add(subRelations);
            }
        }

        if (onapComponentSpec.getPolicyInfo() != null) {
            List<Map<String, String>> policyRelationshipsList =
                policyNodeService.getPolicyRelationships(onapComponentSpec);
            relationships.addAll(policyRelationshipsList);
        }

        if (onapComponentSpec.getAuxilary().getDatabases() != null) {
            List<Map<String, String>> pgaasRelationshipsList =
                pgaasNodeService.getPgaasNodeRelationships(onapComponentSpec);
            relationships.addAll(pgaasRelationshipsList);
        }

        dmaapNode.setRelationships(relationships);

        Map<String, Object> propertiesResponse =
            propertiesService.createDmaapProperties(inputs, onapComponentSpec, override);
        inputs = (Map<String, Map<String, Object>>) propertiesResponse.get("inputs");
        dmaapNode.setProperties(
            (org.onap.blueprintgenerator.model.common.Properties) propertiesResponse
                .get("properties"));

        response.put("dmaapNode", dmaapNode);
        response.put("inputs", inputs);
        return response;
    }

    /**
     * Creates Feed Node for Streams
     *
     * @param inputs Inputs
     * @param name Name
     * @return
     */
    public Map<String, Object> createFeedNode(
        Map<String, Map<String, Object>> inputs, String name) {
        Map<String, Object> response = new HashMap<>();
        Node feedNode = new Node();

        Map<String, Object> stringType = new LinkedHashMap();
        stringType.put("type", "string");

        feedNode.setType(Constants.FEED);

        org.onap.blueprintgenerator.model.common.Properties props =
            new org.onap.blueprintgenerator.model.common.Properties();
        GetInput topicInput = new GetInput();
        topicInput.setBpInputName(name + "_name");
        props.setFeed_name(topicInput);
        props.setUseExisting(true);
        inputs.put(name + "_name", stringType);
        feedNode.setProperties(props);

        response.put("feedNode", feedNode);
        response.put("inputs", inputs);
        return response;
    }

    /**
     * Creates Topic Node for Streams
     *
     * @param inputs Inpts
     * @param name Name
     * @return
     */
    public Map<String, Object> createTopicNode(
        Map<String, Map<String, Object>> inputs, String name) {
        Map<String, Object> response = new HashMap<>();
        Node topicNode = new Node();

        Map<String, Object> stringType = new LinkedHashMap();
        stringType.put("type", "string");

        topicNode.setType(Constants.TOPIC);

        org.onap.blueprintgenerator.model.common.Properties props = new Properties();
        GetInput topicInput = new GetInput();
        topicInput.setBpInputName(name + "_name");
        props.setTopic_name(topicInput);
        inputs.put(name + "_name", stringType);
        topicNode.setProperties(props);

        response.put("topicNode", topicNode);
        response.put("inputs", inputs);
        return response;
    }
}
