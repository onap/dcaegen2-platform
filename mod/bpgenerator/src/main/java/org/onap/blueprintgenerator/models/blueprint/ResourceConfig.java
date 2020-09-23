/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 Copyright (c) 2020 Nokia. All rights reserved.
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

import static org.onap.blueprintgenerator.common.blueprint.BlueprintHelper.createInputValue;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO: Auto-generated Javadoc
/* (non-Javadoc)
* @see java.lang.Object#toString()
*/
@Getter @Setter

/* (non-Javadoc)
* @see java.lang.Object#toString()
*/
@Builder

/**
* Instantiates a new resource config obj.
*/
@NoArgsConstructor

/**
* Instantiates a new resource config obj.
*
* @param limits the limits
* @param requests the requests
*/
@AllArgsConstructor

public class ResourceConfig {

	/** The limits. */
	private TreeMap<String, GetInput> limits;

	/** The requests. */
	private TreeMap<String, GetInput> requests;


	/**
	 * Creates the resource config.
	 *
	 * @param inps the inps
	 * @param name the name
	 * @return the tree map
	 */
	public TreeMap<String, LinkedHashMap<String, Object>> createResourceConfig(TreeMap<String, LinkedHashMap<String, Object>> inps, String name){

		LinkedHashMap<String, Object> memoryLimit = createInputValue("string", "", "128Mi");
		LinkedHashMap<String, Object> cpuLimit = createInputValue("string", "", "250m");

		if(!name.equals("")) {
			name = name + "_";
		}

		//set the limits
		TreeMap<String, GetInput> limits = new TreeMap<>();

		GetInput cpu = new GetInput();
		cpu.setBpInputName(name + "cpu_limit");
		limits.put("cpu", cpu);

		GetInput memL = new GetInput();
		memL.setBpInputName(name + "memory_limit");
		limits.put("memory", memL);

		inps.put(name + "cpu_limit", cpuLimit);
		inps.put(name + "memory_limit", memoryLimit);

		this.setLimits(limits);

		//set the requests
		TreeMap<String, GetInput> requests = new TreeMap<>();

		GetInput cpuR = new GetInput();
		cpuR.setBpInputName(name + "cpu_request");
		requests.put("cpu", cpuR);

		GetInput memR = new GetInput();
		memR.setBpInputName(name + "memory_request");
		requests.put("memory", memR);

		inps.put(name + "cpu_request", cpuLimit);
		inps.put(name + "memory_request", memoryLimit);

		this.setRequests(requests);

		return inps;
	}
}

