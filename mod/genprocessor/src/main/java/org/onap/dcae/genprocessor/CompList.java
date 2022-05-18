/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.genprocessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompList {

    static final Logger LOG = LoggerFactory.getLogger(CompList.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompShort {
        @JsonProperty("id")
        public String id;
        @JsonProperty("name")
        public String name;
        @JsonProperty("version")
        public String version;
        @JsonProperty("description")
        public String description;
        @JsonProperty("componentType")
        public String componentType;
        @JsonProperty("owner")
        public String owner;
        @JsonProperty("componentUrl")
        public String componentUrl;
        @JsonProperty("whenAdded")
        public String whenAdded;

        public String getNameForJavaClass() {
            return Utils.formatNameForJavaClass(this.name);
        }

        public URI getComponentUrlAsURI() {
            try {
                return new URI(this.componentUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Component URL is bad");
            }
        }
    }

    @JsonProperty("components")
    public List<CompShort> components;

}

