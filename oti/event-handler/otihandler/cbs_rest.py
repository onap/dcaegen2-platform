# ================================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

"""REST for high-level information retrievals from Consul KVs"""

import copy
import logging

from otihandler.consul_client import ConsulClient


class CBSRest(object):
    _logger = logging.getLogger("oti_handler.cbs_rest")

    @staticmethod
    def get_value(key, default=None):
        """Wrap ConsulClient.get_value() to ignore exceptions."""

        data = default
        try:
            data = ConsulClient.get_value(key)
        except Exception as e:
            pass

        return data

    @staticmethod
    def get_kvs(key):
        """Wrap ConsulClient.get_kvs() to ignore exceptions."""

        data = {}
        try:
            data = ConsulClient.get_kvs(key, trim_prefix=True)
        except Exception as e:
            data = {}

        if not data:
            data = {}
        return data

    @staticmethod
    def get_service_component(service_name):
        """Get the fully-bound config for a service_name."""

        return ConsulClient.get_service_component(service_name)

    @staticmethod
    def get_service_component_all(service_name, service_location=None, policies_as_list=False):
        """Get all Consul objects for a service_name."""

        r_dict = ConsulClient.get_service_component_all(service_name, policies_as_list=policies_as_list)
        if r_dict and r_dict.get('oti'):
            r_dict['oti'] = CBSRest.get_oti(service_name, service_location=service_location)
        return r_dict

    @staticmethod
    def get_oti(service_name=None, vnf_type=None, vnf_id=None, service_location=None):
        """
        Get DTI events.

        Parameters
        ----------
        service_name : string
            optional.  The service component name assigned by dockerplugin to the component that is unique to the cloudify node instance and used in its Consul key(s).
        vnf_type : string
            optional, allows multiple values separated by commas.  Gets DTI events for these vnf_type(s).
        vnf_id : string
            optional.  Requires vnf_type also.  Gets DTI event for this vnf_id.
        service_location : string
            optional, allows multiple values separated by commas.  Filters DTI events with dcae_service_location in service_location.
            If service_location is not provided, then defaults to dockerhost or k8s cluster master node service Consul TAGs if service_name is provided,
            otherwise results are not location filtered.

        Returns
        -------
        dict
            Dictionary of DTI event(s). 
            If one vnf_type and vnf_id are both specified, then object returned will be just the one DTI event.
            If one vnf_type is specified but not vnf_id, then DTI events will be keyed by vnf_id.
            Otherwise the DTI events will be keyed by vnf_type, sub-keyed by vnf_id.

        """

        lc_vnf_type = vnf_type
        if vnf_type:
            lc_vnf_type = vnf_type.lower()

        r_dict = {}

        want_locs = []
        if service_location:
            want_locs = service_location.split(',')

        give_types = []
        if service_name:
            if not want_locs:  # default to TAGs of container's dockerhost or k8s cluster master node
               try:
                   node_name = ConsulClient.lookup_service(service_name)[0].get("Node")
                   if node_name:
                       services = ConsulClient.lookup_node(node_name).get("Services")
                       if services:
                           for node_svc in list(services.keys()):
                               if "-component-dockerhost-" in node_svc or "_component_kubernetes_master" in node_svc:
                                   want_locs = services[node_svc].get("Tags", [])
                                   break
               except:
                   pass
            supported_types = ConsulClient.get_value(service_name + ":oti")
            if supported_types:
                supported_types = [type.lower() for type in list(supported_types.keys())]
            if supported_types:
                if lc_vnf_type:  # If user specifies vnf_type(s), constrain to supported ones
                    for type in lc_vnf_type.split(','):
                        if type in supported_types:
                            give_types.append(type)
                else:
                    give_types = supported_types
            if not give_types or (len(give_types) == 1 and give_types[0] == ''):
                return r_dict
        elif lc_vnf_type:
            give_types = lc_vnf_type.split(',')


        # If they specified only one vnf_type ...
        if lc_vnf_type and ',' not in lc_vnf_type:
            type = give_types[0]

            # ... and vnf_id
            if vnf_id:
                # get just one vnf_id
                t_dict = CBSRest.get_value("oti_events/" + type + "/" + vnf_id, default=None)
                if t_dict:
                    event_loc = t_dict.get('dcae_service_location')
                    if not event_loc or not want_locs or event_loc in want_locs:
                        r_dict = copy.deepcopy(t_dict)

            # ... and not vnf_id
            else:
                # get all DTI events of just one type, indexed by vnf_id
                t_dict = CBSRest.get_kvs("oti_events/" + type + "/")
                if t_dict:
                    if not want_locs:
                        r_dict = copy.deepcopy(t_dict)
                    else:
                        for id in t_dict:
                            event_loc = t_dict[id].get('dcae_service_location')
                            if not event_loc or event_loc in want_locs:
                                r_dict[id] = copy.deepcopy(t_dict[id])

        # If they did not specify either service_name or vnf_type (the only way give_types=[])
        elif not give_types:
            # get all DTI events, indexed by vnf_type then vnf_id
            t_dict = CBSRest.get_kvs("oti_events/")
            if t_dict:
                for type in t_dict:
                    for id in t_dict[type]:
                        if not vnf_id or vnf_id == id:
                            if want_locs:
                                event_loc = t_dict[type][id].get('dcae_service_location')
                            if not want_locs or not event_loc or event_loc in want_locs:
                                if type not in r_dict:
                                    r_dict[type] = {}
                                r_dict[type][id] = copy.deepcopy(t_dict[type][id])

        # If they speclfied multiple vnf_types
        else:
            # get all DTI events of give_types, indexed by vnf_type then vnf_id
            for type in give_types:
                t_dict = CBSRest.get_kvs("oti_events/" + type + "/")
                if t_dict:
                    for id in t_dict:
                        if not vnf_id or vnf_id == id:
                            if want_locs:
                                event_loc = t_dict[id].get('dcae_service_location')
                            if not want_locs or not event_loc or event_loc in want_locs:
                                if type not in r_dict:
                                    r_dict[type] = {}
                                r_dict[type][id] = copy.deepcopy(t_dict[id])

        return r_dict

    @staticmethod
    def get_policies(service_name, policy_id=None):
        """Get one or all policies for a service_name."""

        if policy_id:
            return ConsulClient.get_value(service_name + ":policies/items/" + policy_id)
        else:
            return ConsulClient.get_kvs(service_name + ":policies/items/", trim_prefix=True)
