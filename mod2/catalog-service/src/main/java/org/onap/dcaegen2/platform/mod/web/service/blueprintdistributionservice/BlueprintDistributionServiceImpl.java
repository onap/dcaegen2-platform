/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.mod.web.service.blueprintdistributionservice;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.policymodel.DistributionInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelStatus;
import org.onap.dcaegen2.platform.mod.model.restapi.ErrorResponse;
import org.onap.dcaegen2.platform.mod.model.restapi.SuccessResponse;
import org.onap.dcaegen2.platform.mod.util.BlueprintDistributionUtils;
import org.onap.dcaegen2.platform.mod.util.SSLUtils;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactGateway;
import org.onap.dcaegen2.platform.mod.web.service.deploymentartifact.DeploymentArtifactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Setter
@Slf4j
public class BlueprintDistributionServiceImpl implements BlueprintDistributionService{

    @Autowired
    private DeploymentArtifactService deploymentArtifactService;

    @Autowired
    private BlueprintDistributionUtils blueprintDistributionUtils;

    @Autowired
    private DeploymentArtifactGateway deploymentArtifactGateway;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity distributeBlueprint(String deploymentArtifactId, String env){
        DeploymentArtifact deploymentArtifact = deploymentArtifactService.findDeploymentArtifactById(deploymentArtifactId);
        return postBlueprintToDcae(deploymentArtifact,env);
    }

    private ResponseEntity postBlueprintToDcae(DeploymentArtifact deploymentArtifact, String env){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(blueprintDistributionUtils.getBlueprintDashboardUserName(env),blueprintDistributionUtils.getBlueprintDashboardPassword(env));

        DistributionInfo distributionInfo = DistributionInfo.builder().url(blueprintDistributionUtils.getBlueprintDashboardURL(env)).build();
        deploymentArtifact.setDistributionInfo(distributionInfo);
        JSONObject blueprintJsonObject = prepareBlueprintJsonObject(deploymentArtifact);
        HttpEntity<String> request = new HttpEntity<>(blueprintJsonObject.toString(), headers);

        try{
            SSLUtils.turnOffSslChecking();
            String response = restTemplate.postForObject(blueprintDistributionUtils.getBlueprintDashboardURL(env),request,String.class);
            log.info(response);
            log.info(String.format("Distributed Blueprint to DCAE: %s", blueprintJsonObject.toString()));
            distributionInfo.setStatus(PolicyModelStatus.SUCCESS);
            deploymentArtifactGateway.save(deploymentArtifact);
            blueprintJsonObject.put("distributionInfoStatus", PolicyModelStatus.SUCCESS.name());
            return new ResponseEntity<>(new SuccessResponse(blueprintJsonObject.toString()), HttpStatus.OK);
        }catch (Exception e) {
            log.error("Failed to push Blueprint to DCAE");
            log.error(e.getMessage());
            distributionInfo.setStatus(PolicyModelStatus.FAILED);
            deploymentArtifactGateway.save(deploymentArtifact);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private JSONObject prepareBlueprintJsonObject(DeploymentArtifact deploymentArtifact) {
        JSONObject blueprintJsonObject = new JSONObject();
        blueprintJsonObject.put("owner","dcae_mod");
        blueprintJsonObject.put("typeName",deploymentArtifact.getFileName());
        blueprintJsonObject.put("typeVersion",deploymentArtifact.getVersion());
        blueprintJsonObject.put("blueprintTemplate",deploymentArtifact.getContent());
        blueprintJsonObject.put("application","DCAE");
        blueprintJsonObject.put("component","dcae");
        blueprintJsonObject.put("distributionInfoUrl", deploymentArtifact.getDistributionInfo().getUrl());
        return blueprintJsonObject;
    }

    @Bean
    private RestTemplate getRestTemplate(){
        return  new RestTemplate();
    }

}
