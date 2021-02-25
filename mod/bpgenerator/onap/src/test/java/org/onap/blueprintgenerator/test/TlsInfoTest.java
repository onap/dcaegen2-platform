/*============LICENSE_START=======================================================
org.onap.dcae
================================================================================
Copyright (c) 2020-2021 Nokia. All rights reserved.
Copyright (c) 2020 AT&T. All rights reserved.
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

package org.onap.blueprintgenerator.test;

import java.util.Map;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.common.Node;
import org.onap.blueprintgenerator.model.common.OnapBlueprint;
import org.onap.blueprintgenerator.model.common.ExternalCertificateParameters;
import org.onap.blueprintgenerator.model.common.ExternalTlsInfo;
import org.onap.blueprintgenerator.model.dmaap.TlsInfo;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test Case for Tls Info
 *
 */
public class TlsInfoTest extends BlueprintGeneratorTests {

    @Parameterized.Parameter
    public char bpType;

    @Parameterized.Parameters(name = "Blueprint type: {0}")
    public static List<Character> data() {
        return Arrays.asList('o', 'd');
    }

    /**
     * Test Case for Tls True and External TLS True
     *
     */
    @Test
    public void useTlsTrueAndUseExternalTlsTrueTest() {
        OnapBlueprint bp =
            createBlueprintFromFile(
                Paths.get(
                    "src",
                    "test",
                    "resources",
                    "componentspecs",
                    useTlsTrueAndUseExternalTlsTrueTest)
                    .toFile()
                    .getAbsolutePath());

        assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(bp, true);
        assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, true);
    }
    /**
     * Test Case for Tls False and External TLS False
     *
     */
    @Test
    public void useTlsFalseAndUseExternalTlsFalseTest() {
        OnapBlueprint bp =
            createBlueprintFromFile(
                Paths.get(
                    "src",
                    "test",
                    "resources",
                    "componentspecs",
                    useTlsFalseAndUseExternalTlsFalseTest)
                    .toFile()
                    .getAbsolutePath());

        assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(bp, false);
        assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, false);
    }

    /**
     * Test Case for Tls True and No External TLS Flag
     *
     */
    @Test
    public void useTlsTrueAndNoExternalTlsFlagTest() {
        OnapBlueprint bp =
            createBlueprintFromFile(
                Paths.get(
                    "src",
                    "test",
                    "resources",
                    "componentspecs",
                    useTlsTrueAndNoExternalTlsFlagTest)
                    .toFile()
                    .getAbsolutePath());

        assertBlueprintContainsTlsInfoWithUseFlagDefault(bp, true);
        assertBlueprintHasNoExternalTlsInfo(bp);
    }

    /**
     * Test Case for No Tls Info
     *
     */
    @Test
    public void noTlsInfo() {
        OnapBlueprint bp =
            createBlueprintFromFile(
                Paths.get("src", "test", "resources", "componentspecs", noTlsInfo)
                    .toFile()
                    .getAbsolutePath());

        assertBlueprintHasNoTlsInfo(bp);
        assertBlueprintHasNoExternalTlsInfo(bp);
    }

    private void assertBlueprintContainsExternalTlsInfoWithUseFlagDefault(
        OnapBlueprint bp, boolean useFlagDefault) {
        // should create proper inputs
        assertContainsInputWithDefault(bp, "external_cert_use_external_tls", useFlagDefault);
        assertContainsInputWithDefault(bp, "external_cert_ca_name", "\"RA\"");
        assertContainsInputWithDefault(bp, "external_cert_cert_type", "\"P12\"");
        assertContainsInputWithDefault(bp, "external_cert_common_name", "\"sample.onap.org\"");
        assertContainsInputWithDefault(
            bp, "external_cert_sans", "\"sample.onap.org,component.sample.onap.org\"");

        Node node = bp.getNode_templates().get("test.component.spec");

        // should create proper externalTlsInfo object in node properties
        ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_cert();
        assertNotNull(externalTlsInfo);

        assertEquals("external_cert_ca_name", externalTlsInfo.getCaName().getBpInputName());
        assertEquals("external_cert_cert_type", externalTlsInfo.getCertType().getBpInputName());
        assertEquals(
            "external_cert_use_external_tls", externalTlsInfo.getUseExternalTls().getBpInputName());
        assertEquals("/opt/app/dcae-certificate/", externalTlsInfo.getExternalCertDirectory());

        ExternalCertificateParameters extCertParams =
            externalTlsInfo.getExternalCertificateParameters();
        assertNotNull(extCertParams);

        assertEquals("external_cert_common_name", extCertParams.getCommonName().getBpInputName());
        assertEquals("external_cert_sans", extCertParams.getSans().getBpInputName());
    }

    private void assertBlueprintContainsTlsInfoWithUseFlagDefault(
        OnapBlueprint bp, boolean useFlagDefault) {
        // shold create proper inputs
        assertContainsInputWithDefault(bp, "use_tls", useFlagDefault);

        Node node = bp.getNode_templates().get("test.component.spec");

        // should create proper tlsInfo object in node properties
        TlsInfo tlsInfo = node.getProperties().getTls_info();
        assertEquals("use_tls", tlsInfo.getUseTls().getBpInputName());
        assertEquals("/opt/app/dcae-certificate/", tlsInfo.getCertDirectory());
    }

    private void assertBlueprintHasNoExternalTlsInfo(OnapBlueprint bp) {
        // should not create inputs for external tls
        assertFalse(bp.getInputs().containsKey("external_cert_use_external_tls"));
        assertFalse(bp.getInputs().containsKey("external_cert_common_name"));
        assertFalse(bp.getInputs().containsKey("external_cert_ca_name"));
        assertFalse(bp.getInputs().containsKey("external_cert_sans"));

        Node node = bp.getNode_templates().get("test.component.spec");

        // should not create externalTlsInfo object in node properties
        ExternalTlsInfo externalTlsInfo = node.getProperties().getExternal_cert();
        assertNull(externalTlsInfo);
    }

    private void assertBlueprintHasNoTlsInfo(OnapBlueprint bp) {
        // should not create inputs for tls
        assertFalse(bp.getInputs().containsKey("use_tls"));

        Node node = bp.getNode_templates().get("test.component.spec");

        // should not create tlsInfo object in node properties
        assertNull(node.getProperties().getTls_info());
    }

    private void assertContainsInputWithDefault(
        OnapBlueprint bp, String inputName, Object defaultValue) {
        Map<String, Object> input = bp.getInputs().get(inputName);
        assertNotNull(input);
        assertEquals(defaultValue, input.get("default"));
    }

    private OnapBlueprint createBlueprintFromFile(String path) {
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(path);
        Input input = onapTestUtils.getInput(path, "", "", "", "o", "");
        OnapBlueprint onapBlueprint = onapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);
        return onapBlueprint;
    }
}
