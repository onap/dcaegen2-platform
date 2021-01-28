/*
 *
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

package org.onap.blueprintgenerator.service.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Parameters;
import org.onap.blueprintgenerator.service.InfoService;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.onap.blueprintgenerator.service.common.kafka.KafkaStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfigService.class, BlueprintHelperService.class, DmaapService.class,
    InfoService.class, StreamService.class, KafkaStreamService.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class AppConfigServiceTest {

    public static final String TEST_PARAMETER_NAME = "testParameter";
    public static final String INPUTS = "inputs";
    public static final String TYPE = "type";
    public static final String DEFAULT = "default";

    public static final String PARAMETERS_TYPE_STRING = "string";
    public static final String PARAMETERS_TYPE_INTEGER = "integer";
    public static final String PARAMETERS_TYPE_BOOLEAN = "boolean";
    public static final String PARAMETERS_TYPE_NUMBER = "number";

    public static final String STRING_INPUT_TYPE = "string";
    public static final String INTEGER_INPUT_TYPE = "integer";
    public static final String BOOLEAN_INPUT_TYPE = "boolean";
    public static final String UNKNOWN_TYPE = "test_unknown_type";

    public static final boolean BOOLEAN_TEST_VALUE = true;
    public static final String TEST_STRING_VALUE = "testValue";

    @Autowired
    private AppConfigService appConfigService;

    private OnapComponentSpec componentSpec;

    @Before
    public void setUp() {
        componentSpec = Mockito.mock(OnapComponentSpec.class);
    }

    @Test
    public void shouldCreateStringInputForStringParameter() {

        mockParameters(PARAMETERS_TYPE_STRING, TEST_STRING_VALUE);
        Map<String, LinkedHashMap<String, Object>> inputs = new HashMap<>();
        
        Map<String, Object> appConfig = appConfigService.createAppconfig(inputs, componentSpec, false);
        Map<String, Object> createdInputs = (Map<String, Object>) appConfig.get(INPUTS);
        Map<String, Object> createdParameters = (Map<String, Object>) createdInputs.get(TEST_PARAMETER_NAME);

        assertEquals(STRING_INPUT_TYPE, createdParameters.get(TYPE));
        assertEquals(TEST_STRING_VALUE, createdParameters.get(DEFAULT));
    }

    @Test
    public void shouldCreateStringInputForUnknownParameter() {

        mockParameters(UNKNOWN_TYPE, TEST_STRING_VALUE);
        Map<String, LinkedHashMap<String, Object>> inputs = new HashMap<>();
        
        Map<String, Object> appConfig = appConfigService.createAppconfig(inputs, componentSpec, false);
        Map<String, Object> createdInputs = (Map<String, Object>) appConfig.get(INPUTS);
        Map<String, Object> createdParameters = (Map<String, Object>) createdInputs.get(TEST_PARAMETER_NAME);

        assertEquals(STRING_INPUT_TYPE, createdParameters.get(TYPE));
        assertEquals(TEST_STRING_VALUE, createdParameters.get(DEFAULT));
    }

    @Test
    public void shouldCreateBooleanInputForBooleanParameter() {

        mockParameters(PARAMETERS_TYPE_BOOLEAN, BOOLEAN_TEST_VALUE);
        Map<String, LinkedHashMap<String, Object>> inputs = new HashMap<>();
        
        Map<String, Object> appConfig = appConfigService.createAppconfig(inputs, componentSpec, false);
        Map<String, Object> createdInputs = (Map<String, Object>) appConfig.get(INPUTS);
        Map<String, Object> createdParameters = (Map<String, Object>) createdInputs.get(TEST_PARAMETER_NAME);

        assertEquals(BOOLEAN_INPUT_TYPE, createdParameters.get(TYPE));
        assertEquals(BOOLEAN_TEST_VALUE, createdParameters.get(DEFAULT));
    }

    @Test
    public void shouldCreateIntegerInputForIntegerParameter() {

        mockParameters(PARAMETERS_TYPE_INTEGER, 123);
        Map<String, LinkedHashMap<String, Object>> inputs = new HashMap<>();
        
        Map<String, Object> appConfig = appConfigService.createAppconfig(inputs, componentSpec, false);
        Map<String, Object> createdInputs = (Map<String, Object>) appConfig.get(INPUTS);
        Map<String, Object> createdParameters = (Map<String, Object>) createdInputs.get(TEST_PARAMETER_NAME);

        assertEquals(INTEGER_INPUT_TYPE, createdParameters.get(TYPE));
        assertEquals(123, createdParameters.get(DEFAULT));
    }

    @Test
    public void shouldCreateIntegerInputForNumberParameter() {

        mockParameters(PARAMETERS_TYPE_NUMBER, 123);
        Map<String, LinkedHashMap<String, Object>> inputs = new HashMap<>();
        
        Map<String, Object> appConfig = appConfigService.createAppconfig(inputs, componentSpec, false);
        Map<String, Object> createdInputs = (Map<String, Object>) appConfig.get(INPUTS);
        Map<String, Object> createdParameters = (Map<String, Object>) createdInputs.get(TEST_PARAMETER_NAME);

        assertEquals(INTEGER_INPUT_TYPE, createdParameters.get(TYPE));
        assertEquals(123, createdParameters.get(DEFAULT));
    }

    private void mockParameters(String type, Object value) {
        Parameters testParameter = new Parameters();
        testParameter.setName(TEST_PARAMETER_NAME);
        testParameter.setType(type);
        testParameter.setSourced_at_deployment(true);
        testParameter.setValue(value);
        Parameters[] parametersArray = new Parameters[1];
        parametersArray[0] = testParameter;
        when(componentSpec.getParameters()).thenReturn(parametersArray);
    }
}
