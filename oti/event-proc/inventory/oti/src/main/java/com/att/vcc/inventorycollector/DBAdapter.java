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

import com.att.vcc.inventorycollector.util.Constants;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.att.ecomp.entity.EcompComponent;
import com.att.ecomp.entity.EcompSubComponent;
import com.att.ecomp.entity.EcompSubComponentInstance;
import com.att.ecomp.logger.EcompLogger;
import com.att.vcc.inventorycollector.domain.*;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.messages.inventoryCollectorOperationEnum;

public class DBAdapter {
	private static SessionFactory factory = null;
	private static final int NO_UPDATE = 0;
	private static final int SUCCESS_UPDATE = 1;
	private static final int FAILURE_UPDATE = -1;
	private static final String emptyString = "";
	private static EcompLogger ecompLogger;

	public static String PROCESSEDDIR;
	public static String ERRORDIR;
	public static String INPUTDIR;
	public static String EXECUTESHELL;

	// Initialize log configuration
	static {
		EcompComponent.initialize("DCAE");
		EcompSubComponent.initialize("dti");
		EcompSubComponentInstance.initialize();
		ecompLogger = EcompLogger.getEcompLogger();
	}

	final List<String> TABLE_LIST = Arrays.asList("AvailabilityZone", "Flavor", "Pserver", "Complex", "Customer",
			"Dvsswitch", "GenericVnf", "Image", "Oamnetwork", "Networkprofile", "Physicallink", "Service", "Vnfimage",
			"Vpe", "Vserver", "Tenant", "Vce", "Virtualdatacenter", "Linterface", "Pinterface", "L3network", "Vlan",
			"serviceInstance", "L3InterfaceIpv4AddressList", "L3InterfaceIpv6AddressList", "Laginterface", "Laglink",
			"Logicallink", "serviceSubscription", "Subnet", "Portgroup", "Volume", "RelationshipList", "Cloudregion",
			"Newvce", "Vfmodule", "ServiceCapability", "Vnfc", "Pnf", "SriovVf", "VplsPe", "Metadatum", "Model",
			"ModelVer", "Zone");
	final List<String> KEY_LIST = Arrays.asList("availabilityZoneName", "flavorid", "hostname", "physicallocationid",
			"globalcustomerid", "switchname", "vnfid", "imageid", "networkuuid", "nmprofilename", "linkname",
			"serviceid", "attuuid", "vnfid", "vserverid,tenantid,cloudowner,cloudregionid",
			"tenantid,cloudowner,cloudregionid", "vnfid", "vdcid", "interfacename,parententitytype,parententityid",
			"interfacename,hostname", "networkid", "vlaninterface,interfacename,interfaceparentid",
			"serviceinnstanceid,servicetype,globalcustomerid", "ipv4address,parentinterfacename,grandparententityid",
			"ipv6address,parentinterfacename,grandparententityid", "interfacename", "linkname", "linkname",
			"servicetype,globalcustomerid", "subnetid,networkid", "interfaceid,vnfid", "volumeid,vserverid",
			"fromNodeId,toNodeId,relatedFrom,relatedTo", "cloudowner,cloudregionid", "vnfid2", "vfmoduleid",
			"servicetype", "vnfcname", "pnfname", "pciid,parententityid", "equipmentname", "metaname,parententityid",
			"modelinvariantid", "modelversionid,modelinvariantid", "zoneid");

	public DBAdapter() {
		ecompLogger.setLogContext(inventoryCollectorOperationEnum.DTILoadToPostgresOperation, "Service-Instance-ID",
				null, "Postgres DB");

		StandardServiceRegistry serviceRegistry = null;
		Configuration configuration = null;

		try {

			configuration = new Configuration().configure();
			configuration.setProperty("hibernate.connection.url", System.getenv("PGJDBC_URL"));
			configuration.setProperty("hibernate.connection.username", System.getenv("PGUSERNAME"));
			configuration.setProperty("hibernate.connection.password", System.getenv("PGPASSWORD"));
			// ecompLogger.debug("PGJDBC URL: "+System.getenv("PGJDBC_URL"));
			// ecompLogger.debug("PG Username: "+System.getenv("PGUSERNAME"));
			// ecompLogger.debug("PG password: "+System.getenv("PGPASSWORD"));

			// configuration.setProperty("hibernate.connection.url",
			// "jdbc:postgresql://dcae-pstg-write-ftl2.homer.att.com/dti");
			// configuration.setProperty("hibernate.connection.username",
			// "dti_user");
			// configuration.setProperty("hibernate.connection.password",
			// "test234-ftlu");

			serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
			factory = configuration.buildSessionFactory(serviceRegistry);

		} catch (Throwable ex) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_FAILED_CREATE_SESSIONFACTORY_ERROR,
					ex.getMessage());
		}

	}

	public SessionFactory getDBfactory() {
		return factory;
	}

	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				ecompLogger.info(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_ADD_SHUTDOWN_HOOK_DEBUG);
				if (factory != null)
					factory.close();

			}
		});
		ecompLogger.info(inventoryCollectorMessageEnum.DTI_DBADAPTER_SHUTDOWN_HOOK_ATTACHED_DEBUG);
	}

	public boolean insertTable(Object obj) {
		boolean result = true;
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "insertTable " + obj.getClass().getName());

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(obj);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_OBJECT_ERROR,
					obj.getClass().getName(), e.getCause().getMessage());
			result = false;
		} finally {
			session.close();
		}
		return result;
	}

	public boolean updateAndInsertTable(String str, List<String> paramList, Object obj) {
		boolean result = true;

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(str);
			for (String param : paramList) {
				String[] arr = param.split(":");
				query.setString(arr[0], arr[1]);
			}
			query.executeUpdate();
			session.save(obj);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			result = false;
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_INSERT_OBJECT_ERROR,
					str, obj.getClass().getName(), e.getCause().getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	public boolean updateOrInsertTable(Object obj) {
		boolean result = true;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(obj);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			result = false;
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_INSERT_OBJECT_ERROR,
					obj.getClass().getName(), e.getCause().getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	public int updateTable(String str, List<String> paramList) {
		int result = NO_UPDATE;

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(str);
			for (String param : paramList) {
				String[] arr = param.split(":");
				query.setString(arr[0], arr[1]);
			}
			result = query.executeUpdate();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			result = FAILURE_UPDATE;
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR, str,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	public int deleteTableRow(String str, List<String> paramList) {
		int result = NO_UPDATE;

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(str);
			for (String param : paramList) {
				String[] arr = param.split(":");
				query.setString(arr[0], arr[1]);
			}
			result = query.executeUpdate();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			result = FAILURE_UPDATE;
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR, str,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	public List queryTable(String strQuery, List<String> paramList) {

		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(strQuery);

			for (String param : paramList) {
				String[] arr = param.split(":");
				query.setString(arr[0], arr[1]);
			}
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR, strQuery,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public int processNaradChassis(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for realtime event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? null : arrData[0];
		String chassistype = arrData[1].equals("") ? null : arrData[1];
		String chassisrole = arrData[2].equals("") ? null : arrData[2];
		String serialnumber = arrData[3].equals("") ? null : arrData[3];
		String assettag = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];
		NaradChassis chassis = new NaradChassis(chassisname, chassistype, chassisrole, serialnumber, assettag,
				resourceversion, formattedDate);

		String querySql = " where c.chassisname =:chassisname";
		String invalidateSql = "delete from NaradChassis c " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(chassis);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradPluggableSlot(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPluggableSlot");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 6)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? emptyString : arrData[0];
		String cardslotname = arrData[1].equals("") ? null : arrData[1];
		String cardtype = arrData[2].equals("") ? null : arrData[2];
		String pluggableslotname = arrData[3].equals("") ? null : arrData[3];
		String resourceversion = arrData[4].equals("") ? null : arrData[4];
		String formattedDate = arrData[5].equals("") ? null : arrData[5];
		NaradPluggableSlot pluggableslot = new NaradPluggableSlot(chassisname, pluggableslotname, cardslotname,
				cardtype, resourceversion, formattedDate);

		String querySql = " where p.physicallocationid =:physicallocationid AND ";
		querySql += "p.rackname =:rackname AND p.chassisname =:chassisname AND p.cardslotname =:cardslotname ";
		querySql += "p.cardtype =:cardtype AND p.pluggableslotname =:pluggableslotname";
		String invalidateSql = "delete from NaradPluggableSlot p " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname, "cardslotname:" + cardslotname,
				"cardtype:" + cardtype, "pluggableslotname:" + pluggableslotname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(pluggableslot);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradPluggable(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPluggable");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? null : arrData[0];
		String cardslotname = arrData[1].equals("") ? null : arrData[1];
		String cardtype = arrData[2].equals("") ? null : arrData[2];
		String pluggableslotname = arrData[3].equals("") ? null : arrData[3];
		String pluggabletype = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];
		NaradPluggable pluggable = new NaradPluggable(chassisname, pluggableslotname, pluggabletype, cardslotname,
				cardtype, resourceversion, formattedDate);

		String querySql = " where p.chassisname =:chassisname AND p.cardslotname =:cardslotname ";
		querySql += "p.cardtype =:cardtype AND p.pluggableslotname =:pluggableslotname AND p.pluggabletype =:pluggabletype";
		String invalidateSql = "delete from NaradPluggable p " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname, "cardslotname:" + cardslotname,
				"cardtype:" + cardtype, "pluggableslotname:" + pluggableslotname, "pluggabletype:" + pluggabletype);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(pluggable);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradCable(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processCable");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}

		String[] arrData = data.split("\\^");
		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String physicallocationid = arrData[0].equals("") ? emptyString : arrData[0];
		String cablename = arrData[1].equals("") ? null : arrData[1];
		String cabletype = arrData[2].equals("") ? null : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];
		NaradCable cable = new NaradCable(physicallocationid, cablename, cabletype, resourceversion, formattedDate);

		String querySql = " where c.physicallocationid =:physicallocationid AND c.cablename =:cablename";
		String invalidateSql = "delete from NaradCable c " + querySql;
		List<String> paramList = Arrays.asList("physicallocationid:" + physicallocationid, "cablename:" + cablename);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(cable);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradCard(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processCard");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? null : arrData[0];
		String cardslotname = arrData[1].equals("") ? null : arrData[1];
		String cardtype = arrData[2].equals("") ? null : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];
		NaradCard card = new NaradCard(chassisname, cardslotname, cardtype, resourceversion, formattedDate);

		String querySql = " where c.physicallocationid =:physicallocationid AND ";
		querySql += "c.rackname =:rackname AND c.chassisname =:chassisname AND c.cardslotname =:cardslotname ";
		querySql += "c.cardtype =:cardtype";
		String invalidateSql = "delete from NaradCard c " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname, "cardslotname:" + cardslotname,
				"cardtype:" + cardtype);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(card);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradCardSlot(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processCardSlot");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? null : arrData[0];
		String cardslotname = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];
		NaradCardSlot cardslot = new NaradCardSlot(chassisname, cardslotname, resourceversion, formattedDate);

		String querySql = " where c.chassisname =:chassisname AND c.cardslotname =:cardslotname ";
		String invalidateSql = "delete from NaradCardSlot c " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname, "cardslotname:" + cardslotname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(cardslot);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNaradRack(String data, String action, String entityType, String eventType) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG, "ProcessNaradRack");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 8)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String physicallocationid = arrData[0].equals("") ? emptyString : arrData[0];
		String rackname = arrData[1].equals("") ? null : arrData[1];
		String racktype = arrData[2].equals("") ? null : arrData[2];
		String racklocation = arrData[3].equals("") ? null : arrData[3];
		String rackposition = arrData[4].equals("") ? null : arrData[4];
		String rackpowerdiversity = arrData[5].equals("") ? null : arrData[5];
		String resourceversion = arrData[6].equals("") ? null : arrData[6];
		String formattedDate = arrData[7].equals("") ? null : arrData[7];

		NaradRack naradRack = new NaradRack(physicallocationid, rackname, racktype, racklocation, rackposition,
				rackpowerdiversity, resourceversion, formattedDate);

		String querySql = " where r.physicallocationid =:physicallocationid AND r.rackname =:rackname";
		String invalidateSql = "delete from NaradRack r" + querySql;
		List<String> paramList = Arrays.asList("physicallocationid:" + physicallocationid, "rackname:" + rackname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(naradRack);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processNaradPort(String data, String action, String entityType, String eventType) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG, "ProcessNaradPort");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 12)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String chassisname = arrData[0].equals("") ? null : arrData[0];
		String cardslotname = arrData[1].equals("") ? null : arrData[1];
		String cardtype = arrData[2].equals("") ? null : arrData[2];
		String pluggableslotname = arrData[3].equals("") ? null : arrData[3];
		String pluggabletype = arrData[4].equals("") ? null : arrData[4];
		String portname = arrData[5].equals("") ? null : arrData[5];
		String porttype = arrData[6].equals("") ? null : arrData[6];
		String portrole = arrData[7].equals("") ? null : arrData[7];
		String speedvalue = arrData[8].equals("") ? null : arrData[8];
		String speedunits = arrData[9].equals("") ? null : arrData[9];
		String resourceversion = arrData[10].equals("") ? null : arrData[10];
		String formattedDate = arrData[11].equals("") ? null : arrData[11];

		NaradPort naradPort = new NaradPort(chassisname, portname, cardslotname, cardtype, pluggableslotname,
				pluggabletype, porttype, portrole, speedvalue, speedunits, resourceversion, formattedDate);

		String querySql = " where p.chassisname =:chassisname AND p.cardslotname =:cardslotname ";
		querySql += "p.cardtype =:cardtype AND p.pluggableslotname =:pluggableslotname AND ";
		querySql += "p.pluggabletype =:pluggabletype AND p.portname =:portname";
		String invalidateSql = "delete from pluggable p " + querySql;
		List<String> paramList = Arrays.asList("chassisname:" + chassisname, "cardslotname:" + cardslotname,
				"cardtype:" + cardtype, "pluggableslotname:" + pluggableslotname, "pluggabletype:" + pluggabletype,
				"portname:" + portname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(naradPort);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processNaradInstanceGroup(String data, String action, String entityType, String eventType) {
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG, "ProcessNaradPort");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 8)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String id = arrData[0].equals("") ? emptyString : arrData[0];
		String instanceGroupRole = arrData[1].equals("") ? null : arrData[1];
		String modelInvariantId = arrData[2].equals("") ? null : arrData[2];
		String modelVersionId = arrData[3].equals("") ? null : arrData[3];
		String description = arrData[4].equals("") ? null : arrData[4];
		String instanceGroupType = arrData[5].equals("") ? null : arrData[5];
		String resourceVersion = arrData[6].equals("") ? null : arrData[6];
		String instanceGroupName = arrData[7].equals("") ? null : arrData[7];
		String instanceGroupFunction = arrData[8].equals("") ? null : arrData[8];
		String formattedDate = arrData[9].equals("") ? null : arrData[9];

		NaradInstanceGroup naradInstanceGroup = new NaradInstanceGroup(id, instanceGroupRole, modelInvariantId,
				modelVersionId, description, instanceGroupType, resourceVersion, instanceGroupName,
				instanceGroupFunction, formattedDate);

		String querySql = " where id =:id";
		String invalidateSql = "delete from NaradInstanceGroup " + querySql;
		List<String> paramList = Arrays.asList("id:" + id);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(naradInstanceGroup);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processVirtualDataCenter(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVirtualDataCenter");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vdcid = arrData[0].equals("") ? emptyString : arrData[0];
		String vdcname = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];
		Virtualdatacenter vdc = new Virtualdatacenter(vdcid, vdcname, resourceversion, formattedDate);

		String querySql = " where v.vdcid =:vdcid";
		String invalidateSql = "delete from Virtualdatacenter v " + querySql;
		List<String> paramList = Arrays.asList("vdcid:" + vdcid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vdc);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processVce(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVce");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 18)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfid = arrData[0].equals("") ? emptyString : arrData[0];
		String vnfname = arrData[1].equals("") ? null : arrData[1];
		String vnfname2 = arrData[2].equals("") ? null : arrData[2];
		String vnftype = arrData[3].equals("") ? null : arrData[3];
		String serviceid = arrData[4].equals("") ? null : arrData[4];
		String regionalresourcezone = arrData[5].equals("") ? null : arrData[5];
		String provstatus = arrData[6].equals("") ? null : arrData[6];
		String operationalstatus = arrData[7].equals("") ? null : arrData[7];
		String equipmentrole = arrData[8].equals("") ? null : arrData[8];
		String orchestrationstatus = arrData[9].equals("") ? null : arrData[9];
		String heatstackid = arrData[10].equals("") ? null : arrData[10];
		String msocatalogkey = arrData[11].equals("") ? null : arrData[11];
		String vpeid = arrData[12].equals("") ? null : arrData[12];
		String v6vcewanaddress = arrData[13].equals("") ? null : arrData[13];
		String ipv4oamaddress = arrData[14].equals("") ? null : arrData[14];
		String resourceversion = arrData[15].equals("") ? null : arrData[15];
		String ipv4loopback0address = arrData[16].equals("") ? null : arrData[16];
		String formattedDate = arrData[17].equals("") ? null : arrData[17];
		Vce vce = new Vce(vnfid, vnfname, vnfname2, vnftype, serviceid, regionalresourcezone, provstatus,
				operationalstatus, equipmentrole, orchestrationstatus, heatstackid, msocatalogkey, vpeid,
				v6vcewanaddress, ipv4oamaddress, resourceversion, ipv4loopback0address, formattedDate);

		String querySql = " where v.vnfid =:vnfid";
		String invalidateSql = "delete from Vce v " + querySql;
		List<String> paramList = Arrays.asList("vnfid:" + vnfid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vce);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processTenant(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processTenant");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String tenantid = arrData[0].equals("") ? emptyString : arrData[0];
		String tenantname = arrData[1].equals("") ? null : arrData[1];
		String tenantcontext = arrData[2].equals("") ? null : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String cloudowner = arrData[4].equals("") ? null : arrData[4];
		String cloudregionid = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];

		Tenant tenant = new Tenant(tenantid, cloudowner, cloudregionid, tenantname, resourceversion, tenantcontext,
				formattedDate);
		String querySql = " where t.tenantid =:tenantid and t.cloudowner =:cloudowner and t.cloudregionid =:cloudregionid";
		String invalidateSql = "delete from Tenant t " + querySql;
		List<String> paramList = Arrays.asList("tenantid:" + tenantid, "cloudowner:" + cloudowner,
				"cloudregionid:" + cloudregionid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(tenant);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processNosSserver(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVserver");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 11)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String cloudOwner = arrData[0].equals("") ? emptyString : arrData[0];
		String cloudRegionId = arrData[1].equals("") ? null : arrData[1];
		String tenantId = arrData[2].equals("") ? null : arrData[2];
		String nosServerId = arrData[3].equals("") ? null : arrData[3];
		String nosServerName = arrData[4].equals("") ? null : arrData[4];
		String vendor = arrData[5].equals("") ? null : arrData[5];
		String provStatus = arrData[6].equals("") ? null : arrData[6];
		String nosServerSelflink = arrData[7].equals("") ? null : arrData[7];
		String isInMaint = arrData[8].equals("") ? null : arrData[8];
		String resourceVersion = arrData[9].equals("") ? null : arrData[9];
		String formattedDate = arrData[10].equals("") ? null : arrData[10];

		NosServer nosServer = new NosServer(cloudOwner, cloudRegionId, tenantId, nosServerId, nosServerName, vendor,
				provStatus, nosServerSelflink, isInMaint, resourceVersion, formattedDate);
		String querySql = " where n.nosserverid =:nosserverid and cloudowner=:cloudowner ";
		querySql += "and cloudregionid=:cloudregionid and tenantid=:tenantid";
		String invalidateSql = "delete from Vserver v " + querySql;
		List<String> paramList = Arrays.asList("nosserverid:" + nosServerId, "cloudowner:" + cloudOwner,
				"cloudregionid:" + cloudRegionId, "tenantid:" + tenantId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(nosServer);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;

			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
		}

		return retVal;
	}

	public int processVserver(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVserver");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 12)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vserverId = arrData[0].equals("") ? emptyString : arrData[0];
		String vserverName = arrData[1].equals("") ? null : arrData[1];
		String vserverName2 = arrData[2].equals("") ? null : arrData[2];
		String provStatus = arrData[3].equals("") ? null : arrData[3];
		String selfLink = arrData[4].equals("") ? null : arrData[4];
		String isInMaint = arrData[5].equals("") ? null : arrData[5];
		String isClosedLoopDisabled = arrData[6].equals("") ? null : arrData[6];
		String resourceVersion = arrData[7].equals("") ? null : arrData[7];
		String tenantId = arrData[8].equals("") ? null : arrData[8];
		String cloudOwner = arrData[9].equals("") ? null : arrData[9];
		String cloudRegionId = arrData[10].equals("") ? null : arrData[10];
		String formattedDate = arrData[11].equals("") ? null : arrData[11];

		Vserver vserver = new Vserver(vserverId, tenantId, cloudOwner, cloudRegionId, vserverName, vserverName2,
				provStatus, selfLink, isInMaint, isClosedLoopDisabled, resourceVersion, formattedDate);
		String querySql = " where v.vserverid =:vserverid";
		String invalidateSql = "delete from Vserver v " + querySql;
		List<String> paramList = Arrays.asList("vserverid:" + vserverId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vserver);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;

			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
		}

		return retVal;
	}

	public int processVpe(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVpe");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 22)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfid = arrData[0].equals("") ? emptyString : arrData[0];
		String vnfname = arrData[1].equals("") ? null : arrData[1];
		String vnfname2 = arrData[2].equals("") ? null : arrData[2];
		String vnftype = arrData[3].equals("") ? null : arrData[3];
		String serviceid = arrData[4].equals("") ? null : arrData[4];
		String regionalresourcezone = arrData[5].equals("") ? null : arrData[5];
		String provstatus = arrData[6].equals("") ? null : arrData[6];
		String operationalstatus = arrData[7].equals("") ? null : arrData[7];
		String equipmentrole = arrData[8].equals("") ? null : arrData[8];
		String orchestrationstatus = arrData[9].equals("") ? null : arrData[9];
		String heatstackid = arrData[10].equals("") ? null : arrData[10];
		String msocatalogkey = arrData[11].equals("") ? null : arrData[11];
		String ipv4OamAddress = arrData[12].equals("") ? null : arrData[12];
		long ipv4OamGatewayAddressPrefixLength = 0L;
		try {
			ipv4OamGatewayAddressPrefixLength = arrData[13].equals("") ? 0L : Long.parseLong(arrData[13]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}
		String ipv4OamGatewayAddress = arrData[14].equals("") ? null : arrData[14];
		String v4Loopback0IpAddress = arrData[15].equals("") ? null : arrData[15];
		String vlanIdOuter = arrData[16].equals("") ? null : arrData[16];
		String asNumber = arrData[17].equals("") ? null : arrData[17];
		String resourceVersion = arrData[18].equals("") ? null : arrData[18];
		String summarystatus = arrData[19].equals("") ? null : arrData[19];
		String encryptedaccessflag = arrData[20].equals("") ? null : arrData[20];
		String formattedDate = arrData[21].equals("") ? null : arrData[21];
		Vpe vpe = new Vpe(vnfid, vnfname, vnfname2, vnftype, serviceid, regionalresourcezone, provstatus,
				operationalstatus, equipmentrole, orchestrationstatus, heatstackid, msocatalogkey, ipv4OamAddress,
				ipv4OamGatewayAddressPrefixLength, ipv4OamGatewayAddress, v4Loopback0IpAddress, vlanIdOuter, asNumber,
				resourceVersion, summarystatus, encryptedaccessflag, formattedDate);

		String querySql = " where v.vnfid =:vnfid";
		String invalidateSql = "delete from Vpe v " + querySql;
		List<String> paramList = Arrays.asList("vnfid:" + vnfid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vpe);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processVolume(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVolume");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String volumeid = arrData[0].equals("") ? emptyString : arrData[0];
		String volumeselflink = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String vserverid = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];
		Volume volume = new Volume(volumeid, vserverid, volumeselflink, resourceversion, formattedDate);

		String querySql = "where v.volumeid =:volumeid and v.vserverid=:vserverid";
		String invalidateSql = "delete from Volume v " + querySql;
		List<String> paramList = Arrays.asList("volumeid:" + volumeid, "vserverid:" + vserverid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(volume);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processVnfImage(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVnfImage");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfimageuuid = arrData[0].equals("") ? emptyString : arrData[0];
		String application = arrData[1].equals("") ? null : arrData[1];
		String applicationvendor = arrData[2].equals("") ? null : arrData[2];
		String applicationversion = arrData[3].equals("") ? null : arrData[3];
		String selflink = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];
		Vnfimage vnfimage = new Vnfimage(vnfimageuuid, application, applicationvendor, applicationversion, selflink,
				resourceversion, formattedDate);

		String querySql = "where v.vnfimageuuid =:vnfimageuuid";
		String invalidateSql = "delete from Vnfimage v " + querySql;
		List<String> paramList = Arrays.asList("vnfimageuuid:" + vnfimageuuid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vnfimage);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processVlan(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVlan");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String vlaninterface = arrData[0].equals("") ? emptyString : arrData[0];
		long vlanidinner = 0L;
		long vlanidouter = 0L;

		try {
			vlanidinner = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
			vlanidouter = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		long speedValue = 0;
		try {
			speedValue = arrData[4].equals("") ? 0L : Long.parseLong(arrData[4]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}
		String speedUnits = arrData[5].equals("") ? null : arrData[5];
		String vlanType = null;
		String vlanDesc = null;
		String backdoorConnection = null;
		String vpnKey = null;
		String orchestrationStatus = null;
		String inmaint = null;
		String provstatus = null;
		String isIpUnnumbered = null;
		String interfacename = null;
		String grandparententitytype = null;
		String intId = null;
		String cloudregiontenant = null;
		String pinterfacename = null;
		String laginterfacename = null;
		String formattedDate = null;

		Vlan vlan = null;
		NaradVlan vlanNarad = null;
		String querySql = " where v.vlaninterface =:vlaninterface and v.interfacename=:interfacename and "
				+ "v.grandparententityid=:grandparententityid";
		String invalidateSql = null;

		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			vlanDesc = arrData[6].equals("") ? null : arrData[6];
			backdoorConnection = arrData[7].equals("") ? null : arrData[7];
			vpnKey = arrData[8].equals("") ? null : arrData[8];
			orchestrationStatus = arrData[9].equals("") ? null : arrData[9];
			inmaint = arrData[10].equals("") ? null : arrData[10];
			provstatus = arrData[11].equals("") ? null : arrData[11];
			isIpUnnumbered = arrData[12].equals("") ? null : arrData[12];
			interfacename = arrData[13].equals("") ? null : arrData[13];
			grandparententitytype = arrData[14].equals("") ? null : arrData[14];
			intId = arrData[15].equals("") ? null : arrData[15];
			cloudregiontenant = arrData[16].equals("") ? null : arrData[16];
			pinterfacename = arrData[17].equals("") ? null : arrData[17];
			laginterfacename = arrData[18].equals("") ? null : arrData[18];
			formattedDate = arrData[19].equals("") ? null : arrData[19];
			if (arrData.length < 20)
				return FAILURE_UPDATE;
			vlan = new Vlan(vlaninterface, interfacename, intId, cloudregiontenant, pinterfacename, laginterfacename,
					vlanidinner, vlanidouter, resourceversion, speedValue, speedUnits, vlanDesc, backdoorConnection,
					vpnKey, orchestrationStatus, inmaint, provstatus, isIpUnnumbered, grandparententitytype,
					formattedDate);
			invalidateSql = "delete from Vlan v " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			vlanType = arrData[6].equals("") ? null : arrData[6];
			vlanDesc = arrData[7].equals("") ? null : arrData[7];
			backdoorConnection = arrData[8].equals("") ? null : arrData[8];
			vpnKey = arrData[9].equals("") ? null : arrData[9];
			orchestrationStatus = arrData[10].equals("") ? null : arrData[10];
			inmaint = arrData[11].equals("") ? null : arrData[11];
			provstatus = arrData[12].equals("") ? null : arrData[12];
			isIpUnnumbered = arrData[13].equals("") ? null : arrData[13];
			interfacename = arrData[14].equals("") ? null : arrData[14];
			grandparententitytype = arrData[15].equals("") ? null : arrData[15];
			intId = arrData[16].equals("") ? null : arrData[16];
			cloudregiontenant = arrData[17].equals("") ? null : arrData[17];
			pinterfacename = arrData[18].equals("") ? null : arrData[18];
			laginterfacename = arrData[19].equals("") ? null : arrData[19];
			formattedDate = arrData[20].equals("") ? null : arrData[20];
			if (arrData.length < 21)
				return FAILURE_UPDATE;
			vlanNarad = new NaradVlan(vlaninterface, interfacename, intId, cloudregiontenant, pinterfacename,
					laginterfacename, vlanidinner, vlanidouter, resourceversion, speedValue, speedUnits, vlanType, vlanDesc,
					backdoorConnection, vpnKey, orchestrationStatus, inmaint, provstatus, isIpUnnumbered,
					grandparententitytype, formattedDate);
			invalidateSql = "delete from NaradVlan v " + querySql;
		}
		List<String> paramList = Arrays.asList("vlaninterface:" + vlaninterface, "interfacename:" + interfacename,
				"grandparententityid:" + intId);

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(vlan);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(vlanNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processServiceSubscription(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String servicetype = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String custId = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];
		serviceSubscription ss = new serviceSubscription(servicetype, custId, resourceversion, formattedDate);

		String querySql = "where s.servicetype =:servicetype and s.globalcustomerid=:globalcustomerid";
		String invalidateSql = "delete from serviceSubscription s " + querySql;
		List<String> paramList = Arrays.asList("servicetype:" + servicetype, "globalcustomerid:" + custId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(ss);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processServiceInstance(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processServiceInstance");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 22)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String serviceInstanceId = arrData[0].equals("") ? emptyString : arrData[0];
		String serviceInstanceName = arrData[1].equals("") ? null : arrData[1];
		String modelInvariantId = arrData[2].equals("") ? null : arrData[2];
		String modelVersionId = arrData[3].equals("") ? null : arrData[3];
		String widgetModelId = arrData[4].equals("") ? null : arrData[4];
		String widgetModelVersion = arrData[5].equals("") ? null : arrData[5];
		String bandwidthTotal = arrData[6].equals("") ? null : arrData[6];
		String bandwidthUpWan1 = arrData[7].equals("") ? null : arrData[7];
		String bandwidthDownWan1 = arrData[8].equals("") ? null : arrData[8];
		String bandwidthUpWan2 = arrData[9].equals("") ? null : arrData[9];
		String bandwidthDownWan2 = arrData[10].equals("") ? null : arrData[10];
		String vhnPortalUrl = arrData[11].equals("") ? null : arrData[11];
		String serviceInstanceLocationId = arrData[12].equals("") ? null : arrData[12];
		String resourceVersion = arrData[13].equals("") ? null : arrData[13];
		String selflink = arrData[14].equals("") ? null : arrData[14];
		String orchestrationStatus = arrData[15].equals("") ? null : arrData[15];
		String serviceType = arrData[16].equals("") ? null : arrData[16];
		String servicerole = arrData[17].equals("") ? null : arrData[17];
		String environmentcontext = arrData[18].equals("") ? null : arrData[18];
		String workloadcontext = arrData[19].equals("") ? null : arrData[19];
		String globalCustomerId = arrData[20].equals("") ? null : arrData[20];
		String formattedDate = arrData[21].equals("") ? null : arrData[21];

		serviceInstance si = new serviceInstance(serviceInstanceId, serviceType, globalCustomerId, serviceInstanceName,
				modelInvariantId, modelVersionId, widgetModelId, widgetModelVersion, bandwidthTotal, bandwidthUpWan1,
				bandwidthDownWan1, bandwidthUpWan2, bandwidthDownWan2, vhnPortalUrl, serviceInstanceLocationId,
				resourceVersion, selflink, orchestrationStatus, servicerole, environmentcontext, workloadcontext,
				formattedDate);

		String querySql = "where s.serviceinnstanceid =:serviceinnstanceid and s.servicetype=:servicetype and s.globalcustomerid=:globalcustomerid";
		String invalidateSql = "delete from serviceInstance s " + querySql;
		List<String> paramList = Arrays.asList("serviceinnstanceid:" + serviceInstanceId, "servicetype:" + serviceType,
				"globalcustomerid:" + globalCustomerId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(si);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processAllottedResource(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 14)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String globalCustomerId = arrData[0].equals("") ? null : arrData[0];
		String serviceType = arrData[1].equals("") ? null : arrData[1];
		String serviceInstanceId = arrData[2].equals("") ? emptyString : arrData[2];
		String id = arrData[3].equals("") ? null : arrData[3];
		String description = arrData[4].equals("") ? null : arrData[4];
		String selflink = arrData[5].equals("") ? null : arrData[5];
		String modelInvariantId = arrData[6].equals("") ? null : arrData[6];
		String modelVersionId = arrData[7].equals("") ? null : arrData[7];
		String resourceVersion = arrData[8].equals("") ? null : arrData[8];
		String orchestrationStatus = arrData[9].equals("") ? null : arrData[9];
		String operationalStatus = arrData[10].equals("") ? null : arrData[10];
		String type = arrData[11].equals("") ? null : arrData[11];
		String role = arrData[12].equals("") ? null : arrData[12];
		String formattedDate = arrData[13].equals("") ? null : arrData[13];

		AllottedResource allottedResource = new AllottedResource(globalCustomerId, serviceType, serviceInstanceId, id,
				description, selflink, modelInvariantId, modelVersionId, resourceVersion, orchestrationStatus,
				operationalStatus, type, role, formattedDate);

		String querySql = "where a.globalcustomerid=:globalcustomerid and a.servicetype=:servicetype and "
				+ "a.serviceinnstanceid =:serviceinnstanceid and a.id=:id";
		String invalidateSql = "delete from AllottedResource a " + querySql;
		List<String> paramList = Arrays.asList("globalcustomerid:" + globalCustomerId, "servicetype:" + serviceType,
				"serviceinnstanceid:" + serviceInstanceId, "id:" + id);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(allottedResource);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processService(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processService");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String serviceid = arrData[0].equals("") ? emptyString : arrData[0];
		String servicedescription = arrData[1].equals("") ? null : arrData[1];
		String serviceselflink = arrData[2].equals("") ? null : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];

		Service service = new Service(serviceid, servicedescription, serviceselflink, resourceversion, formattedDate);

		String querySql = "where s.serviceid =:serviceid";
		String invalidateSql = "delete from Service s " + querySql;
		List<String> paramList = Arrays.asList("serviceid:" + serviceid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(service);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processPserver(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPserver");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String hostname = arrData[0].equals("") ? emptyString : arrData[0];
		String ptniiequipname = arrData[1].equals("") ? null : arrData[1];
		String equiptype = arrData[5].equals("") ? null : arrData[5];
		String equipvendor = arrData[6].equals("") ? null : arrData[6];
		String equipmodel = arrData[7].equals("") ? null : arrData[7];
		String fqdn = arrData[8].equals("") ? null : arrData[8];
		String pserverselflink = arrData[9].equals("") ? null : arrData[9];
		String ipv4oamaddress = arrData[10].equals("") ? null : arrData[10];
		String serialnumber = arrData[11].equals("") ? null : arrData[11];
		String ipaddressv4loopback0 = arrData[12].equals("") ? null : arrData[12];
		String ipaddressv6loopback0 = arrData[13].equals("") ? null : arrData[13];
		String ipaddressv4aim = arrData[14].equals("") ? null : arrData[14];
		String ipaddressv6aim = arrData[15].equals("") ? null : arrData[15];
		String ipaddressv6oam = arrData[16].equals("") ? null : arrData[16];
		String invstatus = arrData[17].equals("") ? null : arrData[17];
		String pserverid = arrData[18].equals("") ? null : arrData[18];
		String inmaint = arrData[19].equals("") ? null : arrData[19];
		String internettopology = arrData[20].equals("") ? null : arrData[20];
		String resourceversion = arrData[21].equals("") ? null : arrData[21];

		long numberofcpus = 0L;
		long diskingigabytes = 0L;
		long raminmegabytes = 0L;
		try {
			numberofcpus = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
			diskingigabytes = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
			raminmegabytes = arrData[4].equals("") ? 0L : Long.parseLong(arrData[4]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}

		String pservername2 = arrData[22].equals("") ? null : arrData[22];
		String purpose = arrData[23].equals("") ? null : arrData[23];
		String provstatus = arrData[24].equals("") ? null : arrData[24];
		String managementoption = arrData[25].equals("") ? null : arrData[25];
		String hostProfile = arrData[26].equals("") ? null : arrData[26];
		String role = "";
		String function = "";
		String opsnote = "";
		String formattedDate = "";

		Pserver pserver = null;
		NaradPserver pserverNarad = null;
		String invalidateSql = null;

		String querySql = " where p.hostname =:hostname";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			formattedDate = arrData[27].equals("") ? null : arrData[27];
			if (arrData.length < 28) {
				return FAILURE_UPDATE;
				}
			pserver = new Pserver(hostname, ptniiequipname, numberofcpus, diskingigabytes, raminmegabytes, equiptype,
					equipvendor, equipmodel, fqdn, pserverselflink, ipv4oamaddress, serialnumber, ipaddressv4loopback0,
					ipaddressv6loopback0, ipaddressv4aim, ipaddressv6aim, ipaddressv6oam, invstatus, pserverid, inmaint,
					internettopology, resourceversion, pservername2, purpose, provstatus, managementoption, hostProfile,
					formattedDate);
			invalidateSql = "delete from Pserver p " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			role = arrData[27].equals("") ? null : arrData[27];
			function = arrData[28].equals("") ? null : arrData[28];
			opsnote = arrData[29].equals("") ? null : arrData[29];
			formattedDate = arrData[30].equals("") ? null : arrData[30];
			if (arrData.length < 31) {
				return FAILURE_UPDATE;
				}
			pserverNarad = new NaradPserver(hostname, ptniiequipname, numberofcpus, diskingigabytes, raminmegabytes,
					equiptype, equipvendor, equipmodel, fqdn, pserverselflink, ipv4oamaddress, serialnumber,
					ipaddressv4loopback0, ipaddressv6loopback0, ipaddressv4aim, ipaddressv6aim, ipaddressv6oam,
					invstatus, pserverid, inmaint, internettopology, resourceversion, pservername2, purpose, provstatus,
					managementoption, hostProfile, role, function, opsnote, formattedDate);
			invalidateSql = "delete from NaradPserver p " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("hostname:" + hostname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(pserver);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(pserverNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			// TODO
			break;
		}
		return retVal;
	}

	public int processPhysicalLink(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPhysicalLink");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 13)
			return FAILURE_UPDATE;

		String linkname = arrData[0].equals("") ? emptyString : arrData[0];
		long speedvalue = 0L;
		try {
			speedvalue = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}

		String speedunits = arrData[2].equals("") ? null : arrData[2];
		String circuitid = arrData[3].equals("") ? null : arrData[3];
		String dualmode = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String managementOption = arrData[6].equals("") ? null : arrData[6];
		String serviceprovidername = arrData[7].equals("") ? null : arrData[7];
		String serviceproviderbwupvalue = arrData[8].equals("") ? null : arrData[8];
		String serviceproviderbwupunits = arrData[9].equals("") ? null : arrData[9];
		String serviceproviderbwdownvalue = arrData[10].equals("") ? null : arrData[10];
		String serviceproviderbwdownunits = arrData[11].equals("") ? null : arrData[11];
		String formattedDate = arrData[12].equals("") ? null : arrData[12];
		Physicallink physicallink = null;
		NaradPhysicallink physicallinkNarad = null;
		String invalidateSql = null;

		String querySql = " where p.linkname =:linkname";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			physicallink = new Physicallink(linkname, speedvalue, speedunits, circuitid, dualmode, resourceversion,
					managementOption, serviceprovidername, serviceproviderbwupvalue, serviceproviderbwupunits,
					serviceproviderbwdownvalue, serviceproviderbwdownunits, formattedDate);
			invalidateSql = "delete from Physicallink p " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			physicallinkNarad = new NaradPhysicallink(linkname, speedvalue, speedunits, circuitid, dualmode,
					resourceversion, managementOption, serviceprovidername, serviceproviderbwupvalue,
					serviceproviderbwupunits, serviceproviderbwdownvalue, serviceproviderbwdownunits, formattedDate);
			invalidateSql = "delete from NaradPhysicallink p " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("linkname:" + linkname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(physicallink);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(physicallinkNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processPinterface(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPinterface");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		String interfacename = arrData[0].equals("") ? emptyString : arrData[0];
		String interfacename2 = arrData[1].equals("") ? emptyString : arrData[1];
		long speedvalue = 0L;
		try {
			speedvalue = arrData[2].length() > 0 ? Long.parseLong(arrData[2]) : 0L;
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String speedunits = arrData[3].equals("") ? null : arrData[3];
		String portdescription = arrData[4].equals("") ? null : arrData[4];
		String equipmentidentifier = arrData[5].equals("") ? null : arrData[5];
		String interfacerole = arrData[6].equals("") ? null : arrData[6];
		String interfacetype = arrData[7].equals("") ? null : arrData[7];
		String provstatus = arrData[8].equals("") ? null : arrData[8];
		String resourceversion = arrData[9].equals("") ? null : arrData[9];
		String inmaint = arrData[10].equals("") ? null : arrData[10];
		String invstatus = arrData[11].equals("") ? null : arrData[11];
		String opsnote = "";
		String interfaceFunction = "";
		String macaddr = "";
		String pentityid = "";
		String pentitytype = "";
		String selflink = "";
		String formattedDate = "";

		Pinterface pinterface = null;
		NaradPinterface pinterfaceNarad = null;
		String invalidateSql = null;

		String querySql = " where p.interfacename =:interfacename and p.parententityid=:parententityid";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			pentityid = arrData[12].equals("") ? null : arrData[12];
			pentitytype = arrData[13].equals("") ? null : arrData[13];
			selflink = arrData[14].equals("") ? null : arrData[14];
			formattedDate = arrData[15].equals("") ? null : arrData[15];
			if (arrData.length < 16) {
				return FAILURE_UPDATE;
			}
			pinterface = new Pinterface(interfacename, pentityid, pentitytype, interfacename2, speedvalue, speedunits, portdescription,
					equipmentidentifier, interfacerole, interfacetype, provstatus, resourceversion, inmaint, invstatus,
					selflink, formattedDate);
			invalidateSql = "delete from Pinterface p " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			opsnote = arrData[12].equals("") ? null : arrData[12];
			interfaceFunction = arrData[13].equals("") ? null : arrData[13];
			macaddr = arrData[14].equals("") ? null : arrData[14];
			pentityid = arrData[15].equals("") ? null : arrData[15];
			pentitytype = arrData[16].equals("") ? null : arrData[16];
			selflink = arrData[17].equals("") ? null : arrData[17];
			formattedDate = arrData[18].equals("") ? null : arrData[18];
			if (arrData.length < 19) {
				return FAILURE_UPDATE;
			}
			pinterfaceNarad = new NaradPinterface(interfacename, pentityid, pentitytype, interfacename2, speedvalue, speedunits,
					portdescription, equipmentidentifier, interfacerole, interfacetype, provstatus, resourceversion,
					inmaint, invstatus, opsnote, interfaceFunction, macaddr, selflink, formattedDate);
			invalidateSql = "delete from NaradPinterface p " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("interfacename:" + interfacename, "parententityid:" + pentityid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(pinterface);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(pinterfaceNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processOamNetwork(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 9)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String networkuuid = arrData[0].equals("") ? emptyString : arrData[0];
		String networkname = arrData[1].equals("") ? null : arrData[1];

		long cvlantag = 0L;
		try {
			cvlantag = arrData[2].length() > 0 ? Long.parseLong(arrData[2]) : 0L;
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String ipv4oamgtwyaddr = arrData[3].equals("") ? null : arrData[3];
		long ipv4oamgtwyaddrprefixlen = 0L;
		try {
			ipv4oamgtwyaddrprefixlen = arrData[4].length() > 0 ? Long.parseLong(arrData[4]) : 0L;
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String cloudowner = arrData[6].equals("") ? null : arrData[6];
		String cloudregionid = arrData[7].equals("") ? null : arrData[7];
		String formattedDate = arrData[8].equals("") ? null : arrData[8];
		Oamnetwork oamnetwork = new Oamnetwork(networkuuid, cloudowner, cloudregionid, networkname, cvlantag,
				ipv4oamgtwyaddr, ipv4oamgtwyaddrprefixlen, resourceversion, formattedDate);

		String querySql = " where o.networkuuid =:networkuuid";
		String invalidateSql = "delete from Oamnetwork o " + querySql;
		List<String> paramList = Arrays.asList("networkuuid:" + networkuuid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(oamnetwork);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNetworkProfile(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String nmprofilename = null;
		String communitystring = null;
		String resourceversion = null;
		String formattedDate = null;

		if (arrData[0] == null || arrData[0].isEmpty()) {
			nmprofilename = "";
		} else {
			nmprofilename = arrData[0];
		}
		if (arrData[1] == null || arrData[1].isEmpty()) {
			communitystring = "";
		} else {
			communitystring = arrData[1];
		}
		if (arrData[2] == null || arrData[2].isEmpty()) {
			resourceversion = "";
		} else {
			resourceversion = arrData[2];
		}
		if (arrData[3] == null || arrData[3].isEmpty()) {
			formattedDate = "";
		} else {
			formattedDate = arrData[3];
		}

		Networkprofile np = new Networkprofile(nmprofilename, communitystring, resourceversion, formattedDate);

		String querySql = " where n.nmprofilename =:nmprofilename";
		String invalidateSql = "delete from Networkprofile n " + querySql;
		List<String> paramList = Arrays.asList("nmprofilename:" + nmprofilename);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(np);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processLogicalLink(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 20)
			return FAILURE_UPDATE;

		String linkname = arrData[0].equals("") ? emptyString : arrData[0];
		String inMaint = arrData[1].equals("") ? null : arrData[1];
		String linktype = arrData[2].equals("") ? null : arrData[2];
		long speedValue = 0;
		try {
			speedValue = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String speedunits = arrData[4].equals("") ? null : arrData[4];
		String ipversion = arrData[5].equals("") ? null : arrData[5];
		String routingprotocol = arrData[6].equals("") ? null : arrData[6];
		String resourceversion = arrData[7].equals("") ? null : arrData[7];
		String modelInvariantId = arrData[8].equals("") ? null : arrData[8];
		String modelVersionId = arrData[9].equals("") ? null : arrData[9];
		String widgetModelId = arrData[10].equals("") ? null : arrData[10];
		String widgetModelVersion = arrData[11].equals("") ? null : arrData[11];
		String operationalStatus = arrData[12].equals("") ? null : arrData[12];
		String provStatus = arrData[13].equals("") ? null : arrData[13];
		String linkRole = arrData[14].equals("") ? null : arrData[14];
		String linkName2 = arrData[15].equals("") ? null : arrData[15];
		String linkId = arrData[16].equals("") ? null : arrData[16];
		String circuitId = arrData[17].equals("") ? null : arrData[17];
		String purpose = arrData[18].equals("") ? null : arrData[18];
		String formattedDate = arrData[19].equals("") ? null : arrData[19];

		Logicallink logicalLink = null;
		NaradLogicallink logicalLinkNarad = null;
		String invalidateSql = null;

		String querySql = " where l.linkname =:linkname";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			logicalLink = new Logicallink(linkname, linktype, speedValue, speedunits, ipversion, routingprotocol,
					resourceversion, modelInvariantId, modelVersionId, widgetModelId, widgetModelVersion,
					operationalStatus, provStatus, linkRole, linkName2, linkId, circuitId, purpose, inMaint,
					formattedDate);
			invalidateSql = "delete from Logicallink l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			logicalLinkNarad = new NaradLogicallink(linkname, linktype, speedValue, speedunits, ipversion,
					routingprotocol, resourceversion, modelInvariantId, modelVersionId, widgetModelId,
					widgetModelVersion, operationalStatus, provStatus, linkRole, linkName2, linkId, circuitId, purpose,
					inMaint, formattedDate);
			invalidateSql = "delete from Logicallink l " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("linkname:" + linkname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(logicalLink);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(logicalLinkNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processLagLink(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 3)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String linkname = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String formattedDate = arrData[2].equals("") ? null : arrData[2];
		Laglink lagLink = new Laglink(linkname, resourceversion, formattedDate);

		String querySql = " where l.linkname =:linkname";
		String invalidateSql = "delete from Laglink l " + querySql;
		List<String> paramList = Arrays.asList("linkname:" + linkname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(lagLink);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processLagInterface(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String intname = arrData[0].equals("") ? emptyString : arrData[0];
		String intdesc = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		long speedValue = 0;
		try {
			speedValue = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}
		String speedUnits = arrData[4].equals("") ? null : arrData[4];
		String intid = arrData[5].equals("") ? null : arrData[5];
		String introle = arrData[6].equals("") ? null : arrData[6];
		String provstatus = arrData[7].equals("") ? null : arrData[7];
		String inMaint = arrData[8].equals("") ? null : arrData[8];
		String lacpSystemId = null;
		String opsNote = null;
		String interfaceFunction = null;
		String pEntityType = null;
		String pEntityId = null;
		String formattedDate = null;
		Laginterface lagInt = null;
		NaradLaginterface lagIntNarad = null;
		String invalidateSql = null;

		String querySql = " where l.interfacename =:interfacename and l.parententitytype=:parententitytype "
				+ "and l.parententityid=:parententityid";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			pEntityType = arrData[9].equals("") ? emptyString : arrData[9];
			pEntityId = arrData[10].equals("") ? emptyString : arrData[10];
			formattedDate = arrData[11].equals("") ? null : arrData[11];
			if (arrData.length < 12)
				return FAILURE_UPDATE;
			lagInt = new Laginterface(intname, pEntityType, pEntityId, intdesc, resourceversion, speedValue, speedUnits,
					intid, introle, provstatus, inMaint, formattedDate);
			invalidateSql = "delete from Laginterface l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			lacpSystemId = arrData[9].equals("") ? emptyString : arrData[9];
			opsNote = arrData[10].equals("") ? emptyString : arrData[10];
			interfaceFunction = arrData[11].equals("") ? emptyString : arrData[11];
			pEntityType = arrData[12].equals("") ? emptyString : arrData[12];
			pEntityId = arrData[13].equals("") ? emptyString : arrData[13];
			formattedDate = arrData[14].equals("") ? null : arrData[14];
			if (arrData.length < 15)
				return FAILURE_UPDATE;
			lagIntNarad = new NaradLaginterface(intname, pEntityType, pEntityId, intdesc, resourceversion, speedValue,
					speedUnits, intid, introle, provstatus, inMaint, lacpSystemId, opsNote, interfaceFunction, formattedDate);
			invalidateSql = "delete from NaradLaginterface l " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("interfacename:" + intname, "parententitytype:" + pEntityType,
				"parententityid:" + pEntityId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(lagInt);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(lagIntNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processL3InterfaceIpv6AddressList(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 17)
			return FAILURE_UPDATE;

		String ipv6address = arrData[0].equals("") ? emptyString : arrData[0];
		String isfloating = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String neutronNetworkId = arrData[6].equals("") ? null : arrData[6];
		String neutronNetworkSubId = arrData[7].equals("") ? null : arrData[7];
		String parententitytype = arrData[8].equals("") ? null : arrData[8];
		String parentinterfacename = arrData[9].equals("") ? null : arrData[9];
		String gparententitytype = arrData[10].equals("") ? null : arrData[10];
		String gparententityid = arrData[11].equals("") ? null : arrData[11];
		String cloudregiontenant = arrData[12].equals("") ? null : arrData[12];
		String pinterfacename = arrData[13].equals("") ? null : arrData[13];
		String laginterfacename = arrData[14].equals("") ? null : arrData[14];
		String vlaninterface = arrData[15].equals("") ? null : arrData[15];
		String formattedDate = arrData[16].equals("") ? null : arrData[16];

		long l3interfaceipv6prefixlength = 0L;
		long vlanidinner = 0L;
		long vlanidouter = 0L;
		try {
			l3interfaceipv6prefixlength = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
			vlanidinner = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
			vlanidouter = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}

		L3InterfaceIpv6AddressList ipv6 = null;
		NaradL3InterfaceIpv6AddressList ipv6Narad = null;

		String querySql = " where l.ipv6address =:ipv6address and l.grandparententityid=:grandparententityid";
		String invalidateSql = null;
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			ipv6 = new L3InterfaceIpv6AddressList(ipv6address, gparententityid, parentinterfacename, cloudregiontenant,
					pinterfacename, laginterfacename, vlaninterface, l3interfaceipv6prefixlength, vlanidinner,
					vlanidouter, isfloating, resourceversion, neutronNetworkId, neutronNetworkSubId, parententitytype,
					gparententitytype, formattedDate);
			invalidateSql = "delete from L3InterfaceIpv6AddressList l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			ipv6Narad = new NaradL3InterfaceIpv6AddressList(ipv6address, gparententityid, parentinterfacename,
					cloudregiontenant, pinterfacename, laginterfacename, vlaninterface, l3interfaceipv6prefixlength,
					vlanidinner, vlanidouter, isfloating, resourceversion, neutronNetworkId, neutronNetworkSubId,
					parententitytype, gparententitytype, formattedDate);
			invalidateSql = "delete from NaradL3InterfaceIpv6AddressList l " + querySql;
		}

		List<String> paramList = Arrays.asList("ipv6address:" + ipv6address, "grandparententityid:" + gparententityid);

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(ipv6);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(ipv6Narad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processL3InterfaceIpv4AddressList(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processL3InterfaceIpv4AddressList");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 17)
			return FAILURE_UPDATE;

		String l3interfaceipv4address = arrData[0].equals("") ? emptyString : arrData[0];

		String isfloating = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String neutronNetworkId = arrData[6].equals("") ? null : arrData[6];
		String neutronNetworkSubId = arrData[7].equals("") ? null : arrData[7];
		String parententitytype = arrData[8].equals("") ? null : arrData[8];
		String parentinterfacename = arrData[9].equals("") ? null : arrData[9];

		String gparententitytype = arrData[10].equals("") ? null : arrData[10];
		String gparententityid = arrData[11].equals("") ? null : arrData[11];
		String cloudregiontenant = arrData[12].equals("") ? null : arrData[12];
		String pinterfacename = arrData[13].equals("") ? null : arrData[13];
		String laginterfacename = arrData[14].equals("") ? null : arrData[14];
		String vlaninterface = arrData[15].equals("") ? null : arrData[15];
		String formattedDate = arrData[16].equals("") ? null : arrData[16];

		long l3interfaceipv4prefixlength = 0L;
		long vlanidinner = 0L;
		long vlanidouter = 0L;
		try {
			l3interfaceipv4prefixlength = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
			vlanidinner = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
			vlanidouter = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}

		L3InterfaceIpv4AddressList ipv4 = null;
		NaradL3InterfaceIpv4AddressList ipv4Narad = null;

		String querySql = " where l.ipv4address =:ipv4address and l.grandparententityid=:grandparententityid";
		String invalidateSql = null;

		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			ipv4 = new L3InterfaceIpv4AddressList(l3interfaceipv4address, gparententityid, parentinterfacename,
					cloudregiontenant, pinterfacename, laginterfacename, vlaninterface, l3interfaceipv4prefixlength,
					vlanidinner, vlanidouter, isfloating, resourceversion, neutronNetworkId, neutronNetworkSubId,
					parententitytype, gparententitytype, formattedDate);
			invalidateSql = "delete from L3InterfaceIpv4AddressList l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			ipv4Narad = new NaradL3InterfaceIpv4AddressList(l3interfaceipv4address, gparententityid,
					parentinterfacename, cloudregiontenant, pinterfacename, laginterfacename, vlaninterface,
					l3interfaceipv4prefixlength, vlanidinner, vlanidouter, isfloating, resourceversion,
					neutronNetworkId, neutronNetworkSubId, parententitytype, gparententitytype, formattedDate);
			invalidateSql = "delete from NaradL3InterfaceIpv4AddressList l " + querySql;
		}

		List<String> paramList = Arrays.asList("ipv4address:" + l3interfaceipv4address,
				"grandparententityid:" + gparententityid);

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(ipv4);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(ipv4Narad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processPortGroup(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 13)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String interfaceid = arrData[0].equals("") ? emptyString : arrData[0];
		String neutronnetworkid = arrData[1].equals("") ? null : arrData[1];
		String neutronnetworkname = arrData[2].equals("") ? null : arrData[2];
		String interfacerole = arrData[3].equals("") ? null : arrData[3];
		String resourceversion = arrData[4].equals("") ? null : arrData[4];
		String portgroupid = arrData[5].equals("") ? null : arrData[5];
		String portgroupname = arrData[6].equals("") ? null : arrData[6];
		String switchname = arrData[7].equals("") ? null : arrData[7];
		String orchestrationstatus = arrData[8].equals("") ? null : arrData[8];
		String heatstackid = arrData[9].equals("") ? null : arrData[9];
		String msocatalogkey = arrData[10].equals("") ? null : arrData[10];
		String vnfid = arrData[11].equals("") ? null : arrData[11];
		String formattedDate = arrData[12].equals("") ? null : arrData[12];
		Portgroup portGroup = new Portgroup(interfaceid, vnfid, neutronnetworkid, neutronnetworkname, interfacerole,
				resourceversion, portgroupid, portgroupname, switchname, orchestrationstatus, heatstackid,
				msocatalogkey, formattedDate);

		String querySql = " where p.interfaceid =:interfaceid and p.vnfid=:vnfid";
		String invalidateSql = "delete from Portgroup p " + querySql;
		List<String> paramList = Arrays.asList("interfaceid:" + interfaceid, "vnfid:" + vnfid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(portGroup);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processSubnet(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processSubnet");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String subnetId = null;
		String networkId = null;
		Subnet subnet = null;
		NaradSubnet subnetNarad = null;
		String invalidateSql = null;
		String querySql = " where s.subnetid =:subnetid and s.networkid=:networkid";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			if (arrData.length < 17)
				return FAILURE_UPDATE;

			subnetId = arrData[0].equals("") ? emptyString : arrData[0];
			String subnetName = arrData[1].equals("") ? null : arrData[1];
			String neutronSubnetId = arrData[2].equals("") ? null : arrData[2];
			String gatewayAddress = arrData[3].equals("") ? null : arrData[3];
			String networkStartAddress = arrData[4].equals("") ? null : arrData[4];
			String cidrMask = arrData[5].equals("") ? null : arrData[5];
			String ipVersion = arrData[6].equals("") ? null : arrData[6];
			String orchestrationStatus = arrData[7].equals("") ? null : arrData[7];
			String dhcpEnabled = arrData[8].equals("") ? null : arrData[8];
			String dhcpStart = arrData[9].equals("") ? null : arrData[9];
			String dhcpEnd = arrData[10].equals("") ? null : arrData[10];
			String resourceVersion = arrData[11].equals("") ? null : arrData[11];
			networkId = arrData[12].equals("") ? null : arrData[12];
			String subnetRole = arrData[13].equals("") ? null : arrData[13];
			String ipAssignmentDirection = arrData[14].equals("") ? null : arrData[14];
			String subnetSequence = arrData[15].equals("") ? null : arrData[15];
			String formattedDate = arrData[16].equals("") ? null : arrData[16];

			subnet = new Subnet(subnetId, networkId, subnetName, neutronSubnetId, gatewayAddress, networkStartAddress,
					cidrMask, ipVersion, orchestrationStatus, dhcpEnabled, dhcpStart, dhcpEnd, resourceVersion,
					subnetRole, ipAssignmentDirection, subnetSequence, formattedDate);
			invalidateSql = "delete from Subnet s " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			if (arrData.length < 20)
				return FAILURE_UPDATE;

			subnetId = arrData[0].equals("") ? emptyString : arrData[0];
			String subnetName = arrData[1].equals("") ? null : arrData[1];
			String neutronSubnetId = arrData[2].equals("") ? null : arrData[2];
			String gatewayAddress = arrData[3].equals("") ? null : arrData[3];
			String networkStartAddress = arrData[4].equals("") ? null : arrData[4];
			String cidrMask = arrData[5].equals("") ? null : arrData[5];
			String ipVersion = arrData[6].equals("") ? null : arrData[6];
			String orchestrationStatus = arrData[7].equals("") ? null : arrData[7];
			String description = arrData[8].equals("") ? null : arrData[8];
			String dhcpEnabled = arrData[9].equals("") ? null : arrData[9];
			String dhcpStart = arrData[10].equals("") ? null : arrData[10];
			String dhcpEnd = arrData[11].equals("") ? null : arrData[11];
			String resourceVersion = arrData[12].equals("") ? null : arrData[12];
			String parententitytype = arrData[13].equals("") ? null : arrData[13];
			String parententityid = arrData[14].equals("") ? null : arrData[14];
			String subnetRole = arrData[15].equals("") ? null : arrData[15];
			String ipAssignmentDirection = arrData[16].equals("") ? null : arrData[16];
			String subnetSequence = arrData[17].equals("") ? null : arrData[17];
			String subnetModel = arrData[18].equals("") ? null : arrData[18];
			String opsNote = arrData[19].equals("") ? null : arrData[19];
			String formattedDate = arrData[20].equals("") ? null : arrData[20];

			subnetNarad = new NaradSubnet(subnetId, parententitytype, parententityid, subnetName, neutronSubnetId,
					gatewayAddress, networkStartAddress, cidrMask, ipVersion, orchestrationStatus, description,
					dhcpEnabled, dhcpStart, dhcpEnd, resourceVersion, subnetRole, ipAssignmentDirection, subnetSequence,
					subnetModel, opsNote, formattedDate);
			invalidateSql = "delete from NaradSubnet s " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("subnetid:" + subnetId, "networkid:" + networkId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(subnet);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(subnetNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processL3Network(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 26)
			return FAILURE_UPDATE;

		String networkId = arrData[0].equals("") ? emptyString : arrData[0];
		String networkName = arrData[1].equals("") ? null : arrData[1];
		String networkType = arrData[2].equals("") ? null : arrData[2];
		String networkRole = arrData[3].equals("") ? null : arrData[3];
		String networkTechnology = arrData[4].equals("") ? null : arrData[4];
		String neutronNetworkId = arrData[5].equals("") ? null : arrData[5];
		String isBoundToVpn = arrData[6].equals("") ? null : arrData[6];
		String serviceId = arrData[7].equals("") ? null : arrData[7];
		long networkRoleInstance = 0L;
		try {
			networkRoleInstance = arrData[8].equals("") ? 0L : Long.parseLong(arrData[8]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String resourceVersion = arrData[9].equals("") ? null : arrData[9];
		String orchestrationStatus = arrData[10].equals("") ? null : arrData[10];
		String heatStackId = arrData[11].equals("") ? null : arrData[11];
		String msoCatalogKey = arrData[12].equals("") ? null : arrData[12];
		String contrailNetworkFqdn = arrData[13].equals("") ? null : arrData[13];
		String modelInvariantId = arrData[14].equals("") ? null : arrData[14];
		String modelVersionId = arrData[15].equals("") ? null : arrData[15];
		String modelCustomizationId = arrData[16].equals("") ? null : arrData[16];
		String widgetModelId = arrData[17].equals("") ? null : arrData[17];
		String widgetModelVersion = arrData[18].equals("") ? null : arrData[18];
		String physicalnetworkname = arrData[19].equals("") ? null : arrData[19];
		String isprovidernetwork = arrData[20].equals("") ? null : arrData[20];
		String issharednetwork = arrData[21].equals("") ? null : arrData[21];
		String isexternalnetwork = arrData[22].equals("") ? null : arrData[22];
		String operationalStatus = arrData[23].equals("") ? null : arrData[23];
		String selflink = arrData[24].equals("") ? null : arrData[24];
		String formattedDate = arrData[25].equals("") ? null : arrData[25];

		L3network l3network = null;
		NaradL3network l3networkNarad = null;
		String invalidateSql = null;

		String querySql = " where l.networkid =:networkid";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			l3network = new L3network(networkId, networkName, networkType, networkRole, networkTechnology,
					neutronNetworkId, isBoundToVpn, serviceId, networkRoleInstance, resourceVersion,
					orchestrationStatus, heatStackId, msoCatalogKey, contrailNetworkFqdn, modelInvariantId,
					modelVersionId, modelCustomizationId, widgetModelId, widgetModelVersion, physicalnetworkname,
					isprovidernetwork, issharednetwork, isexternalnetwork, operationalStatus, selflink, formattedDate);
			invalidateSql = "delete from L3network l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			l3networkNarad = new NaradL3network(networkId, networkName, networkType, networkRole, networkTechnology,
					neutronNetworkId, isBoundToVpn, serviceId, networkRoleInstance, resourceVersion,
					orchestrationStatus, heatStackId, msoCatalogKey, contrailNetworkFqdn, modelInvariantId,
					modelVersionId, modelCustomizationId, widgetModelId, widgetModelVersion, physicalnetworkname,
					isprovidernetwork, issharednetwork, isexternalnetwork, operationalStatus, selflink, formattedDate);
			invalidateSql = "delete from NaradL3network l " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("networkid:" + networkId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(l3network);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(l3networkNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processLinterafce(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processLinterafce");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String interfacename = arrData[0].equals("") ? emptyString : arrData[0];
		String interfacerole = arrData[1].equals("") ? null : arrData[1];
		String v6wanlinkip = arrData[2].equals("") ? null : arrData[2];
		String selflink = arrData[3].equals("") ? null : arrData[3];
		String interfaceid = arrData[4].equals("") ? null : arrData[4];
		String macaddr = arrData[5].equals("") ? null : arrData[5];
		String networkname = arrData[6].equals("") ? null : arrData[6];
		String resourceversion = arrData[7].equals("") ? null : arrData[7];
		String managementOption = arrData[8].equals("") ? null : arrData[8];
		String interfacedescription = arrData[9].equals("") ? null : arrData[9];
		String interfacetype = null;
		String isportmirrored = null;
		String inmaint = null;
		String provstatus = null;
		String isipunnumbered = null;
		String parententitytype = null;
		String parententityid = null;
		String allowedaddresspairs = null;
		String adminstatus = null;
		String opsnote = null;
		String interfacefunction = null;
		String cloudregiontenant = null;
		String pinterfacename = null;
		String laginterfacename = null;
		String formattedDate = null;

		Linterface linterface = null;
		NaradLinterface linterfaceNarad = null;
		String querySql = " where l.interfacename =:interfacename and l.parententityid=:parententityid";
		String invalidateSql = null;

		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			isportmirrored = arrData[10].equals("") ? null : arrData[10];
			inmaint = arrData[11].equals("") ? null : arrData[11];
			provstatus = arrData[12].equals("") ? null : arrData[12];
			isipunnumbered = arrData[13].equals("") ? null : arrData[13];
			parententitytype = arrData[14].equals("") ? null : arrData[14];
			parententityid = arrData[15].equals("") ? null : arrData[15];
			allowedaddresspairs = arrData[16].equals("") ? null : arrData[16];
			cloudregiontenant = arrData[17].equals("") ? null : arrData[17];
			pinterfacename = arrData[18].equals("") ? null : arrData[18];
			laginterfacename = arrData[19].equals("") ? null : arrData[19];
			formattedDate = arrData[20].equals("") ? null : arrData[20];
			if (arrData.length < 21)
				return FAILURE_UPDATE;
			linterface = new Linterface(interfacename, parententityid, cloudregiontenant, pinterfacename,
					laginterfacename, interfacerole, v6wanlinkip, selflink, interfaceid, macaddr, networkname,
					resourceversion, managementOption, interfacedescription, isportmirrored, provstatus, inmaint,
					isipunnumbered, parententitytype, allowedaddresspairs, formattedDate);
			invalidateSql = " delete Linterface l " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			interfacetype = arrData[10].equals("") ? null : arrData[10];
			isportmirrored = arrData[11].equals("") ? null : arrData[11];
			inmaint = arrData[12].equals("") ? null : arrData[12];
			provstatus = arrData[13].equals("") ? null : arrData[13];
			isipunnumbered = arrData[14].equals("") ? null : arrData[14];
			parententitytype = arrData[15].equals("") ? null : arrData[15];
			parententityid = arrData[16].equals("") ? null : arrData[16];
			allowedaddresspairs = arrData[17].equals("") ? null : arrData[17];
			adminstatus = arrData[18].equals("") ? null : arrData[18];
			opsnote = arrData[19].equals("") ? null : arrData[19];
			interfacefunction = arrData[20].equals("") ? null : arrData[20];
			cloudregiontenant = arrData[21].equals("") ? null : arrData[21];
			pinterfacename = arrData[22].equals("") ? null : arrData[22];
			laginterfacename = arrData[23].equals("") ? null : arrData[23];
			formattedDate = arrData[24].equals("") ? null : arrData[24];
			if (arrData.length < 25)
				return FAILURE_UPDATE;
			linterfaceNarad = new NaradLinterface(interfacename, parententityid, cloudregiontenant, pinterfacename,
					laginterfacename, interfacerole, v6wanlinkip, selflink, interfaceid, macaddr, networkname,
					resourceversion, managementOption, interfacedescription,  interfacetype, isportmirrored, provstatus, inmaint,
					isipunnumbered, parententitytype, allowedaddresspairs, adminstatus, opsnote, interfacefunction, formattedDate);
			invalidateSql = " delete NaradLinterface l " + querySql;
		}
		List<String> paramList = Arrays.asList("interfacename:" + interfacename, "parententityid:" + parententityid);

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(linterface);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(linterfaceNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processImage(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 11)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String imageid = arrData[0].equals("") ? emptyString : arrData[0];
		String imagename = arrData[1].equals("") ? null : arrData[1];
		String imagearchitecture = arrData[2].equals("") ? null : arrData[2];
		String imageosdistro = arrData[3].equals("") ? null : arrData[3];
		String imageosversion = arrData[4].equals("") ? null : arrData[4];
		String application = arrData[5].equals("") ? null : arrData[5];
		String applicationvendor = arrData[6].equals("") ? null : arrData[6];
		String applicationversion = arrData[7].equals("") ? null : arrData[7];
		String imageselflink = arrData[8].equals("") ? null : arrData[8];
		String resourceversion = arrData[9].equals("") ? null : arrData[9];
		String cloudowner = arrData[10].equals("") ? null : arrData[10];
		String cloudregionid = arrData[11].equals("") ? null : arrData[11];
		String formattedDate = arrData[12].equals("") ? null : arrData[12];

		Image image = new Image(imageid, cloudowner, cloudregionid, imagename, imagearchitecture, imageosdistro,
				imageosversion, application, applicationvendor, applicationversion, imageselflink, resourceversion,
				formattedDate);

		String querySql = " where i.imageid =:imageid";
		String invalidateSql = "delete from Image i " + querySql;
		List<String> paramList = Arrays.asList("imageid:" + imageid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(image);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processGenericVnf(String data, String action, String entityType, String eventType) {
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 45)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfid = arrData[0].equals("") ? emptyString : arrData[0];
		String vnfname = arrData[1].equals("") ? null : arrData[1];
		String vnfname2 = arrData[2].equals("") ? null : arrData[2];
		String vnftype = arrData[3].equals("") ? null : arrData[3];
		String serviceid = arrData[4].equals("") ? null : arrData[4];
		String regionalresourcezone = arrData[5].equals("") ? null : arrData[5];
		String provstatus = arrData[6].equals("") ? null : arrData[6];
		String operationalstatus = arrData[7].equals("") ? null : arrData[7];
		String equipmentrole = arrData[8].equals("") ? null : arrData[8];
		String orchestrationstatus = arrData[9].equals("") ? null : arrData[9];
		String heatstackid = arrData[10].equals("") ? null : arrData[10];
		String msocatalogkey = arrData[11].equals("") ? null : arrData[11];
		String managementoption = arrData[12].equals("") ? null : arrData[12];
		String ipvyoamaddress = arrData[13].equals("") ? null : arrData[13];
		String ipv4loopback0address = arrData[14].equals("") ? null : arrData[14];
		String nmlanv6address = arrData[15].equals("") ? null : arrData[15];
		String managementv6address = arrData[16].equals("") ? null : arrData[16];
		String vcpuunits = arrData[18].equals("") ? null : arrData[18];
		String vmemoryunits = arrData[20].equals("") ? null : arrData[20];

		long vcpu = 0L;
		long vmemory = 0L;
		long vdisk = 0L;
		try {
			vcpu = arrData[17].equals("") ? 0L : Long.parseLong(arrData[17]);
			vmemory = arrData[19].equals("") ? 0L : Long.parseLong(arrData[19]);
			vdisk = arrData[21].equals("") ? 0L : Long.parseLong(arrData[21]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}
		String vdiskunits = arrData[22].equals("") ? null : arrData[22];
		String inmaint = arrData[23].equals("") ? null : arrData[23];
		String isclosedloopdisabled = arrData[24].equals("") ? null : arrData[24];
		String resourceversion = arrData[25].equals("") ? null : arrData[25];
		String summarystatus = arrData[26].equals("") ? null : arrData[26];
		String encryptedaccessflag = arrData[27].equals("") ? null : arrData[27];
		String modelInvariantId = arrData[28].equals("") ? null : arrData[28];
		String modelVersionId = arrData[29].equals("") ? null : arrData[29];
		String modelCustomizationId = arrData[30].equals("") ? null : arrData[30];
		String widgetModelId = arrData[31].equals("") ? null : arrData[31];
		String widgetModelVersion = arrData[32].equals("") ? null : arrData[32];
		String asNumber = arrData[33].equals("") ? null : arrData[33];
		String regionalResourceSubzone = arrData[34].equals("") ? null : arrData[34];
		String nfType = arrData[35].equals("") ? null : arrData[35];
		String nfRole = arrData[36].equals("") ? null : arrData[36];
		String nfFunction = arrData[37].equals("") ? null : arrData[37];
		String nfNamingCode = arrData[38].equals("") ? null : arrData[38];
		String selflink = arrData[39].equals("") ? null : arrData[39];
		String ipv4oamgatewayaddress = arrData[40].equals("") ? null : arrData[40];
		String ipv4oamgatewayaddressprefixlength = arrData[41].equals("") ? null : arrData[41];
		long vlanidouter = 0L;

		try {
			vlanidouter = arrData[42].equals("") ? 0L : Long.parseLong(arrData[42]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String nmprofilename = arrData[43].equals("") ? null : arrData[43];
		String formattedDate = arrData[44].equals("") ? null : arrData[44];
		GenericVnf gVnf = new GenericVnf(vnfid, vnfname, vnfname2, vnftype, serviceid, regionalresourcezone, provstatus,
				operationalstatus, equipmentrole, orchestrationstatus, heatstackid, msocatalogkey, managementoption,
				ipvyoamaddress, ipv4loopback0address, nmlanv6address, managementv6address, vcpu, vcpuunits, vmemory,
				vmemoryunits, vdisk, vdiskunits, inmaint, isclosedloopdisabled, resourceversion, summarystatus,
				encryptedaccessflag, modelInvariantId, modelVersionId, modelCustomizationId, widgetModelId,
				widgetModelVersion, asNumber, regionalResourceSubzone, nfType, nfRole, nfFunction, nfNamingCode,
				selflink, ipv4oamgatewayaddress, ipv4oamgatewayaddressprefixlength, vlanidouter, nmprofilename,
				formattedDate);

		String querySql = " where g.vnfid =:vnfid";
		String invalidateSql = "delete from GenericVnf g " + querySql;
		List<String> paramList = Arrays.asList("vnfid:" + vnfid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(gVnf);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processFlavor(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processFlavor");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 12)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String flavorid = arrData[0].equals("") ? emptyString : arrData[0];
		String flavorname = arrData[1].equals("") ? null : arrData[1];
		long flavorvcpus = 0L;
		long flavorram = 0L;
		long flavordisk = 0L;
		long flavorephemeral = 0L;
		try {
			flavorvcpus = arrData[2].length() > 0 ? Long.parseLong(arrData[2]) : 0L;
			flavorram = arrData[3].length() > 0 ? Long.parseLong(arrData[3]) : 0L;
			flavordisk = arrData[4].length() > 0 ? Long.parseLong(arrData[4]) : 0L;
			flavorephemeral = arrData[5].length() > 0 ? Long.parseLong(arrData[5]) : 0L;
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
		}
		String flavorswap = arrData[6].equals("") ? null : arrData[6];
		String flavorispublic = arrData[7].equals("") ? null : arrData[7];
		String flavorselflink = arrData[8].equals("") ? null : arrData[8];
		String flavordisabled = arrData[9].equals("") ? null : arrData[9];
		String resourceversion = arrData[10].equals("") ? null : arrData[10];
		String cloudowner = arrData[11].equals("") ? null : arrData[11];
		String cloudregionid = arrData[12].equals("") ? null : arrData[12];
		String formattedDate = arrData[13].equals("") ? null : arrData[13];
		Flavor flavor = new Flavor(flavorid, cloudowner, cloudregionid, flavorname, flavorvcpus, flavorram, flavordisk,
				flavorephemeral, flavorswap, flavorispublic, flavorselflink, flavordisabled, resourceversion,
				formattedDate);

		String querySql = " where f.flavorid =:flavorid";
		String invalidateSql = "delete from Flavor f " + querySql;
		List<String> paramList = Arrays.asList("flavorid:" + flavorid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(flavor);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processDvsSwitch(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processDvsSwitch");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String switchname = arrData[0].equals("") ? emptyString : arrData[0];
		String vcenterurl = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];
		Dvsswitch dvsSwitch = new Dvsswitch(switchname, vcenterurl, resourceversion, formattedDate);

		String querySql = " where d.switchname =:switchname";
		String invalidateSql = "delete from Dvsswitch d " + querySql;
		List<String> paramList = Arrays.asList("switchname:" + switchname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(dvsSwitch);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processCustomer(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processCustomer");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String globalcustomerid = arrData[0].equals("") ? emptyString : arrData[0];
		String subscribername = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];

		Customer customer = new Customer(globalcustomerid, subscribername, resourceversion, formattedDate);

		String querySql = " where c.globalcustomerid =:globalcustomerid";
		String invalidateSql = "delete from Customer c " + querySql;
		List<String> paramList = Arrays.asList("globalcustomerid:" + globalcustomerid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(customer);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processComplex(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processComplex");
		boolean result = true;
		int retVal = 0;

		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		if (arrData.length < 17)
			return FAILURE_UPDATE;

		Complex complex = null;
		NaradComplex complexNarad = null;

		String physicallocationid = arrData[0].equals("") ? emptyString : arrData[0];
		String datacentercode = arrData[1].equals("") ? null : arrData[1];
		String complexname = arrData[2].equals("") ? null : arrData[2];
		String identityurl = arrData[3].equals("") ? null : arrData[3];
		String resourceversion = arrData[4].equals("") ? null : arrData[4];
		String physicallocationtype = arrData[5].equals("") ? null : arrData[5];
		String street1 = arrData[6].equals("") ? null : arrData[6];
		String street2 = arrData[7].equals("") ? null : arrData[7];
		String city = arrData[8].equals("") ? null : arrData[8];
		String state = arrData[9].equals("") ? null : arrData[9];
		String postalcode = arrData[10].equals("") ? null : arrData[10];
		String country = arrData[11].equals("") ? null : arrData[11];
		String region = arrData[12].equals("") ? null : arrData[12];
		String latitude = arrData[13].equals("") ? null : arrData[13];
		String longitude = arrData[14].equals("") ? null : arrData[14];
		String elevation = arrData[15].equals("") ? null : arrData[15];
		String lata = arrData[16].equals("") ? null : arrData[16];
		String formattedDate = arrData[17].equals("") ? null : arrData[17];

		String querySql = " where c.physicallocationid =:physicallocationid";
		String invalidateSql = null;
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			complex = new Complex(physicallocationid, datacentercode, complexname, identityurl, resourceversion,
					physicallocationtype, street1, street2, city, state, postalcode, country, region, latitude,
					longitude, elevation, lata, formattedDate);
			invalidateSql = "delete from Complex c " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			complexNarad = new NaradComplex(physicallocationid, datacentercode, complexname, identityurl,
					resourceversion, physicallocationtype, street1, street2, city, state, postalcode, country, region,
					latitude, longitude, elevation, lata, formattedDate);
			invalidateSql = "delete from NaradComplex c " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("physicallocationid:" + physicallocationid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(complex);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(complexNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processCloudRegion(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processCloudRegion");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");
		Cloudregion cloudRegion = null;
		NaradCloudregion cloudRegionNarad = null;

		String cloudowner = arrData[0].equals("") ? emptyString : arrData[0];
		String cloudregionid = arrData[1].equals("") ? null : arrData[1];
		String cloudtype = arrData[2].equals("") ? null : arrData[2];
		String ownerdefinedtype = arrData[3].equals("") ? null : arrData[3];
		String cloudregionversion = arrData[4].equals("") ? null : arrData[4];
		String identityurl = arrData[5].equals("") ? null : arrData[5];
		String cloudzone = arrData[6].equals("") ? null : arrData[6];
		String complexname = arrData[7].equals("") ? null : arrData[7];
		String sriovautomation = arrData[8].equals("") ? null : arrData[8];
		String resourceversion = arrData[9].equals("") ? null : arrData[9];
		String upgradecycle = arrData[10].equals("") ? null : arrData[10];
		String orchestrationdisabled = arrData[11].equals("") ? null : arrData[11];
		String inmaint = arrData[12].equals("") ? null : arrData[12];
		String formattedDate = null;
		String cloudrole;
	    String cloudfunction;
	    String status;

		String invalidateSql = null;
		String querySql = " where c.cloudowner =:cloudowner and c.cloudregionid =:cloudregionid";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			formattedDate = arrData[13].equals("") ? null : arrData[13];
			if (arrData.length < 14)
				return FAILURE_UPDATE;
			cloudRegion = new Cloudregion(cloudowner, cloudregionid, cloudtype, ownerdefinedtype, cloudregionversion,
					identityurl, cloudzone, complexname, sriovautomation, resourceversion, upgradecycle,
					orchestrationdisabled, inmaint, formattedDate);
			invalidateSql = "delete from Cloudregion c " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			cloudrole = arrData[13].equals("") ? null : arrData[13];
			cloudfunction = arrData[14].equals("") ? null : arrData[14];
			status = arrData[15].equals("") ? null : arrData[15];
			formattedDate = arrData[16].equals("") ? null : arrData[16];
			if (arrData.length < 17)
				return FAILURE_UPDATE;
			cloudRegionNarad = new NaradCloudregion(cloudowner, cloudregionid, cloudtype, ownerdefinedtype,
					cloudregionversion, identityurl, cloudzone, complexname, sriovautomation, resourceversion,
					upgradecycle, orchestrationdisabled, inmaint, cloudrole, cloudfunction, status, formattedDate);
			invalidateSql = "delete from NaradCloudregion c " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("cloudowner:" + cloudowner, "cloudregionid:" + cloudregionid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(cloudRegion);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(cloudRegionNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processNewVce(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processNewVce");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 14)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfId2 = arrData[0].equals("") ? emptyString : arrData[0];
		String vnfName = arrData[1].equals("") ? null : arrData[1];
		String vnfName2 = arrData[2].equals("") ? null : arrData[2];
		String vnfType = arrData[3].equals("") ? null : arrData[3];
		String provStatus = arrData[4].equals("") ? null : arrData[4];
		String operationalStatus = arrData[5].equals("") ? null : arrData[5];
		String equipmentRole = arrData[6].equals("") ? null : arrData[6];
		String orchestrationStatus = arrData[7].equals("") ? null : arrData[7];
		String heatStackId = arrData[8].equals("") ? null : arrData[8];
		String msoCatalogKey = arrData[9].equals("") ? null : arrData[9];
		String ipv4OamAddress = arrData[10].equals("") ? null : arrData[10];
		String ipv4LoopbackAddress = arrData[11].equals("") ? null : arrData[11];
		String resourceVersion = arrData[12].equals("") ? null : arrData[12];
		String formattedDate = arrData[13].equals("") ? null : arrData[13];

		Newvce newvce = new Newvce(vnfId2, vnfName, vnfName2, vnfType, provStatus, operationalStatus, equipmentRole,
				orchestrationStatus, heatStackId, msoCatalogKey, ipv4OamAddress, ipv4LoopbackAddress, resourceVersion,
				formattedDate);

		String querySql = " where n.vnfid2 =:vnfid2";
		String invalidateSql = "delete from Newvce n " + querySql;
		List<String> paramList = Arrays.asList("vnfid2:" + vnfId2);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(newvce);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processServiceCapability(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processServiceCapability");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String serviceType = arrData[0].equals("") ? emptyString : arrData[0];
		String vnfType = arrData[1].equals("") ? null : arrData[1];
		String resourceVersion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];

		ServiceCapability serviceCapability = new ServiceCapability(serviceType, vnfType, resourceVersion,
				formattedDate);

		String querySql = " where s.servicetype =:servicetype";
		String invalidateSql = "delete from ServiceCapability s " + querySql;
		List<String> paramList = Arrays.asList("servicetype:" + serviceType);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(serviceCapability);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processVfModule(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVfModule");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 15)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vfModuleId = arrData[0].equals("") ? emptyString : arrData[0];
		String vfModuleName = arrData[1].equals("") ? null : arrData[1];
		String modelInvariantId = arrData[2].equals("") ? null : arrData[2];
		String modelVersionId = arrData[3].equals("") ? null : arrData[3];
		String modelCustomizationId = arrData[4].equals("") ? null : arrData[4];
		String widgetModelId = arrData[5].equals("") ? null : arrData[5];
		String widgetModelVersion = arrData[6].equals("") ? null : arrData[6];
		String heatStackId = arrData[7].equals("") ? null : arrData[7];
		String isBaseVfModule = arrData[8].equals("") ? null : arrData[8];
		String orchestrationStatus = arrData[9].equals("") ? null : arrData[9];
		String resourceVersion = arrData[10].equals("") ? null : arrData[10];
		String contrailfqdn = arrData[11].equals("") ? null : arrData[11];
		String selflink = arrData[13].equals("") ? null : arrData[13];
		String formattedDate = arrData[14].equals("") ? null : arrData[14];

		long moduleIndex = 0L;
		try {
			moduleIndex = arrData[12].equals("") ? 0L : Long.parseLong(arrData[12]);
		} catch (NumberFormatException e) {
			ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_IGNORE_NUMBER_FORMAT_EXCEPTION_DEBUG,
					e.getMessage());
			System.out.println("Ignore");
		}

		Vfmodule vfmodule = new Vfmodule(vfModuleId, vfModuleName, modelInvariantId, modelVersionId,
				modelCustomizationId, widgetModelId, widgetModelVersion, heatStackId, isBaseVfModule,
				orchestrationStatus, resourceVersion, contrailfqdn, moduleIndex, selflink, formattedDate);

		String querySql = " where v.vfmoduleid =:vfmoduleid";
		String invalidateSql = "delete from Vfmodule v " + querySql;
		List<String> paramList = Arrays.asList("vfmoduleid:" + vfModuleId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vfmodule);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processVNFC(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVNFC");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 14)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vnfcName = arrData[0].equals("") ? emptyString : arrData[0];
		String nfcNamingCode = arrData[1].equals("") ? null : arrData[1];
		String nfcFunction = arrData[2].equals("") ? null : arrData[2];
		String provStatus = arrData[3].equals("") ? null : arrData[3];
		String orchestrationStatus = arrData[4].equals("") ? null : arrData[4];
		String resourceVersion = arrData[5].equals("") ? null : arrData[5];
		String inMaint = arrData[6].equals("") ? null : arrData[6];
		String ipaddressv4OamVip = arrData[7].equals("") ? null : arrData[7];
		String isClosedLoopDisabled = arrData[8].equals("") ? null : arrData[8];
		String groupnotation = arrData[9].equals("") ? null : arrData[9];
		String modelInvariantId = arrData[10].equals("") ? null : arrData[10];
		String modelVersionId = arrData[11].equals("") ? null : arrData[11];
		String modelCustomizationId = arrData[12].equals("") ? null : arrData[12];
		String formattedDate = arrData[13].equals("") ? null : arrData[13];

		Vnfc vnfc = new Vnfc(vnfcName, nfcNamingCode, nfcFunction, provStatus, orchestrationStatus, resourceVersion,
				inMaint, ipaddressv4OamVip, isClosedLoopDisabled, groupnotation, modelInvariantId, modelVersionId,
				modelCustomizationId, formattedDate);

		String querySql = " where v.vnfcname =:vnfcname";
		String invalidateSql = "delete from Vnfc v " + querySql;
		List<String> paramList = Arrays.asList("vnfcname:" + vnfcName);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vnfc);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processCp(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVNFC");

		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String cpinstanceid = arrData[0].equals("") ? emptyString : arrData[0];
		Integer portid = arrData[1].equals("") ? null : Integer.parseInt(arrData[1]);
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String vnfcname = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];

		Cp cp = new Cp(cpinstanceid, vnfcname, portid, resourceversion, formattedDate);

		String querySql = " where c.cpinstanceid =:cpinstanceid";
		String invalidateSql = "delete from Cp c " + querySql;
		List<String> paramList = Arrays.asList("cpinstanceid:" + cpinstanceid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(cp);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processAvailabilityZone(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processAvailabilityZone");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String availabilityZoneName = arrData[0].equals("") ? emptyString : arrData[0];
		String hypervisorType = arrData[1].equals("") ? null : arrData[1];
		String operationalStatus = arrData[2].equals("") ? null : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String cloudOwner = arrData[4].equals("") ? null : arrData[4];
		String cloudRegionId = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];

		AvailabilityZone zone = new AvailabilityZone(availabilityZoneName, cloudOwner, cloudRegionId, hypervisorType,
				operationalStatus, resourceversion, formattedDate);

		String querySql = " where a.availabilityZoneName =:availabilityZoneName";
		String invalidateSql = "delete from AvailabilityZone a " + querySql;
		List<String> paramList = Arrays.asList("availabilityZoneName:" + availabilityZoneName);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(zone);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processForwardingPath(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processAvailabilityZone");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String forwardingPathId = arrData[0];
		String forwardingPathName = arrData[1];
		String selflink = arrData[2].equals("") ? emptyString : arrData[2];
		String resourceversion = arrData[3].equals("") ? null : arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];
		ForwardingPath forwardingPath = new ForwardingPath(forwardingPathId, forwardingPathName, selflink,
				resourceversion, formattedDate);

		String querySql = " where fp.forwardingPathId =:forwardingPathId";
		String invalidateSql = "delete from ForwardingPath fp " + querySql;
		List<String> paramList = Arrays.asList("forwardingPathId:" + forwardingPathId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(forwardingPath);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processForwarder(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processAvailabilityZone");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 5)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		Integer sequence = Integer.parseInt(arrData[0]);
		String forwarderRole = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String forwardingPathId = arrData[3];
		String formattedDate = arrData[4].equals("") ? null : arrData[4];
		Forwarder forwarder = new Forwarder(sequence, forwardingPathId, forwarderRole, resourceversion, formattedDate);

		String querySql = " where f.forwardingPathId =:forwardingPathId and f.sequence =:sequence";
		String invalidateSql = "delete from Forwarder f " + querySql;
		List<String> paramList = Arrays.asList("forwardingPathId:" + forwardingPathId, "sequence:" + sequence);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(forwarder);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processModel(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processModel");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String modelInvariantId = arrData[0].equals("") ? emptyString : arrData[0];
		String modelType = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];
		Model model = new Model(modelInvariantId, modelType, resourceversion, formattedDate);

		String querySql = " where m.modelinvariantid =:modelinvariantid";
		String invalidateSql = "delete from Model m " + querySql;
		List<String> paramList = Arrays.asList("modelinvariantid:" + modelInvariantId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(model);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processModelVer(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processModelVer");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 8)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String modelVersionId = arrData[0].equals("") ? emptyString : arrData[0];
		String modelName = arrData[1].equals("") ? null : arrData[1];
		String modelVersion = arrData[2].equals("") ? null : arrData[2];
		String modelDescription = arrData[3].equals("") ? null : arrData[3];
		String distributionStatus = arrData[4].equals("") ? null : arrData[4];
		String resourceVersion = arrData[5].equals("") ? null : arrData[5];
		String modelInvariantId = arrData[6].equals("") ? null : arrData[6];
		String formattedDate = arrData[7].equals("") ? null : arrData[7];
		ModelVer modelVer = new ModelVer(modelVersionId, modelInvariantId, modelName, modelVersion, modelDescription,
				distributionStatus, resourceVersion, formattedDate);

		String querySql = " where m.modelversionid =:modelversionid and m.modelinvariantid =:modelinvariantid";
		String invalidateSql = "delete from ModelVer m " + querySql;
		List<String> paramList = Arrays.asList("modelversionid:" + modelVersionId,
				"modelinvariantid:" + modelInvariantId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(modelVer);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processZone(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processZone");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 7)
			return FAILURE_UPDATE;

		String zoneId = arrData[0].equals("") ? emptyString : arrData[0];
		String zoneName = arrData[1].equals("") ? null : arrData[1];
		String designType = arrData[2].equals("") ? null : arrData[2];
		String zoneContext = arrData[3].equals("") ? null : arrData[3];
		String status = arrData[4].equals("") ? null : arrData[4];
		String resourceVersion = arrData[5].equals("") ? null : arrData[5];
		String inMaint = arrData[6].equals("") ? null : arrData[6];
		String formattedDate = arrData[7].equals("") ? null : arrData[7];

		Zone zone = null;
		NaradZone zoneNarad = null;
		String invalidateSql = null;
		String querySql = " where z.zoneid =:zoneid";

		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			zone = new Zone(zoneId, zoneName, designType, zoneContext, status, resourceVersion, inMaint, formattedDate);
			invalidateSql = "delete from Zone z " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			zoneNarad = new NaradZone(zoneId, zoneName, designType, zoneContext, status, resourceVersion, inMaint,
					formattedDate);
			invalidateSql = "delete from NaradZone z " + querySql;
		}
		// Populate vertica_feed table only for realtime event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("zoneid:" + zoneId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(zone);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(zoneNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processOperationalEnvironment(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processOperationalEnvironment");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 8)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String opEnvId = arrData[0].equals("") ? emptyString : arrData[0];
		String opEnvName = arrData[1].equals("") ? null : arrData[1];
		String opEnvType = arrData[2].equals("") ? null : arrData[2];
		String opEnvStatus = arrData[3].equals("") ? null : arrData[3];
		String tenantContext = arrData[4].equals("") ? null : arrData[4];
		String workloadContext = arrData[5].equals("") ? null : arrData[5];
		String resourceVersion = arrData[6].equals("") ? null : arrData[6];
		String formattedDate = arrData[7].equals("") ? null : arrData[7];
		OperationalEnvironment opEnv = new OperationalEnvironment(opEnvId, opEnvName, opEnvType, opEnvStatus,
				tenantContext, workloadContext, resourceVersion, formattedDate);

		String querySql = " where oe.operationalenvironmentid =:operationalenvironmentid";
		String invalidateSql = "delete from OperationalEnvironment oe " + querySql;
		List<String> paramList = Arrays.asList("operationalenvironmentid:" + opEnvId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(opEnv);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			// retVal=result?SUCCESS_UPDATE:FAILURE_UPDATE;
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}

		return retVal;
	}

	public int processRelationshipList(List<String> lData, String action, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processRelationshipList");
		boolean result = true;
		int retVal = NO_UPDATE;
		RelationshipList relList = null;
		NaradRelationshipList relListNarad = null;
		String invalidateSql = null;
		List<String> paramList = null;
		for (String data : lData) {
			if (data == null) {
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
				return FAILURE_UPDATE;
			}
			String[] arrData = data.split("\\^");
			if (arrData.length < 7)
				return FAILURE_UPDATE;

			String fromNodeId = arrData[0].equals("") ? emptyString : arrData[0];
			String toNodeId = arrData[1].equals("") ? null : arrData[1];
			String relatedFrom = arrData[2].equals("") ? null : arrData[2];
			String relatedTo = arrData[3].equals("") ? null : arrData[3];
			String relatedLink = arrData[4].equals("") ? null : arrData[4];
			String relationshipLabel = arrData[5].equals("") ? null : arrData[5];
			String formattedDate = arrData[6].equals("") ? null : arrData[6];

			String querySql = " where from_node_id =:from_node_id and to_node_id =:to_node_id "
					+ " and related_from =:related_from and related_to =:related_to";
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				relList = new RelationshipList(fromNodeId, toNodeId, relatedFrom, relatedTo, relatedLink,
						relationshipLabel, formattedDate);
				invalidateSql = "delete from RelationshipList " + querySql;
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				relListNarad = new NaradRelationshipList(fromNodeId, toNodeId, relatedFrom, relatedTo, relatedLink,
						relationshipLabel, formattedDate);
				invalidateSql = "delete from NaradRelationshipList " + querySql;
			}
			// Populate vertica_feed table only for real time event.
			if (!action.equalsIgnoreCase(Constants.FULL))
				processVerticaFeed(data, action, "relationship-list", eventType);

			paramList = Arrays.asList("from_node_id:" + arrData[0], "to_node_id:" + arrData[1],
					"related_from:" + relatedFrom, "related_to:" + relatedTo);

			switch (action) {
			case Constants.CREATE:
			case Constants.UPDATE:
			case Constants.FULL:
				if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
					result = updateOrInsertTable(relList);
				} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
					result = updateOrInsertTable(relListNarad);
				}
				retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
				break;
			case Constants.DELETE:
				// invalidate the record
				retVal = deleteTableRow(invalidateSql, paramList);
				break;
			default:
				ecompLogger.debug(Constants.INVALID_ACTION + action);
				break;
			}
			if (retVal == FAILURE_UPDATE)
				break;
		}

		return retVal;
	}

	public String getCurrentTimestampWithMS() {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "getCurrentTimestampWithMS");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public String getCurrentDate() {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "getCurrentDate");
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public int processPNF(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processPNF");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		String pnfname = arrData[0].equals("") ? emptyString : arrData[0];
		String pnfname2 = arrData[1].equals("") ? null : arrData[1];
		String pnfname2source = arrData[2].equals("") ? null : arrData[2];
		String pnfid = arrData[3].equals("") ? null : arrData[3];
		String networkoperatingsystem = "";
		String nfnamingcode = arrData[4].equals("") ? null : arrData[4];
		String equiptype;
		String equipvendor;
		String equipmodel;
		String managementoption;
		String orchestrationstatus;
		String ipaddressv4oam;
		String swversion;
		String inmaint;
		String frameid;
		String serialnumber;
		String ipaddressv4loopback0;
		String ipaddressv6loopback0;
		String ipaddressv4aim;
		String ipaddressv6aim;
		String ipaddressv6oam;
		String invstatus;
		String resourceversion;
		String provstatus;
		String opsnote;
		String configvalidationrequestid;
		String configvalidationstatus;
		String nfrole;	
		String selflink;
		String nftype;
		String nffunction;
		String formattedDate = null;
		Pnf pnf = null;
		NaradPnf pnfNarad = null;
		String invalidateSql = null;

		String querySql = " where pnfname =:pnfname";
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
			equiptype = arrData[5].equals("") ? null : arrData[5];
			equipvendor = arrData[6].equals("") ? null : arrData[6];
			equipmodel = arrData[7].equals("") ? null : arrData[7];
			managementoption = arrData[8].equals("") ? null : arrData[8];
			orchestrationstatus = arrData[9].equals("") ? null : arrData[9];
			ipaddressv4oam = arrData[10].equals("") ? null : arrData[10];
			swversion = arrData[11].equals("") ? null : arrData[11];
			inmaint = arrData[12].equals("") ? null : arrData[12];
			frameid = arrData[13].equals("") ? null : arrData[13];
			serialnumber = arrData[14].equals("") ? null : arrData[14];
			ipaddressv4loopback0 = arrData[15].equals("") ? null : arrData[15];
			ipaddressv6loopback0 = arrData[16].equals("") ? null : arrData[16];
			ipaddressv4aim = arrData[17].equals("") ? null : arrData[17];
			ipaddressv6aim = arrData[18].equals("") ? null : arrData[18];
			ipaddressv6oam = arrData[19].equals("") ? null : arrData[19];
			invstatus = arrData[20].equals("") ? null : arrData[20];
			resourceversion = arrData[21].equals("") ? null : arrData[21];
			provstatus = arrData[22].equals("") ? null : arrData[22];
			nfrole = arrData[23].equals("") ? null : arrData[23];	
			selflink = arrData[24].equals("") ? null : arrData[24];
			formattedDate = arrData[25].equals("") ? null : arrData[25];
			if (arrData.length < 26) {
				return FAILURE_UPDATE;
			}
			pnf = new Pnf(pnfname, pnfname2, pnfname2source, pnfid, nfnamingcode, equiptype, equipvendor, equipmodel,
					managementoption, orchestrationstatus, ipaddressv4oam, swversion, inmaint, frameid, serialnumber, ipaddressv4loopback0,
					ipaddressv6loopback0, ipaddressv4aim, ipaddressv6aim, ipaddressv6oam, invstatus, resourceversion,
					provstatus, nfrole, selflink, formattedDate);
			invalidateSql = "delete from Pnf " + querySql;
		} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			if (arrData.length < 31) {
				return FAILURE_UPDATE;
			}
			networkoperatingsystem = arrData[5].equals("") ? null : arrData[5];
			equiptype = arrData[6].equals("") ? null : arrData[6];
			equipvendor = arrData[7].equals("") ? null : arrData[7];
			equipmodel = arrData[8].equals("") ? null : arrData[8];
			managementoption = arrData[9].equals("") ? null : arrData[9];
			ipaddressv4oam = arrData[10].equals("") ? null : arrData[10];
			swversion = arrData[11].equals("") ? null : arrData[11];
			inmaint = arrData[12].equals("") ? null : arrData[12];
			frameid = arrData[13].equals("") ? null : arrData[13];
			serialnumber = arrData[14].equals("") ? null : arrData[14];
			ipaddressv4loopback0 = arrData[15].equals("") ? null : arrData[15];
			ipaddressv6loopback0 = arrData[16].equals("") ? null : arrData[16];
			ipaddressv4aim = arrData[17].equals("") ? null : arrData[17];
			ipaddressv6aim = arrData[18].equals("") ? null : arrData[18];
			ipaddressv6oam = arrData[19].equals("") ? null : arrData[19];
			invstatus = arrData[20].equals("") ? null : arrData[20];
			resourceversion = arrData[21].equals("") ? null : arrData[21];
			provstatus = arrData[22].equals("") ? null : arrData[22];
			opsnote = arrData[23].equals("") ? null : arrData[23];
			configvalidationrequestid = arrData[24].equals("") ? null : arrData[24];
			configvalidationstatus = arrData[25].equals("") ? null : arrData[25];
			nfrole = arrData[26].equals("") ? null : arrData[26];	
			selflink = arrData[27].equals("") ? null : arrData[27];
			nftype = arrData[28].equals("") ? null : arrData[28];	
			nffunction = arrData[29].equals("") ? null : arrData[29];
			formattedDate = arrData[30].equals("") ? null : arrData[30];
			pnfNarad = new NaradPnf(pnfname, pnfname2, pnfname2source, pnfid, nfnamingcode, networkoperatingsystem, equiptype, equipvendor, equipmodel,
					managementoption, ipaddressv4oam, swversion, inmaint, frameid, serialnumber, ipaddressv4loopback0,
					ipaddressv6loopback0, ipaddressv4aim, ipaddressv6aim, ipaddressv6oam, invstatus, resourceversion,
					provstatus, opsnote, configvalidationrequestid, configvalidationstatus, nfrole, selflink, nftype, nffunction, formattedDate);
			invalidateSql = "delete from NaradPnf " + querySql;
		}
		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		List<String> paramList = Arrays.asList("pnfname:" + pnfname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.AAI_EVENT)) {
				result = updateOrInsertTable(pnf);
			} else if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
				result = updateOrInsertTable(pnfNarad);
			}
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processVPLSPE(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVPLSPE");
		boolean result = true;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 7)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String equipmentname = arrData[0].equals("") ? emptyString : arrData[0];
		String provstatus = arrData[1].equals("") ? null : arrData[1];
		String ipv4oamaddress = arrData[2].equals("") ? null : arrData[2];
		String equipmentrole = arrData[3].equals("") ? null : arrData[3];
		String vlanidouter = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String formattedDate = arrData[6].equals("") ? null : arrData[6];

		VplsPe vplspe = new VplsPe(equipmentname, provstatus, ipv4oamaddress, equipmentrole, vlanidouter,
				resourceversion, formattedDate);

		String querySql = " where equipmentname =:equipmentname";
		String invalidateSql = "delete from VplsPe " + querySql;
		List<String> paramList = Arrays.asList("equipmentname:" + equipmentname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(vplspe);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processSriovVf(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processSriovVf");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 23)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String pciid = arrData[0].equals("") ? emptyString : arrData[0];
		String vfvlanfilter = arrData[1].equals("") ? null : arrData[1];
		String vfmacfilter = arrData[2].equals("") ? null : arrData[2];
		String vfvlanstrip = arrData[3].equals("") ? null : arrData[3];
		String vfvlanantispoofcheck = arrData[4].equals("") ? null : arrData[4];
		String vfmacantispoofcheck = arrData[5].equals("") ? null : arrData[5];
		String vfmirrors = arrData[6].equals("") ? null : arrData[6];
		String vfbroadcastallow = arrData[7].equals("") ? null : arrData[7];
		String vfunknownmulticastallow = arrData[8].equals("") ? null : arrData[8];
		String vfunknownunicastallow = arrData[9].equals("") ? null : arrData[9];
		String vfinsertstag = arrData[10].equals("") ? null : arrData[10];
		String vflinkstatus = arrData[11].equals("") ? null : arrData[11];
		String resourceversion = arrData[12].equals("") ? null : arrData[12];
		String neutronnetworkid = arrData[13].equals("") ? null : arrData[13];
		String pEntityType = arrData[14].equals("") ? null : arrData[14];
		String pEntityId = arrData[15].equals("") ? null : arrData[15];
		String cloudOwner = arrData[16].equals("") ? null : arrData[16];
		String cloudRegionId = arrData[17].equals("") ? null : arrData[17];
		String tenantId = arrData[18].equals("") ? null : arrData[18];
		String grandParentEntityType = arrData[19].equals("") ? null : arrData[19];
		String grandParentEntityId = arrData[20].equals("") ? null : arrData[20];
		String pInterfaceName = arrData[21].equals("") ? null : arrData[21];
		String lagInterfaceName = arrData[22].equals("") ? null : arrData[22];
		String formattedDate = arrData[23].equals("") ? null : arrData[23];

		SriovVf sriovVf = new SriovVf(pciid, grandParentEntityId, vfvlanfilter, vfmacfilter, vfvlanstrip,
				vfvlanantispoofcheck, vfmacantispoofcheck, vfmirrors, vfbroadcastallow, vfunknownmulticastallow,
				vfunknownunicastallow, vfinsertstag, vflinkstatus, resourceversion, neutronnetworkid, pEntityType,
				pEntityId, cloudOwner, cloudRegionId, tenantId, grandParentEntityType, pInterfaceName, lagInterfaceName,
				formattedDate);

		String querySql = " where pciid =:pciid and parententityid=:parententityid";
		String invalidateSql = "delete from SriovVf " + querySql;
		List<String> paramList = Arrays.asList("pciid:" + pciid, "parententityid:" + pEntityId);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(sriovVf);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processSriovPf(String data, String action, String entityType, String eventType) {
		System.out.println("Entering into sriov pf in db adapter");
		ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG, "processSriovPf");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 6)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String pfpciid = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String grandparententitytype = arrData[2].equals("") ? null : arrData[2];
		String grandparententityname = arrData[3].equals("") ? null : arrData[3];
		String interfacename = arrData[4].equals("") ? null : arrData[4];
		String formattedDate = arrData[5].equals("") ? null : arrData[5];

		SriovPf sriovVf = new SriovPf(pfpciid, resourceversion, grandparententitytype, grandparententityname,
				interfacename, formattedDate);

		String querySql = " where pfpciid =:pfpciid and grandparententitytype =:grandparententitytype"
				+ "and grandparententityname =:grandparententityname and interfacename =:interfacename";
		String invalidateSql = "delete from SriovPf " + querySql;
		List<String> paramList = Arrays.asList("pfpciid:" + pfpciid, "grandparententitytype:" + grandparententitytype,
				"grandparententityname:" + grandparententityname, "interfacename:" + interfacename);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(sriovVf);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processLineofBusiness(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processMetadatum");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 3)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String lineofbusinessname = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String formattedDate = arrData[2].equals("") ? null : arrData[2];

		LineofBusiness lineofbusiness = new LineofBusiness(lineofbusinessname, resourceversion, formattedDate);

		String querySql = " where lineofbusinessname =:lineofbusinessname";
		String invalidateSql = "delete from LineofBusiness " + querySql;
		List<String> paramList = Arrays.asList("lineofbusinessname:" + lineofbusinessname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(lineofbusiness);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processOwningEntity(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processMetadatum");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 4)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String owningentityid = arrData[0].equals("") ? emptyString : arrData[0];
		String owningentityname = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String formattedDate = arrData[3].equals("") ? null : arrData[3];

		OwningEntity owningentity = new OwningEntity(owningentityid, owningentityname, resourceversion, formattedDate);

		String querySql = " where owningentityid =:owningentityid";
		String invalidateSql = "delete from OwningEntity " + querySql;
		List<String> paramList = Arrays.asList("owningentityid:" + owningentityid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(owningentity);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processPlatform(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processMetadatum");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 3)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String platformname = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String formattedDate = arrData[2].equals("") ? null : arrData[2];

		Platform platform = new Platform(platformname, resourceversion, formattedDate);

		String querySql = " where platformname =:platformname";
		String invalidateSql = "delete from Platform " + querySql;
		List<String> paramList = Arrays.asList("platformname:" + platformname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(platform);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processProject(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processProject");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 3)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String projectname = arrData[0].equals("") ? emptyString : arrData[0];
		String resourceversion = arrData[1].equals("") ? null : arrData[1];
		String formattedDate = arrData[2].equals("") ? null : arrData[2];

		Project project = new Project(projectname, resourceversion, formattedDate);

		String querySql = " where projectname =:projectname";
		String invalidateSql = "delete from Project " + querySql;
		List<String> paramList = Arrays.asList("projectname:" + projectname);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(project);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processMetadatum(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processMetadatum");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 6)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String metaname = arrData[0].equals("") ? emptyString : arrData[0];
		String metaval = arrData[1].equals("") ? null : arrData[1];
		String resourceversion = arrData[2].equals("") ? null : arrData[2];
		String parententitytype = arrData[3].equals("") ? null : arrData[3];
		String parententityid = arrData[4].equals("") ? null : arrData[4];
		String formattedDate = arrData[5].equals("") ? null : arrData[5];

		Metadatum metadatum = new Metadatum(metaname, parententityid, metaval, resourceversion, parententitytype,
				formattedDate);

		String querySql = " where metaname =:metaname and parententityid=:parententityid";
		String invalidateSql = "delete from Metadatum " + querySql;
		List<String> paramList = Arrays.asList("metaname:" + metaname, "parententityid:" + parententityid);

		switch (action) {
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(metadatum);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	public int processCloudVIPIPV4AddressList(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processOperationalEnvironment");
		boolean result = true;
		int retVal = 0;
		String[] arrData = data.split("\\^");
		if (arrData.length < 11)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vipipv4address = arrData[0].equals("") ? emptyString : arrData[0];
		long vipipv4prefixlength = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
		long vlanidinner = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
		long vlanidouter = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		String isfloating = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String neutronnetworkid = arrData[6].equals("") ? null : arrData[6];
		String neutronsubnetid = arrData[7].equals("") ? null : arrData[7];
		String cloudowner = arrData[8].equals("") ? null : arrData[8];
		String cloudregionid = arrData[9].equals("") ? null : arrData[9];
		String formattedDate = arrData[10].equals("") ? null : arrData[10];
		Cloudvipipv4addresslist cloudvipipv4addresslist = new Cloudvipipv4addresslist(vipipv4address, cloudowner,
				cloudregionid, vipipv4prefixlength, vlanidinner, vlanidouter, isfloating, resourceversion,
				neutronnetworkid, neutronsubnetid, formattedDate);

		String querySql = " where vip.vipipv4address =:vipipv4address AND "
				+ "vip.cloudowner =:cloudowner AND vip.cloudregionid =:cloudregionid";
		String invalidateSql = "delete from Cloudvipipv4addresslist vip " + querySql;
		List<String> paramList = Arrays.asList("vipipv4address:" + vipipv4address, "cloudowner:" + cloudowner,
				"cloudregionid:" + cloudregionid);

		switch (action) {
		case Constants.CREATE:
		case "UPDATEFULL":
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(cloudvipipv4addresslist);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			break;
		}

		return retVal;
	}

	public int processCloudVIPIPV6AddressList(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processOperationalEnvironment");
		boolean result = true;
		int retVal = 0;
		String[] arrData = data.split("\\^");
		if (arrData.length < 11)
			return FAILURE_UPDATE;

		// Populate vertica_feed table only for real time event.
		if (!action.equalsIgnoreCase(Constants.FULL))
			processVerticaFeed(data, action, entityType, eventType);

		String vipipv6address = arrData[0].equals("") ? emptyString : arrData[0];
		long vipipv6prefixlength = arrData[1].equals("") ? 0L : Long.parseLong(arrData[1]);
		long vlanidinner = arrData[2].equals("") ? 0L : Long.parseLong(arrData[2]);
		long vlanidouter = arrData[3].equals("") ? 0L : Long.parseLong(arrData[3]);
		String isfloating = arrData[4].equals("") ? null : arrData[4];
		String resourceversion = arrData[5].equals("") ? null : arrData[5];
		String neutronnetworkid = arrData[6].equals("") ? null : arrData[6];
		String neutronsubnetid = arrData[7].equals("") ? null : arrData[7];
		String cloudowner = arrData[8].equals("") ? null : arrData[8];
		String cloudregionid = arrData[9].equals("") ? null : arrData[9];
		String formattedDate = arrData[10].equals("") ? null : arrData[10];
		Cloudvipipv6addresslist cloudvipipv6addresslist = new Cloudvipipv6addresslist(vipipv6address, cloudowner,
				cloudregionid, vipipv6prefixlength, vlanidinner, vlanidouter, isfloating, resourceversion,
				neutronnetworkid, neutronsubnetid, formattedDate);

		String querySql = " where vip.vipipv6address =:vipipv6address AND "
				+ "vip.cloudowner =:cloudowner AND vip.cloudregionid =:cloudregionid";
		String invalidateSql = "delete from Cloudvipipv6addresslist vip " + querySql;
		List<String> paramList = Arrays.asList("vipipv6address:" + vipipv6address, "cloudowner:" + cloudowner,
				"cloudregionid:" + cloudregionid);

		switch (action) {
		case Constants.CREATE:
		case "UPDATEFULL":
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(cloudvipipv6addresslist);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			break;
		}

		return retVal;
	}

	public int processVerticaFeed(String data, String action, String entityType, String eventType) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processVerticaFeed");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		if (eventType.equalsIgnoreCase(com.att.vcc.inventorycollector.util.Constants.NARAD_EVENT)) {
			entityType = "narad-" + entityType;
		}
		String[] arrData = data.split("\\^");
		String datetimestamp = arrData[arrData.length - 1];

		// Logic to remove the timestamp from the data passed to vETL
		String[] arrData_new = Arrays.copyOfRange(arrData, 0, arrData.length - 1);
		String new_data = StringUtils.join(arrData_new, "^");
		// add validfrom & validto
		String new_rec = datetimestamp + ";;" + new_data.replace("^", ";");
		// vETL / Vertica needs the action type in lower case
		new_rec += ";" + action.toLowerCase() + ";";

		VerticaFeed verticafeed = new VerticaFeed(entityType, action, new_rec, datetimestamp);

		result = insertTable(verticafeed);
		retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
		return retVal;
	}

	public int processDcaeEvent(String data) {
		// ecompLogger.debug(inventoryCollectorMessageEnum.DTI_DBADAPTER_INSIDE_FUNCTION_DEBUG,
		// "processDcaeEvent");
		boolean result;
		int retVal = 0;
		if (data == null) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_DATA_NULL_ERROR, "");
			return FAILURE_UPDATE;
		}
		String[] arrData = data.split("\\^");

		if (arrData.length < 26)
			return FAILURE_UPDATE;

		String dcaetargetname = arrData[0].equals("") ? emptyString : arrData[0];
		String dcaetargettype = arrData[1].equals("") ? null : arrData[1];
		String dcaeservicelocation = arrData[2].equals("") ? null : arrData[2];
		String dcaeserviceaction = arrData[3].equals("") ? null : arrData[3];
		String dcaetargetprovstatus = arrData[4].equals("") ? null : arrData[4];
		String dcaeservicetype = arrData[5].equals("") ? null : arrData[5];
		String dcaetargetinmaint = arrData[6].equals("") ? null : arrData[6];
		String dcaetargetisclosedloopdisabled = arrData[7].equals("") ? null : arrData[7];
		String dcaeserviceinstancemodelinvariantid = arrData[8].equals("") ? null : arrData[8];
		String dcaeserviceinstancemodelversionid = arrData[9].equals("") ? null : arrData[9];
		String dcaegenericvnfmodelinvariantid = arrData[10].equals("") ? null : arrData[10];
		String dcaegenericvnfmodelversionid = arrData[11].equals("") ? null : arrData[11];
		String dcaetargetcollection = arrData[12].equals("") ? null : arrData[12];
		String dcaetargetcollectionip = arrData[13].equals("") ? null : arrData[13];
		String dcaesnmpcommunitystring = arrData[14].equals("") ? null : arrData[14];
		String dcaesnmpversion = arrData[15].equals("") ? null : arrData[15];
		String dcaetargetcloudregionid = arrData[16].equals("") ? null : arrData[16];
		String dcaetargetcloudregionversion = arrData[17].equals("") ? null : arrData[17];
		String dcaetargetservicedescription = arrData[18].equals("") ? null : arrData[18];
		String event = arrData[19].equals("") ? null : arrData[19];
		String aaiadditionalinfo = arrData[20].equals("") ? null : arrData[20];
		String dcaeeventsentflag = arrData[21].equals("") ? null : arrData[21];
		String dcaeeventstatus = arrData[22].equals("") ? null : arrData[22];
		String dcaeeventretryinterval = arrData[23].equals("") ? "60" : arrData[23];
		String dcaeeventretrynumber = arrData[24].equals("") ? "0" : arrData[24];
		String updatedOn = arrData[25].equals("") ? null : arrData[25];

		DcaeEvent dcaeEvent = new DcaeEvent(dcaetargetname, dcaetargettype, dcaeservicelocation, dcaeserviceaction,
				dcaetargetprovstatus, dcaeservicetype, dcaetargetinmaint, dcaetargetisclosedloopdisabled,
				dcaeserviceinstancemodelinvariantid, dcaeserviceinstancemodelversionid, dcaegenericvnfmodelinvariantid,
				dcaegenericvnfmodelversionid, dcaetargetcollection, dcaetargetcollectionip, dcaesnmpcommunitystring,
				dcaesnmpversion, dcaetargetcloudregionid, dcaetargetcloudregionversion, dcaetargetservicedescription,
				event, aaiadditionalinfo, dcaeeventsentflag, dcaeeventstatus, dcaeeventretryinterval,
				dcaeeventretrynumber, updatedOn);

		String querySql = " where dcaetargetname =:dcaetargetname and dcaetargettype=:dcaetargettype";
		String invalidateSql = "delete from DcaeEvent " + querySql;
		List<String> paramList = Arrays.asList("dcaetargetname:" + dcaetargetname, "dcaetargettype:" + dcaetargettype);

		retVal = deleteTableRow(invalidateSql, paramList);

		result = updateOrInsertTable(dcaeEvent);
		retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
		return retVal;
	}

	public List getStaticDcaeEvents(String dcaeeventstatus) {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from DcaeEvent where dcaeeventstatus=:dcaeeventstatus")
					.setParameter("dcaeeventstatus", dcaeeventstatus);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List<CommunityString> getCommunityString(String resourceName) {
		List<CommunityString> list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from CommunityString where resourcename=:resourcename")
					.setParameter("resourcename", resourceName);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List<CommunityString> getCommunityString(String resourceName, String reservationId) {
		List<CommunityString> list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery(
							"from CommunityString where resourcename=:resourcename and reservationid=:reservationid")
					.setParameter("resourcename", resourceName).setParameter("reservationid", reservationId);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List<Vserver> getVserverDetails(String vserverName) {
		List<Vserver> list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Vserver where vservername=:vservername").setParameter("vservername",
					vserverName);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public String getServiceLocation(String emslocation) {
		String servicelocation = "";
		List<String> list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("SELECT r.toNodeId " + "FROM Cloudregion c, RelationshipList r "
					+ "where c.cloudregionid=r.fromNodeId and r.relatedTo=:complex and c.cloudregionid=:emslocation");
			query.setParameter("complex", "complex");
			query.setParameter("emslocation", emslocation);
			list = query.list();
			tx.commit();
			servicelocation = list.get(0);

		} catch (HibernateException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_FAILED_CREATE_SESSIONFACTORY_ERROR,
					e.getMessage());
		} catch (Exception e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_FAILED_CREATE_SESSIONFACTORY_ERROR,
					e.getMessage());
		} finally {
			session.close();
		}

		return servicelocation;
	}

	public int processSeedingmanager(SeedingManager seedingmanager, String action) {
		boolean result = true;
		int retVal = 0;
		String hostname = seedingmanager.getHostname();
		String queryoutput = checkseedingmanagertable(hostname);
		if (queryoutput == null) {
			if (action == null || action.equalsIgnoreCase("")) {
				action = Constants.UPDATE;
			}
			if (Constants.DELETE.equalsIgnoreCase(action)) {
				seedingmanager.setChangetype(Constants.DELETE);
			} else {
				seedingmanager.setChangetype("ADD");
			}
		} else {
			if (action.equalsIgnoreCase("CREATE")) {
				seedingmanager.setChangetype("ADD");
			} else if (action.equalsIgnoreCase(Constants.DELETE)) {
				seedingmanager.setChangetype(Constants.DELETE);
			} else {
				String[] icmpandsnmpversion = queryoutput.split(",");
				if (icmpandsnmpversion[0] != null && icmpandsnmpversion[0].equalsIgnoreCase(seedingmanager.getIcmpip())
						&& icmpandsnmpversion[1] != null
						&& icmpandsnmpversion[1].equalsIgnoreCase(seedingmanager.getSnmpversion())) {
					action = Constants.UPDATE;
					seedingmanager.setChangetype("UNCHANGED");
				} else {
					action = Constants.UPDATE;
					seedingmanager.setChangetype(Constants.UPDATE);
				}
			}
		}
		String querySql = " where hostname =:hostname";
		String invalidateSql = "delete from SeedingManager " + querySql;
		List<String> paramList = Arrays.asList("hostname:" + hostname);

		switch (action) {
		case "SELECT":
		case Constants.CREATE:
		case Constants.UPDATE:
		case Constants.FULL:
			result = updateOrInsertTable(seedingmanager);
			retVal = result ? SUCCESS_UPDATE : FAILURE_UPDATE;
			break;
		case Constants.DELETE:
			// invalidate the record
			retVal = deleteTableRow(invalidateSql, paramList);
			break;
		default:
			ecompLogger.debug(Constants.INVALID_ACTION + action);
			break;
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getModifiedRecordsUsingUpdatedOn(String entityType, String datetimestamp) {
		ecompLogger.info(inventoryCollectorMessageEnum.DTI_DBADAPTER_GET_MODIFIED_RECORD_DEBUG, entityType);

		Map<String, String> updatedRecords = new HashMap<>();
		String param = EventUtil.tablePrimaryKeyProperties.get(entityType);
		String[] arr = param.split(";");
		if (arr.length < 2) {
			System.out.println("tablePrimaryKeyProperties is not having proper values for entity : " + entityType);
			return updatedRecords;
		}
		String table = arr[0];
		String fields = arr[1];
		int fieldLen = fields.split(",").length;

		List<String> paramList = Arrays.asList("updatedon:" + datetimestamp);
		// Get all valid keys from table
		List<Object[]> dbLst = queryTable(
				"select " + fields + ",resourceversion from " + table + " where updatedOn >:updatedon", paramList);

		if (!dbLst.isEmpty()) {

			for (Object[] dbRec : dbLst) {

				String compData = "";
				String resVer = "";
				int j = 0;
				while (j < fieldLen) {
					if (++j == fieldLen) {
						resVer += dbRec[j];
					}
					compData += dbRec[(j - 1)];

					if (j < fieldLen)
						compData += "|";
				}
				updatedRecords.put(compData, resVer);
			}
		}
		return updatedRecords;
	}

	public String checkseedingmanagertable(String hostname) {
		String outputstring = null;
		List<SeedingManager> outputlist = queryHostname(hostname);
		if (outputlist == null) {
			return outputstring;
		}
		Iterator<SeedingManager> it = outputlist.iterator();
		SeedingManager seedingmanager = null;
		while (it.hasNext()) {
			seedingmanager = it.next();
		}
		if (seedingmanager == null) {
			return null;
		} else {
			outputstring = seedingmanager.getIcmpip() + "," + seedingmanager.getSnmpversion();
			return outputstring;
		}
	}

	public List getAllVserver() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Vserver");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllPserver() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Pserver");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllPnf() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Pnf");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllNaradPnf() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from NaradPnf");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllGenericVnf() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from GenericVnf");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllVce() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Vce");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List getAllVnfc() {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Vnfc");
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List queryHostname(String hostname) {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from SeedingManager where hostname=:hostname").setParameter("hostname",
					hostname);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public List queryGetUmatchedTimestamp(String updatedOn) {
		List list = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from SeedingManager where updatedOn!=:updatedOn")
					.setParameter("updatedOn", updatedOn);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
					e.getCause().getMessage());
		} finally {
			session.close();
		}
		return list;
	}

	public String getToNodeId(String fromNodeId) {
		if (fromNodeId != null) {
			List<RelationshipList> list = null;
			Session session = factory.openSession();
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				Query query = session.createQuery("from RelationshipList where from_node_id=:fromnodeid")
						.setParameter("fromnodeid", fromNodeId);
				list = query.list();
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				ecompLogger.error(inventoryCollectorMessageEnum.DTI_DBADAPTER_HIBERNATE_EXCEPTION_SQL_ERROR,
						e.getCause().getMessage());
			} finally {
				session.close();
			}

			RelationshipList relationshipList = null;
			if (list != null) {
				Iterator<RelationshipList> it = list.iterator();
				while (it.hasNext()) {
					relationshipList = it.next();
				}
			}
			if (relationshipList == null) {
				return null;
			} else {
				return relationshipList.getToNodeId();
			}
		}
		return null;
	}

}