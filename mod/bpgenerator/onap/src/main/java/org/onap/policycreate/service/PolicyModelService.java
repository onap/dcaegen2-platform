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

package org.onap.policycreate.service;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.componentspec.common.Parameters;
import org.onap.policycreate.exception.PolicyCreateException;
import org.onap.policycreate.model.PolicyModel;
import org.onap.policycreate.model.PolicyModelNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Service for Policy Model to create Policy Models, Policy Group Names and Convert Policy to Yaml
 */


@Service("onapPolicyModelService")
public class PolicyModelService {

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    @Autowired
    private PolicyModelNodeService policyModelNodeService;

    // Method to create Policy Models
    public void createPolicyModels(Parameters[] params, String filePath) {
        try {
            List<String> policyGroups = getPolicyGroupNames(params);
            for (String s : policyGroups) {
                PolicyModel model = new PolicyModel();
                model.setTosca_definition_version(Constants.TOSCA_SIMPLE_YAML);

                Map<String, Object> response = policyModelNodeService.creatNodeType(s, params);
                String nodeTypeName = "onap.policy." + s;
                Map<String, PolicyModelNode> nodeType = new TreeMap<>();
                nodeType.put(nodeTypeName, (PolicyModelNode) response.get("policyModelNode"));
                model.setNode_types(nodeType);

                if (!"".equals(response.get("hasEntrySchema")))
                    model.setData_types(policyModelNodeService.createDataTypes((String) response.get("hasEntrySchema"), params));
                policyModelToYaml(filePath, model, s);
            }
        } catch (Exception ex) {
            throw new PolicyCreateException("Unable to create Policies from given input parameters", ex);
        }
    }

    private List<String> getPolicyGroupNames(Parameters[] params) {
        List<String> names = new ArrayList<>();
        for (Parameters p : params) {
            if (p.isPolicy_editable()) {
                if (names.isEmpty()) {
                    names.add(p.getPolicy_group());
                } else if (!names.contains(p.getPolicy_group())) {
                        names.add(p.getPolicy_group());
                }
            }
        }
        return names;
    }

    private void policyModelToYaml(String path, PolicyModel model, String name) throws IOException {
        File outputFile = new File(path, name + ".yml");
        outputFile.getParentFile().mkdirs();
        yamlObjectMapper.writeValue(outputFile, model);
    }

}

