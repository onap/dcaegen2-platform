/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.runtime.web.configuration;

import org.onap.dcae.runtime.core.FlowGraphParser;
import org.onap.dcae.runtime.core.blueprint_creator.BlueprintCreatorOnap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BlueprintCreatorConfig {

    @Autowired
    Environment env;

    @Value("${onap.import.cloudifyPlugin}")
    String onapDublinImportCloudifyPlugin;

    @Value("${onap.import.k8sPlugin}")
    String onapDublinImportK8sPlugin;

    @Value("${onap.import.policyPlugin}")
    String onapDublinImportPolicyPlugin;

    @Value("${onap.import.postgresPlugin}")
    String onapDublinImportPostgresPlugin;

    @Value("${onap.import.clampPlugin}")
    String onapDublinImportClampPlugin;

    @Value("${onap.import.dmaapPlugin}")
    String onapDublinImportDmaapPlugin;


    @Profile("onap_dublin")
    @Primary
    @Bean
    public FlowGraphParser getFlowGraphParserForOnapDublin(){
        BlueprintCreatorOnap blueprintCreatorOnap = new BlueprintCreatorOnap();
        blueprintCreatorOnap.setImportFilePath(writeImportsTofile());
        FlowGraphParser flowGraphParser = new FlowGraphParser(blueprintCreatorOnap);
        return flowGraphParser;
    }

    private String writeImportsTofile() {
        String contentToWrite = getContentToWrite();
        String fielPath = "";
        try {
            Path path = createDataImportDirAndImportFile();
            fielPath = Files.write(path,contentToWrite.getBytes()).toString();
            new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(fielPath);
        return fielPath;
    }

    private Path createDataImportDirAndImportFile() {
        Path importDirPath = Paths.get("./data/imports").toAbsolutePath().normalize();
        Path onapImportFilePath = Paths.get("./data/imports/onapImports.yaml").toAbsolutePath().normalize();
        try {
            Files.createDirectories(importDirPath);
            Files.createFile(onapImportFilePath);
        }
        catch (FileAlreadyExistsException ignored){
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return onapImportFilePath;
    }

    private String getContentToWrite() {
        Map<String,Object> result = new HashMap<String, Object>();
        List<String> importList = new ArrayList<String>();
        importList.add(onapDublinImportCloudifyPlugin);
        importList.add(onapDublinImportK8sPlugin);
        importList.add(onapDublinImportPolicyPlugin);

        importList.add(onapDublinImportPostgresPlugin);
        importList.add(onapDublinImportClampPlugin);
        importList.add(onapDublinImportDmaapPlugin);
                
    
   
        result.put("imports",importList);
        return new Yaml().dump(result);
    }

}
