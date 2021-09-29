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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ComponentSpecValidator;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ComponentSpecValidatorImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ComponentSpecValidatorTest {

    private static final String SPEC_SCHEMA = "src/test/input/specs/schemas/component-spec-schema.json";

    ComponentSpecValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComponentSpecValidatorImpl(new Utils());
    }

    @Test
    void validSpecShouldNotThrowAnyException() {
        String specSchema = "src/test/input/specs/schemas/component-spec-schema.json";
        String specFileLocation = "src/test/input/specs/ves.json";
        assertDoesNotThrow(() -> validator.validateSpecFile(specFileLocation, specSchema));
    }

    @Test
    void invalidSpecShouldThrowRuntimeException() {
        String specFileLocation = "src/test/input/specs/invalidSpecSchema.json";
        assertThrows(RuntimeException.class, () -> validator.validateSpecFile(specFileLocation, SPEC_SCHEMA));
    }

    @Test
    void ifNoHelmSectionFoundThrowRuntimeError() {
        String specFileLocation = "src/test/input/specs/invalidSpecNoHelm.json";
        assertThrows(RuntimeException.class, () -> validator.validateSpecFile(specFileLocation, SPEC_SCHEMA));
    }

    @Test
    void ifNoServiceSectionFoundThrowRuntimeError() {
        String specFileLocation = "src/test/input/specs/invalidSpecNoServices.json";
        assertThrows(RuntimeException.class, () -> validator.validateSpecFile(specFileLocation, SPEC_SCHEMA));
    }

}
