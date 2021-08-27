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

import org.onap.dcae.runtime.core.Edge;
import org.onap.dcae.runtime.core.FlowGraph;
import org.onap.dcae.runtime.core.FlowGraphParser;
import org.onap.dcae.runtime.core.FlowGraphParser.BlueprintVessel;
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.web.exception.MainGraphAlreadyExistException;
import org.onap.dcae.runtime.web.exception.MainGraphNotFoundException;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
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

    @Autowired
    private GraphActionsParser actionsParser;

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
        actionsParser.applyActionsToGraph(mainFlowGraph, distributeGraphRequest);

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


}
