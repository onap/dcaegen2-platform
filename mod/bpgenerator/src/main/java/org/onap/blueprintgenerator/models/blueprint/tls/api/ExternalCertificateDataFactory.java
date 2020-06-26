/**
 * ============LICENSE_START=======================================================
 * org.onap.dcae
 * ================================================================================
 * Copyright (c) 2020 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.models.blueprint.tls.api;

import org.onap.blueprintgenerator.models.blueprint.GetInput;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.INPUT_PREFIX;

public abstract class ExternalCertificateDataFactory {

    protected static GetInput createPrefixedGetInput(String fieldName) {
        return new GetInput(addPrefix(fieldName));
    }

    protected static String addPrefix(String fieldName) {
        return INPUT_PREFIX + fieldName;
    }

}
