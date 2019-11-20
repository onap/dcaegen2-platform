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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    public static String loadFileContent(String filePath) {
        String fileContent = "";
        try {
            fileContent = new String(Files.readAllBytes(Paths.get(filePath)),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    public static Map<String, String> loadTestBlueprints() {
        Map<String,String> expectedBlueprints = new HashMap<String, String>();
        expectedBlueprints.put("hello-world-2_blueprint",
                loadFileContent("src/test/data/blueprints/helloworld_test_2.yaml"));
        expectedBlueprints.put("hello-world-1_blueprint",
                loadFileContent("src/test/data/blueprints/helloworld_test_1.yaml"));
        return expectedBlueprints;
    }

    public static Edge getTestEdge(){
        EdgeLocation src = new EdgeLocation("comp1234","DCAE-HELLO-WORLD-PUB-MR");
        EdgeLocation tgt = new EdgeLocation("comp5678", "DCAE-HELLO-WORLD-SUB-MR");
        EdgeMetadata metadata = new EdgeMetadata("sample_topic_1", "json", "MR");
        Edge edge = new Edge(src, tgt, metadata);
        return edge;
    }

    public static FlowGraph<Node, Edge> prepareFlowGraph() {
        FlowGraph<Node, Edge> flowGraph = new FlowGraph("random_id","anyName",true,"someDescription");

        Node node_1 = new Node("comp1234", "hello-world-1",
                Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json"));
        Node node_2 = new Node("comp5678", "hello-world-2",
                Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json"));
        flowGraph.addNode(node_1);
        flowGraph.addNode(node_2);
        flowGraph.addEdge(node_1,node_2,Helper.getTestEdge());

        return flowGraph;
    }
}
