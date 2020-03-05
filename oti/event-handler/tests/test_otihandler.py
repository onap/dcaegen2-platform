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
#

"""test otihandler package of DCAE-Controller"""

import base64
import copy
import json
import logging
import os
import re
import sys
import time
import uuid
# from urlparse import urlparse, parse_qsl
from datetime import datetime

import pytest
import cherrypy
from cherrypy.test import helper

from otihandler.config import Config
from otihandler.consul_client import (ConsulClient,
     ConsulClientConnectionError, ConsulClientServiceNotFoundError,
     ConsulClientKVEntryNotFoundError)
from otihandler.onap.audit import (Audit, AuditHttpCode)
from otihandler.__main__ import LogWriter
from otihandler.web_server import _DTIWeb

OTIHANDLER_VERSION = "1.0.0"

false = False
true = True
null = None

class Settings(object):
    """init all locals"""

    logger = None
    RUN_TS = datetime.utcnow().isoformat()[:-3] + 'Z'
    discovered_config = None

    @staticmethod
    def init():
        """init locals"""

        os.environ["CLOUDIFY"] = '{"cloudify":{"protocol":"https","user":"XXXXX","password":"XXXX","address":"cloudify.bogus.com","port":"443"}}'
        os.environ["CONSUL_URL"] = "http://consul:8500"
        os.environ["OTI_HANDLER_URL"] = "https://oti_handler:8443"

        Config.load_from_file()

        with open("etc/config.json", 'r') as config_json:
            Settings.discovered_config = json.load(config_json)

        Config.load_from_file("etc/config.json")

        Settings.logger = logging.getLogger("otihandler.unit_test")
        sys.stdout = LogWriter(Settings.logger.info)
        sys.stderr = LogWriter(Settings.logger.error)

        print("print ========== run_otihandler ==========")
        Settings.logger.info("========== run_otihandler ==========")
        Audit.init(Config.get_system_name(), OTIHANDLER_VERSION, Config.LOGGER_CONFIG_FILE_PATH)

        Settings.logger.info("starting otihandler with config:")
        Settings.logger.info(Audit.log_json_dumps(Config.config))

Settings.init()


class MonkeyHttpResponse(object):
    """Monkey http response"""

    def __init__(self, headers):
        """init locals"""

        self.headers = headers or {}

class MonkeyRequestsResponse(object):
    """Monkey response"""

    def __init__(self, full_path, res_json, json_body=None, headers=None, status_code=200):
        """init locals"""

        self.full_path = full_path
        self.req_json = json_body or {}
        self.status_code = status_code
        self.request = MonkeyHttpResponse(headers)
        self.res = res_json
        self.text = json.dumps(self.res)

    def json(self):
        """returns json of response"""

        return self.res

    def raise_for_status(self):
        """ignoring"""

        if self.status_code == 200:
            return
        else:
            Settings.logger.warning("raise_for_status found status_code: {}".format(self.status_code))

def kv_encode(key, value):
    """helper function to encode a consul key value"""

    rtn = {
        "LockIndex": 0,
        "Key": key,
        "Flags": 0,
        "Value": base64.b64encode(bytes(value, "utf-8")).decode("utf-8"),
        "CreateIndex": 19,
        "ModifyIndex": 99999
    }
    return rtn

def monkey_consul_client_get(full_path, **kwargs):
    """monkeypatch for GET from consul"""

    rv = None
    res_json = {}
    if full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "cloudify_manager"):
        res_json = [{
            "ID": "048ec7c9-aa2e-bfad-34c7-6755e0007c9c",
            "Node": "zldcdyh1adcc1orcl00.novalocal",
            "Address": "192.168.1.13",
            "Datacenter": "zldcdyh1adcc1",
            "TaggedAddresses": {
                "lan": "192.168.1.13",
                "wan": "192.168.1.13"
            },
            "NodeMeta": {},
            "ServiceID": "cloudify_manager",
            "ServiceName": "cloudify_manager",
            "ServiceTags": [],
            "ServiceAddress": "1.1.1.1",
            "ServicePort": 80,
            "ServiceEnableTagOverride": false,
            "CreateIndex": 1569262,
            "ModifyIndex": 1569262
        }]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_handler"):
        res_json = [{
            "ID": "476991b8-7f40-e3c2-9d5e-f936c2aeaf56",
            "Node": "zldcdyh1adcc1dokr00",
            "Address": "32.68.15.149",
            "Datacenter": "zldcdyh1adcc1",
            "TaggedAddresses": {
                "lan": "32.68.15.149",
                "wan": "32.68.15.149"
            },
            "NodeMeta": {
                "fqdn": "oti_handler"
            },
            "ServiceID": "58a417002f89:oti_handler:8443",
            "ServiceName": "oti_handler",
            "ServiceTags": [
                "oti_handler",
                "oti_handler"
            ],
            "ServiceAddress": "1.1.1.2",
            "ServicePort": 8443,
            "ServiceEnableTagOverride": false,
            "CreateIndex": 1161355,
            "ModifyIndex": 1161355
        }]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "deployment_handler"):
        res_json = [{
            "ID": "476991b8-7f40-e3c2-9d5e-f936c2aeaf56",
            "Node": "zldcdyh1adcc1dokr00",
            "Address": "32.68.15.149",
            "Datacenter": "zldcdyh1adcc1",
            "TaggedAddresses": {
                "lan": "32.68.15.149",
                "wan": "32.68.15.149"
            },
            "NodeMeta": {
                "fqdn": "deployment_handler:8188"
            },
            "ServiceID": "58a417002f89:deployment_handler:8188",
            "ServiceName": "deployment_handler",
            "ServiceTags": [
                "deployment_handler",
                "deployment_handler"
            ],
            "ServiceAddress": "1.1.1.2",
            "ServicePort": 8188,
            "ServiceEnableTagOverride": false,
            "CreateIndex": 1502800,
            "ModifyIndex": 1502800
        }]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "zldcdyh1adcc1-component-dockerhost-1"):
        res_json = [{
            "ID": "4ffed53d-7601-7d47-df93-c091ea66fb45",
            "Node": "zldcdyh1adcc1dokr02",
            "Address": "32.68.15.163",
            "Datacenter": "zldcdyh1adcc1",
            "TaggedAddresses": {
                "lan": "32.68.15.163",
                "wan": "32.68.15.163"
            },
            "NodeMeta": {
                "fqdn": "zldcdyh1adcc1dokr02.bogus.com"
            },
            "ServiceID": "zldcdyh1adcc1-component-dockerhost-1",
            "ServiceName": "zldcdyh1adcc1-component-dockerhost-1",
            "ServiceTags": [
                "LSLEILAA",
                "MDTWNJC1"
            ],
            "ServiceAddress": "1.1.1.5",
            "ServicePort": 2376,
            "ServiceEnableTagOverride": false,
            "CreateIndex": 1704211,
            "ModifyIndex": 1704211
        }]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), Config.get_system_name()):
        res_json = copy.deepcopy(Settings.discovered_config)
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "cloudify_manager"):
        res_json = [kv_encode("cloudify_manager", json.dumps(
            {"cloudify":{"protocol" : "http", "user": "admin", "password":"XXXX"}}
        ))]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_2:oti"):
        res_json = [
            kv_encode("SCN_2:oti", json.dumps(
                {
                    "anot-her": {
                        "another01ems003": {"dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_target_name": "another01ems003", "dcae_target_collection": "true", "event": {}, "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003"}]}}, "protocol": "sftp", "collectionInterval": "300"}}}, "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_prov-status": "PROV"}
                    }
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "docker_plugin/docker_logins"):
        res_json = [
            kv_encode("docker_plugin/docker_logins", json.dumps(
                [{"username": "fake_user", "password": "fake_password",
                  "registry": "registry.bogus.com:5100" }]
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/") \
      or full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/anot-her/"):
        res_json = [
            kv_encode("oti_events/anot-her/another01ems003", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems003",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            )),
            kv_encode("oti_events/anot-her/another01ems042", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.42",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems042",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            )),
            kv_encode("oti_events/anot-her/another01ems044", json.dumps(
                {"dcae_service_location": "MDTWNJC1",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.42",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems044",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/birth-day/"):
        res_json = [
            kv_encode("oti_events/birth-day/birthdy01ems055", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "birth-day",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "birthdy01ems055",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"birthdy01ems055_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "birthdy01ems055"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/new-type/"):
        res_json = [
            kv_encode("oti_events/new-type/newtype01ems084", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "new-type",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "newtype01ems084",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"newtype01ems084_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "newtype01ems084"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/pcrf-oam/"):
        res_json = [
            kv_encode("oti_events/pcrf-oam/pcrfoam01ems009", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "pcrf-oam",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "pcrfoam01ems009",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"pcrfoam01ems009_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "pcrfoam01ems009"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/pnga-xxx/"):
        res_json = [
            kv_encode("oti_events/pnga-xxx/pngaxxx01ems007", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "pnga-xxx",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "pngaxxx01ems007",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"pngaxxx01ems007_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "pngaxxx01ems007"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/vhss-ems/"):
        res_json = [
            kv_encode("oti_events/vhss-ems/vhssems01ems019", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "vhss-ems",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "vhssems01ems019",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"vhssems01ems019_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "vhssems01ems019"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/anot-her/another01ems003"):
        res_json = [
            kv_encode("oti_events/anot-her/another01ems003", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.3",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems003",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/anot-her/another01ems042"):
        res_json = [
            kv_encode("oti_events/anot-her/another01ems042", json.dumps(
                {"dcae_service_location": "LSLEILAA",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.42",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems042",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "oti_events/anot-her/another01ems044"):
        res_json = [
            kv_encode("oti_events/anot-her/another01ems044", json.dumps(
                {"dcae_service_location": "MDTWNJC1",
                 "dcae_target_type": "ANOT-her",
                 "dcae_service_action": "deploy",
                 "dcae_service-instance_model-version-id": "1",
                 "dcae_target_collection_ip": "107.239.85.42",
                 "dcae_target_is-closed-loop-disabled": "false",
                 "dcae_target_in-maint": "false",
                 "dcae_target_name": "another01ems044",
                 "dcae_target_collection": "true",
                 "event": {},
                 "dcae_snmp_version": "2c",
                 "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"serviceType": "MOB-VHSS", "nodeType": "VHSS", "description": "VHSS Data Collection", "priority": 1, "nodeSubtype": "", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroupId": "g1", "serverGroup": [{"isPrimary": "true", "remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044"}]}}, "protocol": "sftp", "collectionInterval": "300"}}},
                 "dcae_snmp_community_string": "my_first_community",
                 "dcae_generic-vnf_model-version-id": "1",
                 "dcae_target_prov-status": "PROV"
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1"):
        res_json = [
            kv_encode("SCN_1", json.dumps(
                {"dcae_target_type": ["pnga-xxx"]}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:oti"):
        res_json = [
            kv_encode("SCN_1:oti", json.dumps(
                {"new-type": {}, "pnga-xxx": {}, "birth-day": {}, "anot-her": {}, "vhss-ems": {}, "pcrf-oam": {}}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:dmaap"):
        res_json = [
            kv_encode("SCN_1:dmaap", json.dumps(
                {"topic0": {
                   "topic_url": "https://dcae-mrtr.bogus.com:3005/events/com.bogus.HelloWorld-PubTopic",
                   "client_role": "com.bogus.member",
                   "location": "loc-1",
                   "client_id": "1575649224792"
                 },
                 "topic1": {
                   "topic_url": "https://dcae-mrtr.bogus.com:3005/events/com.bogus.HelloWorld-PubTopic",
                   "client_role": "com.bogus.member",
                   "location": "loc-1",
                   "client_id": "1575649221094"
                 }
                }
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:rel"):
        res_json = [
            kv_encode("SCN_1:rel", json.dumps(
                {"who-knows", "what this content might look like?"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1"):
        res_json = [
            kv_encode("SCN_1", json.dumps(
                {"dcae_target_type": ["pnga-xxx"]}
            )),
            kv_encode("SCN_1:oti", json.dumps(
                {"new-type": {}, "pnga-xxx": {}, "birth-day": {}, "anot-her": {}, "vhss-ems": {}, "pcrf-oam": {}}
            )),
            kv_encode("SCN_1:policies/event", json.dumps(
                {"action": "updated", "timestamp": "2018-07-16T15:11:44.845Z", "update_id": "e6102aab-3079-435a-ae0d-0397a2cb3c4d", "policies_count": 3}
            )),
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Collectors", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Collectors"}, "type": "JSON", "property": null, "config": {"power_source": "lemmings", "conflicting_key": "green_collectors_wins", "package_type": "plastic", "polling_frequency": "30m"}, "policyVersion": "1"}
            )),
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific"}, "type": "JSON", "property": null, "config": {"conflicting_key": "green_eggs_and_ham_are_better", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "bacon": "soft, not crispy", "preparation": "scrambled", "egg_color": "green", "bread": "pumpernickel"}, "policyVersion": "5"}
            )),
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_In_Service", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_In_Service.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "In_Service"}, "type": "JSON", "property": null, "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "policyVersion": "1"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:policies/items/"):
        res_json = [
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Collectors", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Collectors"}, "type": "JSON", "property": null, "config": {"power_source": "lemmings", "conflicting_key": "green_collectors_wins", "package_type": "plastic", "polling_frequency": "30m"}, "policyVersion": "1"}
            )),
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific"}, "type": "JSON", "property": null, "config": {"conflicting_key": "green_eggs_and_ham_are_better", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "bacon": "soft, not crispy", "preparation": "scrambled", "egg_color": "green", "bread": "pumpernickel"}, "policyVersion": "5"}
            )),
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_In_Service", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_In_Service.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "In_Service"}, "type": "JSON", "property": null, "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "policyVersion": "1"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:policies/items/DCAE_FTL3B.Config_Green_Collectors"):
        res_json = [
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Collectors", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Collectors"}, "type": "JSON", "property": null, "config": {"power_source": "lemmings", "conflicting_key": "green_collectors_wins", "package_type": "plastic", "polling_frequency": "30m"}, "policyVersion": "1"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:policies/items/DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific"):
        res_json = [
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific"}, "type": "JSON", "property": null, "config": {"conflicting_key": "green_eggs_and_ham_are_better", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "bacon": "soft, not crispy", "preparation": "scrambled", "egg_color": "green", "bread": "pumpernickel"}, "policyVersion": "5"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1:policies/items/DCAE_FTL3B.Config_In_Service"):
        res_json = [
            kv_encode("SCN_1:policies/items/DCAE_FTL3B.Config_In_Service", json.dumps(
                {"policyName": "DCAE_FTL3B.Config_In_Service.1.xml", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyConfigStatus": "CONFIG_RETRIEVED", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "In_Service"}, "type": "JSON", "property": null, "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "policyVersion": "1"}
            ))
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "bogus:oti"):
        res_json = None
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == "{}/v1/catalog/services".format(os.environ.get("CONSUL_URL").rstrip("/")):
        res_json = {
                "OTITopologyVM": [],
                "apihandler": [],
                "cloudify_manager": [],
                "config_binding_service": [],
                "consul": [],
                "dashboard": [],
                "deployment_handler": [
                    "deployment_handler"
                ],
                "dmaap_bus_controller": [],
                "dmrb": [],
                "oti_handler": [
                    "oti_handler"
                ],
                "http_dmaap_bus_controller_api": [],
                "https_dmaap_bus_controller_api": [],
                "inventory": [
                    "inventory"
                ],
                "pgda-readonly": [],
                "pgda-service": [],
                "pgda-write": [],
                "policy_handler": [
                    "policy_handler"
                ],
                "pstg-readonly": [],
                "pstg-service": [],
                "pstg-write": [],
                "service-change-handler": [
                    "service-change-handler"
                ],
                "zldcdyh1adcc1-component-dockerhost-1": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adcc1-component-dockerhost-2": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adcc1-component-dockerhost-3": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adcc1-component-dockerhost-4": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adcc1-platform-dockerhost-1": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adcc1-platform-dockerhost-2": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-component-dockerhost-1": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-component-dockerhost-2": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-component-dockerhost-3": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-component-dockerhost-4": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-platform-dockerhost-1": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ],
                "zldcdyh1adce1-platform-dockerhost-2": [
                    "LSLEILAA",
                    "MDTWNJC1"
                ]
        }
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "SCN_1"):
        res_json = [
            {
                "ID": "966d0ef5-7ca2-1b25-d587-2f0541a6ef78",
                "Node": "node_1",
                "Address": "10.1.14.15",
                "Datacenter": "zldcdyh1adcc1",
                "TaggedAddresses": {
                    "lan": "10.1.14.15",
                    "wan": "10.1.14.15"
                },
                "NodeMeta": {
                    "consul-network-segment": "",
                    "fqdn": "kpma00.897658.bogus.com"
                },
                "ServiceKind": "",
                "ServiceID": "scn-1-service-301",
                "ServiceName": "scn-1-service",
                "ServiceTags": [
                    "com-bogus-dcae-controller"
                ],
                "ServiceAddress": "scn-1.bogus.com",
                "ServiceWeights": {
                    "Passing": 1,
                    "Warning": 1
                },
                "ServiceMeta": {
                    "proto": "http"
                },
                "ServicePort": 301,
                "ServiceEnableTagOverride": false,
                "ServiceProxyDestination": "",
                "ServiceProxy": {},
                "ServiceConnect": {},
                "CreateIndex": 30535167,
                "ModifyIndex": 30535167
            }
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), "bogus"):
        res_json = [
        ]
        rv = MonkeyRequestsResponse(full_path, res_json)

    elif full_path == "{}/v1/catalog/node/node_1".format(os.environ.get("CONSUL_URL").rstrip("/")):
        res_json = {
            "Node": {
                "ID": "966d0ef5-7ca2-1b25-d587-2f0541a6ef78",
                "Node": "node_1",
                "Address": "10.1.14.15",
                "Datacenter": "zldcdyh1adcc1",
                "TaggedAddresses": {
                    "lan": "10.1.14.15",
                    "wan": "10.1.14.15"
                },
                "Meta": {
                    "consul-network-segment": "",
                    "fqdn": "kpma00.897658.bogus.com"
                },
                "CreateIndex": 28443495,
                "ModifyIndex": 28443495
            },
            "Services": {
                "apple-320": {
                    "ID": "apple-320",
                    "Service": "apple",
                    "Tags": [
                        "com-bogus-controller-mgmt"
                    ],
                    "Address": "apple.bogus.com",
                    "Meta": {
                        "proto": ""
                    },
                    "Port": 320,
                    "Weights": {
                        "Passing": 1,
                        "Warning": 1
                    },
                    "EnableTagOverride": false,
                    "ProxyDestination": "",
                    "Proxy": {},
                    "Connect": {},
                    "CreateIndex": 28598335,
                    "ModifyIndex": 28598335
                },
                "banana-service-301": {
                    "ID": "banana-service-301",
                    "Service": "banana-service",
                    "Tags": [
                        "com-bogus-controller-dev"
                    ],
                    "Address": "banana.bogus.com",
                    "Meta": {
                        "proto": "http"
                    },
                    "Port": 301,
                    "Weights": {
                        "Passing": 1,
                        "Warning": 1
                    },
                    "EnableTagOverride": false,
                    "ProxyDestination": "",
                    "Proxy": {},
                    "Connect": {},
                    "CreateIndex": 30535167,
                    "ModifyIndex": 30535167
                },
                "d3_kp_platform_kubernetes_master": {
                    "ID": "d3_kp_platform_kubernetes_master",
                    "Service": "d3_kp_platform_kubernetes_master",
                    "Tags": [
                        "KGMTNC20"
                    ],
                    "Address": "10.1.14.15",
                    "Meta": null,
                    "Port": 6443,
                    "Weights": {
                        "Passing": 1,
                        "Warning": 1
                    },
                    "EnableTagOverride": false,
                    "ProxyDestination": "",
                    "Proxy": {},
                    "Connect": {},
                    "CreateIndex": 28443495,
                    "ModifyIndex": 28443495
                }
            }
        }
        rv = MonkeyRequestsResponse(full_path, res_json)

    else:
        Settings.logger.error("monkey_consul_client_get had no mock for {}".format(full_path))
        res_json = None
        rv = MonkeyRequestsResponse(full_path, res_json, status_code=404)

    return rv

def monkey_consul_client_put(full_path, **kwargs):
    """monkeypatch for PUT from consul"""

    Settings.logger.info("monkey_consul_client called with full_path={}, kwargs={}".format(full_path, kwargs))
    rv = False
    res_json = {}
    if full_path == ConsulClient.CONSUL_TRANSACTION_URL.format(os.environ.get("CONSUL_URL").rstrip("/")):
        txn = {}
        if 'json' in kwargs:
            Settings.logger.info("monkey_consul_client called with txn={}".format(str(kwargs.get('json'))))
            txn = kwargs.get('json')[0]
        r_dict = {
            "Results": [
              {
                "KV": {
                  "LockIndex": 99999,
                  "Key": txn.get('Key'),
                  "Flags": 0,
                  "Value": None,
                  "CreateIndex": 99999,
                  "ModifyIndex": 99999
                }
              }
            ],
            "Errors": [
            ]
        }
        status_code = 200
        KV = txn.get('KV')
        if KV and KV.get('Verb') == 'delete' and KV.get('Key') == 'oti_events/anot-her/bogus':
            status_code = 409
            r_dict["Errors"].append({
                "OpIndex": 99,
                "What": "That KV does not exist."
            })
        res_json = [ r_dict ]
        Settings.logger.info("monkey_consul_client produced res_json={} with status_code={}".format(json.dumps(res_json), status_code))
        rv = MonkeyRequestsResponse(full_path, res_json, status_code=status_code)

    elif full_path.startswith(ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), '')):  # wants to write
        # parse_url = urlparse(url)
        # query_dict = dict(parse_qsl(parse_url.query))

        res_json = True
        Settings.logger.info("monkey_consul_client produced res_json={}".format(json.dumps(res_json)))
        rv = MonkeyRequestsResponse(full_path, res_json)

    else:
        Settings.logger.error("monkey_consul_client_put had no mock for {}".format(full_path))

    return rv

class MonkeyCloudifyNode(object):
    """fake cloudify node_instance"""

    def __init__(self, **kwargs):
        """init locals"""

        self.runtime_properties = kwargs.get('runtime_properties', {})
        self.deployment_id = kwargs.get('deployment_id')
        self.node_id = kwargs.get('node_id')
        self.id = kwargs.get('id')
        self.state = kwargs.get('state')

class MonkeyCloudifyClient(object):
    """monkeypatch for CloudifyClient"""

    def __init__(self, **kwargs):
        """init locals"""

        Settings.logger.info("MonkeyCloudifyClient called with kwargs={}".format(json.dumps(kwargs)))
        self._host = kwargs.get('host')
        self._port = kwargs.get('port')
        self._protocol = kwargs.get('protocol')
        self._headers = kwargs.get('headers')

        self.node_instances = self

    def list(self, **kwargs):
        """list node_instances"""

        Settings.logger.info("MonkeyCloudifyClient.list() called with kwargs={}".format(json.dumps(kwargs)))
        rval = []
        deployment_id = kwargs.get('deployment_id')
        sort_field = kwargs.get('_sort')
        if deployment_id or sort_field:
            rval = [
                 MonkeyCloudifyNode(
                     runtime_properties={
                         "container_id": "container_1",
                         "dti_reconfig_script": "dti_reconfig_script_1",
                         "docker_config": {
                             "reconfigs": {
                                 "dti": "dti_reconfig_script_1",
                                 "app": "app_reconfig_script_1",
                                 "special": "special_reconfig_script_1"
                             }
                         },
                         "service_component_name": "SCN_1",
                         "selected_container_destination": "zldcdyh1adcc1-component-dockerhost-1",
                         "service_component_type": "SCType_1"
                     },
                     deployment_id = 'deployment_id_1',
                     node_id = 'node_id_1',
                     id = 'id_1',
                     state = 'state_1'
                 ),
                 MonkeyCloudifyNode(
                     runtime_properties={
                         "container_id": "container_2",
                         "dti_reconfig_script": "dti_reconfig_script_2",
                         "docker_config": {
                             "reconfigs": {
                                 "dti": "dti_reconfig_script_2",
                                 "app": "app_reconfig_script_2",
                                 "special": "special_reconfig_script_2"
                             }
                         },
                         "service_component_name": "SCN_2",
                         "selected_container_destination": "zldcdyh1adcc1-component-dockerhost-1",
                         "service_component_type": "SCType_2"
                     },
                     deployment_id = 'deployment_id_2',
                     node_id = 'node_id_2',
                     id = 'id_2',
                     state = 'state_2'
                 )
            ]

        return rval

class MonkeyEventDbAccess(object):
    """monkdypatch for otihandler.dbclient.apis.EventDbAccess()"""

    def __init__(self, **kwargs):
        """init locals"""
        Settings.logger.info("MonkeyEventDbAccess called with kwargs={}".format(json.dumps(kwargs)))

    def saveDomainObject(self, obj):
        return None

    def deleteDomainObject(self, obj):
        return None

    def query_event_item(self, target_type, target_name):
        Settings.logger.info("MonkeyEventDbAccess.query_event_item({}, {})".format(target_type, target_name))
        return None

    def query_event_data(self, target_type, target_name):
        Settings.logger.info("MonkeyEventDbAccess.query_event_data({}, {})".format(target_type, target_name))
        return []

    def query_event_data_k8s(self, target_type, target_name):
        Settings.logger.info("MonkeyEventDbAccess.query_event_data_k8s({}, {})".format(target_type, target_name))
        return []

    def query_event_info_docker(self, curr_evt, component_scn, deployment_id, container_id):
        Settings.logger.info("MonkeyEventDbAccess.query_event_info_docker({}, {}, {}, {})".format(curr_evt, component_scn, deployment_id, container_id))
        return None

    def update_event_item(self, event, target_type, target_name):
        Settings.logger.info("MonkeyEventDbAccess.update_event_item({}, {}, {})".format(event, target_type, target_name))
        return None

    def query_raw_k8_events(self, cluster, pod, namespace):
        Settings.logger.info("MonkeyEventDbAccess.query_raw_k8_events({}, {}, {})".format(cluster, pod, namespace))
        return []

    def query_raw_docker_events(self, target_types, locations):
        Settings.logger.info("MonkeyEventDbAccess.query_raw_docker_events({}, {})".format(target_types, locations))
        return []

    def query_event_data_k8s_pod(self, curr_evt, component_scn):
        Settings.logger.info("MonkeyEventDbAccess.query_event_k8s_pod({}, {})".format(curr_evt, component_scn))
        return None

class MonkeyDockerClient(object):
    """monkeypatch for docker.APIClient()"""

    def __init__(self, **kwargs):
        """init locals"""

        Settings.logger.info("MonkeyDockerClient called with kwargs={}".format(json.dumps(kwargs)))
        self._base_url = kwargs.get('base_url')
        self._timeout = kwargs.get('timeout')

    def exec_create(self, **kwargs):
        """monkey exec_create"""

        return {"Id": "fake_container_ID"}

    def exec_start(self, **kwargs):
        """monkey exec_create"""

        return "component reconfigured successfully"

    def login(self, **kwargs):
        """monkey login"""

        pass

@pytest.fixture()
def fix_external_interfaces(monkeypatch):
    """monkey consul_client request.get"""

    Settings.logger.info("setup fix_external_interfaces")
    monkeypatch.setattr('otihandler.consul_client.requests.get', monkey_consul_client_get)
    monkeypatch.setattr('otihandler.consul_client.requests.put', monkey_consul_client_put)

    monkeypatch.setattr('otihandler.cfy_client.CloudifyClient', MonkeyCloudifyClient)

    monkeypatch.setattr('otihandler.docker_client.docker.APIClient', MonkeyDockerClient)

    monkeypatch.setattr('otihandler.dti_processor.EventDbAccess', MonkeyEventDbAccess)

    yield fix_external_interfaces  # provide the fixture value
    Settings.logger.info("teardown fix_external_interfaces")


def monkey_cherrypy_engine_exit():
    """monkeypatch for cherrypy exit"""

    Settings.logger.info("monkey_cherrypy_engine_exit()")

@pytest.fixture()
def fix_cherrypy_engine_exit(monkeypatch):
    """monkey cherrypy.engine.exit()"""

    Settings.logger.info("setup fix_cherrypy_engine_exit")
    monkeypatch.setattr('otihandler.web_server.cherrypy.engine.exit',
                        monkey_cherrypy_engine_exit)
    yield fix_cherrypy_engine_exit  # provide the fixture value
    Settings.logger.info("teardown fix_cherrypy_engine_exit")


#----- Tests ----------------------------------------------------------------------------

def test_healthcheck():
    """test /healthcheck"""

    Settings.logger.info("=====> test_healthcheck")
    audit = Audit(req_message="get /healthcheck")
    audit.metrics_start("test /healthcheck")
    time.sleep(0.1)

    audit.metrics("test /healthcheck")
    health = Audit.health() or {}
    audit.audit_done(result=json.dumps(health))

    Settings.logger.info("healthcheck: %s", json.dumps(health))
    assert bool(health)

def test_healthcheck_with_error():
    """test /healthcheck"""

    Settings.logger.info("=====> test_healthcheck_with_error")
    audit = Audit(req_message="get /healthcheck")
    audit.metrics_start("test /healthcheck")
    time.sleep(0.2)
    audit.error("error from test_healthcheck_with_error")
    audit.fatal("fatal from test_healthcheck_with_error")
    audit.debug("debug from test_healthcheck_with_error")
    audit.warn("warn from test_healthcheck_with_error")
    audit.info_requested("info_requested from test_healthcheck_with_error")
    if audit.is_success():
        audit.set_http_status_code(AuditHttpCode.DATA_NOT_FOUND_ERROR.value)
    audit.set_http_status_code(AuditHttpCode.SERVER_INTERNAL_ERROR.value)
    audit.metrics("test /healthcheck")

    health = Audit.health() or {}
    audit.audit_done(result=json.dumps(health))

    Settings.logger.info("healthcheck: %s", json.dumps(health))
    assert bool(health)

def test_consul_client_lookup_service_bogus():
    """test consul_client.lookup_service with bogus service_name"""

    Settings.logger.info("=====> test_consul_client_lookup_service_bogus")
    with pytest.raises(ConsulClientConnectionError, match=r'lookup_service(.*) requests.get(.*)'):
        ConsulClient.lookup_service("bogus")

def test_consul_client_get_service_fqdn_port_none():
    """test consul_client.get_service_fqdn_port with no service_name"""

    Settings.logger.info("=====> test_consul_client_get_service_fqdn_port_none")
    with pytest.raises(ConsulClientConnectionError):
        rv = ConsulClient.get_service_fqdn_port(None)
        assert (rv == None)

def test_consul_client_store_kvs_none():
    """test consul_client.store_kvs with no key"""

    Settings.logger.info("=====> test_consul_client_store_kvs_none")
    rv = ConsulClient.store_kvs(None)
    assert (rv == None)

def test_consul_client_delete_key_none():
    """test consul_client.delete_key with no key"""

    Settings.logger.info("=====> test_consul_client_delete_key_none")
    rv = ConsulClient.delete_key(None)
    assert (rv == None)

def test_consul_client_delete_kvs_none():
    """test consul_client.delete_kvs with no key"""

    Settings.logger.info("=====> test_consul_client_delete_kvs_none")
    rv = ConsulClient.delete_kvs(None)
    assert (rv == None)

def test_consul_client_delete_kvs_bogus():
    """test consul_client.delete_kvs with bogus key"""

    Settings.logger.info("=====> test_consul_client_delete_kvs_bogus")
    rv = ConsulClient.delete_kvs("bogus")
    assert (rv == None)

def test_consul_client_get_value_none():
    """test consul_client.get_value with no key"""

    Settings.logger.info("=====> test_consul_client_get_value_none")
    with pytest.raises(ConsulClientConnectionError, match=r'get_value(.*) requests.get(.*)'):
        ConsulClient.get_value(None)

def test_consul_client_get_kvs_none():
    """test consul_client.get_kvs with no prefix"""

    Settings.logger.info("=====> test_consul_client_get_kvs_none")
    with pytest.raises(ConsulClientConnectionError, match=r'get_kvs(.*) requests.get(.*)'):
        ConsulClient.get_kvs(None)

def test_consul_client_run_transaction_invalid():
    """test consul_client._run_transaction with invalid operation"""

    Settings.logger.info("=====> test_consul_client_run_transaction_invalid")
    rv = ConsulClient._run_transaction("invalid", {"bogus": "bad"})
    assert (rv == None)

class TestsBase(helper.CPWebCase):

    helper.CPWebCase.interactive = False

@pytest.mark.usefixtures("fix_external_interfaces")
class WebServerTest(TestsBase):
    """testing the web-server - runs tests in alphabetical order of method names"""

    def setup_server():
        """setup the web-server"""

        cherrypy.tree.mount(_DTIWeb(), '/')

    setup_server = staticmethod(setup_server)

    def test_web_healthcheck(self):
        """test /healthcheck"""

        Settings.logger.info("=====> test_web_healthcheck")
        result = self.getPage("/healthcheck")
        Settings.logger.info("got healthcheck: %s", self.body)
        self.assertStatus('200 OK')

    def test_web_events_get(self):
        """test GET /events -- wrong method"""

        Settings.logger.info("=====> test_web_events_get")
        result = self.getPage("/events")
        self.assertStatus(404)

    def test_web_events_missing_dcae_service_action(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_missing_dcae_service_action")
        body = json.dumps({})
        expected_result = {"ERROR": "dcae_service_action is missing"}
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('400 Bad Request')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_invalid_dcae_service_action(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_invalid_dcae_service_action")
        body = json.dumps({"dcae_service_action": "bogus"})
        expected_result = {"ERROR": "dcae_service_action is invalid"}
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('400 Bad Request')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_missing_dcae_target_name(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_missing_dcae_target_name")
        body = json.dumps({"dcae_service_action": "deploy"})
        expected_result = {"ERROR": "dcae_target_name is missing"}
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('400 Bad Request')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_missing_dcae_target_type(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_missing_dcae_target_type")
        body = json.dumps({"dcae_service_action": "deploy",
                           "dcae_target_name": "another01ems003"
                         })
        expected_result = {"ERROR": "dcae_target_type is missing"}
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('400 Bad Request')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_deploy(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_deploy")
        body = json.dumps({"dcae_service_action": "deploy",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully"
        }
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_undeploy(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_undeploy")
        body = json.dumps({"dcae_service_action": "undeploy",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_add(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_add")
        body = json.dumps({"dcae_service_action": "add",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully"
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_delete(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_delete")
        body = json.dumps({"dcae_service_action": "delete",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully"
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_update(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_update")
        body = json.dumps({"dcae_service_action": "update",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully",
           "WARNING": "processing update event for anot-her/another01ems003, but current event info is not found in database, executing add event"
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_notify(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_notify")
        body = json.dumps({"dcae_service_action": "notify",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_location(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_location")
        body = json.dumps({"dcae_service_action": "deploy",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her",
                           "dcae_service_location": "LSLEILAA"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully"
        }
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_undeploy_bad(self):
        """test POST /events < <dti_event> -- bad dcae_target_name"""

        Settings.logger.info("=====> test_web_events_undeploy_bad")
        body = json.dumps({"dcae_service_action": "undeploy",
                           "dcae_target_name": "bogus",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully",
            "WARNING": "VNF instance bogus was not in Consul oti_events historical folder"
        }
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_events_undeploy(self):
        """test POST /events < <dti_event>"""

        Settings.logger.info("=====> test_web_events_undeploy")
        body = json.dumps({"dcae_service_action": "undeploy",
                           "dcae_target_name": "another01ems003",
                           "dcae_target_type": "ANOT-her"
                         })
        expected_result = {
            "SCN_1": "ran dti_reconfig_script_1 in docker container container_1 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_1 node node_id_1, got: component reconfigured successfully",
            "SCN_2": "ran dti_reconfig_script_2 in docker container container_2 on zldcdyh1adcc1-component-dockerhost-1 that was deployed by deployment_id_2 node node_id_2, got: component reconfigured successfully"
        }
        expected_result = {
        }
        result = self.getPage("/events", method='POST', body=body,
                              headers=[
                                  ("Content-Type", "application/json"),
                                  ('Content-Length', str(len(body)))
                              ])
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_mockevents(self):
        """test GET /mockevents"""

        Settings.logger.info("=====> test_web_mockevents")
        expected_result = {"KubeNamespace":"com-my-dcae-test", "KubePod":"pod-0", "KubeServiceName":"pod-0.service.local", "KubeServicePort":"8880", "KubeClusterFqdn":"fqdn-1"}
        result = self.getPage("/mockevents")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events_put(self):
        """test PUT /dti_docker_events?service=SCN_1"""

        Settings.logger.info("=====> test_web_dti_docker_events_put")
        result = self.getPage("/dti_docker_events?service=SCN_1", method='PUT')
        self.assertStatus(404)

    def test_web_dti_docker_events_bad_service(self):
        """test GET /dti_docker_events?service=bogus"""

        Settings.logger.info("=====> test_web_dti_docker_events_bad_service")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=bogus")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events(self):
        """test GET /dti_docker_events?service=SCN_1"""

        Settings.logger.info("=====> test_web_dti_docker_events")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=SCN_1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events_bad_location(self):
        """test GET /dti_docker_events?service=SCN_1&location=bogus"""

        Settings.logger.info("=====> test_web_dti_docker_events_bad_location")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=SCN_1&location=bogus")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events_location(self):
        """test GET /dti_docker_events?service=SCN_1&location=LSLEILAA"""

        Settings.logger.info("=====> test_web_dti_docker_events_location")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=SCN_1&location=LSLEILAA")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events_locations(self):
        """test GET /dti_docker_events?service=SCN_1&location=LSLEILAA,MDTWNJC1"""

        Settings.logger.info("=====> test_web_dti_docker_events_locations")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=SCN_1&location=LSLEILAA,MDTWNJC1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_docker_events_bad_locations(self):
        """test GET /dti_docker_events?service=SCN_1&location=LSLEILAA,bogus,MDTWNJC1"""

        Settings.logger.info("=====> test_web_dti_docker_events_bad_locations")
        expected_result = {}
        result = self.getPage("/dti_docker_events?service=SCN_1&location=LSLEILAA,bogus,MDTWNJC1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events_put(self):
        """test PUT /oti_docker_events?service=SCN_1"""

        Settings.logger.info("=====> test_web_oti_docker_events_put")
        result = self.getPage("/oti_docker_events?service=SCN_1", method='PUT')
        self.assertStatus(404)

    def test_web_oti_docker_events_bad_service(self):
        """test GET /oti_docker_events?service=bogus"""

        Settings.logger.info("=====> test_web_oti_docker_events_bad_service")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=bogus")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events(self):
        """test GET /oti_docker_events?service=SCN_1"""

        Settings.logger.info("=====> test_web_oti_docker_events")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=SCN_1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events_bad_location(self):
        """test GET /oti_docker_events?service=SCN_1&location=bogus"""

        Settings.logger.info("=====> test_web_oti_docker_events_bad_location")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=SCN_1&location=bogus")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events_location(self):
        """test GET /oti_docker_events?service=SCN_1&location=LSLEILAA"""

        Settings.logger.info("=====> test_web_oti_docker_events_location")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=SCN_1&location=LSLEILAA")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events_locations(self):
        """test GET /oti_docker_events?service=SCN_1&location=LSLEILAA,MDTWNJC1"""

        Settings.logger.info("=====> test_web_oti_docker_events_locations")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=SCN_1&location=LSLEILAA,MDTWNJC1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_oti_docker_events_bad_locations(self):
        """test GET /oti_docker_events?service=SCN_1&location=LSLEILAA,bogus,MDTWNJC1"""

        Settings.logger.info("=====> test_web_oti_docker_events_bad_locations")
        expected_result = {}
        result = self.getPage("/oti_docker_events?service=SCN_1&location=LSLEILAA,bogus,MDTWNJC1")
        self.assertStatus('200 OK')
        Settings.logger.info("got result: %s", self.body)
        assert ( json.loads(self.body) == expected_result )

    def test_web_reconfig_put(self):
        """test PUT /reconfig -- wrong method"""

        Settings.logger.info("=====> test_web_reconfig_put")
        result = self.getPage("/reconfig/deployment_id_1", method='PUT')
        self.assertStatus(404)

    def test_web_dti_put(self):
        """test PUT /dti -- wrong method"""

        Settings.logger.info("=====> test_web_dti_put")
        result = self.getPage("/dti", method='PUT')
        self.assertStatus(404)

    def test_web_dti(self):
        """test GET /dti"""

        Settings.logger.info("=====> test_web_dti")
        result = self.getPage("/dti")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "anot-her": {
                "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems044": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems044", "dcae_service_location": "MDTWNJC1", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
            }
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_bogus(self):
        """test GET /dti/<service_name> -- bogus SCN"""

        Settings.logger.info("=====> test_web_dti_SCN_bogus")
        result = self.getPage("/dti/SCN_bogus")
        self.assertStatus(404)

    def test_web_dti_SCN(self):
        """test GET /dti/<service_name>"""

        Settings.logger.info("=====> test_web_dti_SCN")
        result = self.getPage("/dti/SCN_1")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "anot-her": {
                "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems044": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems044", "dcae_service_location": "MDTWNJC1", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
            }
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_type(self):
        """test GET /dti/<service_name>?vnf_type=<vnf_type>"""

        Settings.logger.info("=====> test_web_dti_SCN_type")
        result = self.getPage("/dti/SCN_1?vnf_type=ANOT-her")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
            "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
            "another01ems044": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems044", "dcae_service_location": "MDTWNJC1", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_types(self):
        """test GET /dti?vnf_type=<vnf_types>"""

        Settings.logger.info("=====> test_web_dti_types")
        result = self.getPage("/dti?vnf_type=ANOT-her,new-type")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "anot-her": {
                "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems044": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems044", "dcae_service_location": "MDTWNJC1", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
            }
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_type_miss(self):
        """test GET /dti/<service_name>?vnf_type=<vnf_type>"""

        Settings.logger.info("=====> test_web_dti_SCN_type_miss")
        result = self.getPage("/dti/SCN_1?vnf_type=NO-match")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {}
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_type_vnf_id(self):
        """test GET /dti/<service_name>?vnf_type=<vnf_type>;vnf_id=<vnf_id>"""

        Settings.logger.info("=====> test_web_dti_SCN_type_vnf_id")
        result = self.getPage("/dti/SCN_1?vnf_type=ANOT-her;vnf_id=another01ems003")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_type_vnf_id_location(self):
        """test GET /dti/<service_name>?vnf_type=<vnf_type>;vnf_id=<vnf_id>;service_location=<service_location>"""

        Settings.logger.info("=====> test_web_dti_SCN_type_vnf_id_location")
        result = self.getPage("/dti/SCN_1?vnf_type=ANOT-her;vnf_id=another01ems003;service_location=LSLEILAA")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_SCN_types_location(self):
        """test GET /dti/<service_name>?vnf_type=<vnf_types>;service_location=<service_location>"""

        Settings.logger.info("=====> test_web_dti_SCN_types_location")
        result = self.getPage("/dti/SCN_1?vnf_type=ANOT-her,new-type;service_location=MDTWNJC1")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "anot-her": {
                "another01ems044": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems044", "dcae_service_location": "MDTWNJC1", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems044", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
            }
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_location(self):
        """test GET /dti?service_location=<service_location>"""

        Settings.logger.info("=====> test_web_dti_location")
        result = self.getPage("/dti?service_location=LSLEILAA")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "anot-her": {
                "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
                "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
            }
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_dti_type_location(self):
        """test GET /dti?vnf_type=<vnf_type>;service_location=<service_location>"""

        Settings.logger.info("=====> test_web_dti_type_location")
        result = self.getPage("/dti?vnf_type=ANOT-her;service_location=LSLEILAA")
        Settings.logger.info("got dti: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {
            "another01ems003": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems003", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.3", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "remoteServerName": "another01ems003", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}},
            "another01ems042": {"dcae_service_action": "deploy", "dcae_service-instance_model-version-id": "1", "dcae_target_name": "another01ems042", "dcae_service_location": "LSLEILAA", "dcae_target_type": "ANOT-her", "dcae_target_collection": "true", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_in-maint": "false", "dcae_snmp_community_string": "my_first_community", "dcae_generic-vnf_model-version-id": "1", "dcae_target_collection_ip": "107.239.85.42", "dcae_snmp_version": "2c", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"nodeType": "VHSS", "description": "VHSS Data Collection", "protocol": "sftp", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "priority": 1, "nodeSubtype": "", "collectionInterval": "300", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "remoteServerName": "another01ems042", "isPrimary": "true"}], "serverGroupId": "g1"}}}}}, "dcae_target_prov-status": "PROV", "event": {}}
        }
        assert ( json.loads(self.body) == expected_result )

    def test_web_service_component_put(self):
        """test PUT /service_component -- wrong method"""

        Settings.logger.info("=====> test_web_service_component_put")
        result = self.getPage("/service_component/SCN_1", method='PUT')
        self.assertStatus(404)

    def test_web_service_component_bogus(self):
        """test GET /service_component/<service_name> -- bogus SCN"""

        Settings.logger.info("=====> test_web_service_component_bogus")
        result = self.getPage("/service_component/SCN_bogus")
        self.assertStatus(404)

    def test_web_service_component(self):
        """test GET /service_component/<service_name>"""

        Settings.logger.info("=====> test_web_service_component")
        result = self.getPage("/service_component/SCN_1")
        Settings.logger.info("got service_component: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"dcae_target_type": ["pnga-xxx"]}
        assert ( json.loads(self.body) == expected_result )

    def test_web_service_component_all_put(self):
        """test PUT /service_component_all -- wrong method"""

        Settings.logger.info("=====> test_web_service_component_all_put")
        result = self.getPage("/service_component_all/SCN_1", method='PUT')
        self.assertStatus(404)

    def test_web_service_component_all_bogus(self):
        """test GET /service_component_all/<service_name> -- bogus SCN"""

        Settings.logger.info("=====> test_web_service_component_all_bogus")
        result = self.getPage("/service_component_all/SCN_bogus")
        self.assertStatus(404)

    def test_web_service_component_all(self):
        """test GET /service_component_all/<service_name>"""

        Settings.logger.info("=====> test_web_service_component_all")
        result = self.getPage("/service_component_all/SCN_1")
        Settings.logger.info("got service_component_all: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"oti": {"anot-her": {"another01ems003": {"dcae_target_name": "another01ems003", "dcae_target_collection_ip": "107.239.85.3", "event": {}, "dcae_service-instance_model-version-id": "1", "dcae_service_location": "LSLEILAA", "dcae_target_in-maint": "false", "dcae_service_action": "deploy", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_collection": "true", "aai_additional_info": {"TasksItems": {"another01ems003_2PMOBATTHSSEMS0165": {"collectionInterval": "300", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.3", "isPrimary": "true", "remoteServerName": "another01ems003"}], "serverGroupId": "g1"}}, "nodeType": "VHSS", "protocol": "sftp", "nodeSubtype": "", "priority": 1, "description": "VHSS Data Collection"}}}, "dcae_generic-vnf_model-version-id": "1", "dcae_target_prov-status": "PROV", "dcae_target_type": "ANOT-her", "dcae_snmp_community_string": "my_first_community", "dcae_snmp_version": "2c"}, "another01ems042": {"dcae_target_name": "another01ems042", "dcae_target_collection_ip": "107.239.85.42", "event": {}, "dcae_service-instance_model-version-id": "1", "dcae_service_location": "LSLEILAA", "dcae_target_in-maint": "false", "dcae_service_action": "deploy", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_collection": "true", "aai_additional_info": {"TasksItems": {"another01ems042_2PMOBATTHSSEMS0165": {"collectionInterval": "300", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "isPrimary": "true", "remoteServerName": "another01ems042"}], "serverGroupId": "g1"}}, "nodeType": "VHSS", "protocol": "sftp", "nodeSubtype": "", "priority": 1, "description": "VHSS Data Collection"}}}, "dcae_generic-vnf_model-version-id": "1", "dcae_target_prov-status": "PROV", "dcae_target_type": "ANOT-her", "dcae_snmp_community_string": "my_first_community", "dcae_snmp_version": "2c"}, "another01ems044": {"dcae_target_name": "another01ems044", "dcae_target_collection_ip": "107.239.85.42", "event": {}, "dcae_service-instance_model-version-id": "1", "dcae_service_location": "MDTWNJC1", "dcae_target_in-maint": "false", "dcae_service_action": "deploy", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_collection": "true", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"collectionInterval": "300", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "isPrimary": "true", "remoteServerName": "another01ems044"}], "serverGroupId": "g1"}}, "nodeType": "VHSS", "protocol": "sftp", "nodeSubtype": "", "priority": 1, "description": "VHSS Data Collection"}}}, "dcae_generic-vnf_model-version-id": "1", "dcae_target_prov-status": "PROV", "dcae_target_type": "ANOT-her", "dcae_snmp_community_string": "my_first_community", "dcae_snmp_version": "2c"}}}, "policies": {"event": {"update_id": "e6102aab-3079-435a-ae0d-0397a2cb3c4d", "action": "updated", "timestamp": "2018-07-16T15:11:44.845Z", "policies_count": 3}, "items": {"DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific": {"property": null, "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"preparation": "scrambled", "egg_color": "green", "bread": "pumpernickel", "conflicting_key": "green_eggs_and_ham_are_better", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "bacon": "soft, not crispy"}, "responseAttributes": {}, "type": "JSON", "policyVersion": "5", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific", "ECOMPName": "dcae"}}, "DCAE_FTL3B.Config_Green_Collectors": {"property": null, "policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"power_source": "lemmings", "conflicting_key": "green_collectors_wins", "polling_frequency": "30m", "package_type": "plastic"}, "responseAttributes": {}, "type": "JSON", "policyVersion": "1", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "Green_Collectors", "ECOMPName": "dcae"}}, "DCAE_FTL3B.Config_In_Service": {"property": null, "policyName": "DCAE_FTL3B.Config_In_Service.1.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "responseAttributes": {}, "type": "JSON", "policyVersion": "1", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "In_Service", "ECOMPName": "dcae"}}}}, "config": {"dcae_target_type": ["pnga-xxx"]}}
        assert ( json.loads(self.body) == expected_result )

    def test_web_service_component_all_location(self):
        """test GET /service_component_all/<service_name>?service_location=<service_location>"""

        Settings.logger.info("=====> test_web_service_component_all_location")
        result = self.getPage("/service_component_all/SCN_1?service_location=MDTWNJC1")
        Settings.logger.info("got service_component_all: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"oti": {"anot-her": {"another01ems044": {"dcae_target_name": "another01ems044", "dcae_target_collection_ip": "107.239.85.42", "event": {}, "dcae_service-instance_model-version-id": "1", "dcae_service_location": "MDTWNJC1", "dcae_target_in-maint": "false", "dcae_service_action": "deploy", "dcae_target_is-closed-loop-disabled": "false", "dcae_target_collection": "true", "aai_additional_info": {"TasksItems": {"another01ems044_2PMOBATTHSSEMS0165": {"collectionInterval": "300", "serviceType": "MOB-VHSS", "vnfType": "VHSS", "taskId": "2PMOBATTHSSEMS0165", "remoteServerGroups": {"g1": {"serverGroup": [{"remoteServerIp": "107.239.85.42", "isPrimary": "true", "remoteServerName": "another01ems044"}], "serverGroupId": "g1"}}, "nodeType": "VHSS", "protocol": "sftp", "nodeSubtype": "", "priority": 1, "description": "VHSS Data Collection"}}}, "dcae_generic-vnf_model-version-id": "1", "dcae_target_prov-status": "PROV", "dcae_target_type": "ANOT-her", "dcae_snmp_community_string": "my_first_community", "dcae_snmp_version": "2c"}}}, "policies": {"event": {"update_id": "e6102aab-3079-435a-ae0d-0397a2cb3c4d", "action": "updated", "timestamp": "2018-07-16T15:11:44.845Z", "policies_count": 3}, "items": {"DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific": {"property": null, "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"preparation": "scrambled", "egg_color": "green", "bread": "pumpernickel", "conflicting_key": "green_eggs_and_ham_are_better", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "bacon": "soft, not crispy"}, "responseAttributes": {}, "type": "JSON", "policyVersion": "5", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific", "ECOMPName": "dcae"}}, "DCAE_FTL3B.Config_Green_Collectors": {"property": null, "policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"power_source": "lemmings", "conflicting_key": "green_collectors_wins", "polling_frequency": "30m", "package_type": "plastic"}, "responseAttributes": {}, "type": "JSON", "policyVersion": "1", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "Green_Collectors", "ECOMPName": "dcae"}}, "DCAE_FTL3B.Config_In_Service": {"property": null, "policyName": "DCAE_FTL3B.Config_In_Service.1.xml", "policyConfigStatus": "CONFIG_RETRIEVED", "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "responseAttributes": {}, "type": "JSON", "policyVersion": "1", "policyConfigMessage": "Config Retrieved! ", "matchingConditions": {"ONAPName": "dcae", "ConfigName": "In_Service", "ECOMPName": "dcae"}}}}, "config": {"dcae_target_type": ["pnga-xxx"]}}
        assert ( json.loads(self.body) == expected_result )

    def test_web_policies_put(self):
        """test PUT /policies/<service_name> -- wrong method"""

        Settings.logger.info("=====> test_web_policies_put")
        result = self.getPage("/policies/SCN_1", method='PUT')
        self.assertStatus(404)

    def test_web_policies_bogus(self):
        """test GET /policies/<service_name> -- bogus SCN"""

        Settings.logger.info("=====> test_web_policies_bogus")
        result = self.getPage("/policies/SCN_bogus")
        self.assertStatus(404)

    def test_web_policies(self):
        """test GET /policies/<service_name>"""

        Settings.logger.info("=====> test_web_policies")
        result = self.getPage("/policies/SCN_1")
        Settings.logger.info("got policies: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"DCAE_FTL3B.Config_In_Service": {"type": "JSON", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyVersion": "1", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "In_Service"}, "policyConfigStatus": "CONFIG_RETRIEVED", "property": null, "config": {"conflicting_key": "in_service_trumps!", "in_service": true}, "policyName": "DCAE_FTL3B.Config_In_Service.1.xml"}, "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific": {"type": "JSON", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyVersion": "5", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific"}, "policyConfigStatus": "CONFIG_RETRIEVED", "property": null, "config": {"preparation": "scrambled", "bread": "pumpernickel", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "conflicting_key": "green_eggs_and_ham_are_better", "egg_color": "green", "bacon": "soft, not crispy"}, "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml"}, "DCAE_FTL3B.Config_Green_Collectors": {"type": "JSON", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyVersion": "1", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Collectors"}, "policyConfigStatus": "CONFIG_RETRIEVED", "property": null, "config": {"conflicting_key": "green_collectors_wins", "package_type": "plastic", "power_source": "lemmings", "polling_frequency": "30m"}, "policyName": "DCAE_FTL3B.Config_Green_Collectors.1.xml"}}
        assert ( json.loads(self.body) == expected_result )

    def test_web_policies_policy(self):
        """test GET /policies/<service_name>?policy_id=<policy_id>"""

        Settings.logger.info("=====> test_web_policies_policy")
        result = self.getPage("/policies/SCN_1?policy_id=DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific")
        Settings.logger.info("got policies: %s", self.body)
        self.assertStatus('200 OK')

        expected_result = {"type": "JSON", "policyConfigMessage": "Config Retrieved! ", "responseAttributes": {}, "policyVersion": "5", "matchingConditions": {"ONAPName": "dcae", "ECOMPName": "dcae", "ConfigName": "Green_Eggs_and_Ham_specific"}, "policyConfigStatus": "CONFIG_RETRIEVED", "property": null, "config": {"preparation": "scrambled", "bread": "pumpernickel", "dcae_target_type": ["pnga-xxx", "pcrf-oam", "vhss-ems", "anot-her", "new-type"], "conflicting_key": "green_eggs_and_ham_are_better", "egg_color": "green", "bacon": "soft, not crispy"}, "policyName": "DCAE_FTL3B.Config_Green_Eggs_and_Ham_specific.5.xml"}
        assert ( json.loads(self.body) == expected_result )

    @pytest.mark.usefixtures("fix_cherrypy_engine_exit")
    def test_zzz_web_shutdown(self):
        """test /shutdown"""

        Settings.logger.info("=====> test_zzz_web_shutdown")
        Settings.logger.info("sleep before shutdown...")
        time.sleep(1)

        result = self.getPage("/shutdown")
        Settings.logger.info("shutdown result: %s", result)
        self.assertStatus('200 OK')
        Settings.logger.info("got shutdown: %s", self.body)
        self.assertInBody('goodbye! shutdown requested ')
        time.sleep(1)
