/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2021 Nokia. All rights reserved.
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

import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.StartInputs;
import org.onap.blueprintgenerator.model.componentspec.OnapAuxilary;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add Start Inputs
 * Node under Start
 */
@Service
public class StartInputsService {

    @Autowired
    private PgaasNodeService pgaasNodeService;

    /**
     * Creates Start Inputs for Start in Interfaces
     *
     * @param inputs Inputs
     * @param onapComponentSpec OnapComponentSpec
     * @return
     */
    public Map<String, Object> createStartInputs(
        Map<String, Map<String, Object>> inputs, OnapComponentSpec onapComponentSpec) {

        Map<String, Object> response = new HashMap<>();
        StartInputs startInputs = new StartInputs();

        int count = 0;
        List<String> portList = new ArrayList();
        OnapAuxilary aux = onapComponentSpec.getAuxilary();

        if (aux.getPorts() != null) {
            for (Object p : aux.getPorts()) {
                String[] ports = p.toString().split(":");
                portList.add(
                    String.format("concat: [\"%s:\", {get_input: external_port_%d}]", ports[0],
                        count));

                Map<String, Object> portType = new LinkedHashMap();
                portType.put("type", "string");
                portType.put("default", ports[1]);
                inputs.put("external_port_" + count, portType);
                count++;
            }
        }

        startInputs.setPorts(portList);

        Map<String, Object> envMap = new LinkedHashMap();
        if (onapComponentSpec.getAuxilary().getDatabases() != null) {
            Map<String, Object> envVars =
                pgaasNodeService.getEnvVariables(onapComponentSpec.getAuxilary().getDatabases());
            startInputs.setEnvs(envVars);
            envMap.put("default", "&envs {}");
        } else {
            GetInput env = new GetInput();
            env.setBpInputName("envs");
            startInputs.setEnvs(env);
            envMap.put("default", "{}");
        }
        inputs.put("envs", envMap);

        response.put("startInputs", startInputs);
        response.put("inputs", inputs);
        return response;
    }
}
