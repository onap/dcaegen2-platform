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
import org.onap.blueprintgenerator.model.common.ExternalTlsInfo;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to add External TLS Info
 */


@Service
public class ExternalTlsInfoFactoryService extends ExternalCertificateDataFactoryService {

    @Autowired
    private ExternalCertificateParametersFactoryService externalCertificateParametersFactoryService;

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    //Method to create External TLS Info from Component Spec
    public ExternalTlsInfo createFromComponentSpec(OnapComponentSpec cs) {
        ExternalTlsInfo externalTlsInfoBp = new ExternalTlsInfo();
        Map<String, Object> tlsInfoCs = cs.getAuxilary().getTls_info();

        externalTlsInfoBp.setExternalCertDirectory((String) tlsInfoCs.get(Constants.CERT_DIRECTORY_FIELD));
        externalTlsInfoBp.setUseExternalTls(createPrefixedGetInput(Constants.USE_EXTERNAL_TLS_FIELD));
        externalTlsInfoBp.setCaName(createPrefixedGetInput(Constants.CA_NAME_FIELD));
        externalTlsInfoBp.setCertType(createPrefixedGetInput(Constants.CERT_TYPE_FIELD));
        externalTlsInfoBp.setExternalCertificateParameters(externalCertificateParametersFactoryService.create());

        return externalTlsInfoBp;
    }

    //Method to create Input List for External TLS Info from Component Spec
    public Map<String, LinkedHashMap<String, Object>> createInputListFromComponentSpec(OnapComponentSpec cs) {

        Map<String, LinkedHashMap<String, Object>> retInputs = new HashMap<>();

        Map<String, Object> externalTlsInfoCs = cs.getAuxilary().getTls_info();
        LinkedHashMap<String, Object> useTlsFlagInput = blueprintHelperService.createBooleanInput("Flag to indicate external tls enable/disable.",externalTlsInfoCs.get(Constants.USE_EXTERNAL_TLS_FIELD));
        retInputs.put(addPrefix(Constants.USE_EXTERNAL_TLS_FIELD), useTlsFlagInput);

        LinkedHashMap<String, Object> caNameInputMap = blueprintHelperService.createStringInput("Name of Certificate Authority configured on CertService side.",Constants.DEFAULT_CA);
        retInputs.put(addPrefix(Constants.CA_NAME_FIELD), caNameInputMap);

        LinkedHashMap<String, Object> certTypeInputMap = blueprintHelperService.createStringInput("Format of provided certificates",Constants.DEFAULT_CERT_TYPE);
        retInputs.put(addPrefix(Constants.CERT_TYPE_FIELD), certTypeInputMap);

        retInputs.putAll(externalCertificateParametersFactoryService.createInputList());
        return retInputs;
    }

}
