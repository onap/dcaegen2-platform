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

package org.onap.dcaegen2.platform.mod;

import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.Role;
import org.onap.dcaegen2.platform.mod.repositories.RoleRepository;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        populateRoles();
        populateAdminUser();
    }

    private void populateRoles() {
        List<Role> roles = createRoles();
        roles.forEach((role) -> {
            boolean roleNotPresent = !roleRepository.findByName(role.getName()).isPresent();
            if(roleNotPresent)
                roleRepository.save(role);
        });

    }

    private List<Role> createRoles() {
        Role admin = new Role("ROLE_ADMIN");
        Role user = new Role("ROLE_USER");
        Role scrumLead = new Role("ROLE_SCRUMLEAD");
        Role developer = new Role("ROLE_DEVELOPER");
        Role pst = new Role("ROLE_PST");
        Role ops = new Role("ROLE_OPS");
        Role ete = new Role("ROLE_ETE");
        return Arrays.asList(admin, user, scrumLead, developer, pst, ops, ete);
    }

    private void populateAdminUser() {
        boolean adminNotPresent = !userRepository.findByUsername("admin").isPresent();
        if(adminNotPresent) {
            ModUser admin = createAdmin();
            userRepository.save(admin);
        }
    }

    private ModUser createAdmin() {
        ModUser admin = new ModUser();
        admin.setUsername("admin");
        admin.setFullName("Admin");
        admin.setPassword(passwordEncoder.encode("admin@mod"));
        HashSet<Role> roleAdmin = new HashSet<>();
        roleAdmin.add(roleRepository.findByName("ROLE_ADMIN").get());
        admin.setRoles(roleAdmin);
        return admin;
    }
}
