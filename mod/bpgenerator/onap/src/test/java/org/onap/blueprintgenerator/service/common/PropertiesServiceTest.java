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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.Properties;
import org.onap.blueprintgenerator.model.componentspec.OnapAuxilary;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Artifacts;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Self;
import org.onap.blueprintgenerator.model.componentspec.common.Streams;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.model.componentspec.common.Volumes;
import org.onap.blueprintgenerator.model.dmaap.TlsInfo;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.onap.blueprintgenerator.service.dmaap.StreamsService;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesServiceTest {

    private static final String PROPERTIES = "properties";
    private static final String INPUTS = "inputs";

    @InjectMocks
    private PropertiesService propertiesService;
    @Mock
    private AppConfigService appConfigService;
    @Mock
    private ResourceConfigService resourceConfigService;
    @Mock
    private StreamsService streamsService;
    @Mock
    private BlueprintHelperService blueprintHelperService;

    private Map<String, ?> resourceConfigResponse;

    @Before
    public void setup() {
        resourceConfigResponse = Map.of(INPUTS, new LinkedHashMap<>());
        when(resourceConfigService.createResourceConfig(any(), eq(getSelf().getName())))
            .thenReturn((Map<String, Object>) resourceConfigResponse);
    }

    @Test
    public void shouldReturnInputsAndPropertiesForDmaap() {
        //when
        Map<String, Object> response = propertiesService
            .createDmaapProperties(new HashMap<>(), getOnapComponentSpec(), "");

        //then
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        assertNotNull(response.get(INPUTS));
        assertEquals(resourceConfigResponse.get(INPUTS), response.get(INPUTS));
        assertEquals(getProperties("tag_version", false), response.get(PROPERTIES));

        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_subscribes());
        assertNull(actualProperties.getStreams_publishes());
    }

    @Test
    public void shouldReturnInputsAndPropertiesForOnap() {
        //given
        when(appConfigService.createAppconfig(any(), eq(getOnapComponentSpec()), eq(""), eq(false)))
            .thenReturn((Map<String, Object>) resourceConfigResponse);

        //when
        Map<String, Object> response = propertiesService
            .createOnapProperties(new HashMap<>(), getOnapComponentSpec(), "");

        //then
        assertNotNull(response.get(PROPERTIES));
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        assertNotNull(response.get(INPUTS));
        assertEquals(resourceConfigResponse.get(INPUTS), response.get(INPUTS));
        assertEquals(getProperties("image", true), response.get(PROPERTIES));

        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_subscribes());
        assertNull(actualProperties.getStreams_publishes());
    }

    @Test
    public void shouldSetStreamsMessageRoutesPublishesInProperties() {
        //given
        OnapComponentSpec onapComponentSpec = getOnapComponentSpecWithStreamsPublishes();
        Publishes publishes = onapComponentSpec.getStreams().getPublishes()[0];
        when(blueprintHelperService.isMessageRouterType(eq(publishes.getType()))).thenReturn(true);
        when(streamsService.createStreams(any(), eq(publishes.getConfig_key() + Constants._TOPIC),
            eq(publishes.getType()),
            eq(publishes.getConfig_key()),
            eq(publishes.getRoute()),
            eq('p'))).thenReturn(getStreams());

        //when
        Map<String, Object> response = propertiesService
            .createDmaapProperties(new HashMap<>(), onapComponentSpec, "");

        //then
        assertNotNull(response.get(PROPERTIES));
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_subscribes());
        assertNotNull(actualProperties.getStreams_publishes());
        assertEquals(List.of(getDmaapStreams()), actualProperties.getStreams_publishes());
    }

    @Test
    public void shouldSetStreamsDataRoutesPublishesInProperties() {
        //given
        OnapComponentSpec onapComponentSpec = getOnapComponentSpecWithStreamsPublishes();
        Publishes publishes = onapComponentSpec.getStreams().getPublishes()[0];
        when(blueprintHelperService.isDataRouterType(eq(publishes.getType()))).thenReturn(true);
        when(streamsService.createStreams(any(), eq(publishes.getConfig_key() + Constants._FEED),
            eq(publishes.getType()),
            eq(publishes.getConfig_key()),
            eq(publishes.getRoute()),
            eq('p'))).thenReturn(getStreams());

        //when
        Map<String, Object> response = propertiesService
            .createDmaapProperties(new HashMap<>(), onapComponentSpec, "");

        //then
        assertNotNull(response.get(PROPERTIES));
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_subscribes());
        assertNotNull(actualProperties.getStreams_publishes());
        assertEquals(List.of(getDmaapStreams()), actualProperties.getStreams_publishes());
    }

    @Test
    public void shouldSetStreamsDataRoutesSubscribesInProperties() {
        //given
        OnapComponentSpec onapComponentSpec = getOnapComponentSpecWithStreamsSubscribes();
        Subscribes subscribes = onapComponentSpec.getStreams().getSubscribes()[0];
        when(blueprintHelperService.isMessageRouterType(eq(subscribes.getType()))).thenReturn(true);
        when(streamsService.createStreams(any(), eq(subscribes.getConfig_key() + Constants._TOPIC),
            eq(subscribes.getType()),
            eq(subscribes.getConfig_key()),
            eq(subscribes.getRoute()),
            eq('s'))).thenReturn(getStreams());

        //when
        Map<String, Object> response = propertiesService
            .createDmaapProperties(new HashMap<>(), onapComponentSpec, "");

        //then
        assertNotNull(response.get(PROPERTIES));
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_publishes());
        assertNotNull(actualProperties.getStreams_subscribes());
        assertEquals(List.of(getDmaapStreams()), actualProperties.getStreams_subscribes());
    }

    @Test
    public void shouldSetStreamsMessageRoutesSubscribesInProperties() {
        //given
        OnapComponentSpec onapComponentSpec = getOnapComponentSpecWithStreamsSubscribes();
        Subscribes subscribes = onapComponentSpec.getStreams().getSubscribes()[0];
        when(blueprintHelperService.isDataRouterType(eq(subscribes.getType()))).thenReturn(true);
        when(streamsService.createStreams(any(), eq(subscribes.getConfig_key() + Constants._FEED),
            eq(subscribes.getType()),
            eq(subscribes.getConfig_key()),
            eq(subscribes.getRoute()),
            eq('s'))).thenReturn(getStreams());

        //when
        Map<String, Object> response = propertiesService
            .createDmaapProperties(new HashMap<>(), onapComponentSpec, "");

        //then
        assertNotNull(response.get(PROPERTIES));
        assertTrue(response.get(PROPERTIES) instanceof Properties);
        Properties actualProperties = (Properties) response.get(PROPERTIES);
        assertNull(actualProperties.getStreams_publishes());
        assertNotNull(actualProperties.getStreams_subscribes());
        assertEquals(List.of(getDmaapStreams()), actualProperties.getStreams_subscribes());
    }

    private OnapComponentSpec getOnapComponentSpecWithStreamsSubscribes() {
        OnapComponentSpec onapComponentSpec = getOnapComponentSpec();
        Streams streams = new Streams();
        Subscribes subscribes = getSubscribes();
        streams.setSubscribes(new Subscribes[]{subscribes});
        onapComponentSpec.setStreams(streams);
        return onapComponentSpec;
    }

    private OnapComponentSpec getOnapComponentSpecWithStreamsPublishes() {
        OnapComponentSpec onapComponentSpec = getOnapComponentSpec();
        Streams streams = new Streams();
        Publishes publishes1 = getPublishes();
        streams.setPublishes(new Publishes[]{publishes1});
        onapComponentSpec.setStreams(streams);
        return onapComponentSpec;
    }

    private HashMap<String, Object> getStreams() {
        return new HashMap<>() {{
            put("streams", getDmaapStreams());
        }};
    }

    private org.onap.blueprintgenerator.model.dmaap.Streams getDmaapStreams() {
        org.onap.blueprintgenerator.model.dmaap.Streams dmaapStreams = new org.onap.blueprintgenerator.model.dmaap.Streams();
        dmaapStreams.setName("testDmaapStreams");
        return dmaapStreams;
    }

    private Publishes getPublishes() {
        Publishes publishes = new Publishes();
        publishes.setConfig_key("test_config_key");
        publishes.setFormat("test_format");
        publishes.setRoute("test_route");
        publishes.setType("test_type");
        publishes.setVersion("test_version");
        return publishes;
    }

    private Subscribes getSubscribes() {
        Subscribes subscribes = new Subscribes();
        subscribes.setConfig_key("test_config_key_s");
        subscribes.setFormat("test_format_s");
        subscribes.setRoute("test_route_s");
        subscribes.setType("test_type_s");
        subscribes.setVersion("test_version_s");
        return subscribes;
    }

    private Properties getProperties(String image, boolean alwaysPullImage) {
        Properties expectedProperties = new Properties();
        expectedProperties.setDocker_config(getOnapAuxilary());
        expectedProperties.setImage(getGetInput(image));
        expectedProperties.setLocation_id(getGetInput("location_id"));
        expectedProperties.setService_component_type("test-Name");
        expectedProperties.setLog_info(Collections.emptyMap());
        expectedProperties.setReplicas(getGetInput("replicas"));
        expectedProperties.setTls_info(getTlsInfo());
        if (alwaysPullImage) {
            expectedProperties.setAlways_pull_image(getGetInput("always_pull_image"));
        }
        return expectedProperties;
    }

    private TlsInfo getTlsInfo() {
        TlsInfo tlsInfo = new TlsInfo();
        tlsInfo.setUseTls(getGetInput("use_tls"));
        return tlsInfo;
    }

    private GetInput getGetInput(String location_id) {
        GetInput location = new GetInput();
        location.setBpInputName(location_id);
        return location;
    }

    private OnapComponentSpec getOnapComponentSpec() {
        OnapComponentSpec onapComponentSpec = new OnapComponentSpec();
        onapComponentSpec.setArtifacts(new Artifacts[]{getTestUriArtifact()});
        onapComponentSpec.setAuxilary(getOnapAuxilary());
        onapComponentSpec.setSelf(getSelf());
        return onapComponentSpec;
    }

    private Self getSelf() {
        Self self = new Self();
        self.setName("test.Name");
        return self;
    }

    private Artifacts getTestUriArtifact() {
        Artifacts artifacts = new Artifacts();
        artifacts.setUri("test://testUri");
        return artifacts;
    }

    private OnapAuxilary getOnapAuxilary() {
        OnapAuxilary onapAuxilary = new OnapAuxilary();
        onapAuxilary.setLog_info(Map.of());
        onapAuxilary.setTls_info(Map.of());
        onapAuxilary.setPorts(List.of("testPorts"));
        onapAuxilary.setVolumes(new Volumes[]{getVolumes()});
        return onapAuxilary;
    }

    private Volumes getVolumes() {
        return new Volumes();
    }

}
