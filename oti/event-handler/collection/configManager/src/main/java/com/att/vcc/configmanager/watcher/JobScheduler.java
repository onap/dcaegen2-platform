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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit.*;

import com.att.vcc.logger.VccLogger;
import com.att.vcc.utils.*;


public class JobScheduler implements Runnable
{
	private static VccLogger logger = new VccLogger("FileMonitor");
	private static String className = "JobScheduler";
	
	private IFileChangeProcessor eventProcessor = null;
	
	public JobScheduler()
	{
		this.eventProcessor = new DtiEventChangeProcessor();
	}
	
	public void run()
	{
		String methodName  = "run";
		
		logger.debug(className, methodName, "BEGIN");
		
		ArrayList<File> fileList = null;
		
		try {
			String dtiEventDir = System.getenv("DTI_DATA_DIR") + "/process";
			File eventDir = new File(VccUtils.safeFileName(dtiEventDir));
			
			File[] files = eventDir.listFiles();
			Arrays.sort(files);
			
			if (files != null && files.length > 0)
			{
				for (File oneFile : files)
				{
					if (oneFile.isFile())
					{
						eventProcessor.processEvent(oneFile); 
						logger.info(className,  methodName,  "Process DTI event::"+ oneFile.getName());
					}
				}
			}
		} catch (Exception e) {
			logger.error(className, methodName, "Exception", "", e);
		} 
		
		logger.debug(className, methodName, "END");
	}
}