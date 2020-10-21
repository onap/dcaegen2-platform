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


package org.onap.blueprintgenerator.test;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runners.MethodSorters;
import org.onap.blueprintgenerator.test.BlueprintGeneratorTests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: ONAP - Blueprint Generator
 * ONAP Bueprint Jar Comparision with Previos version to make sure Bps are not broken with new changes
 */


@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlueprintJarComparatorTest extends BlueprintGeneratorTests {

    private String previousJarVersion = "0.1";
    private String latestJarVersion = "1.0";
    private String previousVersion = "0_1";
    private String latestVersion = "1_0";
    private String latestJarPath = Paths.get("target").toAbsolutePath().toString() + "\\";
    private String previousJarPath = Paths.get("src", "test", "resources", "archives").toAbsolutePath().toString() + "\\";
    private String inputPath = Paths.get("src", "test", "resources", "componentspecs").toAbsolutePath().toString() + "\\";
    private String inputPolicyPath = Paths.get("src", "test", "resources", "policyjson").toAbsolutePath().toString() + "\\";
    private String outputPath = Paths.get("src", "test", "resources", "outputfiles").toAbsolutePath().toString() + "\\";
    private String previousJar = "onap-blueprint-generator-" + previousJarVersion + ".jar";
    private String latestJar = "onap-blueprint-generator-" + latestJarVersion + ".jar";

    @Test
    @Order(value=1)
    public void filesCleanup() throws IOException {
        FileUtils.deleteDirectory(new File(outputPath));
    }

    @Test
    public void jarTestVeswithDmaapK8s() throws IOException, InterruptedException {
        String inputFileName = ves;
        String outputFileName = "dcae-ves-collector-dmaap-";
        String inputImportsFileName = testImports;

        String previousJarCommand = "java -jar " + previousJarPath + previousJar + " app ONAP -i " + inputPath + inputFileName + " -p  " + outputPath +
                " -n " + outputFileName + previousVersion + " -t " + inputPath  + inputImportsFileName  + " -d";
        Runtime.getRuntime().exec(previousJarCommand);

        String latestJarCommand = "java -jar " + latestJarPath + latestJar + " app ONAP -i " + inputPath + inputFileName + " -p  " + outputPath +
                " -n " + outputFileName + latestVersion + " -t " + inputPath  + inputImportsFileName  + " -d";
        Runtime.getRuntime().exec(latestJarCommand);

        Thread.sleep(8000);

        Assert.assertEquals("The BluePrint files (" + outputFileName + ") for " + inputFileName + " with -m option don't match!",
                FileUtils.readFileToString(new File(outputPath + outputFileName + previousVersion + ".yaml"), "utf-8"),
                FileUtils.readFileToString(new File(outputPath + outputFileName + latestVersion + ".yaml"), "utf-8"));
    }

    @Test
    public void jarTestVeswithoutDmaapK8s() throws IOException, InterruptedException {
        String inputFileName = ves;
        String outputFileName = "dcae-ves-collector-";
        String inputImportsFileName = testImports;

        String previousJarCommand = "java -jar " + previousJarPath + previousJar +  " app ONAP -i "  + inputPath + inputFileName + " -p  " + outputPath +
                " -n " + outputFileName + previousVersion  + " -t " + inputPath  + inputImportsFileName ;
        Runtime.getRuntime().exec(previousJarCommand);

        String latestJarCommand = "java -jar " + latestJarPath + latestJar + " app ONAP -i "  + inputPath + inputFileName + " -p  " + outputPath +
                " -n " + outputFileName + latestVersion  + " -t " + inputPath  + inputImportsFileName ;
        Runtime.getRuntime().exec(latestJarCommand);

        Thread.sleep(8000);

        Assert.assertEquals("The BluePrint files (" + outputFileName + ") for " + inputFileName + " with -m option dont match!",
                FileUtils.readFileToString(new File(outputPath + outputFileName + previousVersion + ".yaml"), "utf-8"),
                FileUtils.readFileToString(new File(outputPath + outputFileName + latestVersion + ".yaml"), "utf-8"));
    }


}

