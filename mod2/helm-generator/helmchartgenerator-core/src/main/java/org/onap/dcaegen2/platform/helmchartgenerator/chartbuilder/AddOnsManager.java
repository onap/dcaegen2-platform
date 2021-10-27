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
import org.apache.commons.io.FileUtils;
import org.onap.dcaegen2.platform.helmchartgenerator.Utils;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.ComponentSpec;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.TlsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * manages addOn template files
 */
@Slf4j
@Component
public class AddOnsManager {

    public AddOnsManager(Utils utils) {
        this.utils = utils;
    }

    @Autowired
    private Utils utils;

    /**
     * include addons template files based on parameters in componentSpec file
     * @param specFileLocation spec file location
     * @param chart chart directory
     * @param chartTemplateLocation chart template location
     */
    public void includeAddons(String specFileLocation, File chart, String chartTemplateLocation) {
        if(externalTlsExists(specFileLocation)){
            includeCertificateYamlAddOn(chart, chartTemplateLocation);
        }
    }

    private void includeCertificateYamlAddOn(File chart, String chartTemplateLocation) {
        Path certificateYaml = Paths.get(chartTemplateLocation, "addons/templates/certificates.yaml");
        if(!Files.exists(certificateYaml)) {
            throw new RuntimeException("certificates.yaml not found under templates directory in addons");
        }
        try {
            File templates = new File(chart, "templates");
            FileUtils.copyFileToDirectory(certificateYaml.toFile(), templates);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("could not add certificates.yaml to templates directory");
        }
    }

    private boolean externalTlsExists(String specFileLocation) {
        ComponentSpec cs = utils.deserializeJsonFileToModel(specFileLocation, ComponentSpec.class);
        TlsInfo tlsInfo = cs.getAuxilary().getTlsInfo();
        return tlsInfo != null && tlsInfo.getUseExternalTls() != null && tlsInfo.getUseExternalTls().equals(true);
    }
}

