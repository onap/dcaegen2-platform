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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.onap.blueprintgenerator.model.common.BaseStream;


/**
 * @author : Tomasz Wrobel
 * @date 01/18/2021 Application: DCAE/ONAP - Blueprint Generator
 * Applications Common Model: A model class which represents Kafka Stream
 */

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaStream extends BaseStream {

    private final String type = "kafka";

    @JsonProperty("aaf_credential")
    private AafCredential aafCredential;

    @JsonProperty("kafka_info")
    private KafkaInfo kafkaInfo;

    public KafkaStream(String topicName) {
        this.aafCredential = new AafCredential(AFF_KAFKA_USER_INPUT_NAME, AAF_KAFKA_PASSWORD_INPUT_NAME);
        this.kafkaInfo = new KafkaInfo(topicName);
    }
}
