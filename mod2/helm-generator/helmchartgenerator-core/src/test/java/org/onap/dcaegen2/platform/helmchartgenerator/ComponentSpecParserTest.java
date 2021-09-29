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

package org.onap.dcaegen2.platform.helmchartgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ComponentSpecParser;
import org.onap.dcaegen2.platform.helmchartgenerator.models.chartinfo.ChartInfo;
import org.onap.dcaegen2.platform.helmchartgenerator.validation.ComponentSpecValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class ComponentSpecParserTest {

    ComponentSpecParser parser;

    @Mock
    ComponentSpecValidator validator;

    @BeforeEach
    void setUp() {
        parser = new ComponentSpecParser(validator, new Utils());
    }

    @Test
    void extractChartInfo() throws Exception{
        String specLocation = "src/test/input/specs/ves.json";
        String chartTemplateLocation = "src/test/input/blueprint";
        String specSchemaLocation = "";
        ChartInfo chartInfo = parser.extractChartInfo(specLocation, chartTemplateLocation, specSchemaLocation);

        assertMetadata(chartInfo);
        assertOuterKeyValues(chartInfo);
        assertApplicationConfigSection(chartInfo);
        assertReadinessCheck(chartInfo);
        assertApplicationEnv(chartInfo);
        assertService(chartInfo);
        assertPolicyInfo(chartInfo);
        assertCertificates(chartInfo);
        assertConfigMap(chartInfo);
        assertPostgres(chartInfo);
        assertSecrets(chartInfo);
    }

    private void assertApplicationConfigSection(ChartInfo chartInfo) {
        Map<String, Object> applicationConfig = (Map<String, Object>) chartInfo.getValues().get("applicationConfig");
        assertThat(applicationConfig.size()).isEqualTo(20);
    }

    private void assertOuterKeyValues(ChartInfo chartInfo) {
        Map<String, Object> outerKv = chartInfo.getValues();
        assertThat(outerKv.get("image")).isEqualTo("nexus3.onap.org:10001/onap/org.onap.dcaegen2.collectors.ves.vescollector:latest");
        assertThat(outerKv.get("logDirectory")).isEqualTo("/opt/app/VESCollector/logs/");
        assertThat(outerKv.get("certDirectory")).isEqualTo("/opt/app/dcae-certificate/");
        assertThat(outerKv.get("tlsServer")).isEqualTo(true);
    }

    private void assertMetadata(ChartInfo chartInfo) {
        assertThat(chartInfo.getMetadata().getName()).isEqualTo("dcae-ves-collector");
        assertThat(chartInfo.getMetadata().getVersion()).isEqualTo("1.8.0");
        assertThat(chartInfo.getMetadata().getDescription()).
                isEqualTo("Collector for receiving VES events through restful interface");
    }

    private void assertReadinessCheck(ChartInfo chartInfo) {
        Map<String, Object> readiness = (Map<String, Object>) chartInfo.getValues().get("readiness");
        assertThat(readiness.get("scheme")).isEqualTo("http");
        assertThat(readiness.get("path")).isEqualTo("/healthcheck");
        assertThat(readiness.get("periodSeconds")).isEqualTo(15);
        assertThat(readiness.get("port")).isEqualTo(8080);
        assertThat(readiness.get("initialDelaySeconds")).isEqualTo(5);
        assertThat(readiness.get("timeoutSeconds")).isEqualTo(1);
    }

    private void assertApplicationEnv(ChartInfo chartInfo) {
        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> applicationEnv = (Map<String, Object>) chartInfo.getValues().get("applicationEnv");
        Map<String, Object> PMSH_PG_USERNAME = oMapper.convertValue(applicationEnv.get("PMSH_PG_USERNAME"), Map.class);
        Map<String, Object> PMSH_PG_PASSWORD = oMapper.convertValue(applicationEnv.get("PMSH_PG_PASSWORD"), Map.class);

        assertThat(applicationEnv.get("PMSH_PG_URL")).isEqualTo("dcae-pmsh-pg-primary");
        assertThat(PMSH_PG_USERNAME.get("secretUid")).isEqualTo("pgUserCredsSecretUid");
        assertThat(PMSH_PG_USERNAME.get("key")).isEqualTo("login");
        assertThat(PMSH_PG_PASSWORD.get("secretUid")).isEqualTo("pgUserCredsSecretUid");
        assertThat(PMSH_PG_PASSWORD.get("key")).isEqualTo("password");
    }

    private void assertService(ChartInfo chartInfo) {
        Map<String, Object> service = (Map<String, Object>) chartInfo.getValues().get("service");
        List<Map> ports = new ArrayList<Map>();
        for(Object portsGroup : (ArrayList) service.get("ports")){
            ports.add((Map<String, Object>) portsGroup);
        }
        assertThat(service.get("type")).isEqualTo("NodePort");
        assertThat(service.get("name")).isEqualTo("dcae-ves-collector");
        assertThat(service.get("has_internal_only_ports")).isEqualTo(true);
        assertThat(ports.get(0).get("name")).isEqualTo("http");
        assertThat(ports.get(0).get("port")).isEqualTo(8443);
        assertThat(ports.get(0).get("plain_port")).isEqualTo(8080);
        assertThat(ports.get(0).get("port_protocol")).isEqualTo("http");
        assertThat(ports.get(0).get("nodePort")).isEqualTo(17);
        assertThat(ports.get(0).get("useNodePortExt")).isEqualTo(true);
        assertThat(ports.get(1).get("name")).isEqualTo("metrics");
        assertThat(ports.get(1).get("port")).isEqualTo(4444);
        assertThat(ports.get(1).get("internal_only")).isEqualTo(true);
    }

    private void assertPolicyInfo(ChartInfo chartInfo) {
        Map<String, Object> policyInfo = (Map<String, Object>) chartInfo.getValues().get("policies");
        assertThat(policyInfo.get("policyID")).isEqualTo("'[\"tca_policy_id_10\", \"tca_policy_id_11\"]'\n");
    }

    private void assertCertificates(ChartInfo chartInfo) {
        List certificates = (List) chartInfo.getValues().get("certificates");
        Map<String, Object> certificate = (Map<String, Object>) certificates.get(0);
        assertThat(certificate.get("mountPath")).isEqualTo("/opt/app/dcae-certificate/external");
        assertThat(certificate.get("commonName")).isEqualTo("dcae-ves-collector");
        assertThat(((List) certificate.get("dnsNames")).get(0)).isEqualTo("dcae-ves-collector");
        assertThat(((List) ((Map<String, Map>) certificate.get("keystore")).get("outputType")).get(0)).isEqualTo("jks");
        assertThat((((Map<String, Map>) certificate.get("keystore")).get("passwordSecretRef")).get("name")).isEqualTo("ves-collector-cmpv2-keystore-password");
        assertThat((((Map<String, Map>) certificate.get("keystore")).get("passwordSecretRef")).get("key")).isEqualTo("password");
        assertThat((((Map<String, Map>) certificate.get("keystore")).get("passwordSecretRef")).get("create")).isEqualTo(true);
    }

    private void assertConfigMap(ChartInfo chartInfo) {
        List externalVolumes = (List) chartInfo.getValues().get("externalVolumes");
        Map<String, Object> volume_one = (Map<String, Object>) externalVolumes.get(0);
        Map<String, Object> volume_two = (Map<String, Object>) externalVolumes.get(1);
        assertThat(volume_one.get("name")).isEqualTo("dcae-external-repo-configmap-schema-map");
        assertThat(volume_one.get("type")).isEqualTo("configMap");
        assertThat(volume_one.get("mountPath")).isEqualTo("/opt/app/VESCollector/etc/externalRepo/");
        assertThat(volume_one.get("optional")).isEqualTo(true);
    }

    private void assertPostgres(ChartInfo chartInfo) {
        Map<String, Object> postgres = (Map<String, Object>) chartInfo.getValues().get("postgres");
        assertThat(postgres.get("nameOverride")).isEqualTo("dcae-ves-collector-postgres");
        assertThat(((Map<String, Object>) postgres.get("service")).get("name")).isEqualTo("dcae-ves-collector-postgres");
        assertThat(((Map<String, Object>) postgres.get("service")).get("name2")).isEqualTo("dcae-ves-collector-pg-primary");
        assertThat(((Map<String, Object>) postgres.get("service")).get("name3")).isEqualTo("dcae-ves-collector-pg-replica");
        assertThat(((Map<String, Object>) ((Map<String, Object>) postgres.get("container")).get("name")).get("primary")).isEqualTo("dcae-ves-collector-pg-primary");
        assertThat(((Map<String, Object>) ((Map<String, Object>) postgres.get("container")).get("name")).get("replica")).isEqualTo("dcae-ves-collector-pg-replica");
        assertThat(((Map<String, Object>) postgres.get("persistence")).get("mountSubPath")).isEqualTo("dcae-ves-collector/data");
        assertThat(((Map<String, Object>) postgres.get("persistence")).get("mountInitPath")).isEqualTo("dcae-ves-collector");
        assertThat(((Map<String, Object>) postgres.get("config")).get("pgUserName")).isEqualTo("ves-collector");
        assertThat(((Map<String, Object>) postgres.get("config")).get("pgDatabase")).isEqualTo("ves-collector");
        assertThat(((Map<String, Object>) postgres.get("config")).get("pgUserExternalSecret")).isEqualTo("{{ include \"common.release\" . }}-ves-collector-pg-user-creds");
    }

    private void assertSecrets(ChartInfo chartInfo) {
        List<Object> secrets = (List<Object>) chartInfo.getValues().get("secrets");
        Map<String, Object> secret1 = (Map<String, Object>) secrets.get(0);
        assertThat(secret1.get("uid")).isEqualTo("pg-user-creds");
        assertThat(secret1.get("name")).isEqualTo("{{ include \"common.release\" . }}-ves-collector-pg-user-creds");
        assertThat(secret1.get("type")).isEqualTo("basicAuth");
        assertThat(secret1.get("externalSecret")).isEqualTo("{{ ternary \"\" (tpl (default \"\" .Values.postgres.config.pgUserExternalSecret) .) (hasSuffix \"ves-collector-pg-user-creds\" .Values.postgres.config.pgUserExternalSecret) }}");
        assertThat(secret1.get("login")).isEqualTo("{{ .Values.postgres.config.pgUserName }}");
        assertThat(secret1.get("password")).isEqualTo("{{ .Values.postgres.config.pgUserPassword }}");
        assertThat(secret1.get("passwordPolicy")).isEqualTo("generate");
    }
}