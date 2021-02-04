/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.platform.mod.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelDistributionEnv;
import org.onap.dcaegen2.platform.mod.web.service.blueprintdistributionservice.BlueprintDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/deployment-artifact")
@Api(tags = "Graph", description = "API to distribute Blueprint to DCAE")
public class BlueprintDistributionController {

    @Autowired
    private BlueprintDistributionService blueprintDistributionService;

    @PostMapping(value = "/{deploymentArtifactId}/distribute")
    @ApiOperation("Distribution of Blueprint to DCAE")
    public ResponseEntity distributeBlueprint(@PathVariable(value = "deploymentArtifactId") String deploymentArtifactId
        ,@RequestParam PolicyModelDistributionEnv env){

       return blueprintDistributionService.distributeBlueprint(deploymentArtifactId,env.name());
    }

}
