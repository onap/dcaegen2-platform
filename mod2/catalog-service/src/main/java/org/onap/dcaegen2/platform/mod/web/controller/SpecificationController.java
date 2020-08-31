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

import org.onap.dcaegen2.platform.mod.model.restapi.SpecificationRequest;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class to manage Specification's REST endpoints
 */
@CrossOrigin
@RestController
@Api(tags = "Component Specification", description = "APIs to manage Component Specifications")
@RequestMapping("/api/specification")
@Slf4j
public class SpecificationController {

    @Autowired
    SpecificationService specificationService;

    @GetMapping("/{msInstanceId}")
    @ApiOperation("Get all specifications for a Microservice Instance")
    public List<Specification> getAllSpecsByMsInstanceId(@PathVariable String msInstanceId) {
        log.info(msInstanceId);
        return specificationService.getAllSpecsByMsInstanceId(msInstanceId);
    }

    @PostMapping("/{msInstanceId}")
    @ApiOperation("Create Specification for a Microservice Instance")
    @ResponseStatus(HttpStatus.CREATED)
    public Specification createSpecification(@PathVariable String msInstanceId, @RequestBody SpecificationRequest request) {
        log.info(request.toString());
        return specificationService.createSpecification(msInstanceId, request);
    }
}
