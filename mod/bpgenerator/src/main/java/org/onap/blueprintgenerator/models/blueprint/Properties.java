/**============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 ================================================================================
 Modifications Copyright (c) 2020 Nokia. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.onap.blueprintgenerator.models.componentspec.Auxilary;
import org.onap.blueprintgenerator.models.componentspec.ComponentSpec;
import org.onap.blueprintgenerator.models.componentspec.Publishes;
import org.onap.blueprintgenerator.models.componentspec.Subscribes;
import org.onap.blueprintgenerator.models.dmaapbp.DmaapStreams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class Properties {
	private Appconfig application_config;
	private Auxilary docker_config;
	private Object image;
	private GetInput location_id;
	private String service_component_type;
	private TreeMap<String, Object> log_info;
	private String dns_name;
	private Object replicas;
	private String name;
	private GetInput topic_name;
	private GetInput feed_name;
	ArrayList<DmaapStreams> streams_publishes;
	ArrayList<DmaapStreams> streams_subscribes;
	private TlsInfo tls_info;
	private ExternalTlsInfo external_tls_info;
	private ResourceConfig resource_config;
	private GetInput always_pull_image;
	//private boolean useExisting;

	public TreeMap<String, LinkedHashMap<String, Object>> createOnapProperties(TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec cs, String override) {
		TreeMap<String, LinkedHashMap<String, Object>> retInputs = new TreeMap<String, LinkedHashMap<String, Object>>();
		retInputs = inps;

		//set the image
		GetInput image = new GetInput();
		image.setGet_input("image");
		this.setImage(image);
		LinkedHashMap<String, Object> img = new LinkedHashMap<String, Object>();
		img.put("type", "string");
		img.put("default", cs.getArtifacts()[0].getUri());
		retInputs.put("image", img);

		//set the location id
		GetInput location = new GetInput();
		location.setGet_input("location_id");
		this.setLocation_id(location);
		LinkedHashMap<String, Object> locMap = new LinkedHashMap();
		locMap.put("type", "string");
		locMap.put("default", "");
		retInputs.put("location_id", locMap);

		//set the log info
		this.setLog_info(cs.getAuxilary().getLog_info());

		//set the replicas
		GetInput replica = new GetInput();
		replica.setGet_input("replicas");
		this.setReplicas(replica);
		LinkedHashMap<String, Object> rep = makeInput("integer", "number of instances", 1);
		retInputs.put("replicas", rep);

		//set the dns name
		this.setDns_name(cs.getSelf().getName());

		//set the name
		this.setName(cs.getSelf().getName());

		//set the docker config
		Auxilary aux = cs.getAuxilary();
//		if(aux.getPorts() != null) {
//			retInputs = aux.createPorts(retInputs);
//		}
		this.setDocker_config(aux);

		//set the app config
		Appconfig app = new Appconfig();
		retInputs = app.createAppconfig(retInputs, cs, override, false);
		this.setApplication_config(app);

		// set always_pull_image
		this.always_pull_image = new GetInput();
		this.always_pull_image.setGet_input("always_pull_image");
		LinkedHashMap<String, Object> inputAlwaysPullImage = makeInput("boolean",
				"Set to true if the image should always be pulled",
				true);
		retInputs.put("always_pull_image", inputAlwaysPullImage);

		//set the tls info
		if (cs.getAuxilary().getTls_info() != null) {
			addTlsInfo(cs, retInputs);
		}
		//set the external tls info
		if (cs.getAuxilary().getExternal_tls_info() != null) {
			addExternalTlsInfo(cs, retInputs);
		}
		//set the reource config
		ResourceConfig resource = new ResourceConfig();
		retInputs = resource.createResourceConfig(retInputs, cs.getSelf().getName());
		this.setResource_config(resource);

		return retInputs;
	}

	public TreeMap<String, LinkedHashMap<String, Object>> createDmaapProperties(TreeMap<String, LinkedHashMap<String, Object>> inps, ComponentSpec cs, String override) {
		TreeMap<String, LinkedHashMap<String, Object>> retInputs = new TreeMap<String, LinkedHashMap<String, Object>>();
		retInputs = inps;

		//set the image
		GetInput image = new GetInput();
		image.setGet_input("tag_version");
		this.setImage(image);
		LinkedHashMap<String, Object> img = new LinkedHashMap<String, Object>();
		img.put("type", "string");
		img.put("default", cs.getArtifacts()[0].getUri());
		retInputs.put("tag_version", img);

		//set the log info
		this.setLog_info(cs.getAuxilary().getLog_info());

		//set service component type
		String sType = cs.getSelf().getName();
		sType = sType.replace('.', '-');
		this.setService_component_type(sType);

		//set the tls info
		if (cs.getAuxilary().getTls_info() != null) {
			addTlsInfo(cs, retInputs);
		}
		//set the external tls info
		if (cs.getAuxilary().getExternal_tls_info() != null) {
			addExternalTlsInfo(cs, retInputs);
		}
		//set the replicas
		GetInput replica = new GetInput();
		replica.setGet_input("replicas");
		this.setReplicas(replica);
		LinkedHashMap<String, Object> rep = makeInput("integer", "number of instances", 1);
		retInputs.put("replicas", rep);

//		//set the dns name
//		this.setDns_name(cs.getSelf().getName());

//		//set the name
//		this.setName(cs.getSelf().getName());

		//set the docker config
		Auxilary aux = cs.getAuxilary();
//		if(aux.getPorts() != null) {
//			retInputs = aux.createPorts(retInputs);
//		}
		this.setDocker_config(aux);

		//set the appconfig
		Appconfig app = new Appconfig();
		retInputs = app.createAppconfig(retInputs, cs, override, true);
		this.setApplication_config(app);

		//set the stream publishes
		ArrayList<DmaapStreams> pubStreams = new ArrayList();
		if (cs.getStreams().getPublishes() != null) {
			for (Publishes p : cs.getStreams().getPublishes()) {
				if (p.getType().equals("message_router") || p.getType().equals("message router")) {
					String topic = p.getConfig_key() + "_topic";
					DmaapStreams mrStreams = new DmaapStreams();
					retInputs = mrStreams.createStreams(inps, cs, topic, p.getType(), p.getConfig_key(), p.getRoute(), 'p');
					pubStreams.add(mrStreams);
				} else if (p.getType().equals("data_router") || p.getType().equals("data router")) {
					String feed = p.getConfig_key() + "_feed";
					DmaapStreams drStreams = new DmaapStreams();
					retInputs = drStreams.createStreams(inps, cs, feed, p.getType(), p.getConfig_key(), p.getRoute(), 'p');
					pubStreams.add(drStreams);
				}
			}
		}

		//set the stream subscribes
		ArrayList<DmaapStreams> subStreams = new ArrayList();
		if (cs.getStreams().getSubscribes() != null) {
			for (Subscribes s : cs.getStreams().getSubscribes()) {
				if (s.getType().equals("message_router") || s.getType().equals("message router")) {
					String topic = s.getConfig_key() + "_topic";
					DmaapStreams mrStreams = new DmaapStreams();
					retInputs = mrStreams.createStreams(inps, cs, topic, s.getType(), s.getConfig_key(), s.getRoute(), 's');
					subStreams.add(mrStreams);
				} else if (s.getType().equals("data_router") || s.getType().equals("data router")) {
					String feed = s.getConfig_key() + "_feed";
					DmaapStreams drStreams = new DmaapStreams();
					retInputs = drStreams.createStreams(inps, cs, feed, s.getType(), s.getConfig_key(), s.getRoute(), 's');
					subStreams.add(drStreams);
				}
			}
		}

		if (pubStreams.size() != 0) {
			this.setStreams_publishes(pubStreams);
		}
		if (subStreams.size() != 0) {
			this.setStreams_subscribes(subStreams);
		}

		//set the reource config
		ResourceConfig resource = new ResourceConfig();
		retInputs = resource.createResourceConfig(retInputs, cs.getSelf().getName());
		this.setResource_config(resource);


		return retInputs;
	}

	private void addTlsInfo(ComponentSpec cs, TreeMap<String, LinkedHashMap<String, Object>> retInputs) {
		TlsInfo tlsInfo = new TlsInfo();
		tlsInfo.setCertDirectory((String) cs.getAuxilary().getTls_info().get("cert_directory"));
		GetInput useTLSFlag = new GetInput();
		useTLSFlag.setGet_input("use_tls");
		tlsInfo.setUseTls(useTLSFlag);
		this.setTls_info(tlsInfo);
		LinkedHashMap<String, Object> useTlsFlagInput = makeInput("boolean",
				"flag to indicate tls enable/disable",
				cs.getAuxilary().getTls_info().get("use_tls"));
		retInputs.put("use_tls", useTlsFlagInput);
	}

	private void addExternalTlsInfo(ComponentSpec cs, TreeMap<String, LinkedHashMap<String, Object>> retInputs) {
		final String inputPrefix = "external_tls_";
		final String useExternalTlsInputName = inputPrefix + "use_external_tls";
		final String caNameInputName = inputPrefix + "ca_name";
		final String commonNameInputName = inputPrefix + "common_name";
		final String sansInputName = inputPrefix + "sans";

		ExternalTlsInfo externalTlsInfoBp = new ExternalTlsInfo();
		TreeMap<String, Object> externalTlsInfoCs = cs.getAuxilary().getExternal_tls_info();

		GetInput useExternalTlsFlag = new GetInput(useExternalTlsInputName);
		GetInput caNameInput = new GetInput(caNameInputName);
		externalTlsInfoBp.setExternalCertDirectory((String) externalTlsInfoCs.get("external_cert_directory"));
		externalTlsInfoBp.setUseExternalTls(useExternalTlsFlag);
		externalTlsInfoBp.setCaName(caNameInput);

		ExternalCertificateParameters externalCertificateParameters = new ExternalCertificateParameters();
		GetInput commonNameInput = new GetInput(commonNameInputName);
		GetInput sansInput = new GetInput(sansInputName);
		externalCertificateParameters.setCommonName(commonNameInput);
		externalCertificateParameters.setSans(sansInput);
		externalTlsInfoBp.setExternalCertificateParameters(externalCertificateParameters);

		this.setExternal_tls_info(externalTlsInfoBp);

		LinkedHashMap<String, Object> useTlsFlagInput = makeInput("boolean",
				"Flag to indicate external tls enable/disable.",
				externalTlsInfoCs.get("use_external_tls"));
		retInputs.put(useExternalTlsInputName, useTlsFlagInput);

		LinkedHashMap<String, Object> caNameInputMap = makeInput("string",
				"Name of Certificate Authority configured on CertService side.",
				externalTlsInfoCs.get("ca_name"));
		retInputs.put(caNameInputName, caNameInputMap);

		LinkedHashMap<String, Object> externalTlsCertParams = (LinkedHashMap<String, Object>) externalTlsInfoCs.get("external_certificate_parameters");

		LinkedHashMap<String, Object> commonNameInputMap = makeInput("string",
				"Common name which should be present in certificate.",
				externalTlsCertParams.get("common_name"));
		retInputs.put(commonNameInputName, commonNameInputMap);

		LinkedHashMap<String, Object> sansInputMap = makeInput("string",
				"\"List of Subject Alternative Names (SANs) which should be present in certificate. " +
						"Delimiter - : Should contain common_name value and other FQDNs under which given " +
						"component is accessible.\"",
				externalTlsCertParams.get("sans"));
		retInputs.put(sansInputName, sansInputMap);
	}

	private LinkedHashMap<String, Object> makeInput(String type, String description, Object defaultValue) {
		LinkedHashMap<String, Object> useTlsFlagInput = new LinkedHashMap<String, Object>();
		useTlsFlagInput.put("type", type);
		useTlsFlagInput.put("description", description);
		useTlsFlagInput.put("default", defaultValue);
		return useTlsFlagInput;
	}
}
