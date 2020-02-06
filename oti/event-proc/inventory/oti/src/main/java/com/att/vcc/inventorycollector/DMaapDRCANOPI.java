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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.util.DTIException;


public class DMaapDRCANOPI {

	private ConfigurationFileHandler configHandler = new ConfigurationFileHandler();
	
	
	private static EcompLogger ecompLogger;
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}
	
	public static void main(String[] args) {

		boolean result = EventUtil.loadConfigs();
		if (!result) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_COLLECTOR_LOAD_CONFIG_ERROR);
			System.exit(1);
		}
		
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		DMaapDRCANOPI dmaapDRCANOPI=new DMaapDRCANOPI();
		String input=args.length > 0 ? args[0] : "";
		if(input.equalsIgnoreCase("Samsung")){
			dmaapDRCANOPI.generateSamsungDCAEEvent();
		}else if(input.equalsIgnoreCase("D2MSN")){
			dmaapDRCANOPI.generateD2MSNDCAEEvent();
		}else if (input.equalsIgnoreCase("PCRF")) {
            dmaapDRCANOPI.generatePCRFDCAEEvent();
        }
	}
	
	public void generateSamsungDCAEEvent(){
		
		
		File folder = new File(EventUtil.SAMSUNG_FILE_FOLDER);
				
		Map<String,String> fileList=new HashMap<String,String>();
		fileList = EventUtil.listFilesForFolder(folder);
	
		if ( fileList == null || fileList.isEmpty() ) {
			ecompLogger.debug("Samsung input folder is empty");
			return;			
		}
		
		for(Map.Entry<String,String> entry:fileList.entrySet()){
			String samsungFilePath = entry.getValue();
			String samsungFileName=entry.getKey();
			
			if(!EventUtil.isSamsungFile(samsungFileName)){
				EventUtil.moveFile(entry.getKey(),samsungFilePath);
				continue;
			}
			
			String eventString=EventUtil.unZipIt(samsungFilePath);
			try {				
				configHandler.getAAIUpdates(eventString);
				if (EventUtil.moveFile(entry.getKey(),samsungFilePath)) {
					ecompLogger.debug("Samsung Feed file : " + samsungFilePath + " moved to processed directory");
					} else {
					  ecompLogger.debug("Problem moving Samsung Feed file : " + samsungFilePath + " to processed directory");
					}
			} catch (DTIException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
						samsungFilePath);
			}
		}
				
	}
	
	public void generateD2MSNDCAEEvent(){
						
			File folder = new File(EventUtil.D2MSN_FILE_FOLDER);					
			Map<String,String> fileList=new HashMap<String,String>();
			fileList = EventUtil.listFilesForFolder(folder);
		
			if ( fileList == null || fileList.isEmpty() ) {
				ecompLogger.debug("D2MSN input folder is empty");
				return;			
			}
			
			for(Map.Entry<String,String> entry:fileList.entrySet()){
				String d2MSNFilePath = entry.getValue();
				String d2MSNFileName=entry.getKey();
				
				if(!EventUtil.isD2MSNFile(d2MSNFileName)){
					EventUtil.moveFile(entry.getKey(),d2MSNFilePath);
					continue;
				}
				
				String eventString=EventUtil.xmlToJson(d2MSNFilePath);
				try {				
					configHandler.getAAIUpdates(eventString);
					if (EventUtil.moveFile(entry.getKey(),d2MSNFilePath)) {
						ecompLogger.debug("D2MSN Feed file : " + d2MSNFilePath + " moved to processed directory");
						} else {
						  ecompLogger.debug("Problem moving D2MSN Feed file : " + d2MSNFilePath + " to processed directory");
						}
				} catch (DTIException e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
							d2MSNFilePath);
				}
			}
					
	  }
	
	public void generatePCRFDCAEEvent() {

		final File folder = new File(EventUtil.SAMSUNG_FILE_FOLDER);
        Map<String, String> fileList = new HashMap<String, String>();
        fileList = (Map<String, String>)EventUtil.listFilesForFolder(folder);
        
        if (fileList == null || fileList.isEmpty()) {
        	ecompLogger.debug("PCRF input folder is empty");
            return;
        }
        for (final Map.Entry<String, String> entry : fileList.entrySet()) {
            final String pcrfFilePath = entry.getValue();
            final String pcrfFileName = entry.getKey();
            if (!EventUtil.isPCRFFile(pcrfFileName)) {
                EventUtil.moveFile((String)entry.getKey(), pcrfFilePath);
            }
            else {
                final String eventString = EventUtil.xmlToString(pcrfFilePath);
                try {
                    this.configHandler.getAAIUpdates(eventString);
                    if (EventUtil.moveFile((String)entry.getKey(), pcrfFilePath)) {
                    	ecompLogger.debug("PCRF Feed file : " + pcrfFilePath + " moved to processed directory");
                    }
                    else {
                    	ecompLogger.debug("Problem moving PCRF Feed file : " + pcrfFilePath + " to processed directory");
                    }
                }
                catch (DTIException e) {
                	ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_UTIL_IOEXCEPTION_ERROR, e.toString(),
                			pcrfFilePath);
                }
            }
        }
    }
	    
}
