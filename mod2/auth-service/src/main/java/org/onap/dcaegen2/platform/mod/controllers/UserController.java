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

package org.onap.dcaegen2.platform.mod.controllers;

import org.onap.dcaegen2.platform.mod.exceptions.UserNotFoundException;
import org.onap.dcaegen2.platform.mod.models.GenericResponse;
import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.models.UpdateUserRequest;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsServiceImpl;
import org.onap.dcaegen2.platform.mod.services.MODUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * @author
 * @date 09/08/2020
 * User Operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private MODUserDetailService modUserDetailService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    public List<ModUser> getAllUsers() {
        return modUserDetailService.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/{username}")
    public UserDetails getUser(@PathVariable String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{username}")
    public ModUser adminUpdateUserProfile(@PathVariable String username, @RequestBody @Valid UpdateUserRequest
            userRequest, @RequestHeader (name="Authorization") String token) {
        return userDetailsService.adminUpdateUser(username, userRequest, token);
    }

    @PreAuthorize("hasRole('USER') or hasRole('DEVELOPER')")
    @PatchMapping("/user/{username}")
    public ModUser userUpdateOwnProfile(@PathVariable String username, @RequestBody @Valid UpdateUserRequest
            userRequest, @RequestHeader (name="Authorization") String token) {
        return userDetailsService.userUpdateOwnProfile(username, userRequest, token);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        modUserDetailService.deleteUserByUsername(username);
        return ResponseEntity.ok(new GenericResponse("User " + username + " was removed"));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void userNotFoundHandler(UserNotFoundException ex) {
    }
}
