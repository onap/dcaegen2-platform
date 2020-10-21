/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia Intellectual Property. All rights reserved.
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

package com.att.blueprintgenerator.test;

import com.att.blueprintgenerator.model.common.ExternalCertificateParameters;
import com.att.blueprintgenerator.service.common.ExternalCertificateParametersFactoryService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.LinkedHashMap;
import java.util.Map;

import static com.att.blueprintgenerator.constants.Constants.*;
import static org.junit.Assert.*;

public class ExternalCertificateParametersFactoryServiceTest extends BlueprintGeneratorTests{

    private static final String PREFIXED_COMMON_NAME_FIELD = INPUT_PREFIX + COMMON_NAME_FIELD;
    private static final String PREFIXED_SANS_FIELD = INPUT_PREFIX + SANS_FIELD;
    private static final String DEFAULT = "default";

    @Autowired
    private ExternalCertificateParametersFactoryService externalCertificateParametersFactoryService;

    @Test
    public void shouldCreateExternalCertificatePropertiesObject() {

        ExternalCertificateParameters result = externalCertificateParametersFactoryService.create();
        assertEquals(result.getCommonName().getBpInputName(), PREFIXED_COMMON_NAME_FIELD);
        assertEquals(result.getSans().getBpInputName(), PREFIXED_SANS_FIELD);
    }

    @Test
    public void shouldCreateCorrectInputListWithDefaultValuesTakenFromComponentSpec() {

        Map<String, LinkedHashMap<String, Object>> result = externalCertificateParametersFactoryService.createInputList();
        assertEquals(DEFAULT_COMMON_NAME, result.get(PREFIXED_COMMON_NAME_FIELD).get(DEFAULT));
        assertEquals(DEFAULT_SANS, result.get(PREFIXED_SANS_FIELD).get(DEFAULT));
    }
}
