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

package org.onap.dcaegen2.platform.mod.web.service.specification;

import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstanceStatus;
import org.onap.dcaegen2.platform.mod.model.restapi.SpecificationRequest;
import org.onap.dcaegen2.platform.mod.model.specification.Specification;
import org.onap.dcaegen2.platform.mod.model.specification.SpecificationStatus;
import org.onap.dcaegen2.platform.mod.web.service.microserviceinstance.MsInstanceService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Setter
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationGateway specificationGateway;

    @Autowired
    private MsInstanceService msInstanceService;

    @Autowired
    private SpecificationValidatorService specificationValidatorService;

    @Override
    public List<Specification> getAllSpecsByMsInstanceId(String id) {
        return specificationGateway.getSpecificationByMsInstanceId(id);
    }

    @Override
    @Transactional
    public Specification createSpecification(String msInstanceId, SpecificationRequest request) {
        MsInstance msInstance = msInstanceService.getMsInstanceById(msInstanceId);
        specificationValidatorService.validateSpecForRelease(request, msInstance.getRelease());
        Specification newSpec = createSpecification(request, msInstance);
        makePreviousSpecInactive(msInstance);
        Specification savedSpec = specificationGateway.save(newSpec);
        updateMsInstance(msInstance, savedSpec);
        return savedSpec;
    }

    private Specification createSpecification(SpecificationRequest request, MsInstance msInstance) {
        return Specification.builder()
                .status(SpecificationStatus.ACTIVE)
                .specContent(request.getSpecContent())
                .policyJson(request.getPolicyJson())
                .type(request.getType())
                .metadata(getMetadata(request))
                .msInstanceInfo(buildMsInstanceInfo(msInstance))
                .build();
    }

    private void updateMsInstance(MsInstance msInstance, Specification savedSpecification) {
        msInstance.setActiveSpec(savedSpecification);
        msInstance.setStatus(MsInstanceStatus.IN_DEV);
        msInstanceService.updateMsInstance(msInstance);
    }

    private void makePreviousSpecInactive(MsInstance msInstance) {
        if (msInstance.getActiveSpec() != null) {
            msInstance.getActiveSpec().setStatus(SpecificationStatus.INACTIVE);
            specificationGateway.save(msInstance.getActiveSpec());
        }
    }

    private Map<String, Object> getMetadata(SpecificationRequest request) {
        Map<String, Object> metadata = request.getMetadata();
        metadata.put("createdBy", request.getUser());
        metadata.put("createdOn", new Date());
        return metadata;
    }

    private Map<String, Object> buildMsInstanceInfo(MsInstance msInstance) {
        Map<String, Object> msInstanceInfo = new HashMap<>();
        msInstanceInfo.put("id", msInstance.getId());
        msInstanceInfo.put("name", msInstance.getName());
        msInstanceInfo.put("release", msInstance.getRelease());
        return msInstanceInfo;
    }

    @Override
    @Transactional
    public void updateMsInstanceRef(MsInstance msInstance) {
        List<Specification> specifications = getAllSpecsByMsInstanceId(msInstance.getId());
        specifications.forEach((specification) ->{
            specification.getMsInstanceInfo().put("name", msInstance.getName());
            specification.getMsInstanceInfo().put("release", msInstance.getRelease());
            specificationGateway.save(specification);
        });
    }


}
