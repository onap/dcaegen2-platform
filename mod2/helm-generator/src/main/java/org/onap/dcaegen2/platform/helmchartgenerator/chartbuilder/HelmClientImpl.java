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
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HelmClient implementation which uses helm command installed in the runtime environment.
 * @author Dhrumin Desai
 */
@Component
@Slf4j
public class HelmClientImpl implements HelmClient {

    /**
     * performs <code>helm lint</code> operation
     * @param chartLocation helm chart location
     */
    @Override
    public void lint(File chartLocation) {
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

    private File runProcess(ProcessBuilder builder, String command) {
        Process process = null;
        String chartPath = "";
        try {
            process = builder.start();
            if(command.equals("lint")) {
                printLintingProcessOutput(process);
            }
            else {
                chartPath = printPackagingProcessOutput(process);
            }
            assertExitCode(process);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error occurred while running helm command.");
        }
        return new File(chartPath);
    }

    private void printLintingProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(log::info);
    }

    private String printPackagingProcessOutput(Process process) throws IOException {
        String helmChartPath = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null){
            if (line.contains("Successfully packaged chart and saved it to: ")){
                helmChartPath = line.split("Successfully packaged chart and saved it to: ")[1];
            }
            log.info(line);
        }
        if(helmChartPath.isEmpty()){
            throw new RuntimeException("Could not generate the chart.");
        }
        return helmChartPath;
    }

    private void assertExitCode(Process process) throws InterruptedException {
        int exitCode = 0;
        exitCode = process.waitFor();
        if (exitCode != 0){
            throw new RuntimeException("Error occurred while running helm command.");
        }
    }
}
