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

import org.onap.dcaegen2.platform.mod.model.restapi.SpecificationRequest;
import org.onap.dcaegen2.platform.mod.model.specification.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.util.TestUtil;

public class SpecificationObjectMother {
    public static final String SPEC_REQUEST = "src/test/resources/http/requests/CreateSpecificationRequest.json";
    public static final String SPEC_RESPONSE = "src/test/resources/http/requests/CreateSpecificationResponse.json";

    public static SpecificationRequest getSpecificationRequest() {
        return TestUtil.deserializeJsonFileToModel(SPEC_REQUEST, SpecificationRequest.class);
    }

    public static Specification getMockSpecification(DeploymentType type) {
        Specification specification = TestUtil.deserializeJsonFileToModel(SPEC_RESPONSE, Specification.class);
        specification.setType(type);
        return specification;
    }
}
