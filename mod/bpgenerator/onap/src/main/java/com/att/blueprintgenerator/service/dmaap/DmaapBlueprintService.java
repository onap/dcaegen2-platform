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

package com.att.blueprintgenerator.service.dmaap;

import com.att.blueprintgenerator.constants.Constants;
import com.att.blueprintgenerator.exception.BlueprintException;
import com.att.blueprintgenerator.model.common.Input;
import com.att.blueprintgenerator.model.common.Node;
import com.att.blueprintgenerator.model.common.OnapBlueprint;
import com.att.blueprintgenerator.model.componentspec.OnapComponentSpec;
import com.att.blueprintgenerator.model.componentspec.common.Publishes;
import com.att.blueprintgenerator.model.componentspec.common.Subscribes;
import com.att.blueprintgenerator.service.base.BlueprintHelperService;
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
 * Service to create DMAAP Blueprint
 */

@Service
public class DmaapBlueprintService extends BlueprintService {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PolicyNodeService policyNodeService;

	@Autowired
	private PgaasNodeService pgaasNodeService;

	@Autowired
	private QuotationService quotationService;

	@Autowired
	private BlueprintHelperService blueprintHelperService;

	public OnapBlueprint createBlueprint(OnapComponentSpec onapComponentSpec, Input input) {
		try {
			OnapBlueprint blueprint = new OnapBlueprint();
			blueprint.setTosca_definitions_version(Constants.TOSCA_DEF_VERSION);
			blueprint.setDescription(onapComponentSpec.getSelf().getDescription());

			Map<String, LinkedHashMap<String, Object>> inputs = new TreeMap<>();

			//if (!"".equals(input.getImportPath()))
			if (input.getImportPath() != null)
				blueprint.setImports(importsService.createImportsFromFile(input.getImportPath()));
			else
				blueprint.setImports(importsService.createImports(input.getBpType()));

			Map<String, Node> nodeTemplate = new TreeMap();

			Map<String, Object> dmaapNodeResponse = nodeService.createDmaapNode(onapComponentSpec, inputs, input.getServiceNameOverride());
			inputs = (Map<String, LinkedHashMap<String, Object>>) dmaapNodeResponse.get("inputs");
			nodeTemplate.put(onapComponentSpec.getSelf().getName(), (Node) dmaapNodeResponse.get("dmaapNode"));

			if (onapComponentSpec.getStreams().getPublishes() != null) {
				for (Publishes publishes : onapComponentSpec.getStreams().getPublishes()) {
					if (blueprintHelperService.isMessageRouterType(publishes.getType())) {
						String topic = publishes.getConfig_key() + Constants._TOPIC;
						Map<String, Object> topicNodeResponse = nodeService.createTopicNode(inputs, topic);
						inputs = (Map<String, LinkedHashMap<String, Object>>) topicNodeResponse.get("inputs");
						nodeTemplate.put(topic, (Node) topicNodeResponse.get("topicNode"));
					} else if (blueprintHelperService.isDataRouterType(publishes.getType())) {
						String feed = publishes.getConfig_key() + Constants._FEED;
						Map<String, Object> feedNodeResponse = nodeService.createFeedNode(inputs, feed);
						inputs = (Map<String, LinkedHashMap<String, Object>>) feedNodeResponse.get("inputs");
						nodeTemplate.put(feed, (Node) feedNodeResponse.get("feedNode"));
					}
				}
			}

			if (onapComponentSpec.getStreams().getSubscribes() != null) {
				for (Subscribes s : onapComponentSpec.getStreams().getSubscribes()) {
					if (blueprintHelperService.isMessageRouterType(s.getType())) {
						String topic = s.getConfig_key() + Constants._TOPIC;
						Map<String, Object> topicNodeResponse = nodeService.createTopicNode(inputs, topic);
						inputs = (Map<String, LinkedHashMap<String, Object>>) topicNodeResponse.get("inputs");
						nodeTemplate.put(topic, (Node) topicNodeResponse.get("topicNode"));
					} else if (blueprintHelperService.isDataRouterType(s.getType())) {
						String feed = s.getConfig_key() + Constants._FEED;
						Map<String, Object> feedNodeResponse = nodeService.createFeedNode(inputs, feed);
						inputs = (Map<String, LinkedHashMap<String, Object>>) feedNodeResponse.get("inputs");
						nodeTemplate.put(feed, (Node) feedNodeResponse.get("feedNode"));
					}
				}
			}

			if (onapComponentSpec.getPolicyInfo() != null)
				policyNodeService.addPolicyNodesAndInputs(onapComponentSpec, nodeTemplate, inputs);

			if (onapComponentSpec.getAuxilary().getDatabases() != null)
				pgaasNodeService.addPgaasNodesAndInputs(onapComponentSpec, nodeTemplate, inputs);

			blueprint.setNode_templates(nodeTemplate);
			blueprint.setInputs(inputs);
			return quotationService.setQuotations(blueprint);
		} catch (Exception ex) {
			throw new BlueprintException("Unable to create ONAP DMAAP Blueprint Object from given input parameters", ex);
		}
	}
}