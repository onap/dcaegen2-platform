# ================================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

"""Our client interface to Cloudify"""
import base64
import copy
import json
import logging
import os

import requests

from dtihandler.consul_client import ConsulClient


class CfyClientConsulError(RuntimeError):
    pass


class CloudifyClient(object):
    """quick replacement for cloudify_rest_client -- this supports pagination and scans all DCAE tenants"""

    def __init__(self, **kwargs):
        self._protocol = kwargs.get('protocol', 'http')
        self._host = kwargs.get('host')
        self._port = kwargs.get('port')
        self._headers = kwargs.get('headers')

        self.node_instances = self

    def list(self, **kwargs):
        url_mask = "{}://{}:{}/api/v3.1/tenants".format(self._protocol, self._host, self._port)
        # s = Session()
        # req = Request('GET', url_mask, headers=self._headers)
        # prepped = req.prepare()
        # response = s.send(prepped,verify=False,timeout=30)
        response = requests.get(url_mask, headers=self._headers, timeout=30)
        obj = response.json()
        tenants = [x["name"] for x in obj["items"]]
        tenants_with_containers = [x for x in tenants if 'DCAE' in x]

        size = 1000
        url_mask = "{}://{}:{}/api/v3.1/node-instances?_size={}&_offset={}".format(
                   self._protocol, self._host, self._port, size, "{}")
        if kwargs:
            for (key,val) in kwargs.items():
                if isinstance(val, str):
                    url_mask = url_mask + '&{}={}'.format(key, val)
                elif isinstance(val, list):
                    url_mask = url_mask + '&{}={}'.format(key, ','.join(val))

        for tenant in tenants_with_containers:
            self._headers_with_tenant = copy.deepcopy(self._headers)
            self._headers_with_tenant['Tenant'] = tenant

            offset = 0
            total = 1
            while offset < total:
                # s = Session()
                # req = Request('GET', url_mask.format(offset), headers=self._headers_with_tenant)
                # prepped = req.prepare()
                # response = s.send(prepped, verify=False, timeout=30)
                response = requests.get(url_mask.format(offset), headers=self._headers_with_tenant, timeout=30)
                response.raise_for_status()
                obj = response.json()
                offset = offset + len(obj["items"])
                total = obj["metadata"]["pagination"]["total"]
                for item in obj["items"]:
                    yield NodeInstance(item)

    def update_node_instance(self, node_instance_id, body, **kwargs):
        headers = copy.deepcopy(self._headers_with_tenant)
        headers['Content-Type'] = "application/json"
        url_mask = "{}://{}:{}/api/v3.1/node-instances/{}".format(
                   self._protocol, self._host, self._port, node_instance_id)
        response = requests.patch(url_mask, json=body, headers=headers, timeout=30)
        obj = response.json()
        return obj


class NodeInstance(object):
    """quick replacement for cloudify_rest_client"""

    def __init__(self, instance):
        self.id = instance.get("id")
        self.deployment_id = instance.get("deployment_id")
        self.host_id = instance.get("host_id")
        self.runtime_properties = instance.get("runtime_properties")
        self.relationships = instance.get("relationships")
        self.state = instance.get("state")
        self.version = instance.get("version")
        self.node_id = instance.get("node_id")
        self.scaling_groups = instance.get("scaling_groups")


class CfyClient(object):
    _logger = logging.getLogger("dti_handler.cfy_client")
    _client = None


    @staticmethod
    def __set_cloudify_manager_client():
        """Create connection to Cloudify_Manager."""

        if CfyClient._client:
            return

        host = None
        port = None
        obj = json.loads(os.environ.get("CLOUDIFY", "{}")).get("cloudify")
        source = "CLOUDIFY environment variable"
        if not obj:
            CM_KEY = 'cloudify_manager'
            source = "Consul key '{}'".format(CM_KEY)

            try:
                results = ConsulClient.lookup_service(CM_KEY)
            except Exception as e:
                msg = "Unexpected exception {}: {!s} from ConsulClient.lookup_service({})".format(type(e).__name__, e, CM_KEY)
                CfyClient._logger.error(msg)
                raise CfyClientConsulError(msg)
            result = results[0]
            host = result['ServiceAddress']
            port = result['ServicePort']

            try:
                obj = ConsulClient.get_value(CM_KEY)
            except Exception as e:
                msg = "Unexpected exception {}: {!s} from ConsulClient.get_value({})".format(type(e).__name__, e, CM_KEY)
                CfyClient._logger.error(msg)
                raise CfyClientConsulError(msg)
            if not obj:
                raise CfyClientConsulError("{} value is empty or invalid".format(source))

            obj = obj.get('cloudify')

        if not obj:
            raise CfyClientConsulError("{} value is missing 'cloudify' key or value".format(source))

        host = obj.get('address', host)
        if not host:
            raise CfyClientConsulError("{} value is missing 'cloudify.address'".format(source))

        port = obj.get('port', port)
        if not port:
            raise CfyClientConsulError("{} value is missing 'cloudify.port'".format(source))

        protocol = obj.get('protocol')
        if not protocol:
            raise CfyClientConsulError("{} value is missing 'cloudify.protocol'".format(source))
        username = obj.get('user')
        if not username:
            raise CfyClientConsulError("{} value is missing 'cloudify.user'".format(source))
        password = obj.get('password')
        if not password:
            raise CfyClientConsulError("{} value is missing 'cloudify.password'".format(source))

        b64_encoded_str = base64.b64encode(bytes("{}:{}".format(username, password), 'utf-8')).decode("utf-8")
        headers = {'Authorization': 'Basic ' + b64_encoded_str.rstrip('\n')}
        #headers = {'Authorization': 'Basic ' + '{}:{}'.format(username, password).encode("base64").rstrip('\n')}
        
        CfyClient._client = CloudifyClient(host=host, port=port, protocol=protocol, headers=headers)
    

    @staticmethod
    def query_k8_components(in_cluster_fqdn):
        """
        Iterate components that belong to a cluster fqdn.

        Parameters
        ----------
        in_cluster_fqdn : string
            k8s cluster FQDN

        Returns
        -------
        A generator of tuples of component information
            [ (proxy_fqdn, namespace, scn, replicas, scn_port), ... ]
        """

        cnt_found = 0
        CfyClient.__set_cloudify_manager_client()
        for node_instance in CfyClient._client.node_instances.list(_sort="deployment_id"):
            rtp = node_instance.runtime_properties
            scn_port = None
            cluster_fqdn = None
            proxy_fqdn = None
            dti_info = rtp.get('dti_info')
            if dti_info:
                env_items = dti_info.get('env')
                for env in env_items:
                    if env.get("name") == 'KUBE_CLUSTER_FQDN':
                        cluster_fqdn = env.get("value")
                    if env.get("name") == 'KUBE_PROXY_FQDN':
                        proxy_fqdn = env.get("value")
                ports = dti_info.get('ports')
                if ports:
                    scn_port = ports[0].split(':')[0]
            else:
                continue

            if in_cluster_fqdn != cluster_fqdn:
                continue

            controller_type = rtp.get('k8s_controller_type')
            if not controller_type:
                CfyClient._logger.debug("controller type is missing")
                continue
            elif controller_type != "statefulset":
                CfyClient._logger.debug("not a stateful set")
                continue

            container_id = rtp.get('k8s_deployment')
            if not container_id:
                CfyClient._logger.debug("{} {} runtime_properties has no container_id or k8s_deployment".format(
                    node_instance.deployment_id, node_instance.id))
                continue

            try:
                namespace = container_id.get('namespace')
            except:
                namespace = ''
                pass

            replicas = 1
            try:
                replicas = rtp.get('replicas')
            except:
                pass

            scn = rtp.get('service_component_name')
            if not scn:
                CfyClient._logger.debug(
                    "{} {} runtime_properties has no service_component_name".format(node_instance.deployment_id,
                                                                                    node_instance.id))
                continue

            cnt_found += 1
            yield (proxy_fqdn, namespace, scn, replicas, scn_port)
            continue

        msg = "Found {} components (collectors) for cluster={}" \
            .format(cnt_found, in_cluster_fqdn)
        CfyClient._logger.debug(msg)


    @staticmethod
    def iter_components(dcae_target_type, dcae_service_location='', component_type=''):
        """
        Iterate components that handle a given dcae_target_type.
    
        Parameters
        ----------
        dcae_target_type : string
            VNF Type
        dcae_service_location : string
            Location of the component (optional)
        component_type : string
            Type of the component (optional)
    
        Returns
        -------
        A generator of tuples of component information
           [ (scn, deployment_id, container_id, node_id, node_instance_id, node_instance_state, docker_host, reconfig_script, "docker"), ... ]
        or
           [ (scn, deployment_id, k8s_deployment, node_id, node_instance_id, node_instance_state, config_content, reconfig_script, "k8s"), ... ]

        """
    
        cnt_found = 0
    
        # get dockerhost and kubernetes_master services that are TAGged for the dcae_service_location (CLLI)
        dockerhosts = []
        k8s_svcs_tagged_with_clli = []
        if dcae_service_location:
            try:
                dockerhosts = ConsulClient.search_services("-component-dockerhost-", [dcae_service_location])
            except Exception as e:
                msg = "Unexpected exception {}: {!s} from ConsulClient.search_services({}, {!s})".format(type(e).__name__, e, "-component-dockerhost-", [dcae_service_location])
                CfyClient._logger.error(msg)
                raise CfyClientConsulError(msg)
            try:
                k8s_svcs_tagged_with_clli = ConsulClient.search_services("_component_kubernetes_master", [dcae_service_location])
            except Exception as e:
                msg = "Unexpected exception {}: {!s} from ConsulClient.search_services({}, {!s})".format(type(e).__name__, e, "_component_kubernetes_master", [dcae_service_location])
                CfyClient._logger.error(msg)
                raise CfyClientConsulError(msg)
    
        CfyClient.__set_cloudify_manager_client()
        for node_instance in CfyClient._client.node_instances.list(_sort="deployment_id"):
            rtp = node_instance.runtime_properties

            # Skip this node_instance if it is not a collector
            container_type = "docker"
            container_id = rtp.get('container_id')
            docker_host = ''
            svc_with_my_clli_tags = ''
            if not container_id:
                container_type = "k8s"
                container_id = rtp.get('k8s_deployment')
                if not container_id:
                    CfyClient._logger.debug("{} {} runtime_properties has no container_id or k8s_deployment".format(node_instance.deployment_id, node_instance.id))
                    continue
            docker_config = rtp.get('docker_config')
            if not docker_config:
                CfyClient._logger.debug("{} {} runtime_properties has no docker_config".format(node_instance.deployment_id, node_instance.id))
                continue
            dti_reconfig_script = ""
            if container_type == "docker":
                dti_reconfig_script = rtp.get('dti_reconfig_script')
                if not dti_reconfig_script:
                    CfyClient._logger.debug("{} {} runtime_properties has no dti_reconfig_script".format(node_instance.deployment_id, node_instance.id))
                    continue
            elif container_type == "k8s":
                dti_reconfig_script = docker_config.get('reconfigs',{}).get('dti')
                if not dti_reconfig_script:
                    CfyClient._logger.debug("{} {} runtime_properties docker_config has no reconfigs.dti".format(node_instance.deployment_id, node_instance.id))
                    continue

            scn = rtp.get('service_component_name')
            scn_address = None
            scn_port = None
            if not scn:
                CfyClient._logger.debug("{} {} runtime_properties has no service_component_name".format(node_instance.deployment_id, node_instance.id))
                continue
            if container_type == "docker":
                docker_host = rtp.get('selected_container_destination')
                if not docker_host:
                    CfyClient._logger.debug("{} {} runtime_properties has no selected_container_destination".format(node_instance.deployment_id, node_instance.id))
                    continue
            elif container_type == "k8s":
                try:
                    srvcCatalogItem = ConsulClient.lookup_service(scn)[0]
                    scn_address = srvcCatalogItem.get("ServiceAddress")
                except:
                    CfyClient._logger.debug(
                        "{} {} runtime_properties has no consul svc catalog registry".format(node_instance.deployment_id,
                                                                                       node_instance.id))
                    continue
                svc_with_my_clli_tags = rtp.get('svc_with_my_clli_tags')
                 # e.g., scn="s908d92e232ed43..."
                if not svc_with_my_clli_tags:
                    # We should not incur this burden.  k8splugin should store this into runtime properties.
                    try:
                        node_name = srvcCatalogItem.get("Node")
                        if node_name:
                            # e.g., node_name="zldcdyh1adce3kpma00"
                            services = ConsulClient.lookup_node(node_name).get("Services")
                            if services:
                                for node_svc in list(services.keys()):
                                    if "_component_kubernetes_master" in node_svc:
                                        # e.g., node_svc="zldcdyh1adce3_kp_component_kubernetes_master"
                                        svc_with_my_clli_tags = node_svc
                                        break
                    except:
                        pass
                    # ... cache results we find into runtime properties to avoid searching again
                    if svc_with_my_clli_tags:
                        CfyClient._logger.debug("{} {} storing runtime property svc_with_my_clli_tags={}".format(
                            node_instance.deployment_id, node_instance.id, svc_with_my_clli_tags))
                        rtp['svc_with_my_clli_tags'] = svc_with_my_clli_tags
                        body = {
                                 "runtime_properties": rtp,
                                 "state": node_instance.state,
                                 "version": 1 + int(node_instance.version)
                               }
                        try:
                            CfyClient._client.update_node_instance(node_instance.id, body)
                        except:
                            pass

                if not svc_with_my_clli_tags:
                    CfyClient._logger.debug("{} {} runtime_properties has no svc_with_my_clli_tags".format(node_instance.deployment_id, node_instance.id))
                    continue

                # get the nodeport for statefulset sidecar service
                dti_info = rtp.get('dti_info')
                if dti_info:
                    ports = dti_info.get('ports')
                    if ports:
                        scn_port = ports[0].split(':')[1]
                docker_host = rtp.get('configuration',{}).get('file_content')
                if not docker_host:
                    CfyClient._logger.debug("{} {} runtime_properties has no configuration.file_content".format(node_instance.deployment_id, node_instance.id))
                    continue
    
            # If DTI Event specifies dcae_service_location, then collector's dockerhost service in Consul must have that TAG
            if dcae_service_location:
                if container_type == "docker" and docker_host not in dockerhosts:
                    CfyClient._logger.debug("{} {} dockerhost {} is not TAGged with DTI Event dcae_service_location {}"
                        .format(node_instance.deployment_id, node_instance.id, docker_host, dcae_service_location))
                    continue
                elif container_type == "k8s" and svc_with_my_clli_tags not in k8s_svcs_tagged_with_clli:
                    CfyClient._logger.debug("{} {} svc_with_my_clli_tags {} is not TAGged with DTI Event dcae_service_location {}"
                        .format(node_instance.deployment_id, node_instance.id, svc_with_my_clli_tags, dcae_service_location))
                    continue
    
            # If DTI Event specifies component_type, then collector's service_component_type must match
            if component_type:
                c_component_type = rtp.get('service_component_type')
                if component_type != c_component_type:
                    CfyClient._logger.debug("{} {} component_types don't match".format(node_instance.deployment_id, node_instance.id))
                    continue
    
            # Check if the collector supports this VNF Type
            # scn:dti Consul key is authoritative for vnfTypes that a collector supports (not docker_config)
            dti_key = scn + ':dti'
            try:
                obj = ConsulClient.get_value(dti_key)
            except Exception as e:
                CfyClient._logger.error(
                    "Unexpected exception {}: {!s} from ConsulClient.get_value({}) for {} {}"
                    .format(type(e).__name__, e, dti_key, node_instance.deployment_id, node_instance.id)
                )
                continue
            if not obj:
                CfyClient._logger.debug("{} {} Consul key '{}' is empty or invalid".format(node_instance.deployment_id, node_instance.id, dti_key))
                continue
            obj_types = set(k.lower() for k in obj)
            if dcae_target_type.lower() in obj_types:
                CfyClient._logger.debug("{} {} is a valid collector for VNF Type {}".format(node_instance.deployment_id, node_instance.id, dcae_target_type))
                cnt_found += 1
                yield (scn, node_instance.deployment_id, container_id, node_instance.node_id, node_instance.id, node_instance.state, docker_host, dti_reconfig_script, container_type, scn_address, scn_port )
                continue
            else:
                CfyClient._logger.debug("{} {} VNF Type {} is not in Consul key '{}'".format(node_instance.deployment_id, node_instance.id, dcae_target_type, dti_key))
                continue
    
        msg = "Found {} components (collectors) for dcae_target_type={}, dcae_service_location={}, component_type={}"\
              .format(cnt_found, dcae_target_type, dcae_service_location, component_type)
        CfyClient._logger.debug(msg)

    @staticmethod
    def iter_components_for_docker(dcae_target_type, dcae_service_location='', component_type=''):
        """
        Iterate components that handle a given dcae_target_type to find the components of docker type

        Parameters
        ----------
        dcae_target_type : string
            VNF Type
        dcae_service_location : string
            Location of the component (optional)
        component_type : string
            Type of the component (optional)

        Returns
        -------
        A generator of tuples of component information
           [ (scn, deployment_id, container_id, node_id, node_instance_id, node_instance_state, docker_host, reconfig_script, "docker"), ... ]

        """

        cnt_found = 0
        # get dockerhost and kubernetes_master services that are TAGged for the dcae_service_location (CLLI)
        dockerhosts = []

        if dcae_service_location:
            try:
                dockerhosts = ConsulClient.search_services("-component-dockerhost-", [dcae_service_location])
            except Exception as e:
                msg = "Unexpected exception {}: {!s} from ConsulClient.search_services({}, {!s})".format(
                    type(e).__name__, e, "-component-dockerhost-", [dcae_service_location])
                CfyClient._logger.error(msg)
                raise CfyClientConsulError(msg)

        CfyClient.__set_cloudify_manager_client()
        for node_instance in CfyClient._client.node_instances.list(_sort="deployment_id"):
            rtp = node_instance.runtime_properties

            # Skip this node_instance if it is not a collector
            container_type = "docker"
            container_id = rtp.get('container_id')
            if not container_id:
                if not container_id:
                    CfyClient._logger.debug("{} {} runtime_properties has no container_id".format(
                        node_instance.deployment_id, node_instance.id))
                    continue
            docker_config = rtp.get('docker_config')
            if not docker_config:
                CfyClient._logger.debug(
                    "{} {} runtime_properties has no docker_config".format(node_instance.deployment_id,
                                                                           node_instance.id))
                continue
            dti_reconfig_script = ""
            dti_reconfig_script = rtp.get('dti_reconfig_script')
            if not dti_reconfig_script:
                CfyClient._logger.debug(
                    "{} {} runtime_properties has no dti_reconfig_script".format(node_instance.deployment_id,
                                                                                 node_instance.id))
                continue
            scn = rtp.get('service_component_name')
            if not scn:
                CfyClient._logger.debug(
                    "{} {} runtime_properties has no service_component_name".format(node_instance.deployment_id,
                                                                                    node_instance.id))
                continue
            docker_host = rtp.get('selected_container_destination')
            if not docker_host:
                CfyClient._logger.debug("{} {} runtime_properties has no selected_container_destination".format(
                    node_instance.deployment_id, node_instance.id))
                continue

            # If DTI Event specifies dcae_service_location, then collector's dockerhost service in Consul must have that TAG
            if dcae_service_location:
                if docker_host not in dockerhosts:
                    CfyClient._logger.debug("{} {} dockerhost {} is not TAGged with DTI Event dcae_service_location {}"
                                            .format(node_instance.deployment_id, node_instance.id, docker_host,
                                                    dcae_service_location))
                    continue

            # If DTI Event specifies component_type, then collector's service_component_type must match
            if component_type:
                c_component_type = rtp.get('service_component_type')
                if component_type != c_component_type:
                    CfyClient._logger.debug(
                        "{} {} component_types don't match".format(node_instance.deployment_id, node_instance.id))
                    continue

            # Check if the collector supports this VNF Type
            # scn:dti Consul key is authoritative for vnfTypes that a collector supports (not docker_config)
            dti_key = scn + ':dti'
            try:
                obj = ConsulClient.get_value(dti_key)
            except Exception as e:
                CfyClient._logger.error(
                    "Unexpected exception {}: {!s} from ConsulClient.get_value({}) for {} {}"
                        .format(type(e).__name__, e, dti_key, node_instance.deployment_id, node_instance.id)
                )
                continue
            if not obj:
                CfyClient._logger.debug(
                    "{} {} Consul key '{}' is empty or invalid".format(node_instance.deployment_id, node_instance.id,
                                                                       dti_key))
                continue
            obj_types = set(k.lower() for k in obj)
            if dcae_target_type.lower() in obj_types:
                CfyClient._logger.debug(
                    "{} {} is a valid collector for VNF Type {}".format(node_instance.deployment_id, node_instance.id,
                                                                        dcae_target_type))
                cnt_found += 1
                yield (scn, node_instance.deployment_id, container_id, node_instance.node_id, node_instance.id,
                       node_instance.state, docker_host, dti_reconfig_script, container_type, '', '')
                continue
            else:
                CfyClient._logger.debug(
                    "{} {} VNF Type {} is not in Consul key '{}'".format(node_instance.deployment_id, node_instance.id,
                                                                         dcae_target_type, dti_key))
                continue

        msg = "Found {} components (collectors) for dcae_target_type={}, dcae_service_location={}, component_type={}" \
            .format(cnt_found, dcae_target_type, dcae_service_location, component_type)
        CfyClient._logger.debug(msg)


    @staticmethod
    def iter_components_of_deployment(deployment_id, node_id=None, reconfig_type="app"):
        """
        Iterate components of a specific deployment_id.

        Parameters
        ----------
        deployment_id : string
            Cloudify deployment ID that created the component(s).
        node_id : string
            Cloudify node ID that created the component.
        reconfig_type : string
            "app"
    
        Returns
        -------
        A generator of tuples of component information
           [ (scn, deployment_id, container_id, node_id, node_instance_id, node_instance_state, docker_host, reconfig_script, "docker"), ... ]
        or
           [ (scn, deployment_id, k8s_deployment, node_id, node_instance_id, node_instance_state, config_content, reconfig_script, "k8s"), ... ]

        """

        cnt_found = 0

        CfyClient.__set_cloudify_manager_client()
        for node_instance in CfyClient._client.node_instances.list(
                deployment_id=deployment_id,
                _include=['id','node_id','deployment_id','state','runtime_properties']
            ):
            if node_id and node_instance.node_id != node_id:
                continue

            rtp = node_instance.runtime_properties
    
            # Skip this node_instance if it is not a collector
            container_type = "docker"
            container_id = rtp.get('container_id')
            if not container_id:
                container_type = "k8s"
                container_id = rtp.get('k8s_deployment')
                if not container_id:
                    CfyClient._logger.debug("{} {} runtime_properties has no container_id or k8s_deployment".format(node_instance.deployment_id, node_instance.id))
                    continue
            reconfig_script = rtp.get('docker_config',{}).get('reconfigs',{}).get(reconfig_type)
            if not reconfig_script:
                CfyClient._logger.debug("{} {} runtime_properties has no docker_config.reconfigs.{}".format(node_instance.deployment_id, node_instance.id, reconfig_type))
                continue
            scn = rtp.get('service_component_name')
            if not scn:
                CfyClient._logger.debug("{} {} runtime_properties has no service_component_name".format(node_instance.deployment_id, node_instance.id))
                continue
            if container_type == "docker":
                docker_host = rtp.get('selected_container_destination')
                if not docker_host:
                    CfyClient._logger.debug("{} {} runtime_properties has no selected_container_destination".format(node_instance.deployment_id, node_instance.id))
                    continue
            elif container_type == "k8s":
                docker_host = rtp.get('configuration',{}).get('file_content')
                if not docker_host:
                    CfyClient._logger.debug("{} {} runtime_properties has no configuration.file_content".format(node_instance.deployment_id, node_instance.id))
                    continue
    
            CfyClient._logger.debug("{} {} is a {}-reconfigurable component".format(node_instance.deployment_id, node_instance.id, reconfig_type))
            cnt_found += 1
            yield (scn, node_instance.deployment_id, container_id, node_instance.node_id, node_instance.id, node_instance.state, docker_host, reconfig_script, container_type)
            continue
    
        msg = "Found {} {}-reconfigurable components".format(cnt_found, reconfig_type)
        CfyClient._logger.debug(msg)
