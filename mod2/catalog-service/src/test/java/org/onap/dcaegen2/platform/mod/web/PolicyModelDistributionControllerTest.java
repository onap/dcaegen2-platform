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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.web.controller.PolicyModelDistributionController;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.USER;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_ENV;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_MODEL_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_MODEL_NAME;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PolicyModelDistributionController.class)
public class PolicyModelDistributionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private PolicyModelDistributionService mockPolicyModelDistributionService;

    @BeforeEach
    void setup() {
    }

    @Test
    void test_getPolicyModelById() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();
        ResponseEntity mockResponseEntity = ResponseEntity.status(200).body(policyModel.getContent());

       when(mockPolicyModelDistributionService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID)).thenReturn(mockResponseEntity);

        mockMvc.perform(get("/api/policy-type/" + PM_DISTRIBUTION_MODEL_ID)
                .param("env",PM_DISTRIBUTION_ENV).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockPolicyModelDistributionService, times(1)).getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);
    }

    @Test
    void test_getPolicyModelById_BadRequest() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();
        ResponseEntity mockResponseEntity = ResponseEntity.status(400).body(policyModel.getContent());

        when(mockPolicyModelDistributionService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID)).thenReturn(mockResponseEntity);

        mockMvc.perform(get("/api/policy-type/" + PM_DISTRIBUTION_MODEL_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(mockPolicyModelDistributionService, times(0)).getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);
    }

    @Test
    void test_distributePolicyModelById_shouldReturn201AndResponseBody() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();
        ResponseEntity mockResponseEntity = ResponseEntity.status(200).body(policyModel.getContent());

        when(mockPolicyModelDistributionService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID)).thenReturn(mockResponseEntity);

        mockMvc.perform(post("/api/policy-type/" + PM_DISTRIBUTION_MODEL_ID)
                .param("env",PM_DISTRIBUTION_ENV).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(mockPolicyModelDistributionService, times(1)).distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);
    }

    @Test
    void test_distributePolicyModelById_BadRequest() throws Exception {
        PolicyModel policyModel = getPolicyModelResponse();
        ResponseEntity mockResponseEntity = ResponseEntity.status(400).body(policyModel.getContent());

        when(mockPolicyModelDistributionService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID)).thenReturn(mockResponseEntity);

        mockMvc.perform(post("/api/policy-type/" + PM_DISTRIBUTION_MODEL_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
        verify(mockPolicyModelDistributionService, times(0)).distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);
    }


}