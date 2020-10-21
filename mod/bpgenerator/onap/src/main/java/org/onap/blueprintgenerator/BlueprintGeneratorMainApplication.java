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

package org.onap.blueprintgenerator;


import org.onap.blueprintgenerator.model.base.Blueprint;
import org.onap.blueprintgenerator.model.common.Input;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.base.ComponentSpec;
import org.onap.blueprintgenerator.service.OnapBlueprintService;
import org.onap.blueprintgenerator.service.common.CommonUtils;
import org.onap.blueprintgenerator.service.common.ComponentSpecService;
import org.onap.blueprintgenerator.service.dmaap.DmaapBlueprintService;
import org.onap.policycreate.service.PolicyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static java.lang.System.exit;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * ONAP - Blueprint Generator Main Application to create Policy Models or Blueprints
 */

@ComponentScan({"org.onap.blueprintgenerator","org.onap.policycreate"})
@SpringBootApplication
public class BlueprintGeneratorMainApplication implements CommandLineRunner {

    @Autowired
    private ComponentSpecService componentSpecService;

    @Autowired
    private PolicyModelService policyModelService;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private PolicyModelService onapPolicyModelNodeService;

    @Autowired
    private ComponentSpecService onapComponentSpecService;

    @Autowired
    private CommonUtils onapCommonUtils;

    @Autowired
    private OnapBlueprintService onapBlueprintService;

    @Autowired
    private DmaapBlueprintService dmaapBlueprintService;

    public static void main(String[] args) {
        SpringApplication.run(BlueprintGeneratorMainApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length >=2 && args[0].equals("app") && args[1].equals("ONAP")) {
            if(args.length >=4 && args[2].equals("-type") && args[3].equals("policycreate")){
                Input input = onapCommonUtils.parseInputs(args);
                ComponentSpec componentSpec = componentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
                onapPolicyModelNodeService.createPolicyModels(componentSpec.getParameters(), input.getOutputPath());
            }
            else {
                onapCommonUtils.printInstructions();
                Input input = onapCommonUtils.parseInputs(args);
                OnapComponentSpec onapComponentSpec = onapComponentSpecService.createComponentSpecFromFile(input.getComponentSpecPath());
                if (input.getBpType().equals("o")) {
                    Blueprint blueprint = onapBlueprintService.createBlueprint(onapComponentSpec, input);
                    onapBlueprintService.blueprintToYaml(onapComponentSpec, blueprint, input);
                    System.out.println(onapBlueprintService.blueprintToString(onapComponentSpec, blueprint, input));
                } else if (input.getBpType().equals("d")) {
                    Blueprint blueprint = dmaapBlueprintService.createBlueprint(onapComponentSpec, input);
                    dmaapBlueprintService.blueprintToYaml(onapComponentSpec, blueprint, input);
                    System.out.println(dmaapBlueprintService.blueprintToString(onapComponentSpec, blueprint, input));
                }
            }
        }

        exit(0);
    }

}
