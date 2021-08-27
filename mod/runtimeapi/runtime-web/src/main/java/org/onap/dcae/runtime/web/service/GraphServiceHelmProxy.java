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

import org.onap.dcae.runtime.core.BlueprintData;
import org.onap.dcae.runtime.core.Edge;
import org.onap.dcae.runtime.core.FlowGraph;
import org.onap.dcae.runtime.core.FlowGraphParser;
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.core.helm.HelmChartGeneratorClient;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A proxy class which will be enable if artifact.type is set to HELM
 */
@Service
@Primary
@ConditionalOnProperty(
        value="artifact.type",
        havingValue = "HELM",
        matchIfMissing = true)
@ComponentScan(basePackages = "org.onap.dcae.runtime.core")
public class GraphServiceHelmProxy implements GraphService {

    @Autowired
    private GraphService defaultGraphService;

    @Autowired
    private HelmChartGeneratorClient helmChartGeneratorClient;

    @Autowired
    private GraphActionsParser actionsParser;

    /**
     * returns a main graph
     * @return main graph
     */
    @Override
    public FlowGraph<Node, Edge> getMainGraph() {
        return defaultGraphService.getMainGraph();
    }

    /**
     * initialize a main graph
     * @param mainGraph a main graph request
     */
    @Override
    public boolean initializeMainGraph(GraphRequest mainGraph) {
        return defaultGraphService.initializeMainGraph(mainGraph);
    }

    /**
     * apply actions on the main graph and creates, distributes helm charts
     * @param distributeGraphRequest distribute request
     * @return list of distributed charts
     */
    @Override
    public List<FlowGraphParser.BlueprintVessel> distribute(DistributeGraphRequest distributeGraphRequest) {
        actionsParser.applyActionsToGraph(getMainGraph(), distributeGraphRequest);
        return createHelmCharts();
    }

    public List<FlowGraphParser.BlueprintVessel> createHelmCharts() {
        final FlowGraph<Node, Edge> flowGraph = getMainGraph();
        List<FlowGraphParser.BlueprintVessel> blueprints = new ArrayList<>();
        for(Node node : flowGraph.getNodes()){
            if(node.getComponentId().equals("dummy_id")){
                continue;
            }
            final File helmChart = helmChartGeneratorClient.generateHelmChart(node.getComponentSpec());
            helmChartGeneratorClient.distribute(helmChart);
            BlueprintData blueprintData = new BlueprintData("1", helmChart.getName());
            node.setBlueprintData(blueprintData);
            blueprints.add(createBlueprintVessel(helmChart));
        }
        return blueprints;
    }

    private FlowGraphParser.BlueprintVessel createBlueprintVessel(File helmChart) {
        FlowGraphParser.BlueprintVessel bpv = new FlowGraphParser.BlueprintVessel();
        bpv.blueprint = helmChart.getName();
        bpv.version = 1;
        bpv.name = helmChart.getName();
        return bpv;
    }

    /**
     * deletes main graph
     */
    @Override
    public void deleteMainGraph() {
        defaultGraphService.deleteMainGraph();
    }
}
