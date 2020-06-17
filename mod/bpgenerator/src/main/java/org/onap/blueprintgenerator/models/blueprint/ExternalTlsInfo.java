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

	static final String USE_EXTERNAL_TLS_FIELD = "use_external_tls";

	private static final String DEFAULT_CA = "RA";
	private static final Object DEFAULT_CERT_TYPE = "P12";

	private static final String INPUT_PREFIX = "external_tls_";
	private static final String EXTERNAL_CERT_DIRECTORY_FIELD = "external_cert_directory";
	private static final String CA_NAME_FIELD = "ca_name";
	private static final String CERT_TYPE_FIELD = "cert_type";
	private static final String EXTERNAL_CERTIFICATE_PARAMETERS_FIELD = "external_certificate_parameters";

	@JsonProperty(EXTERNAL_CERT_DIRECTORY_FIELD)
	private String externalCertDirectory;

	@JsonProperty(USE_EXTERNAL_TLS_FIELD)
	private GetInput useExternalTls;

	@JsonProperty(CA_NAME_FIELD)
	private GetInput caName;

	@JsonProperty(CERT_TYPE_FIELD)
	private GetInput certType;

	@JsonProperty(EXTERNAL_CERTIFICATE_PARAMETERS_FIELD)
	private ExternalCertificateParameters externalCertificateParameters;

	static ExternalTlsInfo createFromComponentSpec(ComponentSpec cs) {
		ExternalTlsInfo externalTlsInfoBp = new ExternalTlsInfo();
		TreeMap<String, Object> tlsInfoCs = cs.getAuxilary().getTls_info();

		externalTlsInfoBp.setExternalCertDirectory((String) tlsInfoCs.get("cert_directory"));
		externalTlsInfoBp.setUseExternalTls(createGetInput(USE_EXTERNAL_TLS_FIELD));
		externalTlsInfoBp.setCaName(createGetInput(CA_NAME_FIELD));
		externalTlsInfoBp.setCertType(createGetInput(CERT_TYPE_FIELD));

		ExternalCertificateParameters externalCertificateParameters =
				ExternalCertificateParameters.create();
		externalTlsInfoBp.setExternalCertificateParameters(externalCertificateParameters);

		return externalTlsInfoBp;
	}

	static Map<String, LinkedHashMap<String, Object>> createInputMapFromComponentSpec(ComponentSpec cs){
		Map<String, LinkedHashMap<String, Object>> retInputs = new HashMap<>();

		Map<String, Object> tlsInfoCs = cs.getAuxilary().getTls_info();
		LinkedHashMap<String, Object> useTlsFlagInput = Properties.makeInput("boolean",
				"Flag to indicate external tls enable/disable.",
				tlsInfoCs.get(USE_EXTERNAL_TLS_FIELD));
		retInputs.put(addPrefix(USE_EXTERNAL_TLS_FIELD), useTlsFlagInput);

		LinkedHashMap<String, Object> caNameInputMap = Properties.makeInput("string",
				"Name of Certificate Authority configured on CertService side.",
				DEFAULT_CA);
		retInputs.put(addPrefix(CA_NAME_FIELD), caNameInputMap);

		LinkedHashMap<String, Object> certTypeInputMap = Properties.makeInput("string",
				"Format of provided certificates",
				DEFAULT_CERT_TYPE);
		retInputs.put(addPrefix(CERT_TYPE_FIELD), certTypeInputMap);

		retInputs.putAll(ExternalCertificateParameters.createInputMap());
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

		private static final String DEFAULT_COMMON_NAME = "sample.onap.org";
		private static final String DEFAULT_SANS = "sample.onap.org:component.sample.onap.org";

		private static final String COMMON_NAME_FIELD = "common_name";
		private static final String SANS_FIELD = "sans";

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

		private static Map<String, LinkedHashMap<String, Object>> createInputMap(){
			Map<String, LinkedHashMap<String, Object>> retInputs = new LinkedHashMap<>();

			LinkedHashMap<String, Object> commonNameInputMap = Properties.makeInput("string",
					"Common name which should be present in certificate.",
					DEFAULT_COMMON_NAME);
			retInputs.put(addPrefix(COMMON_NAME_FIELD), commonNameInputMap);

			LinkedHashMap<String, Object> sansInputMap = Properties.makeInput("string",
					"\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
							"Delimiter - : Should contain common_name value and other FQDNs under which given " +
							"component is accessible.\"",
					DEFAULT_SANS);
			retInputs.put(addPrefix(SANS_FIELD), sansInputMap);
			return retInputs;
		}
	}
}
