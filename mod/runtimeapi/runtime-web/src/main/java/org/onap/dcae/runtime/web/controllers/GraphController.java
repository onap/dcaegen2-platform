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
import org.onap.dcae.runtime.core.FlowGraphParser.BlueprintVessel;
import org.onap.dcae.runtime.web.exception.MainGraphNotFoundException;
import org.onap.dcae.runtime.web.models.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;
import org.onap.dcae.runtime.web.models.GraphRequest;
import org.onap.dcae.runtime.web.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.onap.dcae.runtime.web.service.GraphServiceImpl;

import javax.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/api/graph")
@Api(tags = "Graph", description = "API to manage Graph")
public class GraphController {

    @Autowired
    private GraphService graphService;

    Logger logger = LoggerFactory.getLogger(GraphController.class);

    @RequestMapping(method = RequestMethod.GET,value = "/main")
    @ApiOperation("Get main graph")
    public FlowGraph<Node, Edge> getMainGraph(){
        return graphService.getMainGraph();
    }

    @PostMapping(value = "/main")
    @ApiOperation("Initialize a main graph")
    public ResponseEntity initializeGraph(@RequestBody @Valid GraphRequest graphRequest){
        logger.info(graphRequest.toString());
        graphService.initializeMainGraph(graphRequest);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/distribute")
    @ApiOperation("Submit actions on a given graph and distribute blueprints to the inventory")
    public ResponseEntity<Map<String,String>> distributeGraph(@PathVariable(value = "id") String id,
                                                  @RequestBody @Valid DistributeGraphRequest distributeGraphRequest){
        logger.info(distributeGraphRequest.getActions().toString());
        List<BlueprintVessel> blueprints = graphService.distribute(distributeGraphRequest);

        Map<String, String> response = new HashMap<>();
        for (BlueprintVessel bpv : blueprints) {
            response.put(String.format("%s_%d", bpv.name, bpv.version), bpv.blueprint);
        }
        return new ResponseEntity<Map<String,String>>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/main")
    @ApiOperation("Remove the main graph")
    public String deleteMainGraph(){
        graphService.deleteMainGraph();
        return "Main Graph has been deleted.";
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private void graphNotFoundHandler(MainGraphNotFoundException ex){}
}
