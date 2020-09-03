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
import org.onap.dcaegen2.platform.mod.repositories.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

public class RoleObjectMother {

    public static List<Role> getRoles() {
        List<Role> roles = new ArrayList();
        Role role1 = new Role();
        role1.setId("123");
        role1.setName("Admin123");

        Role role2 = new Role();
        role2.setId("1234");
        role2.setName("Admin1234");

        roles.add(role1);
        roles.add(role2);

        return roles;
    }

}
