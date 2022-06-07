/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Huawei. All rights reserved.
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

package sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.net.URISyntaxException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import org.onap.dcae.genprocessor.App;
import org.onap.dcae.genprocessor.CompSpec;
import org.onap.dcae.genprocessor.DCAEProcessor;
import org.onap.dcae.genprocessor.OnboardingAPIClient;
import org.onap.dcae.genprocessor.Utils;
import org.onap.dcae.genprocessor.CompList;
import org.onap.dcae.genprocessor.ProcessorBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Unit test for simple App.
 */
public class AppTest {
    static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

    @Rule
    public final EnvironmentVariables evars = new EnvironmentVariables();

    @Rule
    public TemporaryFolder tfolder = new TemporaryFolder();


    @Test
    public void testUtils() {
        new Utils();
        assertEquals(Utils.formatNameForJavaClass("part1.a-bee"), "Part1ABee");
        HashMap<String, String> mx = new HashMap<>();
        mx.put("name", "SomeJar");
        mx.put("version", "2.0");
        mx.put("description", "desc");
        CompSpec cs = new CompSpec();
        cs.unpackSelf(mx);
        assertEquals(Utils.formatNameForJar(cs), "SomeJar-2.0");
        try {
            CompSpec.loadComponentSpec(new File("sandbox/temp.txt"));
        } catch (RuntimeException e) {
            // expected case
            return;
        }
        fail("Exception is not thrown");
    }

    @Test
    public void testGetNameForJavaClass() {
        CompList.CompShort compShort = new CompList.CompShort();
        compShort.name = "test";
        compShort.getNameForJavaClass();
        compShort.componentUrl = "6:invalidURI";
        try {
            compShort.getComponentUrlAsURI();
        } catch (RuntimeException e) {
            // expected case
            return;
        }
        fail("Exception is not thrwon");
    }


    @Test
    public void testDcaeProcessor() throws ProcessException {
        DCAEProcessor px = new DCAEProcessor() {
            public String getName() {
                return (null);
            }

            public String getVersion() {
                return (null);
            }

            public String getComponentId() {
                return (null);
            }

            public String getComponentUrl() {
                return (null);
            }

            protected List<PropertyDescriptor> buildSupportedPropertyDescriptors() {
                return (new LinkedList<>());
            }

            protected Set<Relationship> buildRelationships() {
                return (new HashSet<>());
            }

            public DCAEProcessor xxx() {
                getSupportedPropertyDescriptors();
                getSupportedPropertyDescriptors();
                return (this);
            }
            }.xxx();
        px.ping();
        px.onTrigger((ProcessContext)null, (ProcessSession)null);
        px.getRelationships();
        px.getRelationships();
    }


    @Test
    public void testPaths() throws InterruptedException, IOException, URISyntaxException {
        /* some trivial cases */
        new OnboardingAPIClient();
        try {
            OnboardingAPIClient.getComponents("6:invalidURI");
        } catch (OnboardingAPIClient.OnboardingAPIClientError oace) {
            // expected case
        }
        try {
            OnboardingAPIClient.getComponent(null);
        } catch (OnboardingAPIClient.OnboardingAPIClientError oace) {
            // expected case
        }
        /* background one shot failure cases */
        evars.clear("GENPROC_SLEEP_SEC");
        App.main(new String[0]);
        evars.set("GENPROC_SLEEP_SEC", "0");
        String wdir = tfolder.newFolder("work").getPath();
        evars.set("GENPROC_WORKING_DIR", wdir);
        String onboardingdir = tfolder.newFolder("onboarding").getPath();
        evars.set("GENPROC_ONBOARDING_API_HOST", (new File(onboardingdir)).toURI().toURL().toString());
        String compfile = onboardingdir + "/compone";
        try (Writer w = new FileWriter(compfile)) {
            w.write("{ \"id\": \"1\", \"spec\": { \"name\": \"one-collector\","
                + " \"version\": \"1.0.0\", \"description\": \"desc\","
                + " \"parameters\": [{\"name\": \"p1\", \"value\": \"v1\","
                + " \"description\": \"d1\"}], \"streams\":"
                + " {\"publishes\":[{\"format\": \"f1\", \"version\": \"v1\","
                + " \"type\": \"t1\", \"config_key\": \"ck1\"}],"
                + " \"subscribes\":[{\"format\": \"f2\", \"version\": \"v2\","
                + " \"type\": \"t2\", \"config_key\": \"ck2\"}]}},"
                + " \"selfUrl\": \"file:" + compfile + "\"}");
        }
        try (Writer w = new FileWriter(onboardingdir + "/components")) {
            w.write("{\"components\": [{\"id\": \"1\", \"name\": \"one\","
                + " \"version\": \"1.0.0\", \"description\": \"desc\","
                + " \"componentType\": \"apple\", \"owner\": \"John Doe\","
                + " \"componentUrl\": \"file:" + compfile + "\","
                + " \"whenAdded\": \"never\" }]}");
        }
        String indexfile = tfolder.newFile("index").getPath();
        try (Writer w = new FileWriter(indexfile)) {
            w.write("[]");
        }
        evars.set("GENPROC_JAR_INDEX_URL", (new File(indexfile)).toURI().toURL().toString());
        App.main(new String[0]);
        /* help case */
        App.main(new String[] { "-h" });
        /* load case */
        App.main(new String[] { "load" });
        /* gen case */
        App.main(new String[] { "gen" });

        URL[] jarURLs = new URL[1];
        try {
            App.loadFromJars(jarURLs);
        } catch (NullPointerException e) {
            // expected case
            return;
        }
        fail("Exception is not thrown");
    }

    @Test
    public void testAddMethod() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass base = pool.get(DCAEProcessor.class.getName());

            CtClass cc = pool.makeClass(String.format("org.onap.dcae.%s", DCAEProcessor.class));
            cc.setSuperclass(base);

            ProcessorBuilder.addMethod(cc, "test");
        } catch (Exception e) {
            // expected case
            return;
        }
        fail("Exception is not thrown");
    }
}

