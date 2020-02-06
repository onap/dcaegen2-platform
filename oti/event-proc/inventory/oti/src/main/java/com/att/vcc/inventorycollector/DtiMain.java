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
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.logger.*;
import com.att.vcc.logger.messages.*;
import com.att.vcc.utils.*;


public class DtiMain {
	private static final String className="DtiMain";
	
	public static String collectionTime;
	public static String cycleFileTime;
	public static String cycleStartTime;
	
    private static String OS = System.getProperty("os.name").toLowerCase();
 
	private static EcompLogger ecompLogger;
	
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	private static VccLogger logger = new VccLogger("dti");

	public static void main(String[] args) {
		
		String methodName = "DtiMain Main";

		DtiMain obj = new DtiMain();
		int total_sub_sites = 1;
		
		int cycle = 0;
		logger.debug(className, methodName, "Staring DtiMain main ");

	
		try {
			long interval = 60;
			//60000 is one minutes
			long collectionPeriod = interval * 1000;
			
			long now = System.currentTimeMillis();
			long cycleStart = now - (now % collectionPeriod)+collectionPeriod;
			long waittime = cycleStart - now;
			
			/*
			if (waittime < 10000)
			{
				Thread.sleep(waittime);
			}
			*/

			cycleFileTime = getTimeStamp(cycleStart, "yyyyMMddHHmmss");
			collectionTime = getCurrentTimeStamp(cycleStart);
			logger.debug(className, methodName, "Cycle : +" + cycle +"collectionTime is : " +collectionTime +" ,cycleFileTime is : " +cycleFileTime );
			
	
			for ( int i=0; i<total_sub_sites; i++ ) {
	
				try {
					obj.processNotifications(i);
				} catch (Exception e) {
					ecompLogger.error(inventoryCollectorMessageEnum.DTI_EXCEPTION);
					System.out.println("error message is ");
					e.printStackTrace();
				}
			
			} // end of for loop - go thru all sites
			
			long collectionEnd = System.currentTimeMillis();
			long elapsed_time = (collectionEnd-now)/100000;
			logger.debug(className, methodName, "cycle:"+ cycle + "duration for" +cycleFileTime+"is"+elapsed_time );
			
			long cycleEnd = collectionEnd - (collectionEnd % collectionPeriod)+collectionPeriod;
			long diff = cycleEnd - collectionEnd;
			
			System.out.println("DtiMain process diff = cycleEnd - collectionEnd=" + diff);
			
		
			Thread.sleep(diff);
		} catch (InterruptedException e) {
			
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EXCEPTION);
			System.out.println("error message is ");
			e.printStackTrace();
		}
				
	}
	

	/**
	 * There is no DMD required for NARAD project
	 * This method is for testing purpose only
	 * 
	 * @param sub_sites
	 */
	public void processNotifications(int sub_sites)
	{
		String methodName = "processNotifications";
		System.out.println(methodName);
		logger.debug(className, methodName, methodName);
		
	}

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
    
	public static String getTimeStamp(long epochTime, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date(epochTime));
	}
	public static String getTimeStamp(long epochTime) {
		return getTimeStamp(epochTime, "yyyy-MM-dd'T'HH:mm:ss");
	}

	public static String getTimeStamp4Output(long epochTime) {
		return getTimeStamp(epochTime, "yyyyMMddHHmmss");
	}
	
	public static String getCurrentTimeStamp(long epochTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date(epochTime));
	}
	
	public static String getTimeStamp() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		Date now = new Date(); 
		return simpleDateFormat.format(now);
	}


}

