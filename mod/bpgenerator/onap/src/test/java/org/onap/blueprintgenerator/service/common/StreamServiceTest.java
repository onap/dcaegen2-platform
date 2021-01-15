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
