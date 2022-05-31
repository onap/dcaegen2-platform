/*
 * # ============LICENSE_START=======================================================
 * # Copyright (c) 2021-2022 AT&T Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.helmchartgenerator.Utils;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.Metadata;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.ComponentSpec;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Artifacts;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.HealthCheck;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Parameters;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Policy;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.PolicyInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Self;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Service;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.TlsInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.Volumes;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ComponentSpecValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ComponentSpecParser reads a componentspec file and collects useful data for helm chart generation.
 * @author Dhrumin Desai
 */
@Slf4j
@Component
public class ComponentSpecParser {

    @Autowired
    private ComponentSpecValidator specValidator;

    @Autowired
    private Utils utils;

    /**
     * Constructor for ComponentSpecParser
     * @param specValidator ComponentSpecValidator implementation
     * @param utils
     */
    public ComponentSpecParser(ComponentSpecValidator specValidator, Utils utils) {
        this.specValidator = specValidator;
        this.utils = utils;
    }

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
        ComponentSpec cs = utils.deserializeJsonFileToModel(specFileLocation, ComponentSpec.class);
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setMetadata(extractMetadata(cs.getSelf()));
        chartInfo.setValues(extractValues(cs, chartTemplateLocation));
        return chartInfo;
    }

    private Map<String, Object> extractValues(ComponentSpec cs, String chartTemplateLocation) {
        Map<String, Object> outerValues = new LinkedHashMap<>();
        if(cs.getAuxilary() != null && cs.getAuxilary().getTlsInfo() != null){
            utils.putIfNotNull(outerValues,"certDirectory", cs.getAuxilary().getTlsInfo().getCertDirectory());
            utils.putIfNotNull(outerValues, "tlsServer", cs.getAuxilary().getTlsInfo().getUseTls());
        }
        if(cs.getAuxilary() != null && cs.getAuxilary().getLogInfo() != null) {
            Map<String, Object> logPath = new LinkedHashMap<>();
            logPath.put("path", cs.getAuxilary().getLogInfo().get("log_directory"));
            outerValues.put("log", logPath);
        }
        if(imageUriExistsForFirstArtifact(cs)){
            utils.putIfNotNull(outerValues,"image", cs.getArtifacts()[0].getUri());
        }
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

    private boolean imageUriExistsForFirstArtifact(ComponentSpec cs) {
        final Artifacts[] artifacts = cs.getArtifacts();
        return artifacts != null && artifacts.length > 0 && artifacts[0].getUri() != null;
    }

    private void populateApplicationConfigSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> applicationConfig = new LinkedHashMap<>();
         Parameters[] parameters = cs.getParameters();
         for(Parameters param : parameters){
             if (Arrays.asList("streams_publishes", "streams_subscribes").contains(param.getName())){
                 applicationConfig.put(param.getName(), parseStringToMap(param.getValue()));
             }else
             {
                 applicationConfig.put(param.getName(), param.getValue());
             }
         }
        utils.putIfNotNull(outerValues,"applicationConfig", applicationConfig);
    }

    private Object parseStringToMap(Object value) {
        if (value instanceof String){
            try {
                return new ObjectMapper().readValue((String)value, Map.class);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                log.warn("could not parse streams_publishes / streams_subscribes. Default value will be used.");
            }
        }
        return value;
    }

    private void populateReadinessSection(Map<String, Object> outerValues, ComponentSpec cs) {

        if (!healthCheckExists(cs)) return;

        Map<String, Object> readiness = new LinkedHashMap<>();
        final HealthCheck healthcheck = cs.getAuxilary().getHealthcheck();
        utils.putIfNotNull(readiness, "initialDelaySeconds", healthcheck.getInitialDelaySeconds());

        if(healthcheck.getInterval() != null) {
            readiness.put("periodSeconds", getSeconds(healthcheck.getInterval(), "interval"));
        }
        if(healthcheck.getTimeout() != null) {
            readiness.put("timeoutSeconds", getSeconds(healthcheck.getTimeout(), "timeout"));
        }

        readiness.put("path", healthcheck.getEndpoint());
        readiness.put("scheme", healthcheck.getType());
        readiness.put("port", healthcheck.getPort());

        outerValues.put("readiness", readiness);
    }

    private int getSeconds(String value, String field) {
        int seconds = 0;
        try {
            seconds = Integer.parseInt(value.replaceAll("[^\\d.]", ""));
        }
        catch (NumberFormatException e){
            throw new RuntimeException(String.format("%s with %s is not given in a correct format", field, value));
        }
        return seconds;
    }

    private boolean healthCheckExists(ComponentSpec cs) {
        return cs.getAuxilary() != null &&
                cs.getAuxilary().getHealthcheck() !=  null;
    }

    private void populateApplicationEnvSection(Map<String, Object> outerValues, ComponentSpec cs){
        if(applicationEnvExists(cs)) {
            Object applicationEnv = cs.getAuxilary().getHelm().getApplicationEnv();
            utils.putIfNotNull(outerValues,"applicationEnv", applicationEnv);
        }
    }

    private boolean applicationEnvExists(ComponentSpec cs) {
        return cs.getAuxilary() != null &&
                cs.getAuxilary().getHelm() !=  null &&
                cs.getAuxilary().getHelm().getApplicationEnv() != null;
    }

    private void populateServiceSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if (!serviceExists(cs)) return;

        Map<String, Object> service = new LinkedHashMap<>();
        final Service serviceFromSpec = cs.getAuxilary().getHelm().getService();

        if(serviceFromSpec.getPorts() != null){
            List<Object> ports = mapServicePorts(serviceFromSpec.getPorts());
            service.put("ports", ports);
            utils.putIfNotNull(service, "type", serviceFromSpec.getType());
        }
        utils.putIfNotNull(service,"name", serviceFromSpec.getName());
        utils.putIfNotNull(service,"has_internal_only_ports", serviceFromSpec.getHasInternalOnlyPorts());
        outerValues.put("service", service);
    }

    private boolean serviceExists(ComponentSpec cs) {
        return cs.getAuxilary() != null &&
                cs.getAuxilary().getHelm() !=  null &&
                cs.getAuxilary().getHelm().getService() !=  null;
    }

    private List<Object> mapServicePorts(Object[] ports) {
        List<Object> portsList = new ArrayList<>();
        Collections.addAll(portsList, ports);
        return portsList;
    }

    private void populatePoliciesSection(Map<String, Object> outerValues, ComponentSpec cs) {
        Map<String, Object> policies = new LinkedHashMap<>();
        final PolicyInfo policyInfo = cs.getPolicyInfo();
        if(policyInfo != null && policyInfo.getPolicy() != null) {
            List<String> policyList = new ArrayList<>();
            for (Policy policyItem : policyInfo.getPolicy()) {
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
        String componentName = getComponentNameWithOmitFirstWordAndTrimHyphens(cs);
        outerValues.put("useCmpv2Certificates", false);
        if(externalTlsExists(tlsInfo)) {
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
            utils.putIfNotNull(certificate,"commonName", cs.getSelf().getName());
            utils.putIfNotNull(certificate,"dnsNames", List.of(cs.getSelf().getName()));
            certificate.put("keystore", keystore);
            outerValues.put("certificates", List.of(certificate));
            outerValues.put("useCmpv2Certificates", true);
        }
    }

    private String getComponentNameWithOmitFirstWordAndTrimHyphens(ComponentSpec cs) {
        return cs.getSelf().getName().substring(cs.getSelf().getName().indexOf("-") + 1).replaceAll("-","");
    }

    private boolean externalTlsExists(TlsInfo tlsInfo) {
        return tlsInfo != null && tlsInfo.getUseExternalTls() != null && tlsInfo.getUseExternalTls();
    }

    private void checkCertificateYamlExists(String chartTemplateLocation) {
        Path certificateYaml = Paths.get(chartTemplateLocation, "addons/templates/certificates.yaml");
        if(!Files.exists(certificateYaml)) {
            throw new RuntimeException("certificates.yaml not found under templates directory in addons");
        }
    }

    private void populateExternalVolumesSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if(cs.getAuxilary().getVolumes() != null) {
            List<Object> externalVolumes = new ArrayList<>();
            Volumes[] volumes = cs.getAuxilary().getVolumes();
            for (Volumes volume : volumes) {
                if(volume.getHost() == null) {
                    Map<String, Object> tempVolume = new LinkedHashMap<>();
                    tempVolume.put("name", volume.getConfigVolume().getName());
                    tempVolume.put("type", "configMap");
                    tempVolume.put("mountPath", volume.getContainer().getBind());
                    tempVolume.put("optional", true);
                    externalVolumes.add(tempVolume);
                }
            }
            if(!externalVolumes.isEmpty()) {
                outerValues.put("externalVolumes", externalVolumes);
            }
        }
    }

    private void populatePostgresSection(Map<String, Object> outerValues, ComponentSpec cs) {
        if(cs.getAuxilary().getDatabases() != null) {
            String componentFullName = cs.getSelf().getName();
            String component = getComponentNameWithOmitFirstWordAndTrimHyphens(cs);
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
            String component = getComponentNameWithOmitFirstWordAndTrimHyphens(cs);
            List<Object> secrets = new ArrayList<>();
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
