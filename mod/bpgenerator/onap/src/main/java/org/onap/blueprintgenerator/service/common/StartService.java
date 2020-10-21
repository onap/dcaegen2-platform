/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
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


import org.onap.blueprintgenerator.model.common.Start;
import org.onap.blueprintgenerator.model.common.StartInputs;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to add Start Node
 */


@Service
public class StartService {

    @Autowired
    private StartInputsService startInputsService;

    // Method to create Start for Interfaces
    public Map<String,Object> createStart(Map<String, LinkedHashMap<String, Object>> inputs, OnapComponentSpec onapComponentSpec) {
        Map<String,Object> response = new HashMap<>();
        Start start = new Start();

        Map<String, Object> startInputsResponse = startInputsService.createStartInputs(inputs, onapComponentSpec);
        inputs = (Map<String, LinkedHashMap<String, Object>>) startInputsResponse.get("inputs");
        start.setInputs((StartInputs) startInputsResponse.get("startInputs"));

        response.put("start", start);
        response.put("inputs", inputs);
        return response;
    }

}
