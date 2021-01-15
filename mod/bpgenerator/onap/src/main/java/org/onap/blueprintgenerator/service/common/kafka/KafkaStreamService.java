/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2021 Nokia Intellectual Property. All rights reserved.
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

package org.onap.blueprintgenerator.service.common.kafka;


import static org.onap.blueprintgenerator.service.common.kafka.KafkaCommonConstants.AAF_KAFKA_PASSWORD_INPUT_NAME;
import static org.onap.blueprintgenerator.service.common.kafka.KafkaCommonConstants.AFF_KAFKA_USER_INPUT_NAME;
import static org.onap.blueprintgenerator.service.common.kafka.KafkaCommonConstants.KAFKA_INFO_BOOTSTRAP_SERVERS_INPUT_NAME;

import java.util.LinkedHashMap;
import java.util.Map;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KafkaStreamService {

    private static final String PUBLISH_URL_SUFFIX = "_publish_url";
    private static final String SUBSCRIBE_URL_SUFFIX = "_subscribe_url";
    private static final String DEFAULT_STREAM_URL = "HV_VES_PERF3GPP";
    private static final String DEFAULT_BOOTSTRAP_SERVER = "message-router-kafka:9092";
    private static final String DEFAULT_AAF_USER = "admin";
    private static final String DEFAULT_AAF_PASSWORD = "admin_secret";

    @Autowired
    private BlueprintHelperService blueprintHelperService;


    public Map<String, LinkedHashMap<String, Object>> createStreamPublishInputs(String config_key) {
        return createStreamInputs(config_key + PUBLISH_URL_SUFFIX);
    }


    public Map<String, LinkedHashMap<String, Object>> createStreamSubscribeInputs(String config_key) {
        return createStreamInputs(config_key + SUBSCRIBE_URL_SUFFIX);
    }

    public Map<String, KafkaStream> createAppPropertiesPublish(String config_key) {

        LinkedHashMap<String, KafkaStream> kafkaStreamMap = new LinkedHashMap<>();
        KafkaStream kafkaStream = createAppProperties(config_key, PUBLISH_URL_SUFFIX);

        kafkaStreamMap.put(config_key, kafkaStream);

        return kafkaStreamMap;
    }

    public Map<String, KafkaStream> createAppPropertiesSubscribe(String config_key) {

        LinkedHashMap<String, KafkaStream> kafkaStreamMap = new LinkedHashMap<>();
        KafkaStream kafkaStream = createAppProperties(config_key, SUBSCRIBE_URL_SUFFIX);

        kafkaStreamMap.put(config_key, kafkaStream);

        return kafkaStreamMap;
    }

    private KafkaStream createAppProperties(String config_key, String urlSuffix) {
        String topicName = config_key + urlSuffix;

        return new KafkaStream(topicName);
    }

    private Map<String, LinkedHashMap<String, Object>> createStreamInputs(String streamName) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> streamInputs = createBaseInputs();
        LinkedHashMap<String, Object> stream =
            blueprintHelperService.createStringInput(DEFAULT_STREAM_URL);
        streamInputs.put(streamName, stream);
        return streamInputs;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Object>> createBaseInputs() {
        LinkedHashMap<String, LinkedHashMap<String, Object>> baseInputs = new LinkedHashMap<>();

        LinkedHashMap<String, Object> kafka_message_router = blueprintHelperService
            .createStringInput(DEFAULT_BOOTSTRAP_SERVER);
        baseInputs.put(KAFKA_INFO_BOOTSTRAP_SERVERS_INPUT_NAME, kafka_message_router);

        LinkedHashMap<String, Object> kafka_username = blueprintHelperService.createStringInput(DEFAULT_AAF_USER);
        baseInputs.put(AFF_KAFKA_USER_INPUT_NAME, kafka_username);

        LinkedHashMap<String, Object> kafka_password = blueprintHelperService.createStringInput(DEFAULT_AAF_PASSWORD);
        baseInputs.put(AAF_KAFKA_PASSWORD_INPUT_NAME, kafka_password);

        return baseInputs;
    }
}
