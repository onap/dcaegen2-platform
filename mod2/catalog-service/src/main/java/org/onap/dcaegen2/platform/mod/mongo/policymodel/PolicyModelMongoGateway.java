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

package org.onap.dcaegen2.platform.mod.mongo.policymodel;

import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Mongo implementation of Policy Model
 */

@Service
public class PolicyModelMongoGateway implements PolicyModelGateway {

    @Autowired
    private PolicyModelMongoRepo repo;

    @Override
    public List<PolicyModel> findAll() {
        Sort sortByCreatedDate = Sort.by(Sort.Direction.DESC, "metadata.createdOn");
        return repo.findAll(sortByCreatedDate);
    }

    @Override
    public Optional<PolicyModel> findById(String id) {
       return repo.findById(id);
    }

    @Override
    public PolicyModel save(PolicyModel newPolicyModel) { return repo.save(newPolicyModel); }

    @Override
    public Optional<PolicyModel> findByNameAndVersion(String name, String version) { return repo.findByNameIgnoreCaseAndVersionIgnoreCase(name,version); }


}
