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

package com.att.vcc.common.audit;
// ************************************************************************
// *
// * File Name: VccAudit.java
// * Created: 12/13/2016   Release: 17.02  Project: 293908 By: Hong Wang
// ************************************************************************

import java.io.*;
import java.text.*;
import java.util.*;
import java.nio.file.*;
import com.att.vcc.utils.JobUtil;

public class VccAudit
{
	private String collectionEntity=null;
	private long collectionIntervalStartTime=-1;
	private String entityType=null;
	private String collectionAddress="NA";
	private String collectionDirectory="NA";
	private String remoteFileName="NA";
	//private String entityStatus="SUCC";
	private String entityStatus="0";
	private long collectionStartTime=-1;
	private int errorId=-1;
	private String additionalErrorText=null;
	private String collectorVmName=null;
	private String vccTaskId=null;
	private String provStatus="NA";
	private String inMaint="NA";
	private int interval=300;
	private String region = "NA";
	private String tenant = "NA";
	private long dataCollected = -1;
	
	private String vccHome = null;
	private String timestamp = "YYYYMMDDHHmmss";
	private String collectionType = null;
	private String vccVmName = null;
	private String taskId = null;
	private String neName = null;
	private String collInterval = null;
	private String outDir = null;
	private String outFile = null;
	
	private String service = "";
	private String nfNamingCode = "";
	
	public VccAudit(String vccHome, String timestamp, String collectionType, String vccVmName)
	{
		this.vccHome = vccHome;
		this.timestamp = timestamp;
		this.collectionType = collectionType;
		this.vccVmName = vccVmName;
		this.taskId = "DUMMY";
		this.neName = "test0001v";
		this.collInterval = "xMIN";
		this.service = "DUMMY";
		this.nfNamingCode = "DUMMY";
	}

	public VccAudit(String vccHome, String timestamp, String collectionType, String vccVmName,String taskId, String neName, String collInterval)
	{
		this.vccHome = vccHome;
		this.timestamp = timestamp;
		this.collectionType = collectionType;
		this.vccVmName = vccVmName;
		this.taskId = taskId;
	    this.neName = neName;
	    this.collInterval = collInterval;
	}

	public VccAudit(String vccHome, String timestamp, String collectionType, String vccVmName,String taskId, String neName, String collInterval, String service, String nfNamingcode)
	{
		this.vccHome = vccHome;
		this.timestamp = timestamp;
		this.collectionType = collectionType;
		this.vccVmName = vccVmName;
		this.taskId = taskId;
	    this.neName = neName;
	    this.collInterval = collInterval;
	    this.service = service;
	    this.nfNamingCode = nfNamingCode;
	}
	
	public synchronized void doWrite() throws Exception
	{
		this.outDir = vccHome+"/data/output/vccAudit/"+collectionType+"/"+taskId+"/"+neName;
		this.outFile = "VCCAUDIT_"+timestamp+"_"+collectionType+"_"+taskId+"_"+neName+"_"+collInterval+"_"+vccVmName+".txt";
		FileOutputStream fOutStream = null;
		OutputStreamWriter outStreamWriter = null;
		BufferedWriter file = null;

		File auditDir = new File(outDir);
		if (!auditDir.exists())
			auditDir.mkdirs();

		try
		{
			String fName = outDir+"/"+outFile; 
			fOutStream = new FileOutputStream(fName, true);
			outStreamWriter = new OutputStreamWriter(fOutStream);
			file = new BufferedWriter(outStreamWriter);

			LineNumberReader lnr = new LineNumberReader(new FileReader(new File(fName)));
			lnr.skip(Long.MAX_VALUE);					
			if (lnr.getLineNumber()==0)
			{
				file.write(this.getHeader());
				file.newLine();
			}
			file.write(this.toString());
			file.newLine();
			lnr.close();
		}
		catch (Exception e) { throw e; }

		try
		{
			if (file != null) { file.close(); }
		}
		catch (Exception e) { throw e; }
	}
		
	public synchronized void copyToPublisher() throws Exception
	{
		try
		{
			String sourcePath = this.outDir;
			String destinationPath = vccHome+"/data/input/publisher";
			
			File destinationDir = new File(destinationPath);
			if (!destinationDir.exists())
				destinationDir.mkdirs();
				
			if (this.outFile != null)
			{
				Files.copy(Paths.get(sourcePath+"/"+outFile), new FileOutputStream(destinationPath+"/"+outFile));
				
				JobUtil jobUtil = new JobUtil();
				File vccAuditFile = new File(sourcePath+"/"+outFile);
				jobUtil.moveToArchiveRW(vccAuditFile);
			}
		}
		catch (Exception e) { throw e; }
	}	

	public String getCollectionEntity()
	{
		return collectionEntity;
	}
	public long getCollectionIntervalStartTime()
	{
		return collectionIntervalStartTime;
	}
	public String getEntityType()
	{
		return entityType;
	}
	public String getCollectionAddress()
	{
		return collectionAddress;
	}
	public String getCollectionDirectory()
	{
		return collectionDirectory;
	}
	public String getRemoteFileName()
	{
		return remoteFileName;
	}
	public String getEntityStatus()
	{
		return entityStatus;
	}
	public long getCollectionStartTime()
	{
		return collectionStartTime;
	}
	public int getErrorId()
	{
		return errorId;
	}
	public String getAdditionalErrorText()
	{
		return additionalErrorText;
	}
	public String getCollectorVmName()
	{
		return collectorVmName;
	}
	public String getVccTaskId()
	{
		return vccTaskId;
	}
	public String getProvStatus()
	{
		return provStatus;
	}
	public String getInMaint()
	{
		return inMaint;
	}
	public int getInterval()
	{
		return interval;
	}
	public String getService()
	{
		return service;
	}
	public String getNfNamingCode()
	{
		return nfNamingCode;
	}
	public String getRegion()
	{
		return region;
	}
	public String getTenant()
	{
		return tenant;
	}
	public long getDataCollected()
	{
		return dataCollected;
	}
	public void setCollectionEntity(String aStr)
	{	
		collectionEntity=aStr;
	}
	public void setCollectionIntervalStartTime(long aLong)
	{
		collectionIntervalStartTime=aLong;
	}
	public void setEntityType(String aStr)
	{
		entityType=aStr;
	}
	public void setCollectionAddress(String aStr)
	{
		collectionAddress=aStr;
	}
	public void setCollectionDirectory(String aStr)
	{
		collectionDirectory=aStr;
	}
	public void setRemoteFileName(String aStr)
	{
		remoteFileName=aStr;
	}
	public void setEntityStatus(String aStr)
	{
		//entityStatus=aStr;
		if ("FAIL".equals(aStr))
			entityStatus="1";
		else if ("SUCC".equals(aStr))
			entityStatus="0";
	}
	public void setCollectionStartTime(long aLong)
	{
		collectionStartTime=aLong;
	}
	public void setErrorId(int aInt)
	{
		errorId=aInt;
	}
	public void setAdditionalErrorText(String aStr)
	{
		additionalErrorText=aStr;
	}
	public void setVmName(String aStr)
	{
		collectorVmName=aStr;
	}
	public void setVccTaskId(String aStr)
	{	
		vccTaskId=aStr;
	}
	public void setProvStatus(String aStr)
	{
		provStatus=aStr;
	}
	public void setInMaint(String aStr)
	{
		//inMaint=aStr;
		if (aStr.toUpperCase().startsWith("T"))
			inMaint="0";	// inMaint = True
		else
			inMaint="1";	// inMaint = False
	}
	public void setInterval(int aInt)
	{
		interval=aInt;
	}
	public void setService(String aStr)
	{
		service=aStr;
	}
	public void setNfNamingCode(String aStr)
	{
		nfNamingCode=aStr;
	}
	public void setRegion(String aStr)
	{
		region=aStr;
	}
	public void setTenant(String aStr)
	{
		tenant=aStr;
	}
	public void setDataCollected(long aLong)
	{
		dataCollected=aLong;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(collectionEntity);
		sb.append('|');
		if (collectionIntervalStartTime > 0)
			sb.append(collectionIntervalStartTime);
		sb.append('|');
		if (collectionStartTime > 0)
			sb.append(collectionStartTime);
		sb.append('|');
		if (entityType != null)
			sb.append(entityType);
		sb.append('|');
		if (collectionAddress != null)
			sb.append(collectionAddress);
		sb.append('|');
		if (collectionDirectory != null)
			sb.append(collectionDirectory);
		sb.append('|');
		if (remoteFileName != null)
			sb.append(remoteFileName);
		sb.append('|');
		if (region != null)
			sb.append(region);
		sb.append('|');
		if (tenant != null)
			sb.append(tenant);
		sb.append('|');
		if (entityStatus != null)
			sb.append(entityStatus);
		sb.append('|');
		if (dataCollected>0)
			sb.append(dataCollected);
		sb.append('|');
		if (errorId > 0)
			sb.append(errorId);
		sb.append('|');
		if (additionalErrorText != null)
			sb.append(additionalErrorText);
		sb.append('|');
		if (collectorVmName != null)
			sb.append(collectorVmName);
		sb.append('|');
		if (vccTaskId != null)
			sb.append(vccTaskId);
		sb.append('|');
		if (provStatus != null)
			sb.append(provStatus);
		sb.append('|');
		if (inMaint != null)
			sb.append(inMaint);
		sb.append('|');
		if (interval>0)
			sb.append(interval);
		sb.append("|");
		sb.append(service);
		sb.append("|");
		sb.append(nfNamingCode);

		return sb.toString();
	}

	public static String getHeader()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("#CollectionEntity|");
		sb.append("CollectionIntervalStartTime|");
		sb.append("CollectionStartTime|");
		sb.append("EntityType|");
		sb.append("CollectionAddress|");
		sb.append("CollectionDirectory|");
		sb.append("RemoteFileName|");
		sb.append("Region|");
		sb.append("Tenant|");
		sb.append("EntityStatus|");
		sb.append("DataCollected|");
		sb.append("ErrorId|");
		sb.append("AdditionalErrorText|");
		sb.append("collectorVmName|");
		sb.append("vccTaskId|");
		sb.append("provStatus|");
		sb.append("inMaint|");
		sb.append("interval|");
		sb.append("service|");
		sb.append("nfNamingCode");

		return sb.toString();
	}
}
