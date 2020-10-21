/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
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

package org.onap.blueprintgenerator.service.common;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.ExternalCertificateParameters;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to add External Certificate Parameters
 */


@Service
public class ExternalCertificateParametersFactoryService extends ExternalCertificateDataFactoryService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    // method to create external certificate parameters
    public ExternalCertificateParameters create() {
        ExternalCertificateParameters externalCertificateParameters = new ExternalCertificateParameters();
        externalCertificateParameters.setCommonName(createPrefixedGetInput(Constants.COMMON_NAME_FIELD));
        externalCertificateParameters.setSans(createPrefixedGetInput(Constants.SANS_FIELD));
        return externalCertificateParameters;
    }

    // method to create input list for external certificate parameters factory
    public Map<String, LinkedHashMap<String, Object>> createInputList() {
        Map<String, LinkedHashMap<String, Object>> retInputs = new LinkedHashMap<>();

        LinkedHashMap<String, Object> commonNameInputMap = blueprintHelperService.createStringInput("Common name which should be present in certificate.",Constants.DEFAULT_COMMON_NAME);
        retInputs.put(addPrefix(Constants.COMMON_NAME_FIELD), commonNameInputMap);

        LinkedHashMap<String, Object> sansInputMap = blueprintHelperService.createStringInput("\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
                "Delimiter - : Should contain a common_name value and other FQDNs under which the given " +"component is accessible.\"",Constants.DEFAULT_SANS);
        retInputs.put(addPrefix(Constants.SANS_FIELD), sansInputMap);

        return retInputs;
    }

}
