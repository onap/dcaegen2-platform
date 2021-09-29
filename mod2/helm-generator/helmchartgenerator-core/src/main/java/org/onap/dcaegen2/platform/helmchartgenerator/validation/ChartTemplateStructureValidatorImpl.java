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

package org.onap.dcaegen2.platform.helmchartgenerator.validation;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class to validate structure of the base helm directory
 */
@Component
public class ChartTemplateStructureValidatorImpl implements ChartTemplateStructureValidator {

    /**
     * validates base helm chart directory and throws error if the structure is not proper.
     * @param chartTemplateLocation base helm chart dir location
     */
    @Override
    public void validateChartTemplateStructure(String chartTemplateLocation) {
        checkBaseDirectory(chartTemplateLocation);
    }

    private void checkBaseDirectory(String chartTemplateLocation) {
        Path base = Paths.get(chartTemplateLocation, "base");
        Path charts = Paths.get(chartTemplateLocation, "base/charts");
        Path templates = Paths.get(chartTemplateLocation, "base/templates");
        Path chart = Paths.get(chartTemplateLocation, "base/Chart.yaml");
        Path values = Paths.get(chartTemplateLocation, "base/values.yaml");
        if(!Files.exists(base)){
            throw new RuntimeException("base directory not found in chart template location");
        }
        if(!Files.exists(charts)){
            throw new RuntimeException("charts directory not found in base directory");
        }
        if(!Files.exists(templates)){
            throw new RuntimeException("templates directory not found in base directory");
        }
        if(!Files.exists(chart)){
            throw new RuntimeException("chart.yaml not found in base directory");
        }
        if(!Files.exists(values)){
            throw new RuntimeException("values.yaml not found in base directory");
        }
    }
}
