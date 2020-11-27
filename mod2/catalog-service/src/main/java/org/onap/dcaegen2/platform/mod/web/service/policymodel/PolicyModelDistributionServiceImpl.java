/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.platform.mod.web.service.policymodel;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.DistributionInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelDistributionEnv;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelStatus;
import org.onap.dcaegen2.platform.mod.util.PolicyModelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Policy Model Service implementation
 */

@Service
@Setter
@Slf4j
public class PolicyModelDistributionServiceImpl implements PolicyModelDistributionService {

    @Autowired
    PolicyModelServiceImpl policyModelServiceImpl;

    @Autowired
    PolicyModelGateway policyModelGateway;

    @Autowired
    PolicyModelUtils policyModelUtils;

    @Override
    public ResponseEntity getPolicyModelDistributionById(String env,String modelId) {

        String responseBody = null;
        HttpStatus httpStatus;
        String url = policyModelUtils.getPolicyEngineURL(env);
        PolicyModel policyModel = policyModelServiceImpl.getPolicyModelById(modelId);

        try{
            WebClient webClient = policyModelUtils.getWebClient(env);
            WebClient.RequestHeadersSpec<?> requestHeadersSpec = webClient.method(HttpMethod.GET).uri(url + "/" + policyModel.getName());
            httpStatus = requestHeadersSpec.exchange().map(response -> response.statusCode()).block();
            if (httpStatus.is2xxSuccessful()) {
                responseBody = requestHeadersSpec.retrieve().bodyToMono(String.class).block();
            } else if(httpStatus.is4xxClientError()){
                if(httpStatus.value() == 401) {
                    responseBody = "Authentication Error";
                } else if(httpStatus.value() == 403) {
                    responseBody = "Authorization Error";
                } else if(httpStatus.value() == 404) {
                    responseBody = "Resource Not Found";
                }
            } else if(httpStatus.is5xxServerError()){
                responseBody = "Internal Server Error";
            }
            else {
                responseBody = "Problem in getting the Policy Model.";
            }
        }catch(Exception ex){
            log.error("Problem in getting the Policy Model.");
            log.error("error: ", ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseBody = "Problem in getting the Policy Model.";

        }

        return ResponseEntity.status(httpStatus).body(responseBody);

    }

    /**
     * Distributes a Policy Model
     *
     * @param env
     * @param modelId
     * @return
     */
    @Override
    @Transactional
    public ResponseEntity distributePolicyModel(String env, String modelId) {

        String responseBody = null;
        HttpStatus httpStatus;
        String url = policyModelUtils.getPolicyEngineURL(env);
        PolicyModel policyModel = policyModelServiceImpl.getPolicyModelById(modelId);
        String content = policyModel.getContent();
        DistributionInfo distributionInfo = DistributionInfo.builder().url(env).build();

        try{
            WebClient webClient = policyModelUtils.getWebClient(env);
            WebClient.RequestHeadersSpec<?> requestHeadersSpec = webClient.method(HttpMethod.POST).uri(url).bodyValue(content.getBytes());
            httpStatus = requestHeadersSpec.exchange().map(response -> response.statusCode()).block();
            if (httpStatus.is2xxSuccessful()) {
                distributionInfo.setStatus(PolicyModelStatus.SUCCESS);
                responseBody = requestHeadersSpec.retrieve().bodyToMono(String.class).block();
            } else if(httpStatus.is4xxClientError()){
                distributionInfo.setStatus(PolicyModelStatus.FAILED);
                if(httpStatus.value() == 400) {
                    responseBody = "Invalid Body";
                } else if(httpStatus.value() == 401) {
                    responseBody = "Authentication Error";
                } else if(httpStatus.value() == 403) {
                    responseBody = "Authorization Error";
                } else if(httpStatus.value() == 406) {
                    responseBody = "Not Acceptable Version";
                } else if(httpStatus.value() == 415) {
                    responseBody = "UnSupported Media Type";
                }
            } else if(httpStatus.is5xxServerError()){
                responseBody = "Internal Server Error";
                distributionInfo.setStatus(PolicyModelStatus.FAILED);
            }
            else {
                distributionInfo.setStatus(PolicyModelStatus.FAILED);
                responseBody = "Problem in Distributing the Policy Model.";
            }
        }catch(Exception ex){
            log.error("Problem in Distributing the Policy Model.");
            log.error("error: ", ex);
            distributionInfo.setStatus(PolicyModelStatus.FAILED);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseBody = "Problem in Distributing the Policy Model.";

        }
        List<DistributionInfo> distributionInfos = new ArrayList<>();
        distributionInfos.add(distributionInfo);
        policyModel.setDistributionInfo(distributionInfos);

        policyModelGateway.save(policyModel);
        return ResponseEntity.status(httpStatus).body(responseBody);

    }



}
