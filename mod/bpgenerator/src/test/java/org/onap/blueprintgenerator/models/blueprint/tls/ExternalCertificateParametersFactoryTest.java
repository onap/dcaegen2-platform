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

    private static final String PREFIXED_COMMON_NAME_FIELD = INPUT_PREFIX + COMMON_NAME_FIELD;
    private static final String PREFIXED_SANS_FIELD = INPUT_PREFIX + SANS_FIELD;
    private static final String EXPECTED_COMMON_VALUE = "simpledemo.onap.org";
    private static final String EXPECTED_SANS_VALUE = "simpledemo.onap.org;ves.simpledemo.onap.org;ves.onap.org";
    private static final String DEFAULT = "default";

    @Test
    public void shouldCreateExternalCertificatePropertiesObject() {
        // given
        ExternalCertificateParametersFactory cut = new ExternalCertificateParametersFactory();
        // when
        ExternalCertificateParameters result = cut.create();
        // then
        assertEquals(result.getCommonName().getBpInputName(), PREFIXED_COMMON_NAME_FIELD);
        assertEquals(result.getSans().getBpInputName(), PREFIXED_SANS_FIELD);
    }

    @Test
    public void shouldCreateCorrectInputListWithDefaultValuesTakenFromComponentSpec() {
        // given
        ExternalCertificateParametersFactory cut = new ExternalCertificateParametersFactory();
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec_withExternalTlsInfo_simple.json");
        // when
        Map<String, LinkedHashMap<String, Object>> result = cut.createInputListFromComponentSpec(cs);
        // then
        assertEquals(EXPECTED_COMMON_VALUE, result.get(PREFIXED_COMMON_NAME_FIELD).get(DEFAULT));
        assertEquals(EXPECTED_SANS_VALUE, result.get(PREFIXED_SANS_FIELD).get(DEFAULT));
    }
}