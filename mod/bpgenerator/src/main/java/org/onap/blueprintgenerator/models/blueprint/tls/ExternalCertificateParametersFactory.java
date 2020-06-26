/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia. All rights reserved.
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

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createInputValue;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.COMMON_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_COMMON_NAME;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_SANS;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.SANS_FIELD;

import java.util.LinkedHashMap;
import java.util.Map;
import org.onap.blueprintgenerator.models.blueprint.tls.api.ExternalCertificateDataFactory;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalCertificateParameters;

/**
 * Factory class for providing parameters of ExternalCertificate. Allow to get ExternalCertificateParameters Object and
 * input list
 */
public class ExternalCertificateParametersFactory extends ExternalCertificateDataFactory {

    /**
     * Create ExternalCertificateParameters Object
     *
     * @return ExternalCertificateParameters
     */
    public ExternalCertificateParameters create() {
        ExternalCertificateParameters externalCertificateParameters = new ExternalCertificateParameters();
        externalCertificateParameters.setCommonName(createPrefixedGetInput(COMMON_NAME_FIELD));
        externalCertificateParameters.setSans(createPrefixedGetInput(SANS_FIELD));
        return externalCertificateParameters;
    }

    /**
     * Create input list for ExternalCertificateParameters
     *
     * @return Input list
     */
    public Map<String, LinkedHashMap<String, Object>> createInputList() {
        Map<String, LinkedHashMap<String, Object>> retInputs = new LinkedHashMap<>();

        LinkedHashMap<String, Object> commonNameInputMap = createInputValue("string",
            "Common name which should be present in certificate.",
            DEFAULT_COMMON_NAME);
        retInputs.put(addPrefix(COMMON_NAME_FIELD), commonNameInputMap);

        LinkedHashMap<String, Object> sansInputMap = createInputValue("string",
            "\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
                "Delimiter - : Should contain common_name value and other FQDNs under which given " +
                "component is accessible.\"",
            DEFAULT_SANS);
        retInputs.put(addPrefix(SANS_FIELD), sansInputMap);

        return retInputs;
    }

}
