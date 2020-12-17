/*
 * ============LICENSE_START=======================================================
 * org.onap.dcae
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.common.OnapBlueprint;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.dmaap.DmaapBlueprintCreatorService;
import org.onap.blueprintgenerator.service.onap.OnapBlueprintCreatorService;

public class BlueprintCreatorServiceTest {

    private static final String ONAP_BP_TYPE = "o";
    private static final String DMAAP_BP_TYPE = "d";

    private OnapBlueprintCreatorService onapBlueprintCreatorService;
    private DmaapBlueprintCreatorService dmaapBlueprintCreatorService;
    private BlueprintCreatorService blueprintCreatorService;

    @Before
    public void setup() {
        onapBlueprintCreatorService = mock(OnapBlueprintCreatorService.class);
        dmaapBlueprintCreatorService = mock(DmaapBlueprintCreatorService.class);
        blueprintCreatorService = new BlueprintCreatorService(onapBlueprintCreatorService,
            dmaapBlueprintCreatorService);
    }

    @Test
    public void shouldCreateOnapBlueprint() {
        OnapComponentSpec cs = new OnapComponentSpec();
        Input input = new Input();
        input.setBpType(ONAP_BP_TYPE);
        when(onapBlueprintCreatorService.createBlueprint(any(), any())).thenReturn(new OnapBlueprint());

        Blueprint blueprint = blueprintCreatorService.createBlueprint(cs, input);

        verify(onapBlueprintCreatorService, times(1)).createBlueprint(cs, input);
        verify(onapBlueprintCreatorService, times(1)).createBlueprint(cs, input);
        verifyNoInteractions(dmaapBlueprintCreatorService);
        assertNotNull(blueprint);
    }

    @Test
    public void shouldCreateDmaapBlueprint() {
        OnapComponentSpec cs = new OnapComponentSpec();
        Input input = new Input();
        input.setBpType(DMAAP_BP_TYPE);
        when(dmaapBlueprintCreatorService.createBlueprint(any(), any())).thenReturn(new OnapBlueprint());

        Blueprint blueprint = blueprintCreatorService.createBlueprint(cs, input);

        verify(dmaapBlueprintCreatorService, times(1)).createBlueprint(cs, input);
        verify(dmaapBlueprintCreatorService, times(1)).createBlueprint(cs, input);
        verifyNoInteractions(onapBlueprintCreatorService);
        assertNotNull(blueprint);
    }

    @Test
    public void shouldReturnNullWhenBpTypeNotDefined(){
        OnapComponentSpec cs = new OnapComponentSpec();
        Input input = new Input();

        Blueprint blueprint = blueprintCreatorService.createBlueprint(cs, input);

        verifyNoInteractions(onapBlueprintCreatorService);
        verifyNoInteractions(dmaapBlueprintCreatorService);
        assertNull(blueprint);
    }

    @Test
    public void shouldReturnNullWhenBpTypeIncorrect(){
        OnapComponentSpec cs = new OnapComponentSpec();
        Input input = new Input();
        input.setBpType("z");

        Blueprint blueprint = blueprintCreatorService.createBlueprint(cs, input);

        verifyNoInteractions(onapBlueprintCreatorService);
        verifyNoInteractions(dmaapBlueprintCreatorService);
        assertNull(blueprint);
    }

}
