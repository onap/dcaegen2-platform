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

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.ResourceConfig;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add
 * ResourceConfig
 */
@Service("onapResourceConfigService")
public class ResourceConfigService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    @Value("${resources.cpuLimit}")
    private String defaultCpuLimit;

    @Value("${resources.memoryLimit}")
    private String defaultMemoryLimit;

    /**
     * Creates Resouce Config for properties
     *
     * @param inputs Inputs
     * @param name Name
     * @return
     */
    public Map<String, Object> createResourceConfig(
        Map<String, LinkedHashMap<String, Object>> inputs, String name) {
        Map<String, Object> response = new HashMap<>();
        ResourceConfig resourceConfig = new ResourceConfig();

        LinkedHashMap<String, Object> memoryLimit =
            blueprintHelperService.createStringInput(defaultMemoryLimit);

        LinkedHashMap<String, Object> cpuLimit =
            blueprintHelperService.createStringInput(defaultCpuLimit);

        name = blueprintHelperService.getNamePrefix(name);

        Map<String, GetInput> lim = new TreeMap<>();

        GetInput cpu = new GetInput();
        cpu.setBpInputName(name + Constants.CPU_LIMIT);
        lim.put("cpu", cpu);

        GetInput memL = new GetInput();
        memL.setBpInputName(name + Constants.MEMORY_LIMIT);
        lim.put("memory", memL);

        inputs.put(name + Constants.CPU_LIMIT, cpuLimit);
        inputs.put(name + Constants.MEMORY_LIMIT, memoryLimit);

        resourceConfig.setLimits(lim);

        Map<String, GetInput> req = new TreeMap<>();

        GetInput cpuR = new GetInput();
        cpuR.setBpInputName(name + Constants.CPU_REQUEST);
        req.put("cpu", cpuR);

        GetInput memR = new GetInput();
        memR.setBpInputName(name + Constants.MEMORY_REQUEST);
        req.put("memory", memR);

        inputs.put(name + Constants.CPU_REQUEST, cpuLimit);
        inputs.put(name + Constants.MEMORY_REQUEST, memoryLimit);

        resourceConfig.setRequests(req);

        response.put("resourceConfig", resourceConfig);
        response.put("inputs", inputs);
        return response;
    }
}
