/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2021  Nokia. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package org.onap.dcaegen2.platform.mod.web.service.policymodel;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.mod.model.exceptions.ErrorMessages;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelConflictException;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.DistributionInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelDistributionEnv;
import org.onap.dcaegen2.platform.mod.model.restapi.MetadataRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelCreateRequest;
import org.onap.dcaegen2.platform.mod.model.restapi.PolicyModelUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * Policy Model Service implementation
 */

@Service
@Setter
@Slf4j
public class PolicyModelServiceImpl implements PolicyModelService {

    @Value("${policymodel.dev.server}")
    private String devServer;

    @Value("${policymodel.dev.port}")
    private String devServerPort;

    @Value("${policymodel.dev.user}")
    private String devServerUser;

    @Value("${policymodel.dev.password}")
    private String devServerPassword;

    @Autowired
    private PolicyModelGateway policyModelGateway;

    /**
     * Lists all Policy Models
     *
     * @return
     */
    @Override
    public List<PolicyModel> getAll() {
        return policyModelGateway.findAll();
    }

    /**
     * List a Policy Model by policyModel ID
     *
     * @param id
     * @return
     */
    @Override
    public PolicyModel getPolicyModelById(String id) {
        return policyModelGateway.findById(id).orElseThrow(() -> new PolicyModelNotFoundException(String.format("Policy Model with id %s not found", id)));
    }

    /**
     * creates a Policy Model
     *
     * @param request
     * @return
     */
    @Override
    @Transactional
    public PolicyModel createPolicyModel(PolicyModelCreateRequest request, String user) {
        checkIfPMNameAndVersionAlreadyExists(request.getName(), request.getVersion());
        PolicyModel policyModel = new PolicyModel();
        policyModel.setName(request.getName());
        policyModel.setContent(request.getContent());
        policyModel.setVersion(request.getVersion());
        policyModel.setOwner(request.getOwner());
        policyModel.setMetadata(getMetadataFields(request.getMetadata(), user));
        return policyModelGateway.save(policyModel);
    }

    /**
     * Update a Policy Model
     *
     * @param request
     * @param modelId
     * @return
     */
    @Override
    public PolicyModel updatePolicyModel(PolicyModelUpdateRequest request, String modelId, String user) {
        PolicyModel policyModel = getPolicyModelById(modelId);
        updateFields(request, policyModel,user);
        return policyModelGateway.save(policyModel);

    }


    /**
     * Cerifies if a Policy Model Exist
     *
     * @param pmName
     * @param pmVersion
     * @return
     */
    private void checkIfPMNameAndVersionAlreadyExists(String pmName, String pmVersion) {
        if (policyModelGateway.findByNameAndVersion(pmName, pmVersion).isPresent())
            throw new PolicyModelConflictException(ErrorMessages.POLICYMODEL_NAME_VERSION_CONFLICT_MESSAGE);
    }

    /**
     * creates a Policy Model Metadata
     *
     * @param metadata
     * @param user
     * @return
     */
    private MetadataRequest getMetadataFields(MetadataRequest metadata, String user) {
        MetadataRequest metadataFields = MetadataRequest.builder().build();
        metadataFields.setCreatedBy(user);
        metadataFields.setCreatedOn(new Date());
        if(metadata != null) {
            if (metadata.getNotes() != null)
                metadataFields.setNotes(metadata.getNotes());
            if (metadata.getLabels() != null)
                metadataFields.setLabels(metadata.getLabels());
        }
        return metadataFields;
    }


    /**
     * Updates a Policy Model
     *
     * @param request
     * @param policyModel
     * @param user
     * @return
     */
    private void updateFields(PolicyModelUpdateRequest request, PolicyModel policyModel, String user) {
        String name = request.getName();
        String version = request.getVersion();
        if (!(name.equalsIgnoreCase(policyModel.getName()) && version.equalsIgnoreCase(policyModel.getVersion()))) {
            checkIfPMNameAndVersionAlreadyExists(name, version);
            policyModel.setName(name);
            policyModel.setVersion(version);
        }
        if (request.getOwner() != null) {
            policyModel.setOwner(request.getOwner());
        }

        if (request.getContent() != null) {
            policyModel.setContent(request.getContent());
        }
        if (request.getMetadata() != null) {
            updateMetadata(request,user);
            policyModel.setMetadata(request.getMetadata());
        }
    }


    /**
     * Updates a Policy Model Metadata
     *
     * @param request
     * @param user
     * @return
     */
    private void updateMetadata(PolicyModelUpdateRequest request, String user) {
        MetadataRequest metadataRequest = request.getMetadata();
        metadataRequest.setUpdatedBy(user);
        metadataRequest.setUpdatedOn(new Date());
    }


}
