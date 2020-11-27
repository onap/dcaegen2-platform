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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.exceptions.OperationNotAllowedException;
import org.onap.dcaegen2.platform.mod.model.exceptions.ResourceConflictException;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother;
import org.onap.dcaegen2.platform.mod.web.controller.BaseMicroserviceController;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.MsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

import static org.onap.dcaegen2.platform.mod.model.exceptions.ErrorMessages.MICROSERVICE_NAME_CONFLICT_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BaseMicroserviceController.class)
class BaseMicroserviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MsService mockBaseMsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void test_GetAllBaseMicroservices_returnsListOfDTOs() throws Exception {
        //arrange
        BaseMicroservice ms1 = new BaseMicroservice();
        ms1.setName("HelloWorld1");
        BaseMicroservice ms2 = new BaseMicroservice();
        ms2.setName("HelloWorld2");

        Mockito.when(mockBaseMsService.getAllMicroservices()).thenReturn(Arrays.asList(ms1, ms2));

        //act/assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/base-microservice")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    void test_addBaseMicroservice_returnsMicroservice() throws Exception {
        //arrange
        MicroserviceCreateRequest microserviceRequest = BaseMsObjectMother.createMockMsRequest();

        //response
        BaseMicroservice microserviceDao = BaseMsObjectMother.createMockMsObject();

        Mockito.when(mockBaseMsService.createMicroservice(microserviceRequest)).thenReturn(microserviceDao);

        //act/assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/base-microservice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(microserviceRequest))
                .characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect((MockMvcResultMatchers.jsonPath("$.id", Matchers.equalTo(BaseMsObjectMother.BASE_MS_ID))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.equalTo(BaseMsObjectMother.BASE_MS_NAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.metadata.createdBy", Matchers.equalTo(BaseMsObjectMother.USER)));
    }

    @Test
    void test_addBaseMicroserviceWithDuplicateName_shouldThrowConflictError() throws Exception{
        //arrange
        MicroserviceCreateRequest microserviceRequest = BaseMsObjectMother.createMockMsRequest();
        Mockito.when(mockBaseMsService.createMicroservice(ArgumentMatchers.any())).thenThrow(new ResourceConflictException(MICROSERVICE_NAME_CONFLICT_MESSAGE));

        //act/assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/base-microservice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(microserviceRequest)))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void test_updateBaseMicroserviceEndpoint() throws Exception{
        MicroserviceUpdateRequest microserviceRequest = BaseMsObjectMother.createUpdateMsRequest();
        String requestedMsId = "id-123";

        mockMvc.perform(MockMvcRequestBuilders.patch(String.format(BaseMicroserviceController.API_BASE_MICROSERVICE + "/%s", requestedMsId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(microserviceRequest))
                .characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        Mockito.verify(mockBaseMsService, Mockito.times(1)).updateMicroservice(requestedMsId, microserviceRequest);
    }

    @Test
    void test_OperationNotAllowedExceptionThrows409() throws Exception{
        MicroserviceUpdateRequest microserviceRequest = BaseMsObjectMother.createUpdateMsRequest();
        String requestedMsId = "id-123";
        Mockito.doThrow(new OperationNotAllowedException("")).
                when(mockBaseMsService).updateMicroservice(requestedMsId, microserviceRequest);

        mockMvc.perform(MockMvcRequestBuilders.patch(String.format(BaseMicroserviceController.API_BASE_MICROSERVICE + "/%s", requestedMsId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(microserviceRequest)))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void test_validateMsRequestShouldThrowCorrectResponse() throws Exception {
        //arrange
        MicroserviceCreateRequest microserviceRequest = BaseMsObjectMother.createMockMsRequest();
        microserviceRequest.setName(" ");
        microserviceRequest.setTag("123");
        microserviceRequest.setServiceName("123");
        microserviceRequest.setUser(" ");

        //response
        BaseMicroservice microserviceDao = BaseMsObjectMother.createMockMsObject();

        Mockito.when(mockBaseMsService.createMicroservice(microserviceRequest)).thenReturn(microserviceDao);

        //act/assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/base-microservice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BaseMsObjectMother.asJsonString(microserviceRequest))
                .characterEncoding("utf-8"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.equalTo("Validation failed.")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors", Matchers.hasSize(4)))
                ;
    }
}