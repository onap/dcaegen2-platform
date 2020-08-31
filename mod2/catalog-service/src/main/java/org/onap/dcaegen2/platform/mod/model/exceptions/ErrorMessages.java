/*
 * ============LICENSE_START======================================================= 
 *  org.onap.dcae 
 *  ================================================================================ 
 *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved. 
 *  ================================================================================ 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0 
 *  
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 *  ============LICENSE_END========================================================= 
 */

package org.onap.dcaegen2.platform.mod.model.exceptions;

public class ErrorMessages {

    public static final String MICROSERVICE_NAME_CONFLICT_MESSAGE = "Microservice with this name already exists.";
    public static final String MICROSERVICE_TAG_CONFLICT_MESSAGE = "Microservice with this tag name already exists.";
    public static final String MS_TAG_NAME_VALIDATION_MESSAGE =
            "Microservice tag name is Invalid. Accepts lowercase letters and hyphens." +
                    " Tag name length cannot exceed 50 characters";
    public static final String MS_SERVICE_NAME_CONFLICT_MESSAGE = "Microservice with this core name already exists.";
    public static final String MS_SERVICE_NAME_VALIDATION_MESSAGE =
            "Service name is Invalid. Accepts lowercase letters and hyphens";
}
