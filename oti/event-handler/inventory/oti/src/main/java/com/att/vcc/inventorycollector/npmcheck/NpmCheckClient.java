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

package com.att.vcc.inventorycollector.npmcheck;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;

import com.att.aft.dme2.internal.javaxwsrs.core.MediaType;
import com.att.aft.dme2.internal.jettison.json.JSONArray;
import com.att.aft.dme2.internal.jettison.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



public class NpmCheckClient {
	static String payloadTemplate ="[\r\n" + 
			"       {\r\n" + 
			"              \"resourceKey\": \"PNF-INSTANCE \",\r\n" + 
			"              \"resourceName\": [ \"jl1-dis\" ],\r\n" +
			"               \"decrypt\": " +false+"\r\n"+
			"       }\r\n" + 	
			"]\r\n" + 
			"\r\n" + 
			"";
	/*static String payloadTemplate ="[\r\n" + 
			"       {\r\n" + 
			"              \"resourceKey\": \"\",\r\n" + 
			"              \"resourceName\": [\"\"],\r\n" +
			"               \"vnfName\": [\"\"]\r\n"+
			"       }\r\n" + 
			"]\r\n" + 
			"\r\n" + 
			"";*/
	public static void main(String[] args) {
		try {
			System.out.println("print substring"+"chcgilcld03".substring(0,8));
			if(args.length>0 && args[0].equals("getallvnfs")){
				System.out.println("configured runtime getallvnfs"+args[0]);
			}
			System.out.println("file seperator"+File.separator);
			String payload = payloadTemplate;
   
			//Where to get these values?? URL, ENV and RouterOffer
			String service="https://microservice-router.lpp.att.com/services/microservice_router/v1/routes?version=0.1&envContext=TEST&routeOffer=ST3";
	
		System.setProperty("AFT_LATITUDE", "35.7719444");
		System.setProperty("AFT_LONGITUDE", "-78.6388889");
		System.setProperty("AFT_ENVIRONMENT","AFTUAT");
		
		//Properties props = new Properties();
		//props.setProperty("AFT_DME2_SSL_ENABLE", "false");
		//System.setProperty("AFT_DME2_CLIENT_IGNORE_SSL_CONFIG", "true");
        //long timeoutMs = Long.parseLong(args[1]);

        // try to call the service just registered
		String mechIdPassword = "test123@vcc.dcae.att.com"+":"+"testtesttest";
		mechIdPassword = Base64.getEncoder().encodeToString(mechIdPassword.getBytes());
   
   System.out.println("mechid password Base64 encoder"+mechIdPassword);
   DME2Client client = new DME2Client(new URI(service), 90000);
   
   HashMap<String,String> resID=new HashMap<String, String>();
   System.out.println("hashmap result"+resID);
   if(resID.isEmpty()){
	   System.out.println("print if"+resID.size());  
   }
   
   if(!resID.isEmpty()){
	   System.out.println("print else"+resID);  
	   
	   client.setQueryParams(resID, true);
   }
   
   client.setMethod("GET");
   client.setPayload("");
 // client.setQueryParams(resID, true);
  client.setSubContext("/passwords");

   client.setAllowAllHttpReturnCodes(true);	
   client.addHeader("Content-Type", MediaType.APPLICATION_JSON);
   client.addHeader("Accept", MediaType.APPLICATION_JSON);
   
	
   client.addHeader("Authorization","Basic " + mechIdPassword);
   DMEReplyHandler replyHandler = new DMEReplyHandler("MyService",true);
  client.setReplyHandler(replyHandler);
  
   client.send();
   
   
        String response = replyHandler.getResponse(90000);
        if (response == null) {
			System.out.println("DME2Client response is empty");
		}
		else {				
			System.out.println("DME2Client response"+response);
        }

		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
   }
}
