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

import java.io.InputStream;
import java.util.Map;

import com.att.aft.dme2.api.DME2SimpleReplyHandler;

public class DMEReplyHandler extends DME2SimpleReplyHandler{

	private Map<String, String> responseHeaders;
	private int responseCode;

	public DMEReplyHandler(String service, boolean allowAllHttpReturnCodes) {
		super(service, allowAllHttpReturnCodes);
		
	}
	@Override 
	public void handleException(Map<String, String> requestHeaders, Throwable e) { 
		super.handleException(requestHeaders, e); 
		this.echoedCharSet = requestHeaders.get("com.att.aft.dme2.test.charset"); 
 
	} 
 
	@Override 
	public void handleReply(int responseCode, String responseMessage, InputStream in, 
			Map<String, String> requestHeaders, Map<String, String> responseHeaders) { 
		this.echoedCharSet = responseHeaders.get("com.att.aft.dme2.test.charset"); 
		this.responseHeaders = responseHeaders; 
		this.responseCode = responseCode; 
		super.handleReply(responseCode, responseMessage, in, requestHeaders, responseHeaders); 
	} 
 
	public Map<String, String> getResponseHeaders() { 
		return responseHeaders; 
	} 
 
	public int getResponseCode() { 
		return this.responseCode; 
	} 
 
	public String echoedCharSet = null;
	
}
