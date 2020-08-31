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

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsLocation;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsStatus;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsType;
import org.onap.dcaegen2.platform.mod.model.common.AuditFields;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class BaseMsObjectMother {

    public static final String BASE_MS_NAME = "ms-1";
    public static final String BASE_MS_ID = "id123";
    public static final BaseMsType BASE_MS_TYPE = BaseMsType.TICK;
    public static final BaseMsLocation LOCATION = BaseMsLocation.CENTRAL;
    public static final String NAMESPACE = "sam.collector.namespace";
    public static final String NOTE = "Sample Note";
    public static final String LABEL_1 = "mylabel1";
    public static final String LABEL_2 = "mylabel2";
    public static final String USER = "abc123";
    private static final String BASE_MS_TAG = "sample-ms-tag" ;
    private static final String BASE_MS_SERVICE_NAME = "sample-core";


    public static MicroserviceCreateRequest createMockMsRequest() {
        Map<String, Object> metadata = new HashMap();
        metadata.put("notes", NOTE);
        metadata.put("labels", Arrays.asList(LABEL_1, LABEL_2));

        MicroserviceCreateRequest request = new MicroserviceCreateRequest();
        request.setName(BASE_MS_NAME);
        request.setTag(BASE_MS_TAG);
        request.setServiceName(BASE_MS_SERVICE_NAME);
        request.setType(BASE_MS_TYPE);
        request.setLocation(LOCATION);
        request.setNamespace(NAMESPACE);
        request.setMetadata(metadata);
        request.setUser(USER);

        return request;
    }

    public static BaseMicroservice createMockMsObject() {
        BaseMicroservice microservice = new BaseMicroservice();
        microservice.setId(BASE_MS_ID);
        microservice.setName(BASE_MS_NAME);
        microservice.setServiceName(BASE_MS_SERVICE_NAME);
        microservice.setTag(BASE_MS_TAG);
        microservice.setType(BASE_MS_TYPE);
        microservice.setLocation(LOCATION);
        microservice.setNamespace(NAMESPACE);
        microservice.setStatus(BaseMsStatus.ACTIVE);
        microservice.setMetadata(prepareAuditFields());
        microservice.setMsInstances(createMsInstanceReferences());
        return microservice;
    }

    private static List<Map<String, String>> createMsInstanceReferences() {
        List<Map<String, String>> msInstanceRefs = new ArrayList<>();
        Map<String, String> msInstance_1 = new HashMap<>();
        msInstance_1.put("name", BASE_MS_NAME);
        msInstance_1.put("id", "instance-1");
        Map<String, String> msInstance_2 = new HashMap<>();
        msInstance_2.put("name", BASE_MS_NAME);
        msInstance_2.put("id", "instance-2");
        msInstanceRefs.add(msInstance_1);
        msInstanceRefs.add(msInstance_2);
        return msInstanceRefs;
    }


    public static AuditFields prepareAuditFields() {
        return AuditFields.builder()
                .createdBy(USER)     // prepared by core
                .createdOn(new Date(12323132L))
                .updatedBy(USER)
                .updatedOn(new Date(12323133L))
                .notes(NOTE)
                .labels(Arrays.asList(LABEL_1, LABEL_2))
                .build();

    }

    public static MicroserviceUpdateRequest createUpdateMsRequest() {
        MicroserviceUpdateRequest updateRequest = new MicroserviceUpdateRequest();
        updateRequest.setName("updatedName");
        updateRequest.setLocation(BaseMsLocation.EDGE);
        updateRequest.setServiceName("updated-core-name");
        updateRequest.setNamespace("updatedNameSpace");
        updateRequest.setType(BaseMsType.ANALYTIC);
        updateRequest.setUser("updater");

        Map<String, Object> metadata = new HashMap();
        metadata.put("notes", "updatedNote");
        metadata.put("labels", Arrays.asList("updatedLabel1", "updatedLabel2"));
        updateRequest.setMetadata(metadata);
        return updateRequest;
    }

    public static String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
