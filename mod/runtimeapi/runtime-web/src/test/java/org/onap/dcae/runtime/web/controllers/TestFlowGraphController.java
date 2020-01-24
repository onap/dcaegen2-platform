/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import java.util.HashMap;
import java.util.LinkedList;

import org.onap.dcae.runtime.core.Edge;
import org.onap.dcae.runtime.core.FlowGraph;
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.web.ClientMocking;
import org.onap.dcae.runtime.web.Helper;
import org.onap.dcae.runtime.web.TestUtils;
import org.onap.dcae.runtime.web.models.Action;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.onap.dcae.runtime.web.service.GraphServiceImpl;
import org.onap.dcae.runtime.web.service.BlueprintInventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestFlowGraphController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlueprintInventory blueprintInventory;

    @Autowired
    private GraphServiceImpl graphService;

    private DistributeGraphRequest distributeGraphRequest;
    private GraphRequest graphRequest;

    @Before
    public void setUp() throws Exception{
        graphRequest = new GraphRequest();
        graphRequest.setId("1234");
        graphRequest.setName("nifi-main");
        graphRequest.setDescription("mock graph");
	distributeGraphRequest = new DistributeGraphRequest();
	ClientMocking inv = new ClientMocking()
		.on("POST /ccsdk-app/api-if", "\"OK\"")
		.applyTo(blueprintInventory);
    }

    @Test
    public void testInitializeGraph() throws Exception{
        graphRequest.setMain(true);
        mockMvc.perform(get("/api/graph/main"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/graph/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(graphRequest)))
                        .andExpect(status().isCreated());
        mockMvc.perform(post("/api/graph/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(graphRequest)))
                        .andExpect(status().isConflict());
        mockMvc.perform(get("/api/graph/main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("1234"))
                .andExpect(jsonPath("name").value("nifi-main"));
	distributeGraphRequest.setActions(Helper.getAddNodesWithEdge());
	mockMvc.perform(post("/api/graph/" + graphRequest.getId() + "/distribute")
			.contentType(MediaType.APPLICATION_JSON)
			.content(TestUtils.convertObjectToJsonBytes(distributeGraphRequest)))
			.andExpect(status().isOk());
	mockMvc.perform(delete("/api/graph/main"))
			.andExpect(status().isOk());
	mockMvc.perform(delete("/api/graph/main"))
			.andExpect(status().isNotFound());
    }
}
