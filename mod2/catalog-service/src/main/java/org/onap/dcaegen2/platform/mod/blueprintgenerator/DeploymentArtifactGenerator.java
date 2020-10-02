/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.mod.blueprintgenerator;

import com.google.gson.Gson;
import org.onap.blueprintgenerator.models.blueprint.Blueprint;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactGeneratorStrategy;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation for DeploymentArtifactGenerator
 */
@Component
public class DeploymentArtifactGenerator implements DeploymentArtifactGeneratorStrategy {

    /**
     * null implementation.
     * @param activeSpec
     * @param release
     * @return
     */

      @Override
    public Map<String, Object> generateForRelease(Specification activeSpec, String release) {

        ComponentSpec inboundComponentSpec = new ComponentSpec();
        inboundComponentSpec.createComponentSpecFromString(new Gson().toJson(activeSpec.getSpecContent()));

        Blueprint blueprint = new Blueprint().createBlueprint(inboundComponentSpec,"",'d',"","");

        Map<String, Object> modifiedResponse = new HashMap<>();
        modifiedResponse.put("content",  blueprint.blueprintToString());
        modifiedResponse.put("fileName", "filenamePlaceholder" + ".yaml");
        return modifiedResponse;
    }

}
