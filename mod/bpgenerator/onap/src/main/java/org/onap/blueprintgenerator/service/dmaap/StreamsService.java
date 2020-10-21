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

package org.onap.blueprintgenerator.service.dmaap;

import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.dmaap.Streams;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Service to create Streams
 */


@Service
public class StreamsService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    //Methos to create streams for Dmaap Blueprint
    public Map<String, Object> createStreams(Map<String, LinkedHashMap<String, Object>> inputs, String name, String type, String key, String route, char o){
        Map<String,Object> response = new HashMap<>();
        Streams streams = new Streams();

        LinkedHashMap<String, Object> stringType = new LinkedHashMap();
        stringType.put("type", "string");

        streams.setName(name);
        streams.setType(type);

        GetInput location = new GetInput();
        location.setBpInputName(key + "_" + name + "_location");
        inputs.put(key + "_" + name + "_location", stringType);
        streams.setLocation(location);

        if(blueprintHelperService.isDataRouterType(type)) {
            if('s' == o) {
                GetInput username = new GetInput();
                username.setBpInputName(key + "_" + name + "_username");
                streams.setUsername(username);
                inputs.put(key + "_" + name + "_username", stringType);

                GetInput password = new GetInput();
                password.setBpInputName(key + "_" + name + "_password");
                streams.setPassword(password);
                inputs.put(key + "_" + name + "_password", stringType);

                GetInput priviliged = new GetInput();
                priviliged.setBpInputName(key + "_" + name + "_priviliged");
                streams.setPrivileged(priviliged);
                inputs.put(key + "_" + name + "_priviliged", stringType);

                GetInput decompress = new GetInput();
                decompress.setBpInputName(key + "_" + name + "_decompress");
                streams.setDecompress(decompress);
                inputs.put(key + "_" + name + "_decompress", stringType);

                streams.setRoute(route);
                streams.setScheme("https");
            }


        } else {
            GetInput client = new GetInput();
            client.setBpInputName(key + "_" + name + "_client_role");
            streams.setClient_role(client);
            inputs.put(key + "_" + name + "_client_role", stringType);
        }
        response.put("streams", streams);
        response.put("inputs", inputs);
        return response;
    }

}
