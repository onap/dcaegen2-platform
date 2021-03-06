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

package org.onap.dcaegen2.platform.mod.model.restapi;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  A model that represent response body to send a detailed error response.
 */
@Data
public class GenericErrorResponse {
    private List<String> errors = new ArrayList<>();
    private HttpStatus status;
    private String message;

    public GenericErrorResponse() {
    }

    public GenericErrorResponse(List<String> errors, HttpStatus status, String message) {
        this.errors = errors;
        this.status = status;
        this.message = message;
    }

    public GenericErrorResponse(String error, HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

}
