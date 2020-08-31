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

package org.onap.dcaegen2.platform.mod.web.service.deploymentartifact;

import org.onap.dcaegen2.platform.mod.model.exceptions.deploymentartifact.BlueprintFileNameCreateException;
import org.onap.dcaegen2.platform.mod.model.microserviceinstance.MsInstance;
import org.springframework.stereotype.Component;

/**
 * A name creator for Deployment Artifact files.
 */
@Component
public class ArtifactFileNameCreator {

    private static final String FILE_FORMAT = ".yaml";

    /**
     * creates a file name
     * @param msInstance
     * @param version
     * @return
     */
    public String createFileName(MsInstance msInstance, int version) {
        if(msInstance.getMsInfo() == null || !msInstance.getMsInfo().containsKey("tag")){
            throwException("MS-tag");
        }
        if(msInstance.getActiveSpec() == null){
            throwException("active-spec");
        }
        return  msInstance.getMsInfo().get("tag") + "_"
                + msInstance.getActiveSpec().getType().toString().toLowerCase() + "_"
                + msInstance.getRelease() + "_"
                + version
                + FILE_FORMAT;
   }

    private void throwException(String missingProperty) {
        throw new BlueprintFileNameCreateException("Can not create bluerprint file name: "
                + missingProperty + " is missing");
    }
}
