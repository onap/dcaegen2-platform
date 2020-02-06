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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.MalformedURLException;
//import java.security.GeneralSecurityException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import org.json.JSONObject;

import com.att.vcc.inventorycollector.domain.SeedingManager;
import com.att.vcc.inventorycollector.data.EntityData;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
//import com.att.nsa.cambria.client.CambriaClientBuilders;
//import com.att.nsa.cambria.client.CambriaConsumer;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;

public class SeedingManagerDmaapMRSub implements Runnable {
	private static MRConsumer cc;
	ExecutorService executorService;

	static FileWriter routeWriter = null;
	static Properties props = null;
	static FileReader routeReader = null;

	private static String preferredRouteFileName = EventUtil.DMAAPCONSUMERPREFERREDROUTE;
	private static String consumerProperties = null;

	private static EcompLogger ecompLogger;
	private ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public SeedingManagerDmaapMRSub() {
		ecompLogger.debug("DMaaP MR constructor");
		consumerProperties = EventUtil.DMAAPCONSUMERFILEPATHSM;
		executorService = Executors.newFixedThreadPool(10);
	}

	public void shutdownExecService() {
		executorService.shutdown();
	}

	public void run() {

		ecompLogger.debug("Staring DmaapMR Subscriber");

		// .authenticatedBy (EventUtil.UEBAPIKEY,EventUtil.UEBAPISECRET)
		try {
			String consumerFilePath = consumerProperties;

			props = new Properties();
			final MRConsumer cc = MRClientFactory.createConsumer(consumerFilePath);

			if (cc == null) {
				ecompLogger
						.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_CAMBRIA_CONSUMER_INIT_FAILURE);
				return;
			}

			while (true) {
				try {
					for (final String msg : cc.fetch()) {
						ecompLogger
								.debug(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_AAI_EVENT,
										msg);
						JSONObject eventJSONObject = new JSONObject(msg);
						if (eventJSONObject != null) {
							String hostname = eventJSONObject == null ? null
									: eventJSONObject.getString("hostName");
							String changeType = eventJSONObject == null ? null
									: eventJSONObject.getString("changeType");
							if (hostname != null && changeType != null)
								configHandler.seedingManagerEvent(hostname, changeType, null,null);
						}
					}
				} catch (IOException e) {
					ecompLogger
							.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
									e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					ecompLogger
							.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
									e.getMessage());
					e.printStackTrace();
				}

				Thread.sleep(2000);
			}
		} catch (

		MalformedURLException e) {
			ecompLogger
					.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
							e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			ecompLogger
					.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
							e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}