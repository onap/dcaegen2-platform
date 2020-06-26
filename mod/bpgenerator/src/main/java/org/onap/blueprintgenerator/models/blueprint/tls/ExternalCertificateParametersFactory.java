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

import org.onap.blueprintgenerator.models.blueprint.tls.api.ExternalCertificateDataFactory;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalCertificateParameters;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.onap.blueprintgenerator.common.blueprint.Manipulation.createInputValue;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.COMMON_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.EXTERNAL_CERTIFICATE_PARAMETERS_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.SANS_FIELD;

public class ExternalCertificateParametersFactory extends ExternalCertificateDataFactory<ExternalCertificateParameters> {

    public ExternalCertificateParameters create() {
        ExternalCertificateParameters externalCertificateParameters = new ExternalCertificateParameters();
        externalCertificateParameters.setCommonName(createPrefixedGetInput(COMMON_NAME_FIELD));
        externalCertificateParameters.setSans(createPrefixedGetInput(SANS_FIELD));
        return externalCertificateParameters;
    }

    public Map<String, LinkedHashMap<String, Object>> createInputListFromComponentSpec(ComponentSpec cs) {
        Map<String, LinkedHashMap<String, Object>> retInputs = new LinkedHashMap<>();
        Map<String, Object> externalTlsCertParams =
                (Map<String, Object>) cs.getAuxilary()
                        .getExternal_tls_info().get(EXTERNAL_CERTIFICATE_PARAMETERS_FIELD);

        LinkedHashMap<String, Object> commonNameInputMap = createInputValue("string",
                "Common name which should be present in certificate.",
                externalTlsCertParams.get(COMMON_NAME_FIELD));
        retInputs.put(addPrefix(COMMON_NAME_FIELD), commonNameInputMap);

        LinkedHashMap<String, Object> sansInputMap = createInputValue("string",
                "\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
                        "Delimiter - : Should contain common_name value and other FQDNs under which given " +
                        "component is accessible.\"",
                externalTlsCertParams.get(SANS_FIELD));
        retInputs.put(addPrefix(SANS_FIELD), sansInputMap);
        return retInputs;
    }

}
