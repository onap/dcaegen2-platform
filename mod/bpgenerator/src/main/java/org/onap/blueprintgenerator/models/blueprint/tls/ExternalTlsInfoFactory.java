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

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createInputValue;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.CA_NAME_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.CERT_DIRECTORY_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.CERT_TYPE_FIELD;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_CA;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.DEFAULT_CERT_TYPE;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.USE_EXTERNAL_TLS_FIELD;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.onap.blueprintgenerator.models.blueprint.tls.api.ExternalCertificateDataFactory;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalTlsInfo;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

public class ExternalTlsInfoFactory extends ExternalCertificateDataFactory {

	private ExternalCertificateParametersFactory externalCertificateParametersFactory;

	public ExternalTlsInfoFactory(ExternalCertificateParametersFactory externalCertificateDataFactory) {
		this.externalCertificateParametersFactory = externalCertificateDataFactory;
	}

	public ExternalTlsInfo createFromComponentSpec(ComponentSpec cs) {
		ExternalTlsInfo externalTlsInfoBp = new ExternalTlsInfo();
		TreeMap<String, Object> tlsInfoCs = cs.getAuxilary().getTls_info();

		externalTlsInfoBp.setExternalCertDirectory((String) tlsInfoCs.get(CERT_DIRECTORY_FIELD));
		externalTlsInfoBp.setUseExternalTls(createPrefixedGetInput(USE_EXTERNAL_TLS_FIELD));
		externalTlsInfoBp.setCaName(createPrefixedGetInput(CA_NAME_FIELD));
		externalTlsInfoBp.setCertType(createPrefixedGetInput(CERT_TYPE_FIELD));
		externalTlsInfoBp.setExternalCertificateParameters(externalCertificateParametersFactory.create());

		return externalTlsInfoBp;
	}

	public Map<String, LinkedHashMap<String, Object>> createInputListFromComponentSpec(ComponentSpec cs){
		Map<String, LinkedHashMap<String, Object>> retInputs = new HashMap<>();

		Map<String, Object> externalTlsInfoCs = cs.getAuxilary().getTls_info();

		LinkedHashMap<String, Object> useTlsFlagInput = createInputValue("boolean",
				"Flag to indicate external tls enable/disable.",
				externalTlsInfoCs.get(USE_EXTERNAL_TLS_FIELD));
		retInputs.put(addPrefix(USE_EXTERNAL_TLS_FIELD), useTlsFlagInput);

		LinkedHashMap<String, Object> caNameInputMap = createInputValue("string",
				"Name of Certificate Authority configured on CertService side.",
				DEFAULT_CA);
		retInputs.put(addPrefix(CA_NAME_FIELD), caNameInputMap);

		LinkedHashMap<String, Object> certTypeInputMap = createInputValue("string",
			"Format of provided certificates",
			DEFAULT_CERT_TYPE);
		retInputs.put(addPrefix(CERT_TYPE_FIELD), certTypeInputMap);

		retInputs.putAll(externalCertificateParametersFactory.createInputList());
		return retInputs;
	}

}
