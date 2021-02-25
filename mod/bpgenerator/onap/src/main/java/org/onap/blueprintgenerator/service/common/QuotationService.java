/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2021 Nokia. All rights reserved.
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

import java.util.Map;
import org.onap.blueprintgenerator.model.common.OnapBlueprint;
import org.springframework.stereotype.Service;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to set Quotations of
 * generated Blueprint
 */
@Service
public class QuotationService {

    /**
     * Adds Quotes for String Types for generated Blueprint
     *
     * @param bp Blueprint
     * @return
     */
    public OnapBlueprint setQuotations(OnapBlueprint bp) {
        for (String s : bp.getInputs().keySet()) {
            Map<String, Object> temp = bp.getInputs().get(s);
            if (temp.get("type") == "string") {
                String def = (String) temp.get("default");
                if (def != null) {
                    def = def.replaceAll("\"$", "").replaceAll("^\"", "");
                }
                def = '"' + def + '"';
                temp.replace("default", def);
                bp.getInputs().replace(s, temp);
            }
        }
        return bp;
    }
}
