/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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
 */

package org.onap.dcaegen2.platform.mod.exceptions;

import org.onap.dcaegen2.platform.mod.models.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author
 * @date 09/08/2020
 * Exception handler for the Auth Service Application
 */
@ControllerAdvice
@Slf4j
public class AppExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<GenericResponse> resolveRuntimeException(UserAlreadyExistsException ex){
        log.error(ex.getMessage());
        return new ResponseEntity<>(new GenericResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<GenericResponse> resolveRoleNotFoundException(RoleNotExistsException ex){
        log.error(ex.getMessage());
        return new ResponseEntity<>(new GenericResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
