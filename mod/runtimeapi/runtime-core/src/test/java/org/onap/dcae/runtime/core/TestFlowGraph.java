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

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TestFlowGraph {

    private FlowGraph<Node, Edge> flowGraph;
    private Node node_1;
    private Node node_2;

    @Before
    public void setUp() throws Exception{
        flowGraph = new FlowGraph("random_id","anyName",true,"someDescription");
        node_1 = new Node("comp1234", "component_1", "<comp-string>");
        node_2 = new Node("comp5678", "component_2", "<comp-string>");
        flowGraph.addNode(node_1);
        flowGraph.addNode(node_2);
    }

    @Test
    public void testFlowGraphProperties() throws Exception{
        assertEquals("random_id",flowGraph.getId());
        assertEquals("anyName",flowGraph.getName());
        assertEquals("someDescription",flowGraph.getDescription());
        assertTrue(flowGraph.isMain());
    }

    @Test
    public void testAddNodeToGraph() throws Exception{
        Set<Node> nodes = new HashSet<Node>();
        nodes.add(node_1);
        nodes.add(node_2);

        assertEquals(2,flowGraph.getNodeSize());
        assertEquals(nodes,flowGraph.getNodes());

    }

    @Test
    public void testAddEdgeToGraph() throws Exception{
        Edge edge_1 = Helper.getTestEdge();
        flowGraph.addEdge(node_1,node_2,edge_1);
        assertEquals(1, flowGraph.getEdgeSize());
        assertEquals(edge_1, flowGraph.getEdge(node_1,node_2));
    }

    @Test
    public void testRemoveNodeFromGraph() throws Exception{
        flowGraph.removeNode(node_1);
        assertEquals(1,flowGraph.getNodeSize());
        assertEquals(node_2,flowGraph.getNodes().toArray()[0]);
    }

    @Test
    public void testRemoveEdgeFromGraph() throws Exception{
        Edge edge_1 = Helper.getTestEdge();
        flowGraph.addEdge(node_1,node_2,edge_1);
        flowGraph.removeEdge(edge_1);
        assertEquals(0,flowGraph.getEdgeSize());
    }

    @Test
    public void testDummyNodeInEdge() throws Exception{
        Node node_dummy = new Node("dummy", "component_dummy", "<comp-string>");
        Edge edge_1 = Helper.getTestEdge();
        Edge edge_2 = Helper.getTestEdge();
        Edge edge_3 = Helper.getTestEdge();
        flowGraph.addEdge(node_dummy,node_2,edge_1);
        flowGraph.addEdge(node_2,node_dummy,edge_2);
        flowGraph.addEdge(node_1,node_dummy,edge_3);
        System.out.println(flowGraph.getNodes());
        System.out.println(flowGraph.getEdges());
    }
}
