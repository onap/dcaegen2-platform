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
import lombok.Setter;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.MsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.onap.dcaegen2.platform.mod.web.controller.BaseMicroserviceController.API_BASE_MICROSERVICE;

/**
 * Controller class to manage Microservice's REST endpoints
 */
@CrossOrigin
@RestController
@RequestMapping(API_BASE_MICROSERVICE)
@Api(tags = "Base Microservice", description = "APIs to manage Base Microservice")
public class BaseMicroserviceController {

    public static final String API_BASE_MICROSERVICE = "/api/base-microservice";
    @Autowired
    @Setter
    MsService baseMsService;

    @GetMapping
    public List<BaseMicroservice> getAll() {
        return baseMsService.getAllMicroservices();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseMicroservice createMicroservice(@RequestBody @Valid MicroserviceCreateRequest request) {
        return baseMsService.createMicroservice(request);
    }

    @PatchMapping(value = "/{msId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMicroservice(@RequestBody @Valid MicroserviceUpdateRequest request,
                                   @PathVariable("msId") String msId){
        baseMsService.updateMicroservice(msId, request);
    }
}
