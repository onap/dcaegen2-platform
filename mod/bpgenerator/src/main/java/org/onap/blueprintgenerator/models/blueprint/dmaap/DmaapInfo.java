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

package org.onap.blueprintgenerator.models.blueprint.dmaap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import org.onap.blueprintgenerator.models.blueprint.GetInput;

import java.util.LinkedHashMap;
import java.util.TreeMap;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class DmaapInfo {

    private static final String UNDERSCORE = "_";

    private GetInput topic_url;
    private GetInput username;
    private GetInput password;
    private GetInput location;
    private GetInput delivery_url;
    private GetInput subscriber_id;

    public TreeMap<String, LinkedHashMap<String, Object>> createOnapDmaapMRInfo(
        TreeMap<String, LinkedHashMap<String, Object>> inps, String config, char type) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = new TreeMap<String, LinkedHashMap<String, Object>>();
        retInputs = inps;
        LinkedHashMap<String, Object> stringType = new LinkedHashMap<String, Object>();
        stringType.put("type", "string");

        config = config.replaceAll("-", "_");
        if (type == 'p') {
            config = config + "_publish_url";
        } else if (type == 's') {
            config = config + "_subscribe_url";
        }

        GetInput topic = new GetInput();
        topic.setBpInputName(config);
        this.setTopic_url(topic);

        retInputs.put(config, stringType);

        return retInputs;
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createOnapDmaapDRInfo(
        TreeMap<String, LinkedHashMap<String, Object>> inps, String config, char type) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;
        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        String userNameInputName = createInputName(config, "username");
        GetInput username = new GetInput(userNameInputName);
        this.setUsername(username);
        retInputs.put(userNameInputName, stringType);

        String passwordInputName = createInputName(config, "password");
        GetInput password = new GetInput(passwordInputName);
        this.setPassword(password);
        retInputs.put(passwordInputName, stringType);

        String locationInputName = createInputName(config, "location");
        GetInput location = new GetInput(locationInputName);
        this.setLocation(location);
        retInputs.put(locationInputName, stringType);

        String deliveryUrlInputName = createInputName(config, "delivery_url");
        GetInput deliveryUrl = new GetInput(deliveryUrlInputName);
        this.setDelivery_url(deliveryUrl);
        retInputs.put(deliveryUrlInputName, stringType);

        String subscriberIdInputName = createInputName(config, "subscriber_id");
        GetInput subscriberID = new GetInput(subscriberIdInputName);
        this.setSubscriber_id(subscriberID);
        retInputs.put(subscriberIdInputName, stringType);

        return retInputs;
    }

    private String createInputName(String config, String inputName) {
        return config + UNDERSCORE + inputName;
    }
}
