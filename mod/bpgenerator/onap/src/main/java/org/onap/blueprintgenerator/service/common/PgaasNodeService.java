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

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.exception.DatabasesNotFoundException;
import org.onap.blueprintgenerator.model.common.*;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to add Pgaas Node
 */


@Service
public class PgaasNodeService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    // method to create Pgaas Nodes and Inputs for Databases
    public void addPgaasNodesAndInputs(OnapComponentSpec onapComponentSpec, Map<String, Node> nodeTemplate, Map<String, LinkedHashMap<String, Object>> inputs)  {
        Map<String, String> databases = onapComponentSpec.getAuxilary().getDatabases();
        if(databases == null)
            throw new DatabasesNotFoundException("databases section not found in componentspec");
        for(Map.Entry<String, String> database : databases.entrySet()){
            addPgaasNode(database, nodeTemplate);
            addPgaasInputs(database, inputs);
        }
    }

    private void addPgaasInputs(Map.Entry<String, String> database, Map<String, LinkedHashMap<String, Object>> inputs) {
        inputs.put(database.getKey() + Constants.NAME_POSTFIX, blueprintHelperService.createStringInput( "db name", ""));
        inputs.put(database.getKey() + Constants.WRITER_FQDN_POSTFIX, blueprintHelperService.createStringInput( "db writerfqdn", ""));
    }

    private void addPgaasNode(Map.Entry<String, String> database, Map<String, Node> nodeTemplate) {
        PgaasNode pgaasNode = new PgaasNode();
        String dbName = database.getKey();
        pgaasNode.setType(Constants.PGAAS_NODE_TYPE);
        pgaasNode.setPgaasNodeProperties(buildPgaasNodeProperties(dbName));
        nodeTemplate.put(dbName + Constants.PGAAS_NODE_NAME_POSTFIX , pgaasNode);
    }

    private PgaasNodeProperties buildPgaasNodeProperties(String dbName) {
        PgaasNodeProperties pgaasNodeProperties = new PgaasNodeProperties();

        GetInput nameValue = new GetInput();
        nameValue.setBpInputName(dbName + Constants.NAME_POSTFIX);
        pgaasNodeProperties.setName(nameValue);

        GetInput writerfqdnValue = new GetInput();
        writerfqdnValue.setBpInputName(dbName + Constants.WRITER_FQDN_POSTFIX);
        pgaasNodeProperties.setWriterfqdn(writerfqdnValue);

        pgaasNodeProperties.setUseExisting(Constants.USE_EXISTING);

        return pgaasNodeProperties;
    }

    // method to create Pgaas Node Relationships for Databases
    public List<Map<String, String>> getPgaasNodeRelationships(OnapComponentSpec onapComponentSpec) {
        List<Map<String, String>> relationships = new ArrayList<>();
        for(Map.Entry<String, String> database : onapComponentSpec.getAuxilary().getDatabases().entrySet()){
            Map<String, String> relationship = new LinkedHashMap<>();
            relationship.put("type", Constants.DB_RELATIONSHIP_TYPE);
            relationship.put("target", database.getKey() + Constants.PGAAS_NODE_NAME_POSTFIX);
            relationships.add(relationship);
        }
        return relationships;
    }

    // method to create Env Variables for Databases
    public Map<String, Object> getEnvVariables(Map<String, String> databases) {
        Map<String, Object> envVariables = new LinkedHashMap<>();
        for(Map.Entry<String, String> database : databases.entrySet()){
            String name = database.getKey().toUpperCase();
            envVariables.put("<<", "*envs");

            GetInput nameValue = new GetInput();
            nameValue.setBpInputName(name.toLowerCase() + Constants.NAME_POSTFIX);
            envVariables.put(name + "_DB_NAME", nameValue);

            GetAttribute adminHostValue = buildGetAttributeValue(name.toLowerCase(), "admin", "host");
            envVariables.put( name.toUpperCase() + "_DB_ADMIN_HOST", adminHostValue);

            GetAttribute adminUserValue = buildGetAttributeValue(name.toLowerCase(), "admin", "user");
            envVariables.put( name.toUpperCase() + "_DB_ADMIN_USER", adminUserValue);

            GetAttribute adminPasswordValue = buildGetAttributeValue(name.toLowerCase(), "admin", "password");
            envVariables.put( name.toUpperCase() + "_DB_ADMIN_PASS", adminPasswordValue);
        }
        return envVariables;
    }

    private GetAttribute buildGetAttributeValue(String dbName, String owner, String type) {
        GetAttribute attribute = new GetAttribute();
        attribute.setAttribute(Arrays.asList(dbName + Constants.PGAAS_NODE_NAME_POSTFIX, owner, type));
        return attribute;
    }
}
