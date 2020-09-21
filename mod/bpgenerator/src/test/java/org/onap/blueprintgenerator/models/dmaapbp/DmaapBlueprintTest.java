/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia. All rights reserved.
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


package org.onap.blueprintgenerator.models.dmaapbp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.componentspec.Artifacts;
import org.onap.blueprintgenerator.models.componentspec.Auxilary;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Parameters;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Self;
import org.onap.blueprintgenerator.models.componentspec.Streams;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;

public class DmaapBlueprintTest {

    private static final String MOCKED_NAME = "sample.name";
    private static final String MESSAGE_ROUTER_TYPE_1 = "message_router";
    private static final String MESSAGE_ROUTER_TYPE_2 = "message router";
    private static final String DATA_ROUTER_TYPE_1 = "data_router";
    private static final String DATA_ROUTER_TYPE_2 = "data router";
    private static final String CONFIG_KEY_1 = "Configkey1";
    private static final String CONFIG_KEY_2 = "Configkey2";

    private static final String TOPIC_NODE_1 = CONFIG_KEY_1 + "_topic";
    private static final String TOPIC_NODE_2 = CONFIG_KEY_2 + "_topic";
    private static final String FEED_NODE_1 = CONFIG_KEY_1 + "_feed";
    private static final String FEED_NODE_2 = CONFIG_KEY_2 + "_feed";

    private static final String SAMPLE_FORMAT = "Format";
    private static final String SAMPLE_VERSION = "1.0.0";
    private static final String SAMPLE_ROUTE = "SampleRoute";
    private static final String SAMPLE_DESCRIPTION = "sample description";
    private static final String SAMPLE_PORTS = "8080:8080";
    private static final String SAMPLE_ARTIFACT_TYPE = "test";
    private static final String SAMPLE_ARTIFACT_URI = "test_uri";

    @Test
    public void dmaapBlueprintShouldHasNodeTemplateWithDmaapNode() {

        //given
        ComponentSpec componentSpec = getMockedComponentSpec();
        DmaapBlueprint dmaapBlueprint = new DmaapBlueprint();

        //when
        Blueprint resultBlueprint = dmaapBlueprint.createDmaapBlueprint(componentSpec, "", "");

        //then
        assertTrue(resultBlueprint.getNode_templates().get(MOCKED_NAME) instanceof DmaapNode);
    }

    @Test
    public void nodeTemplateHasTopicNodeWhenAddMessageRouterAsPublishes() {
        //given
        ComponentSpec componentSpec = getMockedComponentSpec();

        Streams streams = mock(Streams.class);
        List<Publishes> publishesList = getMessageRouterPublishes();
        when(streams.getPublishes()).thenReturn(publishesList.toArray(new Publishes[0]));
        when(streams.getSubscribes()).thenReturn(new Subscribes[0]);
        when(componentSpec.getStreams()).thenReturn(streams);

        DmaapBlueprint dmaapBlueprint = new DmaapBlueprint();

        //when
        Blueprint resultBlueprint = dmaapBlueprint.createDmaapBlueprint(componentSpec, "", "");

        //then
        assertNotNull(resultBlueprint.getNode_templates().get(TOPIC_NODE_1));
        assertNotNull(resultBlueprint.getNode_templates().get(TOPIC_NODE_2));

    }

    @Test
    public void nodeTemplateHasTopicNodeWhenAddMessageRouterAsSubscribes() {
        //given
        ComponentSpec componentSpec = getMockedComponentSpec();

        Streams streams = mock(Streams.class);
        List<Subscribes> subscribesList = getMessageRouterSubscribes();
        when(streams.getPublishes()).thenReturn(new Publishes[0]);
        when(streams.getSubscribes()).thenReturn(subscribesList.toArray(new Subscribes[0]));
        when(componentSpec.getStreams()).thenReturn(streams);

        DmaapBlueprint dmaapBlueprint = new DmaapBlueprint();

        //when
        Blueprint resultBlueprint = dmaapBlueprint.createDmaapBlueprint(componentSpec, "", "");

        //then
        assertNotNull(resultBlueprint.getNode_templates().get(TOPIC_NODE_1));
        assertNotNull(resultBlueprint.getNode_templates().get(TOPIC_NODE_2));

    }

    @Test
    public void nodeTemplateHasFeedNodeWhenAddDataRouterAsPublishes() {
        //given
        ComponentSpec componentSpec = getMockedComponentSpec();
        Streams streams = mock(Streams.class);
        List<Publishes> publishesList = getDataRouterPublishes();

        when(streams.getPublishes()).thenReturn(publishesList.toArray(new Publishes[0]));
        when(streams.getSubscribes()).thenReturn(new Subscribes[0]);
        when(componentSpec.getStreams()).thenReturn(streams);

        DmaapBlueprint dmaapBlueprint = new DmaapBlueprint();

        //when
        Blueprint resultBlueprint = dmaapBlueprint.createDmaapBlueprint(componentSpec, "", "");

        //then
        assertNotNull(resultBlueprint.getNode_templates().get(FEED_NODE_1));
        assertNotNull(resultBlueprint.getNode_templates().get(FEED_NODE_2));

    }

    @Test
    public void nodeTemplateHasFeedNodeWhenAddDataRouterAsSubscribes() {
        //given
        ComponentSpec componentSpec = getMockedComponentSpec();
        Streams streams = mock(Streams.class);
        List<Subscribes> subscribesList = getDataRouterSubscribes();

        when(streams.getPublishes()).thenReturn(new Publishes[0]);
        when(streams.getSubscribes()).thenReturn(subscribesList.toArray(new Subscribes[0]));
        when(componentSpec.getStreams()).thenReturn(streams);

        DmaapBlueprint dmaapBlueprint = new DmaapBlueprint();

        //when
        Blueprint resultBlueprint = dmaapBlueprint.createDmaapBlueprint(componentSpec, "", "");

        //then
        assertNotNull(resultBlueprint.getNode_templates().get(FEED_NODE_1));
        assertNotNull(resultBlueprint.getNode_templates().get(FEED_NODE_2));

    }

    private List<Publishes> getMessageRouterPublishes() {
        List<Publishes> publishesList = new ArrayList<>();

        publishesList.add(createSamplePublishes(MESSAGE_ROUTER_TYPE_1, CONFIG_KEY_1));
        publishesList.add(createSamplePublishes(MESSAGE_ROUTER_TYPE_2, CONFIG_KEY_2));
        return publishesList;
    }

    private List<Subscribes> getMessageRouterSubscribes() {
        List<Subscribes> subscribesList = new ArrayList<>();

        subscribesList.add(createSampleSubscribes(MESSAGE_ROUTER_TYPE_1, CONFIG_KEY_1));
        subscribesList.add(createSampleSubscribes(MESSAGE_ROUTER_TYPE_2, CONFIG_KEY_2));
        return subscribesList;
    }


    private List<Publishes> getDataRouterPublishes() {
        List<Publishes> publishesList = new ArrayList<>();

        publishesList.add(createSamplePublishes(DATA_ROUTER_TYPE_1, CONFIG_KEY_1));
        publishesList.add(createSamplePublishes(DATA_ROUTER_TYPE_2, CONFIG_KEY_2));
        return publishesList;
    }

    private List<Subscribes> getDataRouterSubscribes() {
        List<Subscribes> subscribesList = new ArrayList<>();

        subscribesList.add(createSampleSubscribes(DATA_ROUTER_TYPE_1, CONFIG_KEY_1));
        subscribesList.add(createSampleSubscribes(DATA_ROUTER_TYPE_2, CONFIG_KEY_2));
        return subscribesList;
    }

    private Publishes createSamplePublishes(String type, String key) {
        Publishes publishes = new Publishes();

        publishes.setType(type);
        publishes.setConfig_key(key);
        publishes.setFormat(SAMPLE_FORMAT);
        publishes.setVersion(SAMPLE_VERSION);
        publishes.setRoute(SAMPLE_ROUTE);

        return publishes;
    }

    private Subscribes createSampleSubscribes(String type, String key) {
        Subscribes subscribes = new Subscribes();

        subscribes.setType(type);
        subscribes.setConfig_key(key);
        subscribes.setFormat(SAMPLE_FORMAT);
        subscribes.setVersion(SAMPLE_FORMAT);
        subscribes.setRoute(SAMPLE_ROUTE);

        return subscribes;
    }

    private ComponentSpec getMockedComponentSpec() {
        Self self = mock(Self.class);
        when(self.getDescription()).thenReturn(SAMPLE_DESCRIPTION);
        when(self.getName()).thenReturn(MOCKED_NAME);

        Auxilary auxilary = mock(Auxilary.class);
        ArrayList<Object> ports = new ArrayList<>();
        ports.add(SAMPLE_PORTS);
        when(auxilary.getPorts()).thenReturn(ports);

        Streams streams = mock(Streams.class);
        when(streams.getPublishes()).thenReturn(new Publishes[0]);
        when(streams.getSubscribes()).thenReturn(new Subscribes[0]);

        Artifacts artifact = new Artifacts();
        artifact.setType(SAMPLE_ARTIFACT_TYPE);
        artifact.setUri(SAMPLE_ARTIFACT_URI);

        Artifacts[] arrayArtifacts = new Artifacts[10];
        arrayArtifacts[0] = artifact;

        ComponentSpec componentSpec = mock(ComponentSpec.class);
        when(componentSpec.getSelf()).thenReturn(self);
        when(componentSpec.getAuxilary()).thenReturn(auxilary);
        when(componentSpec.getStreams()).thenReturn(streams);
        when(componentSpec.getArtifacts()).thenReturn(arrayArtifacts);
        when(componentSpec.getParameters()).thenReturn(new Parameters[0]);
        return componentSpec;
    }
}
