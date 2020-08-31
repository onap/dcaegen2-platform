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

import org.onap.dcaegen2.platform.mod.model.exceptions.MissingRequestBodyException;
import org.onap.dcaegen2.platform.mod.model.exceptions.OperationNotAllowedException;
import org.onap.dcaegen2.platform.mod.model.exceptions.ResourceConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.basemicroservice.BaseMicroserviceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.exceptions.common.UserNotPassedException;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.DeploymentArtifactNotFound;
import org.onap.dcaegen2.platform.mod.model.exceptions.msinstance.MsInstanceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.exceptions.specification.SpecificationInvalid;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.model.restapi.GenericErrorResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * a class to manage all exceptions
 */
@ControllerAdvice
@Slf4j
public class AppExceptionHandler {

    @ExceptionHandler(value = {WebClientResponseException.class})
    public ResponseEntity<ErrorResponse> handleCompSpecInvalidException
            (WebClientResponseException ex, WebRequest request) {
        return new ResponseEntity<ErrorResponse>
                (new ErrorResponse(ex.getResponseBodyAsString()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {JsonParseException.class})
    public ResponseEntity<GenericErrorResponse> handleJsonParsedException
            (JsonParseException ex, WebRequest request) {

        GenericErrorResponse response = new GenericErrorResponse();
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.setMessage("Invalid JSON request body format.");
        response.setErrors(Arrays.asList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<GenericErrorResponse> specificationInvalid(SpecificationInvalid ex) {
        Map<String, Object> errorResponse = new Gson().fromJson(ex.getMessage(), Map.class);
        GenericErrorResponse response = new GenericErrorResponse();
        response.setMessage((String) errorResponse.get("summary"));
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.setErrors((List<String>) errorResponse.get("errors"));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<GenericErrorResponse> missingRequestBodyException(MissingRequestBodyException ex){
        GenericErrorResponse response = new GenericErrorResponse();
        response.setMessage("Missing paramaters");
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.setErrors(Arrays.asList("Missing required request body paramaters"));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveUserNotPassedException(UserNotPassedException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveMsInstanceNotFoundException(MsInstanceNotFoundException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveDeploymentArtifactNotFound(DeploymentArtifactNotFound ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveOperationNotAllowed(OperationNotAllowedException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> resolveResourceConflict(ResourceConflictException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {BaseMicroserviceNotFoundException.class})
    public ResponseEntity<ErrorResponse> resolveResourceNotFoundExcetions(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericErrorResponse> resolveBeanValidationException(MethodArgumentNotValidException ex){
        log.error(ex.getMessage());
        GenericErrorResponse response = new GenericErrorResponse();
        response.setMessage("Validation failed.");
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.setErrors(getBeanValidationErrors(ex));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private List<String> getBeanValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(String.format("%s: %s", fieldName, errorMessage));
        });
        return errors;
    }
}
