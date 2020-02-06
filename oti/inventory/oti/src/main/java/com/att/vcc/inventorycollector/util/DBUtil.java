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

package com.att.vcc.inventorycollector.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


import com.att.dcaetd.common.utilities.logging.EcompLogger;
import com.att.dcaetd.common.utilities.logging.LogType;
import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;


public class DBUtil  {

	private static final String subcomponent = "DTI";
	
	private static Map<String,String> llc = new HashMap<String,String>();
	
	private static Logger errorLog=new EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_ERROR).forClass(DBUtil.class).build().getLog();
	private static Logger debugLog=new EcompLogger.Builder().setCommonLoggingContext(subcomponent).withLocalContext(llc).usingLogType(LogType.ECOMP_DEBUG).forClass(DBUtil.class).build().getLog();
	
	public static String PROCESSEDDIR;
	public static String ERRORDIR;
	public static String INPUTDIR;
	public static String EXECUTESHELL;
	
	private static DBUtil thisInstance = null;
	private static Connection dc = null;

	private DBUtil() {
		if (dc == null ) {
			dc = getConnection();
		}
	}
	
	public static DBUtil getInstance() {
		if (thisInstance == null) {
			thisInstance = new DBUtil();
        
		}
		return thisInstance;
	}
	
	
	public Connection getConnection()
	{
		if (dc != null ) {
			System.out.println("Returning connection");
			return dc;
		} else {
			debugLog.debug("dc is null");
			debugLog.debug("Create connection");
		}
		
		String url = System.getenv("PGJDBC_URL");
        String user = System.getenv("PGUSERNAME");
        String password = System.getenv("PGPASSWORD");

        Connection con=null;
        try {
        		
            con = DriverManager.getConnection(url, user, password);
            
            // DatabaseMetaData  databaseMetaData = con.getMetaData();
            // debugLog.debug("Username:"+databaseMetaData.getDriverName()+":"+databaseMetaData.getUserName());

        } catch (SQLException ex) {
        	String errorMsg = "getConnection:"+ex.getMessage();
        	
    	    System.out.println(errorMsg);
    	    errorLog.error(errorMsg);
        } 
        
        debugLog.debug("Finished getConnection");
        return con;
	}
	
	
	public Vector<String> executeQuery(final String sql) throws DTIException
	{
		if ( sql == null || sql.isEmpty() ) {
			System.out.println("(A) sql to be executed: sql is empty! returning...");
		}
		
		Vector<String> records = new Vector<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		String psql = null;
		
		try
		{
			if ( dc != null ) {
				dc.close();
			}
			dc = null;
			dc = getConnection();

			conn =  getConnection(); 
			if (conn == null) {
				errorLog.error("executeQuery: Unable to get database connection in executeQuery");
				throw new DTIException("Connection not made");
			}
			
			conn.setAutoCommit(false);

			psql = sql;
			System.out.println("(A) sql to be executed: sql = " + psql);
			
			stmt = conn.prepareStatement(sql);

			rs = stmt.executeQuery();
			
			ResultSetMetaData rsm = rs.getMetaData();
			int iColumnCount = rsm.getColumnCount();
			while (rs.next()) {
				String value = "";
				StringBuilder output = new StringBuilder();

				for (int i = 1; i < iColumnCount + 1; i++)
				{
					// String column = rsm.getColumnName(i);
					Object columnobject = rs.getObject(i);
					if (rs.wasNull()) {
						value = "null";
					} else {
						value = columnobject.toString();
					}

					if (output.length() != 0) {
						output.append(";");
					}
					// output.append(column + "=" + value);
					output.append(value);
				}
				records.add(output.toString());
			}
			
			if ( rs != null ) {
				rs.close();
			}
			stmt.close();
		} catch (SQLException e) {
			errorLog.error("(A) executeQuery: " + e.getMessage());
			
			errorLog.error("Unable to execute sql." + e.getMessage());
			System.out.println("Unable to execute sql." + e.getMessage());
			
		} catch (Exception e2) 	{
			errorLog.error("executeQuery: case 1 - Exception." + e2.getMessage());
		} finally {
			if ( rs != null ) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null || conn != null ) {
				safeClose(stmt, conn);
			}
		}
		
		System.out.println("records size = " + records.size());
		return records;
	}
	
	
	/**
	 * This method is used to insert or delete a record operation
	 * 
	 * @param sql
	 * @return
	 * @throws DTIException
	 */
	public int executeSql(final Vector<String> sqlList) throws DTIException 
	{
		if ( sqlList == null || sqlList.isEmpty() || sqlList.size() == 0) {
			errorLog.error("(B) Query list is empty.  executeSql returning.");
			return 0;
		}
		
		int len = sqlList.size();
		
		PreparedStatement stmt = null;
		Connection conn = null;
		boolean result = true;
		int count = 0;
		String psql = "";
	
		try {
			// for batch operation
			if ( dc != null ) {
				dc.close();
			}
			dc = null;
			dc = getConnection();
			
			conn = getConnection();
			conn.setAutoCommit(false);
			
			debugLog.debug("(B) executeSql: size = " + len);

			for ( int i=0; i<len; i++ ) {
				String sql = sqlList.elementAt(i);
				
				psql = sql; 	// just in case for printout exception
				// debugLog.debug("sql to be executed: sql = " + sql);
				
				stmt = conn.prepareStatement(sql);
	
				// result = stmt.execute(sql);
				result = stmt.execute();
				
			    if (!result) {
				    // debugLog.debug("database execution is completed.");
			    	count ++;
			    }
			}
			
			commit(conn);
			safeClose(stmt, conn);
			
		} catch (SQLException e) {
			
			rollback(stmt, conn);
			
			errorLog.error("(B) executeSql: " + e.getMessage());
			
			errorLog.error("Unable to execute sql." + e.getMessage());
			System.out.println("Unable to execute sql." + e.getMessage());

			throw new DTIException(e.toString());
		
		} finally {
			
			safeClose(stmt, conn);
		}
		
		// System.out.println("executeSql completed");
		return count;
	}
	
	/**
	 * For data update
	 * 
	 * @param sqlList
	 * @return
	 * @throws DTIException
	 */
	public int executeUpdate(final Vector<String> sqlList) throws DTIException 
	{
		if ( sqlList == null || sqlList.isEmpty() ) {
			debugLog.debug("(C) Query list is empty.  executeUpdate returning.");
			return 0;
		}
		
		int len = sqlList.size();
		
		PreparedStatement stmt = null;
		Connection conn = null;
	
		int updateCount = 0;

		try {
			conn =  getConnection(); 
			if (conn == null) {
				throw new DTIException("Unable to get database connection in update: connection was null");
			}
			conn.setAutoCommit(false);

			for ( int i=0; i<len; i++ ) {
				String sql = sqlList.elementAt(i);
				System.out.println("(C) -- executeUpdate(): sql query = " + sql);

				debugLog.debug("record to be updated: sql = " + sql);
				
				stmt = conn.prepareStatement(sql);
			
				stmt.executeUpdate();
				updateCount ++;
			}
			
			commit(conn);
			safeClose(stmt, conn);
			
		} catch (SQLException e) {
			errorLog.error("Unable to update records");
			
			rollback(stmt, conn);
			
			throw new DTIException(e.toString());

		} finally {
			safeClose(stmt, conn);
		}
		
		return updateCount;
	}

	
	public void rollback(PreparedStatement stmt, Connection conn) throws DTIException {
		try {
			conn.rollback();
		} catch (SQLException e) {
			errorLog.error("rollback exception received: " + e.toString(), e);
			throw new DTIException(e.toString());
		} finally {
			if (stmt != null || conn != null ) {
				safeClose(stmt, conn);
			}
		}
	}

	public void commit(Connection conn) throws DTIException {
		try {
			conn.commit();
		} catch (SQLException e) {
			errorLog.error("commit: " + e.toString(), e);
			throw new DTIException("Error committing updates: " + e.getMessage());
		}
	}
	
	public static void safeClose(PreparedStatement stmt, Connection conn) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				errorLog.error("Error occured while DB resource is closed");
			}
		}
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				errorLog.error("Error occured while DB resource is closed");
			}
		}
	}
		
}
