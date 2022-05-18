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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CompSpec {

    static final Logger LOG = LoggerFactory.getLogger(App.class);

    public String name;
    // Name of component to be transformed to be more Java style
    public String nameJavaClass;
    public String version;
    public String description;

    // https://stackoverflow.com/questions/37010891/how-to-map-a-nested-value-to-a-property-using-jackson-annotations
    @JsonProperty("self")
    public void unpackSelf(Map<String, String> self) {
        this.name = self.get("name");
        this.nameJavaClass = Utils.formatNameForJavaClass(self.get("name"));
        this.version = self.get("version");
        this.description = self.get("description");
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameter {
        @JsonProperty("name")
        public String name;
        @JsonProperty("value")
        public String value;
        @JsonProperty("description")
        public String description;
        @JsonProperty("sourced_at_deployment")
        public boolean sourcedAtDeployment;
        @JsonProperty("policy_editable")
        public boolean policyEditable;
        @JsonProperty("designer_editable")
        public boolean designerEditable;

        public String toString() {
            String[] params = new String[] {
                String.format("name: \"%s\"", this.name)
                , String.format("value: \"%s\"", this.value)
                , String.format("description: \"%s\"", this.description)
            };
            return String.join(", ", params);
        }
    }

    @JsonProperty("parameters")
    public List<Parameter> parameters;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Connection {
        @JsonProperty("format")
        public String format;
        @JsonProperty("version")
        public String version;
        @JsonProperty("type")
        public String type;
        @JsonProperty("config_key")
        public String configKey;
    }

    @JsonProperty("streams")
    public Map<String, List<Connection>> streams;

    public List<Connection> getPublishes() {
        return streams.containsKey("publishes") ? streams.get("publishes") : null;
    }

    public List<Connection> getSubscribes() {
        return streams.containsKey("subscribes") ? streams.get("subscribes") : null;
    }

    public String toString(String delimiter) {
        List<String> items = new ArrayList();
        items.add(String.format("name: %s", name));
        items.add(String.format("version: %s", version));
        items.add(String.format("description: %s", description));
        items.add(String.format("parameters: %d", parameters.size()));

        if (!parameters.isEmpty()) {
            // Cap at MAX
            int MAX=parameters.size() > 3 ? 3 : parameters.size();
            for (int i=0; i<MAX; i++) {
                items.add(String.format("\t%s", parameters.get(i).toString()));
            }
        }

        items.add("\t..");

        return String.join(delimiter, items.toArray(new String[items.size()]));
    }

    public static CompSpec loadComponentSpec(File compSpecFile) {
        return loadComponentSpec(compSpecFile.toURI());
    }

    public static CompSpec loadComponentSpec(URI compSpecURI) {
        JsonFactory jf = new JsonFactory();
        ObjectMapper om = new ObjectMapper();

        try {
            return om.readValue(jf.createParser(compSpecURI.toURL()), CompSpec.class);
        } catch (Exception e) {
            LOG.error("Uhoh", e);
        }

        throw new RuntimeException("REPLACE ME");
    }

}

