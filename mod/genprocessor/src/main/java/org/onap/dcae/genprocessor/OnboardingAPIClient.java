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

import java.net.URI;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnboardingAPIClient {

    static final Logger LOG = LoggerFactory.getLogger(OnboardingAPIClient.class);

    public static CompList getComponents(String hostOnboardingAPI) {
        JsonFactory jf = new JsonFactory();
        ObjectMapper om = new ObjectMapper();

        try {
            URI uri = new URI(hostOnboardingAPI + "/components");
            return om.readValue(jf.createParser(uri.toURL()), CompList.class);
        } catch (Exception e) {
            String message = "Error while pulling components from onboarding API";
            LOG.error(message, e);
            throw new OnboardingAPIClientError(message, e);
        }
    }

    public static Comp getComponent(URI componentUri) {
        JsonFactory jf = new JsonFactory();
        ObjectMapper om = new ObjectMapper();

        try {
            return om.readValue(jf.createParser(componentUri.toURL()), Comp.class);
        } catch (Exception e) {
            String message = "Error while pulling component from onboarding API";
            LOG.error(message, e);
            throw new OnboardingAPIClientError(message, e);
        }
    }

    public static class OnboardingAPIClientError extends RuntimeException {
        public OnboardingAPIClientError(String message, Throwable exception) {
            super(message, exception);
        }
    }

}

