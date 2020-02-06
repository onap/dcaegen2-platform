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

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;
//EELF wrapper import
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;

public class InventoryCollector {
	private static Properties dtiProps = new Properties();

	private static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");

		boolean result = EventUtil.loadConfigs();
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_LOAD_CONFIG_ERROR);
			System.exit(1);
		}

		String type = args.length > 0 ? args[0] : "UEB";
		System.out.println(type);
		if (type.equalsIgnoreCase("DTIHANDLER")) {
			System.out.println("Inside DTI Handler block.");
			String filename = args.length > 1 ? args[1] : "/opt/app/vcc/temp_config/json_event.json";
			String orchPostURL = args.length > 2 ? args[2]
					: "https://zldcrdm5adcc1aapih00.2b931b.rdm5a.tci.att.com:8188";
			String configDir = System.getenv("DTI_CONFIG");
			if (configDir == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
				return;
			}

			FileInputStream dtiStream = null;
			FileInputStream fis = null;
			String eventsJSON = "";
			try {
				dtiStream = new FileInputStream(Util.safeFileName(configDir) + "/dti.properties");
				dtiProps.load(dtiStream);

				fis = new FileInputStream(ValidationData.cleanPathString(filename));
				eventsJSON = IOUtils.toString(fis, "UTF-8");
				System.out.println(eventsJSON);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				closeFileInputStream(dtiStream);
				closeFileInputStream(fis);
			}

			RESTClient restClient = new RESTClient();
			int statusCode = 0;
			try {
				statusCode = restClient.post(eventsJSON, orchPostURL);
				System.out.println(statusCode);
				System.out.println("DTI Handler Response code: " + String.valueOf(statusCode));
			} catch (DTIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (type.equalsIgnoreCase("AAIEVENT")) {
			System.out.println("Inside AAI EVENT block.");
			String callConfigHandler = args.length > 1 ? args[1] : "No";
			String filename = args.length > 2 ? args[2] : "/opt/app/vcc/temp_config/aaievent.json";
			String configDir = System.getenv("DTI_CONFIG");
			if (configDir == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
				return;
			}

			FileInputStream dtiStream = null;
			FileInputStream fis = null;
			String eventsJSON = "";
			try {
				dtiStream = new FileInputStream(
						ValidationData.cleanPathString(Util.safeFileName(configDir) + "/dti.properties"));
				dtiProps.load(dtiStream);

				fis = new FileInputStream(ValidationData.cleanPathString(filename));
				eventsJSON = IOUtils.toString(fis, "UTF-8");
				System.out.println(eventsJSON);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				closeFileInputStream(dtiStream);
				closeFileInputStream(fis);
			}

			ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
			System.out.println("This is aaievent");
			if ("Yes".equalsIgnoreCase(callConfigHandler)) {
				try {
					configHandler.getAAIUpdates(eventsJSON);
				} catch (DTIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			AAIEventProcessor eventp = new AAIEventProcessor();
			/*
			 * for (Map.Entry<String, String> entry :
			 * EventUtil.entitytypeTableMap.entrySet()) {
			 * System.out.println(entry.getKey() + "::" + entry.getValue()); }
			 */

			result = eventp.processAAIEvent(eventsJSON);
			if (!result) {
				System.out.println("Problem with processing json message.");
			}

		} else if (type.equalsIgnoreCase("GENERATE_AAI_EVENT_FROM_DB")) {
			String entityType = args.length > 1 ? args[1] : "";
			ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
			switch (entityType) {
			case Constants.PSERVER_ENTITY:
				configHandler.generateAAIEvents(Constants.PSERVER_ENTITY);
				break;
			case Constants.PNF_ENTITY:
				configHandler.generateAAIEvents(Constants.PNF_ENTITY);
				break;
			case Constants.GENERIC_VNF_ENTITY:
				configHandler.generateAAIEvents(Constants.GENERIC_VNF_ENTITY);
				break;
			case Constants.VCE_ENTITY:
				configHandler.generateAAIEvents(Constants.VCE_ENTITY);
				break;
			case Constants.VNFC_ENTITY:
				configHandler.generateAAIEvents(Constants.VNFC_ENTITY);
				break;
			case Constants.VSERVER_ENTITY:
				configHandler.generateAAIEvents(Constants.VSERVER_ENTITY);
				break;
			default:
				break;
			}
		} else if (type.equalsIgnoreCase("SEND_FEED_VETL")) {
			String datetimestamp = getCurrentTimestamp();
			datetimestamp = args.length > 1 ? args[1] : datetimestamp;
			EventUtil eventUtil = new EventUtil();
			eventUtil.sendRealtimeFeedsToVETL(datetimestamp);
		} else if (type.equalsIgnoreCase("LOAD_STATIC_EVENTS")) {
			System.out.println("Inside Load Static Events block.");

			ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
			configHandler.generateStaticDcaeEventDB();
		} else if (type.equalsIgnoreCase("NARAD_INIT_PNFS")) {
			// Full Sync
			EventProcessor eventp = new EventProcessor();
			try {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_FULL_SYNC_DEBUG);
				String url = "https://narad-conexus-prod.ecomp.cci.att.com:8443/narad/v2";
				url = args.length > 1 ? args[1] : url;
				String datetimestamp = getCurrentTimestamp();
				result = eventp.initNaradPnfs(url, datetimestamp);
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_FINISHED_FULL_SYNC_DEBUG);
			} catch (Exception e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXCEPTION_ERROR,
						e.getMessage());
				e.printStackTrace();
			}
		} else if (type.equalsIgnoreCase("GENERATE_NARAD_EVENT_FROM_DB")) {
			ConfigurationFileHandlerNarad configHandler = new ConfigurationFileHandlerNarad();
			configHandler.generateNaradEvents(Constants.PNF_ENTITY);
		} else if (type.equalsIgnoreCase("SYNC")) {
			// Full Sync
			// EventProcessor eventp = new EventProcessor();
			try {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_FULL_SYNC_DEBUG);
				String datetimestamp = getCurrentTimestamp();
				datetimestamp = args.length > 1 ? args[1] : datetimestamp;
				EventUtil.SEEDINGMANAGERDATETIMESTAMP = datetimestamp;

				EventUtil eventU = new EventUtil();
				eventU.sendFullSyncFeedsToVETL(datetimestamp);
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_FINISHED_FULL_SYNC_DEBUG);
			} catch (Exception e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXCEPTION_ERROR,
						e.getMessage());
				e.printStackTrace();
			}
		} else if (type.equalsIgnoreCase("SEEDREDISCOVERY")) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_DMAAPMRSUB_DEBUG);
			SeedingManagerDmaapMRSub dmaaps = new SeedingManagerDmaapMRSub();
			Thread dmaapThread = new Thread(dmaaps);
			dmaapThread.start();

			try {

				dmaapThread.join();

			} catch (InterruptedException e) {
				// e.printStackTrace();
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_DMAAPMR_EXCEPTION_ERROR,
						e.getMessage());
			}
			dmaaps.shutdownExecService();
		} else if (type.equalsIgnoreCase("NARADEVENT")) {
			System.out.println("Inside NARAD EVENT block.");
			String callConfigHandler = args.length > 1 ? args[1] : "No";
			String filename = args.length > 2 ? args[2] : "/opt/app/vcc/temp_config/naradevent.json";
			String configDir = System.getenv("DTI_CONFIG");
			if (configDir == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
				return;
			}

			FileInputStream dtiStream = null;
			FileInputStream fis = null;
			String eventsJSON = "";
			try {
				dtiStream = new FileInputStream(
						ValidationData.cleanPathString(Util.safeFileName(configDir) + "/dti.properties"));
				dtiProps.load(dtiStream);

				fis = new FileInputStream(ValidationData.cleanPathString(filename));
				eventsJSON = IOUtils.toString(fis, "UTF-8");
				System.out.println(eventsJSON);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				closeFileInputStream(dtiStream);
				closeFileInputStream(fis);
			}

			ConfigurationFileHandlerNarad configHandler = new ConfigurationFileHandlerNarad();
			System.out.println("This is naradevent");
			if ("Yes".equalsIgnoreCase(callConfigHandler)) {
				try {
					configHandler.getNARADUpdates(eventsJSON);
				} catch (DTIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {

				EventProcessor eventp = new EventProcessor();
				/*
				 * for (Map.Entry<String, String> entry :
				 * EventUtil.entitytypeTableMap.entrySet()) {
				 * System.out.println(entry.getKey() + "::" + entry.getValue());
				 * }
				 */

				result = eventp.processUEBMsg(eventsJSON);
				if (!result) {
					System.out.println("Problem with processing json message.");
				}
			}
		} else if (type.equalsIgnoreCase("NARAD")) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_DMAAPMRSUB_DEBUG);
			String updateDB = args.length > 1 ? args[1] : "No";
			NARADDmaapMRSub dmaaps = new NARADDmaapMRSub(updateDB);
			Thread dmaapThread = new Thread(dmaaps);
			dmaapThread.start();

			try {

				dmaapThread.join();

			} catch (InterruptedException e) {
				// e.printStackTrace();
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_DMAAPMR_EXCEPTION_ERROR,
						e.getMessage());
			}
			dmaaps.shutdownExecService();
		} else {
			if (EventUtil.AAICLIENT.equalsIgnoreCase("dmaap")) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_DMAAPMRSUB_DEBUG);
				String updateDB = args.length > 1 ? args[1] : "No";
				DmaapMRSub dmaaps = new DmaapMRSub(updateDB);
				Thread dmaapThread = new Thread(dmaaps);
				dmaapThread.start();

				try {

					dmaapThread.join();

				} catch (InterruptedException e) {
					// e.printStackTrace();
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_DMAAPMR_EXCEPTION_ERROR,
							e.getMessage());
				}
				dmaaps.shutdownExecService();
			} else if (EventUtil.AAICLIENT.equalsIgnoreCase("ueb")) {
				ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_START_UEBSUB_DEBUG);
				UEBSubscriber uebs = new UEBSubscriber();
				Thread uebThread = new Thread(uebs);
				uebThread.start();

				try {

					uebThread.join();

				} catch (InterruptedException e) {
					// e.printStackTrace();
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_UEB_EXCEPTION_ERROR,
							e.getMessage());
				}
				uebs.shutdownExecService();
			} else {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EVENT_SUB_ERROR);
				System.exit(1);

			}
		}
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_EXIT_DEBUG);
		// System.exit(0);

	}

	public static void closeFileInputStream(FileInputStream fis) {
		if (fis != null) {
			try {
				fis.close();
			} catch (IOException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_CONFIG_FILE_HANDLER_CONFIG_NOT_SET);
			}
		}
	}

	public static String getCurrentTimestamp() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}
