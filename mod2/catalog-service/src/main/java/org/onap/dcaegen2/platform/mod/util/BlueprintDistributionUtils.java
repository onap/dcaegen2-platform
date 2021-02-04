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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.onap.dcaegen2.platform.mod.model.exceptions.policymodel.PolicyModelDistributionEnvNotFoundException;
import org.onap.dcaegen2.platform.mod.model.policymodel.EnvInfo;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModelDistributionEnv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Policy Model Service Utils to get URL, Username, Password, Webclient
 */

@Component
public class BlueprintDistributionUtils {

    @Value("${dcae.platform.url.path}")
    private String urlpath;

    @Value("${dcae.platform.dev.server}")
    private String devServer;

    @Value("${dcae.platform.dev.port}")
    private String devServerPort;

    @Value("${dcae.platform.dev.user}")
    private String devServerUser;

    @Value("${dcae.platform.dev.password}")
    private String devServerUserPassword;

    @Value("${dcae.platform.pst.server}")
    private String pstServer;

    @Value("${dcae.platform.pst.port}")
    private String pstServerPort;

    @Value("${dcae.platform.pst.user}")
    private String pstServerUser;

    @Value("${dcae.platform.pst.password}")
    private String pstServerUserPassword;

    @Value("${dcae.platform.ete.server}")
    private String eteServer;

    @Value("${dcae.platform.ete.port}")
    private String eteServerPort;

    @Value("${dcae.platform.ete.user}")
    private String eteServerUser;

    @Value("${dcae.platform.ete.password}")
    private String eteServerUserPassword;

    @Value("${dcae.platform.prod.server}")
    private String prodServer;

    @Value("${dcae.platform.prod.port}")
    private String prodServerPort;

    @Value("${dcae.platform.prod.user}")
    private String prodServerUser;

    @Value("${dcae.platform.prod.password}")
    private String prodServerUserPassword;

    Map<String, EnvInfo> envMap;

    /**
     * Creates a Blueprint Distribution Dashboard URL for the Environment requested
     */
    @PostConstruct
    public void init() {
        envMap = new HashMap<>();
        envMap.put(PolicyModelDistributionEnv.DEV.name(), EnvInfo.builder().url("https://"+ devServer + ":" + devServerPort + urlpath).username(devServerUser).password(devServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.PST.name(), EnvInfo.builder().url("https://"+ pstServer + ":" + pstServerPort + urlpath).username(pstServerUser).password(pstServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.ETE.name(), EnvInfo.builder().url("https://"+ eteServer + ":" + eteServerPort + urlpath).username(eteServerUser).password(eteServerUserPassword).build());
        envMap.put(PolicyModelDistributionEnv.PROD.name(), EnvInfo.builder().url("https://"+ prodServer + ":" + prodServerPort + urlpath).username(prodServerUser).password(prodServerUserPassword).build());
    }

    /**
     * Generates a Blueprint Distribution Dashboard URL for the Environment
     *
     * @param env
     * @return
     */
    public String getBlueprintDashboardURL(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Blueprint Dashboard Environment with env %s invalid", env));
        return envMap.get(env).getUrl();
    }

    /**
     * Generates a Blueprint Distribution Dashboard UserName for the Environment
     *
     * @param env
     * @return
     */

    public String getBlueprintDashboardUserName(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Blueprint Dashboard Environment with env %s invalid", env));
        return envMap.get(env).getUsername();
    }


    /**
     * Generates a Blueprint Distribution Dashboard Password for the Environment
     *
     * @param env
     * @return
     */

    public String getBlueprintDashboardPassword(String env) {
        if(!envMap.containsKey(env)) throw new PolicyModelDistributionEnvNotFoundException(String.format("Blueprint Dashboard Environment with env %s invalid", env));
        return envMap.get(env).getPassword();
    }

}
