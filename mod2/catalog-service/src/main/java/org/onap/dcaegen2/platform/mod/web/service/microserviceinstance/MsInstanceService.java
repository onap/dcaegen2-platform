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
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceUpdateRequest;

import java.util.List;

/**
 * An interface to access MsInstance Services
 */
public interface MsInstanceService {

    List<MsInstance> getAll();

    MsInstance createMicroserviceInstance(String msName, MsInstanceRequest request);

    MsInstance getMsInstanceById(String id);

    void updateMsInstance(MsInstance msInstance);

    void updateStatusBasedOnDeploymentArtifactsStatuses(String msInstanceId);

    void removeDeploymentArtifactFromMsInstance(DeploymentArtifact deploymentArtifact);

    void updateMicroserviceReference(BaseMicroservice msToBeUpdated);

    MsInstance updateMsInstance(MsInstanceUpdateRequest updateRequest, String msInstanceId);
}
