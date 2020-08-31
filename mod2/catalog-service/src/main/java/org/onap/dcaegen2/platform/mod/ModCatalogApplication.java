/*
 * ============LICENSE_START=======================================================
 *  org.onap.dcae
 *  ================================================================================
 *  Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.mod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * The application class
 */
@SpringBootApplication
@EnableSwagger2
public class ModCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModCatalogApplication.class, args);
	}
	
	@Bean
	public Docket swaggerConfiguration(){
		// return a prepared Docket instance
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				//.paths(PathSelectors.ant("/api/*"))
				.apis(RequestHandlerSelectors.basePackage("org.onap.dcaegen2.platform.mod"))
				.build()
				.apiInfo(apiDetails());
	}

	private ApiInfo apiDetails() {
		Contact DEFAULT_CONTACT = new Contact("", "", "");
		return new ApiInfo(
				"MOD APIs",
				"APIs for MOD",
				"1.0.0"
				,"", DEFAULT_CONTACT, "", "", new ArrayList<>()
		);
	}
	//http://localhost:8080/swagger-ui.html for web page view
	//http://localhost:8080/v2/api-docs for json view
}
