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

package com.att.blueprintgenerator.service;

import com.att.blueprintgenerator.model.common.GetInput;
import com.att.blueprintgenerator.model.common.Info;
import com.att.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Service to create Message Router and Data Router Information
 */

@Service
public class InfoService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    public Map<String,Object> createMessageRouterInfo(Map<String, LinkedHashMap<String, Object>> inputs, String config, char type) {

        Map<String,Object> response = new HashMap<>();
        Info info = new Info();

        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        config = config.replaceAll("-", "_");
        if(type == 'p') {
            config = config + "_publish_url";
        }
        else if(type == 's') {
            config = config+ "_subscribe_url";
        }

        GetInput topic = new GetInput();
        topic.setBpInputName(config);
        info.setTopic_url(topic);

        inputs.put(config, stringType);

        response.put("info", info);
        response.put("inputs", inputs);
        return response;
    }

    public Map<String,Object> createDataRouterInfo(Map<String, LinkedHashMap<String, Object>> inputs, String config) {

        Map<String,Object> response = new HashMap<>();
        Info info = new Info();

        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        String userNameInputName = blueprintHelperService.joinUnderscore(config, "username");
        GetInput username = new GetInput(userNameInputName);
        info.setUsername(username);
        inputs.put(userNameInputName, stringType);

        String userpasswordInputName = blueprintHelperService.joinUnderscore(config, "password");
        GetInput password = new GetInput(userpasswordInputName);
        info.setPassword(password);
        inputs.put(userpasswordInputName, stringType);

        String userlocationInputName = blueprintHelperService.joinUnderscore(config, "location");
        GetInput location = new GetInput(userlocationInputName);
        info.setLocation(location);
        inputs.put(userlocationInputName, stringType);

        String userdeliveryUrlInputName = blueprintHelperService.joinUnderscore(config, "delivery_url");
        GetInput deliveryUrl = new GetInput(userdeliveryUrlInputName);
        info.setDelivery_url(deliveryUrl);
        inputs.put(userdeliveryUrlInputName, stringType);

        String usersubscriberIDInputName = blueprintHelperService.joinUnderscore(config, "subscriber_id");
        GetInput subscriberID = new GetInput(usersubscriberIDInputName);
        info.setSubscriber_id(subscriberID);
        inputs.put(usersubscriberIDInputName, stringType);

        response.put("info", info);
        response.put("inputs", inputs);
        return response;
    }

}
