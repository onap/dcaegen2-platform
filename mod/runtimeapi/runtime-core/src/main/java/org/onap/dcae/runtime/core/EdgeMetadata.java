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
package org.onap.dcae.runtime.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdgeMetadata {

    private String name;
    private String dataType;
    private String dmaapType;

    public EdgeMetadata(@JsonProperty("name") String name,@JsonProperty("data_type") String dataType,
                        @JsonProperty("dmaap_type") String dmaapType) {
        this.name = name;
        this.dataType = dataType;
        this.dmaapType = dmaapType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDmaapType() {
        return dmaapType;
    }

    public void setDmaapType(String dmaapType) {
        this.dmaapType = dmaapType;
    }
}
