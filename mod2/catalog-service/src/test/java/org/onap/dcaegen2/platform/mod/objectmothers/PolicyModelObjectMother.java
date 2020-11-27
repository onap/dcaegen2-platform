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

package org.onap.dcaegen2.platform.mod.objectmothers;

import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.onap.dcaegen2.platform.mod.util.TestUtil;

public class PolicyModelObjectMother {
    public static final String PM_CREATE_REQUEST = "src/test/resources/http/requests/policy-model_create_request.json";
    public static final String PM_UPDATE_REQUEST = "src/test/resources/http/requests/policy-model_update_request.json";
    public static final String PM_RESPONSE = "src/test/resources/http/responses/policy-model_create_response.json";
    public static final String POLICY_MODEL_ID = "5fb896389387ea1087bdb1aa"; //"5fae8956cd1bac74e55c9d3a";

    public static PolicyModelCreateRequest getPolicyModelCreateRequest() {
        return TestUtil.deserializeJsonFileToModel(PM_CREATE_REQUEST, PolicyModelCreateRequest.class);
    }

    public static PolicyModel getPolicyModelResponse() {
        return TestUtil.deserializeJsonFileToModel(PM_RESPONSE, PolicyModel.class);
    }

    public static PolicyModelUpdateRequest getPolicyModelUpdateRequest() {
        return TestUtil.deserializeJsonFileToModel(PM_UPDATE_REQUEST, PolicyModelUpdateRequest.class);
    }
}
