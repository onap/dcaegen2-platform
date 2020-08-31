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

package org.onap.dcaegen2.platform.mod.mongo.basemicroservice;

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMicroservice;
import org.onap.dcaegen2.platform.mod.web.service.basemicroservice.BaseMicroserviceGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of BaseMicroserviceGateway
 */
@Service
public class BaseMicroserviceMongoGateway implements BaseMicroserviceGateway {

    @Autowired
    BaseMicroserviceMongoRepo repo;

    @Override
    public Optional<BaseMicroservice> findByName(String name) {
        return repo.findByNameIgnoreCase(name);
    }

    @Override
    public Optional<BaseMicroservice> findByTag(String tag) {
        return repo.findByTagIgnoreCase(tag);
    }

    @Override
    public Optional<BaseMicroservice> findByServiceName(String serviceName) {
        return repo.findByServiceNameIgnoreCase(serviceName);
    }

    @Override
    public BaseMicroservice save(BaseMicroservice microservice) {
        return repo.save(microservice);
    }

    @Override
    public List<BaseMicroservice> findAll() {
        Sort sortByCreatedDate = Sort.by(Sort.Direction.DESC, "metadata.createdOn");
        return repo.findAll(sortByCreatedDate);
    }

    @Override
    public Optional<BaseMicroservice> findById(String msId) {
        return Optional.empty();
    }
}
