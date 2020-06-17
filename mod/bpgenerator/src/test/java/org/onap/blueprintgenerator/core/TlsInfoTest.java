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

package org.onap.blueprintgenerator.core;

import org.junit.Test;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.blueprint.ExternalTlsInfo;
import org.onap.blueprintgenerator.models.blueprint.TlsInfo;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.onapbp.OnapNode;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TlsInfoTest {

	@Test
	public void useTlsTrueAndUseExternalTlsTrueTest(){
		Blueprint bp = createBlueprintFromFile("TestCases/TlsInfo/testComponentSpec_withTlsTrueAndExternalTlsTrue.json");

		assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(bp, true);
		assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, true);
	}

	@Test
	public void useTlsFalseAndUseExternalTlsFalseTest(){
		Blueprint bp = createBlueprintFromFile("TestCases/TlsInfo/testComponentSpec_withTlsFalseAndExternalTlsFalse.json");

		assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(bp, false);
		assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, false);
	}

	@Test
	public void useTlsTrueAndNoExternalTlsFlagTest(){
		Blueprint bp = createBlueprintFromFile("TestCases/TlsInfo/testComponentSpec_withTlsTrueAndNoExternalTls.json");

		assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, true);
		assertBlueprintHasNoExternalTlsInfo(bp);
	}

	@Test
	public void noTlsInfo(){
		Blueprint bp = createBlueprintFromFile("TestCases/TlsInfo/testComponentSpec_withoutTlsInfo.json");

		assertBlueprintHasNoTlsInfo(bp);
		assertBlueprintHasNoExternalTlsInfo(bp);
	}

	private void assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(Blueprint bp, boolean useFlagDefault) {
		//should create proper inputs
		assertContainsInputWithDefault(bp, "external_tls_use_external_tls", useFlagDefault);
		assertContainsInputWithDefault(bp, "external_tls_ca_name", "\"RA\"");
		assertContainsInputWithDefault(bp, "external_tls_cert_type", "\"P12\"");
		assertContainsInputWithDefault(bp, "external_tls_common_name", "\"sample.onap.org\"");
		assertContainsInputWithDefault(bp, "external_tls_sans",
				"\"sample.onap.org:component.sample.onap.org\"");

		OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

		//should create proper externalTlsInfo object in node properties
		ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_tls_info();
		assertNotNull(externalTlsInfo);

		assertEquals("external_tls_ca_name", externalTlsInfo.getCaName().getGet_input());
		assertEquals("external_tls_cert_type", externalTlsInfo.getCertType().getGet_input());
		assertEquals("external_tls_use_external_tls", externalTlsInfo.getUseExternalTls().getGet_input());
		assertEquals("/opt/app/dcae-certificate/", externalTlsInfo.getExternalCertDirectory());

		ExternalTlsInfo.ExternalCertificateParameters extCertParams = externalTlsInfo.getExternalCertificateParameters();
		assertNotNull(extCertParams);

		assertEquals("external_tls_common_name", extCertParams.getCommonName().getGet_input());
		assertEquals("external_tls_sans", extCertParams.getSans().getGet_input());
	}

	private void assertBlueprintContainsTlsInfoWithUseFlagDefault(Blueprint bp, boolean useFlagDefault) {
		//shold create proper inputs
		assertContainsInputWithDefault(bp, "use_tls", useFlagDefault);

		OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

		//should create proper tlsInfo object in node properties
		TlsInfo tlsInfo = node.getProperties().getTls_info();
		assertEquals("use_tls", tlsInfo.getUseTls().getGet_input());
		assertEquals("/opt/app/dcae-certificate/", tlsInfo.getCertDirectory());

	}

	private void assertBlueprintHasNoExternalTlsInfo(Blueprint bp) {
		//should not create inputs for external tls
		assertFalse(bp.getInputs().containsKey("external_tls_use_external_tls"));
		assertFalse(bp.getInputs().containsKey("external_tls_common_name"));
		assertFalse(bp.getInputs().containsKey("external_tls_ca_name"));
		assertFalse(bp.getInputs().containsKey("external_tls_sans"));

		OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

		//should not create externalTlsInfo object in node properties
		ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_tls_info();
		assertNull(externalTlsInfo);
	}


	private void assertBlueprintHasNoTlsInfo(Blueprint bp) {
		//should not create inputs for tls
		assertFalse(bp.getInputs().containsKey("use_tls"));

		OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

		//should not create tlsInfo object in node properties
		assertNull(node.getProperties().getTls_info());
	}

	private void assertContainsInputWithDefault(Blueprint bp, String inputName, Object defaultValue) {
		LinkedHashMap<String, Object> input = bp.getInputs().get(inputName);
		assertNotNull(input);
		assertEquals(defaultValue, input.get("default"));
	}

	private Blueprint createBlueprintFromFile(String path) {
		ComponentSpec cs = new ComponentSpec();
		cs.createComponentSpecFromFile(path);

		Blueprint bp = new Blueprint();
		bp = bp.createBlueprint(cs, "", 'o', "", "");
		return bp;
	}
}
