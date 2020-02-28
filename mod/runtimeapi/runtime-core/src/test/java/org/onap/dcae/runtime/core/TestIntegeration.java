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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.dcae.runtime.core.blueprint_creator.BlueprintCreatorOnap;

public class TestIntegeration {

    private FlowGraphParser flowGraphParser;

    @Before
    public void setUp() throws Exception{
        //1. prepare Graph
        FlowGraph<Node, Edge> flowGraph = Helper.prepareFlowGraph();

        //2. Inject graph in FlowGraphParser
        BlueprintCreator blueprintCreator = new BlueprintCreatorOnap();
        flowGraphParser = new FlowGraphParser(blueprintCreator);
        flowGraphParser.parse(flowGraph);
    }

    @Test
    @Ignore
    public void testParserCreatesBlueprintsFromFlowGraph() throws Exception{
        /*
        TODO: FIX
        //3. Check if the parser can create blueprints for the componentSpec collection
        //resultMap has key as a component-name and value is a blueprint string
        Map<String,String> expectedBlueprints = Helper.loadTestBlueprints();
        Map<String,String> resultBlueprints = flowGraphParser.createAndProcessBlueprints();

        assertEquals(expectedBlueprints,resultBlueprints);
        */
    }
}
