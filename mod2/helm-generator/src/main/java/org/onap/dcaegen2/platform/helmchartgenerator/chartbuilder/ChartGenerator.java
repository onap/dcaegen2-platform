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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.onap.dcaegen2.platform.helmchartgenerator.Utils.cloneFileToTempLocation;
import static org.onap.dcaegen2.platform.helmchartgenerator.Utils.deleteTempFileLocation;

/**
 * ChartGenerator interacts with HelmClient and generates a packaged helm chart
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class ChartGenerator {

    @Setter
    @Autowired
    HelmClient helmClient;

    @Setter
    @Autowired
    KeyValueMerger merger;

    /**
     * Merges the key-values from the helm base template and parsed spec file and generates a new packaged helm chart
     * @param chartBlueprintLocation location of the base helm chart template
     * @param chartInfo chartInfo object with key-values parsed from the specfile.
     * @param outputLocation location to store the helm chart
     * @return generated helm chart tgz file
     */
    public File generate(String chartBlueprintLocation, ChartInfo chartInfo, String outputLocation) {
        File newChartDir = cloneFileToTempLocation(chartBlueprintLocation + "/base");
        merger.mergeValuesToChart(chartInfo, newChartDir);
        helmClient.lint(newChartDir);
        final File chartLocation = helmClient.packageChart(newChartDir, outputLocation);
        deleteTempFileLocation(newChartDir);
        return chartLocation;
    }
}
