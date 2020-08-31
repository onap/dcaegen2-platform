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

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.SpecificationRequest;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.model.specification.SpecificationStatus;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationGateway;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationServiceImpl;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.MS_INSTANCE_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.SpecificationObjectMother.getMockSpecification;
import static org.onap.dcaegen2.platform.mod.objectmothers.SpecificationObjectMother.getSpecificationRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class SpecificationServiceTest {

    private SpecificationServiceImpl service;

    @Mock
    private SpecificationGateway specRepo;

    @Mock
    private MsInstanceService msInstanceService;

    @Mock
    private SpecificationValidatorService validatorService;

    @BeforeEach
    void setUp() {
        service = new SpecificationServiceImpl();
        service.setMsInstanceService(msInstanceService);
        service.setSpecificationValidatorService(validatorService);
        service.setSpecificationGateway(specRepo);
    }

    @Test
    void createSpecificationTest() throws Exception {
        //given
        SpecificationRequest request = getSpecificationRequest();
        Specification specFromRepo = getMockSpecification(DeploymentType.K8S);
        MsInstance msInstance = MsInstanceObjectMother.createMsInstance();

        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);
        when(specRepo.save(any(Specification.class))).thenReturn(specFromRepo);

        //when
        Specification spec = service.createSpecification(MS_INSTANCE_ID, request);

        //then
        assertThatFieldsAreCorrect(request, spec);
        verifyCalls(request, msInstance);

    }

    private void assertThatFieldsAreCorrect(SpecificationRequest request, Specification spec) {
        assertThat(spec.getStatus()).isEqualTo(SpecificationStatus.ACTIVE);
        assertThat(spec.getSpecContent()).isEqualTo(request.getSpecContent());
        assertThat(spec.getPolicyJson()).isEqualTo(request.getPolicyJson());
        assertThat(spec.getType()).isEqualTo(request.getType());
        assertThat(spec.getMetadata().get("createdBy")).isEqualTo(request.getUser());
        assertThat(spec.getMetadata().get("createdOn")).isNotNull();
        assertThat(spec.getMsInstanceInfo()).isNotNull();
    }

    private void verifyCalls(SpecificationRequest request, MsInstance msInstance) {
        verify(msInstanceService, times(1)).getMsInstanceById(MS_INSTANCE_ID);
        verify(validatorService, times(1)).validateSpecForRelease(request, msInstance.getRelease());
        verify(specRepo, times(2)).save(any(Specification.class));
        verify(msInstanceService, times(1)).updateMsInstance(msInstance);
    }
}
