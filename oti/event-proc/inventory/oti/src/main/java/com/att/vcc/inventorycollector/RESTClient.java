// ============LICENSE_START=======================================================
// Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
// ================================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============LICENSE_END=========================================================

package com.att.vcc.inventorycollector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.lang.String;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;


import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
//import org.glassfish.jersey.client.ClientResponse;

import com.att.aft.dme2.internal.jersey.client.urlconnection.HTTPSProperties;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
/*import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;*/

public class RESTClient {
	private static KeyManagerFactory kmf = null;
	private static EcompLogger ecompLogger;
	private static Client client = null;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();

		FileInputStream fin = null;
		try {

			if (Constants.TRUE.equalsIgnoreCase(EventUtil.SET_TRUSTSTORE)) {
				System.setProperty("javax.net.ssl.trustStore", EventUtil.TRUSTSTORE_PATH);
				System.setProperty("javax.net.ssl.trustStorePassword", EventUtil.TRUSTSTORE_PASSWORD);
			}

			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE, EventUtil.KEYSTORE_PATH);
			kmf = KeyManagerFactory.getInstance("SunX509");
			if (!EventUtil.KEYSTORE_PATH.contains(Constants.PATH_BLACKLIST))
				fin = new FileInputStream(ValidationData.cleanPathString(EventUtil.KEYSTORE_PATH));
			else {
				// errorLog.error("EventUtil.KEYSTORE_PATH:"+
				// EventUtil.KEYSTORE_PATH + " is not proper. Please check");
			}
			KeyStore ks = KeyStore.getInstance("PKCS12");
			char[] pwd = (EventUtil.KEYSTORE_PASSWORD).toCharArray();
			ks.load(fin, pwd);
			kmf.init(ks, pwd);
			ClientConfig config = new ClientConfig();
			SSLContext ctx = null;
			try {
				ctx = SSLContext.getInstance("TLS");

				try {
					ctx.init(kmf.getKeyManagers(), null, null);
				} catch (KeyManagementException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_SSL_INIT_ISSUE, e.toString());
					throw new DTIException("Error while initializing SSL Context " + e.getMessage());
				}
				config.property(ClientProperties.READ_TIMEOUT, Integer.parseInt(EventUtil.READ_TIMEOUT));
				config.property(ClientProperties.CONNECT_TIMEOUT, Integer.parseInt(EventUtil.CONNECTION_TIMEOUT));
			} catch (NoSuchAlgorithmException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_SSL_INIT_ISSUE, e.toString());
			}

			client = ClientBuilder.newBuilder().withConfig(config).hostnameVerifier(new TrustAllHostNameVerifier())
					.sslContext(ctx).build();
		} catch (Exception e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE_FAILURE,
					Util.errorStacktoString(e.getStackTrace()));
			e.printStackTrace();
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE_FAILURE,
							Util.errorStacktoString(e.getStackTrace()));
					e.printStackTrace();
				}
			}
		}
	}

	public String retrieveAAIObject(String url) throws DTIException {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_SSL_INIT);
		// Using jersey for REST calls
		/*ClientConfig config = new ClientConfig();
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");

			try {
				ctx.init(kmf.getKeyManagers(), null, null);
			} catch (KeyManagementException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_SSL_INIT_ISSUE, e.toString());
				throw new DTIException("Error while initializing SSL Context " + e.getMessage());
			}
			config.property(ClientProperties.READ_TIMEOUT, Integer.parseInt(EventUtil.READ_TIMEOUT));
			config.property(ClientProperties.CONNECT_TIMEOUT, Integer.parseInt(EventUtil.CONNECTION_TIMEOUT));
		} catch (NoSuchAlgorithmException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_SSL_INIT_ISSUE, e.toString());
			return "";
		}

		Client client = ClientBuilder.newBuilder().withConfig(config).hostnameVerifier(new TrustAllHostNameVerifier())
				.sslContext(ctx).build();*/
		
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE_URL, url);
		String res = "";
		try {
			res = client.target(url).request(MediaType.APPLICATION_JSON).header("X-FromAppId", "DCAE-CCS")
					.header("X-TransactionId", "get_aai_subscr").get(String.class);

			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE_SUCCESS, res);
		} catch (Exception e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_RETRIEVE_FAILURE, e.toString());
			e.printStackTrace();
		}

		return res;
	}

	public int post(String input, String url) throws DTIException {
		BufferedReader reader = null;
		try {

			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_POST_URL, url);
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_POST_REQUEST, input);
			ClientConfig config = new ClientConfig();
			Client client = ClientBuilder.newClient(config);
			WebTarget webTarget = client.target(url);

			Response response = webTarget.request("application/json").post(Entity.json(input),
					Response.class);

			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_POST_RESPONSE_CODE,
					String.valueOf(response.getStatus()), response.getStatusInfo().getReasonPhrase());
			String line = null;
			line = response.readEntity(String.class);
			System.out.println(line);
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_REST_CLIENT_POST_RESPONSE_MESSAGE, line);

			return response.getStatus();
		} catch (Exception e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_REST_CLIENT_POST_FAILURE, url, e.toString());
			// e.printStackTrace();
		}
		return 0;
	}

	public static class TrustAllHostNameVerifier implements HostnameVerifier {

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}

	}
}