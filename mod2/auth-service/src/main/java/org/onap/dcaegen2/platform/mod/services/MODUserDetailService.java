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

package org.onap.dcaegen2.platform.mod.services;

import org.onap.dcaegen2.platform.mod.models.ModUser;
import org.onap.dcaegen2.platform.mod.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MODUserDetailService {

    @Autowired
    private UserRepository userRepository;

    public MODUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<ModUser> findAll() {
        return userRepository.findAll();
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

//    public ModUser getUserDetails(String id){
//        //ModUser user = userRepository.findByUsername(id);
//        ModUser user = null;
//        if(user == null) throw new UserNotFoundException("");
//        return user;
//    }
}
