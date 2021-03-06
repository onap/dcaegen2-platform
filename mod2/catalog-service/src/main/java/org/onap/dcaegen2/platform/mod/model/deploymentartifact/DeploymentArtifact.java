/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.platform.mod.model.deploymentartifact;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.onap.dcaegen2.platform.mod.model.policymodel.DistributionInfo;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * A model class which represents Deployment-Artifact entity
 */
@Data
@Document("deployment-artifact")
public class DeploymentArtifact {

    private String id;

    private Integer version;

    private String content;

    private String fileName;

    private DeploymentArtifactStatus status;

    private Map<String, Object> metadata;

    private MsInstanceInfo msInstanceInfo;

    private Map<String, Object> specificationInfo;

    private DistributionInfo distributionInfo;

}
