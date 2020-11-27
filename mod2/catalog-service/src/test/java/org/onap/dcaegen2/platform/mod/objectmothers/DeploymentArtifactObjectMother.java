/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.mod.objectmothers;

import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifact;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.DeploymentArtifactStatus;
import org.onap.dcaegen2.platform.mod.model.deploymentartifact.MsInstanceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onap.dcaegen2.platform.mod.objectmothers.MsInstanceObjectMother.USER;

public class DeploymentArtifactObjectMother {


    public static final String BLUEPRINT_FILENAME = "hello-world-k8s-blueprint.yaml";
    public static final String BLUEPRINT_CONTENT = "\\n#Basic java app to print out at&t buzzwords\\n#1.0" +
            ".0\\n#\\n---\\" + "ntosca_definitions_version: cloudify_dsl_1_3\\nimports:\\n- http://www.getcloudify" +
            ".org/spec/cloudify/4.4/types" + ".yaml\\n- http://dockercentral.it.att" +
            ".com:8093/nexus/repository/rawcentral/com.att.dcae.controller/type_files/" + "k8splugin/1.7.4/node-type" +
            ".yaml\\n- http://dockercentral.it.att.com:8093/nexus/repository/rawcentral/com.att.d" + "cae.controller" +
            "/type_files/relationship/2006001.1.0/types.yaml\\n- http://dockercentral.it.att.com:8093/nexus/" +
            "repository/rawcentral/com.att.dcae.controller/type_files/cloudifydmaapplugin/1.4.10/node-type.yaml\\n- " +
            "http:/" + "/dockercentral.it.att.com:8093/nexus/repository/rawcentral/com.att.dcae" +
            ".controller/type_files/dcaepolicyplugi" + "n/2.3.3/node-type.yaml\\n- http://dockercentral.it.att" +
            ".com:8093/nexus/repository/rawcentral/com.att.dcae.cont" + "roller/type_files/pgaas/0.3.2/pgaas_types" +
            ".yaml\\ninputs:\\n  ConsulTest1:\\n    type: string\\n    description" + ": test description\\n    " +
            "default: 'TEST1'\\n  ConsulTest2:\\n    type: string\\n    description: test description\\n    default: " +
            "'TEST2'\\n  aaf_cert_directory:\\n    type: string\\n    default: '/opt/app/aafcertman'\\n    " +
            "description: directory location for the aaf-tls certs\\n  additionalsans:\\n    type: string\\n    " +
            "default: ''\\n    description: additional sans (string)\\n  annotations:\\n    default: {}\\n  " +
            "app_name:\\n    type: string\\n    default: 'dcae'\\n    description: This is used to generateForRelease different" +
            " secret code for DCAE or D2A based\\n      on Tosca or Helm based BP\\n  dcae_service_location:\\n    " +
            "type: string\\n    description: Docker host override for docker bps (string)\\n  " +
            "dti_sidecar_cpu_limit:\\n    type: string\\n    default: '250m'\\n    description: cpu limit for " +
            "deployment (string)\\n  dti_sidecar_cpu_request:\\n    type: string\\n    default: '250m'\\n    " +
            "description: cpu requested for deployment (string)\\n  dti_sidecar_image:\\n    type: string\\n    " +
            "default: 'dockercentral.it.att.com:5100/com.att.dcae.controller/dcae-controller-sidecar:19.11-001'\\n   " +
            " description: dti side car image for dti (string)\\n  dti_sidecar_memory_limit:\\n    type: string\\n   " +
            " default: '128Mi'\\n    description: memory limit for deployment (string)\\n  " +
            "dti_sidecar_memory_request:\\n    type: string\\n    default: '128Mi'\\n    description: memory " +
            "requested for deployment (string)\\n  dti_sidecar_port:\\n    type: string\\n    default: ''\\n    " +
            "description: Port for the side car (string)\\n  hello-buzzword_cpu_limit:\\n    type: string\\n    " +
            "default: '250m'\\n    description: cpu limit for deployment (string)\\n  hello-buzzword_cpu_request:\\n " +
            "   type: string\\n    default: '250m'\\n    description: cpu requested for deployment (string)\\n  " +
            "hello-buzzword_memory_limit:\\n    type: string\\n    default: '128Mi'\\n    description: memory limit " +
            "for deployment (string)\\n  hello-buzzword_memory_request:\\n    type: string\\n    default: '128Mi'\\n " +
            "   description: memory requested for deployment (string)\\n  idns_fqdn:\\n    type: string\\n    " +
            "default: ''\\n    description: The idns you will be using for your deployment (string)\\n  image:\\n    " +
            "type: string\\n    default: 'test-image-uri'\\n    description: The docker image for your microservice " +
            "(string)\\n  namespace:\\n    type: string\\n  replicas:\\n    type: integer\\n    default: 1\\n    " +
            "description: The number of replicas for your kubernetes deployment (integer)\\n  " +
            "service_component_name_override:\\n    type: string\\n    default: 'hello-buzzword'\\n    description: " +
            "Unique identifier for your deployment (string)\\n  use_aaf_tls:\\n    type: boolean\\n    default: " +
            "false\\n    description: To use or not use the aaf section (boolean)\\n  use_dti_info:\\n    type: " +
            "boolean\\n    default: true\\n    description: Flag to use or not use dti (boolean)\\nnode_templates:\\n" +
            "  hello-buzzword_hello-buzzword:\\n    type: dcae.nodes.ContainerizedServiceComponent\\n    " +
            "properties:\\n      application_config:\\n        services_calls: []\\n        streams_publishes: {}\\n " +
            "       streams_subscribes: {}\\n        ConsulTest1:\\n          get_input: ConsulTest1\\n        " +
            "ConsulTest2:\\n          get_input: ConsulTest2\\n      docker_config:\\n        healthcheck:\\n        " +
            "  interval: 180s\\n          timeout: 30s\\n          script: \\\"true\\\"\\n          type: docker\\n  " +
            "      livehealthcheck:\\n          interval: 180s\\n          timeout: 30s\\n          script: " +
            "\\\"true\\\"\\n          type: docker\\n        reconfigs:\\n          dti: dti/test-script\\n          " +
            "app_reconfig: /app-reconfig/test-script\\n        env:\\n        - name: DTI_DATA_DIR\\n          value:" +
            " /dtidata\\n        - name: KUBE_CLUSTER_FQDN\\n          value: {get_secret: " +
            "kc-kubernetes_master_ip}\\n      image:\\n        get_input: image\\n      location_id:\\n        " +
            "get_input: dcae_service_location\\n      service_component_type: hello-buzzword\\n      replicas:\\n    " +
            "    get_input: replicas\\n      service_component_name_override:\\n        concat:\\n        - " +
            "get_secret: location_id\\n        - '-'\\n        - get_input: service_component_name_override\\n      " +
            "k8s_controller_type: statefulset\\n      configuration:\\n        file_content:\\n          apiVersion: " +
            "v1\\n          clusters:\\n          - name: default-cluster\\n            cluster:\\n              " +
            "server:\\n                concat:\\n                - https://\\n                - get_secret: " +
            "kc-kubernetes_master_ip\\n                - ':'\\n                - get_secret: " +
            "kc-kubernetes_master_port\\n              insecure-skip-tls-verify: true\\n          contexts:\\n       " +
            "   - name: default-context\\n            context:\\n              cluster: default-cluster\\n           " +
            "   namespace:\\n                get_input: namespace\\n              user: default-user\\n          " +
            "kind: Config\\n          preferences: {}\\n          users:\\n          - name: default-user\\n         " +
            "   user:\\n              token:\\n                get_secret:\\n                  concat:\\n            " +
            "      - get_input: app_name\\n                  - -mechid-k8s-token\\n          current-context: " +
            "default-context\\n      resource_config:\\n        limits:\\n          cpu:\\n            get_input: " +
            "hello-buzzword_cpu_limit\\n          memory:\\n            get_input: hello-buzzword_memory_limit\\n    " +
            "    requests:\\n          cpu:\\n            get_input: hello-buzzword_cpu_request\\n          " +
            "memory:\\n            get_input: hello-buzzword_memory_request\\n      aaf_tls_info:\\n        " +
            "use_aaf_tls:\\n          get_input: use_aaf_tls\\n        cert_directory:\\n          get_input: " +
            "aaf_cert_directory\\n        image: dockercentral.it.att.com:5100/com.att.ecompcntr" +
            ".public/ecompc-aaf-init-container:1.0.2\\n        env:\\n        - name: NAMESPACE\\n          " +
            "valueFrom:\\n            fieldRef:\\n              fieldPath: metadata.namespace\\n        - name: " +
            "deployer_id\\n          valueFrom:\\n            secretKeyRef:\\n              name:\\n                " +
            "concat:\\n                - get_input: namespace\\n                - -cert-secret\\n              key: " +
            "deployerid\\n        - name: deployer_pass\\n          valueFrom:\\n            secretKeyRef:\\n        " +
            "      name:\\n                concat:\\n                - get_input: namespace\\n                - " +
            "-cert-secret\\n              key: deployerpass\\n        - name: cert_id\\n          valueFrom:\\n      " +
            "      secretKeyRef:\\n              name:\\n                concat:\\n                - get_input: " +
            "namespace\\n                - -cert-secret\\n              key: certid\\n        - name: cm_url\\n      " +
            "    valueFrom:\\n            secretKeyRef:\\n              name:\\n                concat:\\n           " +
            "     - get_input: namespace\\n                - -cert-secret\\n              key: cmurl\\n        - " +
            "name: idns_fqdn\\n          value:\\n            get_input: idns_fqdn\\n        - name: " +
            "app_service_names\\n          value:\\n            concat:\\n            - get_secret: location_id\\n   " +
            "         - '-'\\n            - get_input: service_component_name_override\\n        args:\\n        - " +
            "place\\n        - cmtemplate\\n        - -idnsfqdn=$(idns_fqdn)\\n        - -cmurl=$(cm_url)\\n        -" +
            " -deployerid=$(deployer_id)\\n        - -deployerpass=$(deployer_pass)\\n        - -certid=$(cert_id)\\n" +
            "        - -namespace=$(NAMESPACE)\\n        - -services=$(app_service_names)\\n        - concat:\\n     " +
            "     - -additionalsans=\\n          - get_input: additionalsans\\n        use_aaf_tls_renewal: true\\n  " +
            "      renewal_args:\\n        - renew\\n        - -idnsfqdn=$(idns_fqdn)\\n        - -cmurl=$(cm_url)\\n" +
            "        resource_config:\\n          limits:\\n            cpu: 250m\\n            memory: 256Mi\\n     " +
            "     requests:\\n            cpu: 100m\\n            memory: 256Mi\\n      annotations:\\n        " +
            "get_input: annotations\\n      dti_info:\\n        image:\\n          get_input: dti_sidecar_image\\n   " +
            "     use_dti_info:\\n          get_input: use_dti_info\\n        healthcheck:\\n          interval: " +
            "90s\\n          timeout: 60s\\n          type: https\\n          endpoint: /healthcheck\\n        " +
            "livehealthcheck:\\n          interval: 90s\\n          timeout: 60s\\n          type: https\\n          " +
            "endpoint: /healthcheck\\n        dtidata_directory: /dtidata\\n        resource_config:\\n          " +
            "limits:\\n            cpu:\\n              get_input: dti_sidecar_cpu_limit\\n            memory:\\n    " +
            "          get_input: dti_sidecar_memory_limit\\n          requests:\\n            cpu:\\n              " +
            "get_input: dti_sidecar_cpu_request\\n            memory:\\n              get_input: " +
            "dti_sidecar_memory_request\\n        env:\\n        - name: DTI_DATA_DIR\\n          value: /dtidata\\n " +
            "       - name: KUBE_CLUSTER_FQDN\\n          value: {get_secret: kc-kubernetes_master_ip}\\n        - " +
            "name: KUBE_PROXY_FQDN\\n          value: {get_secret: kube_proxy_fqdn}\\n        - name: POD_SVC_PORT\\n" +
            "          value: '9999'\\n        ports:\\n        - concat:\\n          - '9999:'\\n          - " +
            "get_input: dti_sidecar_port\\n    relationships: []";

    public static final String SPEC_FILE_AS_STRING = String.format("{\r\n\t\"self\": {\r\n\t\t\"component_type\": " +
            "\"docker\",\r\n\t\t\"description\": \"Basic java app to print out at&t buzzwords\",\r\n\t\t\"name\": " +
            "\"hello-buzzword\",\r\n\t\t\"version\": \"1.0.0\"\r\n\t},\r\n\t\r\n\t\"services\": {\r\n\t\t\"calls\": " +
            "[],\r\n\t\t\"provides\": []\r\n\t},\r\n\t\"streams\": {\r\n\t\t\"publishes\": [],\r\n\t\t\"subscribes\":" +
            " []\r\n\t},\r\n\t\"parameters\": [\r\n\t\t{\r\n            \"name\": \"ConsulTest1\",\r\n            " +
            "\"value\": \"TEST1\",\r\n            \"description\": \"Test consul output\"," +
            "\r\n\t\t\t\"sourced_at_deployment\": true,\r\n\t\t\t\"designer_editable\": true," +
            "\r\n\t\t\t\"policy_editable\": false,\r\n\t\t\t\"type\": \"string\" ,\r\n\t\t\t\"description\": \"test " +
            "description\"        \r\n        },\r\n        {\r\n            \"name\": \"ConsulTest2\",\r\n          " +
            "  \"value\": \"TEST2\",\r\n\t\t\t\"sourced_at_deployment\": true,\r\n\t\t\t\"designer_editable\": true," +
            "\r\n\t\t\t\"policy_editable\": false,\r\n\t\t\t\"type\": \"string\",\r\n\t\t\t\"description\": \"test " +
            "description\"    \r\n        }\r\n       \r\n\t],\r\n\r\n\t\"auxilary\": {\r\n\t\t\"healthcheck\": " +
            "{\r\n\t\t\t\"type\": \"docker\",\r\n        \t\"script\": \"true\",\r\n        \t\"timeout\": \"30s\"," +
            "\r\n        \t\"interval\": \"180s\"\r\n\t\t},\r\n\t\t\"livehealthcheck\": {\r\n\t\t\t\"type\": " +
            "\"docker\",\r\n        \t\"script\": \"true\",\r\n        \t\"timeout\": \"30s\",\r\n        " +
            "\t\"interval\": \"180s\"\r\n\t\t},\r\n\t\t\"reconfigs\": {\r\n\t\t\t\"app_reconfig\" : " +
            "\"/app-reconfig/test-script\",\r\n\t\t\t\"dti\" : \"dti/test-script\"}}," +
            "\r\n\t\"artifacts\": [{\r\n\t\t\"type\": \"docker image\",\r\n\t\t\"uri\": " +
            "\"test-image-uri\"\r\n\t}]\r\n}");

    public static DeploymentArtifact createDeploymentArtifactDAO(DeploymentArtifactStatus status) {
        DeploymentArtifact artifact = new DeploymentArtifact();
        artifact.setId("id-123");
        artifact.setFileName("helloworld-k8s-blueprint.yaml");
        artifact.setContent("some " + "yaml content");
        artifact.setStatus(status);
        artifact.setVersion(1);
        artifact.setMetadata(createMetaData());
        artifact.setMsInstanceInfo(createMsInstanceInfo());
        artifact.setSpecificationInfo(createSpecificationInfo());

       return artifact;
    }

    private static Map<String, Object> createSpecificationInfo() {
        Map<String, Object> msInstanceInfo = new HashMap<>();
        msInstanceInfo.put("id", "id-123");
        return msInstanceInfo;
    }

    private static MsInstanceInfo createMsInstanceInfo() {
        MsInstanceInfo msInstanceInfo = new MsInstanceInfo();
        msInstanceInfo.setId(MsInstanceObjectMother.MS_INSTANCE_ID);
        msInstanceInfo.setName(MsInstanceObjectMother.MS_INSTANCE_NAME);
        msInstanceInfo.setRelease(MsInstanceObjectMother.RELEASE);
        return msInstanceInfo;
    }

    private static Map<String, Object> createMetaData() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdBy", USER);
        metadata.put("createdOn", "someDate");
        metadata.put("notes", "This is a test Deployment Artifact");
        metadata.put("labels", Arrays.asList("hello", "world"));
        return metadata;
    }

    public static Map<String, Object> createBlueprintResponse() {
        Map<String, Object> blueprintMap = new HashMap<>();
        blueprintMap.put("fileName", BLUEPRINT_FILENAME);
        blueprintMap.put("content", BLUEPRINT_CONTENT); return blueprintMap;
    }

    public static Map<String, Object> createToolboxBlueprintResponse() {
        Map<String, Object> blueprintResponseMap = new HashMap<>();
        blueprintResponseMap.put("blueprint_name", "hello-buzzword-eom-k8s");
        blueprintResponseMap.put("blueprint_content", BLUEPRINT_CONTENT);
        blueprintResponseMap.put("componentSpecValidated", true);
        return blueprintResponseMap;
    }

    public static List<DeploymentArtifact> createMockDeploymentArtifactsWithDifferentStatuses
            (boolean devCompleteRequire) {
        DeploymentArtifact d1;
        if(devCompleteRequire){
            d1  = createDeploymentArtifactDAO(DeploymentArtifactStatus.DEV_COMPLETE);
        }else {
            d1  = createDeploymentArtifactDAO(DeploymentArtifactStatus.NOT_NEEDED);
        }
        DeploymentArtifact d2 = createDeploymentArtifactDAO(DeploymentArtifactStatus.IN_DEV);
        return new ArrayList<>(Arrays.asList(d1, d2));
    }

}
