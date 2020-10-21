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

package org.onap.blueprintgenerator.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.blueprintgenerator.model.common.Imports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAp and DCAE Blueprint Applications
 * Service: For Imports
 */

@Service
public class ImportsService {

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

    @Value("${import.Postgres}")
    private String importPostgres;

    @Value("${import.Clamp}")
    private String importClamp;

    @Value("${mrTopic.URL}")
    private String mrTopicURL;

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    public List<String> createImports(String bpType) {
        List<String> imports = new ArrayList<>();
        if (bpType.equals("o")) {
            imports.add(importsOnapTypes);
            imports.add(importsOnapK8sPlugintypes);
            imports.add(importsOnapK8sDcaepolicyplugin);
            imports.add(importPostgres);
            imports.add(importClamp);
            imports.add(mrTopicURL);
        }
        else {
            imports.add(importsDmaapTypes);
            imports.add(importsDmaapK8sPlugintypes);
            imports.add(importsDmaapDmaapplugin);
            imports.add(importPostgres);
            imports.add(importClamp);
            imports.add(mrTopicURL);
        }
        return imports;
    }

    public List<String> createImportsFromFile(String path) throws IOException {
        File importPath = new File(path);
        Imports imports = yamlObjectMapper.readValue(importPath, Imports.class);
        imports.getImports().removeIf(String::isBlank);
        return imports.getImports();
    }

}
