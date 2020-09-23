/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 Copyright (c) 2020 Nokia. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.models.blueprint;

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createBooleanInput;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createIntegerInput;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createStringInput;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isMessageRouterType;
import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.isDataRouterType;
import static org.onap.blueprintgenerator.models.blueprint.tls.TlsConstants.USE_EXTERNAL_TLS_FIELD;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.onap.blueprintgenerator.models.blueprint.tls.ExternalCertificateParametersFactory;
import org.onap.blueprintgenerator.models.blueprint.tls.ExternalTlsInfoFactory;
import org.onap.blueprintgenerator.models.blueprint.tls.TlsInfo;
import org.onap.blueprintgenerator.models.blueprint.tls.impl.ExternalTlsInfo;
import org.onap.blueprintgenerator.models.componentspec.Auxilary;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;
import org.onap.blueprintgenerator.models.dmaapbp.DmaapStreams;


@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class Properties {

    private static final String NO_DESCRIPTION = "";
    private static final String EMPTY_VALUE = "";

    ArrayList<DmaapStreams> streams_publishes;
    ArrayList<DmaapStreams> streams_subscribes;
    private Appconfig application_config;
    private Auxilary docker_config;
    private Object image;
    private GetInput location_id;
    private String service_component_type;
    private TreeMap<String, Object> log_info;
    private String dns_name;
    private Object replicas;
    private String name;
    private GetInput topic_name;
    private GetInput feed_name;
    private TlsInfo tls_info;
    private ExternalTlsInfo external_cert;
    private ResourceConfig resource_config;
    private GetInput always_pull_image;
    //private boolean useExisting;
    @JsonIgnore
    private ExternalTlsInfoFactory externalCertFactory;

    public Properties() {
        ExternalCertificateParametersFactory externalCertificateDataFactory = new ExternalCertificateParametersFactory();
        externalCertFactory = new ExternalTlsInfoFactory(externalCertificateDataFactory);
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createOnapProperties(
        TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec componentSpec, String override) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;

        //set the image
        GetInput image = new GetInput();
        image.setBpInputName("image");
        this.setImage(image);
        retInputs.put("image", createStringInput(NO_DESCRIPTION, componentSpec.getImageUri()));

        //set the location id
        GetInput location = new GetInput();
        location.setBpInputName("location_id");
        this.setLocation_id(location);
        retInputs.put("location_id", createStringInput(NO_DESCRIPTION, EMPTY_VALUE));

        //set the log info
        this.setLog_info(componentSpec.getAuxilary().getLog_info());

        //set the replicas
        GetInput replica = new GetInput();
        replica.setBpInputName("replicas");
        this.setReplicas(replica);
        LinkedHashMap<String, Object> rep = createIntegerInput("number of instances", 1);
        retInputs.put("replicas", rep);

        //set the dns name
        //this.setDns_name(cs.getSelf().getName());

        //set the name
        //this.setName(cs.getSelf().getName());

        //set the docker config
        Auxilary aux = componentSpec.getAuxilary();
//		if(aux.getPorts() != null) {
//			retInputs = aux.createPorts(retInputs);
//		}
        this.setDocker_config(aux);

        //set the app config
        Appconfig app = new Appconfig();
        retInputs = app.createAppconfig(retInputs, componentSpec, override, false);
        this.setApplication_config(app);

        // set always_pull_image
        this.always_pull_image = new GetInput();
        this.always_pull_image.setBpInputName("always_pull_image");
        LinkedHashMap<String, Object> inputAlwaysPullImage = createBooleanInput(
            "Set to true if the image should always be pulled",
            true);
        retInputs.put("always_pull_image", inputAlwaysPullImage);

        //set service component type
        String serviceComponentType = componentSpec.getSelfName().replace('.', '-');
        this.setService_component_type(serviceComponentType);

        //set the tls info for internal and external communication
        TreeMap<String, Object> tls_info = componentSpec.getAuxilary().getTls_info();
        if (tls_info != null) {
            addTlsInfo(componentSpec, retInputs);
            if (tls_info.get(USE_EXTERNAL_TLS_FIELD) != null) {
                retInputs.putAll(addExternalTlsInfo(componentSpec));
            }
        }

        //set the reource config
        ResourceConfig resource = new ResourceConfig();
        retInputs = resource.createResourceConfig(retInputs, componentSpec.getSelf().getName());
        this.setResource_config(resource);

        return retInputs;
    }

    public TreeMap<String, LinkedHashMap<String, Object>> createDmaapProperties(
        TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec componentSpec, String override) {
        TreeMap<String, LinkedHashMap<String, Object>> retInputs = inps;

        //set the image
        GetInput image = new GetInput();
        image.setBpInputName("tag_version");
        this.setImage(image);
        retInputs.put("tag_version", createStringInput(NO_DESCRIPTION, componentSpec.getImageUri()));

        //set the location id
        GetInput location = new GetInput();
        location.setBpInputName("location_id");
        this.setLocation_id(location);
        retInputs.put("location_id", createStringInput(NO_DESCRIPTION, EMPTY_VALUE));

        //set the log info
        this.setLog_info(componentSpec.getAuxilary().getLog_info());

        //set service component type
        String serviceComponentType = componentSpec.getSelfName().replace('.', '-');
        this.setService_component_type(serviceComponentType);

        //set the tls info for internal and external communication
        TreeMap<String, Object> tls_info = componentSpec.getAuxilary().getTls_info();
        if (tls_info != null) {
            addTlsInfo(componentSpec, retInputs);
            if (tls_info.get(USE_EXTERNAL_TLS_FIELD) != null) {
                retInputs.putAll(addExternalTlsInfo(componentSpec));
            }
        }

        //set the replicas
        GetInput replica = new GetInput();
        replica.setBpInputName("replicas");
        this.setReplicas(replica);
        LinkedHashMap<String, Object> rep = createIntegerInput("number of instances", 1);
        retInputs.put("replicas", rep);

//		//set the dns name
//		this.setDns_name(cs.getSelf().getName());

//		//set the name
//		this.setName(cs.getSelf().getName());

        //set the docker config
        Auxilary aux = componentSpec.getAuxilary();
//		if(aux.getPorts() != null) {
//			retInputs = aux.createPorts(retInputs);
//		}
        this.setDocker_config(aux);

        //set the appconfig
        Appconfig app = new Appconfig();
        retInputs = app.createAppconfig(retInputs, componentSpec, override, true);
        this.setApplication_config(app);

        //set the stream publishes
        ArrayList<DmaapStreams> pubStreams = new ArrayList<>();
        if (componentSpec.getStreams().getPublishes() != null) {
            for (Publishes publishes : componentSpec.getStreams().getPublishes()) {
                if (isMessageRouterType(publishes.getType())) {
                    String topic = publishes.getConfig_key() + "_topic";
                    DmaapStreams mrStreams = new DmaapStreams();
                    retInputs = mrStreams
                        .createStreams(inps, componentSpec, topic, publishes.getType(), publishes.getConfig_key(),
                            publishes.getRoute(), 'p');
                    pubStreams.add(mrStreams);
                } else if (isDataRouterType(publishes.getType())) {
                    String feed = publishes.getConfig_key() + "_feed";
                    DmaapStreams drStreams = new DmaapStreams();
                    retInputs = drStreams
                        .createStreams(inps, componentSpec, feed, publishes.getType(), publishes.getConfig_key(),
                            publishes.getRoute(), 'p');
                    pubStreams.add(drStreams);
                }
            }
        }

        //set the stream subscribes
        ArrayList<DmaapStreams> subStreams = new ArrayList<>();
        if (componentSpec.getStreams().getSubscribes() != null) {
            for (Subscribes subscribes : componentSpec.getStreams().getSubscribes()) {
                if (isMessageRouterType(subscribes.getType())) {
                    String topic = subscribes.getConfig_key() + "_topic";
                    DmaapStreams mrStreams = new DmaapStreams();
                    retInputs = mrStreams
                        .createStreams(inps, componentSpec, topic, subscribes.getType(), subscribes.getConfig_key(),
                            subscribes.getRoute(), 's');
                    subStreams.add(mrStreams);
                } else if (isDataRouterType(subscribes.getType())) {
                    String feed = subscribes.getConfig_key() + "_feed";
                    DmaapStreams drStreams = new DmaapStreams();
                    retInputs = drStreams
                        .createStreams(inps, componentSpec, feed, subscribes.getType(), subscribes.getConfig_key(),
                            subscribes.getRoute(), 's');
                    subStreams.add(drStreams);
                }
            }
        }

        if (!pubStreams.isEmpty()) {
            this.setStreams_publishes(pubStreams);
        }
        if (!subStreams.isEmpty()) {
            this.setStreams_subscribes(subStreams);
        }

        //set the reource config
        ResourceConfig resource = new ResourceConfig();
        retInputs = resource.createResourceConfig(retInputs, componentSpec.getSelf().getName());
        this.setResource_config(resource);

        return retInputs;
    }

    private void addTlsInfo(ComponentSpec cs, TreeMap<String, LinkedHashMap<String, Object>> retInputs) {
        TlsInfo tlsInfo = new TlsInfo();
        tlsInfo.setCertDirectory((String) cs.getAuxilary().getTls_info().get("cert_directory"));
        GetInput useTLSFlag = new GetInput();
        useTLSFlag.setBpInputName("use_tls");
        tlsInfo.setUseTls(useTLSFlag);
        this.setTls_info(tlsInfo);
        LinkedHashMap<String, Object> useTlsFlagInput = createBooleanInput(
            "flag to indicate tls enable/disable",
            cs.getAuxilary().getTls_info().get("use_tls"));
        retInputs.put("use_tls", useTlsFlagInput);
    }

    private Map<String, LinkedHashMap<String, Object>> addExternalTlsInfo(ComponentSpec cs) {
        this.setExternal_cert(externalCertFactory.createFromComponentSpec(cs));
        return externalCertFactory.createInputListFromComponentSpec(cs);
    }

}
