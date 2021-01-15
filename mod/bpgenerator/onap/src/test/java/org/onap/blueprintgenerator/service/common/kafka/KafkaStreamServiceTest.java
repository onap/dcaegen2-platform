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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KafkaStreamService.class, BlueprintHelperService.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class KafkaStreamServiceTest {

    private static final String TEST_STREAM_NAME = "test_stream_name";
    private static final String PUBLISH_URL_SUFFIX = "_publish_url";
    private static final String SUBSCRIBE_URL_SUFFIX = "_subscribe_url";
    private static final String DEFAULT_KEY = "default";
    private static final String KAFKA_TYPE = "kafka";

    @Autowired
    private KafkaStreamService kafkaStreamService;

    @Test
    public void createCorrectStreamCommonInputs() {

        Map<String, LinkedHashMap<String, Object>> publishInputs = kafkaStreamService
            .createStreamPublishInputs("test_stream_name");

        LinkedHashMap<String, Object> kafka_bootstrap_servers = publishInputs.get("kafka_bootstrap_servers");
        LinkedHashMap<String, Object> kafka_username = publishInputs.get("kafka_username");
        LinkedHashMap<String, Object> kafka_password = publishInputs.get("kafka_password");

        assertNotNull(kafka_bootstrap_servers);
        assertNotNull(kafka_username);
        assertNotNull(kafka_password);

        assertNotNull(kafka_bootstrap_servers.get(DEFAULT_KEY));
        assertNotNull(kafka_username.get(DEFAULT_KEY));
        assertNotNull(kafka_password.get(DEFAULT_KEY));
    }

    @Test
    public void createCorrectStreamPublishInput() {
        Map<String, LinkedHashMap<String, Object>> publishInputs = kafkaStreamService
            .createStreamPublishInputs(TEST_STREAM_NAME);

        LinkedHashMap<String, Object> kafka_stream_name = publishInputs.get(TEST_STREAM_NAME + PUBLISH_URL_SUFFIX);

        assertNotNull(kafka_stream_name);

        assertNotNull(kafka_stream_name.get(DEFAULT_KEY));
    }

    @Test
    public void createCorrectStreamSubscribeInput() {
        Map<String, LinkedHashMap<String, Object>> publishInputs = kafkaStreamService
            .createStreamSubscribeInputs(TEST_STREAM_NAME);

        LinkedHashMap<String, Object> kafka_stream_name = publishInputs.get(TEST_STREAM_NAME + SUBSCRIBE_URL_SUFFIX);

        assertNotNull(kafka_stream_name);

        assertNotNull(kafka_stream_name.get(DEFAULT_KEY));
    }

    @Test
    public void createCorrectPublishAppConfig() {
        Map<String, KafkaStream> appPropertiesPublish = kafkaStreamService
            .createAppPropertiesPublish(TEST_STREAM_NAME);

        KafkaStream kafkaStream = appPropertiesPublish.get(TEST_STREAM_NAME);

        assertEquals(KAFKA_TYPE, kafkaStream.getType());
        assertNotNull(kafkaStream.getAafCredential());
        assertNotNull(kafkaStream.getKafkaInfo());
        assertTrue(kafkaStream.getKafkaInfo().toString().contains(TEST_STREAM_NAME + PUBLISH_URL_SUFFIX));

    }

    @Test
    public void createCorrectSubscribeAppConfig() {
        Map<String, KafkaStream> appPropertiesSubscribe = kafkaStreamService
            .createAppPropertiesSubscribe(TEST_STREAM_NAME);

        KafkaStream kafkaStream = appPropertiesSubscribe.get(TEST_STREAM_NAME);

        assertEquals(KAFKA_TYPE, kafkaStream.getType());
        assertNotNull(kafkaStream.getAafCredential());
        assertNotNull(kafkaStream.getKafkaInfo());
        assertTrue(kafkaStream.getKafkaInfo().toString().contains(TEST_STREAM_NAME + SUBSCRIBE_URL_SUFFIX));

    }
}
