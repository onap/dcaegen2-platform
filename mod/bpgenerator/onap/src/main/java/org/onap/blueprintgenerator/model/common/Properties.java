/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
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

package org.onap.blueprintgenerator.model.common;

import org.onap.blueprintgenerator.model.componentspec.OnapAuxilary;
import org.onap.blueprintgenerator.model.dmaap.Streams;
import org.onap.blueprintgenerator.model.dmaap.TlsInfo;
import org.onap.blueprintgenerator.service.common.ExternalTlsInfoFactoryService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator ONAP Common Model: A model class which
 * represents Properties
 */
@Data
@JsonInclude(value = Include.NON_NULL)
public class Properties {

    private Appconfig application_config;

    private OnapAuxilary docker_config;

    private Object image;

    private GetInput location_id;

    private String service_component_type;

    private Map<String, Object> log_info;

    private String dns_name;

    private Object replicas;

    private String name;

    private GetInput topic_name;

    private GetInput feed_name;

    private List<Streams> streams_publishes;

    private List<Streams> streams_subscribes;

    private TlsInfo tls_info;

    private ResourceConfig resource_config;

    private GetInput always_pull_image;

    private Boolean useExisting;

    @JsonIgnore
    private ExternalTlsInfoFactoryService externalCertFactory;

    private ExternalTlsInfo external_cert;
}
