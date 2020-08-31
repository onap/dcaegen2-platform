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

package org.onap.dcaegen2.platform.mod.model.basemicroservice;

import org.onap.dcaegen2.platform.mod.model.common.AuditFields;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A model class which represents Base-Microservice entity
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
//TODO: migrate the document to microservices
@Document("base-microservices")
public class BaseMicroservice {
    private String id;

    @ApiModelProperty(required = true)
    private String name;

    @ApiModelProperty(required = true)
    private String tag;

    private String serviceName;

    private BaseMsType type;

    private BaseMsLocation location;

    private String namespace;

    private BaseMsStatus status;

    private AuditFields metadata;

    private List<Map<String, String>> msInstances = new ArrayList<>();
}

