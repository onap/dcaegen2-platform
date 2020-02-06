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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.att.vcc.inventorycollector.messages.inventoryCollectorMessageEnum;
import com.att.vcc.inventorycollector.schema.CloudRegion;
import com.att.vcc.inventorycollector.schema.CloudRegions;
import com.att.vcc.inventorycollector.schema.GenericVnf;
import com.att.vcc.inventorycollector.schema.L3InterfaceIpv4AddressList;
import com.att.vcc.inventorycollector.schema.LInterface;
import com.att.vcc.inventorycollector.schema.LInterfaces;
import com.att.vcc.inventorycollector.schema.Pnf;
import com.att.vcc.inventorycollector.schema.Pserver;
import com.att.vcc.inventorycollector.schema.Relationship;
import com.att.vcc.inventorycollector.schema.RelationshipData;
import com.att.vcc.inventorycollector.schema.RelationshipList;
import com.att.vcc.inventorycollector.schema.Tenant;
import com.att.vcc.inventorycollector.schema.Vserver;
import com.att.vcc.inventorycollector.util.DTIException;
import com.att.vcc.inventorycollector.domain.SeedingManager;
import com.att.vcc.inventorycollector.domain.Zone;
import com.att.ecomp.logger.EcompLogger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class SeedingManagerController {

	private ObjectMapper mapper;
	private static EcompLogger ecompLogger = EcompLogger.getEcompLogger();

	public SeedingManagerController() {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		mapper.registerModule(new JaxbAnnotationModule());
	}

	// This method is not used for 1802 release
	public String getPNF(DBAdapter dbadapter, Pnf pnf, String datetimestamp, RelationshipList relationshipList) {
		String aaipnfstringoutput = null;
		if (pnf != null) {
			SeedingManager seedingmanagerpojo = new SeedingManager();
			seedingmanagerpojo.setHostname(pnf.getPnfName());
			seedingmanagerpojo.setDesigntype(zonedesigntype(dbadapter, pnf.getPnfName()));

			String functioncode = pnf.getPnfName().substring(8, 11);
			String[] functioncodespineandleafarray = { "js1", "js2", "jl1", "jl2" };
			String functioncodel2sw = "ilx";
			String functioncodeterminalserver = "nm9";

			List<String> list = Arrays.asList(functioncodespineandleafarray);
			if (list.contains(functioncode)) {
				seedingmanagerpojo.setIcmpip(pnf.getIpaddressV4Aim());
				seedingmanagerpojo.setSnmpip(pnf.getIpaddressV4Aim());
				seedingmanagerpojo.setOutputobjects("!arpTable");
			}
			if (functioncode.equalsIgnoreCase(functioncodel2sw)) {
				seedingmanagerpojo.setIcmpip(pnf.getIpaddressV4Oam());
				seedingmanagerpojo.setSnmpip(pnf.getIpaddressV4Oam());
				seedingmanagerpojo.setDesigntype("Medium-lite");
				seedingmanagerpojo.setOutputobjects("!arpTable");
			}
			if (functioncode.equalsIgnoreCase(functioncodeterminalserver)) {
				seedingmanagerpojo.setIcmpip(pnf.getIpaddressV4Oam());
				seedingmanagerpojo.setSnmpip(pnf.getIpaddressV4Oam());
				seedingmanagerpojo.setDesigntype("Medium-lite");
				seedingmanagerpojo.setOutputobjects("!arpTable");
			}
			seedingmanagerpojo.setCommunitystring("communitystring");
			seedingmanagerpojo.setSnmpversion("snmpversion");
			seedingmanagerpojo.setLocation("locationid");
			seedingmanagerpojo.setDevicetype(pnf.getEquipType());
			seedingmanagerpojo.setEntitytype("pnf");
			seedingmanagerpojo.setFunctioncode(functioncode);
			seedingmanagerpojo.setDevicechangetimestamp(datetimestamp);
			seedingmanagerpojo.setIcmpintervalclass("A");
			seedingmanagerpojo.setFmmibpollerintervalclass("A");
			seedingmanagerpojo.setChangetype("ADD");

			dbadapter.processSeedingmanager(seedingmanagerpojo, "add");
			aaipnfstringoutput = "success";
		}
		return aaipnfstringoutput;
	}

	public SeedingManager getVNF(DBAdapter dbadapter, String entitystring, String action) {
		GenericVnf genericVnf;
		try {
			genericVnf = mapper.readValue(entitystring, GenericVnf.class);
			String vnftype = genericVnf.getVnfType();
			String devicename = genericVnf.getVnfName();
			int devicelength = devicename.length();
			String vcevpedevice = devicename.substring(devicelength - 3);
			String provStatus = genericVnf.getProvStatus();
			final String regex = "([Rr][vV][1-7])$";
			final Pattern pattern = Pattern.compile(regex);
			final Matcher matcher = pattern.matcher(devicename);
			if (provStatus != null && "PROV,NVTPROV".contains(provStatus)) {
				if (vnftype != null) {
					if (devicename.endsWith("me6") || devicename.endsWith("vbc") || devicename.endsWith("vs1")
							|| matcher.find() || vnftype.equalsIgnoreCase("HP") || vnftype.equalsIgnoreCase("HG")) {
						SeedingManager seedingmanagerpojo = new SeedingManager();
						seedingmanagerpojo.setHostname(genericVnf.getVnfName());
						seedingmanagerpojo.setIcmpip(genericVnf.getIpv4OamAddress());
						seedingmanagerpojo.setSnmpip(genericVnf.getIpv4OamAddress());
						// seedingmanagerpojo.setCommunitystring("NA");
						// seedingmanagerpojo.setSnmpversion("NA");
						seedingmanagerpojo.setDesigntype("NA");
						seedingmanagerpojo.setLocation(getLocationId(genericVnf.getRelationshipList()));
						seedingmanagerpojo.setDevicetype(genericVnf.getVnfType());
						seedingmanagerpojo.setEntitytype("generic-vnf");
						seedingmanagerpojo.setOutputobjects("!arpTable");
						seedingmanagerpojo.setFmmibpollerintervalclass("A");
						if (vcevpedevice.equalsIgnoreCase("me6")) {
							seedingmanagerpojo.setIcmpip(genericVnf.getIpv4Loopback0Address());
							seedingmanagerpojo.setSnmpip(genericVnf.getIpv4Loopback0Address());
							seedingmanagerpojo.setFunctioncode("me6");
						} else if (vcevpedevice.equalsIgnoreCase("vs1")) {
							seedingmanagerpojo.setFunctioncode("vs1");
						} else if (vcevpedevice.equalsIgnoreCase("vbc")) {
							seedingmanagerpojo.setFunctioncode("vbc");
						} else if (genericVnf.getVnfType().equalsIgnoreCase("HP")) {
							seedingmanagerpojo.setFunctioncode("HP");
						} else if (genericVnf.getVnfType().equalsIgnoreCase("HG")) {
							seedingmanagerpojo.setFunctioncode("HG");
						} else {
							seedingmanagerpojo.setIcmpip(genericVnf.getIpv4Loopback0Address());
							seedingmanagerpojo.setSnmpip(genericVnf.getIpv4Loopback0Address());
							String functioncode = matcher.group(1);
							seedingmanagerpojo.setFunctioncode(functioncode);
						}
						seedingmanagerpojo.setDevicechangetimestamp(EventUtil.getCurrentTimestamp());
						seedingmanagerpojo.setIcmpintervalclass("B");
						seedingmanagerpojo.setFmmibpollerintervalclass("A");
						if ("DELETE".equalsIgnoreCase(action)) {
							seedingmanagerpojo.setChangetype("DELETE");
						} else {
							seedingmanagerpojo.setChangetype("ADD");
						}
						int status = dbadapter.processSeedingmanager(seedingmanagerpojo, action);
						if (status < 1) {
							System.out.println("Error in DB processing.");
						} else {
							if (!action.equalsIgnoreCase("DELETE")) {
								List<SeedingManager> outputList = dbadapter.queryHostname(genericVnf.getVnfName());
								Iterator<SeedingManager> it = outputList.iterator();
								while (it.hasNext()) {
									seedingmanagerpojo = it.next();
								}
							} else {
								seedingmanagerpojo.setChangetype("DELETE");
							}
						}
						return seedingmanagerpojo;
					}
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getVNF(DBAdapter dbadapter, GenericVnf vnf, String datetimestamp, RelationshipList relationshipList) {
		String vnftype = vnf.getVnfType();
		String vnfName = vnf.getVnfName();
		final String regex = "([Rr][vV][1-7])$";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(vnfName);
		if (vnf != null && vnfName.endsWith("vbc") || vnfName.endsWith("me6") || vnfName.endsWith("vs1")
				|| matcher.find() || vnftype.equalsIgnoreCase("HP") || vnftype.equalsIgnoreCase("HG")) {
			SeedingManager seedingmanagerpojo = new SeedingManager();
			seedingmanagerpojo.setHostname(vnf.getVnfName());
			seedingmanagerpojo.setIcmpip(vnf.getIpv4OamAddress());
			seedingmanagerpojo.setSnmpip(vnf.getIpv4OamAddress());
			seedingmanagerpojo.setCommunitystring("NA");
			seedingmanagerpojo.setSnmpversion("NA");
			seedingmanagerpojo.setDesigntype("NA");
			seedingmanagerpojo.setLocation(getLocationId(relationshipList));
			seedingmanagerpojo.setDevicetype(vnf.getVnfType());
			seedingmanagerpojo.setOutputobjects("!arpTable");
			seedingmanagerpojo.setFmmibpollerintervalclass("A");
			seedingmanagerpojo.setEntitytype("generic-vnf");
			if (vnfName.endsWith("me6")) {
				seedingmanagerpojo.setFunctioncode("me6");
				seedingmanagerpojo.setFmmibpollerintervalclass("B");
			} else if (vnfName.endsWith("vs1")) {
				seedingmanagerpojo.setFunctioncode("vs1");
			} else if (vnfName.endsWith("vbc")) {
				seedingmanagerpojo.setFunctioncode("vbc");
				seedingmanagerpojo.setOutputobjects("N/A");
			} else if (vnf.getVnfType().equalsIgnoreCase("HP")) {
				seedingmanagerpojo.setFunctioncode("HP");
				seedingmanagerpojo.setOutputobjects("N/A");
			} else if (vnf.getVnfType().equalsIgnoreCase("HG")) {
				seedingmanagerpojo.setFunctioncode("HG");
				seedingmanagerpojo.setOutputobjects("N/A");
			} else {
				seedingmanagerpojo.setIcmpip(vnf.getIpv4Loopback0Address());
				seedingmanagerpojo.setSnmpip(vnf.getIpv4Loopback0Address());
				String functioncode = matcher.group(1);
				seedingmanagerpojo.setFunctioncode(functioncode);
			}
			seedingmanagerpojo.setDevicechangetimestamp(datetimestamp);
			seedingmanagerpojo.setIcmpintervalclass("B");
			seedingmanagerpojo.setChangetype("ADD");

			dbadapter.processSeedingmanager(seedingmanagerpojo, "add");
			return "success";
		}
		return null;
	}

	public String getVServer(DBAdapter dbadapter, Tenant tenant, Vserver vserver, String cloudOwner,
			String cloudRegionId, String action, String datetimestamp) {
		String aaipnfstringoutput = null;
		if (vserver != null) {
			SeedingManager seedingmanagerpojo = new SeedingManager();
			seedingmanagerpojo.setHostname(vserver.getVserverName());
			seedingmanagerpojo.setIcmpip(getl3interfaceipv4address(vserver));
			seedingmanagerpojo.setSnmpip(getl3interfaceipv4address(vserver));
			seedingmanagerpojo.setCommunitystring("communitystring");
			seedingmanagerpojo.setSnmpversion("snmpversion");
			seedingmanagerpojo.setDesigntype("zone.desing-type");
			seedingmanagerpojo.setLocation("location");
			seedingmanagerpojo.setDevicetype("N/A");
			seedingmanagerpojo.setEntitytype("vserver");
			seedingmanagerpojo.setFunctioncode("tvpr");
			seedingmanagerpojo.setOutputobjects("N/A");
			seedingmanagerpojo.setDevicechangetimestamp(datetimestamp);
			seedingmanagerpojo.setIcmpintervalclass("B");
			seedingmanagerpojo.setFmmibpollerintervalclass("A");
			seedingmanagerpojo.setChangetype("ADD");
			dbadapter.processSeedingmanager(seedingmanagerpojo, "add");
			aaipnfstringoutput = "success";
		}
		return aaipnfstringoutput;
	}

	private String getl3interfaceipv4address(Vserver vserver) {
		LInterfaces linterfaces = vserver.getLInterfaces();
		List<LInterface> linterfacelist = linterfaces.getLInterface();
		String oamnetwork = "OAM_NET";
		String l3interfaceipv4address = null;
		for (LInterface lInterface : linterfacelist) {
			if (oamnetwork.equalsIgnoreCase(lInterface.getNetworkName())) {
				List<L3InterfaceIpv4AddressList> l3InterfaceIpv4AddressLists = lInterface
						.getL3InterfaceIpv4AddressList();
				for (L3InterfaceIpv4AddressList l3InterfaceIpv4AddressList : l3InterfaceIpv4AddressLists) {
					l3interfaceipv4address = l3InterfaceIpv4AddressList.getL3InterfaceIpv4Address();
				}
			}
		}
		return l3interfaceipv4address;
	}

	private String zonedesigntype(DBAdapter dbadapter, String fromnodeid) {
		String tonodeid = dbadapter.getToNodeId(fromnodeid);
		RESTClient client = new RESTClient();
		Zone zone = null;
		if (tonodeid == null) {
			return null;
		} else {
			String aaiurl;
			try {
				aaiurl = EventUtil.API_URL + "network/zones/zone/" + URLEncoder.encode(tonodeid, "UTF-8");
				String aairesult = client.retrieveAAIObject(aaiurl);
				zone = mapper.readValue(aairesult, Zone.class);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (DTIException e) {
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (zone == null)
			return null;

		return zone.getDesigntype();
	}

	public String getLocationId(RelationshipList relationshiplist) {
		List<Relationship> relationships = relationshiplist.getRelationship();
		for (Relationship relationship : relationships) {
			if (relationship.getRelatedTo().equalsIgnoreCase("vserver")) {
				Vserver vserver = getVserver(relationship);
				if (vserver != null) {
					List<Relationship> relationshipservers = vserver.getRelationshipList().getRelationship();
					for (Relationship relationshipvserver : relationshipservers) {
						if (relationshipvserver.getRelatedTo().equalsIgnoreCase("pserver")) {
							Pserver pserver = getPserver(relationshipvserver);
							return getComplexLocationIdFromPserver(pserver);
						}
					}
				}
			}
		}
		return null;
	}

	private String getComplexLocationIdFromPserver(Pserver pserver) {
		if (pserver != null) {
			List<Relationship> relationships = pserver.getRelationshipList().getRelationship();
			for (Relationship relationship : relationships) {
				if (relationship.getRelatedTo().equalsIgnoreCase("complex")) {
					List<RelationshipData> relationshipdatas = relationship.getRelationshipData();
					for (RelationshipData relationshipdata : relationshipdatas) {
						if (relationshipdata.getRelationshipKey().equalsIgnoreCase("complex.physical-location-id"))
							return relationshipdata.getRelationshipValue();
					}
				}
			}
		}
		return null;
	}

	private Vserver getVserver(Relationship relationship) {
		String path = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9) + relationship.getRelatedLink();
		RESTClient client = new RESTClient();
		String result = "";
		Vserver vserver = null;
		try {
			result = client.retrieveAAIObject(path);
		} catch (DTIException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_DTIEXCEPTION, path, e.getMessage());
		}
		try {
			vserver = mapper.readValue(result, Vserver.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vserver;
	}

	private Pserver getPserver(Relationship relationship) {
		String path = EventUtil.API_URL.substring(0, EventUtil.API_URL.length() - 9) + relationship.getRelatedLink();
		RESTClient client = new RESTClient();
		String result = "";
		Pserver pserver = null;
		try {
			result = client.retrieveAAIObject(path);
		} catch (DTIException e) {
			ecompLogger.error(inventoryCollectorMessageEnum.DTI_EVENT_PROCESSOR_DTIEXCEPTION, path, e.getMessage());
		}
		try {
			pserver = mapper.readValue(result, Pserver.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pserver;
	}
}