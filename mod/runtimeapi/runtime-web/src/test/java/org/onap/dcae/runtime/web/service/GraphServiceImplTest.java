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

import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.web.Helper;
import org.onap.dcae.runtime.web.exception.MainGraphAlreadyExistException;
import org.onap.dcae.runtime.web.exception.MainGraphNotFoundException;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class GraphServiceImplTest {

    private GraphService graphService;
    private GraphRequest graphRequest;

    @Before
    public void setUp() throws Exception{
        graphService = new GraphServiceImpl();
        graphRequest = new GraphRequest();
        graphRequest.setId("1234");
        graphRequest.setName("nifi-main");
        graphRequest.setDescription("mock graph");
    }

    @Test
    public void testInitializeMainFlowGraph() throws Exception{
        graphService.initializeMainGraph(graphRequest);
        assertEquals(graphService.getMainGraph().getName(),"nifi-main");
        assertTrue(graphService.getMainGraph().isMain());
    }

    @Test
    public void testThrowConflictIfMainGraphAlreadyExist() throws Exception{
        graphService.initializeMainGraph(graphRequest);
        try {
            graphService.initializeMainGraph(graphRequest);
            fail("Exception has to be thrown since Main Graph already exists");
        }catch(MainGraphAlreadyExistException ex){
            assertEquals("Can not initialize the main graph, it already exists",ex.getMessage());
        }
    }

    @Test
    public void testMainGraphNotFound() throws Exception{
        try{
            graphService.getMainGraph();
            fail("Error should be thrown if the main graph is not initialized");
        }catch (MainGraphNotFoundException ex){
        }
    }

    @Test
    public void testAddNodesWithEdgeGeneratesBlueprints() throws Exception{
        /*
        TODO: FIX
        //arrange
        graphService.initializeMainGraph(graphRequest);
        DistributeGraphRequest distributeGraphRequest = new DistributeGraphRequest();
        distributeGraphRequest.setActions(Helper.getAddNodesWithEdge());
        Set<Node> nodes = prepareTestNodes();

        //act
        Map<String,String> result = graphService.distribute(distributeGraphRequest);

        //assert
        assertEquals(nodes.size(),graphService.getMainGraph().getNodes().size());
        //assertArrayEquals(nodes.toArray(),graphService.getMainGraph().getNodes().toArray());
        assertEquals(Helper.prepareExpectedResult(),result);
        */
    }

    private Set<Node> prepareTestNodes() {
        Set<Node> nodes = new HashSet<Node>();
        nodes.add(new Node("comp123","HelloWorld",
                Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json")));
        nodes.add(new Node("comp456","Toolbox",
                Helper.loadFileContent("src/test/data/compspecs/componentSpec_New_Toolbox.json")));
        return nodes;
    }

}