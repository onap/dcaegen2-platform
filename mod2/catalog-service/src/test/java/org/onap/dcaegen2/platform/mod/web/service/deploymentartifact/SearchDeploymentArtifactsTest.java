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
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SearchDeploymentArtifactsTest {

    private DeploymentArtifactServiceImpl service;

    @Mock
    private DeploymentArtifactGateway repository;

    private DeploymentArtifact artifact_1;
    private DeploymentArtifact artifact_2;
    private DeploymentArtifact artifact_3;

    @BeforeEach
    void setUp() {
        service = new DeploymentArtifactServiceImpl();
        service.setDeploymentArtifactGateway(repository);

        //given
        artifact_1 = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        artifact_1.getMsInstanceInfo().setRelease("2008");
        artifact_2 = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.DEV_COMPLETE);
        artifact_2.getMsInstanceInfo().setRelease("2010");
        artifact_3 = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.DEV_COMPLETE);
        artifact_3.getMsInstanceInfo().setRelease("2008");

    }

//    @Test
//    void findArtifacts_filteredWithRelease() throws Exception {
//        List<DeploymentArtifact> artifacts = Arrays.asList(artifact_1, artifact_3);
//        when(repository.findByReleaseOrStatusOfMsInstance("2008", null)).thenReturn(artifacts);
//
//        //when
//        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
//        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
//        filter.setRelease("2008");
//        search.setFilter(filter);
//
//        List<DeploymentArtifact> result = core.searchDeploymentArtifacts(search);
//
//        //assert
//        assertThat(result.size()).isEqualTo(2);
//    }
//
//    @Test
//    void findArtifacts_filteredWithStatus() throws Exception{
//        List<DeploymentArtifact> artifacts = Arrays.asList(artifact_2, artifact_3);
//        when(repository.findByReleaseOrStatusOfMsInstance(null, DeploymentArtifactStatus.DEV_COMPLETE))
//                .thenReturn(artifacts);
//
//        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
//        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
//        filter.setStatus(DeploymentArtifactStatus.DEV_COMPLETE);
//        search.setFilter(filter);
//
//        List<DeploymentArtifact> result = core.searchDeploymentArtifacts(search);
//        assertThat(result.size()).isEqualTo(2);
//
//    }
}
