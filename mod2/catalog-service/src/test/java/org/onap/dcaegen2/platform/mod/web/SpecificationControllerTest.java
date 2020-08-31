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

import org.onap.dcaegen2.platform.mod.model.specification.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.restapi.SpecificationRequest;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.web.controller.SpecificationController;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.asJsonString;
import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.MS_INSTANCE_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.SpecificationObjectMother.getMockSpecification;
import static org.onap.dcaegen2.platform.mod.objectmothers.SpecificationObjectMother.getSpecificationRequest;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SpecificationController.class)
public class SpecificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private SpecificationService mockSpecificationService;

    @BeforeEach
    void setup() {
    }

    @Test
    void test_addSpecification_returnsSpecification() throws Exception {
        //arrange
        SpecificationRequest specificationRequest = getSpecificationRequest();
        Specification specification = getMockSpecification(DeploymentType.DOCKER);

        when(mockSpecificationService.createSpecification(MS_INSTANCE_ID, specificationRequest)).thenReturn(specification);

        //act/assert
        mockMvc.perform(post("/api/specification/" + MS_INSTANCE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(specificationRequest)).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(status().isCreated());
        verify(mockSpecificationService, times(1)).createSpecification(MS_INSTANCE_ID, specificationRequest);
    }

}