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

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.controller.PolicyModelController;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.USER;
import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.asJsonString;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.POLICY_MODEL_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelCreateRequest;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelResponse;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelUpdateRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PolicyModelController.class)
public class PolicyModelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private PolicyModelService mockPolicyModelService;

    @BeforeEach
    void setup() {
    }

    @Test
    void test_getAllPolicyModels() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();

        when(mockPolicyModelService.getAll()).thenReturn(Arrays.asList(policyModel));

        mockMvc.perform(get("/api/policy-model")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }

    @Test
    void test_getPolicyModelById() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();

       when(mockPolicyModelService.getPolicyModelById(POLICY_MODEL_ID)).thenReturn(policyModel);

        mockMvc.perform(get("/api/policy-model/" + POLICY_MODEL_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void test_patchPolicyModel_shouldReturn204AndResponseBody() throws Exception{
        PolicyModelUpdateRequest policyModelpdateRequest = getPolicyModelUpdateRequest();
        PolicyModel policyModel = getPolicyModelResponse();

        when(mockPolicyModelService.updatePolicyModel(policyModelpdateRequest,POLICY_MODEL_ID, USER)).thenReturn(policyModel);

        mockMvc.perform(patch("/api/policy-model/" + POLICY_MODEL_ID).param("user", USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(policyModelpdateRequest)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(mockPolicyModelService, times(0)).updatePolicyModel(policyModelpdateRequest,POLICY_MODEL_ID, USER);
    }

}