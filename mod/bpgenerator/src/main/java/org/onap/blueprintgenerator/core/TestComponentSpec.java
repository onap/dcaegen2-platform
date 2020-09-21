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

package org.onap.blueprintgenerator.core;

import java.util.ArrayList;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.onap.blueprintgenerator.models.componentspec.Artifacts;
import org.onap.blueprintgenerator.models.componentspec.Auxilary;
import org.onap.blueprintgenerator.models.componentspec.CallsObj;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Container;
import org.onap.blueprintgenerator.models.componentspec.HealthCheck;
import org.onap.blueprintgenerator.models.componentspec.Host;
import org.onap.blueprintgenerator.models.componentspec.Parameters;
import org.onap.blueprintgenerator.models.componentspec.Policy;
import org.onap.blueprintgenerator.models.componentspec.ProvidesObj;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Self;
import org.onap.blueprintgenerator.models.componentspec.Services;
import org.onap.blueprintgenerator.models.componentspec.Streams;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;
import org.onap.blueprintgenerator.models.componentspec.Volumes;

@Getter @Setter
public class TestComponentSpec {
	private String cs = "{\r\n" +
			"	\"self\": {\r\n" + 
			"		\"component_type\": \"docker\",\r\n" + 
			"		\"description\": \"Test component spec\",\r\n" + 
			"		\"name\": \"test.component.spec\",\r\n" + 
			"		\"version\": \"1.0.1\"\r\n" + 
			"	},\r\n" + 
			"\r\n" + 
			"	\"service\": {\r\n" + 
			"		\"calls\": [],\r\n" + 
			"		\"provides\": []\r\n" + 
			"	},\r\n" + 
			"\r\n" + 
			"	\"streams\": {\r\n" + 
			"				\"publishes\": [{\r\n" + 
			"				\"config_key\": \"TEST-PUB-DR\",\r\n" + 
			"				\"format\": \"dataformat_Hello_World_PM\",\r\n" + 
			"				\"type\": \"data_router\",\r\n" + 
			"				\"version\": \"1.0.0\"\r\n" + 
			"			},\r\n" + 
			"			{\r\n" + 
			"				\"config_key\": \"TEST-PUB-MR\",\r\n" + 
			"				\"format\": \"dataformat_Hello_World_PM\",\r\n" + 
			"				\"type\": \"message_router\",\r\n" + 
			"				\"version\": \"1.0.0\"\r\n" + 
			"			}\r\n" + 
			"		],\r\n" + 
			"\r\n" + 
			"		\"subscribes\": [{\r\n" + 
			"				\"config_key\": \"TEST-SUB-MR\",\r\n" + 
			"				\"format\": \"dataformat_Hello_World_PM\",\r\n" + 
			"				\"route\": \"/TEST_HELLO_WORLD_SUB_MR\",\r\n" + 
			"				\"type\": \"message_router\",\r\n" + 
			"				\"version\": \"1.0.0\"\r\n" + 
			"			},\r\n" + 
			"			{\r\n" + 
			"				\"config_key\": \"TEST-SUB-DR\",\r\n" + 
			"				\"format\": \"dataformat_Hello_World_PM\",\r\n" + 
			"				\"route\": \"/TEST-HELLO-WORLD-SUB-DR\",\r\n" + 
			"				\"type\": \"data_router\",\r\n" + 
			"				\"version\": \"1.0.0\"\r\n" + 
			"			}		\r\n" + 
			"		]\r\n" + 
			"	},\r\n" + 
			"\r\n" + 
			"	\"parameters\":\r\n" + 
			"	[\r\n" + 
			"		{\r\n" + 
			"			\"name\": \"testParam1\",\r\n" + 
			"			\"description\": \"test parameter 1\",\r\n" + 
			"			\"value\": \"test-param-1\",\r\n" + 
			"			\"type\": \"string\",\r\n" + 
			"			\"sourced_at_deployment\": true,\r\n" + 
			"			\"designer_editable\": true,\r\n" + 
			"			\"policy_editable\": true,\r\n" + 
			"			\"policy_group\": \"Test_Parameters\",\r\n" + 
			"			\"required\": true\r\n" + 
			"		}\r\n" + 
			"	],\r\n" + 
			"\r\n" + 
			"	\"auxilary\": {\r\n" + 
			"		\"healthcheck\": {\r\n" + 
			"			\"type\": \"docker\",\r\n" + 
			"			\"interval\": \"300s\",\r\n" + 
			"			\"timeout\": \"120s\",\r\n" + 
			"			\"script\": \"/etc/init.d/nagios status\"\r\n" + 
			"		},\r\n" + 
			"\r\n" + 
			"		\"databases\" : {\r\n" + 
			"          \"TestDB1\": \"PGaaS\",\r\n" + 
			"          \"TestDB2\": \"PGaaS\"\r\n" + 
			"        },\r\n" + 
			"\r\n" + 
			"		\"policy\": {\r\n" + 
			"			\"trigger_type\": \"docker\",\r\n" + 
			"			\"script_path\": \"/opt/app/manager/bin/reconfigure.sh\"\r\n" + 
			"		},\r\n" + 
			"		\"volumes\": [\r\n" + 
			"			{\r\n" + 
			"				\"container\": {\r\n" + 
			"					\"bind\": \"/opt/app/manager/config/hostname\"\r\n" + 
			"				},\r\n" + 
			"				\"host\": {\r\n" + 
			"					\"path\": \"/etc/hostname\",\r\n" + 
			"					\"mode\": \"ro\"\r\n" + 
			"				}\r\n" + 
			"			}\r\n" + 
			"\r\n" + 
			"		],\r\n" + 
			"		\"ports\": [\r\n" + 
			"			\"80:80\"\r\n" + 
			"		]\r\n" + 
			"	},\r\n" + 
			"\r\n" + 
			"	    \"artifacts\": [{\r\n" + 
			"		\"type\": \"docker image\",\r\n" + 
			"		\"uri\": \"test.tester\"\r\n" + 
			"	}]	\r\n" + 
			"\r\n" + 
			"}";
	private ComponentSpec csConcrete;

	public TestComponentSpec() {
		this.csConcrete = createComponentSpec();
	}

	private ComponentSpec createComponentSpec(){
		//Manually fill a component spec object with the values from the file itself
		ComponentSpec manualSpec = new ComponentSpec();

		Self self = new Self();
		self.setComponent_type("docker");
		self.setDescription("Test component spec");
		self.setName("test.component.spec");
		self.setVersion("1.0.1");
		manualSpec.setSelf(self);

		Services services = new Services();
		CallsObj[] calls = new CallsObj[0];
		ProvidesObj[] provides = new ProvidesObj[0];
		services.setCalls(calls);
		services.setProvides(provides);
		manualSpec.setServices(null);

		Streams streams = new Streams();
		Publishes[] publishes = new Publishes[2];
		Publishes pub1 = new Publishes();
		pub1.setConfig_key("TEST-PUB-DR");
		pub1.setFormat("dataformat_Hello_World_PM");
		pub1.setType("data_router");
		pub1.setVersion("1.0.0");

		Publishes pub2 = new Publishes();
		pub2.setConfig_key("TEST-PUB-MR");
		pub2.setFormat("dataformat_Hello_World_PM");
		pub2.setType("message_router");
		pub2.setVersion("1.0.0");
		publishes[0] = pub1;
		publishes[1] = pub2;
		streams.setPublishes(publishes);

		Subscribes[] subscribes = new Subscribes[2];
		Subscribes sub1 = new Subscribes();
		sub1.setConfig_key("TEST-SUB-MR");
		sub1.setFormat("dataformat_Hello_World_PM");
		sub1.setRoute("/TEST_HELLO_WORLD_SUB_MR");
		sub1.setType("message_router");
		sub1.setVersion("1.0.0");

		Subscribes sub2 = new Subscribes();
		sub2.setConfig_key("TEST-SUB-DR");
		sub2.setFormat("dataformat_Hello_World_PM");
		sub2.setRoute("/TEST-HELLO-WORLD-SUB-DR");
		sub2.setType("data_router");
		sub2.setVersion("1.0.0");
		subscribes[0] = sub1;
		subscribes[1] = sub2;
		streams.setSubscribes(subscribes);

		manualSpec.setStreams(streams);

		Parameters[] parameters = new Parameters[1];
		Parameters par1 = new Parameters();
		par1.setName("testParam1");
		par1.setValue("test-param-1");
		par1.setDescription("test parameter 1");
		par1.setSourced_at_deployment(true);
		par1.setDesigner_editable(true);
		par1.setPolicy_editable(true);
		par1.setPolicy_group("Test_Parameters");
		par1.setRequired(true);
		par1.setType("string");
		parameters[0] = par1;

		manualSpec.setParameters(parameters);

		Auxilary auxilary = new Auxilary();
		HealthCheck healthcheck = new HealthCheck();
		healthcheck.setInterval("300s");
		healthcheck.setTimeout("120s");
		healthcheck.setScript("/etc/init.d/nagios status");
		healthcheck.setType("docker");
		auxilary.setHealthcheck(healthcheck);

		Volumes[] volumes = new Volumes[1];
		Volumes vol1 = new Volumes();
		Container con1 = new Container();
		con1.setBind("/opt/app/manager/config/hostname");
		Host host1 = new Host();
		host1.setPath("/etc/hostname");
		host1.setMode("ro");
		vol1.setContainer(con1);
		vol1.setHost(host1);

		volumes[0] = vol1;

		auxilary.setVolumes(volumes);

		ArrayList<Object> ports = new ArrayList();
		ports.add("80:80");

		TreeMap<String, String> dataBases = new TreeMap<>();
		dataBases.put("TestDB1", "PGaaS");
		dataBases.put("TestDB2", "PGaaS");
		auxilary.setDatabases(dataBases);

		Policy pol = new Policy();
		pol.setTrigger_type("docker");
		pol.setScript_path("/opt/app/manager/bin/reconfigure.sh");
		auxilary.setPolicy(pol);

		auxilary.setPorts(ports);

		manualSpec.setAuxilary(auxilary);

		Artifacts[] artifacts = new Artifacts[1];
		Artifacts art = new Artifacts();
		art.setType("docker image");
		art.setUri("test.tester");

		artifacts[0] = art;
		manualSpec.setArtifacts(artifacts);
		return manualSpec;
	}
}
