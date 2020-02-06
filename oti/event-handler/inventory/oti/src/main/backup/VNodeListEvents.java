package com.att.vcc.inventorycollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.att.dcaetd.common.utilities.logging.EcompLogger;
import com.att.dcaetd.common.utilities.logging.LogType;

import com.att.vcc.inventorycollector.util.DBUtil;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.data.VNode;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.messages.inventoryCollectorOperationEnum;
import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.Util;
import com.att.vcc.inventorycollector.util.ValidationData;

// import org.slf4j.Logger;

public class VNodeListEvents {
	
	private static EcompLogger ecompLogger;
	
	private static TreeMap<String, String> serviceMap = null;

	private static final String subcomponent = "DTI";
	private static Map<String,String> llc = new HashMap<String,String>();
	
	// private static int dataBlock = 2000;
	private static int dataBlock = 5000;
	
	// private static Logger errorLog=new EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_ERROR).forClass(VNodeListEvents.class).build().getLog();
	// private static Logger debugLog=new EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_DEBUG).forClass(VNodeListEvents.class).build().getLog();
	
	public  static String DTI = System.getenv("DTI");
	public  static String DTI_CONFIG = System.getenv("DTI_CONFIG");
	
	// public  static Properties config = null;		// for vnodelist.properties
	
	private static Properties dtiProps = new Properties();
	
	// private static Properties configProps = new Properties();
	private static PropertiesConfiguration configProps = new PropertiesConfiguration();
	
	private static Properties neaProps = new Properties();
	private static Properties vmvnfcProps = new Properties();
	private static Properties vmsequenceProps = new Properties();
	
	
	public static void main(String[] args) {
		
        // VNodeListEvents obj = new VNodeListEvents();
        // obj.getAAIEventData();
	}
	
	
	public VNodeListEvents() {
		
		if (!dtiProps.isEmpty()) {
			return;
		}
		
		// default DTI env
		String configDir = System.getenv("DTI_CONFIG");
		if (configDir == null) {
			// Windows env
			configDir = "C:\\temp\\";
			System.out.println("config dir = " + configDir);
			
			System.out.println("It is in Windows env.");
			// return;
		}

		try (FileInputStream dtiStream = new FileInputStream(Util.safeFileName(configDir) + "/dti.properties");
				
				FileInputStream neaStream = new FileInputStream(
						Util.safeFileName(configDir) + "/network_element_attr.properties");
				
				FileInputStream vnodeStream = new FileInputStream(
						Util.safeFileName(configDir) + "/vnodelist_events.properties");
				
				FileInputStream vmStream = new FileInputStream(
						Util.safeFileName(configDir) + "/vm_vnfc_map.properties");
				
				FileInputStream vnfStream = new FileInputStream(
						Util.safeFileName(configDir) + "/vnfc_sequence.properties")) 
		{
			dtiProps.load(dtiStream);
			
			configProps.setDelimiterParsingDisabled(true);
			configProps.load(vnodeStream);
			
			neaProps.load(neaStream);
			vmvnfcProps.load(vmStream);
			vmsequenceProps.load(vnfStream);
			
		} catch (IOException e) {
			e.getMessage();
			return;
		} catch (Exception e) {
			e.getMessage();
			return;
		}
	}
	
	
	public static String getTimeStamp() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		Date now = new Date(); 
		return simpleDateFormat.format(now);
	}
	
	
	public void parserGenericVnfObjects(JSONObject entity)
	{
		System.out.println("\n Entering parserGenericVnfObjects()");

		VNode vnodeForVNF = new VNode();
		vnodeForVNF = getGenericVnfObjects(entity);
		
		if ( vnodeForVNF == null ) {
			return;
		}
		
		// save vnodelist object for VNF to a file
		ArrayList<VNode> vnodelist = new ArrayList<VNode>();
		vnodelist.add(vnodeForVNF);
		
		saveDataToFile(vnodelist);
		
	}
	
	
	public void saveDataToFile(final ArrayList<VNode> vnodelist) {
		
		// Step 1:  saveRecordsToIncrementalFile and publish to Dmaap
		String saveToFileFlag = configProps.getString("publishIncrementalFileToDMaap");
		if ( saveToFileFlag == null || saveToFileFlag.isEmpty() ) {
			System.out.println("saveRecordsToIncrementalFile is not set"); 
			return;
		}
		
		if ( "Y".equalsIgnoreCase(saveToFileFlag) ) {
			System.out.println("saveRecordsToIncrementalFile flag = " + saveToFileFlag);
			
			saveFileAndPublishToDammp(vnodelist);
			
		} else {
			System.out.println("saveRecordsToIncrementalFile flag is not set to \"Y\".  Records will not be saved to a file. Returning...");
			return;
		}
		
		// Step 2:  updateDatabaseRecords
		String updateDBRecords = configProps.getString("updateDatabaseRecords");
		if ( updateDBRecords == null || updateDBRecords.isEmpty() ) {
			System.out.println("updateDBRecords flag is not set"); 
			return;
		}
		
		if ( "Y".equalsIgnoreCase(updateDBRecords) ) {
			System.out.println("updateDBRecords flag = " + updateDBRecords);
			
			// This feature is disabled since there is no need to update database records from events
			// updateDatabaseRecordsFromEvents(vnodelist);
			
		} else {
			System.out.println("updateDBRecords flag is not set to \"Y\".  Records will not be updated to database. Returning...");
			return;
		}
		
		// Step 3:  publish All Active Records in database To DMaap
		String publishAllActiveRecordsInDB = configProps.getString("publishFullFileToDMaap");
		if ( publishAllActiveRecordsInDB == null || publishAllActiveRecordsInDB.isEmpty() ) {
			System.out.println("publishAllActiveRecordsInDB flag is not set"); 
			return;
		}
		
		if ( "Y".equalsIgnoreCase(publishAllActiveRecordsInDB) ) {
			System.out.println("publishAllActiveRecordsInDB flag = " + updateDBRecords);
			
			publishSnapshotToDMaap();
			
		} else {
			System.out.println("publishAllActiveRecordsInDB flag is not set to \"Y\".  All active records will not be published to DMaap. Returning...");
			return;
		}
		
	}
	
	
	/**
	 *  This method is to publish all active records in database to DMaap
	 *  All active records are those records with validto to be null
	 *  It is also called a snapshot 
	 * 
	 * 
	 * @param inputDateTime
	 * @param inputService
	 * @return
	 */
	public boolean publishSnapshotToDMaap()
	{
		if ( DTI == null ) {
			System.out.println("It is in local Windowes envrionment and can't publish file to DMD."); 
			return true;
		}
		
		// (1) vnodelist
		// get all active records from table vnodelist
		// to be published to DMaap
		Vector<String> vnodelistActiveRecords = new Vector<String>();
		String sqlStr = configProps.getString("publishAllActiveRecords");
		vnodelistActiveRecords = selectRecords(sqlStr);
		  
        if ( vnodelistActiveRecords == null || vnodelistActiveRecords.isEmpty() ) {
        	System.out.println("publishAllActiveRecords is empty!");
        	return false;
        } else {
        	System.out.println("publishAllActiveRecords size = " + vnodelistActiveRecords.size());
        }

		System.out.println("before publishing vnodelist to DMaap..."); 

		String ts = getTimeStamp();
		String fn = "vNodelist.full";
		storeListToFileAndSend(vnodelistActiveRecords, ts, fn);
        
		System.out.println("publish vnodelist to DMaap is done"); 

		// (2) networkelementattributes
		// get all active records from table networkelementattributes
		// to be published to DMaap
		Vector<String> neaActiveRecords = new Vector<String>();
		String sqlStr2 = configProps.getString("publishNEAttrRecords");
		neaActiveRecords = selectRecords(sqlStr2);
		  
        if ( neaActiveRecords == null || neaActiveRecords.isEmpty() ) {
        	System.out.println("networkelementattributes Active Records is empty!");
        	return false;
        } else {
        	System.out.println("networkelementattributes Active Records size = " + neaActiveRecords.size());
        }
        
		System.out.println("before publishing networkelementattributes ..."); 

		ts = getTimeStamp();
		fn = "NetworkElementAttributes.full";
		storeListToFileAndSend(neaActiveRecords, ts, fn);
        
		System.out.println("publishing networkelementattributes is done"); 

        
		return true;
	}
	

	
	public void updateDatabaseRecordsFromEvents(final ArrayList<VNode> vnodelist) 
	{
		System.out.println("DTI: Start to process database operations()...");
					
		if ( vnodelist == null || vnodelist.isEmpty() ) {
			System.out.println("updateDatabaseRecordsFromEvents(): vnodelist is empty");
			return;
		}
		
		int len = vnodelist.size();
		for ( int i=0; i<len; i++ ) {
			// Step 1  check to see if it is a new record or not
			boolean isNewRecord = true;
			
			VNode vnodeObj = vnodelist.get(i);
			String node_name = vnodeObj.getNodeName();
			String service = vnodeObj.getService();
			isNewRecord = getRecordForNodeNameandService(node_name, service);
			
			if ( isNewRecord ) {
				// insert the new record
				Vector<String> sqlList = new Vector<String> ();
				
				String sqlStr = createInsertSql(vnodeObj);
				System.out.println("inserting new records.  sqlStr = " + sqlStr);
		    	sqlList.add(sqlStr);
		    	
				
				int insertCount2 = sqlList.size();
				if ( insertCount2 > 0 ) {
			
					try {
						insertRecordsToVNodeList(sqlList);
					} catch (Exception e) {
						System.out.println("ERROR:   Exception inserting new records into table vNodeList.");
						e.printStackTrace();
						return;
					}
					
				} 
				
			} else {
				// update the existing record
				System.out.println("\n update the existing records.\n");
				
				Vector<String> sqlVec = new Vector<String>();
				sqlVec = createUpdateRecordsSql(vnodeObj);
				
				try {
					updateRecordsToVNodeList_With_DataBlock(sqlVec);
				} catch (Exception e) {
					e.printStackTrace();
				}			

				System.out.println("\n completed the update for the existing records.\n");
			}
		} 	
	} 
	
	public void updateRecordsToVNodeList_With_DataBlock(final Vector<String> sqlList) 
	{
        try {
			int count = DBUtil.getInstance().executeUpdate(sqlList);
			System.out.println("Total " + count + " records have been updated into database table.");

		} catch (DTIException e) {
			System.out.println("ERROR:   Exception when updating data into database table vNodeList.");
			e.printStackTrace();
	
		}
	}
	
	public Vector<String> createUpdateRecordsSql(final VNode vnodeObj)
	{
		Vector<String> nodeNameList = new Vector<String>();
		
		if ( vnodeObj == null ) {
			return nodeNameList;
		}
		
		String timeStamp  = getTimeStamp();
		// String timeStamp  = inputDateTime;
		
		String updateSql = "update dti.vnodelist set "
				+ " validfrom = '" + timeStamp + "', " 
				+ " deleteby = '', sendtoetl = 'Y', " 
				+ " NODE_NAME = '" + vnodeObj.getNodeName() + "', "
				+ " NODELEVEL = '" + vnodeObj.getNodelevel() + "', "
				+ " PROV_STATUS = '" + vnodeObj.getProvStatus() + "', "
				+ " IN_MAINT = '" + vnodeObj.getInMaint() + "', "
				+ " SERVICE = '" + vnodeObj.getService() + "', "
				+ " FUNC_CODE = '" + vnodeObj.getFuncCode() + "', "
				+ " NETWORK = '" + vnodeObj.getNetwork() + "', "
				+ " SERVICE_TYPE = '" + vnodeObj.getServiceType() + "', "
				+ " NODE_TYPE = '" + vnodeObj.getNodeType() + "', "
				+ " NODE_SUB_TYPE = '" + vnodeObj.getNodeSubType() + "', "
				+ " CLLI = '" + vnodeObj.getClli() + "', "
				+ " REGION_ZONE = '" + vnodeObj.getRegionZone() + "', "
				+ " OAMP_IP_ADDR = '" + vnodeObj.getOampIpAddr() + "', "
				+ " TVSP_IP_ADDR = '" + vnodeObj.getTvspIpAddr() + "', "
				+ " COL_NODE_NAME = '" + vnodeObj.getColNodeName() + "', "
				+ " COL_IP_ADDR = '" + vnodeObj.getColIpAddr() + "', "
				+ " UUID = '" + vnodeObj.getUuid() + "', "
				+ " FQDN = '" + vnodeObj.getFqdn() + "', "
				+ " VNF_PARENT = '" + vnodeObj.getVnfParent() + "', "
				+ " VS_PARENT = '" + vnodeObj.getVsParent() + "', "
				+ " PS_PARENT = '" + vnodeObj.getPsParent() + "', "
				+ " MATE_NODE = '" + vnodeObj.getMateNode() + "', "
				+ " TENANT_ID = '" + vnodeObj.getTenantId() + "', "
				+ " DKAT_SERVICE = '" + vnodeObj.getDkatService() + "', "
				+ " DKAT_NODETYPE = '" + vnodeObj.getDkatNodeType() + "' "
				+ " where (VALIDTO='' or VALIDTO is null) "
				+ " and node_name = '" + vnodeObj.getNodeName() + "' "
				+ " and service = '" + vnodeObj.getService() + "' ";
		
		nodeNameList.add(updateSql);
		
		return nodeNameList;
	}
	
	
	public void insertRecordsToVNodeList(final Vector<String> sqlList) 
	{
		if ( sqlList == null || sqlList.size() == 0 ) {
			return;
		}
		
		System.out.println("start to insert records into vNodeList...");
		
		int size = sqlList.size();
		
		if ( size <= dataBlock ) {
			System.out.println("Total size is less than " + dataBlock);
			
			try {
				insertNodesToVNodeList(sqlList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Total size is greater than " + dataBlock);
			
			Vector<String> newDataList = new Vector<String>();
			
			int count = 1;
			for ( int i=1; i<=size; i++ ) {
				String sqlStr = sqlList.elementAt(i-1);
				newDataList.add(sqlStr);
				
				int newCount = count*dataBlock;
				
				// bulk inserting for every dataBlock records
				if ( newCount <= size ) {
					if ( (i!= 0) && (i % dataBlock == 0) ) {
	
						System.out.println("count = " + (count*dataBlock) + " newDataList size = " + newDataList.size());
					
						try {
							insertNodesToVNodeList(newDataList);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						// reset the list 
						newDataList = new Vector<String>();
						count ++;
					}
				} else if ( i == size ) {
					System.out.println("Number of remaining records = " + newDataList.size() );
					
					try {
						insertNodesToVNodeList(newDataList);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}  // end of insertRecordsToVNodeList()
	
	
	public void insertNodesToVNodeList(final Vector<String> sqlList)
	{
        try {
			int count = DBUtil.getInstance().executeSql(sqlList);
			System.out.println("Total " + count + " records have been inserted into database table.");

		} catch (DTIException e) {
			System.out.println("ERROR:   Exception when inserting data into database table.");
			e.printStackTrace();
		}
	}
	
	
	public String createInsertSql(final VNode vNodeObj)
	{
		String headerList = configProps.getString("headerList");
		
		String timeStamp  = getTimeStamp();
		
		String sqlStr = "insert into DTI.VNODELIST (" + headerList + ") values (";
		
		sqlStr += "nextval('VNODELIST_VNODELISTKEY_SEQ'), 'admin','" + timeStamp + "',null,'','','Y'";
		
		sqlStr += ",'" + checkNullString(vNodeObj.getNodeName()) + "'" +
				  ",'" + checkNullString(vNodeObj.getNodelevel()) + "'" +
				  ",'" + checkNullString(vNodeObj.getProvStatus()) + "'" +
				  ",'" + checkNullString(vNodeObj.getInMaint()) + "'" +
				  ",'" + checkNullString(vNodeObj.getService()) + "'" +
				  ",'" + checkNullString(vNodeObj.getFuncCode()) + "'" +
				  ",'" + checkNullString(vNodeObj.getNetwork()) + "'" +
				  ",'" + checkNullString(vNodeObj.getServiceType()) + "'" +
				  ",'" + checkNullString(vNodeObj.getNodeType()) + "'" +
				  ",'" + checkNullString(vNodeObj.getNodeSubType()) + "'" +
				  ",'" + checkNullString(vNodeObj.getClli()) + "'" +
				  ",'" + checkNullString(vNodeObj.getRegionZone()) + "'" +
				  ",'" + checkNullString(vNodeObj.getOampIpAddr()) + "'" +
				  ",'" + checkNullString(vNodeObj.getTvspIpAddr()) + "'" +
				  ",'" + checkNullString(vNodeObj.getColNodeName()) + "'" +
				  ",'" + checkNullString(vNodeObj.getColIpAddr()) + "'" +
				  ",'" + checkNullString(vNodeObj.getUuid()) + "'" +
				  ",'" + checkNullString(vNodeObj.getFqdn()) + "'" +
				  ",'" + checkNullString(vNodeObj.getVnfParent()) + "'" +
				  ",'" + checkNullString(vNodeObj.getVsParent()) + "'" +
				  ",'" + checkNullString(vNodeObj.getPsParent()) + "'" +
				  ",'" + checkNullString(vNodeObj.getMateNode()) + "'" +
				  ",'" + checkNullString(vNodeObj.getTenantId()) + "'" +
				  ",'" + checkNullString(vNodeObj.getVnfcFuncCode()) + "'" +
				  ",'" + checkNullString(vNodeObj.getDkatService()) + "'" +
				  ",'" + checkNullString(vNodeObj.getDkatNodeType()) + "')";
		
		// System.out.println("insert sql = " + sqlStr);
		
		return sqlStr;
		
	}
	
	public String checkNullString(final String inputStr)
	{
		if ( inputStr == null || inputStr.isEmpty() ) {
			return "";
		}
		
		String rec = "";
		rec = inputStr.replaceAll("null", "");
		rec = rec.replaceAll("NULL", "");
		
		return rec;
	}
	
	
	public String reformatString(final String inputStr)
	{
		if ( inputStr == null || inputStr.isEmpty() ) {
			return "";
		}
		
		String rec = inputStr.replaceAll("\"", "");
		return rec;
	}
	

	/**
	 * 
	 * 
	 * 
	 * @param node_name
	 * @param service
	 * @return
	 */
	public boolean getRecordForNodeNameandService(final String node_name, final String service) 
	{

		String sqlStr = "select node_name, service from dti.vnodelist where (VALIDTO='' or VALIDTO is null) and node_name='" + node_name + "'  and service='" + service + "'";
		
		// System.out.println("getValidRecordsForNodeNameandService():  sqlStr = " + sqlStr);
		
		boolean isNewRecord = true;
		Vector<String> tmpList = selectRecords(sqlStr);
		if ( tmpList == null || tmpList.isEmpty() ) {
			System.out.println("query reUlt is empty.  It is a new record.");
		} else {
			System.out.println("query reUlt is empty.  It is an existing record.");
			isNewRecord = false;
		}

		return isNewRecord;
	}
	
	public Vector<String> selectRecords(final String sql) {
		
        // get data from table
        Vector<String> records = new Vector<String>();
         
        try {
			records = DBUtil.getInstance().executeQuery(sql);
		} catch (DTIException e) {
			System.out.println("ERROR:   Exception when query data into database table.");
			e.printStackTrace();
		}
        
 
        return records;
	}
	
	
	
	public void saveFileAndPublishToDammp(final ArrayList<VNode> vnodelist) {
		
		Vector<String> listOfRecords = new Vector<String>();
		
		int size = vnodelist.size();
		
		for ( int i=0; i<size; i++ ) {
			
			VNode vnodeObj = vnodelist.get(i);
			
			String validFrom = getTimeStamp();
			String validto = "";
			String userId = "admin";
		
			String record = validFrom + ";" + 
				validto + ";" + 
				userId + ";" + 
				vnodeObj.getNodeName() + ";" + 
				vnodeObj.getNodelevel() + ";" + 
				vnodeObj.getProvStatus() + ";" + 
				vnodeObj.getInMaint() + ";" + 
				vnodeObj.getService() + ";" + 
				vnodeObj.getFuncCode() + ";" + 
				vnodeObj.getNetwork() + ";" + 
				vnodeObj.getServiceType() + ";" + 
				vnodeObj.getNodeType() + ";" + 
				vnodeObj.getNodeSubType() + ";" + 
				vnodeObj.getClli() + ";" + 
				vnodeObj.getRegionZone() + ";" + 
				vnodeObj.getOampIpAddr() + ";" + 
				vnodeObj.getTvspIpAddr() + ";" + 
				vnodeObj.getColNodeName() + ";" + 
				vnodeObj.getColIpAddr() + ";" + 
				vnodeObj.getUuid() + ";" + 
				vnodeObj.getFqdn() + ";" + 
				vnodeObj.getVnfParent() + ";" + 
				vnodeObj.getVsParent() + ";" + 
				vnodeObj.getPsParent() + ";" + 
				vnodeObj.getMateNode() + ";" + 
				vnodeObj.getTenantId() + ";" + 
				vnodeObj.getVnfcFuncCode() + ";" + 
				vnodeObj.getDkatService() + ";" + 
				vnodeObj.getDkatNodeType(); 
		
			listOfRecords.add(record);
		}
		
		String ts = getTimeStamp();
		String fn = "vNodelist.delta";
		storeListToFileAndSend(listOfRecords, ts, fn);
	}

	
	
	public VNode getGenericVnfObjects(JSONObject entity)
	{
		System.out.println("\n Entering getGenericVnfObjects()");

		if (entity == null ) {
			System.out.println("Generic_vnf entity is empty");
			return null;
		}
		
		String vnf_name = entity.optString("vnf-name");
		if (vnf_name == null || vnf_name.isEmpty() ) {
			System.out.println("VNF_NAME is empty");
			return null;
		}
		
		// vserver_name.matches(vmPatternMap.getProperty("voip"))
		if ( vnf_name.length() == 9 && vnf_name.matches("[A-Za-z]{4}\\d{4}[vV]")) {
			System.out.println("vnf_name matches the pattern");
		} else {
			System.out.println("vnf_name not matches the pattern");
			return null;
		}	
		
		String provStatus = entity.optString("prov-status");
		if ( provStatus == null || provStatus.isEmpty() ) {
			System.out.println("VNF level provStatus is empty");
			return null;
		}
		
		if ( "PROV".equalsIgnoreCase(provStatus) || "PREPROV".equalsIgnoreCase(provStatus) ) {
			System.out.println("provStatus = " + provStatus);
		} else {
			System.out.println("VNF level provStatus is empty");
			return null;
		}
	
		String regional_resource_zone = entity.optString("regional-resource-zone");
		String ipv4_oam_address = entity.optString("ipv4-oam-address");
		

		String in_maint = entity.optString("in-maint");
		String vnf_id = entity.optString("vnf-id");
		String resource_version = entity.optString("resource-version");
		
		// VNode object for VNF node
    	VNode vNodeObj = new VNode();
		String nodeLevel = "VNF";
		vNodeObj.setNodelevel(nodeLevel);
	
		String nodeName = vnf_name;
		vNodeObj.setNodeName(nodeName);	
		vNodeObj.setProvStatus(provStatus);
		
		if ( in_maint == null || in_maint.isEmpty() ) {
			vNodeObj.setInMaint("Y");
		} else if ( "false".equalsIgnoreCase(in_maint) ) {
			vNodeObj.setInMaint("N");
		} else {
			vNodeObj.setInMaint("Y");
		}
		
		String service_id = entity.optString("service-id");
		if ( service_id == null || service_id.isEmpty() ) {
			System.out.println("VNF level service_id is empty");
			return null;
		}
		System.out.println("service_id = " + service_id);
		
		if ( serviceMap == null || serviceMap.isEmpty() ) {
			setServiceMap();
		}
		
		String servie_description = serviceMap.get(service_id);
		if ( servie_description == null || servie_description.isEmpty() ) {
			System.out.println("It is not service for voip application");
			
			// servie_description = "NA";
			return null;
		} 
		System.out.println("It is voip service. servie_description = " + servie_description);
		
		
		String service = servie_description;
		service = service.toUpperCase();
		vNodeObj.setService(service);
		
		String funcCode = getFuncCode(nodeName);
		
		String funcCode_Service = funcCode + "___" + service.replaceAll(" ", "_");
		System.out.println("funcCode_Service = " + funcCode_Service);

		String neAttr = neaProps.getProperty(funcCode_Service);
		if ( neAttr == null || neAttr.isEmpty() ) {
			System.out.println("config file doesn't contain this properties:  funcCode_Service = " + funcCode_Service);
			return null;
			
		} else {

    		neAttr = neAttr.replaceAll("\"", "");
    		
    		System.out.println("neAttr = " + neAttr);
			String arr2 [] = neAttr.split(";");
			
			vNodeObj.setFuncCode(funcCode);
			
			String node_type = arr2[3];
			vNodeObj.setNodeType(node_type);
			
			String node_subtype = arr2[4];
			vNodeObj.setNodeSubType(node_subtype);
			
			String network = arr2[5];
			vNodeObj.setNetwork(network);
			
			String service_type = arr2[6];
			vNodeObj.setServiceType(service_type);
			
    		String dkat_service = arr2[7];
    		vNodeObj.setDkatService(dkat_service);

    		String dkat_nodetype = arr2[8];
    		vNodeObj.setDkatNodeType(dkat_nodetype);
			
		}
		
		vNodeObj.setClli("");
		
		vNodeObj.setRegionZone(regional_resource_zone);
		
		vNodeObj.setOampIpAddr(ipv4_oam_address);
		vNodeObj.setTvspIpAddr("");
		
		vNodeObj.setColIpAddr("");
		vNodeObj.setColNodeName("");

		vNodeObj.setUuid(vnf_id);
		vNodeObj.setFqdn("");
		vNodeObj.setVnfParent("");
		vNodeObj.setVsParent("");
		vNodeObj.setPsParent("");

		vNodeObj.setMateNode("");
		
		// TBD
		/*
		vNodeObj.setTenantId(TenantId);
		
		if ( VnfId_to_TenantId_Map == null || VnfId_to_TenantId_Map.size() == 0 ) {
			vNodeObj.setTenantId("");
		} else {
			String TenantId = VnfId_to_TenantId_Map.get(vnf_id);
			vNodeObj.setTenantId(TenantId);
		}
		*/

       	vNodeObj.setVnfcFuncCode("");
       	
       	System.out.println("VNF node: " + vNodeObj.toString() );

    	// vNodeMap.put(nodeName, vNodeObj);
       	return vNodeObj;
		
	}  // end of getGenericVnfObjects()
	

	public VNode getSimplifiedGenericVnfObjects(JSONObject entity)
	{
		System.out.println("\n Entering getGenericVnfObjects()");

		if (entity == null ) {
			System.out.println("Generic_vnf entity is empty");
			return null;
		}
		
		String vnf_name = entity.optString("vnf-name");
		if (vnf_name == null || vnf_name.isEmpty() ) {
			System.out.println("VNF_NAME is empty");
			return null;
		}
		
		// vserver_name.matches(vmPatternMap.getProperty("voip"))
		if ( vnf_name.length() == 9 && vnf_name.matches("[A-Za-z]{4}\\d{4}[vV]")) {
			System.out.println("vnf_name matches the pattern");
		} else {
			System.out.println("vnf_name not matches the pattern");
			return null;
		}	
		
	
		String regional_resource_zone = entity.optString("regional-resource-zone");
		String ipv4_oam_address = entity.optString("ipv4-oam-address");
		
		String vnf_id = entity.optString("vnf-id");
		
		// VNode object for VNF node
    	VNode vNodeObj = new VNode();
		String nodeLevel = "VNF";
		vNodeObj.setNodelevel(nodeLevel);
	
		String nodeName = vnf_name;
		vNodeObj.setNodeName(nodeName);	
		
	
		String service_id = entity.optString("service-id");
		if ( service_id == null || service_id.isEmpty() ) {
			System.out.println("VNF level service_id is empty");
			return null;
		}
		System.out.println("service_id = " + service_id);
		
		if ( serviceMap == null || serviceMap.isEmpty() ) {
			setServiceMap();
		}
		
		String servie_description = serviceMap.get(service_id);
		if ( servie_description == null || servie_description.isEmpty() ) {
			System.out.println("It is not service for voip application");
			
			// servie_description = "NA";
			return null;
		} 
		System.out.println("It is voip service. servie_description = " + servie_description);
		
		
		String service = servie_description;
		service = service.toUpperCase();
		vNodeObj.setService(service);
		
		String funcCode = getFuncCode(nodeName);
		
		String funcCode_Service = funcCode + "___" + service.replaceAll(" ", "_");
		System.out.println("funcCode_Service = " + funcCode_Service);

		String neAttr = neaProps.getProperty(funcCode_Service);
		if ( neAttr == null || neAttr.isEmpty() ) {
			System.out.println("config file doesn't contain this properties:  funcCode_Service = " + funcCode_Service);
			return null;
			
		} else {

    		neAttr = neAttr.replaceAll("\"", "");
    		
    		System.out.println("neAttr = " + neAttr);
			String arr2 [] = neAttr.split(";");
			
			vNodeObj.setFuncCode(funcCode);
			
			String node_type = arr2[3];
			vNodeObj.setNodeType(node_type);
			
			String node_subtype = arr2[4];
			vNodeObj.setNodeSubType(node_subtype);
			
			String network = arr2[5];
			vNodeObj.setNetwork(network);
			
			String service_type = arr2[6];
			vNodeObj.setServiceType(service_type);
			
    		String dkat_service = arr2[7];
    		vNodeObj.setDkatService(dkat_service);

    		String dkat_nodetype = arr2[8];
    		vNodeObj.setDkatNodeType(dkat_nodetype);
			
		}
		
		vNodeObj.setClli("");
		
		vNodeObj.setRegionZone(regional_resource_zone);
		
		vNodeObj.setOampIpAddr(ipv4_oam_address);
		vNodeObj.setTvspIpAddr("");
		
		vNodeObj.setColIpAddr("");
		vNodeObj.setColNodeName("");

		vNodeObj.setUuid(vnf_id);
		vNodeObj.setFqdn("");
		vNodeObj.setVnfParent("");
		vNodeObj.setVsParent("");
		vNodeObj.setPsParent("");

		vNodeObj.setMateNode("");
		
		// TBD
		/*
		vNodeObj.setTenantId(TenantId);
		
		if ( VnfId_to_TenantId_Map == null || VnfId_to_TenantId_Map.size() == 0 ) {
			vNodeObj.setTenantId("");
		} else {
			String TenantId = VnfId_to_TenantId_Map.get(vnf_id);
			vNodeObj.setTenantId(TenantId);
		}
		*/

       	vNodeObj.setVnfcFuncCode("");
       	
       	System.out.println("VNF node: " + vNodeObj.toString() );

    	// vNodeMap.put(nodeName, vNodeObj);
       	return vNodeObj;
		
	}  // end of getSimplifiedGenericVnfObjects()
	

	public void parserVserverObjects(JSONObject entityVM, JSONObject entityVNF )
	{
		System.out.println("\n Entering parserVserverObjects()");
		
		if (entityVM == null) {
			System.out.println("parserPserverObjects() - entity is null");
			return;
		}
		
		VNode vnodeForVNF = new VNode();
		vnodeForVNF = getSimplifiedGenericVnfObjects(entityVNF);
		if ( vnodeForVNF == null ) {
			System.out.println("The call to getGenericVnfObjects is returning empty");
			return;
		}
		System.out.println("\n Returning to parserVserverObjects()");
		
		JSONObject tenantsObj = entityVM.optJSONObject("tenants");
		if (tenantsObj == null) {
			System.out.println("parserVserverObjects() - tenantsObj is null");
			return;
		}
		
		JSONArray tenantArray = tenantsObj.optJSONArray("tenant");
		int len = tenantArray.length();
		
		for (int i = 0; tenantArray != null && i < len; i++) {
			JSONObject tenantObject = tenantArray.optJSONObject(i);
			if (tenantObject == null) {
				System.out.println("parserVserverObjects() - tenantsObj is null");
				return;
			}
			
			String tenant_id = tenantObject.optString("tenant-id");
		
			JSONObject vserversObject = tenantObject.optJSONObject("vservers");
			if (vserversObject == null) {
				System.out.println("parserVserverObjects() - vserversObject is null");
				return;
			}
			JSONArray vserverArray = vserversObject.optJSONArray("vserver");
			
			int len2 = vserversObject.length();
			
			for (int j = 0; vserverArray != null && j < vserverArray.length(); j++) {
				JSONObject vserverObject = vserverArray.optJSONObject(j);
				if (vserverObject == null) {
					System.out.println("parserVserverObjects() - vserversObject is null");
					return;
				}
				
				String vserver_name = vserverObject.optString("vserver-name");
				if (StringUtils.isBlank(vserver_name)) {
					System.out.println("parserVserverObjects() - vserver_name is empty");
					return;
				}
				
				// vserver_name.matches(vmPatternMap.getProperty("voip"))
				if ( vserver_name.length() == 13 && vserver_name.matches("[A-Za-z]{4}\\d{4}[vV][mM]\\d{3}")) {
					System.out.println("vserver_name matches voip pattern");
				} else {
					System.out.println("parserVserverObjects() - vserver_name does not match voip pattern");
					return;
				}	
				System.out.println("vserver_name = " + vserver_name);
				
				String provStatus = vserverObject.optString("prov-status");
				if ( provStatus == null || provStatus.isEmpty() ) {
					System.out.println("VM level provStatus is empty");
					return;
				}
				
				// A VM node must be in "PROV" or "PREPROV" status to be included in vnodelist
				// and to create its VNFC nodes
				if ( "PROV".equalsIgnoreCase(provStatus) || "PREPROV".equalsIgnoreCase(provStatus) ) {
					System.out.println("provStatus = " + provStatus);
				} else {
					System.out.println("VM provStatus is not PROV/PREPROV");
					return;
				}
			
				String in_maint = vserverObject.optString("in-maint");
				
				String vserver_id = vserverObject.optString("vserver-id");
		    	if ( vserver_id == null || vserver_id.isEmpty() ) {
		    		System.out.println("vserver_id is empty");
		    		return;
		    	}

				String resource_version = vserverObject.optString("resource-version");

				String vnf_name = vnodeForVNF.getNodeName();
				String vnf_region_zone = vnodeForVNF.getRegionZone();
				String vnf_service = vnodeForVNF.getService();
				String vnf_func_code = vnodeForVNF.getFuncCode();
				

				// VNode object for VM node
		    	VNode vnodeForVM = new VNode();
		    	String nodeLevel = "VM";
		    	
				String vm_funcCode = getFuncCode(vserver_name);
				if ( !vnf_func_code.equalsIgnoreCase(vm_funcCode) ) {
					System.out.println("nf_func_code is ot equal to vm_funcCode");
					return;
				}
				vnodeForVM.setFuncCode(vm_funcCode);
				
		    	String nodeName = vserver_name;
		    	vnodeForVM.setNodeName(vserver_name);
		    	vnodeForVM.setNodelevel(nodeLevel);
		
		    	vnodeForVM.setProvStatus(provStatus);
		    	
				if ( in_maint == null || in_maint.isEmpty() ) {
					vnodeForVM.setInMaint("Y");
				} else if ( "false".equalsIgnoreCase(in_maint) ) {
					vnodeForVM.setInMaint("N");
				} else {
					vnodeForVM.setInMaint("Y");
				}
		    	    
		    	vnodeForVM.setService(vnf_service);

				String funcCode_Service = vm_funcCode + "___" + vnf_service.replaceAll(" ", "_");
				System.out.println("VM: funcCode_Service = " + funcCode_Service);

				String neAttr = neaProps.getProperty(funcCode_Service);
				if ( neAttr == null || neAttr.isEmpty() ) {
					System.out.println("NetworkElementAttributes config file doesn't contain this properties:  funcCode_Service = " + funcCode_Service);
					return;
					
				} else {

		    		neAttr = neAttr.replaceAll("\"", "");
		    		
					String arr2 [] = neAttr.split(";");
					
					String node_type = arr2[3];
					vnodeForVM.setNodeType(node_type);
					
					String node_subtype = arr2[4];
					vnodeForVM.setNodeSubType(node_subtype);
					
					String network = arr2[5];
					vnodeForVM.setNetwork(network);
					
					String service_type = arr2[6];
					vnodeForVM.setServiceType(service_type);
					
		    		String dkat_service = arr2[7];
		    		vnodeForVM.setDkatService(dkat_service);

		    		String dkat_nodetype = arr2[8];
		    		vnodeForVM.setDkatNodeType(dkat_nodetype);
					
		    	}
		    	
		    	// TBD
		    	/*
		    	String pserver_id = VserverId_to_PserverId_Map.get(vserver_id);
		    	pserver_id = checkData(pserver_id, nodeLevel, "pserver_id", true);
		
		    	System.out.println("VserverId_to_PserverHostname_Map size = " + VserverId_to_PserverHostname_Map.size());
		    	
		    	String pserver_hostname = VserverId_to_PserverHostname_Map.get(vserver_id);
		    	pserver_hostname = checkData(pserver_hostname, nodeLevel, "pserver_hostname", true);
		    	
		    	String location = PserverHostname_to_PhysicalLocationId_Map.get(pserver_hostname);
		    	vnodeForVM.setClli(location);
		
		    	vnodeForVM.setRegionZone(regional_resource_zone);
		    	
		    	// vnodeForVM.setPsParent(pserver_id);
		    	// vnodeForVM.setPsParent(pserver_hostname);
		    	*/

				// TBD
		    	// oamp_ip_addr
				String trinity_ip_addr = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "Trinity_OAMP_3900", "l3-interface-ipv4-address");
				System.out.println("trinity_ip_addr = " + trinity_ip_addr);
				
			    String vUsp_ip_addr = getLInterfaceData(vserverObject.optJSONObject("l-interfaces"), "OAM",	"l3-interface-ipv4-address");
			    System.out.println("vUsp_ip_addr = " + vUsp_ip_addr);
				
		    	vnodeForVM.setOampIpAddr(trinity_ip_addr);
		    	
		    	// ipaddr = processIPAddress(ipaddr);
		    	vnodeForVM.setTvspIpAddr("");
		
				vnodeForVM.setColIpAddr("");
				vnodeForVM.setColNodeName("");
		    	
		    	vnodeForVM.setUuid(vserver_id);	// VM level
		    	vnodeForVM.setFqdn("");
		    	
		    	vnodeForVM.setVnfParent(vnf_name);
		    	vnodeForVM.setVsParent("");
		    	
		    	vnodeForVM.setMateNode("");
		
		    	vnodeForVM.setTenantId(tenant_id);
		
		    	vnodeForVM.setVnfcFuncCode("");
		
		    	System.out.println("VM node: " + vnodeForVM.toString() );

				// save vnodelist object for VM to a file
				ArrayList<VNode> vnodelist = new ArrayList<VNode>();
				vnodelist.add(vnodeForVM);
				
				saveDataToFile(vnodelist);
				
		    	// setup VNFC nodes
		    	setVNFCObjects(vnodeForVM, vnodeForVNF);
			}    
		}		
	}
	
	
	private String getLInterfaceData(JSONObject linterfaces, String nwNamePattern, String resultField) 
	{
		System.out.println("Entering getLInterfaceData()");


		if (linterfaces == null || StringUtils.isBlank(nwNamePattern) || StringUtils.isBlank(resultField))
			return null;

		JSONArray linterfaceArray = linterfaces.optJSONArray("l-interface");
		JSONObject linterfaceArrayObject = null;
		JSONArray linterfaceipAddrArray = null;
		JSONObject linterfaceipAddrArrayObject = null;
		for (int i = 0; linterfaceArray != null && i < linterfaceArray.length(); i++) {
			linterfaceArrayObject = linterfaceArray.optJSONObject(i);
			if (linterfaceArrayObject != null && linterfaceArrayObject.has("network-name")) {
				if (!StringUtils.containsIgnoreCase(linterfaceArrayObject.getString("network-name"), nwNamePattern))
					continue;
				linterfaceipAddrArray = linterfaceArrayObject.optJSONArray("l3-interface-ipv4-address-list");
				for (int j = 0; linterfaceipAddrArray != null && j < linterfaceipAddrArray.length(); j++) {
					linterfaceipAddrArrayObject = linterfaceipAddrArray.optJSONObject(j);
					if (linterfaceipAddrArrayObject != null)
						return linterfaceipAddrArrayObject.getString(resultField);
				}
			}
		}
		return null;
	}

	
	/**
	 * Only creates a VNFC record if 
	 * 	(1) the correspond VM is available from A&AI
	 *  (2) VM prov_status is PROV or PREPROV
	 *  
	 * 
	 * @param vnodeForVM
	 * @param vnodeForVNF
	 */
	public void setVNFCObjects(final VNode vnodeForVM, final VNode vnodeForVNF )
	{
		System.out.println("\n Entering setVNFCObjects()");
		
    	String vm_node_name = vnodeForVM.getNodeName();
    	String vnfc_node_name = vmvnfcProps.getProperty(vm_node_name);
    	
    	if ( vnfc_node_name == null || vnfc_node_name.isEmpty() ) {
    		System.out.println("vnfc_node_name is not in the vm_vnfc_map");
    	} else {
    		System.out.println("vnfc_node_name = " + vnfc_node_name);
    	}
    	
    	Vector<String> vnfc_nodes_list = new Vector<String>();
    	vnfc_nodes_list = getVNFCNodesFromVMNode(vnodeForVM);
    	
    	if ( vnfc_nodes_list == null || vnfc_nodes_list.size() == 0 ) {
    		System.out.println("vnfc_nodes_list is empty. Return");
    	}

    	int len = vnfc_nodes_list.size();
    	for ( int i=0; i<len; i++ ) {

        	VNode vnodeForVNFC = new VNode();
        	String nodeLevel = "VNFC";
        	vnodeForVNFC.setNodelevel(nodeLevel);
        	
        	String vnfc_nodename = vnfc_nodes_list.elementAt(i);
        	vnodeForVNFC.setNodeName(vnfc_nodename);
        	
	    	vnodeForVNFC.setProvStatus(vnodeForVM.getProvStatus());
	    	
	    	String in_maint = vnodeForVM.getInMaint();
			if ( in_maint == null || in_maint.isEmpty() ) {
				vnodeForVNFC.setInMaint("Y");
			} else if ( "false".equalsIgnoreCase(in_maint) ) {
				vnodeForVNFC.setInMaint("N");
			} else {
				vnodeForVNFC.setInMaint("Y");
			}
	
	    	vnodeForVNFC.setFuncCode(vnodeForVM.getFqdn());
	    	vnodeForVNFC.setService(vnodeForVM.getService());
    		vnodeForVNFC.setNodeType(vnodeForVM.getNodeType());
    		vnodeForVNFC.setNodeSubType(vnodeForVM.getNodeSubType());
    		vnodeForVNFC.setNetwork(vnodeForVM.getNetwork());
    		vnodeForVNFC.setServiceType(vnodeForVM.getServiceType());
    		vnodeForVNFC.setDkatService(vnodeForVM.getDkatService());
    		vnodeForVNFC.setDkatNodeType(vnodeForVM.getDkatNodeType());
	
     		
	    	/*
	    	// CLLI - in VNFC level
	    	String pserver_hostname = "";
	
	    	if ( vserver_id == null || vserver_id.isEmpty() || VserverId_to_PserverHostname_Map == null ) {
	    		System.out.println("VserverId_to_PserverHostname_Map is null");
	        	vNodeObj.setClli("");
	    	} else {
	        	pserver_hostname = VserverId_to_PserverHostname_Map.get(vserver_id);
	        	pserver_hostname = checkData(pserver_hostname, nodeLevel, "pserver_hostname", true);
	        	
	        	String location = PserverHostname_to_PhysicalLocationId_Map.get(pserver_hostname);
	        	vNodeObj.setClli(location);
	    	}
	    	*/

    		vnodeForVNFC.setVnfParent(vnodeForVNF.getNodeName());
    		vnodeForVNFC.setVsParent(vnodeForVM.getNodeName());

    		vnodeForVNFC.setOampIpAddr(vnodeForVM.getOampIpAddr());
    		vnodeForVNFC.setTvspIpAddr(vnodeForVM.getTvspIpAddr());
    		
    		vnodeForVNFC.setRegionZone(vnodeForVM.getRegionZone());
    		vnodeForVNFC.setTenantId(vnodeForVM.getTenantId());

	
			vnodeForVNFC.setColIpAddr("");
			vnodeForVNFC.setColNodeName("");
	    	
	    	vnodeForVNFC.setUuid("");
	    	vnodeForVNFC.setFqdn("");
	    	
	    	// TBD
	    	// vnodeForVNFC.setPsParent(pserver_id);
	    	// vnodeForVNFC.setPsParent(pserver_hostname);
	
	    	vnodeForVNFC.setMateNode("");
	    	
	    	System.out.println("VNFC node: " + vnodeForVNFC.toString() );

	    	
			// save vnodelist object for VNFC to a file
			ArrayList<VNode> vnodelist = new ArrayList<VNode>();
			vnodelist.add(vnodeForVNFC);
			
			saveDataToFile(vnodelist);
    	}
		
	}
	
	
	
	public void parserPserverObjects(JSONObject entity, final String physical_location_id)
	{
		System.out.println("\n Entering parserPserverObjects()");
		
		if (entity == null) {
			System.out.println("parserPserverObjects() - entity is null");
			return;
		}
	
		String hostname = entity.optString("hostname");
		if (hostname == null || hostname.isEmpty() ) {
			System.out.println("parserPserverObjects() - hostname is null");
			return;
		}
		System.out.println("parserPserverObjects() - hostname = " + hostname);
		
		String ipv4_oam_address = entity.optString("ipv4-oam-address");

		String in_maint = entity.optString("in-maint");
		String pserver_id = entity.optString("pserver-id");
		String resource_version = entity.optString("resource-version");
		String fqdn = entity.optString("fqdn");
		
		System.out.println("parserPserverObjects() - ipv4_oam_address = " + ipv4_oam_address);
		
		VNode vNodeObj = new VNode();
    	String nodeLevel = "PS";
    	
     	String prov_status = "NA";
    	
    	String nodeName = hostname;
     	vNodeObj.setNodeName(nodeName);
    	vNodeObj.setNodelevel(nodeLevel);

    	vNodeObj.setProvStatus(prov_status);
    	
		if ( in_maint == null || in_maint.isEmpty() ) {
			vNodeObj.setInMaint("Y");
		} else if ( "false".equalsIgnoreCase(in_maint) ) {
			vNodeObj.setInMaint("N");
		} else {
			vNodeObj.setInMaint("Y");
		}

 		vNodeObj.setService("NA");
   		vNodeObj.setFuncCode("NA");
		vNodeObj.setNetwork("NA");
		vNodeObj.setServiceType("NA");
		vNodeObj.setNodeType("NA");
		vNodeObj.setNodeSubType("NA");
		
		vNodeObj.setDkatService("NA");
		vNodeObj.setDkatNodeType("NA");

    	// String clli = PserverHostname_to_PhysicalLocationId_Map.get(hostName);
    	vNodeObj.setClli(physical_location_id);

    	
    	vNodeObj.setRegionZone("");

    	vNodeObj.setOampIpAddr(ipv4_oam_address);
    	vNodeObj.setTvspIpAddr("");

    	vNodeObj.setColIpAddr("");	
    	vNodeObj.setColNodeName("");
    	
    	vNodeObj.setUuid(pserver_id);	// PS level
    	vNodeObj.setFqdn(fqdn);
    	
    	vNodeObj.setVnfParent("");
    	vNodeObj.setVsParent("");
    	vNodeObj.setPsParent("");

    	vNodeObj.setMateNode("");

    	vNodeObj.setTenantId("");


    	System.out.println("PS node: " + vNodeObj.toString() );
    	// save the record to a file
    	
		// save the object for PServer to a file
		ArrayList<VNode> vnodelist = new ArrayList<VNode>();
		vnodelist.add(vNodeObj);
		
		saveDataToFile(vnodelist);
    	
	}
	
	
    public String getServiceAndFuncCodeFromVectorString(final Vector<String> v, final int number1, final int number2)
    {
    	String returnList = "";
    	
    	if ( v == null || v.isEmpty() ) {
    		System.out.println("parserPserverObjects() - NetworkElementAtrribute data is null");
    		return returnList;
    	}
    	
    	int len = v.size();
    	if ( len < number1 || len < number2 ) {
    		System.out.println("getServiceAndFuncCodeFromVectorString() - Incorrect NetworkElementAtrribute data!");
    		return returnList;    		
    	}
    	
    	for ( int i=0; i<len; i++ ) {
    		String s = v.elementAt(i);
    		String [] arr = s.split(";");
    		String s1 = arr[number1];
    		String s2 = arr[number2];
    		
    		String str = s1 + "___" + s2;
   		
    		if ( i == 0 ) {
    			returnList = str;
    		} else if ( !returnList.contains(str) ) {
    			returnList += ";" + str;
    		}
    	}
    	
    	System.out.println("getServiceAndFuncCodeFromVectorString() - returnList = " + returnList );
    	
    	return returnList;
    }
    
    public Vector<String> getVNFCNodesFromVMNode(
    		final VNode vnodeForVM)
    {
    	
    	Vector<String> vnfc_node_name_list = new Vector<String> ();
    	
    	String vm_func_code = vnodeForVM.getFuncCode();
    	System.out.println("vm_func_code = " + vm_func_code + ",  func_code = " + vm_func_code);
    	
    	Set<Object> keys = vmsequenceProps.keySet();

        TreeMap<String, String> vnfc_func_code_map = new TreeMap<String, String>();
        for(Object k:keys){
            String key = (String) k;
            
            String value = vmsequenceProps.getProperty(key);
            String fc = key.substring(0,4);

            if (vm_func_code.equalsIgnoreCase(fc)) {
            	// System.out.println("key = " + key  + ",  value = " + value);
            	vnfc_func_code_map.put(key, value);
            }
            
        }
        
        int len = vnfc_func_code_map.size();
        // System.out.println("vnfc_func_code_map size = " + len);
        String vm_node_name = vnodeForVM.getNodeName();
        String fc_code = vm_node_name.substring(11, 13);
        
		int fc_code_num = 0;
		try {
			fc_code_num = Integer.parseInt(fc_code);
		} catch (NumberFormatException e) {
			System.out.println("ERROR:   ERROR 1 - NumberFormatException when parsering vm sequence!");
			e.printStackTrace();
		}
		System.out.println("fc_code_num = " + fc_code_num);
        
        
        // case 1
        if ( len == 0 ) {
        	System.out.println("ERROR:   Can not find the vnfc_sequence mapping.  return");
        	return vnfc_node_name_list;
        } else if ( len == 1 ) {
            
        	String key = vnfc_func_code_map.firstKey();
        	// System.out.println("key = " + key);
        	String vnfc_func_code = key.substring(5, 8);
        	String vnfc_node_name = vm_node_name + vnfc_func_code + "001";
        	
        	System.out.println("vnfc_func_code = " + vnfc_func_code + ",  vnfc_node_name = " + vnfc_node_name);
        } else if ( len >= 2 ) {
        	
        	boolean vnfcNodeCreated = false;

    		Set<String> ks = vnfc_func_code_map.keySet();
    		Iterator<String> it = ks.iterator();
    		while ( it.hasNext() ) {
    			String key = (String) it.next();
    			String value = vnfc_func_code_map.get(key);
    			value = value.replaceAll("\"", "");
    			
    			System.out.println("vnfc_func_code_map: value = " + value);
    	        if ( "\\d{3}".equals(value) ) {
    	           	String vnfc_func_code = key.substring(5, 8);
    	        	String vnfc_node_name = vm_node_name + vnfc_func_code + "001"; 
    	        	
    	        	System.out.println("vnfc_node_name = " + vnfc_node_name);
    	        	vnfc_node_name_list.add(vnfc_node_name);
    	        	
    	        } else {
    	        	if ( value == null || value.isEmpty() || value.length() < 3 ) {
    	        		System.out.println("vm_sequence _number is empty!  It must be at least 3-digits data number");
    	        		continue;
    	        	}
    	        	
    	        	int len_1 = value.length();
    	        	int num1=0, num2 = 0;
    	        	if ( len_1 == 3 ) {
    	        		// for pattern = 001
    	        		num1 = Integer.parseInt(value);
    	        		if ( num1 == 0 ) {
    	        			continue;
    	        		} else if ( num1 == fc_code_num ) {
    	        			
    	        			String vnfc_node_name = "";
    	        			String vnfc_func_code = key.substring(5, 8);
    	        			vnfc_node_name = vm_node_name + vnfc_func_code + "001";
    	        			
    	        			// System.out.println("vnfc_node_name = " + vnfc_node_name);
    	    	        	
    	    	        	if ( !vnfc_node_name_list.contains(vnfc_node_name) ) {
    	    	        		vnfc_node_name_list.add(vnfc_node_name);
    	    	        	}
    	        		}
    	        		
    	        	} else if ( len_1 >= 6 ) {
    	        		// len_1 = 6 is for pattern = 00[12]
    	        		// len_1 = 7 is for pattern = 00[1-2]
    	        		// len_1 > 7 is for pattern = (00[3-9]|0[1-9][0-9]|[1-9][0-9][0-9])
    	        		
    	        		String t1 = ""; 
    	        		String t2 = "";
    	        		if ( len_1 == 6 ) {
	    	        		t1 = value.substring(3, 4);
	    	        		t2 = value.substring(4, 5);
    	        		} else if ( len_1 == 7) {
	    	        		t1 = value.substring(3, 4);
	    	        		t2 = value.substring(5, 6);
    	        		} else {
    	        			// for  len_1 > 7 
	    	        		t1 = value.substring(4, 5);
	    	        		t2 = value.substring(6, 7);
    	        		}
    	        		
    	        		try {
							num1 =  Integer.parseInt(t1);
							num2 =  Integer.parseInt(t2);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
    	        		
    	        		// System.out.println("num1 = " + num1 + ",  num2 = " + num2);
 
    	        		if ( num1 == 0 ) {
    	        			continue;
    	        		} else if ( len_1 == 6 ) {
    	        			if ( !vnfcNodeCreated ) {
	    	        			String vnfc_node_name = "";
	    	        			
	    	        			if ( num1 == fc_code_num ) {
		    	        			String vnfc_func_code = key.substring(5, 8);
		    	        			vnfc_node_name = vm_node_name + vnfc_func_code + "001";
	    	        			} 
	
	    	        			// System.out.println("short value: vnfc_node_name = " + vnfc_node_name);
	    	    	        	vnfc_node_name_list.add(vnfc_node_name); 
	    	    	        	vnfcNodeCreated = true;
    	        			}
    	    	        	
    	        		} else if ( len_1 >= 7 && num1 <= fc_code_num && num2 >= fc_code_num ) {
    	        			String vnfc_func_code = key.substring(5, 8);
    	        			String vnfc_node_name = vm_node_name + vnfc_func_code + "001";
    	        			
    	        			// System.out.println("long value: vnfc_node_name = " + vnfc_node_name);
    	    	        	vnfc_node_name_list.add(vnfc_node_name);
    	        		}
    	        		
    	        		System.out.println("");
    	        		
    	        	} else {
    	        		System.out.println("wrong sequence number! vnfc_node_name = " + value);
    	        	}
    	        }
    		}
        	
        }
    	
        System.out.println("vnfc_node_name_list size = " + vnfc_node_name_list.size());
    	return vnfc_node_name_list;
    }
	
    
    
	public static String readJsonStringFromFile(String fileName) {
		
		if ( fileName == null || fileName.isEmpty() ) {
			System.out.println("ERROR:   file name can't be found.  fileName = "+ fileName);
			System.exit(0);
		}
			
		String fileStr = "";
		try {
			fileStr = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
        if ( fileStr == null || fileStr.isEmpty() ) {
        	System.out.println("ERROR:   File is empty.  fileName = "+ fileName);
        	return "ok";
        }
        
        return fileStr;
        
 	}
	
	public String getFuncCode(final String nodeName) 
	{
		String funcCode = "0000";
		if ( nodeName == null || nodeName.isEmpty() ) {
			return funcCode;
		} 
		
		if ( nodeName.length() < 4 ) {
			funcCode = funcCode.toLowerCase();
			// System.out.println("funcCode is less than 4 characters! funcCode = " + funcCode);
			return funcCode;
		}
		
		funcCode = nodeName.substring(0, 4);
		funcCode = funcCode.toLowerCase();
		// System.out.println("funcCode = " + funcCode);
		
		return funcCode;
	}
	

	public boolean storeListToFileAndSend(
			final Vector<String> data, 
			final String datetimestamp,
			final String fn)
	{
		System.out.println("In storeListToFileAndSend");
		PrintWriter writer=null;

		String fileName=fn+"."+datetimestamp + ".txt";
		System.out.println("VNodelIstUtil - storeListToFileAndSend(): File name = " + fileName);
		
		int size = data.size();
		System.out.println("VNodelIstUtil - storeListToFileAndSend(): Number of records = " + size);

		try {
			String configDir = System.getenv("DTI_CONFIG");
			if (configDir != null) {
				// Unix/Linux
				writer = new PrintWriter(System.getenv("DTI") + "/feeds/outgoing/dmaap/" + fileName, "UTF-8");
			} else {
				// Windows
				writer = new PrintWriter("C:\\Temp\\output_files\\" + fileName, "UTF-8");
			} 
			
			for ( int i=0; i<size; i++ ) {
				String rec = data.elementAt(i);
				writer.println(rec); 
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("ERROR:   storeListToFileAndSend:"+e.getMessage());
			return false;
			
		} catch (UnsupportedEncodingException e) {
			
			System.out.println("ERROR:   storeListToFileAndSend:"+e.getMessage());
			return false;
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		String inputFileName=System.getenv("DTI") + "/feeds/outgoing/dmaap/" + fileName;
		String gzFileName=System.getenv("DTI")+ "/feeds/outgoing/dmaap/" + fileName + ".gz";
		
		boolean retVal=compressGzipFile(inputFileName,gzFileName);
		if(retVal)
		{
			Path movefrom = FileSystems.getDefault().getPath(ValidationData.cleanPathString(gzFileName));
		    Path target = FileSystems.getDefault().getPath(ValidationData.cleanPathString(System.getenv("DTI") + "/feeds/outgoing/dmaap/" + fileName + ".gz"));
		     
		    try {
		    	Path deletePath=FileSystems.getDefault().getPath(ValidationData.cleanPathString(inputFileName));
		    	Files.delete(deletePath);
		    	Files.copy(movefrom, target);
		    } catch (IOException e) {
		    	System.out.println("ERROR:   IOException " + e.toString() + " for :"+ fileName);
		    }
			
		//   DmaapDRPub dmaapDRPub= new DmaapDRPub();
		//   dmaapDRPub.publishFile(gzFileName,"");	
		//   dmaapDRPub.closeDmaapClient();
		}
		System.out.println("Finished storeListToFileAndSend");
		return true;
	}
	
	private boolean compressGzipFile(String file, String gzipFile)
	{
		System.out.println("In compressGzipFile");
		FileInputStream fis=null;
		FileOutputStream fos=null;
		GZIPOutputStream gzipOS=null;
		boolean result=true;
        try {
            fis = new FileInputStream(ValidationData.cleanPathString(file));
            fos = new FileOutputStream(ValidationData.cleanPathString(gzipFile));
            gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            
        } catch (IOException e) {
        	System.out.println("ERROR:   compressGzipFile:"+e.getMessage());
            result=false;
        }
        finally {
        	//close resources
        	if(gzipOS!=null)
        	{
        		try {
					gzipOS.close();
				} catch (IOException e) {
					System.out.println("ERROR:   compressGzipFile:"+e.getMessage());

				}
        	}
        	if(fos!=null)
        	{
        		try {
					fos.close();
				} catch (IOException e) {
					System.out.println("ERROR:   compressGzipFile:"+e.getMessage());

				}
        	}
        	if(fis!=null)
        	{
        		try {
					fis.close();
				} catch (IOException e) {
					System.out.println("ERROR:   compressGzipFile:"+e.getMessage());

				}
        	}
		}
        System.out.println("Finished compressGzipFile");
        return result;
	}
	
	

	
	/**
	 * TBD
	 * This method will be replaced by the realtime restful interface to get service object from A&AI
	 * 
	 */
	public void setServiceMap() {
		serviceMap = new TreeMap<String, String>();
		
		String sqlStr = "select service_id, service_description, max(resource_version) from dti.rt_service group by service_id, service_description";
		
		Vector<String> tmpList = selectRecords(sqlStr);
		if ( tmpList == null || tmpList.isEmpty() ) {
			System.out.println("Can't get service values! query result for serviceMap is empty.");
			
			// add those static data
			serviceMap.put("c7611ebe-c324-48f1-8085-94aef0c6ef3d", "HOSTED COMMUNICATIONS");
			serviceMap.put("17cc1042-527b-11e6-beb8-9e71128cae77", "FLEXREACH");
			serviceMap.put("db171b8f-115c-4992-a2e3-ee04cae357e0", "FIRSTNET");
			serviceMap.put("e433710f-9217-458d-a79d-1c7aff376d89", "VIRTUAL USP");

			return;
		} else {
			System.out.println("serviceMap size = " + tmpList.size());
		}
		
		int len = tmpList.size();
		for ( int i=0; i<len; i++ ) {
			String s = tmpList.elementAt(i);
			if ( s == null || s.isEmpty() ) {
				continue;
			} else {
				String arr[] = s.split(";");
				String key = arr[0];
				String value = arr[1];
				System.out.println("key = " + key + ", value = " + value);
				serviceMap.put(key, value);
			}
		}
	}


	public String getEvent() {
		String newEventMessage = "";
		
		String addEvent = configProps.getString("AddEvent");
		System.out.println("addEvent = " + addEvent);
		
		if ( "Y".equalsIgnoreCase(addEvent) ) {
			newEventMessage = configProps.getString("NewEvent");
		}

		return newEventMessage;
	}

	
}
