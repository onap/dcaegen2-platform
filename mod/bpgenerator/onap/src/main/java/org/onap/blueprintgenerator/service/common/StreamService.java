/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Modifications Copyright (c) 2021 Nokia
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.BaseStream;
import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.onap.blueprintgenerator.service.common.kafka.KafkaStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : Joanna Jeremicz
 * @date 01/15/2021 Application: ONAP - Blueprint Generator Common ONAP Service to create publishes and subscribes
 * streams
 */
@Service("streamService")
public class StreamService {

    @Autowired
    private KafkaStreamService kafkaStreamsService;

    /**
     * Creates publishes stream for given Inputs and ComponentSpec
     *
     * @param onapComponentSpec      Onap Component Specification
     * @param blueprintHelperService Blueprint Helper Service
     * @param dmaapService           Dmaap Service
     * @param inputs                 Inputs
     * @param isDmaap                Dmaap Argument
     * @return
     */
    public Map<String, BaseStream> createStreamPublishes(
        OnapComponentSpec onapComponentSpec,
        BlueprintHelperService blueprintHelperService,
        DmaapService dmaapService,
        Map<String, Map<String, Object>> inputs,
        boolean isDmaap) {

        Map<String, BaseStream> streamPublishes = new TreeMap<>();
        if (onapComponentSpec.getStreams() == null || onapComponentSpec.getStreams().getPublishes() == null) {
            return streamPublishes;
        }

        for (Publishes publishes : onapComponentSpec.getStreams().getPublishes()) {
            if (blueprintHelperService.isDataRouterType(publishes.getType())) {
                String config = publishes.getConfig_key();
                String name = config + Constants.A_FEED;
                Map<String, Object> dmaapDataRouterResponse =
                    dmaapService.createDmaapDataRouter(inputs, config, name, isDmaap);
                inputs =
                    (Map<String, Map<String, Object>>) dmaapDataRouterResponse
                        .get("inputs");
                Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                dmaap.setType(publishes.getType());
                streamPublishes.put(config, dmaap);
            } else if (blueprintHelperService.isMessageRouterType(publishes.getType())) {
                String config = publishes.getConfig_key();
                String name = config + Constants.A_TOPIC;
                Map<String, Object> dmaapDataRouterResponse =
                    dmaapService
                        .createDmaapMessageRouter(inputs, config, 'p', name, name, isDmaap);
                inputs =
                    (Map<String, Map<String, Object>>) dmaapDataRouterResponse
                        .get("inputs");
                Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                dmaap.setType(publishes.getType());
                streamPublishes.put(config, dmaap);
            } else if (blueprintHelperService.isKafkaStreamType(publishes.getType())) {
                inputs.putAll(kafkaStreamsService.createStreamPublishInputs(publishes.getConfig_key()));
                streamPublishes.putAll(kafkaStreamsService.createAppPropertiesPublish(publishes.getConfig_key()));
            }
        }
        return streamPublishes;
    }

    /**
     * Creates subscribes stream for given Inputs and ComponentSpec
     *
     * @param onapComponentSpec      Onap Component Specification
     * @param blueprintHelperService Blueprint Helper Service
     * @param dmaapService           Dmaap Service
     * @param inputs                 Inputs
     * @param isDmaap                Dmaap Argument
     * @return
     */
    public Map<String, BaseStream> createStreamSubscribes(
        OnapComponentSpec onapComponentSpec,
        BlueprintHelperService blueprintHelperService,
        DmaapService dmaapService,
        Map<String, Map<String, Object>> inputs,
        boolean isDmaap) {

        Map<String, BaseStream> streamSubscribes = new TreeMap<>();
        if (onapComponentSpec.getStreams() == null || onapComponentSpec.getStreams().getSubscribes() == null) {
            return streamSubscribes;
        }

        for (Subscribes subscribes : onapComponentSpec.getStreams().getSubscribes()) {
            if (blueprintHelperService.isDataRouterType(subscribes.getType())) {
                String config = subscribes.getConfig_key();
                String name = config + Constants.A_FEED;
                Map<String, Object> dmaapDataRouterResponse =
                    dmaapService.createDmaapDataRouter(inputs, config, name, isDmaap);
                inputs =
                    (Map<String, Map<String, Object>>) dmaapDataRouterResponse
                        .get("inputs");
                Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                dmaap.setType(subscribes.getType());
                streamSubscribes.put(config, dmaap);
            } else if (blueprintHelperService.isMessageRouterType(subscribes.getType())) {
                String config = subscribes.getConfig_key();
                String name = config + Constants.A_TOPIC;
                Map<String, Object> dmaapDataRouterResponse =
                    dmaapService
                        .createDmaapMessageRouter(inputs, config, 's', name, name, isDmaap);
                inputs =
                    (Map<String, Map<String, Object>>) dmaapDataRouterResponse
                        .get("inputs");
                Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                dmaap.setType(subscribes.getType());
                streamSubscribes.put(config, dmaap);
            } else if (blueprintHelperService.isKafkaStreamType(subscribes.getType())) {
                inputs.putAll(kafkaStreamsService.createStreamSubscribeInputs(subscribes.getConfig_key()));
                streamSubscribes.putAll(kafkaStreamsService.createAppPropertiesSubscribe(subscribes.getConfig_key()));
            }


        }
        return streamSubscribes;
    }

}
