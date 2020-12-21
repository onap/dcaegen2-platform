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

import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add DMAAP Message
 * and Data Routers
 */
@Service
public class DmaapService {

    @Autowired
    private InfoService infoService;

    /**
     * Creates Dmaap Message Router from given inputs
     *
     * @param inputs Input Arguments
     * @param config Configuration
     * @param type BP Type
     * @param counter Counter
     * @param num Number Incrementor
     * @param isDmaap Dmaap Argument
     * @return
     */
    public Map<String, Object> createDmaapMessageRouter(
        Map<String, LinkedHashMap<String, Object>> inputs,
        String config,
        char type,
        String counter,
        String num,
        boolean isDmaap) {

        Map<String, Object> response = new HashMap<>();
        Dmaap dmaap = new Dmaap();

        LinkedHashMap<String, Object> stringType = new LinkedHashMap();
        stringType.put("type", "string");

        if (!isDmaap) {
            Map<String, Object> infoResponse = infoService
                .createMessageRouterInfo(inputs, config, type);
            inputs = (Map<String, LinkedHashMap<String, Object>>) infoResponse.get("inputs");
            dmaap.setDmaap_info(infoResponse.get("info"));
        } else {
            String infoType = "<<" + counter + ">>";
            dmaap.setDmaap_info(infoType);

            GetInput u = new GetInput();
            u.setBpInputName(config + "_" + num + "_aaf_username");
            dmaap.setUser(u);
            inputs.put(config + "_" + num + "_aaf_username", stringType);

            GetInput p = new GetInput();
            p.setBpInputName(config + "_" + num + "_aaf_password");
            dmaap.setPass(p);
            inputs.put(config + "_" + num + "_aaf_password", stringType);
        }
        response.put("dmaap", dmaap);
        response.put("inputs", inputs);
        return response;
    }

    /**
     * Creates Dmaap Data Router from given inputs
     *
     * @param inputs Input Arguments
     * @param config Configuration
     * @param counter Counter
     * @param isDmaap Dmaap Argument
     * @return
     */
    public Map<String, Object> createDmaapDataRouter(
        Map<String, LinkedHashMap<String, Object>> inputs,
        String config,
        String counter,
        boolean isDmaap) {

        Map<String, Object> response = new HashMap<>();
        Dmaap dmaap = new Dmaap();

        if (!isDmaap) {
            Map<String, Object> infoResponse = infoService.createDataRouterInfo(inputs, config);
            inputs = (Map<String, LinkedHashMap<String, Object>>) infoResponse.get("inputs");
            dmaap.setDmaap_info(infoResponse.get("info"));
        } else {
            String infoType = "<<" + counter + ">>";
            dmaap.setDmaap_info(infoType);
        }
        response.put("dmaap", dmaap);
        response.put("inputs", inputs);
        return response;
    }
}
