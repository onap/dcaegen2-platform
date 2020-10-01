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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.Test;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

public class AppconfigTest {

    @Test
    public void createAppconfigShouldReturnExpectedResult() throws FileNotFoundException {
        TreeMap<String, LinkedHashMap<String, Object>> inputs = new TreeMap<String, LinkedHashMap<String, Object>>();
        ComponentSpec cs = new ComponentSpec();
        cs.createComponentSpecFromFile("TestCases/testComponentSpec.json");

        TreeMap<String, LinkedHashMap<String, Object>> result = new Appconfig().createAppconfig(inputs, cs, "", false);

        assertEquals(getExpectedStringFromFile(), result.toString());
    }

    private String getExpectedStringFromFile() throws FileNotFoundException {
        File file = new File("TestCases/expects/createAppConfigResult.txt");
        InputStream inputStream = new FileInputStream(file);
        return readFromInputStream(inputStream);
    }

    private String readFromInputStream(InputStream inputStream) {
        return new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining(""));
    }
}
