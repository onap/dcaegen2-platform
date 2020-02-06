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

package com.att.vcc.common.ha;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import com.att.vcc.logger.VccLogger;
//import common.utils.PecUtils;
import com.att.vcc.utils.*;

class HighAvailabilityProcess {


	public String current_machine = "";
	public String config_file = "";
	public static final String TILDADELIMITER = "~";
	public static final String NOT_ACTIVE_FLAG = "N";
	public static final String ACTIVE_FLAG = "Y";
	private String str;
	private PrintWriter config_file_output;
	private PrintWriter hacounter_file_output;
	RandomAccessFile rafpmOutPut;
	long filePointerOffSet;
	private String[] passedvalues;
	public static final short ALLPOS=-1;
	private static final String className="___HighAvailabilityProcess";
    private static final String classString= "["+className+" ]";
	private static String applName = "ha";
	private static VccLogger logger = new VccLogger(applName);
	static String errorID1="VCC_HA_E001";
	static String errorID2="VCC_HA_E002";
    private static boolean debuggOn = false;

	public static void main(String args[]) {
			if(args.length != 2) {
				logger.error(className,"MAIN","USAGE: HighAvailabilityProcess current_machine_name config_file",errorID1);
				System.exit(0);
			}
			java.util.Date mydate = new java.util.Date();
			long ms = mydate.getTime();
			ms = ms + (1000*60*60*24*30L);
			java.util.Date mydate2 = new java.util.Date(ms);
			String dateString;
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
			dateString = df.format(mydate);
			String current_machine = args[0];
			String config_file_str = args[1];
			String base_dir = System.getProperty("basedir");
			String status_file = System.getProperty("statusfile");
			logger.info(className,"MAIN","The status_file is :: " + status_file);
			File config_file = new File(config_file_str);
			if (!config_file.exists()) {
				logger.error(className,"MAIN","Pod Active Config file does not exist or not passed correctly!",errorID1);
				System.exit(0);
			}
			HighAvailabilityProcess haprocess;
			logger.info(className,"MAIN","The Machine this process is running on is  = " + current_machine);
			logger.info(className,"MAIN","The config file is " + config_file);
			haprocess = new HighAvailabilityProcess();
			haprocess.loadData(current_machine, config_file_str, base_dir, status_file);
			System.exit(0);
	}

	
	
	private void loadData(String current_machine, String config_file_str, String base_dir, String status_file) {
		String methodName = "loadData";
		try	{
			 
				HashMap<String, String> ha_counters = new HashMap<String, String>();
				//HashMap ha_counters = null;
				int counter_val = 0;
				
				java.util.Date mydate = new java.util.Date();
				String dateString;
				java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
				dateString = df.format(mydate);
				
				logger.debug(className,methodName,"Inside the LoadData Method!");
				String config_file_tmp = config_file_str + ".tmp";

				String ha_counter_str = base_dir + "/tmp/ha/ha_counter.txt";
				String ha_counter_tmp = base_dir + "/tmp/ha/ha_counter.txt.tmp";
				File file1 = new File(config_file_str);
				File file2 = new File(config_file_tmp);
				File file3 = new File(ha_counter_str);
				File file4 = new File(ha_counter_tmp);
				String config_file_inter = base_dir + "/config/common/." + file1.getName();
				File file5 = new File(config_file_inter);
				String vccNetworkName = "";
				String vccLogicalSystemName = "";

				boolean hasConfigFileChanged = false;
				boolean hasHaCounterFileChanges = false;

				/**	
				boolean success_flag;
				success_flag = file1.renameTo(file2);
				**/

				file2 = copyFile(config_file_str, config_file_tmp);


				config_file_output = new PrintWriter(new FileWriter(config_file_inter, true), true);
				config_file_output.flush();

				rafpmOutPut = new RandomAccessFile(status_file, "rw");
				filePointerOffSet = rafpmOutPut.length();
				rafpmOutPut.seek(filePointerOffSet);

			if (file3.exists()) {
					file3.renameTo(file4);
					//file4 = copyFile(ha_counter_str, ha_counter_tmp);
					
					hacounter_file_output = new PrintWriter(new FileWriter(ha_counter_str, true), true);
					hacounter_file_output.flush();

					BufferedReader actIn = new BufferedReader(new FileReader(ha_counter_tmp));
					String line;
					while ((line = actIn.readLine()) != null) {
						if (line.startsWith("#")) {
							hacounter_file_output.println(line);
							continue;
						}
						String[] actTokens = line.split("\\~");
						if (actTokens.length == 2)
						{
							ha_counters.put(actTokens[0],actTokens[1]);
						}
					}

				} else {
					hacounter_file_output = new PrintWriter(new FileWriter(ha_counter_str, true), true);
					hacounter_file_output.flush();
					hacounter_file_output.println("#HLC/LLC Server~counter value");
				}

				Vector tmpmachine_info = new Vector();

				String machine_info = "";
				String active_flag = "";
				String manual_override_flag = "";

				try{
					BufferedReader in;
					if (!file1.exists()) {
						logger.error(className,methodName,"No file is passed!!",errorID1);
					}
					in = new BufferedReader(new FileReader(file1));		

					while ( (str =in.readLine()) != null ) {
							passedvalues=null;
							if (str.startsWith("#")) {
								config_file_output.println(str);
								continue;
							}
								
							 if ( str.indexOf('~') != -1 ) {
							 passedvalues = ConvertStringToArrayOnToken(
									str, TILDADELIMITER,
									ALLPOS, true );
							}
							if(passedvalues == null) {
								logger.warn(className,methodName,"The config file is empty!!!");
							}

							if(passedvalues.length == 3) {
								for(int i=0;i<passedvalues.length;i++) {
									if(i==0) {
										vccLogicalSystemName = passedvalues[0];
										if("".equals(vccLogicalSystemName)) {
											logger.info(className,methodName,"The VLSN name is empty!!!");
										} 
									} else if(i==1) {
										active_flag = passedvalues[1];
										if("".equals(active_flag)) {
											logger.info(className,methodName,"The Active Flag is empty!!!");
										} 				
									} else if(i==2) {
										manual_override_flag = passedvalues[2];
										if("".equals(manual_override_flag)) {
											logger.info(className,methodName,"The Manual Over ride Flag is empty!!!");
										}
									}
								}
							}
							logger.debug(className,methodName,"The VLSN is ::: "+vccLogicalSystemName);
							vccNetworkName = VccUtils.getVccNetworkName(vccLogicalSystemName);
							logger.debug(className,methodName,"The VCC Network Name is ::: "+vccNetworkName);

							if (vccNetworkName != null && !vccNetworkName.equals(""))
							{
								machine_info = vccNetworkName.trim();
							}
							
							//String machine_info_trim = machine_info;
							String machine_info_trim = machine_info;
							//String machine_info_trim = "";
							int index = machine_info.indexOf(".");
							if (index != -1)
							{
								machine_info_trim = machine_info.substring(0,index);
							}
							if(machine_info_trim.equals(""))
							{
								machine_info_trim = machine_info;
							}
							
							//System.out.println("The machine Info Trim is ::: " + machine_info_trim);
							if (machine_info_trim != null && !current_machine.equals(machine_info_trim) && active_flag != null && manual_override_flag != null)
							{
								//Write the logic to ping the machine if its not the same machine.
								//System.out.println("INside the not null condition!");
								int success = 0;
								int failure = 0;
								for (int i=0;i<5;i++ )
								{
									try
										{
											int timeOut = 3000; // I recommend 3 seconds at least	
											boolean status = InetAddress.getByName(machine_info).isReachable(timeOut);
											if(status) {
												//System.out.println("Alive!") ;
												success++;
											 } else {
												logger.error(className,methodName,machine_info +"-Dead or echo port not responding",errorID1);
												failure++;
											 }		
										}
										catch (SocketTimeoutException soce)
										{
											logger.error(className,methodName,machine_info +"-Inside the SocketTimeoutException! and the exception is :: " + soce,errorID1);
											failure++;
										}
										catch (ConnectException ce)
										{
											logger.error(className,methodName,machine_info+"-Inside the ConnectException! and the exception is :: " + ce,errorID1);
											failure++;
										}
										catch (UnknownHostException uhe)
										{
											logger.error(className,methodName,machine_info+"-Inside the UnknownHostException! and the exception is :: " + uhe,errorID1);
											failure++;
										}
										catch (Exception e)
										{
											logger.error(className,methodName,machine_info+"-Inside the Exception! and the exception is :: " + e,errorID1);
											failure++;
										}
								}
								logger.debug(className,methodName,"Success : "+ success);
								logger.debug(className,methodName,"Failure : "+ failure);
								
								String count_val = (String) ha_counters.get(vccLogicalSystemName);
								if(count_val == null)
									count_val = "0";
								counter_val = Integer.parseInt(count_val);

								if (failure>=3)
								{
									if (active_flag.equals(ACTIVE_FLAG))
									{
										if(counter_val < 6) {
											counter_val = counter_val + 1;
										} else {
											active_flag = NOT_ACTIVE_FLAG;
											//update in the new daily status flag change file....
											rafpmOutPut.writeBytes(dateString +" :" + className + ": " +"FAILURE - Updating the HA flag from Y to N for Server : " + machine_info + " ( VLSN : " + vccLogicalSystemName + " )");
											rafpmOutPut.writeBytes("\n");
											logger.error(className,methodName,machine_info+"(VLSN : "+ vccLogicalSystemName + " ) is down or not responding!",errorID2);
											hasConfigFileChanged = true;
										}
									} else {
										active_flag = NOT_ACTIVE_FLAG;
									}
								}else {
									if (active_flag.equals(NOT_ACTIVE_FLAG))
									{
										if(counter_val > 3) {
											counter_val = counter_val - 1;
										} else {
											active_flag = ACTIVE_FLAG;
											//update in the new daily status flag change file....
											rafpmOutPut.writeBytes(dateString +" :" + className + ": " +"SUCCESS - Updating the HA flag from N to Y for Server : " + machine_info + " ( VLSN : " + vccLogicalSystemName + " )");
											rafpmOutPut.writeBytes("\n");
											hasConfigFileChanged = true;
										}
									} else if(active_flag.equals(ACTIVE_FLAG)) {
										counter_val = 0;
									}
								}
							}
							logger.debug(className,methodName,"The active flag is ::: " + active_flag);
							logger.debug(className,methodName,"The counter_val is ::: " + counter_val);
							StringBuffer sb = new StringBuffer();
							//sb.append(machine_info);
							//Vinodh commenting for Vtier change here pass the VLSN)
							sb.append(vccLogicalSystemName);
							sb.append("~");
							sb.append(active_flag);
							sb.append("~");
							sb.append(manual_override_flag);
							config_file_output.println(sb.toString());
							StringBuffer sb1 = new StringBuffer();
							sb1.append(vccLogicalSystemName);
							sb1.append("~");
							sb1.append(counter_val);
							hacounter_file_output.println(sb1.toString());
					}
					in.close();
				} catch (Exception e) {
						logger.error(className,methodName,"exception is " + e,errorID1);
				}
		if(hasConfigFileChanged) {
			file5.renameTo(file1);
		} else {	
			file5.delete();
		}
		file4.delete();
		config_file_output.flush();
		logger.info(className,methodName,"Exiting LoadData Method!" + "\n");
		}

		catch (Exception e)
		{
			logger.error(className,methodName,"exception in the printwriter of log file.. and the exception is " + e,errorID1);
		}
	}
	
	public static File copyFile(String sourceFile, String destFile) throws IOException {
        File src = new File(sourceFile);
        FileInputStream fis = new FileInputStream(src);
 
        File dest = new File(destFile);
        FileOutputStream fos = new FileOutputStream(dest);
        
        int val = -1;
        while ( (val = fis.read()) != -1) {
            fos.write(val);
        }
        fos.flush();
        fos.close();
        fis.close();
        
        return dest;
    }

	/**
	  *     Utility method to get String to String Array based on token
	  */
	public static final String[] ConvertStringToArrayOnToken(
															String strOriginal, String strSep,
															short nPosition,
															boolean bIsEmptyString ) {
			Vector<String> listComAddr = new Vector<String>();
			String[] strElements = null;
			String strElement = null;
			if( strOriginal != null ) {
					int nCount = 1;
					boolean bIsTokenMatch=false;
					for(;; ) {
							int nFirstIndex = strOriginal.indexOf( strSep );
							if(     nFirstIndex != -1 && !bIsTokenMatch ) {
									strElement =
											strOriginal.substring(0, nFirstIndex ).trim();
									if( strElement != null &&
											(strElement.length() > 0 || bIsEmptyString )) {
											if(nPosition == ALLPOS ) {
													listComAddr.addElement(strElement);
											}
											else if( nPosition == nCount ) {
													listComAddr.addElement(strElement);
													bIsTokenMatch=true;
											}
									}
									nCount++;
									strOriginal = strOriginal.substring( nFirstIndex+1 ).trim();
							}
							else {
									if( strOriginal != null &&
											(strOriginal.length() > 0 || bIsEmptyString )) {
											if( nPosition == ALLPOS ||
												(nPosition == nCount && !bIsTokenMatch)) {
													listComAddr.addElement(strOriginal);
											}
									}
									strElements = new String[listComAddr.size()];
									listComAddr.toArray( strElements );
									break;
							}
					}
			}
			listComAddr=null;
			return strElements;
	}

}
