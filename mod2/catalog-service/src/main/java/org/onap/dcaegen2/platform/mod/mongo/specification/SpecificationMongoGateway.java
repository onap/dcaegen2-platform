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

package org.onap.dcaegen2.platform.mod.mongo.specification;

import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.web.service.specification.SpecificationGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mongo implementation of Specification
 */
@Service
public class SpecificationMongoGateway implements SpecificationGateway {

    @Autowired
    private SpecificationMongoRepo repo;

    @Override
    public List<Specification> getSpecificationByMsInstanceId(String id) {
        return repo.getSpecificationsByMsInstaceId(id);
    }

    @Override
    public Specification save(Specification newSpec) {
        return repo.save(newSpec);
    }
}
