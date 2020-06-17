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

package org.onap.blueprintgenerator.models.blueprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@NoArgsConstructor
public class ExternalTlsInfo {

	static final String INPUT_PREFIX = "external_tls_";
	static final String EXTERNAL_CERT_DIRECTORY_FIELD = "external_cert_directory";
	static final String USE_EXTERNAL_TLS_FIELD = "use_external_tls";
	static final String CA_NAME_FIELD = "ca_name";
	static final String EXTERNAL_CERTIFICATE_PARAMETERS_FIELD = "external_certificate_parameters";
	static final String COMMON_NAME_FIELD = "common_name";
	static final String SANS_FIELD = "sans";

	@JsonProperty(EXTERNAL_CERT_DIRECTORY_FIELD)
	private String externalCertDirectory;

	@JsonProperty(USE_EXTERNAL_TLS_FIELD)
	private GetInput useExternalTls;

	@JsonProperty(CA_NAME_FIELD)
	private GetInput caName;

	@JsonProperty(EXTERNAL_CERTIFICATE_PARAMETERS_FIELD)
	private ExternalCertificateParameters externalCertificateParameters;

	static ExternalTlsInfo createFromComponentSpec(ComponentSpec cs) {
		ExternalTlsInfo externalTlsInfoBp = new ExternalTlsInfo();
		TreeMap<String, Object> externalTlsInfoCs = cs.getAuxilary().getExternal_tls_info();

		externalTlsInfoBp.setExternalCertDirectory((String) externalTlsInfoCs.get(EXTERNAL_CERT_DIRECTORY_FIELD));
		externalTlsInfoBp.setUseExternalTls(createGetInput(USE_EXTERNAL_TLS_FIELD));
		externalTlsInfoBp.setCaName(createGetInput(CA_NAME_FIELD));

		ExternalCertificateParameters externalCertificateParameters =
				ExternalCertificateParameters.create();
		externalTlsInfoBp.setExternalCertificateParameters(externalCertificateParameters);

		return externalTlsInfoBp;
	}

	static Map<String, LinkedHashMap<String, Object>> createInputListFromComponentSpec(ComponentSpec cs){
		Map<String, LinkedHashMap<String, Object>> retInputs = new HashMap<>();

		Map<String, Object> externalTlsInfoCs = cs.getAuxilary().getExternal_tls_info();
		LinkedHashMap<String, Object> useTlsFlagInput = Properties.makeInput("boolean",
				"Flag to indicate external tls enable/disable.",
				externalTlsInfoCs.get(USE_EXTERNAL_TLS_FIELD));
		retInputs.put(addPrefix(USE_EXTERNAL_TLS_FIELD), useTlsFlagInput);

		LinkedHashMap<String, Object> caNameInputMap = Properties.makeInput("string",
				"Name of Certificate Authority configured on CertService side.",
				externalTlsInfoCs.get(CA_NAME_FIELD));
		retInputs.put(addPrefix(CA_NAME_FIELD), caNameInputMap);

		retInputs.putAll(ExternalCertificateParameters.createInputListFromComponentSpec(cs));
		return retInputs;
	}

	private static GetInput createGetInput(String fieldName) {
		return new GetInput(addPrefix(fieldName));
	}

	private static String addPrefix(String fieldName) {
		return INPUT_PREFIX + fieldName;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ExternalCertificateParameters {

		@JsonProperty(COMMON_NAME_FIELD)
		private GetInput commonName;

		@JsonProperty(SANS_FIELD)
		private GetInput sans;

		private static ExternalCertificateParameters create() {
			ExternalCertificateParameters externalCertificateParameters = new ExternalCertificateParameters();
			externalCertificateParameters.setCommonName(createGetInput(COMMON_NAME_FIELD));
			externalCertificateParameters.setSans(createGetInput(SANS_FIELD));
			return externalCertificateParameters;
		}

		private static Map<String, LinkedHashMap<String, Object>> createInputListFromComponentSpec(ComponentSpec cs){
			Map<String, LinkedHashMap<String, Object>> retInputs = new LinkedHashMap<>();
			Map<String, Object> externalTlsCertParams =
					(Map<String, Object>) cs.getAuxilary()
							.getExternal_tls_info().get(EXTERNAL_CERTIFICATE_PARAMETERS_FIELD);

			LinkedHashMap<String, Object> commonNameInputMap = Properties.makeInput("string",
					"Common name which should be present in certificate.",
					externalTlsCertParams.get(COMMON_NAME_FIELD));
			retInputs.put(addPrefix(COMMON_NAME_FIELD), commonNameInputMap);

			LinkedHashMap<String, Object> sansInputMap = Properties.makeInput("string",
					"\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
							"Delimiter - : Should contain common_name value and other FQDNs under which given " +
							"component is accessible.\"",
					externalTlsCertParams.get(SANS_FIELD));
			retInputs.put(addPrefix(SANS_FIELD), sansInputMap);
			return retInputs;
		}
	}
}
