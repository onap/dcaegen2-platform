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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * HelmClient implementation which uses helm command installed in the runtime environment.
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class HelmClientImpl implements HelmClient {

    private final String repoUrl;

    private final String username;

    private final String password;

    public HelmClientImpl(@Value("${chartmuseum.baseurl}")String repoUrl,
                          @Value("${chartmuseum.auth.basic.username}")String username,
                          @Value("${chartmuseum.auth.basic.password}")String password) {
        this.repoUrl = repoUrl;
        this.username = username;
        this.password = password;
        try{
            repoAdd(repoUrl, username,password);
        }catch (Exception e){
            log.warn("Could not add helm repo.");
        }
    }

    private void repoAdd(String repoUrl, String username, String password) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command("helm", "repo", "add", "local",  repoUrl,
                        "--username", username, "--password", password);
        runProcess(builder, "repoAdd");
    }

    /**
     * performs <code>helm lint</code> operation
     * @param chartLocation helm chart location
     */
    @Override
    public void lint(File chartLocation) {
        helmDepUp(chartLocation.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("helm", "lint", chartLocation.getAbsolutePath());
        runProcess(builder, "lint");
    }

    /**
     *  performs <code>helm package</code> operation
     * @param chartLocation helm chart location
     * @param outputLocation location to store the generated helm package
     * @return generated helm tgz file
     */
    @Override
    public File packageChart(File chartLocation, String outputLocation) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(System.getProperty("user.dir")));
        builder.command("helm", "package", "-d", outputLocation, chartLocation.getAbsolutePath());
        return runProcess(builder, "package");
    }

    private void helmDepUp(String chartLocation) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command("helm", "dep", "up",chartLocation);
        runProcess(builder, "helmDepUp");
    }

    private File runProcess(ProcessBuilder builder, String command) {
        log.info("running: " + String.join(" ",builder.command()));
        Process process = null;
        String chartPath = "";
        try {
            process = builder.start();
            if(command.equals("package")) {
                chartPath = printPackagingProcessOutput(process);
            }
            else {
                printProcessOutput(process);
            }
            assertExitCode(process);
        }  catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error occurred while running helm command.");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("execution interrupted");
        }
        return new File(chartPath);
    }

    private void printProcessOutput(Process process) throws IOException {
        final InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        reader.lines().forEach(log::info);
        inputStream.close();
    }

    private String printPackagingProcessOutput(Process process) throws IOException {
        String helmChartPath = "";
        final InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            if (line.contains("Successfully packaged chart and saved it to: ")){
                helmChartPath = line.split("Successfully packaged chart and saved it to: ")[1];
            }
            log.info(line);
        }
        inputStream.close();
        if(helmChartPath.isEmpty()){
            throw new RuntimeException("Could not generate the chart.");
        }
        return helmChartPath;
    }

    private void assertExitCode(Process process) throws InterruptedException {
        int exitCode = 0;
        exitCode = process.waitFor();
        process.destroy();
        if (exitCode != 0){
            throw new RuntimeException("Error occurred while running helm command.");
        }
    }
}
