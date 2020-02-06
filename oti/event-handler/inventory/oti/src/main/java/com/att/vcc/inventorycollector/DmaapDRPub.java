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

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.dcae.DmpClient.DmpClientConfigurations;
import com.att.dcae.DmpClient.DmpClientFactory;
import com.att.dcae.DmpClient.DmpClientWriter;
import com.att.dcae.DmpClient.DmpConfigurationException;
import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;

/**
 * Provides ability to ingest messages to a DMaaP DR feed.
 */
public class DmaapDRPub{
	DmpClientWriter client=null;
	private static EcompLogger ecompLogger;

	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}
	
    public DmaapDRPub()
    {
    	ecompLogger.debug("In DmaapDRPub");
    	
    	if(Constants.TRUE.equalsIgnoreCase(EventUtil.ENABLE_DMAAP_PUBLISH)) {
    		DmpClientConfigurations.setConfigurationFilePath(EventUtil.DMAAPCONFPATH);
    		try {
    			client = DmpClientFactory.buildDmpClientWriter("dti");

    		} catch (DmpConfigurationException e) {
    			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DMAAP_PUB_CONFIG_EXCEPTION, e.getMessage());
    		}
    	}
    }
    
    public void publishFile(String fileName,String metaData)
    {
    	//debugLog.debug("In publishFile, fileName:"+fileName);
    	client.writeFromFile(fileName, metaData);
    	//System.out.println(fileName +" published to Dmaap");
    	ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DMAAP_PUB_SUCCESS , fileName);
    }
    
    public void closeDmaapClient()
    {
    	//debugLog.debug("In closeDmaapClient");
    	client.close(true);
    	//debugLog.debug("Finished closeDmaapClient");
    }
    public static void main(String[] args) {
        //debugLog.debug("Starting DmaapDRPub Main");
		String datetimestamp=args[0];
	    EventUtil eventUtil= new EventUtil();
	    EventUtil.loadConfigs();
	    eventUtil.sendFullSyncFeedsToVETL(datetimestamp);
	    //debugLog.debug("Finished DmaapDRPub Main");

    }
}