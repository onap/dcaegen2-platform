/**============LICENSE_START======================================================= 
 org.onap.dcae 
 ================================================================================ 
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved. 
 ================================================================================ 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
      http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License. 
 ============LICENSE_END========================================================= 
 
*/

package org.onap.blueprintgenerator.models.blueprint;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(value=Include.NON_NULL)
public class Imports {
	/** The imports. */
	private ArrayList<String> imports;

	public static ArrayList<String> createOnapImports() {
		ArrayList<String> imps = new ArrayList<>();
		imps.add("https://www.getcloudify.org/spec/cloudify/4.5.5/types.yaml");
		imps.add("plugin:k8splugin?version=3.4.2");
		imps.add("plugin:dcaepolicyplugin?version=2.4.0");
		return imps;
	}

	public static ArrayList<String> createDmaapImports(){
		ArrayList<String> imps = new ArrayList<>();
		imps.add("https://www.getcloudify.org/spec/cloudify/4.5.5/types.yaml");
		imps.add("plugin:k8splugin?version=3.4.2");
		imps.add("plugin:dmaap?version=1.5.0");
		return imps;
	}

	public static ArrayList<String> createImportsFromFile(String path) {
		ObjectMapper importMapper = new ObjectMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
		File importPath = new File(path);
		try {
			Imports imports = importMapper.readValue(importPath, Imports.class);
			imports.getImports()
				.removeIf(String::isBlank);
			return imports.getImports();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
