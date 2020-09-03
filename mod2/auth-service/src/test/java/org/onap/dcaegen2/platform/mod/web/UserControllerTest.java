/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 */

package org.onap.dcaegen2.platform.mod.web;

import org.apache.tools.ant.taskdefs.optional.extension.Specification;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.Request;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.platform.mod.controllers.RoleController;
import org.onap.dcaegen2.platform.mod.controllers.UserController;
import org.onap.dcaegen2.platform.mod.models.LoginRequest;
import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.UpdateUserRequest;
import org.onap.dcaegen2.platform.mod.repositories.RoleRepository;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.onap.dcaegen2.platform.mod.security.jwt.AuthEntryPointJwt;
import org.onap.dcaegen2.platform.mod.security.jwt.JwtUtils;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsServiceImpl;
import org.onap.dcaegen2.platform.mod.services.MODUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.onap.dcaegen2.platform.mod.objectmothers.AuthObjectMother.asJsonString;
import static org.onap.dcaegen2.platform.mod.objectmothers.AuthObjectMother.getModUser;
import static org.onap.dcaegen2.platform.mod.objectmothers.RoleObjectMother.getRoles;
import static org.onap.dcaegen2.platform.mod.objectmothers.UserObjectMother.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author
 * @date 09/22/2020
 * Mock Test cases for UserController
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    MODUserDetailService modUserDetailService;

    @MockBean
    RoleRepository roleRepository;

    @MockBean
    AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    JwtUtils jwtUtils;

    @Mock
    Authentication authentication;


    @BeforeEach
    void setUp() {
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void test_getUsername() throws Exception {

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new ModUser()));

        MvcResult result =  mockMvc.perform(get("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        Assert.assertNotNull(result.getResponse().getContentAsString());
        verify(userRepository, times(1)).findByUsername(any());
    }

    @Test
    void test_getAllUsers() throws Exception {

        when(modUserDetailService.findAll()).thenReturn(getUsers());

        MvcResult result =  mockMvc.perform(get("/api/users/getAll")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        Assert.assertNotNull(result.getResponse().getContentAsString());
        verify(modUserDetailService, times(1)).findAll();
    }


    @WithMockUser(roles="ADMIN")
    @Test
    void test_deleteUser() throws Exception {

        doNothing().when(modUserDetailService).deleteUserByUsername(any(String.class));

        MvcResult result =  mockMvc.perform(delete("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        Assert.assertNotNull(result.getResponse().getContentAsString());
        verify(modUserDetailService, times(1)).deleteUserByUsername(any(String.class));
    }


    @WithMockUser(username="ADMIN")
    @Test
    void test_userUpdateOwnProfile_returnsSuccessResponse() throws Exception {
        //arrange
        UpdateUserRequest updateUserRequest = getUpdateUserRequest();

        when(userDetailsService.adminUpdateUser(userId,updateUserRequest,"token")).thenReturn(getModUser());

        mockMvc.perform(patch("/api/users/admin/" + userId)
                //.header("Authorization", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateUserRequest)).accept(MediaType.APPLICATION_JSON))
                //.andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(status().isOk()).andReturn();

        verify(userDetailsService, times(1)).adminUpdateUser(anyString(),updateUserRequest,anyString());
    }


}