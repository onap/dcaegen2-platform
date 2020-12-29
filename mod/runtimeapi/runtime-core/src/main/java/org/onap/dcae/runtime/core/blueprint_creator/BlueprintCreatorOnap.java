/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2020 Nokia. All rights reserved.
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

import java.util.LinkedHashMap;
import java.util.Map;
import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.BlueprintCreatorService;
import org.onap.blueprintgenerator.service.base.BlueprintService;
import org.onap.blueprintgenerator.service.base.FixesService;
import org.onap.blueprintgenerator.service.common.ComponentSpecService;
import org.onap.dcae.runtime.core.Node;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class BlueprintCreatorOnap implements BlueprintCreator {

    private String topicUrl;
    private String importFilePath;
    private boolean useDmaapPlugin;
    private final ComponentSpecService componentSpecService;
    private final BlueprintCreatorService blueprintCreatorService;
    private final BlueprintService blueprintService;
    private final FixesService fixesService;

    public BlueprintCreatorOnap(ComponentSpecService componentSpecService,
        BlueprintCreatorService blueprintCreatorService, BlueprintService blueprintService,
        FixesService fixesService) {
        this.componentSpecService = componentSpecService;
        this.blueprintCreatorService = blueprintCreatorService;
        this.blueprintService = blueprintService;
        this.fixesService = fixesService;
    }

    public void setTopicUrl(String topicUrl) {
        this.topicUrl = topicUrl;
    }

    public void setImportFilePath(String importFilePath) {
        this.importFilePath = importFilePath;
    }

    public void setUseDmaapPlugin(boolean useDmaapPlugin) {
        this.useDmaapPlugin = useDmaapPlugin;
    }

    @Override
    public String createBlueprint(String componentSpecString) {
        OnapComponentSpec componentSpec = componentSpecService.createComponentSpecFromString(componentSpecString);
        Input input = new Input();
        input.setBpType(useDmaapPlugin ? "d" : "o");
        input.setImportPath(importFilePath);
        Blueprint blueprint = blueprintCreatorService.createBlueprint(componentSpec, input);
        return blueprintService.blueprintToString(componentSpec, blueprint, input);
    }

    @Override
    public void resolveDmaapConnection(Node node, String locationPort, String dmaapEntityName) {
        if (node == null || locationPort == null) {
            return;
        }
        String blueprintContent = node.getBlueprintData().getBlueprint_content();
        locationPort = locationPort.replaceAll("-", "_");
        Yaml yaml = getYamlInstance();
        Map<String, Object> obj = yaml.load(blueprintContent);
        Map<String, Object> inputsObj = (Map<String, Object>) obj.get("inputs");
        for (Map.Entry<String, Object> entry : inputsObj.entrySet()) {
            LinkedHashMap<String, Object> modified = retainQuotesForDefault(entry.getValue());
            entry.setValue(modified);
            if (entry.getKey().matches(locationPort + ".*url")) {
                Map<String, String> inputValue = (Map<String, String>) entry.getValue();
                inputValue.put("default", topicUrl + "/" + dmaapEntityName);
            }
        }
        node.getBlueprintData().setBlueprint_content(fixesService.applyFixes(yaml.dump(obj)));
    }

    private LinkedHashMap<String, Object> retainQuotesForDefault(Object valueOfInputObject) {
        LinkedHashMap<String, Object> temp = (LinkedHashMap<String, Object>) valueOfInputObject;
        if (temp.containsKey("type") && temp.get("type").equals("string")) {
            String def = (String) temp.get("default");
            if (def != null) {
                def = def.replaceAll("\"$", "").replaceAll("^\"", "");
            }
            def = '"' + def + '"';
            temp.replace("default", def);
        }
        return temp;
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
