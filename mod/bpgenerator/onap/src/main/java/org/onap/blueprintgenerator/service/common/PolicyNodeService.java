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
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.Node;
import org.onap.blueprintgenerator.model.common.PolicyNode;
import org.onap.blueprintgenerator.model.common.PolicyNodeProperties;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.TypePolicy;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to add Policy Node
 */
@Service("onapPolicyNodeService")
public class PolicyNodeService {

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    /**
     * Creates Policy Nodes and Inputs
     *
     * @param onapComponentSpec OnapComponentSpec
     * @param nodeTemplate Node Template
     * @param inputs Inputs
     * @return
     */
    public void addPolicyNodesAndInputs(
        OnapComponentSpec onapComponentSpec,
        Map<String, Node> nodeTemplate,
        Map<String, LinkedHashMap<String, Object>> inputs) {
        List<TypePolicy> policyList = onapComponentSpec.getPolicyInfo().getTypePolicyList();
        for (TypePolicy policy : policyList) {
            addPolicyNodesToNodeTemplate(policy, nodeTemplate);
            addPolicyInputs(policy, inputs);
        }
    }

    private void addPolicyInputs(
        TypePolicy policy, Map<String, LinkedHashMap<String, Object>> inputs) {
        String defaultValue = policy.getPolicy_id();
        defaultValue = defaultValue != null ? defaultValue : "";
        inputs.put(
            policy.getNode_label() + "_policy_id",
            blueprintHelperService.createStringInput("policy_id", defaultValue));
    }

    private void addPolicyNodesToNodeTemplate(TypePolicy policy, Map<String, Node> nodeTemplate) {
        PolicyNode policyNode = new PolicyNode();
        policyNode.setType(Constants.POLICY_NODE_TYPE);
        policyNode.setPolicyNodeProperties(getPolicyNodeProperties(policy));
        nodeTemplate.put(policy.getNode_label(), policyNode);
    }

    private PolicyNodeProperties getPolicyNodeProperties(TypePolicy policy) {
        PolicyNodeProperties policyNodeProperties = new PolicyNodeProperties();
        GetInput policyIdGetInput = new GetInput();
        policyIdGetInput.setBpInputName(policy.getNode_label() + "_policy_id");
        policyNodeProperties.setPolicyId(policyIdGetInput);
        policyNodeProperties.setPolicyModelId(policy.getPolicy_model_id());
        return policyNodeProperties;
    }

    /**
     * Creates Policy Relationships
     *
     * @param onapComponentSpec OnapComponentSpec
     * @return
     */
    public List<Map<String, String>> getPolicyRelationships(OnapComponentSpec onapComponentSpec) {
        List<Map<String, String>> relationships = new ArrayList<>();
        List<TypePolicy> policyList = onapComponentSpec.getPolicyInfo().getTypePolicyList();
        for (TypePolicy policy : policyList) {
            Map<String, String> relationship = new LinkedHashMap<>();
            relationship.put("type", Constants.POLICY_RELATIONSHIP_TYPE);
            relationship.put("target", policy.getNode_label());
            relationships.add(relationship);
        }
        return relationships;
    }
}
