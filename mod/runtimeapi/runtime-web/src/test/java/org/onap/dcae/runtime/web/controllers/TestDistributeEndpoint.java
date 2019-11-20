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
package org.onap.dcae.runtime.web.controllers;

import org.onap.dcae.runtime.web.Helper;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.onap.dcae.runtime.web.service.GraphServiceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TestDistributeEndpoint {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GraphServiceImpl graphService;

    private DistributeGraphRequest distributeGraphRequest;

    @Before
    public void setUp() throws Exception{
        //initialze the Main Graph
        GraphRequest graphRequest = new GraphRequest();
        graphRequest.setId("1234");
        graphRequest.setName("nifi-main");
        graphRequest.setDescription("mock graph");
        graphService.initializeMainGraph(graphRequest);
        distributeGraphRequest = new DistributeGraphRequest();

    }

    @Test
    @Ignore
    public void testAddNodesGeneratesBlueprints() throws Exception{

        distributeGraphRequest.setActions(Helper.getAddNodeActionsForRequest());
        Map<String,String> expectedResult = Helper.prepareExpectedResult();

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/graph/1234/distribute",distributeGraphRequest,Map.class);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertTrue(expectedResult.equals(response.getBody()));

//        mockMvc.perform(post("/api/graph/1234/distribute")
//        .contentType(MediaType.APPLICATION_JSON)
//        .content(TestUtils.convertObjectToJsonBytes(distributeGraphRequest)))
//        .andExpect(status().isOk());

//        assertEquals(Helper.loadFileContent("src/test/data/blueprints_samples/helloworld_test_2.yaml"),
//                Helper.loadFileContent("src/test/data/blueprints_from_tests/HelloWorld_blueprint.yaml"));
//        fail("Needs to complete the test");
    }

    @Test
    public void testAddNodesAndEdgeGeneratesCorrectBlueprints() throws Exception{
        distributeGraphRequest.setActions(Helper.getAddNodesWithEdge());
        Map<String,String> expectedResult = Helper.prepareExpectedResultForAddEdge();

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/graph/1234/distribute",distributeGraphRequest,Map.class);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertTrue(expectedResult.equals(response.getBody()));

    }
}
