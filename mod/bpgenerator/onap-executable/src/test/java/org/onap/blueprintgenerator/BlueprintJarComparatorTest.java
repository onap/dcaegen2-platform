/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  Copyright (c) 2020  Nokia. All rights reserved.
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

package org.onap.blueprintgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: ONAP - Blueprint Generator ONAP Bueprint Jar Comparision with Previos version to make
 * sure Bps are not broken with new changes
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlueprintJarComparatorTest {

    private static final String ves = "ves.json";
    private static final String testImports = "testImports.yaml";
    private static final String previousJarVersion = "1.7.0-SNAPSHOT";
    private static final String latestJarVersion = "1.7.1-SNAPSHOT";
    private static final String previousVersion = "0_1";
    private static final String latestVersion = "1_0";
    private static final String latestJarPath = buildPathAsString("target");
    private static final String previousJarPath = buildPathAsString("src", "test", "resources", "archives");
    private static final String inputPath = buildPathAsString("src", "test", "resources", "componentspecs");
    private static final String outputPath = buildPathAsString("src", "test", "resources", "outputfiles");
    private static final String previousJar = "blueprint-generator-onap-executable-" + previousJarVersion + ".jar";
    private static final String latestJar = "blueprint-generator-onap-executable-" + latestJarVersion + ".jar";

    @BeforeClass
    @AfterClass
    public static void filesCleanup() throws IOException {
        FileUtils.deleteDirectory(new File(outputPath));
    }

    private static String buildPathAsString(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().toString() + File.separator;
    }

    @Test
    public void jarTestVeswithDmaapK8s() throws IOException, InterruptedException {
        String inputFileName = ves;
        String outputFileName = "dcae-ves-collector-dmaap-";
        String inputImportsFileName = testImports;

        Process process = runBpgenWithDmaap(inputFileName, outputFileName, inputImportsFileName, previousJarPath,
            previousJar, previousVersion);

        Process process1 = runBpgenWithDmaap(inputFileName, outputFileName, inputImportsFileName, latestJarPath,
            latestJar, latestVersion);

        process.waitFor();
        process1.waitFor();

        Assert.assertEquals(
            "The BluePrint files (" + outputFileName + ") for " + inputFileName + " with -m option don't match!",
            FileUtils.readFileToString(new File(outputPath + outputFileName + previousVersion + ".yaml"), "utf-8"),
            FileUtils.readFileToString(new File(outputPath + outputFileName + latestVersion + ".yaml"), "utf-8"));
    }

    @Test
    public void jarTestVeswithoutDmaapK8s() throws IOException, InterruptedException {
        String inputFileName = ves;
        String outputFileName = "dcae-ves-collector-";
        String inputImportsFileName = testImports;

        Process process = runBpgenWithoutDmaap(inputFileName, outputFileName, inputImportsFileName, previousJarPath,
            previousJar, previousVersion);

        Process process1 = runBpgenWithoutDmaap(inputFileName, outputFileName, inputImportsFileName, latestJarPath,
            latestJar, latestVersion);

        process.waitFor();
        process1.waitFor();

        Assert.assertEquals(
            "The BluePrint files (" + outputFileName + ") for " + inputFileName + " with -m option dont match!",
            FileUtils.readFileToString(new File(outputPath + outputFileName + previousVersion + ".yaml"), "utf-8"),
            FileUtils.readFileToString(new File(outputPath + outputFileName + latestVersion + ".yaml"), "utf-8"));
    }

    private Process runBpgenWithoutDmaap(String inputFileName, String outputFileName, String inputImportsFileName,
            String jarPath, String jarName, String outputFileVersion) throws IOException {
        String jarCommand = "java -jar " + jarPath + jarName + " app ONAP -i " + inputPath + inputFileName + " -p  " +
            outputPath + " -n " + outputFileName + outputFileVersion + " -t " + inputPath + inputImportsFileName;
        return Runtime.getRuntime().exec(jarCommand);
    }

    private Process runBpgenWithDmaap(String inputFileName, String outputFileName, String inputImportsFileName,
            String jarPath, String jarName, String outputFileVersion) throws IOException {
        String jarCommand = "java -jar " + jarPath + jarName + " app ONAP -i " + inputPath + inputFileName + " -p  " +
            outputPath + " -n " + outputFileName + outputFileVersion + " -t " + inputPath + inputImportsFileName
            + " -d";
        return Runtime.getRuntime().exec(jarCommand);
    }

}

