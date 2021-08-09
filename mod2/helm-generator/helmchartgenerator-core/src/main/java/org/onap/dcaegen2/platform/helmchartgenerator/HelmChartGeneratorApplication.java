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

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartBuilder;
import org.onap.dcaegen2.platform.helmchartgenerator.distribution.ChartDistributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class to run the application.
 * @author Dhrumin Desai
 */
@SpringBootApplication
@Slf4j
public class HelmChartGeneratorApplication implements CommandLineRunner {

	@Autowired
	private final ChartBuilder builder;

	@Autowired
	private final ChartDistributor distributor;

	public HelmChartGeneratorApplication(ChartBuilder builder, ChartDistributor distributor) {
		this.builder = builder;
		this.distributor = distributor;
	}

	public static void main(String[] args) {
		SpringApplication.run(HelmChartGeneratorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		List<String> argList = new ArrayList<>(Arrays.asList(args));
		boolean isDistribute = false;
		if(argList.contains("--help") || argList.size() < 3){
			printUsage();
			return;
		}
		if(argList.contains("--distribute")){
			isDistribute = true;
			argList.remove("--distribute");
		}

		log.info("STARTED HELM GENERATION:");
		String specSchemaLocation;
		try {
			specSchemaLocation = argList.get(3);
		}
		catch (Exception e) {
			specSchemaLocation = "";
		}

		final File chartPackage = builder.build(argList.get(0), argList.get(1), argList.get(2), specSchemaLocation);
		if(isDistribute) {
			log.info("Distributing..");
			distributor.distribute(chartPackage);
		}
	}

	private void printUsage() throws IOException {
		InputStream inputStream = new ClassPathResource("Usage.txt").getInputStream();
		log.info(new String(inputStream.readAllBytes()));
	}
}
