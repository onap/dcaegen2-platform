/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.apache.nifi.nar;

import org.apache.nifi.bundle.Bundle;
import org.apache.nifi.bundle.BundleCoordinate;
import org.apache.nifi.bundle.BundleDetails;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.jar.Attributes;


/**
 * Class responsible for loading JARs for DCAEProcessors into Nifi
 */
public class DCAEClassLoaders {

    public static class DCAEClassLoadersError extends RuntimeException {
        public DCAEClassLoadersError(Throwable e) {
            super("Error while using DCAEClassLoaders", e);
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

    private static BundleDetails createBundleDetails(URLClassLoader classLoader) {
        try {
            URL url = classLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());

            final Attributes attributes = manifest.getMainAttributes();

            final BundleDetails.Builder builder = new BundleDetails.Builder();
            // NOTE: Working directory cannot be null so set it to some bogus dir
            // because we aren't really using this. Or maybe should create our own
            // working directory
            builder.workingDir(new File("/tmp"));

            final String group = attributes.getValue("Group");
            final String id = attributes.getValue("Id");
            final String version = attributes.getValue("Version");
            builder.coordinate(new BundleCoordinate(group, id, version));

            return builder.build();
        } catch (IOException e) {
            throw new DCAEClassLoadersError(e);
        }
    }

    /**
     * From a list of URLs to remote JARs where the JARs contain DCAEProcessor classes,
     * create a bundle for each JAR. You will never get a partial list of bundles.
     * 
     * @param jarURLs
     * @return
     */
    public static Set<Bundle> createDCAEBundles(List<URL> jarURLs) {
        Set<Bundle> bundles = new HashSet<>();

        for (URL jarURL : jarURLs) {
            URLClassLoader classLoader = new URLClassLoader(new URL[] {jarURL});
            Bundle bundle = new Bundle(createBundleDetails(classLoader), classLoader);
            bundles.add(bundle);
        }

        return bundles;
     }

}
