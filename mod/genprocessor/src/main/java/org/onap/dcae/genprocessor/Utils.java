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
package org.onap.dcae.genprocessor;

public class Utils {

    /**
     * Make a name like this "dcae-ves-collector" to "DcaeVesCollector"
     * 
     * @param name
     * @return
     */
    public static String formatNameForJavaClass(String name) {
        // From the sample of 134 specs, 79 had dashes and 102 had dots which means some
        // had both
        String[] segments = name.split("[\\-\\.]");

        for (int i=0; i<segments.length; i++) {
            segments[i] = segments[i].substring(0, 1).toUpperCase() + segments[i].substring(1);
        }

        return String.join("", segments);
    }

    public static String formatNameForJar(CompSpec compSpec) {
        return String.format("%s-%s", compSpec.name, compSpec.version);
    }

}

