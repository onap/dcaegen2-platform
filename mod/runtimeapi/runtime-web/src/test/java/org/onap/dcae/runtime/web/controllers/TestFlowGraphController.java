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

import org.onap.dcae.runtime.core.Edge;
import org.onap.dcae.runtime.core.FlowGraph;
import org.onap.dcae.runtime.core.Node;
import org.onap.dcae.runtime.web.TestUtils;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.onap.dcae.runtime.web.service.GraphServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class TestFlowGraphController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GraphServiceImpl graphService;

    private GraphRequest graphRequest;

    @Before
    public void setUp() throws Exception{
        graphRequest = new GraphRequest();
        graphRequest.setId("1234");
        graphRequest.setName("nifi-main");
        graphRequest.setDescription("mock graph");
    }

    @Test
    public void testInitializeGraph() throws Exception{
        graphRequest.setMain(true);
        mockMvc.perform(post("/api/graph/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(graphRequest)))
                        .andExpect(status().isCreated());
    }

    @Test
    public void testGetMainGraph() throws Exception{
        FlowGraph<Node, Edge> flowGraph = new FlowGraph<>("1234","nifi-main",true,"demo");
        when(this.graphService.getMainGraph()).thenReturn(flowGraph);
        mockMvc.perform(get("/api/graph/main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("1234"))
                .andExpect(jsonPath("name").value("nifi-main"));
    }

//    @Test
//    public void testMainGraphNotFound() throws Exception{
//        when(this.graphService.getMainGraph()).thenReturn(null);
//        mockMvc.perform(get("/api/graph/main"))
//                .andExpect(status().isNotFound());
//    }

//    @Test
//    public void testConflictIfMainGraphExists() throws Exception{
//        when(this.graphService.initializeMainGraph(graphRequest)).thenThrow(new MainGraphAlreadyExistException(""));
//        mockMvc.perform(post("/api/graph/main")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(TestUtils.convertObjectToJsonBytes(graphRequest)))
//                .andExpect(status().isConflict());
//    }

}
