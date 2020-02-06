#!/usr/bin/python
# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

import os
import os.path
import json
from pprint import pprint

# regex conversion for pattern
name_pattern_dict = {}
name_pattern_dict['PTNII-US: <ccccc><nnn><vvv>'] = '[A-Za-z0-9]{5}\\\\d{3}'
name_pattern_dict['PTNII-US-NC: <ccccc><nnnn><vvv>'] = '[A-Za-z0-9]{5}\\\\d{4}'
name_pattern_dict['PTNII-MOW-NC: <ccccccc><nnnn><vvv>'] = '[A-Za-z0-9]{7}\\\\d{4}'
name_pattern_dict['PTNII-MOW: <ccccccc><nnn><vvv>'] = '[A-Za-z0-9]{7}\\\\d{3}'
name_pattern_dict['PTNII-US-NC-P1: <ccccc><nn><vvv>'] = '[A-Za-z0-9]{5}\\\\d{2}'

# Convert the target type pattern according to our needs.
target_type_pattern_dict = {}
target_type_pattern_dict['vvvv-ppp-5: <functionCode>-<collectorType>'] = '<functionCode>-<collectorType>'
target_type_pattern_dict['vvvv-ppp-6: <collectorType>-<collectorGroupType>'] = '<collectorType>-<collectorGroupType>'

# covert dcae* naming convention to it's java equivalent
dcae_event_policy_dict = {}
dcae_event_policy_dict['dcae_target_name'] = 'dcaeTargetName'
dcae_event_policy_dict['dcae_target_collection_ip'] = 'dcaeTargetCollectionIp'
dcae_event_policy_dict['dcae_service_location'] = 'dcaeServiceLocation'
dcae_event_policy_dict['dcae_snmp_community_string'] = 'dcaeSnmpCommunityString'
dcae_event_policy_dict['dcae_snmp_version'] = 'dcaeSnmpVersion'
dcae_event_policy_dict['dcae_service_type'] = 'dcaeServiceType'
dcae_event_policy_dict['<zone.design_type>'] = 'related-to:zone,zone.design-type'

#config_policy_path = os.environ['VCC_HOME'] + '/config/policy/'
config_policy_path = '/opt/app/vcc/config/policy/'

#filepath = os.environ['VCC_HOME'] + '/tmp/configManager/output/POLICY_Config.json'
filepath = '/opt/app/vcc/tmp/configManager/output/POLICY_Config.json'


if os.path.isfile(filepath):
    with open(filepath) as f:
        data = json.load(f)
        for item in data["items"]:
            collector_type = item["config"]["content"]["dtiEventProcPolicy"][0]["collectorType"]
            print("collector_type: {}".format(collector_type))
            function_code = item["config"]["content"]["dtiEventProcPolicy"][0]["functionCode"]
            entity_type = item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiObjectName"]
            event_type = item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiSource"]
            name_pattern = item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiObjectInstanceNamePatterns"]
            collector_group_type = item["config"]["content"]["dtiEventProcPolicy"][0]["collectorGroupType"]
            name_pattern_type = name_pattern.split(":", 2)

            # Create policy properties file
            if collector_group_type != "na":
                print "inside group_type"
                policy_file_name = entity_type + '_' + collector_type + '_' + function_code.replace(',','_') + '_' + collector_group_type + '_' + name_pattern_type[0] + '.properties'
            else:
                policy_file_name = entity_type + '_' + collector_type + '_' + function_code.replace(',','_') + '_' + name_pattern_type[0] + '.properties'
            policy_file_name_full = config_policy_path+policy_file_name
            print("policy_file_name_full: {}".format(policy_file_name_full))
            file = open(policy_file_name_full, "w")
            file.write("event-type=" + event_type + "\n")
            file.write("functionCode=" + function_code + "\n")
            file.write("collectorType=" + collector_type + "\n")
            file.write("collectorGroupType=" + collector_group_type + "\n")

            # Logic to add the functionCode to regex pattern.
            add_name_pattern = ''
            function_code_len = len(function_code)
            if collector_group_type == "na":
                if function_code_len > 0:
                    for x in range(0, function_code_len):
                       if function_code[x] == ",":
                    	   add_name_pattern +="|"
                       else:
                           if function_code[x].isalpha():
                               add_name_pattern += '['+function_code[x]+function_code[x].upper()+']'                            
                           else:
                               add_name_pattern += function_code[x]
            else:
                add_name_pattern += '[A-Za-z0-9]{3}'
            file.write("namePattern=" + name_pattern_dict[name_pattern] + "(" +add_name_pattern +")"+ "\n")

            target_type_pattern = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetTypePattern"]
            if target_type_pattern == "na":
                dcae_target_type = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetType"]
                if dcae_target_type != "na":
                    print("dcae_target_type: {}".format(dcae_target_type))                        
            	    file.write("dcaeTargetType=" + dcae_target_type + "\n")
            else:
            	file.write("dcaeTargetType=" + target_type_pattern_dict[target_type_pattern] + "\n")
            	
            dcae_service_location = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeServiceLocationSrc"]
            file.write("dcaeServiceLocation=" + dcae_service_location + "\n")
            file.write("dcaeServiceAction=<event-header.action>|mapRuleAction\n")
            vnf_mod_inv_id = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeGenericVnfModInvariantIdSrc"]
            if (vnf_mod_inv_id.lower() != "na"):
                file.write("dcaeGenericVnfModelInvariantId="+vnf_mod_inv_id+"\n")
            else:
                file.write("dcaeGenericVnfModelInvariantId=\n")
            vnf_mod_ver_id = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeServiceInstModVersionIdSrc"]
            if (vnf_mod_ver_id.lower() != "na"):
                file.write("dcaeGenericVnfModelVersionId="+vnf_mod_ver_id+"\n")
            else:
                file.write("dcaeGenericVnfModelVersionId=\n")
            serv_inst_mod_inv_id = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeServiceInstModInvarIdSrc"]
            if (serv_inst_mod_inv_id.lower() != "na"):
                file.write("dcaeServiceInstanceModelInvariantId="+serv_inst_mod_inv_id+"\n")
            else:
                file.write("dcaeServiceInstanceModelInvariantId=\n")
            serv_inst_mod_ver_id = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeServiceInstModVersionIdSrc"]
            if (serv_inst_mod_ver_id.lower() != "na"):
                file.write("dcaeServiceInstanceModelVersionId="+serv_inst_mod_ver_id+"\n")
            else:
                file.write("dcaeServiceInstanceModelVersionId=\n")
            file.write("dcaeServiceType=" + item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeServiceTypeSrc"]+"\n")
            file.write("dcaeSnmpCommunityString=" + item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeSnmpCommunityStringSrc"]+"\n")
            if 'dcaeSnmpVersionSrc' in item["config"]["content"]["dtiEventProcPolicy"][0]:
                file.write("dcaeSnmpVersion=" + item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeSnmpVersionSrc"]+"\n")
            cloud_region_id = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetCloudRegionIdSrc"]
            if (cloud_region_id.lower() != "na"):
                file.write("dcaeTargetCloudRegionId="+cloud_region_id+"\n")
            else:
                file.write("dcaeTargetCloudRegionId=\n")
            cloud_region_version = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetCloudRegionVersionSrc"]
            if (cloud_region_version.lower() != "na"):
                file.write("dcaeTargetCloudRegionVersion="+cloud_region_version+"\n")
            else:
                file.write("dcaeTargetCloudRegionVersion=\n")
            dcae_target_collection = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetCollection"]
            if (dcae_target_collection.lower() != "na"):
                file.write("dcaeTargetCollection="+dcae_target_collection+"\n")
            else:
                file.write("dcaeTargetCollection=true\n")

            file.write("dcaeTargetIsClosedLoopDisabled=\n")
            service_description = item["config"]["content"]["dtiEventProcPolicy"][0]["dcaeTargetServiceDescriptionSrc"]
            if (service_description.lower() != "na"):
                file.write("dcaeTargetServiceDescription="+service_description+"\n")
            else:
                file.write("dcaeTargetServiceDescription=\n")


            # Populate AAI Additional Info for PM & FM collectors
            if "aaiAdditionalInfoSrcList" in item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]:
                len_aai_add_info = len(item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"])
                for y in range(0, len_aai_add_info):
                    replace_key = ''
                    replace_policy=item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"].replace('<policy.','<')
                    if (replace_policy != item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"]):
                        item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"] = replace_policy
                    for dcae_params in dcae_event_policy_dict.keys():
                        if dcae_params in item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"]:
                            replace_key = dcae_params
                            break
                    if len(replace_key) > 0 :
                        file.write("aaiAdditionalInfo-" + item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"].replace(":","=").replace(replace_key, dcae_event_policy_dict[replace_key])+"\n")
                    else:
                        file.write("aaiAdditionalInfo-" + item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiAdditionalInfoSrcList"][y]["aaiAdditionalInfoSrc"].replace(":","=")+"\n")


            file.write("mapRuleAction_CREATE=add\n")
            file.write("mapRuleAction_UPDATE=update\n")
            file.write("mapRuleAction_DELETE=delete\n")
            len_map_rule_list = len(item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiObjectsMapRuleList"])
            for i in range(0, len_map_rule_list):
                file.write("mapRule_" + item["config"]["content"]["dtiEventProcPolicy"][0]["aaiObjectsList"][0]["aaiObjectsMapRuleList"][i]["aaiObjectsMapRule"].replace(":", "=")+"\n")
            file.close()
else:
    print "Error: problem with opening the file for reading: " + filepath
