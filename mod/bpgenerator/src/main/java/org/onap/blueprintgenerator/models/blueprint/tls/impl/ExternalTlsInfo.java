/**============LICENSE_START=======================================================
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

package org.onap.blueprintgenerator.models.blueprint.tls.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.blueprint.tls.api.ExternalCertificateData;

import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.CA_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.EXTERNAL_CERTIFICATE_PARAMETERS_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.EXTERNAL_CERT_DIRECTORY_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.USE_EXTERNAL_TLS_FIELD;

@Getter
@Setter
@NoArgsConstructor
public class ExternalTlsInfo extends ExternalCertificateData {

	@JsonProperty(EXTERNAL_CERT_DIRECTORY_FIELD)
	private String externalCertDirectory;

	@JsonProperty(USE_EXTERNAL_TLS_FIELD)
	private GetInput useExternalTls;

	@JsonProperty(CA_NAME_FIELD)
	private GetInput caName;

	@JsonProperty(EXTERNAL_CERTIFICATE_PARAMETERS_FIELD)
	private ExternalCertificateParameters externalCertificateParameters;

}
