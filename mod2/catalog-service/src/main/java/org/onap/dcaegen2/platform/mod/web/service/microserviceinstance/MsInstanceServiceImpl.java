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

package org.onap.dcaegen2.platform.mod.web.service.microserviceinstance;

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.exceptions.msinstance.MsInstanceAlreadyExistsException;
import org.onap.dcaegen2.platform.mod.model.exceptions.msinstance.MsInstanceNotFoundException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstanceStatus;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceUpdateRequest;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.MsService;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MsInstance Service implementation
 */
@Service
@Setter
@Slf4j
public class MsInstanceServiceImpl implements MsInstanceService {

    @Autowired
    private MsInstanceGateway msInstanceRepository;

    @Autowired
    private MsService msService;

    @Autowired
    private MsInstanceStatusChangeHandler msInstanceStatusChangeHandler;

    @Autowired
    private SpecificationService specificationService;

    @Autowired
    private DeploymentArtifactService deploymentArtifactService;

    @Override
    public List<MsInstance> getAll() {
        return msInstanceRepository.findAll();
    }

    @Override
    @Transactional
    public MsInstance createMicroserviceInstance(String msName, MsInstanceRequest request) {
        BaseMicroservice microservice = msService.getMicroserviceByName(msName);
        checkIftheCombinationOfNameAndReleaseIsUnique(request.getName(), request.getRelease());
        MsInstance msInstance = new MsInstanceCreator(request, microservice).create();
        MsInstance savedMsInstance = msInstanceRepository.save(msInstance);
        msService.saveMsInstanceReferenceToMs(microservice, savedMsInstance);
        return savedMsInstance;
    }

    private void checkIftheCombinationOfNameAndReleaseIsUnique(String name, String release) {
        if (msInstanceRepository.findByNameAndRelease(name, release).isPresent())
            throw new MsInstanceAlreadyExistsException();
    }

    @Override
    public MsInstance getMsInstanceById(String id) {
        return msInstanceRepository.findById(id).orElseThrow(() ->
                new MsInstanceNotFoundException(String.format("Ms Instance with id %s not found", id)));
    }

    @Override
    public void updateMsInstance(MsInstance msInstance) {
        log.info("Saving the msInstance {} to database..", msInstance);
        if(msInstance != null) msInstanceRepository.save(msInstance);
    }

    @Override
    public void updateStatusBasedOnDeploymentArtifactsStatuses(String msInstanceId) {
        MsInstance msInstance = getMsInstanceById(msInstanceId);
        msInstanceStatusChangeHandler.updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);
        updateMsInstance(msInstance);
    }

    @Override
    @Transactional
    public void removeDeploymentArtifactFromMsInstance(DeploymentArtifact deploymentArtifact) {
        MsInstance msInstance = getMsInstanceById(deploymentArtifact.getMsInstanceInfo().getId());
        removeDeploymentArtifactReferenceFromMsInstance(msInstance, deploymentArtifact.getId());
        msInstanceStatusChangeHandler.updateStatusBasedOnDeploymentArtifactsStatuses(msInstance);
        updateMsInstance(msInstance);
    }

    @Override
    //TODO: update msInstanceReference in specification and deployment artifact
    public void updateMicroserviceReference(BaseMicroservice microservice) {
        List<Map<String, String>> msInstanceRefs = microservice.getMsInstances();
        for(Map<String, String> ref : msInstanceRefs){
            MsInstance msInstance = getMsInstanceById(ref.get("id"));
            msInstance.setName(microservice.getName());
            msInstance.getMsInfo().put("name", microservice.getName());
            cascadeUpdates(msInstance);
            msInstanceRepository.save(msInstance);
        }
    }

    @Override
    @Transactional
    public MsInstance updateMsInstance(MsInstanceUpdateRequest updateRequest, String msInstanceId) {
        MsInstance msInstance = getMsInstanceById(msInstanceId);
        updateRelease(updateRequest, msInstance);
        updateVersion(updateRequest, msInstance);
        updateMetadata(updateRequest, msInstance);
        cascadeUpdates(msInstance);
        return msInstanceRepository.save(msInstance);
    }

    private void cascadeUpdates(MsInstance msInstance) {
        specificationService.updateMsInstanceRef(msInstance);
        deploymentArtifactService.updateMsInstanceRef(msInstance);
        msService.updateMsInstanceRef(msInstance);
    }

    private void updateMetadata(MsInstanceUpdateRequest updateRequest, MsInstance msInstance) {
        if(updateRequest.getMetadata() != null){
            msInstance.getMetadata().putAll(updateRequest.getMetadata());
        }

        msInstance.getMetadata().put("updatedOn", new Date());
        msInstance.getMetadata().put("updatedBy", updateRequest.getUser());
    }

    private void updateVersion(MsInstanceUpdateRequest updateRequest, MsInstance msInstance) {
        if(updateRequest.getVersion() != null){
            msInstance.setVersion(updateRequest.getVersion());
        }
    }

    private void updateRelease(MsInstanceUpdateRequest updateRequest, MsInstance msInstance) {
        if(updateRequest.getRelease() != null) {
            if(!updateRequest.getRelease().equals(msInstance.getRelease()))
                checkIftheCombinationOfNameAndReleaseIsUnique(msInstance.getName(), updateRequest.getRelease());
            msInstance.setRelease(updateRequest.getRelease());
        }
    }

    private void removeDeploymentArtifactReferenceFromMsInstance(MsInstance msInstance, String deploymentArtifactId) {
        if(msInstance.getDeploymentArtifactsInfo() != null){
            List<String> refIds = msInstance.getDeploymentArtifactsInfo().getDeploymentArtifacts();
            refIds.remove(deploymentArtifactId);
        }
    }

    private class MsInstanceCreator {
        private MsInstanceRequest request;
        private BaseMicroservice microserviceDAO;

        MsInstanceCreator(MsInstanceRequest request, BaseMicroservice microserviceDAO) {
            this.request = request;
            this.microserviceDAO = microserviceDAO;
        }

        MsInstance create() {
            //prepare MsInstance from the request
            return MsInstance.builder()
                    .name(request.getName())
                    .release(request.getRelease())
                    .status(MsInstanceStatus.NEW)
                    .version(request.getVersion())
                    .msInfo(getMsReference(microserviceDAO))
                    .metadata(getMetadata(request))
                    .build();
        }

        private Map<String, Object> getMsReference(BaseMicroservice microserviceDAO) {
            Map<String,Object> msInfo = new HashMap<>();
            msInfo.put("id", microserviceDAO.getId());
            msInfo.put("name", microserviceDAO.getName());
            msInfo.put("tag", microserviceDAO.getTag());
            return msInfo;
        }

        private Map<String, Object> getMetadata(MsInstanceRequest request) {
            Map<String, Object> metadata = request.getMetadata();
            metadata.put("createdBy", request.getUser());
            metadata.put("createdOn", new Date());
            return metadata;
        }
    }
}
