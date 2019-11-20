/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcae.runtime.web.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@ApiModel(description = "Class representing Action which can be perform on a graph")
public class Action {

    @NotBlank
    @ApiModelProperty(notes = "command to perform on graph",
            allowableValues = "addnode,removenode,renamenode,changenode,addedge,removeedge,changeedge,addgroup,removegroup,renamegroup,changegroup",
            required = true)
    String command;

    @NotBlank
    @ApiModelProperty(notes = "Targeted graph id",required = true)
    String target_graph_id;

    @NotBlank
    @ApiModelProperty(notes = "payload",required = true)
    Map<String, Object> payload;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTarget_graph_id() {
        return target_graph_id;
    }

    public void setTarget_graph_id(String target_graph_id) {
        this.target_graph_id = target_graph_id;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
