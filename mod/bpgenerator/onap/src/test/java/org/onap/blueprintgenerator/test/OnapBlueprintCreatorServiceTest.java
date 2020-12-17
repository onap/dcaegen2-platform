/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2020  Nokia. All rights reserved.
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

package org.onap.blueprintgenerator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.common.Node;
import org.onap.blueprintgenerator.model.common.OnapBlueprint;
import org.onap.blueprintgenerator.model.common.Properties;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * ONAP Blueprint Test Cases for ONAP and DMAAP
 */


public class OnapBlueprintCreatorServiceTest extends BlueprintGeneratorTests {

    private String outputfilesPath = Paths.get("src", "test", "resources", "outputfiles").toAbsolutePath().toString();

    @DisplayName("Testing K8s Blueprint for Service Name Override Component Spec Input File")
    @Test
    public void testServiceNameOverrideK8sBlueprint() throws IOException {
        Input input = onapTestUtils.getInput(Paths.get("src", "test", "resources", "componentspecs" , ves).toFile().getAbsolutePath(), outputfilesPath, "testServiceNameOverrideK8sBlueprint", "", "o", "");
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
        assertNotNull("K8s Blueprint for Service Name Override Component Spec is NULL", onapComponentSpec);

        OnapBlueprint onapBlueprint = onapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);
        blueprintService.blueprintToYaml(onapComponentSpec, onapBlueprint, input);
        System.out.println(blueprintService.blueprintToString(onapComponentSpec, onapBlueprint, input));

        onapTestUtils.verifyToscaDefVersion("Service Name Override K8s",onapBlueprint, Constants.TOSCA_DEF_VERSION);
        onapTestUtils.verifyBpImports("Service Name Override K8s",onapBlueprint,false);

        Map<String, LinkedHashMap<String, Object>> k8sBpInputs = onapBlueprint.getInputs();
        assertNotNull("Service Name Override K8s Blueprint Inputs Section is NULL", k8sBpInputs);
        assertTrue("Service Name Override K8s Blueprint Inputs Section is Empty", k8sBpInputs.size() > 0);

        assertEquals("Service Name Override K8s Blueprint:Inputs " + Constants.ONAP_INPUT_CPU_LIMIT + " Default is not matching", k8sBpInputs.get(Constants.ONAP_INPUT_CPU_LIMIT).get("default"), Constants.ONAP_DEFAULT250m);
        assertEquals("Service Name Override K8s Blueprint:Inputs " + Constants.SERVICE_COMPONENT_NAME_OVERRIDE + " Default is not matching", k8sBpInputs.get(Constants.SERVICE_COMPONENT_NAME_OVERRIDE).get("default"), Constants.ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT);

        Map<String, Node> k8sBpNodeTemplates = onapBlueprint.getNode_templates();
        assertNotNull("Service Name Override K8s Blueprint Node Templates Section is NULL in the K8s DMAAP Blueprint", k8sBpNodeTemplates);
        assertTrue("Service Name Override K8s Blueprint Node Templates Section Size is Empty", k8sBpNodeTemplates.size() > 0);

        assertEquals("Service Name Override K8s Blueprint:NodeTemplates:Type is not Matching", k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES).getType(), Constants.ONAP_NODETEMPLATES_TYPE);

        Properties k8sBpNodeTemplateProperties = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getProperties();
        assertNotNull("Service Name Override K8s Blueprint Node Templates:Properties Section is NULL", k8sBpNodeTemplateProperties);


        Map<String, LinkedHashMap<String, Object>> bpInputs = onapBlueprint.getInputs();
        onapTestUtils.verifyArtifacts("Service Name Override K8s",onapComponentSpec,bpInputs,"o");

        onapTestUtils.verifyStreamsPublishes("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyStreamsSubscribes("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyAuxilary("Service Name Override K8s",onapComponentSpec);
        onapTestUtils.verifyHealthCheck("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyServicesCalls("Service Name Override K8s",onapComponentSpec);
        onapTestUtils.verifyServicesProvides("Service Name Override K8s",onapComponentSpec);
        onapTestUtils.verifyDockerConfig("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyParameters("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplates);
        onapTestUtils.verifyVolumes("Service Name Override K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
    }

    @DisplayName("Testing K8s Blueprint for Service Name Override Component Spec Input File with Import File")
    @Test
    public void testServiceNameOverrideImportFileK8sBlueprint() throws IOException {
        Input input = onapTestUtils.getInput(Paths.get("src", "test", "resources", "componentspecs", ves).toFile().getAbsolutePath(), "", "", Paths.get("src", "test", "resources", "componentspecs" , testImports).toFile().getAbsolutePath(), "o", "");
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
        assertNotNull("K8s Blueprint for Service Name Override with Import File Component Spec is NULL", onapComponentSpec);

        OnapBlueprint onapBlueprint = onapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);

        onapTestUtils.verifyToscaDefVersion("Service Name Override with Import File K8s",onapBlueprint, Constants.TOSCA_DEF_VERSION);
        onapTestUtils.verifyBpImportsFromFile("Service Name Override with Import File K8s",onapBlueprint,input.getImportPath());


        Map<String, LinkedHashMap<String, Object>> k8sBpInputs = onapBlueprint.getInputs();
        assertNotNull("Service Name Override with Import File K8s Blueprint Inputs Section is NULL", k8sBpInputs);
        assertTrue("Service Name Override with Import File K8s Blueprint Inputs Section is Empty", k8sBpInputs.size() > 0);

        assertEquals("Service Name Override with Import File K8s Blueprint:Inputs " + Constants.ONAP_INPUT_CPU_LIMIT + " Default is not matching", k8sBpInputs.get(Constants.ONAP_INPUT_CPU_LIMIT).get("default"), Constants.ONAP_DEFAULT250m);
        assertEquals("Service Name Override with Import File K8s Blueprint:Inputs " + Constants.SERVICE_COMPONENT_NAME_OVERRIDE + " Default is not matching", k8sBpInputs.get(Constants.SERVICE_COMPONENT_NAME_OVERRIDE).get("default"), Constants.ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT);

        Map<String, Node> k8sBpNodeTemplates = onapBlueprint.getNode_templates();
        assertNotNull("Service Name Override with Import File K8s Blueprint Node Templates Section is NULL in the K8s DMAAP Blueprint", k8sBpNodeTemplates);
        assertTrue("Service Name Override with Import File K8s Blueprint Node Templates Section Size is Empty", k8sBpNodeTemplates.size() > 0);

        assertEquals("Service Name Override with Import File K8s Blueprint:NodeTemplates:Type is not Matching", k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES).getType(), Constants.ONAP_NODETEMPLATES_TYPE);

        Properties k8sBpNodeTemplateProperties = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getProperties();
        assertNotNull("Service Name Override with Import File K8s Blueprint Node Templates:Properties Section is NULL", k8sBpNodeTemplateProperties);


        Map<String, LinkedHashMap<String, Object>> bpInputs = onapBlueprint.getInputs();
        onapTestUtils.verifyArtifacts("Service Name Override with Import File K8s",onapComponentSpec,bpInputs,"o");

        onapTestUtils.verifyStreamsPublishes("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyStreamsSubscribes("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyAuxilary("Service Name Override with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyHealthCheck("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyServicesCalls("Service Name Override with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyServicesProvides("Service Name Override with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyDockerConfig("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyParameters("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplates);
        onapTestUtils.verifyVolumes("Service Name Override with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
    }

    @DisplayName("Testing K8s Blueprint with DB")
    @Test
    public void testDMAAPK8sBlueprintWithDB() {
        Input input = onapTestUtils.getInput(Paths.get("src", "test", "resources", "componentspecs", ves).toFile().getAbsolutePath(), outputfilesPath, "testDMAAPK8sBlueprintWithDB", "", "d", "");
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
        assertNotNull("K8s Blueprint for DMAAP Component Spec is NULL", onapComponentSpec);

        OnapBlueprint onapBlueprint = dmaapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);
        blueprintService.blueprintToYaml(onapComponentSpec, onapBlueprint, input);
    }

    @DisplayName("Testing K8s Blueprint for DMAAP Component Spec Input File")
    @Test
    public void testDMAAPK8sBlueprint() {
        Input input = onapTestUtils.getInput(Paths.get("src", "test", "resources", "componentspecs" , ves).toFile().getAbsolutePath(), outputfilesPath, "testDMAAPK8sBlueprint", "", "d", "");
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
        assertNotNull("K8s Blueprint for DMAAP Component Spec is NULL", onapComponentSpec);

        OnapBlueprint onapBlueprint = dmaapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);
        blueprintService.blueprintToYaml(onapComponentSpec, onapBlueprint, input);
        System.out.println(blueprintService.blueprintToString(onapComponentSpec, onapBlueprint, input));

        policyModelService.createPolicyModels(onapComponentSpec.getParameters(), "models");

        onapTestUtils.verifyToscaDefVersion("DMAAP K8s",onapBlueprint, Constants.TOSCA_DEF_VERSION);
        //onapTestUtils.verifyBpImports("DMAAP K8s",onapBlueprint,false);

        Map<String, LinkedHashMap<String, Object>> k8sBpInputs = onapBlueprint.getInputs();
        assertNotNull("DMAAP K8s Blueprint Inputs Section is NULL", k8sBpInputs);
        assertTrue("DMAAP K8s Blueprint Inputs Section is Empty", k8sBpInputs.size() > 0);

        //assertEquals("DMAAP K8s Blueprint:Inputs " + Constants.ALWAYS_PULL_IMAGE + " Type is not matching", k8sBpInputs.get(Constants.ALWAYS_PULL_IMAGE).get("type"), "boolean");
        //assertEquals("DMAAP K8s Blueprint:Inputs Always Pull Image  Default is not matching", k8sBpInputs.get(Constants.ALWAYS_PULL_IMAGE).get("default"), Constants.ALWAYS_PULL_IMAGE_DEFAULT);
        assertEquals("DMAAP K8s Blueprint:Inputs " + Constants.ONAP_INPUT_CPU_LIMIT + " Default is not matching", k8sBpInputs.get(Constants.ONAP_INPUT_CPU_LIMIT).get("default"), Constants.ONAP_DEFAULT250m);
        assertEquals("DMAAP K8s Blueprint:Inputs " + Constants.SERVICE_COMPONENT_NAME_OVERRIDE + " Default is not matching", k8sBpInputs.get(Constants.SERVICE_COMPONENT_NAME_OVERRIDE).get("default"), Constants.ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT);

        Map<String, Node> k8sBpNodeTemplates = onapBlueprint.getNode_templates();
        assertNotNull("DMAAP K8s Blueprint Node Templates Section is NULL in the K8s DMAAP Blueprint", k8sBpNodeTemplates);
        assertTrue("DMAAP K8s Blueprint Node Templates Section Size is Empty", k8sBpNodeTemplates.size() > 0);

        assertEquals("DMAAP K8s Blueprint:NodeTemplates:Type is not Matching", k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES).getType(), Constants.DMAAP_NODETEMPLATES_TYPE);

        Properties k8sBpNodeTemplateProperties = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getProperties();
        assertNotNull("DMAAP K8s Blueprint Node Templates:Properties Section is NULL", k8sBpNodeTemplateProperties);

        List bpNodeTemplateRelationships = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getRelationships();
        assertNotNull("DMAAP K8s Blueprint Node Templates:Relationships Section is NULL", bpNodeTemplateRelationships);

        onapTestUtils.verifyStreamsPublishes("DMAAP K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyStreamsSubscribes("DMAAP K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyAuxilary("DMAAP K8s",onapComponentSpec);
        onapTestUtils.verifyHealthCheck("DMAAP K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyServicesCalls("DMAAP K8s",onapComponentSpec);
        onapTestUtils.verifyServicesProvides("DMAAP K8s",onapComponentSpec);
        onapTestUtils.verifyDockerConfig("DMAAP K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyParameters("DMAAP K8s",onapComponentSpec,k8sBpNodeTemplates);
    }

    @DisplayName("Testing K8s Blueprint for DMAAP Component Spec Input File with Import File")
    @Test
    public void testDMAAPK8sImportFileBlueprint() throws IOException {
        Input input = onapTestUtils.getInput(Paths.get("src", "test", "resources", "componentspecs" , ves).toFile().getAbsolutePath(), "", "", Paths.get("src", "test", "resources", "componentspecs" , testImports).toFile().getAbsolutePath(), "d", "");
        onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
        assertNotNull("K8s Blueprint for DMAAP Component Spec with Import File is NULL", onapComponentSpec);

        OnapBlueprint onapBlueprint = dmaapBlueprintCreatorService.createBlueprint(onapComponentSpec, input);

        onapTestUtils.verifyToscaDefVersion("DMAAP with Import File K8s",onapBlueprint, Constants.TOSCA_DEF_VERSION);
        onapTestUtils.verifyBpImportsFromFile("DMAAP with Import File K8s",onapBlueprint,input.getImportPath());

        Map<String, LinkedHashMap<String, Object>> k8sBpInputs = onapBlueprint.getInputs();
        assertNotNull("DMAAP with Import File K8s Blueprint Inputs Section is NULL", k8sBpInputs);
        assertTrue("DMAAP with Import File K8s Blueprint Inputs Section is Empty", k8sBpInputs.size() > 0);

        //assertEquals("DMAAP with Import File K8s Blueprint:Inputs " + Constants.ALWAYS_PULL_IMAGE + " Type is not matching", k8sBpInputs.get(Constants.ALWAYS_PULL_IMAGE).get("type"), "boolean");
        //assertEquals("DMAAP with Import File K8s Blueprint:Inputs Always Pull Image  Default is not matching", k8sBpInputs.get(Constants.ALWAYS_PULL_IMAGE).get("default"), Constants.ALWAYS_PULL_IMAGE_DEFAULT);
        assertEquals("DMAAP with Import File K8s Blueprint:Inputs " + Constants.ONAP_INPUT_CPU_LIMIT + " Default is not matching", k8sBpInputs.get(Constants.ONAP_INPUT_CPU_LIMIT).get("default"), Constants.ONAP_DEFAULT250m);
        assertEquals("DMAAP with Import File K8s Blueprint:Inputs " + Constants.SERVICE_COMPONENT_NAME_OVERRIDE + " Default is not matching", k8sBpInputs.get(Constants.SERVICE_COMPONENT_NAME_OVERRIDE).get("default"), Constants.ONAP_SERVICE_COMPONENTNAME_OVERRIDE_DEFAULT);

        Map<String, Node> k8sBpNodeTemplates = onapBlueprint.getNode_templates();
        assertNotNull("DMAAP with Import File K8s Blueprint Node Templates Section is NULL in the K8s DMAAP Blueprint", k8sBpNodeTemplates);
        assertTrue("DMAAP with Import File K8s Blueprint Node Templates Section Size is Empty", k8sBpNodeTemplates.size() > 0);

        assertEquals("DMAAP with Import File K8s Blueprint:NodeTemplates:Type is not Matching", k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES).getType(), Constants.DMAAP_NODETEMPLATES_TYPE);

        Properties k8sBpNodeTemplateProperties = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getProperties();
        assertNotNull("DMAAP with Import File K8s Blueprint Node Templates:Properties Section is NULL", k8sBpNodeTemplateProperties);

        List bpNodeTemplateRelationships = ((Node) k8sBpNodeTemplates.get(Constants.ONAP_NODETEMPLATES)).getRelationships();
        assertNotNull("DMAAP with Import File K8s Blueprint Node Templates:Relationships Section is NULL", bpNodeTemplateRelationships);

        onapTestUtils.verifyStreamsPublishes("DMAAP with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyStreamsSubscribes("DMAAP with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyAuxilary("DMAAP with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyHealthCheck("DMAAP with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyServicesCalls("DMAAP with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyServicesProvides("DMAAP with Import File K8s",onapComponentSpec);
        onapTestUtils.verifyDockerConfig("DMAAP with Import File K8s",onapComponentSpec,k8sBpNodeTemplateProperties);
        onapTestUtils.verifyParameters("DMAAP with Import File K8s",onapComponentSpec,k8sBpNodeTemplates);
    }

}



