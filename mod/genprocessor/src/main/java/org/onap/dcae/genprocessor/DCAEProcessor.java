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

import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class DCAEProcessor extends AbstractProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DCAEProcessor.class);

    // These are properties of the DCAE component that may be useful in the future..
    abstract public String getName();
    abstract public String getVersion();
    abstract public String getComponentId();
    abstract public String getComponentUrl();

    public void ping() {
        LOG.info("pong");
    }

    @Override
    public void onTrigger(ProcessContext arg0, ProcessSession arg1) throws ProcessException {
        LOG.info("Bang you triggered DCAEProcessor!");
        return;
    }

    /**
     * This function gets implemented by the ProcessorBuilder magic to build a new custom list of
     * PropertyDescriptor every time. This is to be used by getSupportedPropertyDescriptors() which
     * *should* only call this method once to initially fill the cache.
     * 
     * @return list of PropertyDescriptor
     */
    abstract protected List<PropertyDescriptor> buildSupportedPropertyDescriptors();

    // Cache of PropertyDescriptors which should be static
    private List<PropertyDescriptor> properties;

    /**
     * This is the critical Nifi function that is used to populate the configuration parameters
     * in the UI and drive all the other configuration related functions in the ConfigurableComponent
     * base class
     */
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        if (this.properties == null) {
            this.properties = buildSupportedPropertyDescriptors();
        }
        return this.properties;
    }

    abstract protected Set<Relationship> buildRelationships();

    // Cache of Relationships which should be static
    private Set<Relationship> relationships;

    @Override
    public Set<Relationship> getRelationships() {
        if (this.relationships == null) {
            this.relationships = buildRelationships();
        }
        return this.relationships;
    }

}

