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

import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class TestYamlStringToObject {

    @Test
    public void testYamlStringToObject() throws Exception{
        String bp_content = Helper.loadFileContent("src/test/data/blueprints/helloworld_onap_dublin.yaml");
        String locationPort = "DCAE-HELLO-WORLD-SUB-MR";
        Yaml yaml = getYamlInstance();
        Map<String,Object>  obj = yaml.load(bp_content);
        Map<String,Object> inputsObj = (Map<String, Object>) obj.get("inputs");
        for(Map.Entry<String,Object> entry: inputsObj.entrySet()){
            System.out.println(String.format("^%s.*url",locationPort.replaceAll("-","_")));
            if(entry.getKey().matches(String.format("^%s.*url",locationPort.replaceAll("-","_")))) {
                Map<String,String> inputValue = (Map<String, String>) entry.getValue();
                inputValue.put("default","test_topic");
                System.out.println(entry);
            }
        }
       // System.out.println(yaml.dump(obj));
    }

    private Yaml getYamlInstance() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }
}
