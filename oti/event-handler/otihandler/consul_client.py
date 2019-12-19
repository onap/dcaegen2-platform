# ================================================================================
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

"""client to talk to consul at consul port 8500"""

import base64
import copy
import json
import logging
import os
import re
import socket

import requests


class ConsulClientError(RuntimeError):
    pass

class ConsulClientConnectionError(RuntimeError):
    pass

class ConsulClientServiceNotFoundError(RuntimeError):
    pass

class ConsulClientNodeNotFoundError(RuntimeError):
    pass

class ConsulClientKVEntryNotFoundError(RuntimeError):
    pass


class ConsulClient(object):
    """talking to consul"""

    CONSUL_SERVICE_MASK = "{}/v1/catalog/service/{}"
    CONSUL_KV_MASK = "{}/v1/kv/{}"
    CONSUL_KVS_MASK = "{}/v1/kv/{}?recurse=true"
    CONSUL_TRANSACTION_URL = "{}/v1/txn"
    _logger = logging.getLogger("oti_handler.consul_client")

    MAX_OPS_PER_TXN = 64
    # MAX_VALUE_LEN = 512 * 1000

    OPERATION_SET = "set"
    OPERATION_DELETE = "delete"
    OPERATION_DELETE_FOLDER = "delete-tree"


    #----- Methods for Consul services

    @staticmethod
    def lookup_service(service_name):
        """find the service record in consul"""

        service_path = ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), service_name)

        ConsulClient._logger.info("lookup_service(%s)", service_path)

        try:
            response = requests.get(service_path, timeout=30)
            response.raise_for_status()
        # except requests.exceptions.HTTPError as e:
        # except requests.exceptions.ConnectionError as e:
        # except requests.exceptions.Timeout as e:
        except requests.exceptions.RequestException as e:
            msg = "lookup_service({}) requests.get({}) threw exception {}: {!s}".format(
                      service_name, service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            return_list = response.json()
        # except ValueError as e:
        except Exception as e:
            msg = "lookup_service({}) parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      service_name, service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        if not return_list:
            msg = "lookup_service({}) got empty or no value from requests.get({})".format(
                      service_name, service_path)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        return return_list


    @staticmethod
    def get_all_services():
        """List all services from consul"""

        service_path = "{}/v1/catalog/services".format(os.environ.get("CONSUL_URL").rstrip("/"))

        ConsulClient._logger.info("get_all_services(%s)", service_path)

        try:
            response = requests.get(service_path, timeout=30)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "get_all_services() requests.get({}) threw exception {}: {!s}".format(
                      service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            return_dict = response.json()
        except Exception as e:
            msg = "get_all_services() parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        if not return_dict:
            msg = "get_all_services() got empty or no value from requests.get({})".format(
                      service_path)
            ConsulClient._logger.info(msg)
            # raise ConsulClientServiceNotFoundError(msg)

        return return_dict


    @staticmethod
    def _find_matching_services(services, name_search, tags):
        """Find matching services given search criteria"""
        sub_tags = tags[0][4:6]
        tags.append(sub_tags)

        def is_match(service):
            srv_name, srv_tags = service
            return name_search in srv_name and \
                    any([tag in srv_tags for tag in tags])

        return [ srv[0] for srv in list(services.items()) if is_match(srv) ]


    @staticmethod
    def search_services(name_search, tags):
        """
        Search for services that match criteria

        Args:
        -----
        name_search: (string) Name to search for as a substring
        tags: (list) List of strings that are tags. A service must match **ANY OF** the
            tags in the list.

        Returns:
        --------
        List of names of services that matched
        """

        matches = []

        # srvs is dict where key is service name and value is list of tags
        srvs = ConsulClient.get_all_services()

        if srvs:
            matches = ConsulClient._find_matching_services(srvs, name_search, tags)

        return matches


    @staticmethod
    def get_service_fqdn_port(service_name, node_meta=False):
        """find the service record in consul"""

        service_path = ConsulClient.CONSUL_SERVICE_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), service_name)

        ConsulClient._logger.info("get_service_fqdn_port(%s)", service_path)

        try:
            response = requests.get(service_path, timeout=30)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "get_service_fqdn_port({}) requests.get({}) threw exception {}: {!s}".format(
                      service_name, service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            service = response.json()
        except Exception as e:
            msg = "get_service_fqdn_port({}) parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      service_name, service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        if not service:
            msg = "get_service_fqdn_port({}) got empty or no value from requests.get({})".format(
                      service_name, service_path)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        try:
            service = service[0]     # arbitrarily choose the first one
            port = service["ServicePort"]

            # HTTPS certificate validation requires FQDN not IP address
            fqdn = ""
            if node_meta:
                meta = service.get("NodeMeta")
                if meta:
                    fqdn = meta.get("fqdn")
            if not fqdn:
                fqdn = socket.getfqdn(str(service["ServiceAddress"]))
        except Exception as e:
            msg = "get_service_fqdn_port({}) parsing result from requests.get({}) threw exception {}: {!s}".format(
                      service_name, service_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientServiceNotFoundError(msg)

        return (fqdn, port)


    #----- Methods for Consul nodes

    @staticmethod
    def lookup_node(node_name):
        """find the node record in consul"""

        node_path = "{}/v1/catalog/node/{}".format(os.environ.get("CONSUL_URL").rstrip("/"), node_name)

        ConsulClient._logger.info("lookup_node(%s)", node_path)

        try:
            response = requests.get(node_path, timeout=30)
            response.raise_for_status()
        # except requests.exceptions.HTTPError as e:
        # except requests.exceptions.ConnectionError as e:
        # except requests.exceptions.Timeout as e:
        except requests.exceptions.RequestException as e:
            msg = "lookup_node({}) requests.get({}) threw exception {}: {!s}".format(
                      node_name, node_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            return_dict = response.json()
        # except ValueError as e:
        except Exception as e:
            msg = "lookup_node({}) parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      node_name, node_path, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientNodeNotFoundError(msg)

        if not return_dict:
            msg = "lookup_node({}) got empty or no value from requests.get({})".format(
                      node_name, node_path)
            ConsulClient._logger.error(msg)
            raise ConsulClientNodeNotFoundError(msg)

        return return_dict


    #----- Methods for Consul key-values

    @staticmethod
    def put_value(key, data, cas=None):
        """put the value for key into consul-kv"""

        # ConsulClient._logger.info("put_value(%s)", str(key))

        URL = ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), key)
        if cas is not None:
            URL = '{}?cas={}'.format(URL, cas)

        try:
            response = requests.put(URL, data=json.dumps(data), timeout=30)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "put_value({}) requests.put({}) threw exception {}: {!s}".format(
                      key, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            updated = response.json()
        except Exception as e:
            msg = "put_value({}) parsing JSON from requests.put({}) threw exception {}: {!s}".format(
                      key, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        return updated


    @staticmethod
    def get_value(key, get_index=False):
        """get the value for key from consul-kv"""

        URL = ConsulClient.CONSUL_KV_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), key)

        try:
            response = requests.get(URL, timeout=30)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "get_value({}) requests.get({}) threw exception {}: {!s}".format(
                      key, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            data = response.json()
        except Exception as e:
            msg = "get_value({}) parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      key, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        if not data:
            msg = "get_value({}) got empty or no value from requests.get({})".format(
                      key, URL)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        try:
            value = base64.b64decode(data[0]["Value"]).decode("utf-8")
            value_dict = json.loads(value)
        except Exception as e:
            msg = "get_value({}) decoding value from requests.get({}) threw exception {}: {!s}".format(
                      key, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        ConsulClient._logger.info("consul-kv key=%s value(%s) data=%s",
                                     key, value, json.dumps(data))

        if get_index:
            return data[0]["ModifyIndex"], value_dict

        return value_dict


    @staticmethod
    def get_kvs(prefix, nest=True, trim_prefix=False):
        """get key-values for keys beginning with prefix from consul-kv"""

        URL = ConsulClient.CONSUL_KVS_MASK.format(os.environ.get("CONSUL_URL").rstrip("/"), prefix)

        try:
            response = requests.get(URL, timeout=30)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "get_kvs({}) requests.get({}) threw exception {}: {!s}".format(
                      prefix, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientConnectionError(msg)

        try:
            data = response.json()
        except Exception as e:
            msg = "get_kvs({}) parsing JSON from requests.get({}) threw exception {}: {!s}".format(
                      prefix, URL, type(e).__name__, e)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        if not data:
            msg = "get_kvs({}) got empty or no value from requests.get({})".format(
                      prefix, URL)
            ConsulClient._logger.error(msg)
            raise ConsulClientKVEntryNotFoundError(msg)

        def put_level_value(level_keys, value, level_dict={}):
            if level_keys:
                key = level_keys.pop(0)
                level_dict[key] = put_level_value(level_keys, value, level_dict.get(key, {}))
                return level_dict
            else:
                return value

        rdict = {}
        for item in data:
            v = base64.b64decode(item["Value"]).decode("utf-8")
            try:
                value = json.loads(v)
            except Exception as e:
                value = v
            key = item['Key']
            if trim_prefix:
                key = key[len(prefix):]
            if nest:
                level_keys = key.split('/')
                rdict = put_level_value(level_keys, value, rdict)
            else:
                rdict[key] = value

        ConsulClient._logger.info("consul-kv prefix=%s value(%s) data=%s",
                                  prefix, json.dumps(rdict), json.dumps(data))
        return rdict


    @staticmethod
    def _gen_txn_operation(verb, key, value=None):
        """returns the properly formatted operation to be used inside transaction"""

        # key = urllib.quote(key)  # can't use urllib.quote() because it kills ':' in the key
        if value:
            return {"KV": {"Verb": verb, "Key": key, "Value": base64.b64encode(value)}}
        return {"KV": {"Verb": verb, "Key": key}}


    @staticmethod
    def _run_transaction(operation_name, txn):
        """run a single transaction of several operations at consul /txn"""

        if not txn:
            return

        txn_url = ConsulClient.CONSUL_TRANSACTION_URL.format(os.environ.get("CONSUL_URL").rstrip("/"))
        response = None
        try:
            response = requests.put(txn_url, json=txn, timeout=30)
        except requests.exceptions.RequestException as e:
            ConsulClient._logger.error("failed to {} at {}: exception {}: {!s} on txn={}"
                .format(operation_name, txn_url, type(e).__name__, e, json.dumps(txn)))
            return

        if response.status_code != requests.codes.ok:
            ConsulClient._logger.error("failed {} {}: {} text={} txn={} headers={}"
                .format(operation_name, txn_url, response.status_code,
                        response.text, json.dumps(txn),
                        json.dumps(dict(list(response.request.headers.items())))))
            return

        ConsulClient._logger.info("response for {} {}: {} text={} txn={} headers={}"
            .format(operation_name, txn_url, response.status_code,
                    response.text, json.dumps(txn),
                    json.dumps(dict(list(response.request.headers.items())))))

        return True


    @staticmethod
    def store_kvs(kvs):
        """put kvs into consul-kv"""

        if not kvs:
            ConsulClient._logger.warn("kvs not supplied to store_kvs()")
            return

        store_kvs = [
            ConsulClient._gen_txn_operation(ConsulClient.OPERATION_SET,
                                            key, json.dumps(value))
                for key, value in kvs.items()
        ]
        txn = []
        idx_step = ConsulClient.MAX_OPS_PER_TXN - len(txn)
        for idx in range(0, len(store_kvs), idx_step):
            txn += store_kvs[idx : idx + idx_step]
            if not ConsulClient._run_transaction("store_kvs", txn):
                return False
            txn = []

        return ConsulClient._run_transaction("store_kvs", txn)


    @staticmethod
    def delete_key(key):
        """delete key from consul-kv"""

        if not key:
            ConsulClient._logger.warn("key not supplied to delete_key()")
            return

        delete_key = [
            ConsulClient._gen_txn_operation(ConsulClient.OPERATION_DELETE, key)
        ]
        return ConsulClient._run_transaction("delete_key", delete_key)


    @staticmethod
    def delete_kvs(key):
        """delete key from consul-kv"""

        if not key:
            ConsulClient._logger.warn("key not supplied to delete_kvs()")
            return

        delete_kvs = [
            ConsulClient._gen_txn_operation(ConsulClient.OPERATION_DELETE_FOLDER, key)
        ]
        return ConsulClient._run_transaction("delete_kvs", delete_kvs)


    #----- Methods for Config Binding Service

    @staticmethod
    def get_service_component(scn):
        config = json.dumps(ConsulClient.get_value(scn))

        try:
            dmaap = ConsulClient.get_value(scn + ":dmaap")
        except Exception as e:
            dmaap = None
        if dmaap:
            for key in list(dmaap.keys()):
                config = re.sub('"<<' + key + '>>"', json.dumps(dmaap[key]), config)

        try:
            rel = ConsulClient.get_value(scn + ":rel")
        except Exception as e:
            rel = None
        if rel:
            for key in list(rel.keys()):
                config = re.sub('"{{' + key + '}}"', json.dumps(rel[key]), config)

        return json.loads(config)


    @staticmethod
    def get_service_component_all(scn, policies_as_list=True):
        t_scn = scn + ":"
        t_len = len(t_scn)
        a_dict = ConsulClient.get_kvs(scn)
        b_dict = {}
        for key in a_dict:
            b_key = None
            if key == scn:
                b_dict["config"] = ConsulClient.get_service_component(scn)
            elif key == scn + ":dmaap":
                continue
            elif key[0:t_len] == t_scn:
                b_key = key[t_len:]
                # policies_as_list = True formats policies items in a list like ONAP's CBS; False keeps policy_ids keys
                if policies_as_list and b_key == "policies":  # convert items from KVs to a values list
                    b_dict[b_key] = {}
                    for sub_key in a_dict[key]:
                        if sub_key == "items":
                            b_dict[b_key][sub_key] = []
                            d_dict = a_dict[key][sub_key]
                            for item in sorted(d_dict.keys()):  # old CBS sorted them so we emulate
                                b_dict[b_key][sub_key].append(d_dict[item])
                        else:
                            b_dict[b_key][sub_key] = copy.deepcopy(a_dict[key][sub_key])
                else:
                    b_dict[b_key] = copy.deepcopy(a_dict[key])
        return b_dict


    @staticmethod
    def add_vnf_id(scn, vnf_type, vnf_id, dti_dict):
        """
        Add VNF instance to Consul scn:dti key.

        Treat its value as a JSON string representing a dict.
        Extend the dict by adding a dti_dict for vnf_id under vnf_type.
        Turn the resulting extended dict into a JSON string.
        Store the string back into Consul under scn:dti key.
        Watch out for conflicting concurrent updates.
        """

        key = scn + ':dti'
        lc_vnf_type = vnf_type.lower()
        while True:     # do until update succeeds
            (mod_index, v) = ConsulClient.get_value(key, get_index=True)
            lc_v = {ky.lower():vl for ky,vl in list(v.items())}  # aware this arbitrarily picks keys that only differ in case
                                                           # but DCAE-C doesn't create such keys

            if lc_vnf_type not in lc_v:
                return  # That VNF type is not supported by this component
            lc_v[lc_vnf_type][vnf_id] = dti_dict  # add or replace the VNF instance

            updated = ConsulClient.put_value(key, lc_v, cas=mod_index)
            if updated:
                return lc_v


    @staticmethod
    def delete_vnf_id(scn, vnf_type, vnf_id):
        """
        Delete VNF instance from Consul scn:dti key.

        Treat its value as a JSON string representing a dict.
        Modify the dict by deleting the vnf_id key entry from under vnf_type.
        Turn the resulting extended dict into a JSON string.
        Store the string back into Consul under scn:dti key.
        Watch out for conflicting concurrent updates.
        """

        key = scn + ':dti'
        lc_vnf_type = vnf_type.lower()
        while True:     # do until update succeeds
            (mod_index, v) = ConsulClient.get_value(key, get_index=True)
            lc_v = {ky.lower():vl for ky,vl in list(v.items())}  # aware this arbitrarily picks keys that only differ in case
                                                           # but DCAE-C doesn't create such keys

            if lc_vnf_type not in lc_v:
                return  # That VNF type is not supported by this component
            if vnf_id not in lc_v[lc_vnf_type]:
                return lc_v
            del lc_v[lc_vnf_type][vnf_id]  # delete the VNF instance

            updated = ConsulClient.put_value(key, lc_v, cas=mod_index)
            if updated:
                return lc_v


if __name__ == "__main__":
    value = None

    if value:
        print(json.dumps(value, sort_keys=True, indent=4, separators=(',', ': ')))
