/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.dmaapbp.DmaapNode;
import org.onap.blueprintgenerator.models.onapbp.OnapNode;
import org.onap.blueprintgenerator.models.policymodel.PolicyModel;
import picocli.CommandLine;

// TODO: Auto-generated Javadoc

/**
 * The Class BlueprintGeneratorTest.
 */
public class BlueprintGeneratorTest {

    /**
     * Component spec test.
     *
     */
    @Test
    public void componentSpecTest() {
        ComponentSpec spec = new ComponentSpec();
        TestComponentSpec test = new TestComponentSpec();
        spec.createComponentSpecFromString(test.getCs());
        ComponentSpec expectedSpec = test.getCsConcrete();

        assertEquals(expectedSpec.getSelf(), spec.getSelf());
        assertEquals(expectedSpec.getServices(), spec.getServices());
        assertEquals(expectedSpec.getStreams(), spec.getStreams());
        assertArrayEquals(expectedSpec.getParameters(), spec.getParameters());
        assertEquals(expectedSpec.getAuxilary(), spec.getAuxilary());
        assertArrayEquals(expectedSpec.getArtifacts(), spec.getArtifacts());
    }

    /**
     * Tosca definition test.
     */
    @Test
    public void toscaDefinitionTest() {
        ComponentSpec cs = new ComponentSpec();
        TestComponentSpec test = new TestComponentSpec();
        cs.createComponentSpecFromString(test.getCs());
        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        assertEquals("cloudify_dsl_1_3", bp.getTosca_definitions_version());
    }

    /**
     * Imports test.
     */
    @Test
    public void importsTest() {
        ComponentSpec cs = new ComponentSpec();
        TestComponentSpec test = new TestComponentSpec();
        cs.createComponentSpecFromString(test.getCs());

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        ArrayList<String> imps = new ArrayList<>();

        imps.add("http://www.getcloudify.org/spec/cloudify/3.4/types.yaml");
        imps.add(
            "https://nexus.onap.org/service/local/repositories/raw/content/org.onap.dcaegen2.platform.plugins/R6/k8splugin/1.7.2/k8splugin_types.yaml");
        imps.add(
            "https://nexus.onap.org/service/local/repositories/raw/content/org.onap.dcaegen2.platform.plugins/R6/dcaepolicyplugin/2.4.0/dcaepolicyplugin_types.yaml");
        assertEquals(imps, bp.getImports());
    }

    @Test
    public void inputTest() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        TreeMap<String, LinkedHashMap<String, Object>> inputs = new TreeMap<>();

        //mr inputs
        LinkedHashMap<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");

        //necessary inputs
        LinkedHashMap<String, Object> tag = new LinkedHashMap<>();
        tag.put("type", "string");
        String tester = "test.tester";
        tag.put("default", '"' + tester + '"');
        inputs.put("tag_version", tag);

        inputs.put("log_directory", stringType);

        LinkedHashMap<String, Object> cert = new LinkedHashMap<>();
        cert.put("type", "string");
        cert.put("default", "");
        inputs.put("cert_directory", cert);

        LinkedHashMap<String, Object> env = new LinkedHashMap<>();
        env.put("default", "{}");
        inputs.put("envs", env);

        LinkedHashMap<String, Object> port = new LinkedHashMap<>();
        port.put("type", "string");
        port.put("description", "Kubernetes node port on which collector is exposed");
        port.put("default", "99");
        inputs.put("external_port", port);

        LinkedHashMap<String, Object> rep = new LinkedHashMap<>();
        rep.put("type", "integer");
        rep.put("description", "number of instances");
        rep.put("default", 1);
        inputs.put("replicas", rep);

        LinkedHashMap<String, Object> aaf = new LinkedHashMap<>();
        aaf.put("type", "boolean");
        aaf.put("default", false);
        inputs.put("use_tls", aaf);

        //parmaeter input
        LinkedHashMap<String, Object> test = new LinkedHashMap<>();
        test.put("type", "string");
        String testParam = "test-param-1";
        test.put("default", '"' + testParam + '"');
        inputs.put("testParam1", test);

        //mr/dr inputs
        inputs.put("TEST-PUB-DR_feed0_client_role", stringType);
        inputs.put("TEST-PUB-DR_feed0_password", stringType);
        inputs.put("TEST-PUB-DR_feed0_username", stringType);
        inputs.put("TEST-PUB-MR_topic1_aaf_password", stringType);
        inputs.put("TEST-PUB-MR_topic1_aaf_username", stringType);
        inputs.put("TEST-PUB-MR_topic1_client_role", stringType);
        inputs.put("TEST-SUB-DR_feed1_client_role", stringType);
        inputs.put("TEST-SUB-DR_feed1_password", stringType);
        inputs.put("TEST-SUB-DR_feed1_username", stringType);
        inputs.put("TEST-SUB-MR_topic0_client_role", stringType);
        inputs.put("TEST-SUB-MR_topic2_aaf_password", stringType);
        inputs.put("TEST-SUB-MR_topic2_aaf_username", stringType);
        inputs.put("namespace", stringType);
        inputs.put("idn_fqdn", cert);
        inputs.put("feed0_name", stringType);
        inputs.put("feed1_name", stringType);
        inputs.put("topic0_name", stringType);
        inputs.put("topic1_name", stringType);

        LinkedHashMap<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("type", "string");
        cpu.put("default", "250m");
        inputs.put("test.component.spec_cpu_limit", cpu);
        inputs.put("test.component.spec_cpu_request", cpu);

        LinkedHashMap<String, Object> mem = new LinkedHashMap<>();
        mem.put("type", "string");
        mem.put("default", "128Mi");
        inputs.put("test.component.spec_memory_limit", mem);
        inputs.put("test.component.spec_memory_request", mem);
    }

    @Test
    public void interfaceTest() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

        OnapNode testNode = new OnapNode();

        //set the type
        testNode.setType("dcae.nodes.ContainerizedServiceComponent");

        ArrayList<String> ports = new ArrayList<>();
        ports.add("concat: [\"80:\", {get_input: external_port }]");
        ports.add("concat: [\"99:\", {get_input: external_port }]");
    }

    @Test
    public void parametersTest() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

        GetInput par = (GetInput) node.getProperties().getApplication_config().getParams().get("testParam1");
        assertEquals("testParam1", par.getBpInputName());
    }

    @Test
    public void streamPublishesTest() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'o', "", "");

        OnapNode node = (OnapNode) bp.getNode_templates().get("test.component.spec");

        assertFalse(node.getProperties().getApplication_config().getStreams_publishes().isEmpty());
    }

    @Test
    public void dmaapPluginTest() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        Blueprint bp = new Blueprint();
        bp = bp.createBlueprint(cs, "", 'd', "", "");

        DmaapNode dmaap = (DmaapNode) bp.getNode_templates().get("test.component.spec");

        //check if the stream publishes and subscribes are not null to see if the dmaap plugin was invoked properly
        assertNotNull(dmaap.getProperties().getStreams_publishes());
        assertNotNull(dmaap.getProperties().getStreams_subscribes());
    }

    @Test
    public void testPrintInstructionsBlueprintCommand() {
        BlueprintCommand objUnderTest = new BlueprintCommand();
        CommandLine cli = new CommandLine(objUnderTest);
        PrintStream mockStdOutWriter = Mockito.mock(PrintStream.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        cli.usage(mockStdOutWriter);
        verify(mockStdOutWriter, times(1)).print(any(Object.class));

    }

    @Test
    public void testPrintInstructionsPolicyCommand() {
        PolicyCommand objUnderTest = new PolicyCommand();
        CommandLine cli = new CommandLine(objUnderTest);
        PrintStream mockStdOutWriter = Mockito.mock(PrintStream.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        cli.usage(mockStdOutWriter);
        verify(mockStdOutWriter, times(1)).print(any(Object.class));
    }

    @Test
    public void testPolicyModels() {
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        PolicyModel p = new PolicyModel();
        p.createPolicyModels(cs, "TestModels");
    }
}
