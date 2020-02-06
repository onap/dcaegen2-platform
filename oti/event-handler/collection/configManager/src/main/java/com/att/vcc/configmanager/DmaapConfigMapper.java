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

package com.att.vcc.configmanager;

import org.json.JSONObject;

public class DmaapConfigMapper {

	public JSONObject makeDmaapConf(JSONObject inputJsonObj) {
	
		JSONObject outputJsonObj = new JSONObject();

		JSONObject streamPublishObj = inputJsonObj
				.getJSONObject("streams_publishes");

		if (streamPublishObj != null && streamPublishObj.keys() != null
				&& streamPublishObj.keys().hasNext()) {
			String key = streamPublishObj.keys().next();
			if (key != null && !key.isEmpty()) {
				JSONObject streamItem = streamPublishObj.getJSONObject(key);
				if (streamItem != null) {

					JSONObject dmaapInputObj = streamItem
							.getJSONObject("dmaap_info");
					if (dmaapInputObj != null) {
						outputJsonObj
								.put("$class",
										"org.openecomp.dcae.controller.core.stream.DmaapStream");
						outputJsonObj.put("dmaapDataType", "file");
						outputJsonObj.put("dmaapAction", "publish");
						outputJsonObj.put("dmaapUrl",
								dmaapInputObj.get("publish_url"));
						outputJsonObj.put("dmaapUserName",
								dmaapInputObj.get("username"));
						outputJsonObj.put("dmaapPassword",
								dmaapInputObj.get("password"));
						outputJsonObj.put("dmaapAuthMethod", "password");
						outputJsonObj.put("dmaapStreamId", key);

					}

				}
			}
		}

		return outputJsonObj;
	}

}
