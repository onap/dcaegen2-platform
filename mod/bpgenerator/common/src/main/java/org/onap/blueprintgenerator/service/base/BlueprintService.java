/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Copyright (c) 2021 Nokia. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package org.onap.blueprintgenerator.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.componentspec.base.ComponentSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Date;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: DCAE/ONAP - Blueprint Generator Common Module: Used by both ONAp
 * and DCAE Blueprint Applications Service: For Adding Quotes and Converting Blueprint to
 * Yaml/String
 */
@Service
public class BlueprintService {

    private static final String TYPE_KEY = "type";
    private static final String DEFAULT_KEY = "default";

    @Autowired
    protected FixesService fixesService;

    @Qualifier("objectMapper")
    @Autowired
    protected ObjectMapper objectMapper;

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    private static final Logger logger = LoggerFactory.getLogger(BlueprintService.class);

    /**
     * Convertes blueprint to Yaml for given ComponentSpec, Blueprint and input
     *
     * @param cs ComponentSpec
     * @param blueprint Blueprint
     * @param input Input
     * @return
     */
    public void blueprintToYaml(ComponentSpec cs, Blueprint blueprint, Input input) {
        String bluePrintName = input.getBluePrintName();
        String outputPath = input.getOutputPath();
        String comment = "# " + input.getComment() + '\n';

        try {
            String name =
                StringUtils.isEmpty(bluePrintName) ? cs.getSelf().getName() : bluePrintName;
            if (name.contains(".")) {
                name = name.replaceAll(Pattern.quote("."), "_");
            }
            if (name.contains(" ")) {
                name = name.replace(" ", "");
            }

            File outputFile = createFile(outputPath, name);
            String appVersion = readAppVersion();

            String version = "#blueprint_version: " + cs.getSelf().getVersion() + '\n';
            String description = "#description: " + cs.getSelf().getDescription() + '\n';
            String date = "#blueprint_created_date: " + new Date() + '\n';

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));
            writer.write(description);
            writer.write(version);
            writer.write(date);
            writer.write(appVersion);

            if (isBpTypeMatches(input)) {
                writer.write(comment);
            }

            writer.close();

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
            yamlObjectMapper.writeValue(out, blueprint);
            out.close();

            if (isBpTypeMatches(input)) {
                fixesService.fixDcaeSingleQuotes(outputFile);
            } else {
                fixesService.fixOnapSingleQuotes(outputFile);
            }

            logger.debug("Blueprint is created with valid YAML Format");
        } catch (Exception ex) {
            throw new RuntimeException(
                "Unable to generate YAML file from Blueprint  or the generated YAML is not valid",
                ex);
        }
    }

    /**
     * Add quotes for given input
     *
     * @param inputs Inputs
     * @return
     */
    public Map<String, LinkedHashMap<String, Object>> addQuotes(
        Map<String, LinkedHashMap<String, Object>> inputs) {

        inputs.forEach(
            (key, value) -> {
                if (value.get(TYPE_KEY) != null) {
                    if (value.get(TYPE_KEY).equals("string")
                        && value.get(DEFAULT_KEY) != null
                        && !key.contains("policies")) {
                        value.replace(DEFAULT_KEY, "'" + value.get(DEFAULT_KEY).toString() + "'");
                    } else if (value.get(TYPE_KEY).equals("map") || value.get(TYPE_KEY)
                        .equals("list")) {
                        // Commented the Code as we need to read the object as is for Map and List. If the
                        // List object is to be converted to string uncomment the below code.
              /* if(inputs.get(s).get("type").equals("list")) {
              String temp = inputs.get(s).get("default").toString();
              inputs.get(s).replace("default", temp);
              }*/
                        inputs.get(key).remove(TYPE_KEY);
                    }
                }
            });

        return inputs;
    }

    /**
     * Converts Blueprint to String format
     *
     * @param componentSpec ComponentSpec
     * @param blueprint Blueprint
     * @param input Input
     * @return
     */
    public String blueprintToString(ComponentSpec componentSpec, Blueprint blueprint, Input input) {
        String ret = "";
        try {
            ret = yamlObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(blueprint);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (isBpTypeMatches(input)) {
            ret =
                "# "
                    + componentSpec.getSelf().getDescription()
                    + "\n"
                    + "# "
                    + componentSpec.getSelf().getVersion()
                    + "\n"
                    + "# "
                    + input.getComment()
                    + "\n"
                    + ret;
            ret = fixesService.fixStringQuotes(ret);
        } else {
            ret = fixesService.applyFixes(ret);
        }
        return ret;
    }

    private boolean isBpTypeMatches(Input input) {
        return input.getBpType().equals("dti")
            || input.getBpType().equals("m")
            || input.getBpType().equals("k");
    }

    private File createFile(String outputPath, String name) {
        File outputFile;
        String file = name + ".yaml";
        outputFile = new File(outputPath, file);
        outputFile.getParentFile().mkdirs();
        try {
            boolean isCreated = outputFile.createNewFile();
            if (isCreated) {
                logger.debug("The file " + file + " was successfully created.");
            } else {
                logger.debug("The file " + file + " already existed.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputFile;
    }

    private String readAppVersion() {
        String appVersion = "";
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader("pom.xml"));
            appVersion = "#bpgen_application_version: " + model.getVersion() + '\n';
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersion;
    }
}
