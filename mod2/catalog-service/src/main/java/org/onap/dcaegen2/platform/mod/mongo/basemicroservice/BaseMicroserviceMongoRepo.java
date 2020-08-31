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
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * An interface to use Spring MongoRepository
 */
@Repository
public interface BaseMicroserviceMongoRepo extends MongoRepository<BaseMicroservice, String> {

    Optional<BaseMicroservice> findByNameIgnoreCase(String name);

    Optional<BaseMicroservice> findByTagIgnoreCase(String tag);

    Optional<BaseMicroservice> findByServiceNameIgnoreCase(String serviceName);
}
