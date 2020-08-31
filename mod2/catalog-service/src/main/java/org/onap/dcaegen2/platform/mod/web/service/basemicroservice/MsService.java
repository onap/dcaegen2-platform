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

package org.onap.dcaegen2.platform.mod.web.service.basemicroservice;

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.MicroserviceUpdateRequest;

import java.util.List;

public interface MsService {
    BaseMicroservice createMicroservice(MicroserviceCreateRequest microserviceRequest);

    List<BaseMicroservice> getAllMicroservices();

    BaseMicroservice getMicroserviceById(String baseMsId);

    BaseMicroservice getMicroserviceByName(String msName);

    void updateMicroservice(String requestedMsId, MicroserviceUpdateRequest updateRequest);

    void saveMsInstanceReferenceToMs(BaseMicroservice microservice, MsInstance msInstance);

    void updateMsInstanceRef(MsInstance msInstance);
}
