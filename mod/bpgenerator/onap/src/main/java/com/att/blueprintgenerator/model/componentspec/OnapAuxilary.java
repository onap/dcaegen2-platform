/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
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
 *
 */

package com.att.blueprintgenerator.model.componentspec;

import com.att.blueprintgenerator.model.componentspec.base.Auxilary;
import com.att.blueprintgenerator.model.componentspec.common.Volumes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Component Spec Model for ONAP Auxillary derived from Common Module Auxillary used by both DCAE and ONAP
 */


@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnapAuxilary extends Auxilary {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Object> ports;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Object> log_info;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Object> tls_info;

    private Volumes[] volumes;

}
