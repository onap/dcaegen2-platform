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

package org.onap.blueprintgenerator.service.onap;


import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.exception.BlueprintException;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.common.Node;
import org.onap.blueprintgenerator.model.common.OnapBlueprint;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.service.base.BlueprintService;
import org.onap.blueprintgenerator.service.common.ImportsService;
import org.onap.blueprintgenerator.service.common.NodeService;
import org.onap.blueprintgenerator.service.common.PgaasNodeService;
import org.onap.blueprintgenerator.service.common.PolicyNodeService;
import org.onap.blueprintgenerator.service.common.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
public class OnapBlueprintCreatorService {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PolicyNodeService policyNodeService;

    @Autowired
    private PgaasNodeService pgaasNodeService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    protected ImportsService importsService;

    // Method to generate Onap Blueprint
    public OnapBlueprint createBlueprint(OnapComponentSpec onapComponentSpec, Input input) {
        try {
            OnapBlueprint blueprint = new OnapBlueprint();
            blueprint.setTosca_definitions_version(Constants.TOSCA_DEF_VERSION);

            //if (!"".equals(input.getImportPath()))
            if (!StringUtils.isEmpty(input.getImportPath()))
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

            if (onapComponentSpec.getAuxilary() != null && onapComponentSpec.getAuxilary().getDatabases() != null)
                pgaasNodeService.addPgaasNodesAndInputs(onapComponentSpec, nodeTemplate, inputs);

            blueprint.setInputs(inputs);

            return quotationService.setQuotations(blueprint);
        } catch (Exception ex) {
            throw new BlueprintException("Unable to create ONAP Blueprint Object from given input parameters", ex);
        }
    }


}



