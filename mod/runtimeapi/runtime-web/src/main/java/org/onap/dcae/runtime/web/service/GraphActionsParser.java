/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.runtime.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.dcae.runtime.core.Edge;
import org.onap.dcae.runtime.core.EdgeLocation;
import org.onap.dcae.runtime.core.FlowGraph;
import org.onap.dcae.runtime.core.FlowGraphParser;
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.web.exception.ActionsNotDefinedException;
import org.onap.dcae.runtime.web.models.Action;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A helper class that parses the actions from the request and apply them to the main graph
 */
@Component
public class GraphActionsParser {

    @Autowired
    private FlowGraphParser flowGraphParser;

    void applyActionsToGraph(FlowGraph<Node, Edge> mainFlowGraph, DistributeGraphRequest distributeGraphRequest) {
        if(distributeGraphRequest.getActions() == null){
            throw new ActionsNotDefinedException("Action(s) must be defined in the request");
        }
        for(Action action : distributeGraphRequest.getActions()){
            if(action.getCommand().equals("addnode")){
                Node node = prepareNodeFromAddNAddNodeAction(action);
                mainFlowGraph.addNode(node);
            }
            else if(action.getCommand().equals("addedge")) {
                Edge edge = prepareEdgeFromAddEdgeAction(action);
                Node srcNode = flowGraphParser.getNodeFromId(edge.getSrc().getNode());
                Node tgtNode = flowGraphParser.getNodeFromId(edge.getTgt().getNode());
                srcNode = fillPlaceholderIfNodeIsEmpty(srcNode);
                tgtNode =fillPlaceholderIfNodeIsEmpty(tgtNode);
                mainFlowGraph.addEdge(srcNode,tgtNode,edge);
            }
        }
    }

    private Node fillPlaceholderIfNodeIsEmpty(Node node) {
        if (node == null) {
            node = flowGraphParser.getNodeFromId("dummy_id");
        }
        return node;
    }


    private Edge prepareEdgeFromAddEdgeAction(Action action) {
        ObjectMapper objectMapper = new ObjectMapper();
        Edge edge = objectMapper.convertValue(action.getPayload(),Edge.class);
        return edge;
    }

    private void fillPlaceholderIfLocaionsAreEmpty(Edge edge) {
        if(edge.getSrc().getNode() == null && edge.getSrc().getPort() == null){
            EdgeLocation src = new EdgeLocation("node-id-placeholder", "node-port-placeholder");
            edge.setSrc(src);
        }
        if(edge.getTgt().getNode() == null && edge.getTgt().getPort() == null){
            EdgeLocation tgt = new EdgeLocation("node-id-placeholder", "node-port-placeholder");
            edge.setTgt(tgt);
        }
    }

    private Node prepareNodeFromAddNAddNodeAction(Action action) {
        String componentId = (String) action.getPayload().get("component_id");
        String componentName = (String) action.getPayload().get("name");
        String componentSpec = (String) action.getPayload().get("component_spec");
        return new Node(componentId,componentName,componentSpec);
    }
}
