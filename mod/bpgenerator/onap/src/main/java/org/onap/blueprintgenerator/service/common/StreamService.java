/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  Nokia Intellectual Property. All rights reserved.
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
import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.stereotype.Service;

@Service("streamService")
public class StreamService {

    public Map<String, Dmaap> createStreamPublishes(
        OnapComponentSpec onapComponentSpec,
        BlueprintHelperService blueprintHelperService,
        DmaapService dmaapService,
        Map<String, LinkedHashMap<String, Object>> inputs,
        boolean isDmaap) {

        Map<String, Dmaap> streamPublishes = new TreeMap<>();
        if (onapComponentSpec.getStreams() != null) {
            if (onapComponentSpec.getStreams().getPublishes() != null) {
                for (Publishes publishes : onapComponentSpec.getStreams().getPublishes()) {
                    if (blueprintHelperService.isDataRouterType(publishes.getType())) {
                        String config = publishes.getConfig_key();
                        String name = config + Constants._FEED;
                        Map<String, Object> dmaapDataRouterResponse =
                            dmaapService.createDmaapDataRouter(inputs, config, name, isDmaap);
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>) dmaapDataRouterResponse
                                .get("inputs");
                        Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                        dmaap.setType(publishes.getType());
                        streamPublishes.put(config, dmaap);
                    } else if (blueprintHelperService.isMessageRouterType(publishes.getType())) {
                        String config = publishes.getConfig_key();
                        String name = config + Constants._TOPIC;
                        Map<String, Object> dmaapDataRouterResponse =
                            dmaapService
                                .createDmaapMessageRouter(inputs, config, 'p', name, name, isDmaap);
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>) dmaapDataRouterResponse
                                .get("inputs");
                        Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                        dmaap.setType(publishes.getType());
                        streamPublishes.put(config, dmaap);
                    }
                }
            }
        }
        return streamPublishes;
    }

    public Map<String, Dmaap> createStreamSubscribes(
        OnapComponentSpec onapComponentSpec,
        BlueprintHelperService blueprintHelperService,
        DmaapService dmaapService,
        Map<String, LinkedHashMap<String, Object>> inputs,
        boolean isDmaap) {

        Map<String, Dmaap> streamSubscribes = new TreeMap<>();
        if (onapComponentSpec.getStreams() != null) {
            if (onapComponentSpec.getStreams().getSubscribes() != null) {
                for (Subscribes subscribes : onapComponentSpec.getStreams().getSubscribes()) {
                    if (blueprintHelperService.isDataRouterType(subscribes.getType())) {
                        String config = subscribes.getConfig_key();
                        String name = config + Constants._FEED;
                        Map<String, Object> dmaapDataRouterResponse =
                            dmaapService.createDmaapDataRouter(inputs, config, name, isDmaap);
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>) dmaapDataRouterResponse
                                .get("inputs");
                        Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                        dmaap.setType(subscribes.getType());
                        streamSubscribes.put(config, dmaap);
                    } else if (blueprintHelperService.isMessageRouterType(subscribes.getType())) {
                        String config = subscribes.getConfig_key();
                        String name = config + Constants._TOPIC;
                        Map<String, Object> dmaapDataRouterResponse =
                            dmaapService
                                .createDmaapMessageRouter(inputs, config, 's', name, name, isDmaap);
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>) dmaapDataRouterResponse
                                .get("inputs");
                        Dmaap dmaap = (Dmaap) dmaapDataRouterResponse.get("dmaap");
                        dmaap.setType(subscribes.getType());
                        streamSubscribes.put(config, dmaap);
                    }
                }
            }
        }
        return streamSubscribes;
    }

}
