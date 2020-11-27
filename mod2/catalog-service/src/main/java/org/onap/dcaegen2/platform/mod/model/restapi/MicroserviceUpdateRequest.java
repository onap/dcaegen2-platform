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

package org.onap.dcaegen2.platform.mod.model.restapi;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsLocation;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Map;

/**
 *  A model that represent request body to update a Microservice entity.
 */
@NoArgsConstructor
@Data
public class MicroserviceUpdateRequest {

    @Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    private String name;

    @Pattern(regexp = "^[a-z-]*$", message = "Microservice core name is Invalid. Accepts lowercase letters and hyphens.")
    private String serviceName;

    private BaseMsType type;
    private BaseMsLocation location;
    private String namespace;
    private Map<String, Object> metadata;

    @NotBlank(message = "user cannot be blank")
    private String user;
}
