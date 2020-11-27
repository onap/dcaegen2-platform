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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    private static MockWebServer mockBackEnd;

    @Spy
    private PolicyModelDistributionServiceImpl pmDistributionImplService = new PolicyModelDistributionServiceImpl();

    @Mock
    private PolicyModelServiceImpl policyModelServiceImpl;

    @Mock
    private PolicyModelGateway policyModelGateway;

    @Mock
    private PolicyModelUtils policyModelUtils;

    @BeforeAll
    static void setUp() throws Exception {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

    }

    void init_Success() {
        String webClientResponse = "Operation Successfully";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(200).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }


    void init_BadRequest() {
        String webClientResponse = "Invalid Body of Policy Model";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(400).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }

    void init_UnAuthorized() {
        String webClientResponse = "Authentication Error";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(401).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }

    void init_Forbidden() {
        String webClientResponse = "Authorization Error";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(403).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }

    void init_NotFound() {
        String webClientResponse = "Policy Model Not Found";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(404).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }

    void init_NotAcceptable() {
        String webClientResponse = "Not Acceptable Policy Model Version";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(406).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }

    void init_UnsupportedMediaType() {
        String webClientResponse = "Unsupported Media Type";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(415).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }


    void init_InternalServerError() {
        String webClientResponse = "Internal Server Error";
        mockBackEnd.enqueue(new MockResponse().setResponseCode(500).setBody(webClientResponse)
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1"));
    }



    @BeforeEach
    void initialize() throws SSLException {
        pmDistributionImplService.setPolicyModelServiceImpl(policyModelServiceImpl);
        pmDistributionImplService.setPolicyModelGateway(policyModelGateway);
        pmDistributionImplService.setPolicyModelUtils(policyModelUtils);

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        when(policyModelUtils.getPolicyEngineURL(PM_DISTRIBUTION_ENV)).thenReturn(baseUrl);
        when(policyModelUtils.getWebClient(PM_DISTRIBUTION_ENV)).thenReturn(WebClient.create(baseUrl));
    }



    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }


    @Test
    void test_getPolicyModelDistributionByIdReturnSucess() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_Success();
        init_Success();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
    }


    @Test
    void test_getPolicyModelDistributionByIdReturnUnauthorized()  throws InterruptedException {
        mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_UnAuthorized();
        init_UnAuthorized();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnForbidden() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_Forbidden();
        init_Forbidden();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);


        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnNotFound() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_NotFound();
        init_NotFound();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnUnsupportedMediaType() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_UnsupportedMediaType();
        init_UnsupportedMediaType();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
    }

    @Test
    void test_getPolicyModelDistributionByIdReturnInternalServerError() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById("test")).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_InternalServerError();
        init_InternalServerError();

        ResponseEntity expected = pmDistributionImplService.getPolicyModelDistributionById(PM_DISTRIBUTION_ENV,"test");

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById("test");
    }


    @Test
    void test_distributePolicyModelReturnSucess() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_Success();
        init_Success();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnBadRequest() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_BadRequest();
        init_BadRequest();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID_ERROR);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID_ERROR);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnUnauthorized()  throws InterruptedException {
        mockBackEnd.takeRequest(2, TimeUnit.SECONDS);
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_UnAuthorized();
        init_UnAuthorized();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
        void test_distributePolicyModelReturnForbidden() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_Forbidden();
        init_Forbidden();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnNotAcceptable() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_NotAcceptable();
        init_NotAcceptable();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnUnsupportedMediaType() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById(PM_DISTRIBUTION_MODEL_ID)).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_UnsupportedMediaType();
        init_UnsupportedMediaType();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,PM_DISTRIBUTION_MODEL_ID);

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById(PM_DISTRIBUTION_MODEL_ID);
        verify(policyModelGateway, times(1)).save(any());
    }

    @Test
    void test_distributePolicyModelReturnInternalServerError() {
        PolicyModel policyModel = getPolicyModelResponse();

        when(policyModelServiceImpl.getPolicyModelById("test")).thenReturn(policyModel);
        when(policyModelGateway.save(any())).thenReturn(policyModel);

        // Calling the same method twice to resolve the issue of mock server hanging for response
        init_InternalServerError();
        init_InternalServerError();

        ResponseEntity expected = pmDistributionImplService.distributePolicyModel(PM_DISTRIBUTION_ENV,"test");

        assertThat(expected.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(policyModelServiceImpl, times(1)).getPolicyModelById("test");
        verify(policyModelGateway, times(1)).save(any());
    }

}