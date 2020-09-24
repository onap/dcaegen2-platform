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

package org.onap.blueprintgenerator.models.blueprint.tls;

import org.junit.Test;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalCertificateParameters;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.COMMON_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_COMMON_NAME;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_SANS;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.INPUT_PREFIX;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.SANS_FIELD;

public class ExternalCertificateParametersFactoryTest {

    private static final String PREFIXED_COMMON_NAME_FIELD = INPUT_PREFIX + COMMON_NAME_FIELD;
    private static final String PREFIXED_SANS_FIELD = INPUT_PREFIX + SANS_FIELD;
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
        // when
        Map<String, LinkedHashMap<String, Object>> result = cut.createInputList();
        // then
        assertEquals(DEFAULT_COMMON_NAME, result.get(PREFIXED_COMMON_NAME_FIELD).get(DEFAULT));
        assertEquals(DEFAULT_SANS, result.get(PREFIXED_SANS_FIELD).get(DEFAULT));
    }
}
