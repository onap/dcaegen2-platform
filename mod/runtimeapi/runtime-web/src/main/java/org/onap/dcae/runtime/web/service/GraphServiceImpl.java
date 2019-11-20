/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.onap.dcae.runtime.core.*;
import org.onap.dcae.runtime.core.FlowGraphParser.BlueprintVessel;
import org.onap.dcae.runtime.web.exception.ActionsNotDefinedException;
import org.onap.dcae.runtime.web.exception.MainGraphAlreadyExistException;
import org.onap.dcae.runtime.web.exception.MainGraphNotFoundException;
import org.onap.dcae.runtime.web.models.Action;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphServiceImpl implements GraphService{

    @Autowired
    BlueprintInventory blueprintInventory;

    private FlowGraph<Node, Edge> mainFlowGraph;

    Logger logger = LoggerFactory.getLogger(GraphServiceImpl.class);

    @Autowired
    private FlowGraphParser flowGraphParser;

    @Override
    public FlowGraph<Node, Edge> getMainGraph() {
        if(mainFlowGraph == null){
            throw new MainGraphNotFoundException();
        }
        return mainFlowGraph;
    }

    @Override
    public boolean initializeMainGraph(GraphRequest mainGraphRequest) {
        if(mainFlowGraph != null){
            throw new MainGraphAlreadyExistException("Can not initialize the main graph, it already exists");
        }
        mainFlowGraph = new FlowGraph<Node,Edge>(mainGraphRequest.getId(),mainGraphRequest.getName(),
                            true, mainGraphRequest.getDescription());
        mainFlowGraph.addNode(getDummyNode());
        flowGraphParser.parse(mainFlowGraph);
        return true;
    }

    private Node getDummyNode() {
        return new Node("dummy_id","dummy_name","dummy_compspec");
    }

    @Override
    public List<BlueprintVessel> distribute(DistributeGraphRequest distributeGraphRequest) {
        //1.Iterate through list of actions
        logger.info("applying actions to graph");
        applyActionsToGraph(distributeGraphRequest);

        //2. generate blueprint from compspec of the node
        logger.info("generating blueprints for the affected nodes");
        List<BlueprintVessel> blueprints = generateBlueprintsForAffectedNodes(distributeGraphRequest);

        //3a. Push blueprints to the inventory
        logger.info("pushing bluepirnts to the dashboard inventrory");
        blueprintInventory.distributeToInventory(blueprints);
        //3b. return blueprint map
        return blueprints;
    }

    @Override
    public void deleteMainGraph() {
        if(mainFlowGraph == null){
            throw new MainGraphNotFoundException();
        }
        mainFlowGraph = null;
    }

    private List<BlueprintVessel> generateBlueprintsForAffectedNodes(DistributeGraphRequest distributeGraphRequest) {
        return flowGraphParser.createAndProcessBlueprints();
    }

    private void applyActionsToGraph(DistributeGraphRequest distributeGraphRequest) {
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
