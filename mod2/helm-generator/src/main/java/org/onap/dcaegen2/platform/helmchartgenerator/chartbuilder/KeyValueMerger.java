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

package org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder;

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Map;

/**
 * KeyValueMerger merges helm base templates key-values with key-values parsed from component specification file
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class KeyValueMerger {

    @Autowired
    private Yaml yaml;

    public KeyValueMerger(Yaml yaml) {
        this.yaml = yaml;
    }

    /**
     * merges helm base templates key-values with key-values parsed from component specification file
     * @param chartInfo populated ChartInfo object which holds key-values parsed from component spec file
     * @param chartDir location of the base helm chart template
     */
    public void mergeValuesToChart(ChartInfo chartInfo, File chartDir) {
        mergeChartYamlFile(chartInfo, chartDir);
        mergeValuesYamlFile(chartInfo, chartDir);
    }

    private void mergeChartYamlFile(ChartInfo chartInfo, File chartDir) {
        String chartYamlFilePath = Paths.get(chartDir.getAbsolutePath(), "Chart.yaml").toString();
        checkIfFIleExists(chartYamlFilePath, "Chart.yaml is not found in the given chart template.");

        Map<String, Object> chartYamlKV;
        try {
            chartYamlKV = yaml.load(new FileInputStream(chartYamlFilePath));
            chartYamlKV.put("name", chartInfo.getMetadata().getName());
            chartYamlKV.put("version", chartInfo.getMetadata().getVersion());
            chartYamlKV.put("description", chartInfo.getMetadata().getDescription());
            yaml.dump(chartYamlKV, new PrintWriter(chartYamlFilePath));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    private void mergeValuesYamlFile(ChartInfo chartInfo, File chartDir) {
        String valuesYamlFilePath = Paths.get(chartDir.getAbsolutePath(), "values.yaml").toString();
        checkIfFIleExists(valuesYamlFilePath, "values.yaml is not found in the given chart template.");
        Map<String, Object> valuesYamlKv;
        try {
            valuesYamlKv = yaml.load(new FileInputStream(valuesYamlFilePath));
            valuesYamlKv.putAll(chartInfo.getValues());
            yaml.dump(valuesYamlKv, new PrintWriter(valuesYamlFilePath));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    private void checkIfFIleExists(String filePath, String message) {
        File valuesYaml = new File(filePath);
        if (!valuesYaml.exists()) {
            throw new RuntimeException(message);
        }
    }
}
