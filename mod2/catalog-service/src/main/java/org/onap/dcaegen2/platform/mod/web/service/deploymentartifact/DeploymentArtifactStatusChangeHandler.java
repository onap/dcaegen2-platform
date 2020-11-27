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

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.StatusChangeNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A class responsible for handling status changes of Deployment Artifacts
 */
@Component
@Slf4j
public class DeploymentArtifactStatusChangeHandler {

    @Autowired
    DeploymentArtifactService deploymentArtifactService;

    /**
     * setter
     * @param deploymentArtifactService
     */
    public void setDeploymentArtifactService(DeploymentArtifactService deploymentArtifactService) {
        this.deploymentArtifactService = deploymentArtifactService;
    }

    /**
     * handles status changes
     * @param status
     * @param deploymentArtifact
     */
    public void handleStatusChange(DeploymentArtifactStatus status, DeploymentArtifact deploymentArtifact) {
        String msInstanceId = deploymentArtifact.getMsInstanceInfo().getId();
        List<DeploymentArtifact> artifacts = deploymentArtifactService.findByMsInstanceId(msInstanceId);
        if( status == DeploymentArtifactStatus.DEV_COMPLETE){
            for(DeploymentArtifact artifact : artifacts){
                if(artifact.getStatus() == DeploymentArtifactStatus.DEV_COMPLETE){
                    log.error("Status change is not allowed.");
                    throw new StatusChangeNotValidException(createValidationErrorMessage(deploymentArtifact));
                }
            }
        }
        deploymentArtifact.setStatus(status);
        log.info("Deployment Artifact's status changed successfully.");
    }

    private String createValidationErrorMessage(DeploymentArtifact artifact) {
        return String.format( "%s (v%d) for %s - Status change not allowed."
                + "  Only 1 blueprint can be in the DEV_COMPLETE state.  " +
               "Change the current DEV_COMPLETE blueprint to NOT_NEEDED or IN_DEV before changing another"
                + " to DEV_COMPLETE.", artifact.getMsInstanceInfo().getName(),
                artifact.getVersion(), artifact.getMsInstanceInfo().getRelease());
    }
}
