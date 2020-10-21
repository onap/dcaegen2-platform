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

package com.att.blueprintgenerator.service.common;

import com.att.blueprintgenerator.model.common.Input;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static java.lang.System.exit;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to Print Instructions and Parse Inputs
 */


@Service("onapCommonUtilsService")
public class CommonUtils {

    public void printInstructions() {
        System.out.println("OPTIONS:");
        System.out.println("-i OR --component-spec: The path of the ONAP Blueprint INPUT JSON SPEC FILE (Required)");
        System.out.println("-p OR --blueprint-path: The path of the ONAP Blueprint OUTPUT where it will be saved (Required)");
        System.out.println("-n OR --blueprint-name: The NAME of the ONAP Blueprint OUTPUT that will be created (Optional)");
        System.out.println("-t OR --imports: The path of the ONAP Blueprint IMPORT FILE (Optional)");
        System.out.println("-o OR --service-name-override: The Value used to OVERRIDE the SERVICE NAME of the ONAP Blueprint  (Optional)");
        System.out.println("-d OR --dmaap-plugin: The option to create a ONAP Blueprint with DMAAP Plugin (Optional)");
    }

    public Input parseInputs(String[] args) {
        String[] modArgs = new String[args.length];
        for(int i=0; i<args.length; i++){
            if(args[i].contains("--component-spec"))
                modArgs[i] = "-_component_spec";
            else if(args[i].contains("--blueprint-path"))
                modArgs[i] = "-_blueprint_path";
            else if(args[i].contains("--blueprint-name"))
                modArgs[i] = "-_blueprint_name";
            else if(args[i].contains("--imports"))
                modArgs[i] = "-_imports";
            else if(args[i].contains("--service-name-override"))
                modArgs[i] = "-_service_name_override";
            else if(args[i].contains("--dmaap-plugin"))
                modArgs[i] = "-_dmaap_plugin";
            else
                modArgs[i] = args[i];
        }
        String commands = "";
        for (String s : modArgs) {
            if (commands.length() == 0)
                commands = s;
            else
                commands = commands + " " + s;
        }

        //checks if the required inputs are present or not
        if (!(commands.contains("-i ") || commands.contains(" -i") || commands.contains(" -_component_spec") || commands.contains("-_component_spec "))
                && !(commands.contains("-p ") || commands.contains(" -p") || commands.contains("-_blueprint_path ") || commands.contains(" -_blueprint_path") ) ) {
            System.out.println("\n Please enter the ONAP Blueprint required inputs for: \n         -i (The path of the ONAP Blueprint INPUT JSON SPEC FILE), \n         -p (The path of the ONAP Blueprint OUTPUT where it will be saved)");
            exit(-1);
        }


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        Options options = new Options();

        options.addOption("i", "Spec", true, "ComponentSpec Input File of the ONAP Blueprint");
        options.addOption("_component_spec", "Spec", true, "ComponentSpec Input File of the ONAP Blueprint");
        options.addOption("p", "Path", true, "Path of the ONAP Blueprint OUTPUT");
        options.addOption("_blueprint_path", "Path", true, "Path of the ONAP Blueprint OUTPUT");
        options.addOption("n", "name", true, "NAME of the ONAP Blueprint OUTPUT");
        options.addOption("_blueprint_name", "name", true, "NAME of the ONAP Blueprint OUTPUT");
        options.addOption("t", "Import File", true, "Import file for the OUTPUT Blueprint Imports");
        options.addOption("_imports", "Import File", true, "Import file for the OUTPUT Blueprint Imports");
        options.addOption("o", "Service name Override", true, "Value used to override the OUTPUT Blueprint service name");
        options.addOption("_service_name_override", "Service name Override", true, "Value used to override the OUTPUT Blueprint service name");
        options.addOption("d", "Dmaap Plugin", false, "Flag used to indicate ONAP Blueprint OUTPUT uses the DMaaP plugin");
        options.addOption("_dmaap_plugin", "Dmaap Plugin", false, "Flag used to indicate ONAP Blueprint OUTPUT uses the DMaaP plugin");

        Input input = new Input();
        try {
            CommandLine commandLine = parser.parse(options, modArgs);
            input.setComponentSpecPath(commandLine.getOptionValue("i") == null ? commandLine.getOptionValue("_component_spec") : commandLine.getOptionValue("i"));
            input.setOutputPath(commandLine.getOptionValue("p") == null ? commandLine.getOptionValue("_blueprint_path") : commandLine.getOptionValue("p"));
            input.setBluePrintName(commandLine.getOptionValue("n") == null ? commandLine.getOptionValue("_blueprint_name") : commandLine.getOptionValue("n"));
            input.setImportPath(commandLine.getOptionValue("t")  == null ? commandLine.getOptionValue("_imports") : commandLine.getOptionValue("t"));
            input.setBpType((commands.contains("-d ") || commands.contains(" -d"))|| (commands.contains("-_dmaap_plugin ") || commands.contains(" -_dmaap_plugin")) ? "d" : "o");
            input.setServiceNameOverride(commandLine.getOptionValue("o") == null ? commandLine.getOptionValue("_service_name_override") ==  null ? "" : commandLine.getOptionValue("_service_name_override") : commandLine.getOptionValue("o"));
        } catch (ParseException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            formatter.printHelp("Required/Valid Inputs to create ONAP Blueprint are not provided", options);
            exit(-1);
        }
        if (StringUtils.isEmpty(input.getComponentSpecPath())) {
            System.out.println("The path of the ONAP Blueprint INPUT JSON SPEC FILE  is not specified");
            exit(-1);
        }
        if (StringUtils.isEmpty(input.getOutputPath())) {
            System.out.println("The path of the ONAP Blueprint OUTPUT where it will be saved is not specified");
            exit(-1);
        }
        if (commands.contains("-n ") || commands.contains(" -n") |commands.contains("-_blueprint_name ") || commands.contains(" -_blueprint_name ")) {
            if (StringUtils.isEmpty(input.getBluePrintName())) {
                System.out.println("The NAME of the ONAP Blueprint OUTPUT that will be created is not specified");
                exit(-1);
            }
        }
        if (commands.contains("-t ") || commands.contains(" -t") || commands.contains("-_imports ") || commands.contains(" -_imports")) {
            if (StringUtils.isEmpty(input.getImportPath())) {
                System.out.println("The path of the ONAP Blueprint Imports File is not specified");
                exit(-1);
            }
        }

        return input;
    }

}
