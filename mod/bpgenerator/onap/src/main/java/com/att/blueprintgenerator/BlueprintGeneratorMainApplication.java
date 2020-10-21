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

package com.att.blueprintgenerator;


import com.att.blueprintgenerator.model.base.Blueprint;
import com.att.blueprintgenerator.model.common.Input;
import com.att.blueprintgenerator.model.componentspec.OnapComponentSpec;
import com.att.blueprintgenerator.model.componentspec.base.ComponentSpec;
import com.att.blueprintgenerator.service.OnapBlueprintService;
import com.att.blueprintgenerator.service.common.CommonUtils;
import com.att.blueprintgenerator.service.common.ComponentSpecService;
import com.att.blueprintgenerator.service.dmaap.DmaapBlueprintService;
import com.att.policycreate.service.PolicyModelService;
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

@ComponentScan({"com.att.blueprintgenerator","com.att.policycreate"})
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
        if (args[0].equals("app") && args[1].equals("ONAP")) {
            if(args[2].equals("-type") && args[3].equals("policycreate")){
                ComponentSpec componentSpec = componentSpecService.createComponentSpecFromFile("sam.json");
                onapPolicyModelNodeService.createPolicyModels(componentSpec.getParameters(), "models");
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
