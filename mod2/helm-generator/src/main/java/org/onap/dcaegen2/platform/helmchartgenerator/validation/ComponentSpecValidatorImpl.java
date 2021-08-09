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

import org.apache.commons.io.FileUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.onap.dcaegen2.platform.helmchartgenerator.Utils;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.ComponentSpec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * A class for Component specification validation.
 * @author Dhrumin Desai
 */
@Service
public class ComponentSpecValidatorImpl implements ComponentSpecValidator {

    /**
     * Validates the spec json file  against schema and prints errors if found
     * @param specFileLocation specification json file location
     * @param specSchemaLocation json schema file location
     * @throws IOException
     */
    @Override
    public void validateSpecFile(String specFileLocation, String specSchemaLocation) throws IOException {
        File schemaFile = getSchemaFile(specSchemaLocation);
        ComponentSpec cs = Utils.deserializeJsonFileToModel(specFileLocation, ComponentSpec.class);
        validateSpecSchema(new File(specFileLocation), schemaFile);
        validateHelmRequirements(cs);
    }

    private File getSchemaFile(String specSchemaLocation) throws IOException {
        if(specSchemaLocation.isEmpty())
            return defaultSchemaFile();
        else
            return new File(specSchemaLocation);
    }

    private File defaultSchemaFile() throws IOException {
        File schemaFile = File.createTempFile("schema", ".json");
        InputStream inputStream = new ClassPathResource("specSchema.json").getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, schemaFile);
        return schemaFile;
    }

    private void validateSpecSchema(File specFile, File schemaFile) {
        try {
            JSONTokener schemaData = new JSONTokener(new FileInputStream(schemaFile));
            JSONObject jsonSchema = new JSONObject(schemaData);

            JSONTokener jsonDataFile = new JSONTokener(new FileInputStream(specFile));
            JSONObject jsonObject = new JSONObject(jsonDataFile);

            Schema schemaValidator = SchemaLoader.load(jsonSchema);
            schemaValidator.validate(jsonObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void validateHelmRequirements(ComponentSpec cs) {
        checkHealthCheckSection(cs);
        checkHelmSection(cs);
    }

    private void checkHealthCheckSection(ComponentSpec cs) {
        if(cs.getAuxilary().getHealthcheck().getPort() == null) {
            throw new RuntimeException("port in healthcheck section is a required field but was not found");
        }
    }

    private void checkHelmSection(ComponentSpec cs) {
        if(cs.getAuxilary().getHelm() == null){
            throw new RuntimeException("helm section in component spec is required but was not found");
        }
    }
}
