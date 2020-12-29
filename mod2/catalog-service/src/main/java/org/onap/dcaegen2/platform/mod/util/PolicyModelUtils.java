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

package org.onap.dcaegen2.platform.mod.util;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.EnvInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelDistributionEnv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Policy Model Service Utils to get URL, Username, Password, Webclient
 */

@Component
public class PolicyModelUtils {

    @Value("${url.path}")
    private String urlpath;

    @Value("${dev.server}")
    private String devServer;

    @Value("${dev.port}")
    private String devServerPort;

    @Value("${dev.user}")
    private String devServerUser;

    @Value("${dev.password}")
    private String devServerUserPassword;

    @Value("${pst.server}")
    private String pstServer;

    @Value("${pst.port}")
    private String pstServerPort;

    @Value("${pst.user}")
    private String pstServerUser;

    @Value("${pst.password}")
    private String pstServerUserPassword;

    @Value("${ete.server}")
    private String eteServer;

    @Value("${ete.port}")
    private String eteServerPort;

    @Value("${ete.user}")
    private String eteServerUser;

    @Value("${ete.password}")
    private String eteServerUserPassword;

    @Value("${prod.server}")
    private String prodServer;

    @Value("${prod.port}")
    private String prodServerPort;

    @Value("${prod.user}")
    private String prodServerUser;

    @Value("${prod.password}")
    private String prodServerUserPassword;

    Map<String, EnvInfo> envMap;

    /**
     * Creates a Policy Model Distribution Engine URL for the Environment requested
     */
    @PostConstruct
    public void init() {
        envMap = new HashMap<>();
        envMap.put(PolicyModelDistributionEnv.DEV.name(), EnvInfo.builder().url("https://"+ devServer + ":" + devServerPort + urlpath).username(devServerUser).password(devServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.DEV.name(), EnvInfo.builder().url("https://"+ pstServer + ":" + pstServerPort + urlpath).username(pstServerUser).password(pstServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.DEV.name(), EnvInfo.builder().url("https://"+ eteServer + ":" + eteServerPort + urlpath).username(eteServerUser).password(eteServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.DEV.name(), EnvInfo.builder().url("https://"+ prodServer + ":" + prodServerPort + urlpath).username(prodServerUser).password(prodServerUserPassword).build());
    }

    /**
     * Generates a Policy Model Distribution Engine URL for the Environment
     *
     * @param env
     * @return
     */
    public String getPolicyEngineURL(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Policy Model Environment with env %s invalid", env));
        return envMap.get(env).getUrl();
    }



    /**
     * Generates a Policy Model Distribution Engine UserName for the Environment
     *
     * @param env
     * @return
     */

    public String getUserName(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Policy Model Environment with env %s invalid", env));
        return envMap.get(env).getUsername();
    }


    /**
     * Generates a Policy Model Distribution Engine Password for the Environment
     *
     * @param env
     * @return
     */

    public String getPassword(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Policy Model Environment with env %s invalid", env));
        return envMap.get(env).getPassword();
    }

    /**
     * Generates a Policy Model Distribution Engine Webclient for the Environment
     *
     * @param env
     * @return
     */
    public WebClient getWebClient(String env) throws SSLException {
        String userName = getUserName(env);
        String password = getPassword(env);

        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/yaml")
                .filter(ExchangeFilterFunctions.basicAuthentication(userName, password))
                .build();
    }



}
