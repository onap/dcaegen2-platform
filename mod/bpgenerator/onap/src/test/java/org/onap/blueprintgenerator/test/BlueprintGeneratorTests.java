/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2020  Nokia. All rights reserved.
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

package org.onap.blueprintgenerator.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.onap.blueprintgenerator.BlueprintGeneratorConfiguration;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.BlueprintCreatorService;
import org.onap.blueprintgenerator.service.base.BlueprintService;
import org.onap.blueprintgenerator.service.common.ComponentSpecService;
import org.onap.blueprintgenerator.service.dmaap.DmaapBlueprintCreatorService;
import org.onap.blueprintgenerator.service.onap.OnapBlueprintCreatorService;
import org.onap.policycreate.service.PolicyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator ONAP Test Cases
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BlueprintGeneratorConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(
    properties = {
        "ves=ves.json",
        "testImports=testImports.yaml",
        "useTlsTrueAndUseExternalTlsTrueTest=testComponentSpec_withTlsTrueAndExternalTlsTrue.json",
        "useTlsFalseAndUseExternalTlsFalseTest=testComponentSpec_withTlsFalseAndExternalTlsFalse.json",
        "useTlsTrueAndNoExternalTlsFlagTest=testComponentSpec_withTlsTrueAndNoExternalTls.json",
        "noTlsInfo=testComponentSpec_withoutTlsInfo.json"
    }
)
@Ignore
public class BlueprintGeneratorTests {

    @Qualifier("objectMapper")
    @Autowired
    protected ObjectMapper objectMapper;

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    @Value("${ves}")
    protected String ves;

    @Value("${testImports}")
    protected String testImports;

    @Value("${useTlsTrueAndUseExternalTlsTrueTest}")
    protected String useTlsTrueAndUseExternalTlsTrueTest;

    @Value("${useTlsFalseAndUseExternalTlsFalseTest}")
    protected String useTlsFalseAndUseExternalTlsFalseTest;

    @Value("${useTlsTrueAndNoExternalTlsFlagTest}")
    protected String useTlsTrueAndNoExternalTlsFlagTest;

    @Value("${noTlsInfo}")
    protected String noTlsInfo;

    @Autowired
    protected ComponentSpecService onapComponentSpecService;

    @Autowired
    protected DmaapBlueprintCreatorService dmaapBlueprintCreatorService;

    @Autowired
    protected OnapBlueprintCreatorService onapBlueprintCreatorService;

    @Autowired
    protected BlueprintCreatorService blueprintCreatorService;

    @Autowired
    protected BlueprintService blueprintService;

    @Autowired
    protected PolicyModelService policyModelService;

    @Autowired
    protected OnapTestUtils onapTestUtils;

    protected OnapComponentSpec onapComponentSpec = null;

}
