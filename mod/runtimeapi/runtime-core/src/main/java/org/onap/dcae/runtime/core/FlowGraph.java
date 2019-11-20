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

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import java.util.Set;

public class FlowGraph<Node,Edge> {
    private  String id;
    private  String name;
    private  boolean isMain;
    private  String description;
    private MutableNetwork<Node,Edge> mutableNetwork;

    public FlowGraph(String id, String name, boolean isMain, String description) {
        this.id = id;
        this.name = name;
        this.isMain = isMain;
        this.description = description;
        this.mutableNetwork = NetworkBuilder.directed().build();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addNode(Node node) {
        mutableNetwork.addNode(node);
    }

    public int getNodeSize() {
        return mutableNetwork.nodes().size();
    }

    public Set<Node> getNodes() {
        return mutableNetwork.nodes();
    }

    public Set<Edge> getEdges() {
        return mutableNetwork.edges();
    }

    public void addEdge(Node node_1, Node node_2, Edge edge) {
        mutableNetwork.addEdge(node_1,node_2,edge);
    }

    public int getEdgeSize() {
        return mutableNetwork.edges().size();
    }

    public Edge getEdge(Node node_1, Node node_2) {
        return mutableNetwork.edgeConnecting(node_1,node_2).get();
    }

    public void removeNode(Node node_1) {
        mutableNetwork.removeNode(node_1);
    }

    public void removeEdge(Edge edge_1) {
        mutableNetwork.removeEdge(edge_1);
    }

}
