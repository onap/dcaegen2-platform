/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
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

package org.onap.blueprintgenerator.test;

import org.onap.blueprintgenerator.exception.ComponentSpecException;
import org.onap.blueprintgenerator.model.componentspec.base.ComponentSpec;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator ONAP ComponentSpec Test Cases
 */
public class OnapComponentSpecTest extends BlueprintGeneratorTests {

    /**
     * Test Case for ComponentSpec File Generation for Invalid File
     *
     */
    @DisplayName("Testing ComponentSpec File Generation for Invalid File")
    @Test(expected = ComponentSpecException.class)
    public void testComponentSpecForInvalidFile() {
        onapComponentSpecService.createComponentSpecFromFile("invalid.json");
    }

    /**
     * Test Case for ComponentSpec File Generation for Valid DMAAP Fil
     *
     */
    @DisplayName("Testing ComponentSpec File Generation for Valid DMAAP File")
    @Test
    public void testComponentSpecForValidVesFile() {
        ComponentSpec onapComponentSpec =
            onapComponentSpecService.createComponentSpecFromFile(
                Paths.get("src", "test", "resources", "componentspecs", ves)
                    .toFile()
                    .getAbsolutePath());
        assertEquals(
            "ComponentSpec name not matching for Valid Ves File",
            onapComponentSpec.getSelf().getName(),
            "dcae-ves-collector");
    }
}
