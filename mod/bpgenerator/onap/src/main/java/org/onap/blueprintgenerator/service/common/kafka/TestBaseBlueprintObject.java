/*
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2021 Nokia Intellectual Property. All rights reserved.
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

package org.onap.blueprintgenerator.service.common.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class TestBaseBlueprintObject implements BaseBlueprintObject {

    private String test = "test";

    private AafCredential aafCredential = new AafCredential("test", "test");

    @JsonIgnore
    @Override
    public LinkedHashMap<String, LinkedHashMap<String, Object>> getInputs() {
        return null;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getMappedObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("test", test);
        map.put("aaf_credential", aafCredential);

        return map;
    }
}
