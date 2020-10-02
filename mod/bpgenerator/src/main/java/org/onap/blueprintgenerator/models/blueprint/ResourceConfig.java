/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 Copyright (c) 2020 Nokia. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ============LICENSE_END=========================================================

 */

package org.onap.blueprintgenerator.models.blueprint;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createStringInput;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO: Auto-generated Javadoc
/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Getter
@Setter

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Builder

/**
 * Instantiates a new resource config obj.
 */
@NoArgsConstructor

/**
 * Instantiates a new resource config obj.
 *
 * @param limits the limits
 * @param requests the requests
 */
@AllArgsConstructor

public class ResourceConfig {

    private TreeMap<String, GetInput> limits;
    private TreeMap<String, GetInput> requests;


    /**
     * Creates the resource config.
     *
     * @param inputs the inputs
     * @param name the name
     * @return the tree map
     */
    public TreeMap<String, LinkedHashMap<String, Object>> createResourceConfig(
        TreeMap<String, LinkedHashMap<String, Object>> inputs, String name) {

        LinkedHashMap<String, Object> memoryLimit = createStringInput("128Mi");
        LinkedHashMap<String, Object> cpuLimit = createStringInput("250m");

        String namePrefix = getNamePrefix(name);

        this.setLimits(createInputs(inputs, namePrefix, "limit", cpuLimit, memoryLimit));
        this.setRequests(createInputs(inputs, namePrefix, "request", cpuLimit, memoryLimit));

        return inputs;
    }

    private TreeMap<String, GetInput> createInputs(TreeMap<String, LinkedHashMap<String, Object>> inputs,
        String namePrefix,
        String inputType,
        LinkedHashMap<String, Object> cpuLimit,
        LinkedHashMap<String, Object> memoryLimit) {

        final String cpuKey = namePrefix + "cpu_" + inputType;
        final String memoryKey = namePrefix + "memory_" + inputType;
        TreeMap<String, GetInput> inps = new TreeMap<>();

        insertInput("cpu", cpuKey, inps);
        insertInput("memory", memoryKey, inps);

        inputs.put(cpuKey, cpuLimit);
        inputs.put(memoryKey, memoryLimit);

        return inps;
    }

    private void insertInput(String type, String name, TreeMap<String, GetInput> inputs) {
        GetInput input = new GetInput();
        input.setBpInputName(name);
        inputs.put(type, input);
    }

    private String getNamePrefix(String name) {
        return name == null || name.isEmpty() ? "" : name + "_";
    }
}

