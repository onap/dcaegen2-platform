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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.KeyValueMerger;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.Metadata;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class KeyValueMergerTest {

    private KeyValueMerger merger;

    @Mock
    private Yaml yamlHelper;

    private File chartDir;

    @BeforeEach
    void setUp() {
        merger = new KeyValueMerger(yamlHelper);
    }

    @Test
    void mergeValuesToChart() throws IOException {
        ChartInfo chartInfo = prepareChartInfo();
        chartDir = prepareChartDir();

        Mockito.when(yamlHelper.load(any(InputStream.class))).thenReturn(new HashMap<String, Object>());

        merger.mergeValuesToChart(chartInfo, chartDir);
        Mockito.verify(yamlHelper, Mockito.times(2)).dump(any(HashMap.class), any(PrintWriter.class));
    }

    @AfterEach
    void tearDown(){
        FileUtils.deleteQuietly(chartDir);
    }

    private File prepareChartDir() throws IOException {
        final Path chartDir = Files.createTempDirectory("chartDir");
        Files.createFile(chartDir.resolve("Chart.yaml"));
        Files.createFile(chartDir.resolve("values.yaml"));
        return chartDir.toFile();
    }

    private ChartInfo prepareChartInfo() {
        ChartInfo chartInfo = new ChartInfo();

        Metadata metadata = new Metadata();
        metadata.setName("someComponent");
        metadata.setVersion("someVersion");
        metadata.setDescription("someDescription");

        Map<String, Object> values = new HashMap<>();
        values.put("someKey", "someValue");

        chartInfo.setMetadata(metadata);
        chartInfo.setValues(values);

        return chartInfo;
    }
}