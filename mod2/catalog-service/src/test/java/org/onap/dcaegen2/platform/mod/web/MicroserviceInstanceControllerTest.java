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

import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceUpdateRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.onap.dcaegen2.platform.mod.web.controller.MicroserviceInstanceController;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MicroserviceInstanceController.class)
class MicroserviceInstanceControllerTest {

    @MockBean
    MsInstanceService service;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAll() throws Exception {
        MsInstance instance_1 = MsInstance.builder().id("123").build();
        MsInstance instance_2 = MsInstance.builder().id("345").build();

        when(service.getAll()).thenReturn(Arrays.asList(instance_1,instance_2));

        mockMvc.perform(get("/api/microservice-instance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)));
        verify(service, times(1)).getAll();
    }

    @Test
    void createMsInstance_shouldReturn201AndResponseBody() throws Exception {

        MsInstanceRequest request = getMsInstanceMockRequest();
        MsInstance msInstance = createMsInstance();

        when(service.createMicroserviceInstance(BaseMsObjectMother.BASE_MS_NAME, request)).thenReturn(msInstance);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/microservice-instance/"+ BaseMsObjectMother.BASE_MS_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BaseMsObjectMother.asJsonString(request)).accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.name",equalTo(MS_INSTANCE_NAME)));

        verify(service, times(1)).createMicroserviceInstance(BaseMsObjectMother.BASE_MS_NAME,request);
    }

    @Test
    void patchMsInstance_shouldReturn204NoContent() throws Exception{
        //given
        String updatedVersion = "updatedVersion";
        String updatedRelease = "updatedRelease";

        MsInstance mockedMsInstance = prepareMockMsInstance(updatedVersion, updatedRelease);
        String msInstanceId = mockedMsInstance.getId();

        MsInstanceUpdateRequest updateRequest = prepareMsInstanceUpdateRequest(updatedVersion, updatedRelease);

       when(service.updateMsInstance(updateRequest, msInstanceId)).thenReturn(mockedMsInstance);

       mockMvc.perform(patch("/api/microservice-instance/" + msInstanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BaseMsObjectMother.asJsonString(updateRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.release", equalTo(updatedRelease)))
                        .andExpect(jsonPath("$.version", equalTo(updatedVersion)))
                        .andExpect(jsonPath("$.metadata.scrumLead", equalTo("updatedScrumLead")));

       verify(service, times(1)).updateMsInstance(updateRequest, msInstanceId);
    }

    private MsInstanceUpdateRequest prepareMsInstanceUpdateRequest(String updatedVersion, String updatedRelease) {
        MsInstanceUpdateRequest updateRequest = new MsInstanceUpdateRequest();
        updateRequest.setRelease(updatedRelease);
        updateRequest.setVersion(updatedVersion);
        updateRequest.setMetadata(prepareMetadataToBeUpdated());
        return updateRequest;
    }

    private Map<String, Object> prepareMetadataToBeUpdated() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scrumLead", "updatedScrumLead");
        return metadata;
    }

    private MsInstance prepareMockMsInstance(String updatedVersion, String updatedRelease) {
        MsInstance msInstanceToBeUpdated = MsInstanceObjectMother.createMsInstance();
        msInstanceToBeUpdated.setVersion(updatedVersion);
        msInstanceToBeUpdated.setRelease(updatedRelease);
        msInstanceToBeUpdated.getMetadata().put("scrumLead", "updatedScrumLead");
        return msInstanceToBeUpdated;
    }

}