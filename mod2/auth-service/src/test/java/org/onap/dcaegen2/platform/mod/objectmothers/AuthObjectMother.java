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

package org.onap.dcaegen2.platform.mod.objectmothers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.dcaegen2.platform.mod.models.LoginRequest;
import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.Role;
import org.onap.dcaegen2.platform.mod.models.SignupRequest;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsImpl;
import org.onap.dcaegen2.platform.mod.util.TestUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthObjectMother {
    public static final String LOGIN_REQUEST = "src/test/resources/http/requests/AuthLoginRequest.json";
    public static final String SIGNUP_REQUEST = "src/test/resources/http/requests/AuthSignupRequest.json";

    public static String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LoginRequest getLoginRequest() {
        return TestUtil.deserializeJsonFileToModel(LOGIN_REQUEST, LoginRequest.class);
    }

    public static SignupRequest getSignupRequest() {
        return TestUtil.deserializeJsonFileToModel(SIGNUP_REQUEST, SignupRequest.class);
    }

    public static UserDetailsImpl getUserDetailsImpl() {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List authorities = new ArrayList();
        authorities.add(simpleGrantedAuthority);
        return new UserDetailsImpl("admin123", "admin123", "admin123", "admin123", authorities);
    }

    public static ModUser getModUser() {
        ModUser user = new ModUser();
        user.setUsername("test");
        user.setFullName("test");
        user.setPassword("password");
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName("test");
        role.setId("123");
        roles.add(role);
        user.setRoles(roles);
        return user;
    }
}
