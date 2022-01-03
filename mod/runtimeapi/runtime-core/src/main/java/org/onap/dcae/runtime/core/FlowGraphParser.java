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
package org.onap.dcae.runtime.core;

import org.onap.dcae.runtime.core.blueprint_creator.BlueprintCreator;

import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class FlowGraphParser {

    private FlowGraph<Node,Edge> flowGraph;
    private BlueprintCreator blueprintCreator;

    public FlowGraphParser(BlueprintCreator blueprintCreator) {
        this.blueprintCreator = blueprintCreator;
    }

    public void parse(FlowGraph<Node, Edge> flowGraph) {
        this.flowGraph = flowGraph;
    }

    public static class BlueprintVessel {
        public String blueprint;
        public String name;
        public int version;

        @Override
        public String toString() {
            return String.format("%s:%d", this.name, this.version);
        }
    }

    private static int createBlueprintVersion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmm").withZone(ZoneId.of("UTC"));
        Instant instant = Instant.now();
        String timestamp = formatter.format(instant);
        return Integer.parseInt(timestamp);
    }

    private static String createBlueprintName(FlowGraph flowGraph, String componentName) {
        // NOTE: Replacing whitespaces with dash
        // NOTE: Separator must be dash or underbar otherwise cloudify will flip out
        return String.format("%s_%s", flowGraph.getName().replace(" ", "-"), componentName);
    }

    public List<BlueprintVessel> createAndProcessBlueprints() {
        //1. generate blueprints for all the nodes
        for(Node node : flowGraph.getNodes()){
            if(node.getComponentId().equals("dummy_id")){
                continue;
            }
            BlueprintData blueprintData = new BlueprintData("1", blueprintCreator.createBlueprint(node.getComponentSpec()));
            node.setBlueprintData(blueprintData);
        }
        //2. replace dmaap info from the edges
        for(Edge edge : flowGraph.getEdges()){
            String srcNodeId = edge.getSrc().getNode();
            Node srcNode = getNodeFromId(srcNodeId);
            blueprintCreator.resolveDmaapConnection(srcNode ,edge.getSrc().getPort(),edge.getMetadata().getName());

            String tgtNodeId = edge.getTgt().getNode();
            Node tgtNode = getNodeFromId(tgtNodeId);
            blueprintCreator.resolveDmaapConnection(tgtNode ,edge.getTgt().getPort(),edge.getMetadata().getName());
        }

        //3. return processed blueprints along with blueprint_name
        List<BlueprintVessel> blueprints = new ArrayList<>();
        for(Node node: flowGraph.getNodes()){
            if(node.getComponentId().equals("dummy_id")) {
                continue;
            }

            BlueprintVessel bpv = new BlueprintVessel();
            bpv.blueprint = node.getBlueprintData().getBlueprint_content();
            bpv.version = createBlueprintVersion();
            bpv.name = createBlueprintName(flowGraph, node.getComponentName());
            blueprints.add(bpv);
        }
        return blueprints;
    }

    public Node getNodeFromId(String srcNodeId) {
            for(Node node : flowGraph.getNodes()){
                if (node.getComponentId().equals(srcNodeId)) return node;
            }
            return null;
        }
}
