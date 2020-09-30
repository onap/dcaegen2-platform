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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.CONTENERIZED_SERVICE_COMPONENT_USING_DMAAP;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.FEED;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.SUBSCRIBE_TO_EVENTS;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.SUBSCRIBE_TO_FILES;
import static org.onap.blueprintgenerator.models.blueprint.BpConstants.TOPIC;

import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.blueprintgenerator.core.TestComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Auxilary;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Streams;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;

public class DmaapNodeTest {

    private static final String DATA_ROUTER_TYPE = "data_router";
    private static final String MESSAGE_ROUTER_TYPE = "message_router";

    private static final String CONFIG_KEY = "Configkey";

    private static final String SAMPLE_FORMAT = "Format";
    private static final String SAMPLE_VERSION = "1.0.0";
    private static final String SAMPLE_ROUTE = "SampleRoute";
    private static final String TYPE = "type";
    private static final String TARGET = "target";


    @Test
    public void dmaapNodeShouldHaveExpectedNodeType() {

        ComponentSpec mockedComponentSpec = getSpiedComponentSpecWithoutRelationships();

        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createDmaapNode(mockedComponentSpec, new TreeMap<>(), "");

        assertEquals(CONTENERIZED_SERVICE_COMPONENT_USING_DMAAP, dmaapNode.getType());
    }

    @Test
    public void createdDmaapNodeShouldHaveRelationshipWithTypeAndTargetForMessageRouterPublishes() {
        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        Streams streams = new Streams();
        streams.setSubscribes(new Subscribes[0]);
        streams.setPublishes(createSamplePublishes(MESSAGE_ROUTER_TYPE));

        when(componentSpec.getStreams()).thenReturn(streams);
        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createDmaapNode(componentSpec, new TreeMap<>(), "");

        Map<String, String> relationship = dmaapNode.getRelationships().get(0);

        assertNotNull(relationship.get(TYPE));
        assertNotNull(relationship.get(TARGET));
    }

    @Test
    public void createdDmaapNodeShouldHaveRelationshipWithTypeAndTargetForDataRouterPublishes() {
        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        Streams streams = new Streams();
        streams.setSubscribes(new Subscribes[0]);
        streams.setPublishes(createSamplePublishes(DATA_ROUTER_TYPE));

        when(componentSpec.getStreams()).thenReturn(streams);
        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createDmaapNode(componentSpec, new TreeMap<>(), "");

        Map<String, String> relationship = dmaapNode.getRelationships().get(0);

        assertNotNull(relationship.get(TYPE));
        assertNotNull(relationship.get(TARGET));
    }

    @Test
    public void createdDmaapNodeShouldHaveRelationshipWithTypeAndTargetForMessageRouterSubscribes() {
        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        Streams streams = new Streams();
        streams.setSubscribes(createSampleSubscribes(MESSAGE_ROUTER_TYPE));
        streams.setPublishes(new Publishes[0]);

        when(componentSpec.getStreams()).thenReturn(streams);
        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createDmaapNode(componentSpec, new TreeMap<>(), "");

        Map<String, String> relationship = dmaapNode.getRelationships().get(0);

        assertEquals(SUBSCRIBE_TO_EVENTS, relationship.get(TYPE));
        assertNotNull(relationship.get(TARGET));
    }

    @Test
    public void createdDmaapNodeShouldHaveRelationshipWithTypeAndTargetForDataRouterSubscribes() {
        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        Streams streams = new Streams();
        streams.setSubscribes(createSampleSubscribes(DATA_ROUTER_TYPE));
        streams.setPublishes(new Publishes[0]);

        when(componentSpec.getStreams()).thenReturn(streams);
        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createDmaapNode(componentSpec, new TreeMap<>(), "");

        Map<String, String> relationship = dmaapNode.getRelationships().get(0);

        assertEquals(SUBSCRIBE_TO_FILES, relationship.get(TYPE));
        assertNotNull(relationship.get(TARGET));
    }

    @Test
    public void createFeedNodeShouldSetFeedNodeType() {
        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createFeedNode(componentSpec, new TreeMap<>(), "");

        assertEquals(FEED, dmaapNode.getType());
    }

    @Test
    public void createTopicNodeShouldSetTopicNodeType() {

        ComponentSpec componentSpec = getSpiedComponentSpecWithoutRelationships();

        DmaapNode dmaapNode = new DmaapNode();
        dmaapNode.createTopicNode(componentSpec, new TreeMap<>(), "");

        assertEquals(TOPIC, dmaapNode.getType());
    }

    private Publishes[] createSamplePublishes(String type) {
        Publishes publishes = new Publishes();

        publishes.setType(type);
        publishes.setConfig_key(CONFIG_KEY);
        publishes.setFormat(SAMPLE_FORMAT);
        publishes.setVersion(SAMPLE_VERSION);
        publishes.setRoute(SAMPLE_ROUTE);

        return new Publishes[]{publishes};
    }

    private Subscribes[] createSampleSubscribes(String type) {
        Subscribes subscribes = new Subscribes();

        subscribes.setType(type);
        subscribes.setConfig_key(CONFIG_KEY);
        subscribes.setFormat(SAMPLE_FORMAT);
        subscribes.setVersion(SAMPLE_VERSION);
        subscribes.setRoute(SAMPLE_ROUTE);

        return new Subscribes[]{subscribes};
    }

    private ComponentSpec getSpiedComponentSpecWithoutRelationships() {
        ComponentSpec baseComponentSpec = new ComponentSpec();
        baseComponentSpec.createComponentSpecFromString(new TestComponentSpec().getComponentSpecAsString());
        ComponentSpec componentSpec = spy(baseComponentSpec);

        Streams streams = new Streams();
        streams.setSubscribes(new Subscribes[0]);
        streams.setPublishes(new Publishes[0]);
        when(componentSpec.getStreams()).thenReturn(streams);

        Auxilary auxilary = spy(baseComponentSpec.getAuxilary());
        when(auxilary.getDatabases()).thenReturn(null);

        when(componentSpec.getAuxilary()).thenReturn(auxilary);
        when(componentSpec.getPolicyInfo()).thenReturn(null);

        return componentSpec;
    }
}
