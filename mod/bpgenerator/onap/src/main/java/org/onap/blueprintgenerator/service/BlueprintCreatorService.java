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

import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.dmaap.DmaapBlueprintCreatorService;
import org.onap.blueprintgenerator.service.onap.OnapBlueprintCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : Remigiusz Janeczek
 * @date 12/17/2020 Application: ONAP - Blueprint Generator Service to create ONAP and DMAAP Blueprints
 */
@Service
public class BlueprintCreatorService {

    private final OnapBlueprintCreatorService onapBlueprintCreatorService;

    private final DmaapBlueprintCreatorService dmaapBlueprintCreatorService;

    @Autowired
    public BlueprintCreatorService(OnapBlueprintCreatorService onapBlueprintCreatorService,
        DmaapBlueprintCreatorService dmaapBlueprintCreatorService) {
        this.onapBlueprintCreatorService = onapBlueprintCreatorService;
        this.dmaapBlueprintCreatorService = dmaapBlueprintCreatorService;
    }

    /**
     * Creates Blueprint from given OnapComponentSpec and Input objects if the input is json file or not
     *
     * @param componentSpec OnapComponentSpec object
     * @param input         Input object, needs to have bpType defined (either as "o" (ONAP Blueprint) or "d" (DMAAP
     *                      Blueprint)
     * @return blueprint generated from componentSpec and input
     */
    public Blueprint createBlueprint(OnapComponentSpec componentSpec, Input input) {
        if (input.getBpType() == null) {
            return null;
        }
        Blueprint blueprint = null;
        if (input.getBpType().equals("o")) {
            blueprint = onapBlueprintCreatorService.createBlueprint(componentSpec, input);
        } else if (input.getBpType().equals("d")) {
            blueprint = dmaapBlueprintCreatorService.createBlueprint(componentSpec, input);
        }
        return blueprint;
    }

}
