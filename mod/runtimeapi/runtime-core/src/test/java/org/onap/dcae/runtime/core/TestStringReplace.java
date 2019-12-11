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

import org.junit.Ignore;
import org.junit.Test;

public class TestStringReplace {

    @Test
    @Ignore
    public void testFindTopicFromBlueprint() throws Exception{
        String bp_string = Helper.loadFileContent("src/test/data/blueprints/helloworld_test_1.yaml");
//        //Pattern pattern = Pattern.compile("DCAE-HELLO-WORLD-PUB-MR_topic(.*)_name:[\\s\\S]*default: 'DCAE-HELLO-WORLD-PUB-MR'");
//        Pattern pattern = Pattern.compile("'DCAE-HELLO-WORLD-PUB-MR'");
//        Matcher matcher = pattern.matcher(bp_string);
//
//        while(matcher.find()){
//            System.out.println(bp_string.substring(matcher.start(),matcher.end()));
//        }
        System.out.println(bp_string.replaceAll("'DCAE-HELLO-WORLD-PUB-MR'", "'sample_topic_0'"));
    }
}
