/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcae.runtime.core.helm;

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartBuilder;
import org.onap.dcaegen2.platform.helmchartgenerator.distribution.ChartDistributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Implementation class for Helm Chart Generator Client
 */
@Slf4j
@Component
@ComponentScan(basePackages = "org.onap.dcaegen2.platform.helmchartgenerator")
public class HelmChartGeneratorClientImpl implements HelmChartGeneratorClient {

    @Autowired
    private final ChartBuilder chartBuilder;

    @Autowired
    private final ChartDistributor distributor;

    @Value("${helm.base.chart.template.location}")
    private String templateLocation;

    public HelmChartGeneratorClientImpl(ChartBuilder chartBuilder, ChartDistributor distributor) {
        this.chartBuilder = chartBuilder;
        this.distributor = distributor;
    }

    @Override
    public File generateHelmChart(String componentSpec){
        try {
            return chartBuilder.build(createTempSpecFile(componentSpec),templateLocation,
                    createTempChartOutputLocation(), "");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error while generating the helm chart.");
        }
    }

    private String createTempChartOutputLocation() {
        try {
            return Files.createTempDirectory("chart").toAbsolutePath().toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error creating a temporary chart dir.");
        }
    }

    private String createTempSpecFile(String componentSpec) throws IOException{
            File tmpFile = File.createTempFile("spec",".json");
        try (FileWriter writer = new FileWriter(tmpFile)) {
            writer.write(componentSpec);
        }
        return tmpFile.getAbsolutePath();
    }

    @Override
    public void distribute(File helmChart) {
        distributor.distribute(helmChart);
    }
}
