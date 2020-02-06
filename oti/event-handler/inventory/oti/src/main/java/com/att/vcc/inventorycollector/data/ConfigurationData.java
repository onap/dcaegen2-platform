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

import org.apache.commons.lang.StringUtils;

public class ConfigurationData {

	private String deviceName;
	private String targetType;
	private String ipAddress;
	private String zoneName;
	private String complexName;
	private String cityState;
	private String action;
	private String nodeType;
	private String functionId;
	private String networkId;
	private String isInMaint;
	private String locationId;
	private String neVersion;
	private String provStatus;
	private String devicechangetimestamp;
	private String communityString;
	private String snmpversion;
	private String designtype;
	private String outputobject;
	private String icmpintervalclass;
	private String fmmibpollerintervalclass;
	private String changetype;
	private String model;
	private String sshFlag;
	private String collectionNodeName;
	private String sequence;
	
	// Open DCAE - Required by Orchestrator
	private String vnfType = "";
	private String serviceInstanceModelInvariantId = "";
	private String serviceInstanceModelVersionId = "";
	private String generciVnfModelInvariantId = "";
	private String generciVnfModelVersionId = "";
	private String isClosedLoopDisabled = "";
	private String isDeviceforCollection = "true";
	private String targetCollectionIp = "";
	private String snmpCommunityString = "";
	private String snmpVersion = "";
	private String cloudRegionId = "";
	private String cloudRegionVersion = "";
	private String serviceDescription = "";

	public ConfigurationData() {
		// Empty constructor
	}

	public ConfigurationData(String deviceName, String targetType, String ipAddress, String zoneName,
			String complexName, String cityState, String action, String nodeType, String functionId, String networkId,
			String isInMaint, String locationId, String vnfType, String neVersion,
			String serviceInstanceModelInvariantId, String serviceInstanceModelVersionId,
			String generciVnfModelInvariantId, String generciVnfModelVersionId, String provStatus,
			String targetCollectionIp, String snmpCommunityString, String snmpVersion, String cloudRegionId,
			String cloudRegionVersion, String serviceDescription) {
		super();
		this.deviceName = deviceName;
		this.targetType = targetType;
		this.ipAddress = ipAddress;
		this.zoneName = zoneName;
		this.complexName = complexName;
		this.cityState = cityState;
		this.action = action;
		this.nodeType = nodeType;
		this.functionId = functionId;
		this.networkId = networkId;
		this.isInMaint = isInMaint;
		this.locationId = locationId;
		this.vnfType = vnfType;
		this.neVersion = neVersion;
		this.provStatus = provStatus;
		this.targetCollectionIp = targetCollectionIp;
		this.serviceInstanceModelInvariantId = serviceInstanceModelInvariantId;
		this.serviceInstanceModelVersionId = serviceInstanceModelVersionId;
		this.generciVnfModelInvariantId = generciVnfModelInvariantId;
		this.generciVnfModelVersionId = generciVnfModelVersionId;
		this.snmpCommunityString = snmpCommunityString;
		this.snmpVersion = snmpVersion;
		this.cloudRegionId = cloudRegionId;
		this.cloudRegionVersion = cloudRegionVersion;
		this.serviceDescription = serviceDescription;
	}

	public String getIsInMaint() {
		return isInMaint;
	}

	public void setIsInMaint(String isInMaint) {
		this.isInMaint = (isInMaint != null) ? isInMaint.trim() : "";
	}

	public String getNeVersion() {
		return neVersion;
	}

	public void setNeVersion(String neVersion) {
		this.neVersion = (neVersion != null) ? neVersion : "";
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = (locationId != null) ? locationId.trim() : "";
	}

	public String getNetworkId() {
		return (networkId == null ? "" : networkId);
	}

	public void setNetworkId(String networkId) {
		this.networkId = (networkId != null) ? networkId.trim() : "";
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = (ipAddress != null) ? ipAddress.trim() : "";
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName, String suffix) {
		if (StringUtils.isNotBlank(zoneName) && !zoneName.equalsIgnoreCase("null")) {
			String[] tokens = zoneName.split("-");
			if (tokens != null && tokens.length > 0)
				this.zoneName = tokens[0] + suffix;
		}
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public String getComplexName() {
		return complexName;
	}

	public void setComplexName(String complexName) {
		this.complexName = (complexName != null) ? complexName : "";
	}

	public String getCityState() {
		return cityState;
	}

	public void setCityState(String cityState) {
		this.cityState = (cityState != null) ? cityState : "";
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = (deviceName != null) ? deviceName : "";
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = (targetType != null) ? targetType : "";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = (action != null) ? action : "";
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = (nodeType != null) ? nodeType : "";
	}

	public String getFunctionId() {
		return (functionId == null ? "" : functionId);
	}

	public void setFunctionId(String functionId) {
		this.functionId = (functionId != null) ? functionId : "";
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = (vnfType != null) ? vnfType : "";
	}

	public String getProvStatus() {
		return provStatus;
	}

	public void setProvStatus(String provStatus) {
		this.provStatus = (provStatus != null) ? provStatus : "";
	}

	public String getTargetCollectionIp() {
		return targetCollectionIp;
	}

	public void setTargetCollectionIp(String targetCollectionIp) {
		this.targetCollectionIp = (targetCollectionIp != null) ? targetCollectionIp : "";
	}

	public String getSnmpCommunityString() {
		return snmpCommunityString;
	}

	public void setSnmpCommunityString(String snmpCommunityString) {
		this.snmpCommunityString = (snmpCommunityString != null) ? snmpCommunityString : "";
	}

	public String getSnmpVersion() {
		return snmpVersion;
	}

	public void setSnmpVersion(String snmpVersion) {
		this.snmpversion = (snmpVersion != null) ? snmpVersion : "";
	}

	public String getCloudRegionId() {
		return cloudRegionId;
	}

	public void setCloudRegionId(String cloudRegionId) {
		this.cloudRegionId = (cloudRegionId != null) ? cloudRegionId : "";
	}

	public String getCloudRegionVersion() {
		return cloudRegionVersion;
	}

	public void setCloudRegionVersion(String cloudRegionVersion) {
		this.cloudRegionVersion = (cloudRegionVersion != null) ? cloudRegionVersion : "";
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = (serviceDescription != null) ? serviceDescription : "";
	}

	public String getServiceInstanceModelInvariantId() {
		return serviceInstanceModelInvariantId;
	}

	public void setServiceInstanceModelInvariantId(String serviceInstanceModelInvariantId) {
		this.serviceInstanceModelInvariantId = serviceInstanceModelInvariantId;
	}

	public String getServiceInstanceModelVersionId() {
		return serviceInstanceModelVersionId;
	}

	public void setServiceInstanceModelVersionId(String serviceInstanceModelVersionId) {
		this.serviceInstanceModelVersionId = serviceInstanceModelVersionId;
	}

	public String getGenerciVnfModelInvariantId() {
		return generciVnfModelInvariantId;
	}

	public void setGenerciVnfModelInvariantId(String generciVnfModelInvariantId) {
		this.generciVnfModelInvariantId = generciVnfModelInvariantId;
	}

	public String getGenerciVnfModelVersionId() {
		return generciVnfModelVersionId;
	}

	public void setGenerciVnfModelVersionId(String generciVnfModelVersionId) {
		this.generciVnfModelVersionId = generciVnfModelVersionId;
	}

	public String getIsClosedLoopDisabled() {
		return isClosedLoopDisabled;
	}

	public void setIsClosedLoopDisabled(String isClosedLoopDisabled) {
		this.isClosedLoopDisabled = isClosedLoopDisabled;
	}

	public String getIsDeviceforCollection() {
		return isDeviceforCollection;
	}

	public void setIsDeviceforCollection(String isDeviceforCollection) {
		this.isDeviceforCollection = isDeviceforCollection;
	}

	public String getConfigurationData() {
		return this.deviceName + "#" + this.ipAddress + "#" + this.zoneName + "#" + this.complexName + "#"
				+ this.cityState + "#" + this.action + "#" + this.nodeType + "#" + this.functionId + "#"
				+ this.networkId + "#" + this.isInMaint + "#" + this.locationId + "#" + this.vnfType + "#"
				+ this.neVersion + "#" + this.serviceInstanceModelInvariantId + "#" + this.serviceInstanceModelVersionId
				+ "#" + this.generciVnfModelInvariantId + "#" + this.generciVnfModelVersionId + "#" + this.provStatus
				+ "#" + this.isClosedLoopDisabled + "#" + this.isDeviceforCollection + "#" + this.targetCollectionIp;
	}

	public String getDevicechangetimestamp() {
		return devicechangetimestamp;
	}

	public void setDevicechangetimestamp(String devicechangetimestamp) {
		this.devicechangetimestamp = devicechangetimestamp;
	}

	public String getCommunityString() {
		return communityString;
	}

	public void setCommunityString(String communityString) {
		this.communityString = communityString;
	}

	public String getDesigntype() {
		return designtype;
	}

	public void setDesigntype(String designtype) {
		this.designtype = designtype;
	}

	public String getOutputobject() {
		return outputobject;
	}

	public void setOutputobject(String outputobject) {
		this.outputobject = outputobject;
	}

	public String getIcmpintervalclass() {
		return icmpintervalclass;
	}

	public void setIcmpintervalclass(String icmpintervalclass) {
		this.icmpintervalclass = icmpintervalclass;
	}

	public String getFmmibpollerintervalclass() {
		return fmmibpollerintervalclass;
	}

	public void setFmmibpollerintervalclass(String fmmibpollerintervalclass) {
		this.fmmibpollerintervalclass = fmmibpollerintervalclass;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSSHFlag() {
		return sshFlag;
	}

	public void setSSHFlag(String sshFlag) {
		this.sshFlag = sshFlag;
	}

	public String getChangetype() {
		return changetype;
	}

	public void setChangetype(String changetype) {
		this.changetype = changetype;
	}

	public String getCollectionNodeName() {
		return collectionNodeName;
	}

	public void setCollectionNodeName(String collectionNodeName) {
		this.collectionNodeName = collectionNodeName;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
}
