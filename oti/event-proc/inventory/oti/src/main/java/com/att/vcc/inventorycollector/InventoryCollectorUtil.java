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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONObject;

import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;

public class InventoryCollectorUtil {

	private static EcompLogger ecompLogger;
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	public static void storeEvents(String uebMsg,String outputDir)
	{
		//debugLog.debug("Entering storeEvents");
		JSONObject objMsg = new JSONObject(uebMsg);
		
		if(objMsg.isNull("event-header"))
		{
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_UTIL_ERROR, "No EVENT Header in message" , uebMsg);
			return;
		}
		else
		{
			JSONObject uebHeader= objMsg.getJSONObject("event-header");
			
			if(uebHeader.isNull("id"))
			{
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_UTIL_ERROR, "No ID in EVENT Header" , uebMsg);
				return;
			}
			String id=uebHeader.getString("id");
			PrintWriter writer;
			try {
				
				String fileName=outputDir+"//."+id+".json";
	            		writer = new PrintWriter(fileName, "UTF-8");
				writer.println(uebMsg);
				writer.close();
				
				Path movefrom = FileSystems.getDefault().getPath(fileName);
			    Path target = FileSystems.getDefault().getPath(outputDir+"//"+id+".json");
			     
			    try {
			        Files.move(movefrom, target, StandardCopyOption.ATOMIC_MOVE);
			    } catch (IOException e) {
			        System.err.println(e);
			    }
			} catch (FileNotFoundException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_UTIL_ERROR , "FileNotFoundException" , e.getMessage());
				
			} catch (UnsupportedEncodingException e) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_INVENTORY_UTIL_ERROR, "UnsupportedEncodingException" , e.getMessage());
				
			}
		}
		//ecompLogger.info("Leaving storeEvents");
	}
}
