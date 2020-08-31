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

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsLocation;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsType;
import org.onap.dcaegen2.platform.mod.model.exceptions.ResourceConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.basemicroservice.BaseMicroserviceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;
import org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.BaseMicroserviceGateway;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.MsServiceImpl;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.onap.dcaegen2.platform.mod.model.exceptions.ErrorMessages.MICROSERVICE_NAME_CONFLICT_MESSAGE;
import static org.onap.dcaegen2.platform.mod.model.exceptions.ErrorMessages.MICROSERVICE_TAG_CONFLICT_MESSAGE;
import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.createMockMsObject;
import static org.onap.dcaegen2.platform.mod.objectmothers.BaseMsObjectMother.createMockMsRequest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsServiceImplTest {

    @Mock
    private BaseMicroserviceGateway repository;

    @Mock
    private MsInstanceService msInstanceService;

    @Spy
    private MsServiceImpl baseMsService = new MsServiceImpl();

    @BeforeEach
    void setup() throws Exception{
        baseMsService.setRepository(repository);
        baseMsService.setMsInstanceService(msInstanceService);
    }

    /**GET MICROSERVICE TESTS*/
    @Test
    void getAll() {
        //arrange
        BaseMicroservice ms1 = new BaseMicroservice();
        ms1.setName("HelloWorld1");
        BaseMicroservice ms2 = new BaseMicroservice();
        ms2.setName("HelloWorld2");

        when(repository.findAll()).thenReturn(Arrays.asList(ms1, ms2));

        //act
        List<BaseMicroservice> microservices = baseMsService.getAllMicroservices();

        //assert
        assertThat(microservices).hasSizeGreaterThan(0);
    }

    @Test
    void test_getMicroserviceById() throws Exception{
        BaseMicroservice expectedMicroservice = BaseMsObjectMother.createMockMsObject();
        String baseMsId = BaseMsObjectMother.BASE_MS_ID;

        when(repository.findById(baseMsId)).thenReturn(Optional.of(expectedMicroservice));

        BaseMicroservice resultMicroservice = baseMsService.getMicroserviceById(baseMsId);

        assertThat(resultMicroservice).isEqualTo(expectedMicroservice);
        verify(repository, times(1)).findById(baseMsId);
    }

    @Test
    void test_ifMicroserviceNotFoundRaiseException() throws Exception{
        BaseMicroservice expectedMicroservice = BaseMsObjectMother.createMockMsObject();
        String baseMsId = BaseMsObjectMother.BASE_MS_ID;

        when(repository.findById(baseMsId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(BaseMicroserviceNotFoundException.class).isThrownBy(
                () -> baseMsService.getMicroserviceById(baseMsId)
        );
    }

    /**CREATE MICROSERVICE TESTS*/
    @Test
    void createMicroservice() {
        //arrange
        MicroserviceCreateRequest microserviceRequest = createMockMsRequest();
        BaseMicroservice expected = createMockMsObject();

        when(repository.save(any())).thenReturn(expected);

        //act
        BaseMicroservice actual = baseMsService.createMicroservice(microserviceRequest);

        //assert
        assertThat(actual.getMetadata().getCreatedBy()).isEqualTo(microserviceRequest.getUser());
        assertThat(actual.getMetadata().getUpdatedBy()).isEqualTo(microserviceRequest.getUser());
    }

    @Test
    void AddingMsWithDuplicateName_shouldThrowException() throws Exception{
        //arrange
        MicroserviceCreateRequest microserviceRequest = createMockMsRequest();
        BaseMicroservice existedMicroservice = createMockMsObject();

        when(repository.findByName(any())).thenReturn(Optional.of(existedMicroservice));

        //act/assert
        assertThatThrownBy(() -> baseMsService.createMicroservice((microserviceRequest)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage(MICROSERVICE_NAME_CONFLICT_MESSAGE);
    }
    @Test
    void AddingMsWithDuplicateTag_shouldThrowException() throws Exception{
        //arrange
        MicroserviceCreateRequest microserviceRequest = createMockMsRequest();
        BaseMicroservice existedMicroservice = createMockMsObject();

        when(repository.findByTag(any())).thenReturn(Optional.of(existedMicroservice));

        //act/assert
        assertThatThrownBy(() -> baseMsService.createMicroservice((microserviceRequest)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage(MICROSERVICE_TAG_CONFLICT_MESSAGE);
    }

    /**UPDATE MICROSERVICE TESTS*/
    @Test
    void test_updateMicroservice() throws Exception{
        MicroserviceUpdateRequest updateRequest = createUpdateMsRequest();

        BaseMicroservice msToBeUpdated = BaseMsObjectMother.createMockMsObject();
        Date updateTimeBefore = new Date(msToBeUpdated.getMetadata().getUpdatedOn().getTime());

        String baseMsId = BaseMsObjectMother.BASE_MS_ID;

        when(repository.findById(baseMsId)).thenReturn(Optional.of(msToBeUpdated));

        baseMsService.updateMicroservice(baseMsId, updateRequest);

        //assert
        assertUpdatedMsFileds(updateRequest, msToBeUpdated, updateTimeBefore);
        verify(baseMsService, times(1)).getMicroserviceById(baseMsId);
        verify(msInstanceService, times(1)).updateMicroserviceReference(msToBeUpdated);
        verify(repository, times(1)).save(msToBeUpdated);
    }

/*    @Test
    void test_msTagChangeShouldNotBeAllowed() throws Exception{
        MicroserviceCreateRequest updateRequest = new MicroserviceCreateRequest();
        updateRequest.setTag("updateTag");
        String baseMsId = BaseMsObjectMother.BASE_MS_ID;

        assertThatExceptionOfType(OperationNotAllowedException.class).isThrownBy(
                () -> baseMsService.updateMicroservice(baseMsId, updateRequest)
        );
    }*/

    private void assertUpdatedMsFileds(MicroserviceUpdateRequest updateRequest, BaseMicroservice msToBeUpdated,
                                       Date updateTimeBefore) {
        assertThat(msToBeUpdated.getName()).isEqualTo(updateRequest.getName());
        assertThat(msToBeUpdated.getLocation()).isEqualTo(updateRequest.getLocation());
        assertThat(msToBeUpdated.getServiceName()).isEqualTo(updateRequest.getServiceName());
        assertThat(msToBeUpdated.getNamespace()).isEqualTo(updateRequest.getNamespace());
        assertThat(msToBeUpdated.getType()).isEqualTo(updateRequest.getType());

        assertThat(msToBeUpdated.getMetadata().getUpdatedBy()).isEqualTo(updateRequest.getUser());
        assertThat(msToBeUpdated.getMetadata().getUpdatedOn()).isNotEqualTo(updateTimeBefore);

        assertThat(msToBeUpdated.getMetadata().getNotes()).isEqualTo(updateRequest.getMetadata().get("notes"));
        assertThat(msToBeUpdated.getMetadata().getLabels()).isEqualTo(updateRequest.getMetadata().get("labels"));
    }

    private MicroserviceUpdateRequest createUpdateMsRequest() {
        MicroserviceUpdateRequest updateRequest = new MicroserviceUpdateRequest();
        updateRequest.setName("updatedName");
        updateRequest.setLocation(BaseMsLocation.EDGE);
        updateRequest.setServiceName("updatedServiceName");
        updateRequest.setNamespace("updatedNameSpace");
        updateRequest.setType(BaseMsType.ANALYTIC);
        updateRequest.setUser("updater");

        Map<String, Object> metadata = new HashMap();
        metadata.put("notes", "updatedNote");
        metadata.put("labels", Arrays.asList("updatedLabel1", "updatedLabel2"));
        updateRequest.setMetadata(metadata);
        return updateRequest;
    }
}