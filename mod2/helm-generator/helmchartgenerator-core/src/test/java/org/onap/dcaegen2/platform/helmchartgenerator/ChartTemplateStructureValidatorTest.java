/*
 * # ============LICENSE_START=======================================================
 * # Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
 * # ================================================================================
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * # ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.helmchartgenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ChartTemplateStructureValidatorImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChartTemplateStructureValidatorTest {

    private ChartTemplateStructureValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new ChartTemplateStructureValidatorImpl();
    }

    @Test
    void validateTemplateStructure(){
        String validStructureLocation = "src/test/input/blueprint";
        assertDoesNotThrow(() -> validator.validateChartTemplateStructure(validStructureLocation));
    }

    @Test
    void invalidateTemplateStructureShouldThrowRuntimeError() {
        String invalidStructureLocation = "test";
        assertThrows(RuntimeException.class, () -> validator.validateChartTemplateStructure(invalidStructureLocation));
    }
}
