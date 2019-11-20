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
package org.onap.dcae.runtime.web;

import org.onap.dcae.runtime.web.models.Action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        expectedBlueprints.put("hello-world_blueprint",
                loadFileContent("src/test/data/blueprints/helloworld_test_2.yaml"));
        expectedBlueprints.put("toolbox_blueprint",
                loadFileContent("src/test/data/blueprints/dcae-controller-toolbox-gui-eom-k8s.yaml"));
        return expectedBlueprints;
    }

    public static List<Action> getAddNodeActionsForRequest() {
        List<Action> actions = new ArrayList<Action>();
        Map<String,Object> payloadMap;

        Action action_1 = new Action();
        payloadMap = new HashMap<String,Object>();
        payloadMap.put("component_id","comp5678");
        payloadMap.put("name","hello-world-2");
        payloadMap.put("component_spec", Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json"));
        action_1.setPayload(payloadMap);
        action_1.setCommand("addnode");

        Action action_2 = new Action();
        payloadMap = new HashMap<String,Object>();
        payloadMap.put("component_id","comp1234");
        payloadMap.put("name","hello-world-1");
        payloadMap.put("component_spec", Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json"));
        action_2.setPayload(payloadMap);
        action_2.setCommand("addnode");

        actions.add(action_1);
        actions.add(action_2);

        return actions;
    }

    public static Map<String, String> prepareExpectedResult() {
        Map<String,String> result = new HashMap<>();
//        result.put("HelloWorld_blueprint",loadFileContent("src/test/data/blueprints_samples/dcae-collectors-vcc-helloworld-pm-eom-k8s.yaml"));
//        result.put("Toolbox_blueprint",loadFileContent("src/test/data/blueprints_samples/dcae-controller-toolbox-gui-eom-k8s.yaml"));
        result.put("hello-world-1_blueprint",loadFileContent("src/test/data/blueprints_samples/helloworld_test_1.yaml"));
        result.put("hello-world-2_blueprint",loadFileContent("src/test/data/blueprints_samples/helloworld_test_2.yaml"));
        return result;
    }

    public static List<Action> getAddNodesWithEdge() {
        List<Action> actions = getAddNodeActionsForRequest();
        Map<String,Object> payloadMap = new HashMap<String,Object>();

        Map<String,String> src = new HashMap<>();
        src.put("node", "comp1234");
        src.put("port", "DCAE-HELLO-WORLD-PUB-MR");

        Map<String,String> tgt = new HashMap<>();
        tgt.put("node", "comp5678");
        tgt.put("port", "DCAE-HELLO-WORLD-SUB-MR");

        Map<String,String> metadata = new HashMap<>();
        metadata.put("name","sample_topic_1");
        metadata.put("data_type","json");
        metadata.put("dmaap_type","MR");

        payloadMap.put("src",src);
        payloadMap.put("tgt",tgt);
        payloadMap.put("metadata",metadata);

        Action action = new Action();
        action.setCommand("addedge");
        action.setPayload(payloadMap);

        actions.add(action);
        System.out.println(action.getPayload().get("src"));

        return actions;
    }

    public static Map<String, String> prepareExpectedResultForAddEdge() {
        Map<String,String> result = new HashMap<>();
        result.put("hello-world-1_blueprint",loadFileContent("src/test/data/blueprints_samples/helloworld_test_1.yaml"));
        result.put("hello-world-2_blueprint",loadFileContent("src/test/data/blueprints_samples/helloworld_test_2.yaml"));
        return result;
    }
}
