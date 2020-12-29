/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.onap.dcae.runtime.core;

import org.junit.Test;
import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;

public class TestOnapBpGen extends BpGenTestBase {

    @Test
    public void testOnapBPGeneration() throws Exception {
        OnapComponentSpec componentSpec = componentSpecService.createComponentSpecFromString(
            Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world_only_MR.json"));
        Input input = new Input();
        input.setBpType("d");
        Blueprint blueprint = blueprintCreatorService.createBlueprint(componentSpec, input);
        System.out.println(blueprint.getInputs());
    }
}
