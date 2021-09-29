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
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

    private final String chartMuseumUrl;

    private final String username;

    private final String password;

    public ChartMuseumDistributor( @Value("${chartmuseum.baseurl}") String chartMuseumUrl,
                                   @Value("${chartmuseum.auth.basic.username}") String username,
                                   @Value("${chartmuseum.auth.basic.password}")String password) {
        this.chartMuseumUrl = chartMuseumUrl;
        this.username = username;
        this.password = password;
    }

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
            if(!response.isSuccessful()){
                throw new RuntimeException("Distribution Failed.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Request createRequestBody(File chartFile) {
        String credential = Credentials.basic(username, password);
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(chartFile, mediaType);
        return new Request.Builder()
                .url(chartMuseumUrl + "/api/charts")
                .method("POST", body)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Authorization", credential)
                .build();
    }
}
