/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *   Copyright (c) 2020 Nokia. All rights reserved.
 *  *   Copyright (c) 2020 AT&T. All rights reserved.
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
 *
 */

package org.onap.blueprintgenerator.service.common;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.springframework.stereotype.Service;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * Common ONAP Service used by ONAP and DMAAP Blueprint to add External Certificate Data Factory
 */


@Service
public abstract class ExternalCertificateDataFactoryService {

 //  Method to concatenate Constant with field
    protected static GetInput createPrefixedGetInput(String fieldName) {
        return new GetInput(addPrefix(fieldName));
    }

    //  Method to concatenate the Constant INPUT_PREFIX to the input field
    protected static String addPrefix(String fieldName) { return Constants.INPUT_PREFIX + fieldName;  }

}
