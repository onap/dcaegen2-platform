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

package org.onap.dcaegen2.platform.mod.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class PolicyModelUpdateRequestValidationTest {

    public Validator validator;
    private PolicyModelUpdateRequest request;

    @BeforeEach
    public void setup(){
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        request = PolicyModelObjectMother.getPolicyModelUpdateRequest();
    }

    @Test
    void test_pmNameShouldNotBeBlank(){
        request.setName("");
        Set<ConstraintViolation<PolicyModelUpdateRequest>> violations = validator.validate(request);
        Assertions.assertThat(violations.size()).isEqualTo(2);
    }

    @Test
    void test_pmNameShouldFollowRegex() throws Exception{
        request.setName("pm-1");
        Set<ConstraintViolation<PolicyModelUpdateRequest>> violations = validator.validate(request);
        Assertions.assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void test_pmNameSizeValidation() throws Exception {
        request.setName("core-name-should-not-exceed-fifty-chars-core-name-should-not-exceed-fifty-chars");
        Set<ConstraintViolation<PolicyModelUpdateRequest>> violations = validator.validate(request);
        Assertions.assertThat(violations.size()).isEqualTo(1);
    }


    @Test
    void test_pmVersionShouldNotBeNull(){
        request.setVersion("1.1.1");
        Set<ConstraintViolation<PolicyModelUpdateRequest>> violations = validator.validate(request);
        Assertions.assertThat(violations.size()).isEqualTo(1);

    }

    @Test
    void test_pmVersionShouldFollowRegex() throws Exception{
        request.setContent("1.1.1");
        Set<ConstraintViolation<PolicyModelUpdateRequest>> violations = validator.validate(request);
        Assertions.assertThat(violations.size()).isEqualTo(1);
    }

}
