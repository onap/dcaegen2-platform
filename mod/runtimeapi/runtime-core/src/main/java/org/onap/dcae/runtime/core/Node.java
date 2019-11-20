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
package org.onap.dcae.runtime.core;

public class Node {
    private  String componentId;
    private  String componentName;
    private  String componentSpec;
    private BlueprintData blueprintData;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentSpec() {
        return componentSpec;
    }

    public void setComponentSpec(String componentSpec) {
        this.componentSpec = componentSpec;
    }

    public BlueprintData getBlueprintData() {
        return blueprintData;
    }

    public void setBlueprintData(BlueprintData blueprintData) {
        this.blueprintData = blueprintData;
    }

    public Node(String componentId, String componentName, String componentSpec) {
        this.componentId = componentId;
        this.componentName = componentName;
        this.componentSpec = componentSpec;
    }



    @Override
    public boolean equals(Object obj) {
        return this.componentId.equals(((Node)obj).componentId);
    }

    @Override
    public String toString() {
        return componentId;
    }
}
