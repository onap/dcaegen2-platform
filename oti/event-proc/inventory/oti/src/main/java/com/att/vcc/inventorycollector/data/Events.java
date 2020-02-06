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

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dcae_target_name", "dcae_target_type", "dcae_service_type", "dcae_service_action",
		"dcae_service_location", "dcae_target_prov-status", "dcae_target_in-maint",
		"dcae_target_is-closed-loop-disabled", "dcae_service-instance_model-invariant-id",
		"dcae_service-instance_model-version-id", "dcae_generic-vnf_model-invariant-id",
		"dcae_generic-vnf_model-version-id", "dcae_target_collection", "dcae_target_collection_ip",
		"dcae_snmp_community_string", "dcae_snmp_version", "dcae_target_cloud-region-id",
		"dcae_target_cloud-region-version", "dcae_target_service-description", "event", "aai_additional_info" })
public class Events {
	@JsonProperty("dcae_target_name")
	private String dcaeTargetName;
	@JsonProperty("dcae_target_type")
	private String dcaeTargetType;
	@JsonProperty("dcae_service_type")
	private String dcaeServiceType;
	@JsonProperty("dcae_service_action")
	private String dcaeServiceAction;
	@JsonProperty("dcae_service_location")
	private String dcaeServiceLocation;
	@JsonProperty("dcae_target_prov-status")
	private String dcaeTargetProvStatus;
	@JsonProperty("dcae_target_in-maint")
	private String dcaeTargetInMaint;
	@JsonProperty("dcae_target_is-closed-loop-disabled")
	private String dcaeTargetIsClosedLoopDisabled;
	@JsonProperty("dcae_service-instance_model-invariant-id")
	private String dcaeServiceInstanceModelInvariantId;
	@JsonProperty("dcae_service-instance_model-version-id")
	private String dcaeServiceInstanceModelVersionId;
	@JsonProperty("dcae_generic-vnf_model-invariant-id")
	private String dcaeGenericVnfModelInvariantId;
	@JsonProperty("dcae_generic-vnf_model-version-id")
	private String dcaeGenericVnfModelVersionId;
	@JsonProperty("dcae_target_collection")
	private String dcaeTargetCollection;
	@JsonProperty("dcae_target_collection_ip")
	private String dcaeTargetCollectionIp;
	@JsonProperty("dcae_snmp_community_string")
	private String dcaeSnmpCommunityString;
	@JsonProperty("dcae_snmp_version")
	private String dcaeSnmpVersion;
	@JsonProperty("dcae_target_cloud-region-id")
	private String dcaeTargetCloudRegionId;
	@JsonProperty("dcae_target_cloud-region-version")
	private String dcaeTargetCloudRegionVersion;
	@JsonProperty("dcae_target_service-description")
	private String dcaeTargetServiceDescription;
	@JsonRawValue
	@JsonSerialize
	@JsonProperty("event")
	private JSONObject event;
	@JsonRawValue
	@JsonSerialize
	@JsonProperty("aai_additional_info")
	private JSONObject aaiAdditionalInfo;

	public Events() {

	}

	public Events(String dcaeTargetName, String dcaeTargetType, String dcaeServiceAction, String dcaeServiceLocation,
			String dcaeServiceType, String dcaeTargetProvStatus, String dcaeTargetInMaint,
			String dcaeTargetIsClosedLoopDisabled, String dcaeServiceInstanceModelInvariantId,
			String dcaeServiceInstanceModelVersionId, String dcaeGenericVnfModelInvariantId,
			String dcaeGenericVnfModelVersionId, String dcaeTargetCollection, String dcaeTargetCollectionIp,
			String dcaeSnmpCommunityString, String dcaeSnmpVersion, String dcaeTargetCloudRegionId,
			String dcaeTargetCloudRegionVersion, String dcaeTargetServiceDescription, JSONObject event,
			JSONObject aaiAdditionalInfo) {
		super();
		this.dcaeTargetName = dcaeTargetName;
		this.dcaeTargetType = dcaeTargetType;
		this.dcaeServiceType = dcaeServiceType;
		this.dcaeServiceAction = dcaeServiceAction;
		this.dcaeServiceLocation = dcaeServiceLocation;
		this.dcaeTargetProvStatus = dcaeTargetProvStatus;
		this.dcaeTargetIsClosedLoopDisabled = dcaeTargetIsClosedLoopDisabled;
		this.dcaeTargetIsClosedLoopDisabled = dcaeTargetIsClosedLoopDisabled;
		this.dcaeServiceInstanceModelInvariantId = dcaeServiceInstanceModelInvariantId;
		this.dcaeServiceInstanceModelVersionId = dcaeServiceInstanceModelVersionId;
		this.dcaeGenericVnfModelInvariantId = dcaeGenericVnfModelInvariantId;
		this.dcaeGenericVnfModelVersionId = dcaeGenericVnfModelVersionId;
		this.dcaeTargetCollection = dcaeTargetCollection;
		this.dcaeTargetCollectionIp = dcaeTargetCollectionIp;
		this.dcaeSnmpCommunityString = dcaeSnmpCommunityString;
		this.dcaeSnmpVersion = dcaeSnmpVersion;
		this.dcaeTargetCloudRegionId = dcaeTargetCloudRegionId;
		this.dcaeTargetCloudRegionVersion = dcaeTargetCloudRegionVersion;
		this.dcaeTargetServiceDescription = dcaeTargetServiceDescription;
		this.event = event;
		this.aaiAdditionalInfo = aaiAdditionalInfo;
	}

	public String getDcaeTargetProvStatus() {
		return dcaeTargetProvStatus;
	}

	public void setDcaeTargetProvStatus(String dcaeTargetProvStatus) {
		this.dcaeTargetProvStatus = dcaeTargetProvStatus;
	}

	public String getDcaeTargetInMaint() {
		return dcaeTargetInMaint;
	}

	public void setDcaeTargetInMaint(String dcaeTargetInMaint) {
		this.dcaeTargetInMaint = dcaeTargetInMaint;
	}

	public String getDcaeTargetIsClosedLoopDisabled() {
		return dcaeTargetIsClosedLoopDisabled;
	}

	public void setDcaeTargetIsClosedLoopDisabled(String dcaeTargetIsClosedLoopDisabled) {
		this.dcaeTargetIsClosedLoopDisabled = dcaeTargetIsClosedLoopDisabled;
	}

	public String getDcaeTargetCollection() {
		return dcaeTargetCollection;
	}

	public String getDcaeTargetCollectionIp() {
		return dcaeTargetCollectionIp;
	}

	public void setDcaeTargetCollection(String dcaeTargetCollection) {
		this.dcaeTargetCollection = dcaeTargetCollection;
	}

	public void setDcaeTargetCollectionIp(String dcaeTargetCollectionIp) {
		this.dcaeTargetCollectionIp = dcaeTargetCollectionIp;
	}

	public String getDcaeTargetName() {
		return dcaeTargetName;
	}

	public void setDcaeTargetName(String dcaeTargetName) {
		this.dcaeTargetName = dcaeTargetName;
	}

	public String getDcaeTargetType() {
		return dcaeTargetType;
	}

	public void setDcaeTargetType(String dcaeTargetType) {
		this.dcaeTargetType = dcaeTargetType;
	}

	public String getDcaeServiceAction() {
		return dcaeServiceAction;
	}

	public void setDcaeServiceAction(String dcaeServiceAction) {
		this.dcaeServiceAction = dcaeServiceAction;
	}

	public String getDcaeServiceInstanceModelInvariantId() {
		return dcaeServiceInstanceModelInvariantId;
	}

	public void setDcaeServiceInstanceModelInvariantId(String dcaeServiceInstanceModelInvariantId) {
		this.dcaeServiceInstanceModelInvariantId = dcaeServiceInstanceModelInvariantId;
	}

	public String getDcaeServiceInstanceModelVersionId() {
		return dcaeServiceInstanceModelVersionId;
	}

	public void setDcaeServiceInstanceModelVersionId(String dcaeServiceInstanceModelVersionId) {
		this.dcaeServiceInstanceModelVersionId = dcaeServiceInstanceModelVersionId;
	}

	public String getDcaeGenericVnfModelInvariantId() {
		return dcaeGenericVnfModelInvariantId;
	}

	public void setDcaeGenericVnfModelInvariantId(String dcaeGenericVnfModelInvariantId) {
		this.dcaeGenericVnfModelInvariantId = dcaeGenericVnfModelInvariantId;
	}

	public String getDcaeGenericVnfModelVersionId() {
		return dcaeGenericVnfModelVersionId;
	}

	public void setDcaeGenericVnfModelVersionId(String dcaeGenericVnfModelVersionId) {
		this.dcaeGenericVnfModelVersionId = dcaeGenericVnfModelVersionId;
	}

	public String getDcaeServiceType() {
		return dcaeServiceType;
	}

	public void setDcaeServiceType(String dcaeServiceType) {
		this.dcaeServiceType = dcaeServiceType;
	}

	public String getDcaeServiceLocation() {
		return dcaeServiceLocation;
	}

	public void setDcaeServiceLocation(String dcaeServiceLocation) {
		this.dcaeServiceLocation = dcaeServiceLocation;
	}

	public String getDcaeSnmpCommunityString() {
		return dcaeSnmpCommunityString;
	}

	public void setDcaeSnmpCommunityString(String dcaeSnmpCommunityString) {
		this.dcaeSnmpCommunityString = dcaeSnmpCommunityString;
	}

	public String getDcaeSnmpVersion() {
		return dcaeSnmpVersion;
	}

	public void setDcaeSnmpVersion(String dcaeSnmpVersion) {
		this.dcaeSnmpVersion = dcaeSnmpVersion;
	}

	public String getDcaeTargetCloudRegionId() {
		return dcaeTargetCloudRegionId;
	}

	public void setDcaeTargetCloudRegionId(String dcaeTargetCloudRegionId) {
		this.dcaeTargetCloudRegionId = dcaeTargetCloudRegionId;
	}

	public String getDcaeTargetCloudRegionVersion() {
		return dcaeTargetCloudRegionVersion;
	}

	public void setDcaeTargetCloudRegionVersion(String dcaeTargetCloudRegionVersion) {
		this.dcaeTargetCloudRegionVersion = dcaeTargetCloudRegionVersion;
	}

	public String getDcaeTargetServiceDescription() {
		return dcaeTargetServiceDescription;
	}

	public void setDcaeTargetServiceDescription(String dcaeTargetServiceDescription) {
		this.dcaeTargetServiceDescription = dcaeTargetServiceDescription;
	}

	public JSONObject getEvent() {
		return event;
	}

	public void setEvent(JSONObject event) {
		this.event = event;
	}

	public JSONObject getAaiAdditionalInfo() {
		return aaiAdditionalInfo;
	}

	public void setAaiAdditionalInfo(JSONObject aaiAdditionalInfo) {
		this.aaiAdditionalInfo = aaiAdditionalInfo;
	}

}
