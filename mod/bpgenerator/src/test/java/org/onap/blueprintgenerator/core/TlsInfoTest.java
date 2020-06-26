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

package org.onap.blueprintgenerator.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.blueprint.Node;
import org.onap.blueprintgenerator.models.blueprint.tls.TlsInfo;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalCertificateParameters;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalTlsInfo;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class TlsInfoTest {

	@Parameterized.Parameter
	public char bpType;

	@Parameterized.Parameters(name = "Blueprint type: {0}")
	public static List<Character> data() {
		return Arrays.asList('o', 'd');
	}

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
		assertContainsInputWithDefault(bp, "external_cert_use_external_tls", useFlagDefault);
		assertContainsInputWithDefault(bp, "external_cert_ca_name", "\"RA\"");
		assertContainsInputWithDefault(bp, "external_cert_cert_type", "\"P12\"");
		assertContainsInputWithDefault(bp, "external_cert_common_name", "\"sample.onap.org\"");
		assertContainsInputWithDefault(bp, "external_cert_sans",
				"\"sample.onap.org:component.sample.onap.org\"");

		Node node = bp.getNode_templates().get("test.component.spec");

		//should create proper externalTlsInfo object in node properties
		ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_cert();
		assertNotNull(externalTlsInfo);

		assertEquals("external_cert_ca_name", externalTlsInfo.getCaName().getBpInputName());
		assertEquals("external_cert_cert_type", externalTlsInfo.getCertType().getBpInputName());
		assertEquals("external_cert_use_external_tls", externalTlsInfo.getUseExternalTls().getBpInputName());
		assertEquals("/opt/app/dcae-certificate/", externalTlsInfo.getExternalCertDirectory());

		ExternalCertificateParameters extCertParams = externalTlsInfo.getExternalCertificateParameters();
		assertNotNull(extCertParams);

		assertEquals("external_cert_common_name", extCertParams.getCommonName().getBpInputName());
		assertEquals("external_cert_sans", extCertParams.getSans().getBpInputName());
	}

	private void assertBlueprintContainsTlsInfoWithUseFlagDefault(Blueprint bp, boolean useFlagDefault) {
		//shold create proper inputs
		assertContainsInputWithDefault(bp, "use_tls", useFlagDefault);

		Node node = bp.getNode_templates().get("test.component.spec");

		//should create proper tlsInfo object in node properties
		TlsInfo tlsInfo = node.getProperties().getTls_info();
		assertEquals("use_tls", tlsInfo.getUseTls().getBpInputName());
		assertEquals("/opt/app/dcae-certificate/", tlsInfo.getCertDirectory());

	}

	private void assertBlueprintHasNoExternalTlsInfo(Blueprint bp) {
		//should not create inputs for external tls
		assertFalse(bp.getInputs().containsKey("external_cert_use_external_tls"));
		assertFalse(bp.getInputs().containsKey("external_cert_common_name"));
		assertFalse(bp.getInputs().containsKey("external_cert_ca_name"));
		assertFalse(bp.getInputs().containsKey("external_cert_sans"));

		Node node = bp.getNode_templates().get("test.component.spec");

		//should not create externalTlsInfo object in node properties
		ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_cert();
		assertNull(externalTlsInfo);
	}


	private void assertBlueprintHasNoTlsInfo(Blueprint bp) {
		//should not create inputs for tls
		assertFalse(bp.getInputs().containsKey("use_tls"));

		Node node = bp.getNode_templates().get("test.component.spec");

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
		bp = bp.createBlueprint(cs, "", this.bpType, "", "");
		return bp;
	}
}
