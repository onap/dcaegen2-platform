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

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.BlueprintFileNameCreateException;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.DeploymentArtifactNotFound;
import org.onap.dcaegen2.platform.mod.model.restapi.DeploymentArtifactPatchRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.web.controller.DeploymentArtifactController;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(DeploymentArtifactController.class)
class DeploymentArtifactControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeploymentArtifactService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void test_GenerateDeploymentArtifactEndpoint_returnsBlueprint() throws Exception{
        String url = String.format("/api/deployment-artifact/%s?user=%s", MsInstanceObjectMother.MS_INSTANCE_ID, MsInstanceObjectMother.USER);
        DeploymentArtifact response = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);

        Mockito.when(service.generateDeploymentArtifact(MsInstanceObjectMother.MS_INSTANCE_ID, MsInstanceObjectMother.USER)).thenReturn(response);

        mockMvc.perform(post(url))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.fileName").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());

        Mockito.verify(service, Mockito.times(1)).generateDeploymentArtifact(MsInstanceObjectMother.MS_INSTANCE_ID, MsInstanceObjectMother.USER);
    }

    @Test
    void test_RaiseExceptionIfBlueprintNameCanNotBeCreated() throws Exception{
        String url = String.format("/api/deployment-artifact/%s?user=%s", MsInstanceObjectMother.MS_INSTANCE_ID,
                MsInstanceObjectMother.USER);
        DeploymentArtifact response = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);

        Mockito.when(service.generateDeploymentArtifact(MsInstanceObjectMother.MS_INSTANCE_ID, MsInstanceObjectMother.USER)).thenThrow(new BlueprintFileNameCreateException(""));

        mockMvc.perform(post(url))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    void test_GetAllDeploymentArtifactsShouldReturnList() throws Exception{
        List<DeploymentArtifact> daos = createDaos();
        Mockito.when(service.getAllDeploymentArtifacts()).thenReturn(daos);

        mockMvc.perform(get("/api/deployment-artifact"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));

    }

    private List<DeploymentArtifact> createDaos() {
        DeploymentArtifact dao1 = new DeploymentArtifact();
        dao1.setId("123");
        DeploymentArtifact dao2 = new DeploymentArtifact();
        dao2.setId("456");

        return Arrays.asList(dao1, dao2);
    }

    @Test
    void test_GetAllDeploymentArtifactTestShouldReturnAList() throws Exception{

        mockMvc.perform(get(DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL + DeploymentArtifactController.GET_STATUSES))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(DeploymentArtifactStatus.values().length)));
    }

    @Test
    void test_ifUserIsNullRaiseException() throws Exception{
        String id = "id-123";
        String user = "";

        mockMvc.perform(patch(DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL + "/" + id + "?user=" + user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(new DeploymentArtifactPatchRequest())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void test_ChangeStatusOfDeploymentArtifact() throws Exception{

        String id = "id-123";
        String user = "user1";
        DeploymentArtifactPatchRequest partialDto = new DeploymentArtifactPatchRequest();
        partialDto.setStatus(DeploymentArtifactStatus.DEV_COMPLETE);

        mockMvc.perform(patch(DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL + "/" + id + "?user=" + user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(partialDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());

        Mockito.verify(service, Mockito.times(1)).updateDeploymentArtifact(id, partialDto, user);

    }

    @Test
    void test_deploymentArtifactIdNotFound() throws Exception{
        String wrongId = "wrong-id";
        DeploymentArtifactPatchRequest partialDto = new DeploymentArtifactPatchRequest();
        partialDto.setStatus(DeploymentArtifactStatus.DEV_COMPLETE);

        Mockito.doThrow(new DeploymentArtifactNotFound("")).when(service).
                updateDeploymentArtifact(wrongId, partialDto, "user-1");

        mockMvc.perform(patch(DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL + "/" + wrongId + "?user=" + "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(partialDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void test_deleteDeploymentArtifactEndpoint() throws Exception{
        String deploymentArtifactId = "id-123";
        String user = "user-1";
        mockMvc.perform(delete(DeploymentArtifactController.DEPLOYMENT_ARTIFACTS_BASE_URL + "/" + deploymentArtifactId + "?user=" + user))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
        Mockito.verify(service, Mockito.times(1)).deleteDeploymentArtifact(deploymentArtifactId);
    }
}