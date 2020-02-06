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
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import org.json.JSONObject;

import com.att.vcc.inventorycollector.data.EntityData;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;

public class DmaapMRSub implements Runnable {
	AAIEventProcessor aaiEventProcessor;
	ExecutorService executorService;
	private String updateDB = null;

	static FileWriter routeWriter = null;
	static Properties props = null;
	static FileReader routeReader = null;

	private static String consumerProperties = null;

	private static EcompLogger ecompLogger;
	private ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public DmaapMRSub(String updateDB) {
		ecompLogger.debug("DMaaP MR constructor");
		this.updateDB = updateDB;
		if (this.updateDB.equalsIgnoreCase("yes")) {
			DmaapMRSub.consumerProperties = EventUtil.DMAAPCONSUMERFILEPATHDB;
		} else {
			DmaapMRSub.consumerProperties = EventUtil.DMAAPCONSUMERFILEPATH;
		}
		aaiEventProcessor = new AAIEventProcessor();
		executorService = Executors.newFixedThreadPool(10);
	}

	public DmaapMRSub() {
		ecompLogger.debug("DMaaP MR constructor");
		aaiEventProcessor = new AAIEventProcessor();
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
			final MRConsumer consumer = MRClientFactory.createConsumer(consumerFilePath);

			if (consumer == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_CAMBRIA_CONSUMER_INIT_FAILURE);
				return;
			}

			while (true) {
				try {
					for (String msg : consumer.fetch()) {

						ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_AAI_EVENT, msg);

						String entityType = "";
						try {
							entityType = getEntityType(msg);
						} catch (org.json.JSONException e) {
							ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_EVENT_ISSUE,
									"JSONException", e.getMessage());
						}
						if (entityType.isEmpty()) {
							ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_EVENT_ISSUE,
									"entityType is empty", msg);
						}

						if (this.updateDB == null || this.updateDB.equalsIgnoreCase("No")) {

							// Call collector component with UEB message if the
							// event is of type Vce,Vpe,Vserver
							String[] arr = EventUtil.COLLECTOREVENTS.split(",");
							List<String> lCollEvents = Arrays.asList(arr);
							if (lCollEvents.contains(entityType)) {
								if (Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_COLLECTOR_EVENTS)) {
									// Call collector component

									final EntityData entityData = new EntityData("", entityType, "", "", msg);
									final String message = msg;
									executorService.execute(new Runnable() {
										public void run() {
											System.out.println("Asynchronous collector task for event");
											ecompLogger.debug("Calling collector component, entityType:" + entityData);
											try {
												configHandler.getAAIUpdates(message);
											} catch (DTIException e) {
												ecompLogger.error(inventoryCollectorMessageEnum.DTI_EXCEPTION,
														e.getMessage());
											}
										}
									});
								} else {
									ecompLogger.debug(inventoryCollectorMessageEnum.DTI_POST_ORCH_DISABLED);
								}
							}

						} else {

							if (EventUtil.DCAEENV.equalsIgnoreCase("D2")) {

								// Call Event Processor and DB adapter
								Boolean result = aaiEventProcessor.processAAIEvent(msg);
								if (!result) {
									ecompLogger.error(
											inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
											msg);
								}
							}
						}

					}
				} catch (Exception e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE,
							e.getMessage());
				}

				Thread.sleep(20000);
			}
		} catch (MalformedURLException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE, e.getMessage());
		} catch (IOException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_PROCESSING_FAILURE, e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getEntityType(String dmaapMRMsg) throws org.json.JSONException {
		ecompLogger.debug("Entering getEntityType");
		JSONObject objMsg = new JSONObject(dmaapMRMsg);

		if (objMsg.isNull("event-header")) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_EVENT_ISSUE,
					"eventheader is empty", dmaapMRMsg);
			return "";
		} else {
			JSONObject dmaapMRHeader = objMsg.getJSONObject("event-header");

			if (dmaapMRHeader.isNull("entity-type")) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAPMR_SUBSCRIBER_RECEIVED_EVENT_ISSUE,
						"entityType is empty", dmaapMRMsg);
				return "";
			}
			String entityType = dmaapMRHeader.getString("entity-type");
			return entityType;

		}
	}
}