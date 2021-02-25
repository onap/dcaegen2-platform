/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
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

import org.onap.blueprintgenerator.model.common.Interfaces;
import org.onap.blueprintgenerator.model.common.Start;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add Interfaces
 */
@Service
public class InterfacesService {

    @Autowired
    private StartService startService;

    /**
     * Creates Interface to include Start and Start inputs sections in BP for given Inputs and
     * ComponentSpec
     *
     * @param inputs Inputs
     * @param onapComponentSpec  OnapComponentSpec
     * @return
     */
    public Map<String, Object> createInterface(
        Map<String, Map<String, Object>> inputs, OnapComponentSpec onapComponentSpec) {

        Map<String, Object> response = new HashMap<>();
        Interfaces interfaces = new Interfaces();

        Map<String, Object> startResponse = startService.createStart(inputs, onapComponentSpec);
        inputs = (Map<String, Map<String, Object>>) startResponse.get("inputs");

        interfaces.setStart((Start) startResponse.get("start"));

        response.put("interfaces", interfaces);
        response.put("inputs", inputs);
        return response;
    }
}
