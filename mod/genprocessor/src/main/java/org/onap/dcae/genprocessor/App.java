/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
package org.onap.dcae.genprocessor;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.jar.Attributes;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
    static final Logger LOG = LoggerFactory.getLogger(App.class);

    // NOTE: For each new processor, need to: change jar command, change meta-inf
    private static String createClassName(CompSpec compSpec) {
        return String.format("org.onap.dcae.%s", compSpec.nameJavaClass);
    }

    /**
     * Does a series of DCAEProcessor specific verification checks on the generated
     * class.
     * 
     * @param cc
     * @return true if verification is successful
     */
    private static boolean verifyGen(CtClass cc) {
        DCAEProcessor processor;
        try {
            processor = (DCAEProcessor) cc.toClass().newInstance();
        } catch (InstantiationException | IllegalAccessException | CannotCompileException e) {
            LOG.error(e.toString(), e);
            return false;
        }
        java.lang.annotation.Annotation[] anns = processor.getClass().getAnnotations();
        LOG.info(String.format("#Annotations on class: %d", anns.length));

        for (java.lang.annotation.Annotation ann : anns) {
            if (ann.annotationType().getName().contains("Description")) {
                LOG.info(String.format("CapabilityDescription: %s", ((CapabilityDescription) ann).value()));
            } else if (ann.annotationType().getName().contains("Tags")) {
                LOG.info(String.format("Tags: %s", String.join(", ", ((Tags) ann).value())));
            }
        }

        LOG.info(String.format("Processor getters:\nname=%s\nversion=%s\nid=%s\nselfUrl=%s", processor.getName(),
                processor.getVersion(), processor.getComponentId(), processor.getComponentUrl()));

        LOG.info(String.format("#Property descriptors: %d", processor.getPropertyDescriptors().size()));

        if (processor.getPropertyDescriptors().size() > 0) {
            LOG.info(processor.getPropertyDescriptors().get(0).toString());
        }

        LOG.info(String.format("#Relationships: %d", processor.getRelationships().size()));

        // Actually do checks
        return true;
    }

    /**
     * Generates a new DCAEProcessor class given a Component object and writes the
     * class file to a specified directory.
     * 
     * @param directoryName where generated class files will get written
     * @param comp          Component object to generate new DCAEProcessor classes
     */
    public static void generateClassFile(String directoryName, Comp comp) {
        LOG.info("Generating classes");

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass base = pool.get(DCAEProcessor.class.getName());

            CtClass cc = pool.makeClass(createClassName(comp.compSpec));
            cc.setSuperclass(base);

            String[] tags = ProcessorBuilder.createTags(comp.compSpec);

            ProcessorBuilder.addAnnotationsProcessor(cc, comp.compSpec.description, tags);
            ProcessorBuilder.setComponentPropertyGetters(cc, comp);
            ProcessorBuilder.setProcessorPropertyDescriptors(cc, comp.compSpec);
            ProcessorBuilder.setProcessorRelationships(cc, comp.compSpec);

            if (verifyGen(cc)) {
                cc.writeFile(directoryName);
            }
        } catch (Exception e) {
            LOG.error("Uhoh", e);
        }
    }

    private static List<String> generateManifestMF(CompSpec compSpec) {
        return Arrays.asList("Manifest-Version: 1.0", String.format("Id: %s", compSpec.name),
                String.format("Version: %s", compSpec.version)
                // TODO: Group is hardcoded here. Should it be?
                , "Group: org.onap.dcae");
    }

    private static void writeManifestThing(File dirTarget, List<String> lines, String subDir, String fileName) {
        File dirManifest = new File(dirTarget, subDir);

        if (dirManifest.exists() || dirManifest.mkdirs()) {
            Path path = Paths.get(dirManifest.getAbsolutePath(), fileName);
            try {
                Files.write(path, lines, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Could not create Manifest directory");
        }
    }

    private static boolean copyProcessorClassFile(String classResourceName, File dirBuild) {
        File dirSandbox = new File(dirBuild, "org/onap/dcae/genprocessor");

        if (dirSandbox.exists() || dirSandbox.mkdir()) {
            try (InputStream asStream = App.class.getResourceAsStream(classResourceName)) {
                File dest = new File(dirSandbox, classResourceName);
                Files.copy(asStream, dest.toPath());
                return true;
            } catch (FileAlreadyExistsException e) {
                // Do nothing, class file already exists
            } catch (IOException e) {
                throw new RuntimeException(e);
            } 
        }

        return false;
    }

    private static File getDirectoryForJars(File dirWorking) {
        return new File(dirWorking, "nifi-jars");
    }

    private static boolean packageJar(File dirWorking, File dirBuild, String jarName) {
        LOG.info("Package into jar");

        try {
            File dirJars = getDirectoryForJars(dirWorking);

            if (dirJars.exists() || dirJars.mkdir()) {
                String argDashC = String.format("-C %s", dirBuild.getAbsolutePath());
                String cmd = String.join(" ", new String[] {
                    "jar cvfm"
                    , String.format("%s/%s.jar", dirJars.getAbsolutePath(), jarName)
                    , String.format("%s/META-INF/MANIFEST.MF", dirBuild.getAbsolutePath())
                    , argDashC, "org"
                    , argDashC, "org/onap/dcae/genprocessor"
                    , argDashC, "META-INF"
                });
                LOG.debug(String.format("Jar command: %s", cmd));

                if (Runtime.getRuntime().exec(cmd).waitFor() == 0) {
                    return true;
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error while creating jar", e);
        }

        return false;
    }

    private static boolean doesJarExist(File dirWorking, String jarName) {
        File dirJars = getDirectoryForJars(dirWorking);
        String[] jars = dirJars.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(jarName);
            }
        });
        return jars.length > 0;
    }

    /**
     * Looks for the MANIFEST.MF and extracts-to-print expected values (group, id,
     * version)
     * 
     * @param classLoader
     */
    private static void checkManifest(ClassLoader classLoader) {
        try {
            URL url = ((URLClassLoader) classLoader).findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());

            final Attributes attributes = manifest.getMainAttributes();
            final String group = attributes.getValue("Group");
            final String id = attributes.getValue("Id");
            final String version = attributes.getValue("Version");
            LOG.info(String.format("group=%s, id=%s, version=%s", group, id, version));
        } catch (IOException e) {
            throw new RuntimeException("Error while reading manifest", e);
        }
    }

    /**
     * Given a URL to a index.json file, fetches the file and generates a list of
     * URLs for DCAE jars that has Processors packaged.
     * 
     * @param indexDCAEJars
     * @return
     */
    public static List<URL> getDCAEJarsURLs(URI indexDCAEJars) {
        JsonFactory jf = new JsonFactory();
        ObjectMapper om = new ObjectMapper();

        try {
            List<Object> urls = om.readValue(jf.createParser(indexDCAEJars.toURL()), List.class);

            return urls.stream().map(u -> {
                try {
                    Map<String, Object> foo = (Map<String, Object>) u;
                    String name = (String) foo.get("name");
                    String url = String.format("%s/%s", indexDCAEJars.toString(), name);
                    return new URL(url);
                } catch (MalformedURLException e) {
                    // Hopefully you never come here...
                    return null;
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while getting jar URIs", e);
        }
    }

    /**
     * Loads all the Processor classes from the list of jar URLs and does a
     * validation check that prints to screen.
     * 
     * @param jarURLs
     */
    public static void loadFromJars(URL[] jarURLs) {
        URLClassLoader urlClassLoader = new URLClassLoader(jarURLs);
        checkManifest(urlClassLoader);

        final ServiceLoader<?> serviceLoader = ServiceLoader.load(Processor.class, urlClassLoader);

        for (final Object o : serviceLoader) {
            LOG.info(o.getClass().getName());
            DCAEProcessor proc = ((DCAEProcessor) o);
            proc.ping();
            LOG.info(String.format("%s: %s", proc.getName(), proc.getComponentUrl()));
        }

        // TODO: Can fetch the comp spec with the component url to do further
        // validation..
    }

    private static boolean init(File dirWorking) {
        File dirJars = getDirectoryForJars(dirWorking);

        if (dirJars.exists() || dirJars.mkdirs()) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        if (args.length == 0) {
            args = new String[] { "gen" };
            String sleepstr = System.getenv("GENPROC_SLEEP_SEC");
            long sleepdur = (sleepstr != null)? 1000 * Long.parseLong(sleepstr): 0;
            do {
                try {
                    main2(args);
                } catch (Exception e) {
                    LOG.error(e.toString(), e);
                }
                Thread.sleep(sleepdur);
            } while (sleepdur > 0);
            return;
        } else {
            main2(args);
        }
    }


    public static void main2(String[] args) throws URISyntaxException {
        String argsStr = String.join(", ", args);
        if (argsStr.contains("-h")) {
            LOG.info("Here are the possible args:");
            LOG.info("<gen> <load>");
            return;
        }

        boolean shouldGenerate = argsStr.contains("gen") ? true : false;
        boolean shouldLoad = argsStr.contains("load") ? true : false;
        boolean shouldPackage = argsStr.contains("package") ? true : false;

        // Config from env variables
        File dirWorking = new File(System.getenv("GENPROC_WORKING_DIR"));
        String hostOnboardingAPI = System.getenv("GENPROC_ONBOARDING_API_HOST");
        String urlToJarIndex = System.getenv("GENPROC_JAR_INDEX_URL");

        String[] paramsToPrint = new String[] {
            String.format("shouldGenerate=%b", shouldGenerate)
            , String.format("shouldLoad=%b", shouldLoad)
            , String.format("Working directory=%s", dirWorking.getName())
        };
        LOG.info(String.format("Genprocessor configuration: \n\t%s",
            String.join("\n\t", paramsToPrint)));

        init(dirWorking);

        // TODO: Need a way to load from file again

        if (shouldGenerate) {
            CompList compList = OnboardingAPIClient.getComponents(hostOnboardingAPI);
            LOG.info(String.format("Components retrieved: %d", compList.components.size()));

            for (CompList.CompShort cs : compList.components) {
                Comp comp = OnboardingAPIClient.getComponent(cs.getComponentUrlAsURI());
                LOG.info(String.format("Component spec: \n\t%s", comp.compSpec.toString("\n\t")));

                String jarName = Utils.formatNameForJar(comp.compSpec);

                if (doesJarExist(dirWorking, jarName)) {
                    LOG.info(String.format("Jar exists: %s.jar", jarName));
                    continue;
                }

                File dirBuild = new File(dirWorking, jarName);

                if (dirBuild.exists() || dirBuild.mkdir()) {
                    generateClassFile(dirBuild.getAbsolutePath(), comp);
                    writeManifestThing(dirBuild, generateManifestMF(comp.compSpec), "META-INF", "MANIFEST.MF");
                    writeManifestThing(dirBuild, Arrays.asList(createClassName(comp.compSpec)), "META-INF/services",
                            "org.apache.nifi.processor.Processor");
                    copyProcessorClassFile("DCAEProcessor.class", dirBuild);
                    packageJar(dirWorking, dirBuild, jarName);
                }
            }
        }

        if (shouldLoad) {
            List<URL> jarURLs;
            try {
                jarURLs = getDCAEJarsURLs(new URI(urlToJarIndex));
                LOG.info(jarURLs.toString());
            } catch (URISyntaxException e) {
                throw new RuntimeException("URL to index.json is bad");
            }

            for (URL jarURL : jarURLs) {
                loadFromJars(new URL[] {jarURL});
            }
        }
    }
}

