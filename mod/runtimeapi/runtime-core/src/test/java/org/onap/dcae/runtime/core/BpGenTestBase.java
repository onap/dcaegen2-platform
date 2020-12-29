/*-
 * ============LICENSE_START=======================================================
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

import org.junit.runner.RunWith;
import org.onap.blueprintgenerator.BlueprintGeneratorConfiguration;
import org.onap.blueprintgenerator.service.BlueprintCreatorService;
import org.onap.blueprintgenerator.service.base.BlueprintService;
import org.onap.blueprintgenerator.service.base.FixesService;
import org.onap.blueprintgenerator.service.common.ComponentSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@SpringBootTest(classes = {BlueprintGeneratorConfiguration.class})
public class BpGenTestBase {
    @Autowired
    BlueprintService blueprintService;
    @Autowired
    ComponentSpecService componentSpecService;
    @Autowired
    BlueprintCreatorService blueprintCreatorService;
    @Autowired
    FixesService fixesService;
}
