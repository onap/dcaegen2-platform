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

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstanceStatus;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A class responsible for handling status changes of Ms Instances
 */
@Component
@Slf4j
public class MsInstanceStatusChangeHandler {

    @Autowired
    private MsInstanceService msInstanceService;

    @Autowired
    private DeploymentArtifactService deploymentArtifactService;

    public void setMsInstanceService(MsInstanceService msInstanceService) {
        this.msInstanceService = msInstanceService;
    }

    public void setDeploymentArtifactService(DeploymentArtifactService deploymentArtifactService) {
        this.deploymentArtifactService = deploymentArtifactService;
    }

    public void updateStatusBasedOnDeploymentArtifactsStatuses(MsInstance msInstance) {
        log.info("Checking if any Status change required for msInstance {}...", msInstance);
        List<DeploymentArtifact> artifacts = deploymentArtifactService.findByMsInstanceId(msInstance.getId());
        MsInstanceStatus newStatus = getValidStatusBasedOnArtifacts(artifacts);
        msInstance.setStatus(newStatus);
        log.info("Changed Status to {}", newStatus);
    }

    private MsInstanceStatus getValidStatusBasedOnArtifacts(List<DeploymentArtifact> artifacts) {
        if(atLeastOneArtifactHasDevCompleteStatus(artifacts)){
            return MsInstanceStatus.DEV_COMPLETE;
        }
        return MsInstanceStatus.IN_DEV;
    }

    private boolean atLeastOneArtifactHasDevCompleteStatus(List<DeploymentArtifact> artifacts) {
         return artifacts
                 .stream()
                 .anyMatch(artifact -> artifact.getStatus() == DeploymentArtifactStatus.DEV_COMPLETE);
    }
}
