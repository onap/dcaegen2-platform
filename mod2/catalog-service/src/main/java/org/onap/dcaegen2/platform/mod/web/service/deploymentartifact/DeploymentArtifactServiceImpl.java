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
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactSearch;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.MsInstanceInfo;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.DeploymentArtifactNotFound;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.DeploymentArtifactsRef;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.DeploymentArtifactPatchRequest;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

//TODO: Abstract out Object Construction logic
@Service
@Slf4j
@Setter
public class DeploymentArtifactServiceImpl implements DeploymentArtifactService{

    private static final String VERSION_KEY = "mostRecentVersion";

    @Autowired
    private MsInstanceService msInstanceService;

    @Autowired
    private DeploymentArtifactGeneratorStrategy deploymentArtifactGeneratorStrategy;

    @Autowired
    private DeploymentArtifactGateway deploymentArtifactGateway;

    @Autowired
    private ArtifactFileNameCreator fileNameCreator;

    @Autowired
    private DeploymentArtifactStatusChangeHandler statusChangeHandler;

    ///////////////FIND METHODS//////////////////////////
    @Override
    public List<DeploymentArtifact> getAllDeploymentArtifacts() {
        return deploymentArtifactGateway.findAll();
    }

    @Override
    public List<DeploymentArtifact> searchDeploymentArtifacts(DeploymentArtifactSearch search) {
        return deploymentArtifactGateway.findAll(search);
    }

    @Override
    public DeploymentArtifact findDeploymentArtifactById(String id){
        return deploymentArtifactGateway.findById(id).orElseThrow(
                () -> new DeploymentArtifactNotFound("Deployment Artifact with id " + id + " not found")
        );
    }

    @Override
    public List<DeploymentArtifact> findByMsInstanceId(String msInstanceId) {
        return deploymentArtifactGateway.findByMsInstanceId(msInstanceId);
    }

    @Override
    @Transactional
    public void deleteDeploymentArtifact(String deploymentArtifactId) {
        DeploymentArtifact deploymentArtifact = findDeploymentArtifactById(deploymentArtifactId);
        log.info("deleting {}", deploymentArtifact.getFileName());
        deploymentArtifactGateway.deleteById(deploymentArtifactId);
        msInstanceService.removeDeploymentArtifactFromMsInstance(deploymentArtifact);
    }

    @Override
    @Transactional
    public void updateMsInstanceRef(MsInstance msInstance) {
        List<DeploymentArtifact> deploymentArtifacts = findByMsInstanceId(msInstance.getId());
        deploymentArtifacts.forEach((deploymentArtifact) -> {
            deploymentArtifact.getMsInstanceInfo().setName(msInstance.getName());
            deploymentArtifact.getMsInstanceInfo().setRelease(msInstance.getRelease());
            deploymentArtifactGateway.save(deploymentArtifact);
        });
    }

    //////////////////////////////////////////////////////

    @Override
    @Transactional
    //only status update was implemented
    public void updateDeploymentArtifact(String deploymentArtifactId, DeploymentArtifactPatchRequest deploymentArtifactPatchRequest,
                                         String user) {
        DeploymentArtifact deploymentArtifact = findDeploymentArtifactById(deploymentArtifactId);
        updateStatus(deploymentArtifactPatchRequest, deploymentArtifact);
        updateMetadata(user, deploymentArtifact);
        log.info("Updating the artifact in database..");
        deploymentArtifactGateway.save(deploymentArtifact);
        msInstanceService.updateStatusBasedOnDeploymentArtifactsStatuses(deploymentArtifact.getMsInstanceInfo().getId());
    }

    private void updateMetadata(String user, DeploymentArtifact deploymentArtifact) {
        deploymentArtifact.getMetadata().put("updatedBy", user);
        deploymentArtifact.getMetadata().put("updatedOn", new Date());
    }

    private void updateStatus(DeploymentArtifactPatchRequest deploymentArtifactPatchRequest, DeploymentArtifact deploymentArtifact) {
        DeploymentArtifactStatus changeToStatus = deploymentArtifactPatchRequest.getStatus();
        if(changeToStatus != null){
            log.info("Sent request to deployment artifact status change handler: {}", changeToStatus);
            statusChangeHandler.handleStatusChange(changeToStatus, deploymentArtifact);
        }
    }

    @Override
    @Transactional
    public DeploymentArtifact generateDeploymentArtifact(String msInstanceId, String user) {
        MsInstance msInstance = msInstanceService.getMsInstanceById(msInstanceId);

        //Generate the Blueprint for the active specification for the instance
       Map<String, Object> deploymentArtifact =  deploymentArtifactGeneratorStrategy.generateForRelease(msInstance.getActiveSpec(), msInstance.getRelease());

        DeploymentArtifact artifact = new DeploymentArtifact();
        artifact.setContent(String.valueOf(deploymentArtifact.get("content")));
        artifact.setVersion(updateLatestVersion(msInstance.getDeploymentArtifactsInfo()));
        artifact.setStatus(DeploymentArtifactStatus.IN_DEV);
        artifact.setMsInstanceInfo(createMsInstanceReferenceInfo(msInstance));
        artifact.setSpecificationInfo(createSpecificationReferenceInfo(msInstance.getActiveSpec()));
        artifact.setMetadata(createMetadata(user));

        artifact.setFileName(fileNameCreator.createFileName(msInstance, artifact.getVersion()));

        DeploymentArtifact savedDao = deploymentArtifactGateway.save(artifact);
        artifact.setId(savedDao.getId());

        msInstance.setDeploymentArtifactsInfo(updateMsDeploymentArtifactRef(msInstance.getDeploymentArtifactsInfo(), savedDao.getId()));
        msInstanceService.updateMsInstance(msInstance);

        return artifact;
    }

    private int updateLatestVersion(DeploymentArtifactsRef ref) {
        if(ref == null) return 1;
        else return  ref.getMostRecentVersion() + 1;
    }

    private DeploymentArtifactsRef updateMsDeploymentArtifactRef(DeploymentArtifactsRef ref, String deploymentArtifactId) {
        if(ref == null){
            ref = new DeploymentArtifactsRef();
            ref.setMostRecentVersion(1);
            List<String> deploymentArtifacts = new ArrayList<>();
            deploymentArtifacts.add(deploymentArtifactId);
            ref.setDeploymentArtifacts(deploymentArtifacts);
        }
        else{
            ref.setMostRecentVersion(ref.getMostRecentVersion() + 1);
            List<String> deploymentArtifactList = ref.getDeploymentArtifacts();
            deploymentArtifactList.add(deploymentArtifactId);
        }
        return ref;
    }

    private Map<String, Object> createMetadata(String user) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdOn", new Date());
        metadata.put("createdBy", user);
        return metadata;
    }

    private Map<String, Object> createSpecificationReferenceInfo(Specification activeSpec) {
        Map<String, Object> specInfo = new HashMap<>();
        specInfo.put("id", activeSpec.getId());
        return specInfo;
    }

    private MsInstanceInfo createMsInstanceReferenceInfo(MsInstance msInstance) {
        MsInstanceInfo msInstanceInfo = new MsInstanceInfo();
        msInstanceInfo.setId(msInstance.getId());
        msInstanceInfo.setName(msInstance.getName());
        msInstanceInfo.setRelease(msInstance.getRelease());
        return msInstanceInfo;
    }
}
