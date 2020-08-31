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

package org.onap.dcaegen2.platform.mod.objectmothers;

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentType;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.DeploymentArtifactsRef;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstanceStatus;
import org.onap.dcaegen2.platform.mod.model.restapi.MsInstanceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MsInstanceObjectMother {

    public static final String MS_INSTANCE_NAME = "ms-instance-1";
    public static final String MS_INSTANCE_ID = "id-123";
    public static final String RELEASE = "2002";
    public static final String VERSION = "1.1";
    public static final String USER = "user-1";
    public static final String BASE_MS_TAG = "ms-instance-1-tag";
    public static final String SCRUMLEAD = "Sam";
    public static final String SYSTEMSENGINEER = "John";

    public static MsInstanceRequest getMsInstanceMockRequest() {
        Map<String, Object> metadataFromRequest = buildMockMetadataForRequest();

        MsInstanceRequest request = MsInstanceRequest.builder()
                .name(MS_INSTANCE_NAME)
                .release(RELEASE)
                .version(VERSION)
                .user(USER)
                .metadata(metadataFromRequest)
                .build();

        return request;
    }

    private static Map<String, Object> buildMockMetadataForRequest() {
        Map<String, Object> metadataFromRequest = new HashMap<>();
        metadataFromRequest.put("pstDueDate", "14-04-2020");
        metadataFromRequest.put("pstDueIteration", "1.2");
        metadataFromRequest.put("eteDueDate", "21-05-2020");
        metadataFromRequest.put("eteDueIteration", "1.3");
        metadataFromRequest.put("scrumLead", SCRUMLEAD);
        metadataFromRequest.put("systemsEngineer", SYSTEMSENGINEER);
        return metadataFromRequest;
    }


    public static MsInstance createMsInstance() {
        Map<String, Object> metadataFromResponse = buildMockMetadataForRequest().entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        metadataFromResponse.put("createdOn", "currentDate");
        metadataFromResponse.put("createdBy", USER);
        metadataFromResponse.put("scrumLead", SCRUMLEAD);
        metadataFromResponse.put("systemsEngineer", SYSTEMSENGINEER);

        Map<String, Object> msInfo = new HashMap<>();
        msInfo.put("id", BaseMsObjectMother.BASE_MS_ID);
        msInfo.put("name", BaseMsObjectMother.BASE_MS_NAME);
        msInfo.put("tag", BASE_MS_TAG);

        MsInstance msInstance = MsInstance.builder()
                .id(MS_INSTANCE_ID)
                .name(MS_INSTANCE_NAME)
                .release(RELEASE)
                .version(VERSION)
                .status(MsInstanceStatus.NEW)
                .metadata(metadataFromResponse)
                .msInfo(msInfo)
                .activeSpec(SpecificationObjectMother.getMockSpecification(DeploymentType.DOCKER))
                .build();

        return msInstance;
    }

    public static MsInstance getMsInstanceWithExistingDeploymentArtifactRef() {
        MsInstance msInstance = createMsInstance();

        DeploymentArtifactsRef deploymentArtifactRef = new DeploymentArtifactsRef();
        deploymentArtifactRef.setMostRecentVersion(1);

        ArrayList<String> deploymentArtifactList = new ArrayList<>();
        deploymentArtifactList.add("id-456");
        deploymentArtifactRef.setDeploymentArtifacts(deploymentArtifactList);

        msInstance.setDeploymentArtifactsInfo(deploymentArtifactRef);

        return msInstance;
    }
}
