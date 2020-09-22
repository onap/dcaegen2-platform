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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.onap.dcaegen2.platform.mod.controllers.AuthController;
import org.onap.dcaegen2.platform.mod.models.*;
import org.onap.dcaegen2.platform.mod.repositories.RoleRepository;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.onap.dcaegen2.platform.mod.security.jwt.AuthEntryPointJwt;
import org.onap.dcaegen2.platform.mod.security.jwt.JwtUtils;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.*;
import static org.onap.dcaegen2.platform.mod.objectmothers.AuthObjectMother.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author
 * @date 09/22/2020
 * Mock Test cases for AuthenticationController
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    UserRepository userRepository;

    @MockBean
    RoleRepository roleRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @MockBean
    AuthEntryPointJwt authEntryPointJwt;

    @Mock
    Authentication authentication;

    @BeforeEach
    void setUp() {
    }


    @Test
    void test_signin_returnsSuccessResponse() throws Exception {

        LoginRequest loginRequest = getLoginRequest();

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(getUserDetailsImpl());
        when(jwtUtils.generateJwtToken(any())).thenReturn("Demo");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(status().isOk());

        verify(authenticationManager, times(1)).authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        verify(jwtUtils, times(1)).generateJwtToken(any());
    }


    @WithMockUser(roles="ADMIN")
    @Test
    void test_signup_returnsSuccessResponse() throws Exception {

        SignupRequest signUpRequest = getSignupRequest();

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("password");
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role()));
        when(userRepository.save(any())).thenReturn(getModUser());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(signUpRequest)).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).existsByUsername(signUpRequest.getUsername());
        verify(passwordEncoder, times(1)).encode(signUpRequest.getPassword());
        verify(roleRepository, times(1)).findByName(anyString());
        verify(userRepository, times(1)).save(any());
    }


}
