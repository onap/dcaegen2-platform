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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.blueprintgenerator.model.common.Dmaap;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Streams;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;

class StreamServiceTest {

    private StreamService streamService;

    OnapComponentSpec onapComponentSpecMock;
    BlueprintHelperService blueprintHelperServiceMock;
    DmaapService dmaapServiceMock;

    Streams streamsMock;

    @BeforeEach
    public void setup() {
        streamService = new StreamService();
        onapComponentSpecMock = mock(OnapComponentSpec.class);
        blueprintHelperServiceMock = mock(BlueprintHelperService.class);
        dmaapServiceMock = mock(DmaapService.class);

        streamsMock = mock(Streams.class);
    }

    @Test
    void whenStreamsIsNullCreateStreamPublishesShouldReturnEmptyMap() {
        when(onapComponentSpecMock.getStreams()).thenReturn(null);

        Map<String, Dmaap> streamPublishes = streamService.createStreamPublishes(
            onapComponentSpecMock,
            blueprintHelperServiceMock,
            dmaapServiceMock,
            createInputs(),
            true);

        assertTrue(streamPublishes.isEmpty());
    }

    @Test
    void whenPublishesIsNullCreateStreamPublishesShouldReturnEmptyMap() {
        when(streamsMock.getPublishes()).thenReturn(null);
        when(onapComponentSpecMock.getStreams()).thenReturn(streamsMock);

        Map<String, Dmaap> streamPublishes = streamService.createStreamPublishes(
            onapComponentSpecMock,
            blueprintHelperServiceMock,
            dmaapServiceMock,
            createInputs(),
            true);

        assertTrue(streamPublishes.isEmpty());
    }

    @Test
    void whenPublishesIsNotEmptyCreateStreamPublishesShouldReturnNonEmptyMap() {
        when(streamsMock.getPublishes()).thenReturn(createPublishesArray());
        when(onapComponentSpecMock.getStreams()).thenReturn(streamsMock);
        when(blueprintHelperServiceMock.isDataRouterType(anyString())).thenReturn(true);

        DmaapService dmaapService = new DmaapService();

        Map<String, Dmaap> streamPublishes = streamService.createStreamPublishes(
            onapComponentSpecMock,
            blueprintHelperServiceMock,
            dmaapService,
            createInputs(),
            true);


        Map<String, Dmaap> expectedMap = createExpectedMap();

        assertNotNull(streamPublishes);
        assertEquals(expectedMap.size(), streamPublishes.size());
        for(Map.Entry<String, Dmaap> entry : expectedMap.entrySet()) {
            assertTrue(streamPublishes.containsKey(entry.getKey()));
            assertTrue(streamPublishes.get(entry.getKey()).getType().equals(entry.getValue().getType()));
            assertTrue(streamPublishes.get(entry.getKey()).getDmaap_info().equals(entry.getValue().getDmaap_info()));
        }
    }

    private Map<String, Dmaap> createExpectedMap() {
        Map<String, Dmaap> expectedMap = new HashMap<>();
        Dmaap dmaap1 = new Dmaap();
        dmaap1.setType("t1");
        dmaap1.setDmaap_info("<<k1_feed>>");

        Dmaap dmaap2 = new Dmaap();
        dmaap2.setType("t2");
        dmaap2.setDmaap_info("<<k2_feed>>");

        Dmaap dmaap3 = new Dmaap();
        dmaap3.setType("t3");
        dmaap3.setDmaap_info("<<k3_feed>>");

        expectedMap.put("k1", dmaap1);
        expectedMap.put("k2", dmaap2);
        expectedMap.put("k3", dmaap3);
        return expectedMap;
    }

    private Publishes[] createPublishesArray() {
        Publishes pub1 = createPublishes("k1", "t1");
        Publishes pub2 = createPublishes("k2", "t2");
        Publishes pub3 = createPublishes("k3", "t3");

        return new Publishes[]{pub1, pub2, pub3};
    }

    private Publishes createPublishes(String key, String type){
        Publishes publishes = new Publishes();
        publishes.setConfig_key(key);
        publishes.setType(type);
        return publishes;
    }

    private Map<String, LinkedHashMap<String, Object>> createInputs(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("key-1", "obj-1");

        Map<String, LinkedHashMap<String, Object>> mapsMap = new HashMap<>();
        mapsMap.put("inputs", map);
        return mapsMap;
    }
}
