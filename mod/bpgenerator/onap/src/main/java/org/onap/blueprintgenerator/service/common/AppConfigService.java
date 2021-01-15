/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Modifications Copyright (c) 2021 Nokia
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package org.onap.blueprintgenerator.service.common;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.Appconfig;
import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Calls;
import org.onap.blueprintgenerator.model.componentspec.common.Parameters;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service used to create App
 * Config
 */
@Service("onapAppConfigService")
public class AppConfigService {

    @Autowired
    private DmaapService dmaapService;

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    @Autowired
    private StreamService streamService;

    /**
     * Creates Inputs section under App Config with Publishes, Subscribes, Parameters sections by
     * checking Datarouter/MessageRouter/override/Dmaap values
     *
     * @param inputs Inputs
     * @param onapComponentSpec Onap Component Specification
     * @param override Parameter to Service Component Override
     * @param isDmaap Dmaap Argument
     * @return
     */
    public Map<String, Object> createAppconfig(
        Map<String, LinkedHashMap<String, Object>> inputs,
        OnapComponentSpec onapComponentSpec,
        String override,
        boolean isDmaap) {

        Map<String, Object> response = new HashMap<>();
        Appconfig appconfig = new Appconfig();

        Calls[] call = new Calls[0];
        appconfig.setService_calls(call);

        Map<String, Dmaap> streamPublishes = streamService.createStreamPublishes(
            onapComponentSpec, blueprintHelperService, dmaapService, inputs, isDmaap);
        Map<String, Dmaap> streamSubscribes = streamService.createStreamSubscribes(
            onapComponentSpec, blueprintHelperService, dmaapService, inputs, isDmaap);

        appconfig.setStreams_publishes(streamPublishes);
        appconfig.setStreams_subscribes(streamSubscribes);

        Map<String, Object> parameters = new TreeMap<>();
        for (Parameters p : onapComponentSpec.getParameters()) {
            String pName = p.getName();
            if (p.isSourced_at_deployment()) {
                GetInput paramInput = new GetInput();
                paramInput.setBpInputName(pName);
                parameters.put(pName, paramInput);
                if (!"".equals(p.getValue())) {
                    LinkedHashMap<String, Object> pInputs =
                        blueprintHelperService.createStringInput(p.getValue());
                    inputs.put(pName, pInputs);
                } else {
                    LinkedHashMap<String, Object> pInputs = new LinkedHashMap<>();
                    pInputs.put("type", "string");
                    inputs.put(pName, pInputs);
                }
            } else {
                if ("string".equals(p.getType())) {
                    String val = (String) p.getValue();
                    val = '"' + val + '"';
                    parameters.put(pName, val);
                } else {
                    parameters.put(pName, p.getValue());
                    // Updated code to resolve the issue of missing \ for collector.schema.file
                    // parameters.put(pName, pName.equals("collector.schema.file") ?
                    // ((String)p.getValue()).replace("\"", "\\\"") : p.getValue());
                }
            }
        }
        if (override != null) {
            GetInput ov = new GetInput();
            ov.setBpInputName(Constants.SERVICE_COMPONENT_NAME_OVERRIDE);
            parameters.put(Constants.SERVICE_COMPONENT_NAME_OVERRIDE, ov);
            LinkedHashMap<String, Object> over = blueprintHelperService.createStringInput(override);
            inputs.put(Constants.SERVICE_COMPONENT_NAME_OVERRIDE, over);
        }
        appconfig.setParams(parameters);

        response.put("appconfig", appconfig);
        response.put("inputs", inputs);
        return response;
    }
}
