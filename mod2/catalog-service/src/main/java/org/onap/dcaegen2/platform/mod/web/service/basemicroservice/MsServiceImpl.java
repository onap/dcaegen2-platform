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

package org.onap.dcaegen2.platform.mod.web.service.basemicroservice;

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsStatus;
import org.onap.dcaegen2.platform.mod.model.common.AuditFields;
import org.onap.dcaegen2.platform.mod.model.exceptions.ErrorMessages;
import org.onap.dcaegen2.platform.mod.model.exceptions.ResourceConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.basemicroservice.BaseMicroserviceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsServiceImpl implements MsService {

    @Autowired
    @Setter
    private BaseMicroserviceGateway repository;

    @Autowired
    @Setter
    private MsInstanceService msInstanceService;

    @Override
    public BaseMicroservice createMicroservice(MicroserviceCreateRequest microserviceRequest) {
        checkIfThereAreAnyConflicts(microserviceRequest); //TODO: Make fields unique in entity itself
        BaseMicroservice microservice = new BaseMsCreator().create(microserviceRequest);
        return repository.save(microservice);
    }

    /**
     * name, tag and serviceName are unique for the given ms. This method make sure that.
     * */
    private void checkIfThereAreAnyConflicts(MicroserviceCreateRequest microserviceRequest) {
        checkIfMsNameAlreadyExists(microserviceRequest.getName());
        checkIfMsTagAlreadyExists(microserviceRequest.getTag());
        checkiIfServiceNameAlreadyExists(microserviceRequest.getServiceName());
    }

    private void checkIfMsNameAlreadyExists(String msName) {
        if (repository.findByName(msName).isPresent())
            throw new ResourceConflictException(ErrorMessages.MICROSERVICE_NAME_CONFLICT_MESSAGE);
    }

    private void checkIfMsTagAlreadyExists(String msTag) {
        if (repository.findByTag(msTag).isPresent())
            throw new ResourceConflictException(ErrorMessages.MICROSERVICE_TAG_CONFLICT_MESSAGE);
    }

    private void checkiIfServiceNameAlreadyExists(String serviceName) {
        boolean serviceNameIsEmpty = serviceName == null || serviceName.isEmpty();
        if(serviceNameIsEmpty)
            return;
        if (repository.findByServiceName(serviceName).isPresent())
            throw new ResourceConflictException(ErrorMessages.MS_SERVICE_NAME_CONFLICT_MESSAGE);
    }

    @Override
    public List<BaseMicroservice> getAllMicroservices() {
        return repository.findAll();
    }

    @Override
    public BaseMicroservice getMicroserviceById(String baseMsId) {
        return repository.findById(baseMsId).orElseThrow(() ->
                new BaseMicroserviceNotFoundException(String.format("Microservice with id %s not found", baseMsId)));
    }

    @Override
    public BaseMicroservice getMicroserviceByName(String msName) {
        return repository.findByName(msName).orElseThrow(() ->
                new BaseMicroserviceNotFoundException(String.format("Microservice with name %s not found", msName)));
    }

    @Override
    public void updateMicroservice(String requestedMsId, MicroserviceUpdateRequest updateRequest) {
        BaseMicroservice microservice = getMicroserviceById(requestedMsId);
        updateMetadata(updateRequest, microservice);
        updateOtherFields(updateRequest, microservice);
        repository.save(microservice);
        msInstanceService.updateMicroserviceReference(microservice);
    }

    //TODO: Get rid of nulls!
    private void updateMetadata(MicroserviceUpdateRequest updateRequest, BaseMicroservice microservice) {
        if(updateRequest.getUser() != null){
            microservice.getMetadata().setUpdatedBy(updateRequest.getUser());
        }
        if(updateRequest.getMetadata() != null && updateRequest.getMetadata().containsKey("notes")){
            microservice.getMetadata().setNotes((String) updateRequest.getMetadata().get("notes"));
        }
        if(updateRequest.getMetadata() != null && updateRequest.getMetadata().containsKey("labels")){
            microservice.getMetadata().setLabels((List<String>) updateRequest.getMetadata().get("labels"));
        }
        microservice.getMetadata().setUpdatedOn(new Date());
    }

    private void updateOtherFields(MicroserviceUpdateRequest updateRequest, BaseMicroservice microservice) {
        if(updateRequest.getName() != null){
            updateName(updateRequest, microservice);
        }
        if(updateRequest.getType() != null){
            microservice.setType(updateRequest.getType());
        }
        if(updateRequest.getLocation() != null){
            microservice.setLocation(updateRequest.getLocation());
        }
        if(updateRequest.getServiceName() != null){
            updateServiceName(updateRequest, microservice);
        }
        if(updateRequest.getNamespace() != null){
            microservice.setNamespace(updateRequest.getNamespace());
        }
    }

    /**
     * If name requested in the updateRequest doesn't match the name of the ms record which is being worked on,
     * then only check for the uniqueness.
     */
    private void updateName(MicroserviceUpdateRequest updateRequest, BaseMicroservice microservice) {
        boolean notMatchesWithCurrentName = !updateRequest.getName().equals(microservice.getName());
        if(notMatchesWithCurrentName)
            checkIfMsNameAlreadyExists(updateRequest.getName());
        microservice.setName(updateRequest.getName());
    }

    /**
     * If serviceName requested in the updateRequest doesn't match the serviceName of the ms record which is
     * being worked on, then only check for the uniqueness.
     */
    private void updateServiceName(MicroserviceUpdateRequest updateRequest, BaseMicroservice microservice) {
        boolean notMatchesWithCurrentServiceName = !updateRequest.getServiceName().equals(microservice.getServiceName());
        if(notMatchesWithCurrentServiceName)
            checkiIfServiceNameAlreadyExists(updateRequest.getServiceName());
        microservice.setServiceName(updateRequest.getServiceName());
    }

    @Override
    public void saveMsInstanceReferenceToMs(BaseMicroservice microservice, MsInstance msInstance) {
        microservice.getMsInstances().add(getMsInstanceReference(msInstance));
        repository.save(microservice);
    }

    @Override
    public void updateMsInstanceRef(MsInstance msInstance) {
        BaseMicroservice microservice = getMicroserviceById((String) msInstance.getMsInfo().get("id"));
        List<Map<String, String>> msInstancesRef = microservice.getMsInstances();
        msInstancesRef.forEach((ref) -> {
            if(ref.get("id").equals(msInstance.getId()))
                ref.put("name", msInstance.getName());
        });
        repository.save(microservice);
    }

    private Map<String, String> getMsInstanceReference(MsInstance msInstance) {
        Map<String,String> msInstanceInfo = new HashMap<>();
        msInstanceInfo.put("id", msInstance.getId());
        msInstanceInfo.put("name", msInstance.getName());
        return msInstanceInfo;
    }

    private class BaseMsCreator {

        BaseMicroservice create(MicroserviceCreateRequest createRequest) {
            BaseMicroservice microservice = new BaseMicroservice();
            microservice.setLocation(createRequest.getLocation());
            microservice.setName(createRequest.getName());
            microservice.setTag(createRequest.getTag());
            microservice.setServiceName(createRequest.getServiceName());
            microservice.setNamespace(createRequest.getNamespace());
            microservice.setStatus(BaseMsStatus.ACTIVE);
            microservice.setType(createRequest.getType());
            microservice.setMetadata(getMetadataFields(createRequest));
            return microservice;
        }

        private AuditFields getMetadataFields(MicroserviceCreateRequest request) {
            AuditFields auditFields = AuditFields.builder().build();
            auditFields.setCreatedBy(request.getUser());
            auditFields.setCreatedOn(new Date());

            if (request.getMetadata().containsKey("notes"))
                auditFields.setNotes((String) request.getMetadata().get("notes"));
            if (request.getMetadata().containsKey("labels"))
//                auditFields.setLabels((List<String>) request.getMetadata().get("labels"));
                auditFields.setLabels(request.getMetadata().get("labels"));

            return auditFields;

        }
    }
}
