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
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstanceStatus;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceStatusChangeHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsInstanceStatusChangeHandlerTest {

    MsInstanceStatusChangeHandler statusChangeHandler;

    @Mock
    MsInstanceService msInstanceService;

    @Mock
    DeploymentArtifactService deploymentArtifactService;

    @BeforeEach
    void setup() throws Exception{
        statusChangeHandler = new MsInstanceStatusChangeHandler();
        statusChangeHandler.setMsInstanceService(msInstanceService);
        statusChangeHandler.setDeploymentArtifactService(deploymentArtifactService);
    }

    @Test
    void handleStatusChangeFromDeploymentArtifactsWithDevComplete() {
        //arrange
        MsInstance msInstance = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();

        when(deploymentArtifactService.findByMsInstanceId(msInstance.getId())).thenReturn(
                DeploymentArtifactObjectMother.createMockDeploymentArtifactsWithDifferentStatuses(true)
        );

        //act
        statusChangeHandler.updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);

        //assert
        assertThat(msInstance.getStatus()).isEqualTo(MsInstanceStatus.DEV_COMPLETE);
        verify(deploymentArtifactService, times(1)).findByMsInstanceId(msInstance.getId());
    }

    @Test
    void handleStatusChangeFromDeploymentArtifactsWithoutDevComplete() {
        //arrange
        MsInstance msInstance = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();

        //when(msInstanceService.getMsInstanceById(msInstance.getId())).thenReturn(msInstance);
        when(deploymentArtifactService.findByMsInstanceId(msInstance.getId())).thenReturn(
                DeploymentArtifactObjectMother.createMockDeploymentArtifactsWithDifferentStatuses(false)
        );

        //act
        statusChangeHandler.updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);

        //assert
        assertThat(msInstance.getStatus()).isEqualTo(MsInstanceStatus.IN_DEV);
        verify(deploymentArtifactService, times(1)).findByMsInstanceId(msInstance.getId());
    }

}