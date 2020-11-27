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

package org.onap.dcaegen2.platform.mod.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class to manage Policy Model's REST endpoints
 */
@CrossOrigin
@RestController
@Api(tags = "Policy Model Distribution", description = "APIs to manage Policy Model Distribution Rest endpoints")
@RequestMapping("/api/policy-type")
@Slf4j
public class PolicyModelDistributionController {

    @Autowired
    PolicyModelDistributionService policyModelDistributionService;

    @GetMapping("/{modelId}")
    @ApiOperation("Get the status of specific Policy model Distribution")
    public ResponseEntity getPolicyModelDistributionById(@RequestParam("env") String env,@PathVariable String modelId) {
        log.info(modelId);
        return policyModelDistributionService.getPolicyModelDistributionById(env,modelId);
    }

    @PostMapping("/{modelId}")
    @ApiOperation("Distribute a specific Policy Model")
    public ResponseEntity distributePolicyModelById(@RequestParam("env") String env, @PathVariable String modelId) {
        return policyModelDistributionService.distributePolicyModel(env,modelId);
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolvePolicyModelDistributionEnvNotFound(PolicyModelDistributionEnvNotFoundException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


}
