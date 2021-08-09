/*
 * # ============LICENSE_START=======================================================
 * # Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
 * # ================================================================================
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * # ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.helmchartgenerator.distribution;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Distributes helm chart to Chart Museum through REST
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class ChartMuseumDistributor implements ChartDistributor {

    @Value("${chartmuseum.baseurl}")
    private String chartMuseumUrl;

    @Value("${chartmuseum.auth.basic.username}")
    private String username;

    @Value("${chartmuseum.auth.basic.password}")
    private String password;

    /**
     * distributes chart to Chart Museum
     * @param chartFile packaged helm chart tgz file
     */
    @Override
    public void distribute(File chartFile) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = createRequestBody(chartFile);
        try {
            Response response = client.newCall(request).execute();
            log.info(Objects.requireNonNull(response.body()).string());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Request createRequestBody(File chartFile) {
        String credential =Credentials.basic(username, password);
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(chartFile, mediaType);
        Request request = new Request.Builder()
                .url(chartMuseumUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Authorization", credential)
                .build();
        return request;
    }
}
