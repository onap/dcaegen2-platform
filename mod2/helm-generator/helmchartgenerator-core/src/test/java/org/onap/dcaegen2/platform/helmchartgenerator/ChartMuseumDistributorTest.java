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

package org.onap.dcaegen2.platform.helmchartgenerator;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.platform.helmchartgenerator.distribution.ChartMuseumDistributor;

import java.io.File;
import java.io.IOException;

public class ChartMuseumDistributorTest {

    ChartMuseumDistributor distributor;

    public static MockWebServer mockChartMuseumServer;

    private static File mockChartFile;

    @BeforeAll
    static void init() throws IOException {
        mockChartMuseumServer = new MockWebServer();
        mockChartMuseumServer.start();
        mockChartFile = File.createTempFile("dcae-ves-collector-1.8.0","tgz");
    }

    @BeforeEach
    void setUp(){
        String baseUrl = String.format("http://localhost:%s",
                mockChartMuseumServer.getPort());
        distributor = new ChartMuseumDistributor(baseUrl, "mockUsername", "mockPassword");
    }

    @Test
    void distribute() throws Exception{
        mockChartMuseumServer.enqueue(getMock201Response());
        distributor.distribute(mockChartFile);
    }

    private MockResponse getMock201Response() {
        return new MockResponse()
                .setResponseCode(201)
                .setBody("{\"saved\": true}");
    }


    @AfterAll
    static void tearDown() throws IOException {
        mockChartMuseumServer.shutdown();
        FileUtils.deleteQuietly(mockChartFile);
    }
}

