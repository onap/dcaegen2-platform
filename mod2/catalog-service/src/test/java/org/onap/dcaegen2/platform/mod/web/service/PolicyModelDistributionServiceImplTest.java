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

package org.onap.dcaegen2.platform.mod.web.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.mod.model.policymodel.PolicyModel;
import org.onap.dcaegen2.platform.mod.util.PolicyModelUtils;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelDistributionServiceImpl;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelGateway;
import org.onap.dcaegen2.platform.mod.web.service.policymodel.PolicyModelServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_ENV;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_MODEL_ID;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelDistributionObjectMother.PM_DISTRIBUTION_MODEL_ID_ERROR;
import static org.onap.dcaegen2.platform.mod.objectmothers.PolicyModelObjectMother.getPolicyModelResponse;

@ExtendWith(MockitoExtension.class)
class PolicyModelDistributionServiceImplTest {

    @Spy
    private PolicyModelDistributionServiceImpl pmDistributionImplService = new PolicyModelDistributionServiceImpl();

    @Mock
    private PolicyModelServiceImpl policyModelServiceImpl;

    @Mock
    private PolicyModelGateway policyModelGateway;

    @Mock
    private PolicyModelUtils policyModelUtils;

    @BeforeEach
    void initialize(){
        pmDistributionImplService.setPolicyModelServiceImpl(policyModelServiceImpl);
        pmDistributionImplService.setPolicyModelGateway(policyModelGateway);
        pmDistributionImplService.setPolicyModelUtils(policyModelUtils);
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnSucess() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        // Calling the same method twice to resolve the issue of mock server hanging for response
        initResponse(mockBackEnd,200,"Operation Successfull");
        initResponse(mockBackEnd,200,"Operation Successfull");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        mockBackEnd.shutdown();
    }


    @Test
    void test_getPolicyModelDistributionByIdReturnUnauthorized()  throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,401,"Authentication Error");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        mockBackEnd.shutdown();
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnForbidden() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,403,"Authorization Error");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);


        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
        mockBackEnd.shutdown();
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnNotFound()  throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,404,"Policy Model Not Found");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
        mockBackEnd.shutdown();
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnUnsupportedMediaType()  throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,415,"Unsupported Media Type");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        mockBackEnd.shutdown();
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnInternalServerError()  throws IOException  {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById("test")).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,500,"Internal Server Error");

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,"test");

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById("test");
        mockBackEnd.shutdown();
    }


    @Test
    void test_distributePolicyModelReturnSucess() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        // Calling the same method twice to resolve the issue of mock server hanging for response
        initResponse(mockBackEnd,200,"Operation Successfull");
        initResponse(mockBackEnd,200,"Operation Successfull");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
        mockBackEnd.shutdown();
    }

    @Test
    void test_distributePolicyModelReturnBadRequest() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,400,"Invalid Body of Policy Model");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
        verify(policyModelGateway, times(1)).save(any());
        mockBackEnd.shutdown();
    }

    @Test
    void test_distributePolicyModelReturnUnauthorized()  throws IOException  {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,401,"Authentication Error");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
        void test_distributePolicyModelReturnForbidden() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,403,"Authorization Error");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnNotAcceptable() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,406,"Not Acceptable Policy Model Version");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnUnsupportedMediaType() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,415,"Unsupported Media Type");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnInternalServerError() throws IOException {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById("test")).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        MockWebServer mockBackEnd = initialize_webServer();

        initResponse(mockBackEnd,500,"Internal Server Error");

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,"test");

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById("test");
        verify(policyModelGateway, times(1)).save(any());
    }

    private MockWebServer initialize_webServer() throws IOException {
        MockWebServer mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        when(policyModelUtils.getPolicyEngineURL(PM_DISTRIBUTION_ENV)).thenReturn(baseUrl);
        when(policyModelUtils.getWebClient(PM_DISTRIBUTION_ENV)).thenReturn(WebClient.create(baseUrl));

        return mockBackEnd;
    }

    private void initResponse(MockWebServer mockBackEnd, int responseCode, String responseBody) {
        String webClientResponse = responseBody;
        mockBackEnd.enqueue(new MockResponse().setResponseCode(responseCode).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }


}