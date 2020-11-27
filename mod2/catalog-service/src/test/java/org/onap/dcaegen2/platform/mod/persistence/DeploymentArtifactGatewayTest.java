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

package org.onap.dcaegen2.platform.mod.persistence;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactFilter;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactSearch;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.mongo.deploymentartifact.DeploymentArtifactMongoGateway;
import org.onap.dcaegen2.platform.mod.mongo.deploymentartifact.DeploymentArtifactMongoRepo;
import org.onap.dcaegen2.platform.mod.objectmothers.DeploymentArtifactObjectMother;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

@Disabled("Embedded mongodb jar is not available in the maven repo.")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class DeploymentArtifactGatewayTest {

    DeploymentArtifactGateway gateway;

    @Autowired
    DeploymentArtifactMongoRepo repo;

    @Autowired
    MongoOperations operations;

    @BeforeEach
    public void setUp(){
        gateway = new DeploymentArtifactMongoGateway(repo);

        operations.dropCollection(DeploymentArtifact.class);

        String r_2008 = "2008";
        String r_2010 = "2010";

        DeploymentArtifactStatus inDev = DeploymentArtifactStatus.IN_DEV;
        DeploymentArtifactStatus devComplete = DeploymentArtifactStatus.DEV_COMPLETE;

        String tag_1 = "hello-one";
        String tag_2 = "hello-two";
        String tag_3 = "hello-three";

        DeploymentArtifact artifact_1 = getDeploymentArtifact(r_2008, inDev, tag_1);
        DeploymentArtifact artifact_2 = getDeploymentArtifact(r_2010, devComplete, tag_2);
        DeploymentArtifact artifact_3 = getDeploymentArtifact(r_2008, devComplete, tag_3);

        operations.insertAll(Arrays.asList(artifact_1, artifact_2, artifact_3));
        operations.findAll(DeploymentArtifact.class).forEach(System.out::println);

        System.out.println();
    }

    private static DeploymentArtifact getDeploymentArtifact(String r_2008, DeploymentArtifactStatus inDev,
                                                            String tag) {
        DeploymentArtifact artifact_1 = DeploymentArtifactObjectMother.createDeploymentArtifactDAO(inDev);
        artifact_1.getMsInstanceInfo().setRelease(r_2008);
        //Currently searching tag in filename as it is not present in DeploymentArtifact record
        artifact_1.setFileName(tag);
        artifact_1.setId(null);

        return artifact_1;
    }

    @Test
    public void findByOnlyRelease() throws Exception {
        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
        filter.setRelease("2008");
        search.setFilter(filter);

        List<DeploymentArtifact> artifacts = gateway.findAll(search);
        Assertions.assertThat(artifacts.size()).isEqualTo(2);
    }

    @Test
    public void findWithOnlyStatus() throws Exception {
        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
        filter.setStatus(DeploymentArtifactStatus.IN_DEV);
        search.setFilter(filter);

        List<DeploymentArtifact> artifacts = gateway.findAll(search);

        Assertions.assertThat(artifacts.size()).isEqualTo(1);
    }

    @Test
    public void findWithStatusAndRelease() throws Exception {
        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
        filter.setStatus(DeploymentArtifactStatus.DEV_COMPLETE);
        filter.setRelease("2008");
        search.setFilter(filter);

        List<DeploymentArtifact> artifacts = gateway.findAll(search);

        Assertions.assertThat(artifacts.size()).isEqualTo(3);
    }

    @Test
    public void findWithTag() throws Exception {
        DeploymentArtifactSearch search = new DeploymentArtifactSearch();
        DeploymentArtifactFilter filter = new DeploymentArtifactFilter();
        filter.setTag("hello-one");
        search.setFilter(filter);

        List<DeploymentArtifact> artifacts = gateway.findAll(search);

        Assertions.assertThat(artifacts.size()).isEqualTo(1);
    }

    @Test
    public void findWithNoQuery() throws Exception {
        List<DeploymentArtifact> artifacts = gateway.findAll(new DeploymentArtifactSearch());
        Assertions.assertThat(artifacts.size()).isEqualTo(0);
    }
}
