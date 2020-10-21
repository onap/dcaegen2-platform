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

package com.att.blueprintgenerator.service;


import com.att.blueprintgenerator.constants.Constants;
import com.att.blueprintgenerator.exception.BlueprintException;
import com.att.blueprintgenerator.model.common.Input;
import com.att.blueprintgenerator.model.common.Node;
import com.att.blueprintgenerator.model.common.OnapBlueprint;
import com.att.blueprintgenerator.model.componentspec.OnapComponentSpec;
import com.att.blueprintgenerator.service.base.BlueprintService;
import com.att.blueprintgenerator.service.common.NodeService;
import com.att.blueprintgenerator.service.common.PgaasNodeService;
import com.att.blueprintgenerator.service.common.PolicyNodeService;
import com.att.blueprintgenerator.service.common.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Service to create ONAP Blueprint
 */

@Service
public class OnapBlueprintService extends BlueprintService {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PolicyNodeService policyNodeService;

    @Autowired
    private PgaasNodeService pgaasNodeService;

    @Autowired
    private QuotationService quotationService;

    public OnapBlueprint createBlueprint(OnapComponentSpec onapComponentSpec, Input input) {
        try {
            OnapBlueprint blueprint = new OnapBlueprint();
            blueprint.setTosca_definitions_version(Constants.TOSCA_DEF_VERSION);

            //if (!"".equals(input.getImportPath()))
            if (input.getImportPath() != null)
                blueprint.setImports(importsService.createImportsFromFile(input.getImportPath()));
            else
                blueprint.setImports(importsService.createImports(input.getBpType()));

            Map<String, Node> nodeTemplate = new TreeMap<>();
            String nodeName = onapComponentSpec.getSelf().getName();
            Map<String, LinkedHashMap<String, Object>> inputs = new TreeMap<>();

            Map<String, Object> onapNodeResponse = nodeService.createOnapNode(inputs, onapComponentSpec, input.getServiceNameOverride());
            inputs = (Map<String, LinkedHashMap<String, Object>>) onapNodeResponse.get("inputs");
            nodeTemplate.put(nodeName, (Node) onapNodeResponse.get("onapNode"));
            blueprint.setNode_templates(nodeTemplate);

            if (onapComponentSpec.getPolicyInfo() != null)
                policyNodeService.addPolicyNodesAndInputs(onapComponentSpec, nodeTemplate, inputs);

            if (onapComponentSpec.getAuxilary().getDatabases() != null)
                pgaasNodeService.addPgaasNodesAndInputs(onapComponentSpec, nodeTemplate, inputs);

            blueprint.setInputs(inputs);

            return quotationService.setQuotations(blueprint);
        } catch (Exception ex) {
            throw new BlueprintException("Unable to create ONAP Blueprint Object from given input parameters", ex);
        }
    }


}



