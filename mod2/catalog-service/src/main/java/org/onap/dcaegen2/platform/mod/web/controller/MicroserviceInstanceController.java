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

import org.onap.dcaegen2.platform.mod.model.exceptions.msinstance.MsInstanceAlreadyExistsException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@Api(tags = "Microservice Instance", description = "APIs to manage Microservice Instance")
@RequestMapping("/api/microservice-instance")
public class MicroserviceInstanceController {

    @Autowired
    MsInstanceService msInstanceService;

    @GetMapping
    @ApiOperation("Get all Microservices Instances")
    public List<MsInstance> getAll() {
        return msInstanceService.getAll();
    }


    @PostMapping("/{msName}")
    @ApiOperation("Create a Microservice Instance")
    @ResponseStatus(HttpStatus.CREATED)
    public MsInstance createMsInstance(@PathVariable String msName, @RequestBody MsInstanceRequest request) {
        return msInstanceService.createMicroserviceInstance(msName, request);
    }

    @PatchMapping("/{msId}")
    @ApiOperation("Patch a Microservice Instance")
    @ResponseStatus(HttpStatus.OK)
    public MsInstance patchMsInstance(@RequestBody MsInstanceUpdateRequest request, @PathVariable String msId){
        return msInstanceService.updateMsInstance(request, msId);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveMsInstanceConflict(MsInstanceAlreadyExistsException ex) {
        return new ResponseEntity<>(new ErrorResponse("Microservice Instance for the given name and release already exists"), HttpStatus.CONFLICT);
    }
}
