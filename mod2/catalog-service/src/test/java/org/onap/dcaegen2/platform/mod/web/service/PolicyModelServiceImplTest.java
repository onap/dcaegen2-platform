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

package org.onap.dcaegen2.platform.mod.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelGateway;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.USER;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.POLICY_MODEL_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelCreateRequest;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelUpdateRequest;

@ExtendWith(MockitoExtension.class)
class PolicyModelServiceImplTest {

    @Spy
    private PolicyModelServiceImpl pmImplService = new PolicyModelServiceImpl();

    @Mock
    private PolicyModelGateway policyModelGateway;


    @BeforeEach
    void setUp() {
        pmImplService.setPolicyModelGateway(policyModelGateway);
    }

    @Test
    void getAll() {
        PolicyModel instance_1 = PolicyModel.builder().id("123").name("test").owner("user").version("1.0.0").build();
        PolicyModel instance_2 = PolicyModel.builder().id("345").name("test1").owner("user1").version("1.1.0").build();

        when(policyModelGateway.findAll()).thenReturn(Arrays.asList(instance_1, instance_2));

        List<PolicyModel> instances = pmImplService.getAll();

        assertThat(instances.size()).isEqualTo(2);
        verify(policyModelGateway, times(1)).findAll();
    }

    @Test
    void test_getPolicyModelById() {
        PolicyModel expected = PolicyModelObjectMother.getPolicyModelResponse();

        when(policyModelGateway.findById(POLICY_MODEL_ID)).thenReturn(Optional.of(expected));

        PolicyModel original = pmImplService.getPolicyModelById(POLICY_MODEL_ID);

        assertThat(original.getId()).isEqualTo(expected.getId());
        verify(policyModelGateway, times(1)).findById(POLICY_MODEL_ID);
    }

    @Test
    void test_policyModelNotFound_willRaiseException() {
        when(policyModelGateway.findById(POLICY_MODEL_ID)).thenReturn(Optional.empty());
        assertThatExceptionOfType(PolicyModelNotFoundException.class).isThrownBy(() -> pmImplService.getPolicyModelById(POLICY_MODEL_ID));
    }


    @Test
    void test_createPolicyModel() {

       PolicyModelCreateRequest policyModelCreateRequest = getPolicyModelCreateRequest();
       PolicyModel policyModel = PolicyModelObjectMother.getPolicyModelResponse();

       when(policyModelGateway.findByNameAndVersion(policyModelCreateRequest.getName(), policyModelCreateRequest.getVersion())).thenReturn(Optional.empty());
       when(policyModelGateway.save(any())).thenReturn(policyModel);

       pmImplService.createPolicyModel(policyModelCreateRequest,USER);

       assertThat(policyModel.getVersion()).isEqualTo(policyModelCreateRequest.getVersion());
       assertThat(policyModel.getName()).isEqualTo(policyModelCreateRequest.getName());

       verify(policyModelGateway, times(1)).findByNameAndVersion(policyModelCreateRequest.getName(), policyModelCreateRequest.getVersion());
       verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_createPolicyModel_willRaiseException() {
        PolicyModelCreateRequest policyModelCreateRequest = getPolicyModelCreateRequest();
        PolicyModel policyModel = PolicyModelObjectMother.getPolicyModelResponse();

        when(policyModelGateway.findByNameAndVersion(policyModelCreateRequest.getName(), policyModelCreateRequest.getVersion())).thenReturn(Optional.of(policyModel));

        assertThatExceptionOfType(PolicyModelConflictException.class).isThrownBy(() ->  pmImplService.createPolicyModel(policyModelCreateRequest,USER));
    }

    @Test
    void test_updatePolicyModel() {

        PolicyModelUpdateRequest policyModelUpdateRequest = getPolicyModelUpdateRequest();
        policyModelUpdateRequest.setName("test1");
        policyModelUpdateRequest.setVersion("1.2.1");
        PolicyModel policyModel = PolicyModelObjectMother.getPolicyModelResponse();

        when(policyModelGateway.findById(POLICY_MODEL_ID)).thenReturn(Optional.of(policyModel));

        when(policyModelGateway.findByNameAndVersion(policyModelUpdateRequest.getName(), policyModelUpdateRequest.getVersion())).thenReturn(Optional.empty());
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        pmImplService.updatePolicyModel(policyModelUpdateRequest,POLICY_MODEL_ID, USER);

        assertThat(policyModel.getVersion()).isEqualTo(policyModelUpdateRequest.getVersion());
        assertThat(policyModel.getName()).isEqualTo(policyModelUpdateRequest.getName());

        verify(policyModelGateway, times(1)).findById(POLICY_MODEL_ID);
        verify(policyModelGateway, times(1)).findByNameAndVersion(policyModelUpdateRequest.getName(), policyModelUpdateRequest.getVersion());
        verify(policyModelGateway, times(1)).save(any());
    }


}