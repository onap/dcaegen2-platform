/*
 * # ============LICENSE_START=======================================================
 * # Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
 * # ================================================================================
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * # ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder;

import lombok.Setter;
import org.onap.dcaegen2.platform.helmchartgenerator.Utils;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.Metadata;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.ComponentSpec;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.*;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ComponentSpecValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * ComponentSpecParser reads a componentspec file and collects useful data for helm chart generation.
 * @author Dhrumin Desai
 */
@Component
@Setter
public class ComponentSpecParser {

    @Autowired
    Yaml yaml;

    @Autowired
    private ComponentSpecValidator specValidator;

    /**
     *
     * @param specFileLocation location of the application specification json file
     * @param chartTemplateLocation location of the base helm chart template
     * @param specSchemaLocation location of the specification json schema file to validate the application spec
     * @return ChartInfo object populated with key-values
     * @throws Exception
     */
    public ChartInfo extractChartInfo(String specFileLocation, String chartTemplateLocation, String specSchemaLocation) throws Exception {
        specValidator.validateSpecFile(specFileLocation, specSchemaLocation);
        ComponentSpec cs = Utils.deserializeJsonFileToModel(specFileLocation, ComponentSpec.class);
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setMetadata(extractMetadata(cs.getSelf()));
        chartInfo.setValues(extractValues(cs, chartTemplateLocation));
        return chartInfo;
    }

    private Map<String, Object> extractValues(ComponentSpec cs, String chartTemplateLocation) {
        Map<String, Object> outerValues = new LinkedHashMap<>();
        if(cs.getAuxilary().getTlsInfo() != null){
            Utils.putIfNotNull(outerValues,"certDirectory", cs.getAuxilary().getTlsInfo().getCertDirectory());
            Utils.putIfNotNull(outerValues, "tlsServer", cs.getAuxilary().getTlsInfo().getUseTls());
        }
        if(cs.getAuxilary().getLogInfo() != null) {
            Utils.putIfNotNull(outerValues,"logDirectory", cs.getAuxilary().getLogInfo().get("log_directory"));
        }
        Utils.putIfNotNull(outerValues,"image", cs.getArtifacts()[0].getUri());
        populateApplicationConfigSection(outerValues, cs);
        populateReadinessSection(outerValues, cs);
        populateApplicationEnvSection(outerValues, cs);
        populateServiceSection(outerValues, cs);
        populateCertificatesSection(outerValues, cs, chartTemplateLocation);
        populatePoliciesSection(outerValues, cs);
        populateExternalVolumesSection(outerValues, cs);
        populatePostgresSection(outerValues, cs);
        populateSecretsSection(outerValues, cs);
        return outerValues;
    }

    private void populateApplicationConfigSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> applicationConfig = new LinkedHashMap<>();
         Parameters[] parameters = cs.getParameters();
         for(Parameters param : parameters){
            applicationConfig.put(param.getName(), param.getValue());
         }
         Utils.putIfNotNull(outerValues,"applicationConfig", applicationConfig);
    }

    private void populateReadinessSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> readiness = new LinkedHashMap<>();
        Utils.putIfNotNull(readiness, "initialDelaySeconds", cs.getAuxilary().getHealthcheck().getInitialDelaySeconds());
        if(cs.getAuxilary().getHealthcheck().getInterval() != null) {
            readiness.put("periodSeconds", Integer.parseInt(cs.getAuxilary().getHealthcheck().getInterval().replaceAll("[^\\d.]", "")));
        }
        if(cs.getAuxilary().getHealthcheck().getTimeout() != null) {
            readiness.put("timeoutSeconds", Integer.parseInt(cs.getAuxilary().getHealthcheck().getTimeout().replaceAll("[^\\d.]", "")));
        }
        readiness.put("path", cs.getAuxilary().getHealthcheck().getEndpoint());
        readiness.put("scheme", cs.getAuxilary().getHealthcheck().getType());
        readiness.put("port", cs.getAuxilary().getHealthcheck().getPort());
        outerValues.put("readiness", readiness);
    }

    private void populateApplicationEnvSection(Map<String, Object> outerValues, ComponentSpec cs){
        if(cs.getAuxilary().getHelm().getApplicationEnv() != null) {
            Object applicationEnv = cs.getAuxilary().getHelm().getApplicationEnv();
            Utils.putIfNotNull(outerValues,"applicationEnv", applicationEnv);
        }
    }

    private void populateServiceSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> service = new LinkedHashMap<>();
        List<Object> ports = mapServicePorts(cs.getAuxilary().getHelm().getService().getPorts());
        service.put("type", cs.getAuxilary().getHelm().getService().getType());
        service.put("ports", ports);
        Utils.putIfNotNull(service,"name", cs.getAuxilary().getHelm().getService().getName());
        Utils.putIfNotNull(service,"has_internal_only_ports", cs.getAuxilary().getHelm().getService().getHas_internal_only_ports());
        outerValues.put("service", service);
    }

    private List<Object> mapServicePorts(Object[] ports) {
        List<Object> portsList = new ArrayList<>();
        Collections.addAll(portsList, ports);
        return portsList;
    }

    private void populatePoliciesSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> policies = new LinkedHashMap<>();
        if(cs.getPolicyInfo() != null) {
            List<String> policyList = new ArrayList();
            for (Policy policyItem : cs.getPolicyInfo().getPolicy()) {
                policyList.add('"' + policyItem.getPolicyID() + '"');
            }
            policies.put("policyRelease", "onap");
            policies.put("duration", 300);
            policies.put("policyID", "'" + policyList.toString() + "'\n");
            outerValues.put("policies", policies);
        }
    }

    private void populateCertificatesSection(Map<String, Object> outerValues, ComponentSpec cs, String chartTemplateLocation) {
        Map<String, Object> certificate = new LinkedHashMap<>();
        Map<String, Object> keystore = new LinkedHashMap<>();
        Map<String, Object> passwordsSecretRef = new LinkedHashMap<>();
        TlsInfo tlsInfo = cs.getAuxilary().getTlsInfo();
        String componentName = cs.getSelf().getName().substring(cs.getSelf().getName().indexOf("-") + 1);
        if(tlsInfo != null && tlsInfo.getUseExternalTls() != null && tlsInfo.getUseExternalTls()) {
            String mountPath = tlsInfo.getCertDirectory();
            if(tlsInfo.getUseExternalTls() != null && tlsInfo.getUseExternalTls()) {
                checkCertificateYamlExists(chartTemplateLocation);
                mountPath += "external";
            }
            passwordsSecretRef.put("name", componentName + "-cmpv2-keystore-password");
            passwordsSecretRef.put("key", "password");
            passwordsSecretRef.put("create", true);
            keystore.put("outputType", List.of("jks"));
            keystore.put("passwordSecretRef", passwordsSecretRef);
            certificate.put("mountPath", mountPath);
            Utils.putIfNotNull(certificate,"commonName", cs.getSelf().getName());
            Utils.putIfNotNull(certificate,"dnsNames", List.of(cs.getSelf().getName()));
            certificate.put("keystore", keystore);
            outerValues.put("certificates", List.of(certificate));
        }
    }

    private void checkCertificateYamlExists(String chartTemplateLocation) {
        Path certificateYaml = Paths.get(chartTemplateLocation, "addons/templates/certificates.yaml");
        if(!Files.exists(certificateYaml)) {
            throw new RuntimeException("certificates.yaml not found under templates directory in addons");
        }
    }

    private void populateExternalVolumesSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if(cs.getAuxilary().getVolumes() != null) {
            List externalVolumes = new ArrayList();
            Volumes[] volumes = cs.getAuxilary().getVolumes();
            for (Volumes volume : volumes) {
                if(volume.getHost() == null) {
                    Map tempVolume = new LinkedHashMap();
                    tempVolume.put("name", volume.getConfigVolume().getName());
                    tempVolume.put("type", "configMap");
                    tempVolume.put("mountPath", volume.getContainer().getBind());
                    tempVolume.put("optional", true);
                    externalVolumes.add(tempVolume);
                }
            }
            if(externalVolumes.size() > 0) {
                outerValues.put("externalVolumes", externalVolumes);
            }
        }
    }

    private void populatePostgresSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if(cs.getAuxilary().getDatabases() != null) {
            String componentFullName = cs.getSelf().getName();
            String component = componentFullName.substring(componentFullName.indexOf("-") + 1);
            Map<String, Object> postgres = new LinkedHashMap<>();
            Map<String, Object> service = new LinkedHashMap<>();
            Map<String, Object> container = new LinkedHashMap<>();
            Map<String, Object> name = new LinkedHashMap<>();
            Map<String, Object> persistence = new LinkedHashMap<>();
            Map<String, Object> config = new LinkedHashMap<>();
            service.put("name", componentFullName + "-postgres");
            service.put("name2", componentFullName + "-pg-primary");
            service.put("name3", componentFullName + "-pg-replica");
            name.put("primary", componentFullName + "-pg-primary");
            name.put("replica", componentFullName + "-pg-replica");
            container.put("name", name);
            persistence.put("mountSubPath", componentFullName + "/data");
            persistence.put("mountInitPath", componentFullName);
            config.put("pgUserName", component);
            config.put("pgDatabase", component);
            config.put("pgUserExternalSecret", "{{ include \"common.release\" . }}-" + component + "-pg-user-creds");

            postgres.put("enabled", true);
            postgres.put("nameOverride", componentFullName + "-postgres");
            postgres.put("service", service);
            postgres.put("container", container);
            postgres.put("persistence", persistence);
            postgres.put("config", config);
            outerValues.put("postgres", postgres);
        }
    }

    private void populateSecretsSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if(cs.getAuxilary().getDatabases() != null) {
            String component = cs.getSelf().getName().substring(cs.getSelf().getName().indexOf("-") + 1);
            List secrets = new ArrayList();
            Map<String, Object> secret = new LinkedHashMap<>();
            secret.put("uid", "pg-user-creds");
            secret.put("name", "{{ include \"common.release\" . }}-" + component + "-pg-user-creds");
            secret.put("type", "basicAuth");
            secret.put("externalSecret", "{{ ternary \"\" (tpl (default \"\" .Values.postgres.config.pgUserExternalSecret) .) (hasSuffix \"" + component + "-pg-user-creds\" .Values.postgres.config.pgUserExternalSecret) }}");
            secret.put("login", "{{ .Values.postgres.config.pgUserName }}");
            secret.put("password", "{{ .Values.postgres.config.pgUserPassword }}");
            secret.put("passwordPolicy", "generate");
            secrets.add(secret);
            outerValues.put("secrets", secrets);
        }
    }

    private Metadata extractMetadata(Self self) {
        Metadata metadata = new Metadata();
        metadata.setName(self.getName());
        metadata.setDescription(self.getDescription());
        metadata.setVersion(self.getVersion());
        return metadata;
    }
}
