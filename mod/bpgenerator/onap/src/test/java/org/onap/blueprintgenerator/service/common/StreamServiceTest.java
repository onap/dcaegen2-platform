/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2021  Nokia Intellectual Property. All rights reserved.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;

class StreamServiceTest {

    private StreamService streamService;

    OnapComponentSpec onapComponentSpecMock;
    BlueprintHelperService blueprintHelperServiceMock;
    DmaapService dmaapServiceMock;

    @BeforeEach
    public void setup() {
        streamService = new StreamService();
        onapComponentSpecMock = mock(OnapComponentSpec.class);
        blueprintHelperServiceMock = mock(BlueprintHelperService.class);
        dmaapServiceMock = mock(DmaapService.class);
    }

    @Test
    void createStreamPublishes() {

        when(onapComponentSpecMock.getStreams()).thenReturn(null);

        Map<String, Dmaap> streamPublishes = streamService.createStreamPublishes(
            onapComponentSpecMock,
            blueprintHelperServiceMock,
            dmaapServiceMock,
            createInputs(),
            true);

        System.out.println(streamPublishes);
    }

    @Test
    void createStreamSubscribes() {
    }

    private Map<String, LinkedHashMap<String, Object>> createInputs(){

//        Map<String, LinkedHashMap<String, Object>> inputs = new
        return null;
    }
}
