/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 ================================================================================
 Modifications Copyright (c) 2020 Nokia. All rights reserved.
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

        createInput(config, "password");
        GetInput username = createInput(config, "username");
        this.setUsername(username);
        retInputs.put(username.toString(), stringType);

        GetInput password = createInput(config, "password");
        this.setPassword(password);
        retInputs.put(password.toString(), stringType);

        GetInput location = createInput(config, "location");
        this.setLocation(location);
        retInputs.put(location.toString(), stringType);

        GetInput deliveryUrl = createInput(config, "delivery_url");
        this.setDelivery_url(deliveryUrl);
        retInputs.put(deliveryUrl.toString(), stringType);

        GetInput subscriberID = createInput(config, "subscriber_id");
        this.setSubscriber_id(subscriberID);
        retInputs.put(subscriberID.toString(), stringType);

        return retInputs;
    }

    private GetInput createInput(String config, String inputName) {
        return new GetInput(config + UNDERSCORE + inputName);
    }
}
