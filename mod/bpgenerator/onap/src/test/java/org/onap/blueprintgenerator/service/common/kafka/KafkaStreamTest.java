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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class KafkaStreamTest {

    private final static String TEST_TOPIC_NAME = "test_topic";
    private static final String GET_INPUT_KAFKA_USERNAME = "{\"get_input\":\"kafka_username\"}";
    private static final String GET_INPUT_KAFKA_PASSWORD = "{\"get_input\":\"kafka_password\"}";
    private static final String AAF_USERNAME = "username";
    private static final String AAF_PASSWORD = "password";
    private static final String AAF_CREDENTIAL_NODE = "aaf_credential";
    private static final String KAFKA_TYPE_NODE = "type";
    private static final String EXPECTED_KAFKA_TYPE = "\"kafka\"";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "bootstrap_servers";
    private static final String KAFKA_TOPIC_NAME = "topic_name";
    private static final String EXPECTED_GET_INPUT_TOPIC = "{\"get_input\":\"" + TEST_TOPIC_NAME + "\"}";
    private static final String EXPECTED_GET_INPUT_BOOTSTRAP_SERVERS = "{\"get_input\":\"kafka_bootstrap_servers\"}";
    private static final String KAFKA_INFO_NODE = "kafka_info";

    private KafkaStream kafkaStream;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        kafkaStream = new KafkaStream(TEST_TOPIC_NAME);
    }

    @Test
    public void kafkaStreamHasCorrectAafCredential() throws IOException {

        String kafkaStreamJson = mapper.writeValueAsString(kafkaStream);

        JsonNode kafkaStreamNode = mapper.readTree(kafkaStreamJson);
        JsonNode aafCredential = kafkaStreamNode.get(AAF_CREDENTIAL_NODE);

        assertNotNull(aafCredential);
        assertEquals(GET_INPUT_KAFKA_USERNAME, aafCredential.get(AAF_USERNAME).toString());
        assertEquals(GET_INPUT_KAFKA_PASSWORD, aafCredential.get(AAF_PASSWORD).toString());
    }

    @Test
    public void kafkaStreamHasCorrectKafkaInfo() throws IOException {

        String kafkaStreamJson = mapper.writeValueAsString(kafkaStream);

        JsonNode kafkaStreamNode = mapper.readTree(kafkaStreamJson);
        JsonNode kafkaInfo = kafkaStreamNode.get(KAFKA_INFO_NODE);

        assertNotNull(kafkaInfo);
        assertEquals(EXPECTED_GET_INPUT_BOOTSTRAP_SERVERS, kafkaInfo.get(KAFKA_BOOTSTRAP_SERVERS).toString());
        assertEquals(EXPECTED_GET_INPUT_TOPIC, kafkaInfo.get(KAFKA_TOPIC_NAME).toString());

    }

    @Test
    public void kafkaStreamHasCorrectType() throws IOException {

        String kafkaStreamJson = mapper.writeValueAsString(kafkaStream);

        JsonNode kafkaStreamNode = mapper.readTree(kafkaStreamJson);
        JsonNode kafkaType = kafkaStreamNode.get(KAFKA_TYPE_NODE);

        assertNotNull(kafkaType);
        assertEquals(EXPECTED_KAFKA_TYPE, kafkaType.toString());
    }

}
