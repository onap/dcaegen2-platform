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

package org.onap.blueprintgenerator.service.base;

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

    @Value("${imports.docker.types}")
    private String importsDockerTypes;

    @Value("${imports.docker.dockerplugin.node.type}")
    private String importsDockerDockerpluginNodetype;

    @Value("${imports.docker.dmaap}")
    private String importsDockerDmaap;

    @Value("${imports.docker.relationship.types}")
    private String importsDockerRelationshipTypes;

    @Value("${imports.docker.dcaepolicyplugin.node.type}")
    private String importsDockerDcaepolicypluginNodeType;

    @Value("${imports.docker.pgaas.types}")
    private String importsDockerPgaasTypes;

    @Value("${imports.K8s.types}")
    private String importsK8sTypes;

    @Value("${imports.K8s.node.type}")
    private String importsK8sNodetype;

    @Value("${imports.K8s.relationship.types}")
    private String importsK8sRelationshipTypes;

    @Value("${imports.K8s.dmaapplugin.node.type}")
    private String importsK8sDmaapPluginNodeType;

    @Value("${imports.K8s.dcaepolicyplugin.node.type}")
    private String importsK8sDcaepolicypluginNodeType;

    @Value("${imports.K8s.pgaas.types}")
    private String importsK8sPgaasTypes;

    @Value("${imports.eom.docker.types}")
    private String importsEOMDockerTypes;

    @Value("${imports.eom.docker.dockerplugin.node.type}")
    private String importsEOMDockerDockerpluginNodetype;

    @Value("${imports.eom.docker.dmaapplugin.node.type}")
    private String importsEOMDockerDmaappluginNodetype;

    @Value("${imports.eom.docker.relationship.types}")
    private String importsEOMDockerRelationshipTypes;

    @Value("${imports.eom.docker.dcaepolicyplugin.node.type}")
    private String importsEOMDockerDcaepolicypluginNodeType;

    @Value("${imports.eom.docker.pgaas.types}")
    private String importsEOMDockerPgaasTypes;

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

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    public List<String> createImports(String bpType) {
        List<String> imports = new ArrayList<>();
        if (bpType.equals("dti")) {
            imports.add(importsDockerTypes);
            imports.add(importsDockerDockerpluginNodetype);
            imports.add(importsDockerDmaap);
            imports.add(importsDockerRelationshipTypes);
            imports.add(importsDockerDcaepolicypluginNodeType);
            imports.add(importsDockerPgaasTypes);
        } else if (bpType.equals("k")) {
            imports.add(importsK8sTypes);
            imports.add(importsK8sNodetype);
            imports.add(importsK8sRelationshipTypes);
            imports.add(importsK8sDmaapPluginNodeType);
            imports.add(importsK8sDcaepolicypluginNodeType);
            imports.add(importsK8sPgaasTypes);
        } else if (bpType.equals("m")) {
            imports.add(importsEOMDockerTypes);
            imports.add(importsEOMDockerDockerpluginNodetype);
            imports.add(importsEOMDockerDmaappluginNodetype);
            imports.add(importsEOMDockerRelationshipTypes);
            imports.add(importsEOMDockerDcaepolicypluginNodeType);
            imports.add(importsEOMDockerPgaasTypes);
        }
        else if (bpType.equals("o")) {
            imports.add(importsOnapTypes);
            imports.add(importsOnapK8sPlugintypes);
            imports.add(importsOnapK8sDcaepolicyplugin);
        }
        else if (bpType.equals("d")) {
            imports.add(importsDmaapTypes);
            imports.add(importsDmaapK8sPlugintypes);
            imports.add(importsDmaapDmaapplugin);
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
