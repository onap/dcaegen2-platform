/*============LICENSE_START=======================================================
 org.onap.dcae 
 ================================================================================ 
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 Copyright (c) 2020 Nokia. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 

      http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License. 
 ============LICENSE_END========================================================= 

 */

package org.onap.blueprintgenerator.models.blueprint;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isDataRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isMessageRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createStringInput;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.onap.blueprintgenerator.models.blueprint.dmaap.DmaapObj;
import org.onap.blueprintgenerator.models.componentspec.CallsObj;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Parameters;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;

@Getter
@Setter
public class Appconfig {

    private CallsObj[] service_calls;
    private TreeMap<String, DmaapObj> streams_publishes;
    private TreeMap<String, DmaapObj> streams_subscribes;
    private TreeMap<String, Object> params;

    @JsonAnyGetter
    public TreeMap<String, Object> getParams() {
        return params;
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createAppconfig(
        TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec componentSpec, String override,
        boolean isDmaap) {

        service_calls = new CallsObj[0];
        streams_publishes = createStreamPublishes(componentSpec, inps, isDmaap);
        streams_subscribes = createStreamSubscribes(componentSpec, inps, isDmaap);
        params = createParameters(componentSpec, inps, override);

        return inps;
    }

    private TreeMap<String, DmaapObj> createStreamPublishes(ComponentSpec componentSpec,
        TreeMap<String, LinkedHashMap<String, Object>> inps, boolean isDmaap) {
        TreeMap<String, DmaapObj> streamPublishes = new TreeMap<>();
        for (Publishes publishes : componentSpec.getStreams().getPublishes()) {
            if (isDataRouterType(publishes.getType())) {
                //in this case the data router information gets added to the params so for now leave it alone
                String config = publishes.getConfig_key();
                DmaapObj pub = new DmaapObj();
                String name = publishes.getConfig_key() + "_feed";
                pub.createOnapDmaapDRObj(inps, config, 'p', name, name, isDmaap);
                pub.setType(publishes.getType());
                streamPublishes.put(config, pub);
            } else if (isMessageRouterType(publishes.getType())) {
                String config = publishes.getConfig_key();
                DmaapObj pub = new DmaapObj();
                String name = publishes.getConfig_key() + "_topic";
                pub.createOnapDmaapMRObj(inps, config, 'p', name, name, isDmaap);
                pub.setType(publishes.getType());
                streamPublishes.put(config, pub);
            }
        }
        return streamPublishes;
    }

    private TreeMap<String, DmaapObj> createStreamSubscribes(ComponentSpec componentSpec,
        TreeMap<String, LinkedHashMap<String, Object>> inputs, boolean isDmaap) {
        TreeMap<String, DmaapObj> streamSubscribes = new TreeMap<>();
        for (Subscribes subscribes : componentSpec.getStreams().getSubscribes()) {
            String config = subscribes.getConfig_key();
            DmaapObj sub = new DmaapObj();
            if (isDataRouterType(subscribes.getType())) {
                //in this case the data router information gets added to the params so for now leave it alone
                String name = subscribes.getConfig_key() + "_feed";
                sub.createOnapDmaapDRObj(inputs, config, 'p', name, name, isDmaap);
            } else if (isMessageRouterType(subscribes.getType())) {
                String name = subscribes.getConfig_key() + "_topic";
                sub.createOnapDmaapMRObj(inputs, config, 's', name, name, isDmaap);
            }
            sub.setType(subscribes.getType());
            streamSubscribes.put(config, sub);
        }
        return streamSubscribes;
    }

    private TreeMap<String, Object> createParameters(ComponentSpec componentSpec,
        TreeMap<String, LinkedHashMap<String, Object>> inputs,
        String override) {
        TreeMap<String, Object> parameters = new TreeMap<>();
        for (Parameters params : componentSpec.getParameters()) {
            String pName = params.getName();
            if (params.isSourced_at_deployment()) {
                GetInput paramInput = new GetInput();
                paramInput.setBpInputName(pName);
                parameters.put(pName, paramInput);

                if (!params.getValue().equals("")) {
                    LinkedHashMap<String, Object> input = createStringInput(params.getValue());
                    inputs.put(pName, input);
                } else {
                    LinkedHashMap<String, Object> input = new LinkedHashMap<>();
                    input.put("type", "string");
                    inputs.put(pName, input);
                }
            } else {
                if ("string".equals(params.getType())) {
                    String val = (String) params.getValue();
                    val = '"' + val + '"';
                    parameters.put(pName, val);
                } else {
                    parameters.put(pName, params.getValue());
                }
            }
        }
        handleOverride(override, parameters, inputs);
        return parameters;
    }

    private void handleOverride(String override, TreeMap<String, Object> parameters,
        TreeMap<String, LinkedHashMap<String, Object>> inps) {
        if (override != null) {
            GetInput ov = new GetInput();
            ov.setBpInputName("service_component_name_override");
            parameters.put("service_component_name_override", ov);
            LinkedHashMap<String, Object> over = new LinkedHashMap<>();
            over.put("type", "string");
            over.put("default", override);
            inps.put("service_component_name_override", over);
        }
    }

}
