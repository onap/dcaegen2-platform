 /** # ============LICENSE_START=======================================================
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

 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartBuilder;
 import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartGenerator;
 import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ComponentSpecParser;
 import org.onap.dcaegen2.platform.helmchartgenerator.validation.ChartTemplateStructureValidator;

 import static org.mockito.ArgumentMatchers.any;

 @ExtendWith(MockitoExtension.class)
class ChartBuilderTest {

    @Mock
    private ChartGenerator chartGenerator;

    @Mock
    private ComponentSpecParser specParser;

    @Mock
    private ChartTemplateStructureValidator validator;

    @Test
    void testChartBuilderSteps() throws Exception{
        ChartBuilder builder = new ChartBuilder(specParser, chartGenerator, validator);
        builder.build("someSpec", "someChartLocation", "someOutputLocation", "someSpecSchemaLocation");

        Mockito.verify(specParser, Mockito.times(1)).extractChartInfo(any(), any(), any());
        Mockito.verify(chartGenerator, Mockito.times(1)).generate(any(), any(), any());
        Mockito.verify(validator, Mockito.times(1)).validateChartTemplateStructure(any());
    }
}
