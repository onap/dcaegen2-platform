/**============LICENSE_START=======================================================
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

package org.onap.blueprintgenerator.models.blueprint.tls;

import org.junit.Test;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalCertificateParameters;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.COMMON_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.INPUT_PREFIX;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.SANS_FIELD;

public class ExternalCertificateParametersFactoryTest {

    @Test
    public void shouldCreateExternalCertificatePropertiesObject() {
        // given
        ExternalCertificateParametersFactory cut = new ExternalCertificateParametersFactory();
        // when
        ExternalCertificateParameters result = cut.create();
        // then
        assertEquals(result.getCommonName().getBpFieldName(), INPUT_PREFIX + COMMON_NAME_FIELD);
        assertEquals(result.getSans().getBpFieldName(), INPUT_PREFIX + SANS_FIELD);
    }

    @Test
    public void shouldCreateCorrectInputList() {
        // given
        ExternalCertificateParametersFactory cut = new ExternalCertificateParametersFactory();
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec_withExternalTlsInfo_simple.json");
        // when
        Map<String, LinkedHashMap<String, Object>> result = cut.createInputListFromComponentSpec(cs);
        // then
        assertTrue(result.containsKey(INPUT_PREFIX + COMMON_NAME_FIELD));
        assertTrue(result.containsKey(INPUT_PREFIX + SANS_FIELD));

    }
}