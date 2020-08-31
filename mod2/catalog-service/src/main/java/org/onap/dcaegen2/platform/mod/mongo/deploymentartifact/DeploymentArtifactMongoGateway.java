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

package org.onap.dcaegen2.platform.mod.mongo.deploymentartifact;

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactSearch;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.MsInstanceInfo;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of BaseMicroserviceGateway
 */
@Service
public class DeploymentArtifactMongoGateway implements DeploymentArtifactGateway {

    @Autowired
    DeploymentArtifactMongoRepo crudRepo;

    public DeploymentArtifactMongoGateway(DeploymentArtifactMongoRepo repo) {
        this.crudRepo = repo;
    }

    @Override
    public List<DeploymentArtifact> findAll() {
        return crudRepo.findAll();
    }

    @Override
    public List<DeploymentArtifact> findByMsInstanceId(String id) {
        return crudRepo.findByMsInstanceInfo_Id(id);
    }

    @Override
    public Optional<DeploymentArtifact> findById(String id) {
        return crudRepo.findById(id);
    }

    @Override
    public void deleteById(String deploymentArtifactId) {
        crudRepo.deleteById(deploymentArtifactId);
    }

    @Override
    public DeploymentArtifact save(DeploymentArtifact deploymentArtifact) {
        return crudRepo.save(deploymentArtifact);
    }

    @Override
    public List<DeploymentArtifact> findAll(DeploymentArtifactSearch search) {
        DeploymentArtifact artifact = new DeploymentArtifact();
        artifact.setStatus(search.getFilter().getStatus());
        //Currently searching tag in filename as it is not present in DeploymentArtifact record
        artifact.setFileName(search.getFilter().getTag());

        MsInstanceInfo msInstanceInfo = new MsInstanceInfo();
        msInstanceInfo.setRelease(search.getFilter().getRelease());
        artifact.setMsInstanceInfo(msInstanceInfo);

        return crudRepo.findAll(Example.of(artifact,ExampleMatcher.matching().withIgnoreCase()));
    }
}
