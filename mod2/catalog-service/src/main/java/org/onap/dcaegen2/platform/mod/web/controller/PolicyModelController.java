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
import org.onap.dcaegen2.platform.mod.model.exceptions.common.UserNotPassedException;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.DistributionInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Controller class to manage Policy Model's REST endpoints
 */
@CrossOrigin
@RestController
@Api(tags = "Policy Model", description = "APIs to manage Policy Model Rest endpoints")
@RequestMapping("/api/policy-model")
@Slf4j
public class PolicyModelController {

    @Autowired
    PolicyModelService policyModelService;

    /**
     * Controller class to manage Policy Model's GET all policy model's
     */
    @GetMapping
    @ApiOperation("Get all Policy Models")
    public List<PolicyModel> getAll() {
        return policyModelService.getAll();
    }

    /**
     * Controller class to manage Policy Model's GET by policy model id
     */
    @GetMapping("/{modelId}")
    @ApiOperation("Get specific Policy model")
    public PolicyModel getPolicyModelById(@PathVariable String modelId) {
        log.info(modelId);
        return policyModelService.getPolicyModelById(modelId);
    }

    /**
     * Controller class to manage Policy Model's POST
     */
    @PostMapping
    @ApiOperation("Create a Policy Model")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyModel createPolicyModel(@RequestBody @Valid PolicyModelCreateRequest request, @RequestParam @NotBlank String user) {
        return policyModelService.createPolicyModel(request,user);
    }

    /**
     * Controller class to manage Policy Model's PATCH by policy model id
     */
    @PatchMapping("/{modelId}")
    @ApiOperation("Patch a Policy Model")
    @ResponseStatus(HttpStatus.OK)
    public PolicyModel patchPolicyModel(@RequestBody @Valid PolicyModelUpdateRequest request, @PathVariable String modelId, @RequestParam @NotBlank String user) {
        return policyModelService.updatePolicyModel(request, modelId,user);
    }

    /**
     * Controller class to manage Policy Model's conflict exception
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolvePolicyModelConflict(PolicyModelConflictException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

}
