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
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.core.helm.HelmChartGeneratorClient;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//@Service
public class GraphServiceHelmProxy implements GraphService {

    @Autowired
    private GraphService defaultGraphService;

    @Autowired
    private HelmChartGeneratorClient helmChartGeneratorClient;

    @Override
    public FlowGraph<Node, Edge> getMainGraph() {
        return defaultGraphService.getMainGraph();
    }

    @Override
    public boolean initializeMainGraph(GraphRequest mainGraph) {
        return defaultGraphService.initializeMainGraph(mainGraph);
    }

    //Change the implementation for the heml based distribution
    @Override
    public List<FlowGraphParser.BlueprintVessel> distribute(DistributeGraphRequest distributeGraphRequest) {
        return null;
    }

    @Override
    public void deleteMainGraph() {
        defaultGraphService.deleteMainGraph();
    }
}
