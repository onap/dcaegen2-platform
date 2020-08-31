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

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactSearch;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.BlueprintFileNameCreateException;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.StatusChangeNotValidException;
import org.onap.dcaegen2.platform.mod.model.restapi.DeploymentArtifactPatchRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.model.restapi.SuccessResponse;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.onap.dcaegen2.platform.mod.web.controller.DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL;

/**
 * Controller class to manage DeploymentArtifact's REST endpoints
 */
@RestController
@CrossOrigin
@RequestMapping(DEPLOYMENT_ARTIFACTS_BASE_URL)
@Slf4j
@Api(tags = "Deployment Artifact", value = "APIs to manage Deployment Artifacts")
public class DeploymentArtifactController {

    public static final String DEPLOYMENT_ARTIFACTS_BASE_URL = "/api/deployment-artifact";

    public static final String GET_STATUSES = "/statuses";

    @Autowired
    private DeploymentArtifactService service;

    @PostMapping("/{msInstanceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentArtifact generateDeploymentArtifactForMSInstance(@PathVariable String  msInstanceId,
                                                                      @RequestParam String user ){
        return service.generateDeploymentArtifact(msInstanceId, user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DeploymentArtifact> getAllDeploymentArtifacts(){
        return service.getAllDeploymentArtifacts();
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<DeploymentArtifact> searchDeploymentArtifacts(@RequestBody DeploymentArtifactSearch searchRequest){
        log.info("Search on deployment artifacts: {}", searchRequest);
        return service.searchDeploymentArtifacts(searchRequest);
    }

    @GetMapping(GET_STATUSES)
    @ResponseStatus(HttpStatus.OK)
    public List<DeploymentArtifactStatus> getDeploymentArtifactStatuses(){
        return Arrays.asList(DeploymentArtifactStatus.values());
    }

    @PatchMapping(value = "/{deploymentArtifactId}", params = {"user!="})
    public ResponseEntity<SuccessResponse> patchDeploymentArtifact(@PathVariable("deploymentArtifactId") String id,
                                                     @RequestBody DeploymentArtifactPatchRequest deploymentArtifactPatchRequest,
                                                     @RequestParam("user") String user){
        log.info("***Received request {} to update DeploymentArtifact id {} by {}", deploymentArtifactPatchRequest, id, user);
        service.updateDeploymentArtifact(id, deploymentArtifactPatchRequest, user);
        return new ResponseEntity<>(new SuccessResponse("Deployment Artifact was updated."),HttpStatus.OK);
    }

    @DeleteMapping( value = "/{deploymentArtifactId}", params = {"user!="})
    public ResponseEntity<SuccessResponse> deleteDeploymentArtifact(@PathVariable("deploymentArtifactId") String id,
                                                      @RequestParam("user") String user){
        log.info("***Received request to delete DeploymentArtifact id {} by {}", id, user);
        service.deleteDeploymentArtifact(id);
        return new ResponseEntity<>(new SuccessResponse("Deployment Artifact was deleted"), HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveBaseMsServiceNameRegex(BlueprintFileNameCreateException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveStatusValidationFailure(StatusChangeNotValidException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
