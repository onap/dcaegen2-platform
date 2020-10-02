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

import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;

import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class Interfaces {
	private Start start;
	public TreeMap<String, LinkedHashMap<String, Object>> createInterface(TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec cs){
		//create the start object
		Start start = new Start();
		TreeMap<String, LinkedHashMap<String, Object>> retInputs = start.createOnapStart(inps, cs);
		this.setStart(start);
		return retInputs;
	}
}