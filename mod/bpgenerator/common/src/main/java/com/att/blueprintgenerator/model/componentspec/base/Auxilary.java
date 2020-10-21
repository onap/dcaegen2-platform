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

package com.att.blueprintgenerator.model.componentspec.base;


import com.att.blueprintgenerator.model.componentspec.common.HealthCheck;
import com.att.blueprintgenerator.model.componentspec.common.Policy;
import com.att.blueprintgenerator.model.componentspec.common.Reconfigs;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAP and DCAE Blueprint Applications
 * Component Spec Model: For Auxilary
 */

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class Auxilary {

	private HealthCheck healthcheck;

	private HealthCheck livehealthcheck;

	private Policy policy;

	private Reconfigs reconfigs;

	private List<LinkedHashMap<String, String>> env;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Map<String, String> databases;

/*	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Affinity affinity;*/

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Object hpa_config;


}
