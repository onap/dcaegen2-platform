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

package com.att.blueprintgenerator.service.common;

import com.att.blueprintgenerator.exception.ComponentSpecException;
import com.att.blueprintgenerator.model.componentspec.OnapComponentSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to create Component Spec from File
 */


@Service("onapComponentSpecService")
public class ComponentSpecService {

    @Qualifier("objectMapper")
    @Autowired
    private ObjectMapper componentMapper;

    @Qualifier("yamlObjectMapper")
    @Autowired
    private ObjectMapper yamlComponentMapper;

    public OnapComponentSpec createComponentSpecFromFile(String componentSpecPath) {
        OnapComponentSpec componentSpec;
        try {
            if(!componentSpecPath.endsWith(".json"))
                componentMapper = yamlComponentMapper;
            componentSpec = componentMapper.readValue(new File(componentSpecPath), OnapComponentSpec.class);
        } catch (Exception ex) {
            throw new ComponentSpecException("Unable to create ONAP Component Spec from the input file: "+ componentSpecPath, ex);
        }
        return componentSpec;
    }

}
