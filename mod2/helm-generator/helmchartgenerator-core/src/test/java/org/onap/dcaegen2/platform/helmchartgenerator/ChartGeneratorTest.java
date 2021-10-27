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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.AddOnsManager;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartGenerator;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.HelmClient;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.KeyValueMerger;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ChartGeneratorTest {

    @Mock
    private KeyValueMerger kvMerger;

    @Mock
    private HelmClient helmClient;

    @Mock
    private Utils utils;

    @Mock
    private AddOnsManager addOnsManager;

    @Test
    void testChartGenerationSteps() throws Exception{
        ChartGenerator chartGenerator = new ChartGenerator(helmClient, kvMerger, utils, addOnsManager);
        Mockito.when(utils.cloneFileToTempLocation(any())).thenReturn(any());

        chartGenerator.generate("src/test/input/blueprint", new ChartInfo(), "src/test/output", "specFileLocation");

        Mockito.verify(kvMerger, Mockito.times(1)).mergeValuesToChart(any(), any());
        Mockito.verify(helmClient, Mockito.times(1)).lint(any());
        Mockito.verify(helmClient, Mockito.times(1)).packageChart(any(), any());
    }

}
