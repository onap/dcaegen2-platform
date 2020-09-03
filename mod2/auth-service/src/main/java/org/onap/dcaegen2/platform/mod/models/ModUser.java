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

package org.onap.dcaegen2.platform.mod.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class ModUser implements Serializable {

    @Id
    public String _id;

    @NotBlank
    @Size(max = 10)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    @DBRef
    private Set<Role> roles = new HashSet<>();

    public ModUser(@NotBlank @Size(max = 10) String username, @NotBlank String password, @NotBlank String fullName) {
        this.username = username;
        this.password = password;
        this.fullName=fullName;
    }
}
