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

package org.onap.dcaegen2.platform.mod.mongo.microserviceinstance;

import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of MsInstance
 */
@Service
public class MsInstanceMongoGateway implements MsInstanceGateway {

    @Autowired
    private MsInstanceMongoRepo repo;

    @Override
    public Optional<MsInstance> findByNameAndRelease(String name, String release) {
        return repo.findByNameIgnoreCaseAndReleaseIgnoreCase(name, release);
    }

    @Override
    public Optional<MsInstance> findById(String msInstanceId) {
        return repo.findById(msInstanceId);
    }

    @Override
    public List<MsInstance> findAll() {
        Sort sortByCreatedDate = Sort.by(Sort.Direction.DESC, "metadata.createdOn");
        return repo.findAll(sortByCreatedDate);
    }

    @Override
    public MsInstance save(MsInstance msInstance) {
        return repo.save(msInstance);
    }
}
