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

package com.att.vcc.configmanager.watcher;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.util.Arrays;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit.*;

import com.att.vcc.logger.VccLogger;
import com.att.vcc.utils.JobUtilInterface;
import com.att.vcc.utils.JobUtil;
import com.att.vcc.utils.*;

public class FileMonitor
{
	private static VccLogger logger = new VccLogger("FileMonitor");
	private static String className = "FileMonitor";
	
	// static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private FileMonitorProp monProp = new FileMonitorProp();
	
	private ScheduledExecutorService scheduler = null; 
	//private ShutdownHandler shutdownHandler = null;
	
	
	// Start FileMonitor as daemon, check the /dtidata/process dir every 30 seconds
	public void run()
	{
		String methodName = "run";
		logger.debug(className, methodName, "BEGIN");
				
		JobScheduler jobScheduler = new JobScheduler();
		
		// interval in Seconds
		int interval = Integer.parseInt(monProp.getValue("RUN_INTERVAL"));		
				
		//long collectionPeriod = interval * 60 * 1000;  // in milliseconds
		long collectionPeriod = interval * 1000;  		// in milliseconds
		long now, initialDelay;
		
		scheduler = Executors.newScheduledThreadPool(1);
		now = System.currentTimeMillis();	  // milliseconds  
		initialDelay = collectionPeriod - (now % collectionPeriod);
	    logger.debug(className, methodName, "RUN_INTERVAL="+interval+", initialDelay="+initialDelay);
	    
	    // Start the collection on the interval boundary
	    // Valid Values from java.util.concurrent.TimeUnit::DAYS HOURS MICROSECONDS MILLISECONDS MINUTES NANOSECONDS SECONDS 
		scheduler.scheduleAtFixedRate(jobScheduler, initialDelay, collectionPeriod, TimeUnit.MILLISECONDS);
		//shutdownHandler.addShutdownListener(jobScheduler);
		
		logger.debug(className, methodName, "END");
	}
	
	/**
	public void addShutdownHandler(ShutdownHandler sh)
	{
		this.shutdownHandler = sh;
	}
	
	public void shutdownProcess()
	{
		String methodName = "shutdownProcess";
		logger.info(className, methodName, "In shutdownProcess()...");

		try
		{
			if (scheduler != null)
			{
				scheduler.shutdown();

				if (!scheduler.isTerminated())
				{
					scheduler.awaitTermination(60, TimeUnit.SECONDS);
				}
			}
		} catch (Exception ignore)
		{
			logger.info(className, methodName, "Catch Exception::"+ignore.toString());
		}
		
		logger.info(className, methodName, "Exiting ...");
	}
	**/
	
	public static void main(String[] args)
	{
		FileMonitor monitor = new FileMonitor();
		
		logger.info(className, "main", "Start FileMonitor...");
		/**
	    ShutdownHandler sh = new ShutdownHandler(monitor);
	    Runtime.getRuntime().addShutdownHook(sh);
	    monitor.addShutdownHandler(sh);
	    **/
		try
		{
			monitor.run();
			
		} catch (Exception ex) {
			logger.error(className,"main","Exception Caught","",ex);
		}
	}
}

