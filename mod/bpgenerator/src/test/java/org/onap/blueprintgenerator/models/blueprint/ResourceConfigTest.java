/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
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

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.junit.Test;

public class ResourceConfigTest {

    @Test
    public void createResourceConfig() {
        TreeMap<String, LinkedHashMap<String, Object>> result = new ResourceConfig()
            .createResourceConfig(new TreeMap<>(), "demo");

        String expectedResult = "{demo_cpu_limit={type=string, default=250m}, demo_cpu_request={type=string, default=250m}, demo_memory_limit={type=string, default=128Mi}, demo_memory_request={type=string, default=128Mi}}";
        assertEquals(expectedResult, result.toString());
    }
}
