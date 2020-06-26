/**============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.blueprint.Node;
import org.onap.blueprintgenerator.models.blueprint.policy.PolicyNode;
import org.onap.blueprintgenerator.models.blueprint.policy.PolicyNodeProperties;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.policy_info.TypePolicy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class PolicyNodeBuilder {

    private static final String POLICY_NODE_TYPE = "clamp.nodes.policy";
    private static final String POLICY_RELATIONSHIP_TYPE = "cloudify.relationships.depends_on";

    public static void addPolicyNodesAndInputs(ComponentSpec cs, TreeMap<String, Node> nodeTemplate, TreeMap<String, LinkedHashMap<String, Object>> inps) {
        List<TypePolicy> policyList = cs.getPolicyInfo().getTypePolicyList();
        for(TypePolicy policy: policyList){
            addPolicyNodesToNodeTemplate(policy, nodeTemplate);
            addPolicyInputs(policy, inps);
        }
    }

    private static void addPolicyInputs(TypePolicy policy, TreeMap<String, LinkedHashMap<String, Object>> inps) {
        String defaultValue = policy.getPolicy_id() != null ? policy.getPolicy_id() : "";
        inps.put(policy.getNode_label() + "_policy_id", getInputValue("string", "policy_id", defaultValue));
    }

    private static LinkedHashMap<String, Object> getInputValue(String type, String description, Object defaultValue) {
        LinkedHashMap<String, Object> inputValueMap = new LinkedHashMap();
        inputValueMap.put("type", type);
        inputValueMap.put("description", description);
        inputValueMap.put("default", defaultValue);
        return  inputValueMap;
    }

    private static void addPolicyNodesToNodeTemplate(TypePolicy policy, TreeMap<String, Node> nodeTemplate) {
        PolicyNode policyNode = new PolicyNode();
        policyNode.setType(POLICY_NODE_TYPE);
        policyNode.setPolicyNodeProperties(getPolicyNodeProperties(policy));
        nodeTemplate.put(policy.getNode_label(), policyNode);
    }

    private static PolicyNodeProperties getPolicyNodeProperties(TypePolicy policy) {
        PolicyNodeProperties policyNodeProperties = new PolicyNodeProperties();

        GetInput policyIdGetInput = new GetInput();
        policyIdGetInput.setBpInputName(policy.getNode_label() + "_policy_id");
        policyNodeProperties.setPolicyId(policyIdGetInput);

        policyNodeProperties.setPolicyModelId(policy.getPolicy_model_id());

        return policyNodeProperties;
    }

    public static ArrayList<LinkedHashMap<String, String>> getPolicyRelationships(ComponentSpec cs) {
        ArrayList<LinkedHashMap<String, String>> relationships = new ArrayList<>();
        List<TypePolicy> policyList = cs.getPolicyInfo().getTypePolicyList();
        for(TypePolicy policy: policyList){
            LinkedHashMap<String, String> relationship = new LinkedHashMap<>();
            relationship.put("type", POLICY_RELATIONSHIP_TYPE);
            relationship.put("target", policy.getNode_label());
            relationships.add(relationship);
        }
        return relationships;
    }
}
