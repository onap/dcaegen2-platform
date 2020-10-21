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

package com.att.policycreate.service;

import com.att.blueprintgenerator.constants.Constants;
import com.att.blueprintgenerator.model.componentspec.common.EntrySchema;
import com.att.blueprintgenerator.model.componentspec.common.Parameters;
import com.att.blueprintgenerator.model.componentspec.common.PolicySchema;
import com.att.policycreate.model.PolicyModelNode;
import com.att.policycreate.model.PolicyProperties;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * To create Node Type, Data Type and Translate Entry Schema for Policy Model Node
 */


@Service("onapPolicyModelNodeService")
public class PolicyModelNodeService {

    public  Map<String,Object> creatNodeType(String policyName, Parameters[] params) {
        String hasEntrySchema = "";
        Map<String,Object> response = new HashMap<>();
        PolicyModelNode policyModelNode = new PolicyModelNode();

        Map<String, PolicyProperties> props = new TreeMap<>();
        for(Parameters p: params) {
            if(p.getPolicy_group() != null) {
                if(p.getPolicy_group().equals(policyName)) {
                    String name = p.getName();
                    String type = p.getType();
                    PolicyProperties polProps = new PolicyProperties();
                    if(p.getPolicy_schema() != null) {
                        polProps.setType("map");
                        ArrayList<String> entrySchema = new ArrayList<String>();
                        entrySchema.add("type: onap.datatypes." + name);
                        polProps.setEntry_schema(entrySchema);
                        hasEntrySchema = name;
                        props.put(name, polProps);
                    }
                    else {
                        polProps.setType(type);
                        props.put(name, polProps);
                    }
                }
            }
        }
        policyModelNode.setDerived_from(Constants.TOSCA_DATATYPES_ROOT);
        policyModelNode.setProperties(props);
        response.put("hasEntrySchema", hasEntrySchema);
        response.put("policyModelNode", policyModelNode);
        return response;
    }


    public Map<String, PolicyModelNode> createDataTypes(String param, Parameters[] parameters){
        Map<String, PolicyModelNode> dataType = new TreeMap<>();
        PolicyModelNode node = new PolicyModelNode();
        node.setDerived_from(Constants.TOSCA_DATATYPES_ROOT);

        Map<String, PolicyProperties> properties = new TreeMap<>();
        Parameters par = new Parameters();
        for(Parameters p: parameters) {
            if(p.getName().equals(param)) {
                par = p;
                break;
            }
        }

        for(PolicySchema pol: par.getPolicy_schema()) {
            if(pol.getEntry_schema() != null) {
                PolicyProperties prop =new PolicyProperties();
                prop.setType("map");
                ArrayList<String> schema = new ArrayList<String>();
                schema.add("type: onap.datatypes." + pol.getName());
                prop.setEntry_schema(schema);
                properties.put(pol.getName(), prop);
                dataType = translateEntrySchema(dataType, pol.getEntry_schema(), pol.getName());
            }
            else {
                PolicyProperties prop = new PolicyProperties();
                prop.setType(pol.getType());
                properties.put(pol.getName(), prop);
            }
        }

        node.setProperties(properties);
        dataType.put("onap.datatypes." + param, node);
        return dataType;
    }

    private Map<String, PolicyModelNode> translateEntrySchema(Map<String, PolicyModelNode> dataType, EntrySchema[] entry, String name){
        Map<String, PolicyModelNode> data = dataType;
        PolicyModelNode node = new PolicyModelNode();
        node.setDerived_from(Constants.TOSCA_NODES_ROOT);
        Map<String, PolicyProperties> properties = new TreeMap<>();

        for(EntrySchema e: entry) {
            if(e.getEntry_schema() != null) {
                PolicyProperties prop = new PolicyProperties();
                prop.setType("list");
                List<String> schema = new ArrayList<>();
                schema.add("type: onap.datatypes." + e.getName());
                prop.setEntry_schema(schema);
                properties.put(e.getName(), prop);
                data = translateEntrySchema(data, e.getEntry_schema(), e.getName());
                node.setProperties(properties);
            } else {
                PolicyProperties prop = new PolicyProperties();
                prop.setType(e.getType());
                properties.put(e.getName(), prop);
                node.setProperties(properties);
            }
        }

        dataType.put("onap.datatypes." + name, node);
        return data;
    }

}

