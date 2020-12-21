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

package org.onap.blueprintgenerator.model.policy;

import org.onap.blueprintgenerator.constants.Constants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: DCAE/ONAP - Blueprint Generator Common Module: Used by both ONAp
 * and DCAE Blueprint Applications Policy Model: A model class which represents Policies
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Policies {

    private String configAttributes;

    private String configName;

    private String onapName = Constants.ONAP_NAME_DCAE;

    private String policyName = Constants.POLICIES_POLICYNAME_DCAECONFIG;

    private boolean unique = false;
}
