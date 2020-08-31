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

import org.onap.dcaegen2.platform.mod.model.specification.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.BlueprintFileNameCreateException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class ArtifactFileNameCreatorTest {

    private ArtifactFileNameCreator fileNameCreator;

    @BeforeEach
    void setUp() {
        fileNameCreator = new ArtifactFileNameCreator();
    }

    @Test
    void test_createCorrectBlueprintFileName() throws Exception{
        //arrange
        MsInstance msInstance = MsInstanceObjectMother.createMsInstance();
        String expectedName = createExpectedName(msInstance);

        //act
        String fileName = fileNameCreator.createFileName(msInstance, 1);

        //assert
        Assertions.assertThat(fileName).isEqualTo(expectedName);
    }

    @Test
    void test_missingTagForFileNameCreation_ShouldRaiseException() throws Exception{

        //arrange
        MsInstance msInstance = MsInstanceObjectMother.createMsInstance();
        msInstance.setMsInfo(new HashMap<>());

        Assertions.assertThatExceptionOfType(BlueprintFileNameCreateException.class).isThrownBy(
                () -> fileNameCreator.createFileName(msInstance, 1)
        );
    }

    @Test
    void test_missingSpecForFileNameCreation_ShouldRaiseException() throws Exception{

        //arrange
        MsInstance msInstance = MsInstanceObjectMother.createMsInstance();
        msInstance.setActiveSpec(null);

        Assertions.assertThatExceptionOfType(BlueprintFileNameCreateException.class).isThrownBy(
                () -> fileNameCreator.createFileName(msInstance, 1)
        );
    }


    private String createExpectedName(MsInstance msInstance) {
        String fileName = MsInstanceObjectMother.BASE_MS_TAG + "_"
                + DeploymentType.DOCKER.toString().toLowerCase() + "_"
                + msInstance.getRelease() + "_"
                + "1"
                + ".yaml";

        return fileName;
    }
}