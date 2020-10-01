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
        TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec cs, String override, boolean isDmaap) {

        setServiceCalls();

        TreeMap<String, DmaapObj> streamPublishes = setStreamPublishes(cs, inps, isDmaap);
        TreeMap<String, DmaapObj> streamSubscribes = setStreamSubscribes(cs, inps, isDmaap);

        this.setStreams_publishes(streamPublishes);
        this.setStreams_subscribes(streamSubscribes);

        TreeMap<String, Object> parameters = setParameters(cs, inps, override);

        this.setParams(parameters);
        return inps;
    }

    private void setServiceCalls() {
        CallsObj[] call = new CallsObj[0];
        this.setService_calls(call);
    }

    private TreeMap<String, DmaapObj> setStreamPublishes(ComponentSpec cs,
        TreeMap<String, LinkedHashMap<String, Object>> inps, boolean isDmaap) {
        TreeMap<String, DmaapObj> streamPublishes = new TreeMap<>();
        if (cs.getStreams().getPublishes().length != 0) {
            for (Publishes p : cs.getStreams().getPublishes()) {
                if (isDataRouterType(p.getType())) {
                    //in this case the data router information gets added to the params so for now leave it alone
                    String config = p.getConfig_key();
                    DmaapObj pub = new DmaapObj();
                    String name = p.getConfig_key() + "_feed";
                    pub.createOnapDmaapDRObj(inps, config, 'p', name, name, isDmaap);
                    pub.setType(p.getType());
                    streamPublishes.put(config, pub);
                } else if (isMessageRouterType(p.getType())) {
                    String config = p.getConfig_key();
                    DmaapObj pub = new DmaapObj();
                    String name = p.getConfig_key() + "_topic";
                    pub.createOnapDmaapMRObj(inps, config, 'p', name, name, isDmaap);
                    pub.setType(p.getType());
                    streamPublishes.put(config, pub);
                }
            }
        }
        return streamPublishes;
    }

    private TreeMap<String, DmaapObj> setStreamSubscribes(ComponentSpec cs,
        TreeMap<String, LinkedHashMap<String, Object>> inps, boolean isDmaap) {
        TreeMap<String, DmaapObj> streamSubscribes = new TreeMap<>();
        if (cs.getStreams().getSubscribes().length != 0) {
            for (Subscribes s : cs.getStreams().getSubscribes()) {
                if (isDataRouterType(s.getType())) {
                    //in this case the data router information gets added to the params so for now leave it alone
                    String config = s.getConfig_key();
                    DmaapObj sub = new DmaapObj();
                    String name = s.getConfig_key() + "_feed";
                    sub.createOnapDmaapDRObj(inps, config, 'p', name, name, isDmaap);
                    sub.setType(s.getType());
                    streamSubscribes.put(config, sub);
                } else if (isMessageRouterType(s.getType())) {
                    String config = s.getConfig_key();
                    DmaapObj sub = new DmaapObj();
                    String name = s.getConfig_key() + "_topic";
                    sub.createOnapDmaapMRObj(inps, config, 's', name, name, isDmaap);
                    sub.setType(s.getType());
                    streamSubscribes.put(config, sub);
                }
            }
        }
        return streamSubscribes;
    }

    private TreeMap<String, Object> setParameters(ComponentSpec cs, TreeMap<String, LinkedHashMap<String, Object>> inps,
        String override) {
        TreeMap<String, Object> parameters = new TreeMap<>();
        for (Parameters p : cs.getParameters()) {
            String pName = p.getName();
            if (p.isSourced_at_deployment()) {
                GetInput paramInput = new GetInput();
                paramInput.setBpInputName(pName);
                parameters.put(pName, paramInput);

                if (!p.getValue().equals("")) {
                    LinkedHashMap<String, Object> inputs = new LinkedHashMap<>();
                    inputs.put("type", "string");
                    inputs.put("default", p.getValue());
                    inps.put(pName, inputs);
                } else {
                    LinkedHashMap<String, Object> inputs = new LinkedHashMap<>();
                    inputs.put("type", "string");
                    inps.put(pName, inputs);
                }
            } else {
                if ("string".equals(p.getType())) {
                    String val = (String) p.getValue();
                    val = '"' + val + '"';
                    parameters.put(pName, val);
                } else {
                    parameters.put(pName, p.getValue());
                }
            }
        }
        handleOverride(override, parameters, inps);
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
