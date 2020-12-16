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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *  A model that represent request body to create Policy Model entity.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyModelCreateRequest {

   @NotBlank
   @Pattern(regexp = "^([a-zA-Z0-9]([.|-][a-zA-Z0-9])*)+$", message = "Policy Model name is invalid. Accepts alphanumerics, Dot(.) and hyphens.")
   @Size(min = 5, max = 50, message = "Policy Model name length cannot exceed 50 characters")
   private String name;

   @NotBlank
   private String content;

   @NotBlank
   private String owner;

   @NotBlank
   @Pattern(regexp = "[0-9][.][0-9][.][0-9]", message = "Policy Model version is invalid. Should be in x.x.x format.")
   private String version;

   private MetadataRequest metadata;

}
