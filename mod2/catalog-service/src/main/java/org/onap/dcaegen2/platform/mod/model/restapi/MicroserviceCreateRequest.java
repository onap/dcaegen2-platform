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

import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsLocation;
import org.onap.dcaegen2.platform.mod.model.basemicroservice.BaseMsType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;

@NoArgsConstructor
@Data
public class MicroserviceCreateRequest {

    @NotBlank(message = "Microservice tag can not be blank")
    @Pattern(regexp = "^([a-z0-9](-[a-z0-9])*)+$", message = "Microservice tag name is Invalid. Accepts alphanumerics and hyphens.")
    @Size(min = 5, max = 50, message = "Tag name length cannot exceed 50 characters")
    private String tag;

    @NotBlank(message = "Microservice name can not be blank")
    private String name;

    @Pattern(regexp = "^[a-z-]*$", message = "Microservice core name is Invalid. Accepts lowercase letters and hyphens.")
    private String serviceName;

    private BaseMsType type;
    private BaseMsLocation location;
    private String namespace;
    private Map<String, Object> metadata;

    @NotBlank(message = "user can not be blank")
    private String user;

}
