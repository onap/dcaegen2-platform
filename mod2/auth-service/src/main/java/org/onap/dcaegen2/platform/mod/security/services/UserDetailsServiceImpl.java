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

package org.onap.dcaegen2.platform.mod.security.services;

import org.onap.dcaegen2.platform.mod.controllers.AuthController;
import org.onap.dcaegen2.platform.mod.exceptions.UserNotFoundException;
import org.onap.dcaegen2.platform.mod.exceptions.IllegalUserOperationException;
import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.Role;
import org.onap.dcaegen2.platform.mod.models.UpdateUserRequest;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.onap.dcaegen2.platform.mod.security.jwt.JwtUtils;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    @Setter
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthController authController;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ModUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public ModUser adminUpdateUser(String username, UpdateUserRequest userRequest, String token) {
        return updateUserProfile(username, userRequest);
    }

    public ModUser userUpdateOwnProfile(String username, UpdateUserRequest userRequest, String token) {
        String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        if (usernameFromToken.equals(username)) {
            return updateUserProfile(username, userRequest);
        } else {
            throw new IllegalUserOperationException("Permission denied to update user profile of " + username);
        }
    }

    private ModUser updateUserProfile(String username, UpdateUserRequest userRequest) {
        ModUser modUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", username)));
        modUser = updateUserFields(modUser, userRequest);
        return userRepository.save(modUser);
    }

    private ModUser updateUserFields(ModUser modUser, UpdateUserRequest userRequest) {
        if (userRequest.getFullName() != null) modUser.setFullName(userRequest.getFullName());
        if (userRequest.getPassword() != null) modUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        if (userRequest.getRoles() != null) {
            Set<Role> roles = authController.createRoles(userRequest.getRoles());
            modUser.setRoles(roles);
        }
        return modUser;
    }
}
