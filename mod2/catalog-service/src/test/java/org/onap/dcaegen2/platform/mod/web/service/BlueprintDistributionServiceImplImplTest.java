/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2021  Nokia. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package org.onap.dcaegen2.platform.mod.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_ENV;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_MODEL_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_PWD;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_USER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.onap.dcaegen2.platform.mod.util.BlueprintDistributionUtils;
import org.onap.dcaegen2.platform.mod.web.service.blueprintdistributionservice.BlueprintDistributionServiceImpl;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactGateway;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactServiceImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BlueprintDistributionServiceImplImplTest {

    @Spy
    private BlueprintDistributionServiceImpl mockBlueprintDistributionServiceImpl = new BlueprintDistributionServiceImpl();

    @Mock
    private DeploymentArtifactServiceImpl deploymentArtifactService;

    @Mock
    private BlueprintDistributionUtils blueprintDistributionUtils;

    @Mock
    private DeploymentArtifactGateway deploymentArtifactGateway;

    @Mock
    private RestTemplate restTemplate;

    DeploymentArtifact deploymentArtifact;



    @BeforeEach
    void initialize(){
        mockBlueprintDistributionServiceImpl.setDeploymentArtifactService(deploymentArtifactService);
        mockBlueprintDistributionServiceImpl.setDeploymentArtifactGateway(deploymentArtifactGateway);
        mockBlueprintDistributionServiceImpl.setBlueprintDistributionUtils(blueprintDistributionUtils);
        mockBlueprintDistributionServiceImpl.setRestTemplate(restTemplate);
        deploymentArtifact = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
    }


    @Test
    void test_distributeBlueprintReturnSucess() {

        when(deploymentArtifactService.findDeploymentArtifactById(BP_DISTRIBUTION_MODEL_ID)).thenReturn(deploymentArtifact);
        when(blueprintDistributionUtils.getBlueprintDashboardURL(BP_DISTRIBUTION_ENV)).thenReturn("/url");
        when(blueprintDistributionUtils.getBlueprintDashboardUserName(BP_DISTRIBUTION_ENV)).thenReturn(BP_DISTRIBUTION_USER);
        when(blueprintDistributionUtils.getBlueprintDashboardPassword(BP_DISTRIBUTION_ENV)).thenReturn(BP_DISTRIBUTION_PWD);

        ResponseEntity expected = mockBlueprintDistributionServiceImpl.distributeBlueprint(BP_DISTRIBUTION_MODEL_ID,BP_DISTRIBUTION_ENV);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(deploymentArtifactService, times(1)).findDeploymentArtifactById(BP_DISTRIBUTION_MODEL_ID);
        verify(blueprintDistributionUtils, times(2)).getBlueprintDashboardURL(BP_DISTRIBUTION_ENV);
        verify(blueprintDistributionUtils, times(1)).getBlueprintDashboardUserName(BP_DISTRIBUTION_ENV);
        verify(blueprintDistributionUtils, times(1)).getBlueprintDashboardPassword(BP_DISTRIBUTION_ENV);
        verify(deploymentArtifactGateway, times(1)).save(any());

    }

    @Test
    void test_distributeBlueprintReturnBadRequest() {

        when(deploymentArtifactService.findDeploymentArtifactById(BP_DISTRIBUTION_MODEL_ID)).thenReturn(deploymentArtifact);
        when(blueprintDistributionUtils.getBlueprintDashboardURL(BP_DISTRIBUTION_ENV)).thenReturn("/url");
        when(blueprintDistributionUtils.getBlueprintDashboardUserName(BP_DISTRIBUTION_ENV)).thenReturn(BP_DISTRIBUTION_USER);
        when(blueprintDistributionUtils.getBlueprintDashboardPassword(BP_DISTRIBUTION_ENV)).thenReturn(BP_DISTRIBUTION_PWD);
        when(restTemplate.postForObject(blueprintDistributionUtils.getBlueprintDashboardURL(BP_DISTRIBUTION_ENV),
            HttpEntity.class,String.class)).thenReturn("Bad request");

        ResponseEntity expected = mockBlueprintDistributionServiceImpl.distributeBlueprint(BP_DISTRIBUTION_MODEL_ID,BP_DISTRIBUTION_ENV);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(deploymentArtifactService, times(1)).findDeploymentArtifactById(BP_DISTRIBUTION_MODEL_ID);
        verify(blueprintDistributionUtils, times(3)).getBlueprintDashboardURL(BP_DISTRIBUTION_ENV);
        verify(blueprintDistributionUtils, times(1)).getBlueprintDashboardUserName(BP_DISTRIBUTION_ENV);
        verify(blueprintDistributionUtils, times(1)).getBlueprintDashboardPassword(BP_DISTRIBUTION_ENV);
        verify(deploymentArtifactGateway, times(1)).save(any());

    }



}