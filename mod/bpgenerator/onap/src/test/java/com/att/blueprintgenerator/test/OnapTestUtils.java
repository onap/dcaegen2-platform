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

package com.att.blueprintgenerator.test;

import com.att.blueprintgenerator.constants.Constants;
import com.att.blueprintgenerator.model.base.Blueprint;
import com.att.blueprintgenerator.model.common.Input;
import com.att.blueprintgenerator.model.common.Node;
import com.att.blueprintgenerator.model.common.Properties;
import com.att.blueprintgenerator.model.componentspec.OnapAuxilary;
import com.att.blueprintgenerator.model.componentspec.OnapComponentSpec;
import com.att.blueprintgenerator.model.componentspec.common.*;
import com.att.blueprintgenerator.model.dmaap.Streams;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Test Utilities used in Test Suite and Test Cases
 */

@Component
@Ignore
public class OnapTestUtils extends BlueprintGeneratorTests {

    @Value("${imports.onap.types}")
    private String importsOnapTypes;

    @Value("${imports.onap.K8s.plugintypes}")
    private String importsOnapK8sPlugintypes;

    @Value("${imports.onap.K8s.dcaepolicyplugin}")
    private String importsOnapK8sDcaepolicyplugin;

    @Value("${imports.dmaap.types}")
    private String importsDmaapTypes;

    @Value("${imports.dmaap.K8s.plugintypes}")
    private String importsDmaapK8sPlugintypes;

    @Value("${imports.dmaap.dmaapplugin}")
    private String importsDmaapDmaapplugin;



    public Input getInput(String componentSpecPath,String outputPath,String bluePrintName,String importPath,String bpType,String serviceNameOverride){
        Input input = new Input();
        input.setComponentSpecPath(componentSpecPath);
        input.setBluePrintName(bluePrintName);
        input.setOutputPath(outputPath);
        input.setBpType(bpType);
        input.setServiceNameOverride(serviceNameOverride);
        input.setImportPath(importPath);
        return input;
    }

    public void verifyToscaDefVersion(String type,Blueprint blueprint,String toscaDefVersion){
        String bpToscaDefVersion = blueprint.getTosca_definitions_version();
        assertNotNull(type + " TOSCA Definition Version is NULL", bpToscaDefVersion);
        assertEquals(type + " TOSCA Definition Version is not Matching", bpToscaDefVersion, toscaDefVersion);
    }

    public void verifyBpImports(String type,Blueprint blueprint, boolean validateimps) {
        String[] bpImports = blueprint.getImports().toArray(new String[blueprint.getImports().size()]);
        if (validateimps) {
            String[] testImports = {importsDmaapTypes,importsDmaapK8sPlugintypes,importsDmaapDmaapplugin};
            assertArrayEquals(type + " Blueprint Imports is not matching with default Dmaap K8s Blueprint imports", bpImports, testImports);
        }
        else{
            String[] testImports = {importsOnapTypes,importsOnapK8sPlugintypes,importsOnapK8sDcaepolicyplugin};
            assertArrayEquals(type + " Blueprint Imports is not matching with default Onap K8s Blueprint imports", bpImports, testImports);
        }

    }

    public void verifyBpImportsFromFile(String type,Blueprint blueprint, String importPath) throws IOException {
        Blueprint importFileRead = yamlObjectMapper.readValue(new File(importPath), Blueprint.class);
        String[] importFileImports = importFileRead.getImports().toArray(new String[importFileRead.getImports().size()]);
        String[] bpImports = blueprint.getImports().toArray(new String[blueprint.getImports().size()]);
        assertArrayEquals(type + " Blueprint Imports is not matching with default Dmaap K8s Blueprint imports", bpImports, importFileImports);

    }

    public void verifyStreamsPublishes(String type, OnapComponentSpec onapComponentSpec, Properties nodeTemplateProperties) {
        List<Streams> streamsPublishes = nodeTemplateProperties.getStreams_publishes();
        if (!(streamsPublishes == null)) {
            assertNotNull(type + " Blueprint:NodeTemplates:Properties:StreamsPublishes is NULL", streamsPublishes);
            assertTrue(type + " Blueprint:NodeTemplates:Properties:StreamsPublishes Section Size is 0", streamsPublishes.size() > 0);
            assertEquals(type + " Blueprint:NodeTemplates:Properties:StreamsPublishes is Not Matching", streamsPublishes.get(0).getType(), Constants.MESSAGEROUTER_VALUE);
            assertEquals(type + " Blueprint:NodeTemplates:Properties:StreamsPublishes is Not Matching", streamsPublishes.get(1).getType(), Constants.MESSAGEROUTER_VALUE);
        }
    }

    public void verifyStreamsSubscribes(String type,OnapComponentSpec onapComponentSpec, Properties nodeTemplateProperties){
        List<Streams> streamsSubscribes = nodeTemplateProperties.getStreams_subscribes();
        if (!(streamsSubscribes == null)) {
            assertNotNull(type + " Blueprint:NodeTemplates:Properties:StreamsSubscribes is NULL", streamsSubscribes);
            assertTrue(type + " Blueprint:NodeTemplates:Properties:StreamsSubscribes Section Size is 0", streamsSubscribes.size() > 0);
            assertEquals(type + " Blueprint:NodeTemplates:Properties:StreamsSubscribes is Not Matching", streamsSubscribes.get(0).getType(), Constants.MESSAGE_ROUTER);
            assertEquals(type + " Blueprint:NodeTemplates:Properties:StreamsSubscribes is Not Matching", streamsSubscribes.get(1).getType(), Constants.DATA_ROUTER);
        }
    }

    public void verifyServicesCalls(String type, OnapComponentSpec onapComponentSpec){
        Calls[] csServicesCalls = onapComponentSpec.getServices().getCalls();
        assertNotNull(type + " ComponentSpec Services Calls is NULL", csServicesCalls);
        //assertTrue(type + " ComponentSpec Services Calls Section Size is 0", csServicesCalls.length > 0);
    }

    public void verifyServicesProvides(String type, OnapComponentSpec onapComponentSpec){
        Provides[] csServicesProvides = onapComponentSpec.getServices().getProvides();
        assertNotNull(type + " ComponentSpec Services Provides is NULL", csServicesProvides);
        assertTrue(type + " ComponentSpec Services Provides Section Size is 0", csServicesProvides.length > 0);
    }

    public void verifyDockerConfig(String type,OnapComponentSpec onapComponentSpec, Properties nodeTemplateProperties){
        OnapAuxilary dockerConfig =  nodeTemplateProperties.getDocker_config();
        assertNotNull(type +" Blueprint Docker Config Section is NULL", dockerConfig);
    }


    public void verifyParameters(String type, OnapComponentSpec onapComponentSpec, Map<String, Node> nodeTemplates) {
        Parameters[] csParameters = onapComponentSpec.getParameters();
        assertNotNull(type +" ComponentSpec Parameters Section is NULL", csParameters);
        assertTrue(type + " ComponentSpec Parameters Section Size is 0", csParameters.length > 0);
    }

    public void verifyAuxilary(String type, OnapComponentSpec onapComponentSpec){
        OnapAuxilary csAuxilary = onapComponentSpec.getAuxilary();
        assertNotNull(type +" ComponentSpec Auxilary Section is NULL", csAuxilary);
    }

    public void verifyHealthCheck(String type,OnapComponentSpec onapComponentSpec, Properties nodeTemplateProperties){
        HealthCheck csAuxilaryHealthcheck = onapComponentSpec.getAuxilary().getHealthcheck();
        assertNotNull(type +" ComponentSpec Auxilary Health Check Section is NULL", csAuxilaryHealthcheck);
        HealthCheck healthCheck = nodeTemplateProperties.getDocker_config().getHealthcheck();
        assertNotNull(type + " Blueprint:NodeTemplates:DockerConfig:Healthcheck Section is NULL", healthCheck);
        assertEquals(type + " Blueprint:NodeTemplates:DockerConfig:Healthcheck:Interval Tag is not matching", healthCheck.getInterval(), csAuxilaryHealthcheck.getInterval());
        assertEquals(type + " Blueprint:NodeTemplates:DockerConfig:Healthcheck:Timeout Tag is not matching", healthCheck.getTimeout(), csAuxilaryHealthcheck.getTimeout());
        assertEquals(type + " Blueprint:NodeTemplates:DockerConfig:Healthcheck:Script Tag is not matching", healthCheck.getEndpoint(), csAuxilaryHealthcheck.getEndpoint());
        assertEquals(type + " Blueprint:NodeTemplates:DockerConfig:Healthcheck:Type Tag is not matching", healthCheck.getType(), csAuxilaryHealthcheck.getType());
    }

    public void verifyVolumes(String type,OnapComponentSpec onapComponentSpec, Properties nodeTemplateProperties){
        Volumes[] csAuxilaryVolumes = onapComponentSpec.getAuxilary().getVolumes();
        assertNotNull(type +" ComponentSpec Auxilary Live Health Check Section is NULL", csAuxilaryVolumes);
        Volumes[] onapVolumes = nodeTemplateProperties.getDocker_config().getVolumes();
        assertNotNull(type + " Blueprint:NodeTemplates:DockerConfig:LiveHealthcheck Section is NULL", onapVolumes);
    }

    public void verifyArtifacts(String type,OnapComponentSpec onapComponentSpec, Map<String, LinkedHashMap<String, Object>> inputs,String bptype){
        Artifacts[] csArtifacts = onapComponentSpec.getArtifacts();
        assertNotNull(type + " ComponentSpec Artifacts Section is NULL", csArtifacts);
        assertEquals(type + " Blueprint:Artifacts:image is not matching", ((String) inputs.get("image").get("default")), "\"" + csArtifacts[0].getUri() + "\"");
        //assertEquals(type + " Blueprint:Artifacts:image is not matching", ((String) inputs.get("tag_version").get("default")), "\"" + csArtifacts[0].getUri() + "\"");

    }

}