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

package org.onap.dcaegen2.platform.mod.web;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_ENV;
import static org.onap.dcaegen2.platform.mod.objectmothers.BlueprintDistributionObjectMother.BP_DISTRIBUTION_MODEL_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.platform.mod.model.restapi.SuccessResponse;
import org.onap.dcaegen2.platform.mod.web.controller.BlueprintDistributionController;
import org.onap.dcaegen2.platform.mod.web.service.blueprintdistributionservice.BlueprintDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BlueprintDistributionController.class)
public class BlueprintDistributionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BlueprintDistributionService mockBlueprintDistributionService;

    @BeforeEach
    void setup() {
    }


    @Test
    void test_distributeBlueprintById_shouldReturn201AndResponseBody() throws Exception {

        ResponseEntity mockResponseEntity = new ResponseEntity<>(new SuccessResponse("Success"), HttpStatus.OK);

        when(mockBlueprintDistributionService
            .distributeBlueprint(BP_DISTRIBUTION_MODEL_ID,BP_DISTRIBUTION_ENV)).thenReturn(mockResponseEntity);

        mockMvc.perform(post("/api/deployment-artifact/" + BP_DISTRIBUTION_MODEL_ID + "/distribute")
                .param("env",BP_DISTRIBUTION_ENV).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockBlueprintDistributionService, times(1)).distributeBlueprint(BP_DISTRIBUTION_MODEL_ID,BP_DISTRIBUTION_ENV);
    }

}