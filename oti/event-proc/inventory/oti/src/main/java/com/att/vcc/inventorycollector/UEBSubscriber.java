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
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.att.vcc.inventorycollector.data.EntityData;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaConsumer;

public class UEBSubscriber implements Runnable
{
	private static CambriaConsumer cc;
	private static String collectorEvents;
	EventProcessor eventp;
	ExecutorService executorService;
	
	private static EcompLogger ecompLogger;
	private ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
	static {
		EcompComponent.initialize("dcae");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}
	
	public UEBSubscriber()
	{
		ecompLogger.debug(" UEBSubscriber constructor");
		collectorEvents=EventUtil.COLLECTOREVENTS;
		eventp = new EventProcessor();
		executorService = Executors.newFixedThreadPool(10);
	}
	public void shutdownExecService()
	{
		executorService.shutdown();
	}
	
	//"{ \"class\":\"And\", \"filters\":[ {\"class\":\"Equals\",\"field\":\"event-header.entity-type\",\"value\":\"generic-vnf\"}, {\"class\":\"RegEx\",\"field\":\"event-header.domain\",\"value\":\".*INT1.*\"} ] }")
	public void run() {
			
    	ecompLogger.debug("Staring UEBSubscriber");
    			
    	try {
    		final CambriaConsumer cc = new CambriaClientBuilders.ConsumerBuilder()
				.usingHosts (EventUtil.UEBURL)
				.authenticatedBy (EventUtil.UEBAPIKEY,EventUtil.UEBAPISECRET)
				.onTopic ("AAI-EVENT")
				.knownAs ( EventUtil.UEBGROUP, EventUtil.UEBID )
				.waitAtServer (Integer.parseInt(EventUtil.UEBTIMEOUT_MS))
				.receivingAtMost (Integer.parseInt(EventUtil.UEBLIMIT))
				.withServerSideFilter(EventUtil.UEBFILTER)
				.build();
				
    		if(cc==null)
    		{
    			ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_CAMBRIA_CONSUMER_INIT_FAILURE);
    			return;
    		}
    	
    		while ( true )
    		{
    			try {
    				for ( String msg : cc.fetch () )
    				{
    					ecompLogger.debug(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_RECEIVED_AAI_EVENT , msg);
    					//InventoryCollectorUtil.storeEvents(msg, outputDir);
    					System.out.println(msg);
					
    					String entityType="";
    					try {
    						entityType = getEntityType(msg);
    					} catch (org.json.JSONException e) {
    						//e.printStackTrace();
    						ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_RECEIVED_EVENT_ISSUE , "JSONException" , e.getMessage());
    					}
    					if(entityType.isEmpty())
    					{
    						ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_RECEIVED_EVENT_ISSUE , "entityType is empty" , msg);
    					}
					
    					//Call collector component with UEB message if the event is of type Vce,Vpe,Vserver
    					String [] arr=collectorEvents.split(",");
    					List<String> lCollEvents=Arrays.asList(arr);
    					if(lCollEvents.contains(entityType))
    					{
    						if((Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_COLLECTOR_EVENTS) && Constants.DOMAIN2.equalsIgnoreCase(EventUtil.DCAEENV)) ||
								Constants.DOMAIN1.equalsIgnoreCase(EventUtil.DCAEENV)) {
    							//Call collector component
    							final EntityData entityData= new EntityData("", entityType, "", "",msg);
    							final String message = msg;
    							executorService.execute(new Runnable() {
    								public void run() {
    									System.out.println("Asynchronous collector task for event");
    									ecompLogger.debug("Calling collector component, entityType:" + entityData );
    									try {
    										configHandler.getAAIUpdates(message);
    									} catch (DTIException e) {
    										//e.printStackTrace();
    										ecompLogger.error(inventoryCollectorMessageEnum.DTI_EXCEPTION, e.getMessage());
    									}
    								}
    							});
    						} else {
    							ecompLogger.debug(inventoryCollectorMessageEnum.DTI_POST_ORCH_DISABLED);
    						}
    					}
					
    					if(EventUtil.DCAEENV.equalsIgnoreCase("D2"))
    					{
    						//Call Event Processor and DB adapter
    						Boolean result=eventp.processUEBMsg(msg);
    						if(!result)
    						{
    							ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_PROCESSING_FAILURE, msg);
    						}
    					}
					
					
    				}
    			} catch (IOException e) {
    				ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_PROCESSING_FAILURE, e.getMessage());
    				//e.printStackTrace();
    			}

    		}
    	} catch (MalformedURLException | GeneralSecurityException e) {
    		ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_PROCESSING_FAILURE, e.getMessage());
    		e.printStackTrace();
    	}
	}
	
	public String getEntityType(String uebMsg) throws org.json.JSONException
	{
		ecompLogger.debug("Entering getEntityType");
		JSONObject objMsg = new JSONObject(uebMsg);
		
		if(objMsg.isNull("event-header"))
		{
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_RECEIVED_EVENT_ISSUE , "eventheader is empty" , uebMsg);
			return "";
		}
		else
		{
			JSONObject  uebHeader= objMsg.getJSONObject("event-header");
			
			if(uebHeader.isNull("entity-type"))
			{
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_UEB_SUBSCRIBER_RECEIVED_EVENT_ISSUE , "entityType is empty" , uebMsg);
				return "";
			}
			String entityType=uebHeader.getString("entity-type");
			return entityType;

		}
	}
}
