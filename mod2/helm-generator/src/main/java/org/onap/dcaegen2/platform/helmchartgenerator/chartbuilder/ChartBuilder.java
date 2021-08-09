
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
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ChartTemplateStructureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * ChartBuilder is a main class responsible to generate a helm chart.
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class ChartBuilder {

   @Autowired
   private ComponentSpecParser specParser;

   @Autowired
   private ChartGenerator chartGenerator;

    /**
     * this method validates the inputs and generate a helm chart.
     * @param specFileLocation location of the application specification json file
     * @param chartTemplateLocation location of the base helm chart template
     * @param outputLocation location to store the helm chart
     * @param specSchemaLocation location of the specification json schema file to validate the application spec
     * @return generated helm chart tgz file
     * @throws Exception
     */
    public File build(String specFileLocation, String chartTemplateLocation, String outputLocation, String specSchemaLocation ) throws Exception {
        ChartTemplateStructureValidator.validateChartTemplateStructure(chartTemplateLocation);
        ChartInfo chartInfo = specParser.extractChartInfo(specFileLocation, chartTemplateLocation, specSchemaLocation);
        return chartGenerator.generate(chartTemplateLocation, chartInfo, outputLocation);
    }
}
