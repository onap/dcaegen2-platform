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

import org.onap.dcaegen2.platform.mod.exceptions.RoleNotExistsException;
import org.onap.dcaegen2.platform.mod.exceptions.UserAlreadyExistsException;
import org.onap.dcaegen2.platform.mod.models.*;
import org.onap.dcaegen2.platform.mod.repositories.RoleRepository;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.onap.dcaegen2.platform.mod.security.jwt.JwtUtils;
import org.onap.dcaegen2.platform.mod.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author
 * @date 09/08/2020
 * Authentication Operations
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken() {
        return new ResponseEntity("true", HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid  LoginRequest loginRequest) {
        Authentication authentication = authenticateLoginRequest(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        return setUserContext(authentication, jwt);
    }

    private Authentication authenticateLoginRequest(@RequestBody @Valid LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
    }

    private ResponseEntity<?> setUserContext(Authentication authentication, String jwt) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = setRolesFromUserDetails(userDetails);
        return buildUserContext(jwt, userDetails, roles);
    }

    private List<String> setRolesFromUserDetails(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(item -> ((GrantedAuthority) item).getAuthority())
                .collect(Collectors.toList());
    }

    private ResponseEntity<JwtResponse> buildUserContext(String jwt, UserDetailsImpl userDetails, List<String> roles) {
        return ResponseEntity.ok(JwtResponse.builder()
                .id(userDetails.getId())
                .roles(roles)
                .username(userDetails.getUsername())
                .token(jwt)
                .fullName(userDetails.getFullName())
                .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        checkIfUserExists(request);
        ModUser user = createNewUser(request);
        userRepository.save(user);
        return ResponseEntity.ok(new GenericResponse("User registered successfully!"));
    }

    private void checkIfUserExists(@RequestBody @Valid SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
            throw new UserAlreadyExistsException("Username already exists!");
    }

    private ModUser createNewUser(@RequestBody @Valid SignupRequest request) {
        ModUser user = new ModUser();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPassword(getEncodedPassword(request));
        Set<Role> roles = createRoles(request.getRoles());
        user.setRoles(roles);
        return user;
    }

    private String getEncodedPassword(@RequestBody @Valid SignupRequest request) {
        return passwordEncoder.encode(request.getPassword());
    }

    public Set<Role> createRoles(Set<String> roleStrings) {
        Set<Role> roles = new HashSet<>();
        for (String roleStr : roleStrings) {
            roles.add(getRole(roleStr));
        }
        return roles;
    }

    private Role getRole(String roleStr) {
        return roleRepository.findByName(roleStr).orElseThrow(
                () -> new RoleNotExistsException(String.format("Role %s does not exist", roleStr)));
    }
}


