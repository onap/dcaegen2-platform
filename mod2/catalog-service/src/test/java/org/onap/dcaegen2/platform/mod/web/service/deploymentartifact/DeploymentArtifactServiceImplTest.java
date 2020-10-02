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
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.DeploymentArtifactNotFound;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.DeploymentArtifactPatchRequest;
import org.onap.dcaegen2.platform.mod.model.specification.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.SpecificationObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentArtifactServiceImplTest {

    private DeploymentArtifactServiceImpl deploymentArtifactService;

    @Mock
    private MsInstanceService msInstanceService;

    @Mock
    private DeploymentArtifactGeneratorStrategy deploymentArtifactGeneratorStrategy;

    @Mock
    private DeploymentArtifactGateway repository;

    @Mock
    private ArtifactFileNameCreator fileNameCreator;

    @Mock
    private DeploymentArtifactStatusChangeHandler deploymentArtifactStatusChangeHandler;

    private MsInstance msInstance;

    DeploymentArtifact deploymentArtifact;

    @BeforeEach
    void setUp() {
        //Initiated the deployment artifact core with mocks
        deploymentArtifactService = new DeploymentArtifactServiceImpl();
        deploymentArtifactService.setDeploymentArtifactGeneratorStrategy(deploymentArtifactGeneratorStrategy);
        deploymentArtifactService.setDeploymentArtifactGateway(repository);
        deploymentArtifactService.setMsInstanceService(msInstanceService);
        deploymentArtifactService.setFileNameCreator(fileNameCreator);
        deploymentArtifactService.setStatusChangeHandler(deploymentArtifactStatusChangeHandler);
    }

    private void setupMockBehaviours() {
        //Mock methods
        deploymentArtifact = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        msInstance = MsInstanceObjectMother.createMsInstance();

        when(deploymentArtifactGeneratorStrategy.generateForRelease(msInstance.getActiveSpec(), msInstance.getRelease()))
                .thenReturn(DeploymentArtifactObjectMother.createBlueprintResponse());
        when(repository.save(any())).thenReturn(deploymentArtifact);
        when(fileNameCreator.createFileName(any(MsInstance.class), any(Integer.class))).thenReturn(BASE_MS_TAG + "_" +
                msInstance.getActiveSpec().getType().toString().toLowerCase() + "_" + msInstance.getRelease() + "_1.yaml");
    }

    @Test
    void test_getAllDeploymentArtifactInstance() throws Exception{
        when(repository.findAll()).thenReturn(Arrays.asList(deploymentArtifact));
        List<DeploymentArtifact> deployments = deploymentArtifactService.getAllDeploymentArtifacts();
        assertThat(deployments.size()).isEqualTo(1);
    }

    @Test
    void test_GenerateForRelease_shouldReturnCorrectBlueprint(){
        Specification specification = SpecificationObjectMother.getMockSpecification(DeploymentType.K8S);
        when(deploymentArtifactGeneratorStrategy.generateForRelease(specification, "")).thenReturn(DeploymentArtifactObjectMother.createBlueprintResponse());
        Map<String, Object> response = deploymentArtifactGeneratorStrategy.generateForRelease(specification, "");
        verify(deploymentArtifactGeneratorStrategy, atLeastOnce()).generateForRelease(specification,"");
        assertThat(response).isNotNull();
        assertThat(response.get("content")).isNotNull();
        assertThat(response.get("fileName")).isNotNull();
        assertThat((String)response.get("content")).contains("tosca_definitions_version");

    }

    @Test
    void test_GenerateBlueprint_shouldReturnCorrectBlueprint() throws Exception{

        setupMockBehaviours();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);

        //act
        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);

        //assert
        verify(msInstanceService, atLeastOnce()).getMsInstanceById(MS_INSTANCE_ID);
        verify(repository, times(1)).save(any());
        assertThat(resultDAO.getContent()).contains("tosca_definitions_version");
        assertThat(resultDAO.getId()).isNotEmpty();
        assertThat(resultDAO.getVersion()).isEqualTo(1);
        assertThat(resultDAO.getStatus()).isEqualTo(DeploymentArtifactStatus.IN_DEV);
        assertThat(resultDAO.getMsInstanceInfo().getId()).isEqualTo(MS_INSTANCE_ID);
        assertThat(resultDAO.getMsInstanceInfo().getName()).isEqualTo(msInstance.getName());
        assertThat(resultDAO.getMsInstanceInfo().getRelease()).isEqualTo(msInstance.getRelease());
        assertThat(resultDAO.getSpecificationInfo().get("id")).isNotNull();
        assertThat(resultDAO.getMetadata().get("createdBy")).isEqualTo(USER);

    }

    @Test
    void test_deploymentVersionIsInitatedWith1() throws Exception{
        setupMockBehaviours();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);
        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);
        assertThat(resultDAO.getVersion()).isEqualTo(1);
    }

    @Test
    void test_deploymentVersionIncrementsForEachAddForAnInstance() throws Exception{
        setupMockBehaviours();
        MsInstance msInstanceWithRef = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstanceWithRef);

        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);

        assertThat(resultDAO.getVersion()).isEqualTo(2);
    }

    @Test
    void test_deploymentArtifactRefAddedToMsInstanceForFirstTime() throws Exception{
        setupMockBehaviours();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);

        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);
        assertThat(msInstance.getDeploymentArtifactsInfo().getMostRecentVersion()).isEqualTo(1);

        List<String> deploymentArtifactList = msInstance.getDeploymentArtifactsInfo().getDeploymentArtifacts();
        assertThat(deploymentArtifactList.size()).isEqualTo(1);
        assertThat(deploymentArtifactList.get(0)).isEqualTo(resultDAO.getId());
    }

    @Test
    void test_deploymentArtifactRefAddedToMsInstanceForSecondTime() throws Exception{
        setupMockBehaviours();
        MsInstance msInstanceWithRef = MsInstanceObjectMother.getMsInstanceWithExistingDeploymentArtifactRef();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstanceWithRef);

        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);
        assertThat(msInstanceWithRef.getDeploymentArtifactsInfo().getMostRecentVersion()).isEqualTo(2);

        List<String> deploymentArtifactList = msInstanceWithRef.getDeploymentArtifactsInfo().getDeploymentArtifacts();
        assertThat(deploymentArtifactList.size()).isEqualTo(2);
        assertThat(deploymentArtifactList.get(1)).isEqualTo(resultDAO.getId());
    }

    @Test
    void test_ifMsInstanceIsPersistedAfterDeploymentArtifactCreation() throws Exception{
        setupMockBehaviours();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);
        deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);
        verify(msInstanceService, times(1)).updateMsInstance(msInstance);
    }

    @Test
    void test_blueprintFileNameValidation() throws Exception{
        setupMockBehaviours();
        when(msInstanceService.getMsInstanceById(MS_INSTANCE_ID)).thenReturn(msInstance);
        DeploymentArtifact resultDAO = deploymentArtifactService.generateDeploymentArtifact(MS_INSTANCE_ID, USER);
        System.out.println(resultDAO.getFileName());
        assertThat(resultDAO.getFileName().contains(BASE_MS_TAG)).isTrue();
    }

    @Test
    void test_updateStatusForDeploymentArtifact() throws Exception{
        //arrange
        deploymentArtifact = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);

        DeploymentArtifactPatchRequest dtoWithStatus = new DeploymentArtifactPatchRequest();
        dtoWithStatus.setStatus(DeploymentArtifactStatus.NOT_NEEDED);

        when(repository.findById("id-123")).thenReturn(Optional.of(deploymentArtifact));

            //Mocking void method from DeploymentArtifactStatusChangeHandler
        doAnswer(invocation -> {
            deploymentArtifact.setStatus(DeploymentArtifactStatus.NOT_NEEDED);
            return null;
        }).when(deploymentArtifactStatusChangeHandler).handleStatusChange(dtoWithStatus.getStatus(), deploymentArtifact);

        //act
        deploymentArtifactService.updateDeploymentArtifact("id-123", dtoWithStatus, "user1");

        //assert
        assertThat(deploymentArtifact.getStatus()).isEqualTo(DeploymentArtifactStatus.NOT_NEEDED);
        assertThat(deploymentArtifact.getMetadata().get("updatedBy")).isEqualTo("user1");
        assertThat(deploymentArtifact.getMetadata().get("updatedOn")).isNotNull();

        verify(deploymentArtifactStatusChangeHandler, times(1)).handleStatusChange(dtoWithStatus.getStatus(),
                deploymentArtifact);
        verify(msInstanceService, times(1)).
                updateStatusBasedOnDeploymentArtifactsStatuses(deploymentArtifact.getMsInstanceInfo().getId());
        verify(repository, times(1)).save(deploymentArtifact);
    }

    @Test
    void test_findDeploymentArtifactById() throws Exception{
        //arrange
        deploymentArtifact = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        when(repository.findById("id-123")).thenReturn(Optional.of(deploymentArtifact));

        DeploymentArtifact result = deploymentArtifactService.findDeploymentArtifactById("id-123");

        assertThat(result).isEqualTo(deploymentArtifact);
    }

    @Test
    void test_findByIdWithInvalidId() throws Exception{
        when(repository.findById("invalid-id")).thenReturn(Optional.empty());
        assertThatExceptionOfType(DeploymentArtifactNotFound.class).isThrownBy(
                () -> deploymentArtifactService.findDeploymentArtifactById("invalid-id"));
    }

    @Test
    void test_deleteDeploymentArtifact() throws Exception{
        DeploymentArtifact deploymentArtifact =
                DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        String id = deploymentArtifact.getId();

        when(repository.findById(id)).thenReturn(Optional.of(deploymentArtifact));

        deploymentArtifactService.deleteDeploymentArtifact(id);
        verify(msInstanceService, times(1))
                .removeDeploymentArtifactFromMsInstance(deploymentArtifact);
        verify(repository, times(1)).deleteById(id);
    }
}