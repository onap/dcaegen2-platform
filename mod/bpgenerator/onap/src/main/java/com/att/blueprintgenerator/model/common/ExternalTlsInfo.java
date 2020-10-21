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

package com.att.blueprintgenerator.model.common;

import com.att.blueprintgenerator.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * ONAP Common Model used by both ONAP and DMAAP: External TLS Info
 */


@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalTlsInfo {

    @JsonProperty(Constants.EXTERNAL_CERT_DIRECTORY_FIELD)
    private String externalCertDirectory;

    @JsonProperty(Constants.USE_EXTERNAL_TLS_FIELD)
    private GetInput useExternalTls;

    @JsonProperty(Constants.CA_NAME_FIELD)
    private GetInput caName;

    @JsonProperty(Constants.CERT_TYPE_FIELD)
    private GetInput certType;

    @JsonProperty(Constants.EXTERNAL_CERTIFICATE_PARAMETERS_FIELD)
    private ExternalCertificateParameters externalCertificateParameters;

}
