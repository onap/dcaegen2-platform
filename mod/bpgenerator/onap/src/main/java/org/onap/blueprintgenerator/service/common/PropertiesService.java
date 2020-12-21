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

package org.onap.blueprintgenerator.service.common;

import org.onap.blueprintgenerator.constants.Constants;
import org.onap.blueprintgenerator.model.common.Appconfig;
import org.onap.blueprintgenerator.model.common.GetInput;
import org.onap.blueprintgenerator.model.common.ResourceConfig;
import org.onap.blueprintgenerator.model.componentspec.OnapAuxilary;
import org.onap.blueprintgenerator.model.componentspec.OnapComponentSpec;
import org.onap.blueprintgenerator.model.componentspec.common.Publishes;
import org.onap.blueprintgenerator.model.componentspec.common.Subscribes;
import org.onap.blueprintgenerator.model.dmaap.Streams;
import org.onap.blueprintgenerator.model.dmaap.TlsInfo;
import org.onap.blueprintgenerator.service.base.BlueprintHelperService;
import org.onap.blueprintgenerator.service.dmaap.StreamsService;
import org.onap.blueprintgenerator.model.common.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator Common ONAP Service to create Properties
 * Node
 */
@Service("onapPropertiesService")
public class PropertiesService {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private ResourceConfigService resourceConfigService;

    @Autowired
    private StreamsService streamsService;

    @Autowired
    private ExternalTlsInfoFactoryService externalTlsInfoFactoryService;

    @Autowired
    private BlueprintHelperService blueprintHelperService;

    /**
     * Creates ONAP properties
     *
     * @param inputs Inputs
     * @param onapComponentSpec OnapComponentSpec
     * @param override Override
     * @return
     */
    public Map<String, Object> createOnapProperties(
        Map<String, LinkedHashMap<String, Object>> inputs,
        OnapComponentSpec onapComponentSpec,
        String override) {
        Map<String, Object> response = new HashMap<>();
        org.onap.blueprintgenerator.model.common.Properties properties =
            new org.onap.blueprintgenerator.model.common.Properties();

        GetInput image = new GetInput();
        image.setBpInputName("image");
        properties.setImage(image);

        LinkedHashMap<String, Object> img = new LinkedHashMap<>();
        inputs.put(
            "image",
            blueprintHelperService.createStringInput(onapComponentSpec.getArtifacts()[0].getUri()));

        GetInput location = new GetInput();
        location.setBpInputName("location_id");
        properties.setLocation_id(location);

        LinkedHashMap<String, Object> locMap = new LinkedHashMap();
        inputs.put("location_id", blueprintHelperService.createStringInput(Constants.EMPTY_VALUE));

        properties.setLog_info(onapComponentSpec.getAuxilary().getLog_info());

        GetInput replica = new GetInput();
        replica.setBpInputName("replicas");
        properties.setReplicas(replica);

        LinkedHashMap<String, Object> replicas =
            blueprintHelperService.createIntegerInput("number of instances", 1);
        inputs.put("replicas", replicas);

        OnapAuxilary onapAuxilary = onapComponentSpec.getAuxilary();

        properties.setDocker_config(onapAuxilary);

        Map<String, Object> appConfigResponse =
            appConfigService.createAppconfig(inputs, onapComponentSpec, override, false);
        inputs = (Map<String, LinkedHashMap<String, Object>>) appConfigResponse.get("inputs");
        properties.setApplication_config((Appconfig) appConfigResponse.get("appconfig"));

        GetInput always_pull_image = new GetInput();
        always_pull_image.setBpInputName("always_pull_image");

        properties.setAlways_pull_image(always_pull_image);

        LinkedHashMap<String, Object> inputAlwaysPullImage =
            blueprintHelperService.createBooleanInput(
                "Set to true if the image should always be pulled", true);
        inputs.put("always_pull_image", inputAlwaysPullImage);

        String sType = onapComponentSpec.getSelf().getName();
        sType = sType.replace('.', '-');
        properties.setService_component_type(sType);

        Map<String, Object> tls_info = onapComponentSpec.getAuxilary().getTls_info();
        if (tls_info != null) {
            addTlsInfo(onapComponentSpec, inputs, properties);
            if (tls_info.get(Constants.USE_EXTERNAL_TLS_FIELD) != null) {
                inputs.putAll(addExternalTlsInfo(onapComponentSpec, properties));
            }
        }

        Map<String, Object> resourceConfigResponse =
            resourceConfigService
                .createResourceConfig(inputs, onapComponentSpec.getSelf().getName());
        inputs = (Map<String, LinkedHashMap<String, Object>>) resourceConfigResponse.get("inputs");
        properties
            .setResource_config((ResourceConfig) resourceConfigResponse.get("resourceConfig"));

        response.put("properties", properties);
        response.put("inputs", inputs);
        return response;
    }

    /**
     * Creates Dmaap properties
     *
     * @param inputs Inputs
     * @param onapComponentSpec OnapComponentSpec
     * @param override Override
     * @return
     */
    public Map<String, Object> createDmaapProperties(
        Map<String, LinkedHashMap<String, Object>> inputs,
        OnapComponentSpec onapComponentSpec,
        String override) {
        Map<String, Object> response = new HashMap<>();
        org.onap.blueprintgenerator.model.common.Properties properties =
            new org.onap.blueprintgenerator.model.common.Properties();

        GetInput image = new GetInput();
        image.setBpInputName("tag_version");
        properties.setImage(image);

        LinkedHashMap<String, Object> img = new LinkedHashMap<>();
        inputs.put(
            "tag_version",
            blueprintHelperService.createStringInput(onapComponentSpec.getArtifacts()[0].getUri()));

        GetInput location = new GetInput();
        location.setBpInputName("location_id");
        properties.setLocation_id(location);

        LinkedHashMap<String, Object> locMap = new LinkedHashMap();
        inputs.put("location_id", blueprintHelperService.createStringInput(Constants.EMPTY_VALUE));

        properties.setLog_info(onapComponentSpec.getAuxilary().getLog_info());

        String sType = onapComponentSpec.getSelf().getName();
        sType = sType.replace('.', '-');
        properties.setService_component_type(sType);

        Map<String, Object> tls_info = onapComponentSpec.getAuxilary().getTls_info();
        if (tls_info != null) {
            addTlsInfo(onapComponentSpec, inputs, properties);
            if (tls_info.get(Constants.USE_EXTERNAL_TLS_FIELD) != null) {
                inputs.putAll(addExternalTlsInfo(onapComponentSpec, properties));
            }
        }

        GetInput replica = new GetInput();
        replica.setBpInputName("replicas");
        properties.setReplicas(replica);

        LinkedHashMap<String, Object> rep =
            blueprintHelperService.createIntegerInput("number of instances", 1);
        inputs.put("replicas", rep);

        OnapAuxilary onapAuxilary = onapComponentSpec.getAuxilary();

        properties.setDocker_config(onapAuxilary);

        Map<String, Object> appConfigResponse =
            appConfigService.createAppconfig(inputs, onapComponentSpec, override, true);
        inputs = (Map<String, LinkedHashMap<String, Object>>) appConfigResponse.get("inputs");
        properties.setApplication_config((Appconfig) appConfigResponse.get("appconfig"));

        List<Streams> pubStreams = new ArrayList();
        if (onapComponentSpec.getStreams() != null) {
            if (onapComponentSpec.getStreams().getPublishes() != null) {
                for (Publishes publishes : onapComponentSpec.getStreams().getPublishes()) {
                    if (blueprintHelperService.isMessageRouterType(publishes.getType())) {
                        String topic = publishes.getConfig_key() + Constants._TOPIC;
                        Map<String, Object> streamsMessageRouterResponse =
                            streamsService.createStreams(
                                inputs,
                                topic,
                                publishes.getType(),
                                publishes.getConfig_key(),
                                publishes.getRoute(),
                                'p');
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>)
                                streamsMessageRouterResponse.get("inputs");
                        pubStreams.add((Streams) streamsMessageRouterResponse.get("streams"));
                    } else if (blueprintHelperService.isDataRouterType(publishes.getType())) {
                        String feed = publishes.getConfig_key() + Constants._FEED;
                        Map<String, Object> streamsDataRouterResponse =
                            streamsService.createStreams(
                                inputs,
                                feed,
                                publishes.getType(),
                                publishes.getConfig_key(),
                                publishes.getRoute(),
                                'p');
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>)
                                streamsDataRouterResponse.get("inputs");
                        pubStreams.add((Streams) streamsDataRouterResponse.get("streams"));
                    }
                }
            }
        }

        ArrayList<Streams> subStreams = new ArrayList();
        if (onapComponentSpec.getStreams() != null) {
            if (onapComponentSpec.getStreams().getSubscribes() != null) {
                for (Subscribes subscribes : onapComponentSpec.getStreams().getSubscribes()) {
                    if (blueprintHelperService.isMessageRouterType(subscribes.getType())) {
                        String topic = subscribes.getConfig_key() + Constants._TOPIC;
                        Map<String, Object> streamsMessageRouterResponse =
                            streamsService.createStreams(
                                inputs,
                                topic,
                                subscribes.getType(),
                                subscribes.getConfig_key(),
                                subscribes.getRoute(),
                                's');
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>)
                                streamsMessageRouterResponse.get("inputs");
                        subStreams.add((Streams) streamsMessageRouterResponse.get("streams"));
                    } else if (blueprintHelperService.isDataRouterType(subscribes.getType())) {
                        String feed = subscribes.getConfig_key() + Constants._FEED;
                        Map<String, Object> streamsDataRouterResponse =
                            streamsService.createStreams(
                                inputs,
                                feed,
                                subscribes.getType(),
                                subscribes.getConfig_key(),
                                subscribes.getRoute(),
                                's');
                        inputs =
                            (Map<String, LinkedHashMap<String, Object>>)
                                streamsDataRouterResponse.get("inputs");
                        subStreams.add((Streams) streamsDataRouterResponse.get("streams"));
                    }
                }
            }
        }

        if (!pubStreams.isEmpty()) {
            properties.setStreams_publishes(pubStreams);
        }

        if (!subStreams.isEmpty()) {
            properties.setStreams_subscribes(subStreams);
        }

        Map<String, Object> resourceConfigResponse =
            resourceConfigService
                .createResourceConfig(inputs, onapComponentSpec.getSelf().getName());
        inputs = (Map<String, LinkedHashMap<String, Object>>) resourceConfigResponse.get("inputs");
        properties
            .setResource_config((ResourceConfig) resourceConfigResponse.get("resourceConfig"));

        response.put("properties", properties);
        response.put("inputs", inputs);
        return response;
    }

    private void addTlsInfo(
        OnapComponentSpec onapComponentSpec,
        Map<String, LinkedHashMap<String, Object>> inputs,
        Properties properties) {
        TlsInfo tlsInfo = new TlsInfo();
        tlsInfo.setCertDirectory(
            (String) onapComponentSpec.getAuxilary().getTls_info().get("cert_directory"));
        GetInput useTLSFlag = new GetInput();
        useTLSFlag.setBpInputName("use_tls");
        tlsInfo.setUseTls(useTLSFlag);
        properties.setTls_info(tlsInfo);
        LinkedHashMap<String, Object> useTlsFlagInput =
            blueprintHelperService.createBooleanInput(
                "flag to indicate tls enable/disable",
                onapComponentSpec.getAuxilary().getTls_info().get("use_tls"));
        inputs.put("use_tls", useTlsFlagInput);
    }

    private Map<String, LinkedHashMap<String, Object>> addExternalTlsInfo(
        OnapComponentSpec onapComponentSpec, Properties properties) {
        properties.setExternal_cert(
            externalTlsInfoFactoryService.createFromComponentSpec(onapComponentSpec));
        return externalTlsInfoFactoryService.createInputListFromComponentSpec(onapComponentSpec);
    }
}
