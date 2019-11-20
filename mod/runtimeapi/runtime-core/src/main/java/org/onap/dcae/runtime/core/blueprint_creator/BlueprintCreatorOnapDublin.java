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
package org.onap.dcae.runtime.core.blueprint_creator;

import org.onap.dcae.runtime.core.Node;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class BlueprintCreatorOnapDublin implements BlueprintCreator{

    private String topicUrl;
    private String importFilePath;

    public void setTopicUrl(String topicUrl) {
        this.topicUrl = topicUrl;
    }

    public void setImportFilePath(String importFilePath) {
        this.importFilePath = importFilePath;
    }

    @Override
    public String createBlueprint(String componentSpecString) {
        ComponentSpec componentSpec = new ComponentSpec();
        componentSpec.createComponentSpecFromString(componentSpecString);
        Blueprint blueprint = new Blueprint().createBlueprint(componentSpec,"",'o',importFilePath);
        return blueprint.blueprintToString();
    }

    @Override
    public void resolveDmaapConnection(Node node, String locationPort, String dmaapEntityName) {
        if(node == null || locationPort == null){
            return;
        }
        String blueprintContent = node.getBlueprintData().getBlueprint_content();
        locationPort = locationPort.replaceAll("-","_");
        Yaml yaml = getYamlInstance();
        Map<String,Object> obj = yaml.load(blueprintContent);
        Map<String,Object> inputsObj = (Map<String, Object>) obj.get("inputs");
        for(Map.Entry<String,Object> entry: inputsObj.entrySet()){
            if(entry.getKey().matches(locationPort+".*url")) {
                Map<String,String> inputValue = (Map<String, String>) entry.getValue();
                inputValue.put("default",topicUrl + "/" + dmaapEntityName);
            }
        }
        node.getBlueprintData().setBlueprint_content(yaml.dump(obj));
    }

    private Yaml getYamlInstance() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }

//    private String attachSingleQoutes(String str) {
//        return "'" + str + "'";
//    }
}
