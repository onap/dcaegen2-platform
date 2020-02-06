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

package com.att.vcc.inventorycollector.data;

public class EntityData {
	
	private String entityType;
	private String entityLink;
	private String jsonMessage;
	private String action;
	private String name;
	
	
	public EntityData()
	{
		// Empty constructor
	}
		
	public EntityData(String entityLink, String entityType, String action, String name, String jsonMessage) {
		super();
		this.entityLink = entityLink;
		this.entityType = entityType;
		this.action = action;
		this.name = name;
		this.jsonMessage = jsonMessage;
	}

	
	public String getJsonMessage() {
		return jsonMessage;
	}
	
	public void setJsonMessage(String jsonMessage) {
		this.jsonMessage = jsonMessage;
	}
	
	public String getEntityLink() {
		return entityLink;
	}
	
	public void setIpAddress(String entityLink) {
		this.entityLink = entityLink;
	}
	
	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String getName() {
		return name;
	}
	
	public void setvnfName(String name) {
		this.name = name;
	}

		
}
