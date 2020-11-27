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
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.MsInstanceInfo;
import org.onap.dcaegen2.platform.mod.model.exceptions.msinstance.MsInstanceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.MsService;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceGateway;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceServiceImpl;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceStatusChangeHandler;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.*;

@ExtendWith(MockitoExtension.class)
class MsInstanceServiceImplTest {

    @Spy
    private MsInstanceServiceImpl service = new MsInstanceServiceImpl();

    @Mock
    private MsInstanceGateway msInstanceRepository;

    @Mock
    private MsService msService;

    @Mock
    private SpecificationService specificationService;

    @Mock
    private DeploymentArtifactService deploymentArtifactService;

    @Mock
    private MsInstanceStatusChangeHandler msInstanceStatusChangeHandler;


    @BeforeEach
    void setUp() {
        service.setMsService(msService);
        service.setSpecificationService(specificationService);
        service.setDeploymentArtifactService(deploymentArtifactService);
        service.setMsInstanceRepository(msInstanceRepository);
        service.setMsInstanceStatusChangeHandler(msInstanceStatusChangeHandler);
    }

    @Test
    void getAll() {
        MsInstance instance_1 = MsInstance.builder().id("123").build();
        MsInstance instance_2 = MsInstance.builder().id("345").build();

        when(msInstanceRepository.findAll()).thenReturn(Arrays.asList(instance_1, instance_2));

        List<MsInstance> instances = service.getAll();

        assertThat(instances.size()).isEqualTo(2);
        verify(msInstanceRepository, times(1)).findAll();
    }

    @Test
    void test_getMsInstanceById() throws Exception{
        MsInstance expected = MsInstanceObjectMother.createMsInstance();

        when(msInstanceRepository.findById(MS_INSTANCE_ID)).thenReturn(Optional.of(expected));

        MsInstance original = service.getMsInstanceById(MS_INSTANCE_ID);

        assertThat(original.getId()).isEqualTo(expected.getId());
    }

    @Test
    void test_msIntanceNotFound_willRaiseException() throws Exception{
        when(msInstanceRepository.findById(MS_INSTANCE_ID)).thenReturn(Optional.empty());
        assertThatExceptionOfType(MsInstanceNotFoundException.class).isThrownBy(
                () -> service.getMsInstanceById(MS_INSTANCE_ID));
    }

    //TODO require cleaning and more assertions
    @Test
    void createMicroserviceInstance() {

        BaseMicroservice microservice = BaseMsObjectMother.createMockMsObject();
        MsInstanceRequest request = getMsInstanceMockRequest();
        MsInstance msInstanceMockDao = createMsInstance();

        when(msService.getMicroserviceByName(BaseMsObjectMother.BASE_MS_NAME)).thenReturn(microservice);
        when(msInstanceRepository.findByNameAndRelease(request.getName(), request.getRelease()))
                .thenReturn(Optional.empty());
        when(msInstanceRepository.save(any())).thenReturn(msInstanceMockDao);

        MsInstance msInstance = service.createMicroserviceInstance(BaseMsObjectMother.BASE_MS_NAME,request);

        assertThat(msInstance.getId()).isEqualTo(msInstance.getId());
        assertThat(msInstance.getName()).isEqualTo(msInstance.getName());
        assertThat(msInstance.getMsInfo().keySet()).isEqualTo(msInstanceMockDao.getMsInfo().keySet());

        verify(msService, times(1)).getMicroserviceByName(BaseMsObjectMother.BASE_MS_NAME);
        verify(msInstanceRepository, times(1)).save(any(MsInstance.class));
        verify(msService, times(1)).
                saveMsInstanceReferenceToMs(microservice, msInstance);

    }

    @Test
    void test_updateMsInstance() {

    }

    @Test
    void updateStatusBasedOnDeploymentArtifactsStatuses() {
        MsInstance msInstance = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        when(msInstanceRepository.findById(msInstance.getId())).thenReturn(Optional.of(msInstance));

        service.updateStatusBasedOnDeploymentArtifactsStatuses(msInstance.getId());

        verify(msInstanceStatusChangeHandler, times(1)).updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);
        verify(service, times(1)).updateMsInstance(msInstance);

    }

    @Test
    void test_removeDeploymentArtifactFromMsInstance() {
        MsInstance msInstance = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        DeploymentArtifact deploymentArtifact = createDeploymentArtifact(msInstance);

        when(msInstanceRepository.findById(msInstance.getId())).thenReturn(Optional.of(msInstance));
        //when(msInstanceStatusChangeHandler.updateStatusBasedOnDeploymentArtifactsStatuses(any())).thenReturn(msInstance);

        service.removeDeploymentArtifactFromMsInstance(deploymentArtifact);

        assertThat(msInstance.getDeploymentArtifactsInfo().getDeploymentArtifacts().contains(deploymentArtifact.getId())).isFalse();
        verify(msInstanceStatusChangeHandler, times(1)).updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);
        verify(service, times(1)).updateMsInstance(msInstance);

    }

    @Test
    void updateMicroserviceReference() throws Exception{
        BaseMicroservice microservice = BaseMsObjectMother.createMockMsObject();
        MsInstance msInstance_1 = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        msInstance_1.setId("instance-1");
        msInstance_1.getMsInfo().put("name", "old-ms");
        MsInstance msInstance_2 = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        msInstance_2.setId("instance-2");
        msInstance_2.getMsInfo().put("name", "old-ms");

        when(msInstanceRepository.findById("instance-1")).thenReturn(Optional.of(msInstance_1));
        when(msInstanceRepository.findById("instance-2")).thenReturn(Optional.of(msInstance_2));

        service.updateMicroserviceReference(microservice);

        assertThat(msInstance_1.getName()).isEqualTo(microservice.getName());
        assertThat(msInstance_2.getName()).isEqualTo(microservice.getName());

        assertThat(msInstance_1.getMsInfo().get("name")).isEqualTo(microservice.getName());
        assertThat(msInstance_2.getMsInfo().get("name")).isEqualTo(microservice.getName());

        verify(service, times(2)).getMsInstanceById(anyString());
        verify(msInstanceRepository, times(2)).save(any(MsInstance.class));
    }

    private DeploymentArtifact createDeploymentArtifact(MsInstance msInstance) {
        DeploymentArtifact deploymentArtifact = new DeploymentArtifact();
        deploymentArtifact.setId(msInstance.getDeploymentArtifactsInfo().getDeploymentArtifacts().get(0));

        MsInstanceInfo msInstanceInfo = new MsInstanceInfo();
        msInstanceInfo.setId(msInstance.getId());
        deploymentArtifact.setMsInstanceInfo(msInstanceInfo);
        return deploymentArtifact;
    }
}