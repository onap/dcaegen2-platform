/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.runtime.web.service;

import org.onap.dcae.runtime.core.FlowGraphParser.BlueprintVessel;
import org.onap.dcae.runtime.web.models.DashboardConfig;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BlueprintInventory {

    @Autowired
    DashboardConfig dashboardConfig;

    Logger logger = LoggerFactory.getLogger(BlueprintInventory.class);

    public void distributeToInventory(List<BlueprintVessel> blueprints) {
        for (BlueprintVessel bpv : blueprints) {
            JSONObject body = prepareBlueprintJsonObject(bpv.name, bpv.version, bpv.blueprint);
            postToDashboard(body);
            logger.info(String.format("Distributed: %s", bpv.toString()));
            //System.out.println(bpv.blueprint);
        }
    }

    // Should work with inventory too!
    private boolean postToDashboard(JSONObject blueprintJsonObject){
        //1. setup
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // NOTE: This commented out line is to be used for dcae dashboard api and not inventory
        //headers.setBasicAuth(dashboardConfig.getUsername(),dashboardConfig.getPassword());

        //2. request
        HttpEntity<String> request = new HttpEntity<String>(blueprintJsonObject.toString(), headers);
        try{
            SSLUtils.turnOffSslChecking();
            String response = restTemplate.postForObject(dashboardConfig.getUrl(),request,String.class);
            logger.info(response);
            return true;
        }catch (Exception e) {
            logger.error("failed to push on inventory");
            logger.error(e.getMessage());
            return false;
        }
    }

    private JSONObject prepareBlueprintJsonObject(String blueprintName, int version, String blueprintContent) {
        JSONObject blueprintJsonObject = new JSONObject();
        blueprintJsonObject.put("owner","dcae_mod");
        blueprintJsonObject.put("typeName",blueprintName);
        blueprintJsonObject.put("typeVersion",version);
        blueprintJsonObject.put("blueprintTemplate",blueprintContent);
        blueprintJsonObject.put("application","DCAE");
        blueprintJsonObject.put("component","dcae");
        return blueprintJsonObject;
    }


}
