/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2020 Nokia. All rights reserved.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.dcae.runtime.core.blueprint_creator.BlueprintCreatorOnap;

public class TestFlowGraphParser extends BpGenTestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testFlowGraphParser() throws IOException {
        try {
            File importsfile = folder.newFile("imports.yaml");
            try (Writer w = new FileWriter(importsfile)) {
                w.write(
                    "imports:\n" +
                        " - 'http://www.getcloudify.org/spec/cloudify/3.4/types.yaml'\n" +
                        " - 'https://nexus.onap.org/service/local/repositories/raw/content/org.onap.dcaegen2.platform.plugins/R4/k8splugin/1.4.5/k8splugin_types.yaml'\n"
                        +
                        " - 'https://nexus.onap.org/service/local/repositories/raw/content/org.onap.dcaegen2.platform.plugins/R4/dcaepolicyplugin/2.3.0/dcaepolicyplugin_types.yaml'\n"
                );
            }
            FlowGraph<Node, Edge> mainFlowGraph = new FlowGraph<>("1234", "nifi-main", true, "mock graph");
            mainFlowGraph.addNode(new Node("dummy_id", "dummy_name", "dummy_compspec"));
            BlueprintCreatorOnap bcod = new BlueprintCreatorOnap(componentSpecService, blueprintCreatorService,
                blueprintService, fixesService);
            bcod.setTopicUrl("u.r.l");
            bcod.setImportFilePath(importsfile.getAbsolutePath());
            FlowGraphParser flowGraphParser = new FlowGraphParser(bcod);
            flowGraphParser.parse(mainFlowGraph);
            String compspec = Helper.loadFileContent("src/test/data/compspecs/componentSpec_hello_world.json");
            mainFlowGraph.addNode(new Node("comp5678", "hello-world-2", compspec));
            mainFlowGraph.addNode(new Node("comp1234", "hello-world-1", compspec));
            mainFlowGraph.addEdge(
                flowGraphParser.getNodeFromId("comp1234"),
                flowGraphParser.getNodeFromId("comp5678"),
                new Edge(
                    new EdgeLocation("comp1234", "DCAE-HELLO-WORLD-PUB-MR"),
                    new EdgeLocation("comp5678", "DCAE-HELLO-WORLD-SUB-MR"),
                    new EdgeMetadata("sample_topic_1", "json", "MR")));
            flowGraphParser.createAndProcessBlueprints();
        } catch (RuntimeException rte) {
            rte.printStackTrace();
            throw rte;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
