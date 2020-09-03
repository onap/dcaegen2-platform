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


import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.Role;
import org.onap.dcaegen2.platform.mod.models.UpdateUserRequest;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserObjectMother {

    public static final String userId = "admin";

    public static List<ModUser> getUsers() {
        List<ModUser> users = new ArrayList();
        ModUser modUser1 = new ModUser();
        modUser1.set_id("123");
        modUser1.setFullName("Admin123");
        modUser1.setPassword("Admin123");
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName("test");
        role.setId("123");
        roles.add(role);
        modUser1.setRoles(roles);

        ModUser modUser2 = new ModUser();
        modUser2.set_id("1234");
        modUser2.setFullName("Admin1234");
        modUser2.setPassword("Admin1234");
        Set<Role> roles1 = new HashSet<>();
        Role role1 = new Role();
        role.setName("test1");
        role.setId("1234");
        roles.add(role1);
        modUser2.setRoles(roles1);

        users.add(modUser1);
        users.add(modUser2);

        return users;
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

    public static UpdateUserRequest getUpdateUserRequest(){
        UpdateUserRequest user = new UpdateUserRequest();
        user.setFullName("test");
        user.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("Test");
        user.setRoles(roles);
        return user;
    }

}
