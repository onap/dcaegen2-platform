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

package org.onap.dcaegen2.platform.mod.web.service.deploymentartifact;

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.StatusChangeNotValidException;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentArtifactStatusChangeHandlerTest {

    DeploymentArtifactStatusChangeHandler artifactStatusChangeHandler;

    @Mock
    DeploymentArtifactService deploymentArtifactService;

    @BeforeEach
    void setUp() {
        artifactStatusChangeHandler = new DeploymentArtifactStatusChangeHandler();
        artifactStatusChangeHandler.setDeploymentArtifactService(deploymentArtifactService);
    }

    @Test
    void test_DevCompleteToNotNeeded() throws Exception{
        //arrange
        List<DeploymentArtifact> mockDeploymentArticats = DeploymentArtifactObjectMother.createMockDeploymentArtifactsWithDifferentStatuses(true);
        DeploymentArtifact givenDAO = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.DEV_COMPLETE);
        String msInstaneId = givenDAO.getMsInstanceInfo().getId();

        when(deploymentArtifactService.findByMsInstanceId(msInstaneId)).thenReturn(mockDeploymentArticats);

        //act
        artifactStatusChangeHandler.handleStatusChange(DeploymentArtifactStatus.NOT_NEEDED, givenDAO);

        assertThat(givenDAO.getStatus()).isEqualTo(DeploymentArtifactStatus.NOT_NEEDED);
//        verify(msInstanceStatusChangeHandler, times(1))
//                .updateStatusBasedOnDeploymentArtifactsStatuses(msInstaneId);

    }

    @Test
    void test_ValidateIfArtifactWithDevCompleteStatusNotFoundForTheSameInstance() throws Exception{
        //arrange
        DeploymentArtifact givenDAO = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        String msInstaneId = givenDAO.getMsInstanceInfo().getId();

        List<DeploymentArtifact> mockDeploymentArticats = DeploymentArtifactObjectMother.createMockDeploymentArtifactsWithDifferentStatuses(false);
        when(deploymentArtifactService.findByMsInstanceId(msInstaneId)).thenReturn(mockDeploymentArticats);

        //act
        artifactStatusChangeHandler.handleStatusChange(DeploymentArtifactStatus.DEV_COMPLETE, givenDAO);

        //assert
        assertThat(givenDAO.getStatus()).isEqualTo(DeploymentArtifactStatus.DEV_COMPLETE);
        verify(deploymentArtifactService, times(1)).
                findByMsInstanceId(givenDAO.getMsInstanceInfo().getId());
//        verify(msInstanceStatusChangeHandler, times(1))
//                .updateStatusBasedOnDeploymentArtifactsStatuses(msInstaneId);

    }

    @Test
    void DoesntValidateIfArtifactWithDevCompleteStatusAlreadyExistsForTheSameInstance() throws Exception{
        //arrange
        DeploymentArtifact givenDAO = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        List<DeploymentArtifact> mockDeploymentArticats = DeploymentArtifactObjectMother.createMockDeploymentArtifactsWithDifferentStatuses(true);
        when(deploymentArtifactService.findByMsInstanceId("id-123")).thenReturn(mockDeploymentArticats);

        //act/assert
        assertThatExceptionOfType(StatusChangeNotValidException.class).isThrownBy(
                () -> artifactStatusChangeHandler.handleStatusChange(DeploymentArtifactStatus.DEV_COMPLETE, givenDAO)
        );

    }

}