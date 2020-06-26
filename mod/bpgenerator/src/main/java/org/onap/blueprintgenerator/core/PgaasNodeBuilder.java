/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.onap.blueprintgenerator.exception.DatabasesNotFoundException;
import org.onap.blueprintgenerator.models.GetAttribute;
import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.blueprint.Node;
import org.onap.blueprintgenerator.models.blueprint.pgaas.PgaasNode;
import org.onap.blueprintgenerator.models.blueprint.pgaas.PgaasNodeProperties;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createInputValue;

public class PgaasNodeBuilder {

    private static final String PGAAS_NODE_TYPE = "dcae.nodes.pgaas.database";
    private static final String PGAAS_NODE_NAME_POSTFIX = "_pgaasdb";
    private static final String WRITER_FQDN_POSTFIX = "_database_writerfqdn";
    private static final String NAME_POSTFIX = "_database_name";
    private static final boolean USE_EXISTING = true;
    private static final String DB_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";


    public static void addPgaasNodesAndInputs(ComponentSpec cs, TreeMap<String, Node> nodeTemplate,
        TreeMap<String, LinkedHashMap<String, Object>> inps) {
        TreeMap<String, String> databases = cs.getAuxilary().getDatabases();
        if (databases == null) {
            throw new DatabasesNotFoundException("databases section not found in componentspec");
        }
        for (Map.Entry<String, String> database : databases.entrySet()) {
            addPgaasNode(database, nodeTemplate);
            addPgaasInputs(database, inps);
        }
    }

    private static void addPgaasInputs(Map.Entry<String, String> database,
        TreeMap<String, LinkedHashMap<String, Object>> inps) {
        inps.put(database.getKey() + NAME_POSTFIX, createInputValue("string", "db name", ""));
        inps.put(database.getKey() + WRITER_FQDN_POSTFIX, createInputValue("string", "db writerfqdn", ""));
    }

    private static void addPgaasNode(Map.Entry<String, String> database, TreeMap<String, Node> nodeTemplate) {
        PgaasNode pgaasNode = new PgaasNode();
        String dbName = database.getKey();
        pgaasNode.setType(PGAAS_NODE_TYPE);
        pgaasNode.setPgaasNodeProperties(buildPgaasNodeProperties(dbName));
        nodeTemplate.put(dbName + PGAAS_NODE_NAME_POSTFIX, pgaasNode);
    }

    private static PgaasNodeProperties buildPgaasNodeProperties(String dbName) {
        PgaasNodeProperties pgaasNodeProperties = new PgaasNodeProperties();

        GetInput nameValue = new GetInput();
        nameValue.setBpInputName(dbName + NAME_POSTFIX);
        pgaasNodeProperties.setName(nameValue);

        GetInput writerfqdnValue = new GetInput();
        writerfqdnValue.setBpInputName(dbName + WRITER_FQDN_POSTFIX);
        pgaasNodeProperties.setWriterfqdn(writerfqdnValue);

        pgaasNodeProperties.setUseExisting(USE_EXISTING);

        return pgaasNodeProperties;
    }

    public static ArrayList<LinkedHashMap<String, String>> getPgaasNodeRelationships(ComponentSpec cs) {
        ArrayList<LinkedHashMap<String, String>> relationships = new ArrayList<>();
        for (Map.Entry<String, String> database : cs.getAuxilary().getDatabases().entrySet()) {
            LinkedHashMap<String, String> relationship = new LinkedHashMap<>();
            relationship.put("type", DB_RELATIONSHIP_TYPE);
            relationship.put("target", database.getKey() + PGAAS_NODE_NAME_POSTFIX);
            relationships.add(relationship);
        }
        return relationships;
    }

    public static LinkedHashMap<String, Object> getEnvVariables(TreeMap<String, String> databases) {
        LinkedHashMap<String, Object> envVariables = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, String> database : databases.entrySet()) {
            String name = database.getKey().toUpperCase();

            envVariables.put("<<", "*envs");

            GetInput nameValue = new GetInput();
            nameValue.setBpInputName(name.toLowerCase() + NAME_POSTFIX);
            envVariables.put(name + "_DB_NAME", nameValue);

            GetAttribute adminHostValue = buildGetAttributeValue(name.toLowerCase(), "admin", "host");
            envVariables.put(name.toUpperCase() + "_DB_ADMIN_HOST", adminHostValue);

            GetAttribute adminUserValue = buildGetAttributeValue(name.toLowerCase(), "admin", "user");
            envVariables.put(name.toUpperCase() + "_DB_ADMIN_USER", adminUserValue);

            GetAttribute adminPasswordValue = buildGetAttributeValue(name.toLowerCase(), "admin", "password");
            envVariables.put(name.toUpperCase() + "_DB_ADMIN_PASS", adminPasswordValue);
        }
        return envVariables;
    }

    private static GetAttribute buildGetAttributeValue(String dbName, String owner, String type) {
        GetAttribute attribute = new GetAttribute();
        attribute.setAttribute(Arrays.asList(dbName + PGAAS_NODE_NAME_POSTFIX, owner, type));
        return attribute;
    }
}
