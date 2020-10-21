/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
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

package com.att.blueprintgenerator.service.base;

import com.att.blueprintgenerator.model.base.Blueprint;
import com.att.blueprintgenerator.model.common.Input;
import com.att.blueprintgenerator.model.componentspec.base.ComponentSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAp and DCAE Blueprint Applications
 * Service: For Adding Quotes and Converting Blueprint to Yaml/String
 */

@Service
public abstract class BlueprintService {

    @Autowired
    protected ImportsService importsService;

    @Autowired
    protected FixesService fixesService;

    @Qualifier("objectMapper")
    @Autowired
    protected ObjectMapper objectMapper;

    @Qualifier("yamlObjectMapper")
    @Autowired
    protected ObjectMapper yamlObjectMapper;

    public void blueprintToYaml(ComponentSpec cs, Blueprint blueprint, Input input) {
        String bluePrintName = input.getBluePrintName();
        String outputPath = input.getOutputPath();
        String comment = input.getComment();

        try {
            File outputFile;
            if (bluePrintName.equals("")) {
                String name = cs.getSelf().getName();
                if (name.contains(".")) {
                    name = name.replaceAll(Pattern.quote("."), "_");
                }
                if (name.contains(" ")) {
                    name = name.replaceAll(" ", "");
                }
                String file = name + ".yaml";
                outputFile = new File(outputPath, file);
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            } else {
                if (bluePrintName.contains(" ") || bluePrintName.contains(".")) {
                    bluePrintName = bluePrintName.replaceAll(Pattern.quote("."), "_");
                    bluePrintName = bluePrintName.replaceAll(" ", "");
                }
                String file = bluePrintName + ".yaml";
                outputFile = new File(outputPath, file);
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            }

            String version = "#blueprint_version: " + cs.getSelf().getVersion() + '\n';
            String description = "#description: " + cs.getSelf().getDescription() + '\n';
            comment = "#" + comment + '\n';

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));
            writer.write(description);
            writer.write(version);


            if(input.getBpType().equals("dti") || input.getBpType().equals("m") || input.getBpType().equals("k"))
            writer.write(comment);

            writer.close();

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
            yamlObjectMapper.writeValue(out, blueprint);
            out.close();

            if(input.getBpType().equals("dti") || input.getBpType().equals("m") || input.getBpType().equals("k"))
                fixesService.fixDcaeSingleQuotes(outputFile);
            else
                fixesService.fixOnapSingleQuotes(outputFile);

            //new Yaml().load(new FileInputStream(outputFile));

            System.out.println("Blueprint is created with valid YAML Format");
        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate YAML file from Blueprint  or the generated YAML is not valid", ex);
        }
    }

    public Map<String, LinkedHashMap<String, Object>> addQuotes(Map<String, LinkedHashMap<String, Object>> inputs) {
        for (String s : inputs.keySet()) {
            if (inputs.get(s).get("type") != null) {
                if (inputs.get(s).get("type").equals("string")) {
                    if (inputs.get(s).get("default") != null && !s.contains("policies")) {
                        String temp = inputs.get(s).get("default").toString();
                        temp = "'" + temp + "'";
                        inputs.get(s).replace("default", temp);
                    }
                } else if (inputs.get(s).get("type").equals("map") || inputs.get(s).get("type").equals("list")) {
                    // Commented the Code as we need to read the object as is for Map and List. If the List object is to be converted to string uncomment the below code.
                    /* if(inputs.get(s).get("type").equals("list")) {
                        String temp = inputs.get(s).get("default").toString();
                        inputs.get(s).replace("default", temp);
                    }*/
                    inputs.get(s).remove("type");
                }
            }
        }
        return inputs;
    }

    public String blueprintToString(ComponentSpec componentSpec, Blueprint blueprint, Input input) {
        String ret = "";
        try {
            ret = yamlObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(blueprint);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if(input.getBpType().equals("dti") || input.getBpType().equals("m") || input.getBpType().equals("k")){
            ret = "#" + componentSpec.getSelf().getDescription() + "\n" + "#" + componentSpec.getSelf().getVersion() + "\n" + "#" + input.getComment() + "\n" + ret;
            ret = fixesService.fixStringQuotes(ret);
        }else
            ret = fixesService.applyFixes(ret);
        return ret;
    }


}
