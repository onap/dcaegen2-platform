/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ImportsTest {

    private final List<String> expectedImports = Arrays.asList(
        "https://www.getcloudify.org/spec/cloudify/4.5.5/types.yaml",
        "plugin:k8splugin?version=3.4.1",
        "plugin:pgaas?version=1.3.0",
        "plugin:clamppolicyplugin?version=1.1.0",
        "plugin:dmaap?version=1.5.0"
    );

    @Test
    public void shouldReadImportsFromFile() {
        ArrayList<String> importsFromFile = Imports.createImportsFromFile("TestCases/imports/imports.yaml");
        assertEquals(expectedImports, importsFromFile);
    }

    @Test
    public void shouldRemoveBlankImportsFromFile() {
        ArrayList<String> importsFromFile =
            Imports.createImportsFromFile("TestCases/imports/importsWithBlanks.yaml");
        assertEquals(expectedImports, importsFromFile);
    }

}
