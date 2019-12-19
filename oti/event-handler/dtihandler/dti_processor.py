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

"""DTI Event processor for handling all the event types"""
import copy
import json
import logging
from multiprocessing.dummy import Pool as ThreadPool
from threading import Lock

import requests

from dtihandler import utils
from dtihandler.cfy_client import CfyClient
from dtihandler.consul_client import ConsulClient
from dtihandler.dbclient.apis import EventDbAccess
from dtihandler.dbclient.models import Event, EventAck
from dtihandler.docker_client import DockerClient

notify_response_arr = []
lock = Lock()
K8S_CLUSTER_PROXY_NODE_PORT = '30132'


def notify_docker(args_tuple):
    """
    event notification executor inside a process pool to communicate with docker container
    interacts with docker client library
    """
    (dti_event, db_access, ack_item) = args_tuple
    try:
        dcae_service_action = dti_event.get('dcae_service_action')
        component_scn = ack_item.service_component
        deployment_id = ack_item.deployment_id
        container_id = ack_item.container_id
        docker_host = ack_item.docker_host
        reconfig_script = ack_item.reconfig_script
        container_type = 'docker'
    except Exception as e:
        return (
            "ERROR", "dti_processor.notify_docker processing args got exception {}: {!s}".format(type(e).__name__, e))
    what = ""
    try:
        what = "{} in {} container {} on {} that was deployed by {}".format(
            reconfig_script, container_type, container_id, docker_host, deployment_id)
        if dcae_service_action == 'add':
            add_action = {"dcae_service_action": "deploy"}
            dti_event.update(add_action)

        if dcae_service_action == 'delete':
            add_action = {"dcae_service_action": "undeploy"}
            dti_event.update(add_action)

        # dkr = DockerClient(docker_host, reauth=False)
        result = ''
        # result = dkr.notify_for_reconfiguration(container_id, [ reconfig_script, "dti", json.dumps(dti_event) ])
        if dti_event.get('dcae_service_action') == 'undeploy':
            # delete from dti_event_ack table
            try:
                db_access.deleteDomainObject(ack_item)
            except Exception as e:
                msg = "trying to delete event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                DTIProcessor.logger.warn(msg)
                return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
            else:
                return (component_scn, "ran {}, got: {!s}".format(what, result))

    except Exception as e:
        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))


def notify_svc(args_tuple):
    """
    add/update/delete event handler
    event notification executor inside a process pool to communicate with docker container and k8s services
    interacts with docker client library
    interacts with k8s node port services using REST client
    """
    (orig_dti_event, db_access, curr_evt, res_tuple) = args_tuple
    dti_event = copy.deepcopy(orig_dti_event)
    try:
        dcae_service_action = dti_event.get('dcae_service_action').lower()

        component_scn = res_tuple[0]
        deployment_id = res_tuple[1]
        container_id = res_tuple[2]
        node_id = res_tuple[3]
        docker_host = res_tuple[6]
        reconfig_script = res_tuple[7]
        container_type = res_tuple[8]
    except Exception as e:
        return ("ERROR", "dti_processor.notify processing args got exception {}: {!s}".format(type(e).__name__, e))

    what = ""
    if container_type == "docker":
        # exec reconfigure.sh in docker container
        try:
            what = "{} in {} container {} on {} that was deployed by {} node {}".format(
                reconfig_script, container_type, container_id, docker_host, deployment_id, node_id)
            if dcae_service_action == 'add':
                add_action = {"dcae_service_action": "deploy"}
                dti_event.update(add_action)

            if dcae_service_action == 'delete':
                add_action = {"dcae_service_action": "undeploy"}
                dti_event.update(add_action)

            dkr = DockerClient(docker_host, reauth=False)
            result = ''
            if dti_event.get('dcae_service_action') == 'update':
                # undeploy + deploy
                DTIProcessor.logger.debug("update 1 - running undeploy {}".format(what))
                dti_event.update({"dcae_service_action": "undeploy"})
                result = dkr.notify_for_reconfiguration(container_id, [reconfig_script, "dti", json.dumps(dti_event)])
                DTIProcessor.logger.debug("update 2 - running deploy {}".format(what))
                dti_event.update({"dcae_service_action": "deploy"})
                result = dkr.notify_for_reconfiguration(container_id, [reconfig_script, "dti", json.dumps(dti_event)])
                try:
                    upd_evt_ack = db_access.query_event_info_docker(curr_evt, component_scn, deployment_id,
                                                                    container_id)
                    upd_evt_ack.update_action('update')
                    db_access.saveDomainObject(upd_evt_ack)
                except Exception as e:
                    msg = "trying to update event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                    DTIProcessor.logger.warn(msg)
                    return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
            else:
                DTIProcessor.logger.debug("running {}".format(what))
                result = dkr.notify_for_reconfiguration(container_id, [reconfig_script, "dti", json.dumps(dti_event)])
                if dti_event.get('dcae_service_action') == 'deploy':
                    # add into dti_event_ack table
                    try:
                        add_evt_ack = EventAck(service_component=component_scn, deployment_id=deployment_id,
                                               container_type='docker', docker_host=docker_host,
                                               container_id=container_id, reconfig_script=reconfig_script,
                                               event=curr_evt,
                                               action='add')
                        db_access.saveDomainObject(add_evt_ack)
                    except Exception as e:
                        msg = "trying to store event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                        DTIProcessor.logger.warn(msg)
                        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
                else:
                    # remove from dtih_event_ack if present
                    try:
                        del_evt_ack = db_access.query_event_info_docker(curr_evt, component_scn, deployment_id,
                                                                        container_id)
                        db_access.deleteDomainObject(del_evt_ack)
                    except Exception as e:
                        msg = "trying to delete event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                        DTIProcessor.logger.warn(msg)
                        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
        except Exception as e:
            return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))

        return (component_scn, "ran {}, got: {!s}".format(what, result))
    elif container_type == "k8s":
        DTIProcessor.logger.debug("DTIProcessor.notify_svc() handling k8s component")
        # if action is 'update', check if k8s pod info exists already for this event in app db
        if dcae_service_action == 'add':
            DTIProcessor.logger.debug("DTIProcessor.notify_svc() in k8s for add action")
            return notify_k8s((dti_event, db_access, curr_evt, res_tuple))
        else:
            # handle update for pods being tracked and handle add for new pods
            k8s_scn_result = db_access.query_event_data_k8s_pod(curr_evt, component_scn)
            if k8s_scn_result is not None:
                # update
                DTIProcessor.logger.debug("DTIProcessor.notify_svc() in k8s for update action")
                return notify_k8s_pod((dti_event, db_access, k8s_scn_result))
            else:
                # add
                DTIProcessor.logger.debug("DTIProcessor.notify_svc(), convert update to add action in k8s ")
                add_action = {"dcae_service_action": "add"}
                dti_event.update(add_action)
                return notify_k8s((dti_event, db_access, curr_evt, res_tuple))


def notify_k8s(args_tuple):
    """
    add event handler
    event notification executor inside a process pool to communicate with k8s statefulset nodeport service
    uses REST API client to call k8s services
    """
    (dti_event, db_access, curr_evt, res_tuple) = args_tuple
    component_scn = res_tuple[0]
    deployment_id = res_tuple[1]
    node_id = res_tuple[3]
    container_type = res_tuple[8]
    service_address = res_tuple[9]
    service_port = res_tuple[10]
    what = "{} in {} deployment {} that was deployed by {} node {}".format(
        "add", container_type, "statefulset", deployment_id, node_id)
    # call scn node port service REST API
    svc_nodeport_url = "https://{}:{}".format(service_address, service_port)
    try:
        DTIProcessor.logger.debug("running {}".format(what))
        response = requests.put(svc_nodeport_url, json=dti_event, timeout=50)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        msg = "collector nodeport service({}) threw exception {}: {!s}".format(
            svc_nodeport_url, type(e).__name__, e)
        DTIProcessor.logger.error(msg)
        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
    try:
        event_ack_info = response.json()
    except Exception as e:
        msg = "collector nodeport service({}) threw exception {}: {!s}".format(
            svc_nodeport_url, type(e).__name__, e)
        DTIProcessor.logger.error(msg)
        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))

    if not event_ack_info:
        msg = "collector nodeport service returned bad data"
        DTIProcessor.logger.error(msg)
        return (component_scn, "collector nodeport service returned bad data")

    namespace = event_ack_info.get("KubeNamespace")
    svc_name = event_ack_info.get("KubeServiceName")
    svc_port = event_ack_info.get("KubeServicePort")
    proxy_fqdn = event_ack_info.get("KubeProxyFqdn")
    cluster_fqdn = event_ack_info.get("KubeClusterFqdn")
    pod_name = event_ack_info.get("KubePod")
    statefulset = pod_name[0:pod_name.rindex('-')]

    what = "{} in {} deployment {} in namespace {} that was deployed by {} node {}".format(
        "add", container_type, statefulset, namespace, deployment_id, node_id)
    try:
        add_evt_ack = EventAck(k8s_namespace=namespace, k8s_service_name=svc_name, deployment_id=deployment_id,
                               k8s_service_port=svc_port, k8s_cluster_fqdn=cluster_fqdn, k8s_proxy_fqdn=proxy_fqdn,
                               k8s_pod_id=pod_name, event=curr_evt, action='add', container_type='k8s',
                               service_component=component_scn)
        db_access.saveDomainObject(add_evt_ack)
        return (component_scn, "ran {}, got: {!s}".format(what, event_ack_info))
    except Exception as e:
        msg = "trying to store event ack record, got exception {}: {!s}".format(type(e).__name__, e)
        DTIProcessor.logger.warn(msg)
        return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))


def notify_pods(args_tuple):
    """
    notify event handler
    event notification executor inside a process pool to communicate with k8s DTIH proxy nodeport service
    uses REST API client to call k8s services
    """
    event_ack_info = ''
    (dti_event, res_tuple) = args_tuple
    try:
        pod_id = res_tuple[0]
        namespace = res_tuple[1]
        cluster = res_tuple[2]
        port = K8S_CLUSTER_PROXY_NODE_PORT
        svc_name = res_tuple[3]
        svc_port = res_tuple[4]
        item_pod_url = "https://{}:{}/{}/{}?service_name={}&service_port={}".format(cluster, port, namespace,
                                                                                    pod_id, svc_name,
                                                                                    svc_port)
        what = "{} for pod id {} in cluster {} and  namespace {}".format("notify", pod_id, cluster, namespace)
        try:
            DTIProcessor.logger.debug("running {}".format(what))
            response = requests.put(item_pod_url, json=dti_event, timeout=50)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            msg = "stateful set proxy service({}) threw exception {}: {!s}".format(
                item_pod_url, type(e).__name__, e)
            DTIProcessor.logger.error(msg)
            with lock:
                notify_response_arr.append((pod_id, "exception {}: {!s} running {}".format(type(e).__name__, e, what)))
        else:
            try:
                event_ack_info = response.json()
            except Exception as e:
                msg = "stateful set proxy service({}) threw exception {}: {!s}".format(
                    item_pod_url, type(e).__name__, e)
                DTIProcessor.logger.error(msg)
                with lock:
                    notify_response_arr.append(
                        (pod_id, "exception {}: {!s} running {}".format(type(e).__name__, e, what)))

            if not event_ack_info:
                msg = "stateful set proxy service returned bad data"
                DTIProcessor.logger.error(msg)
                # return (pod_id, "no acknowledgement - running {}".format(what))
                with lock:
                    notify_response_arr.append((pod_id, "no acknowledgement - running {}".format(what)))

            with lock:
                notify_response_arr.append((pod_id, "ran {}, got: {!s}".format(what, event_ack_info)))

    except Exception as e:
        with lock:
            notify_response_arr.append(
                ("ERROR", "dti_processor.notify() processing args got exception {}: {!s}".format(type(e).__name__, e)))


def notify_k8s_pod(args_tuple):
    """
    update event handler
    event notification executor inside a process pool to communicate with k8s DTIH proxy service
    uses REST API client to call k8s services
    """
    item_pod_url = ''
    component_scn = ''
    (dti_event, db_access, ack_item) = args_tuple
    # call ingress proxy to dispatch delete event

    action = dti_event.get('dcae_service_action')
    what = "{} to {} ID {} in namespace {} that was deployed in cluster {}".format(
        action, 'k8s pod', ack_item.k8s_pod_id, ack_item.k8s_namespace, ack_item.k8s_cluster_fqdn)
    try:
        DTIProcessor.logger.debug("running {}".format(what))
        item_pod_url = "https://{}:{}/{}/{}?service_name={}&service_port={}".format(
            ack_item.k8s_proxy_fqdn, K8S_CLUSTER_PROXY_NODE_PORT, ack_item.k8s_namespace,
            ack_item.k8s_pod_id, ack_item.k8s_service_name, ack_item.k8s_service_port)
        component_scn = ack_item.service_component
        response = requests.put(item_pod_url, json=dti_event, timeout=50)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        msg = "exception occured, stateful set proxy service({}) threw {}: {!s}".format(
            item_pod_url, type(e).__name__, e)
        DTIProcessor.logger.error(msg)
        return (component_scn, "ran {}, got: {!s}".format(what, msg))
    else:
        if action == 'delete':
            try:
                db_access.deleteDomainObject(ack_item)
            except Exception as e:
                msg = "trying to delete event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                DTIProcessor.logger.warn(msg)
                return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))
        else:
            try:
                ack_item.update_action('update')
                db_access.saveDomainObject(ack_item)
            except Exception as e:
                msg = "trying to update event ack record, got exception {}: {!s}".format(type(e).__name__, e)
                DTIProcessor.logger.warn(msg)
                return (component_scn, "exception {}: {!s} running {}".format(type(e).__name__, e, what))

    return (component_scn, "ran {}, got: {!s}".format(what, response.json()))


class DTIProcessor(object):
    """
    Main event processing class that encapsulates all the logic of this handler application!
    An instance of this class is created per incoming client request.

    Generates input data by querying platform services - cloudify, consul, postgresSql

    It creates a pool of worker processes using a multiprocessing Pool class instance.
    Tasks are offloaded to the worker processes that exist in the pool.
    The input data is distributed across processes of the Pool object to enable parallel execution of
    event notification function across multiple input values (data parallelism).
    """

    logger = logging.getLogger("dti_handler.dti_processor")
    K8S_CLUSTER_PROXY_NODE_PORT = '30132'
    db_access = None
    docker_pool = None
    k8s_pool = None

    def __init__(self, dti_event, send_notification=True):
        self._result = {}
        self.event = dti_event
        self.is_notify = send_notification
        self.action = dti_event.get('dcae_service_action').lower()
        self.target_name = dti_event.get('dcae_target_name')
        self.target_type = dti_event.get('dcae_target_type', '').lower()
        self.event_clli = dti_event.get('dcae_service_location')
        res_dict = None
        try:
            self.docker_pool = ThreadPool(8)
            self.k8s_pool = ThreadPool(8)
        except Exception as e:
            msg = "DTIProcessor.__init__() creating ThreadPool got exception {}: {!s}".format(type(e).__name__, e)
            DTIProcessor.logger.error(msg)
            self._result['ERROR'] = msg
            raise e
        else:
            self.db_access = EventDbAccess()
            self.prim_db_event = None
            try:
                res_dict = self.dispatcher()
            except:
                raise
            finally:
                try:
                    self.docker_pool.close()
                    self.k8s_pool.close()
                except Exception as e:
                    msg = "DTIProcessor.__init__() running pool.close() got exception {}: {!s}".format(type(e).__name__,
                                                                                                       e)
                    DTIProcessor.logger.error(msg)
                    self._result['ERROR'] = msg
                try:
                    self.docker_pool.join()
                    self.k8s_pool.join()
                except Exception as e:
                    msg = "DTIProcessor.__init__() running pool.join() got exception {}: {!s}".format(type(e).__name__,
                                                                                                      e)
                    DTIProcessor.logger.error(msg)
                    self._result['ERROR'] = msg

            # if not send_notification:
            #     DTIProcessor._logger.debug("DTIProcessor.__init__() not notifying DCAE-Controller components")
            #     return

            if res_dict:
                try:
                    utils.update_dict(self._result, res_dict)
                except Exception as e:
                    msg = "DTIProcessor.__init__() running utils.update_dict() got exception {}: {!s}".format(
                        type(e).__name__, e)
                    DTIProcessor.logger.error(msg)
                    self._result['ERROR'] = msg

        DTIProcessor.logger.debug("DTIProcessor.__init__() done notifying new DCAE-Controller components")

    def dispatcher(self):
        """ dispatch method to execute specific method based on event type """

        arg = str(self.action)
        method = getattr(self, arg, lambda: "Invalid action")
        return method()

    def undeploy(self):
        """
        delete event from consul KV store, this functionality will be retired as events are stored
        in postgresSql dti database
        """
        global key
        try:
            # update Consul KV store with DTI Event - storing them in a folder for all components
            key = "{}/{}/{}".format("dti_events", self.target_type, self.target_name)
            result = ConsulClient.delete_key(key)
        except Exception as e:
            msg = "trying to delete Consul history key {}, got exception {}: {!s}".format(key, type(e).__name__, e)
            DTIProcessor.logger.warn(msg)
            self._result['WARNING'] = msg
        else:
            if not result:
                msg = "VNF instance {} was not in Consul dti_events historical folder".format(self.target_name)
                DTIProcessor.logger.warn(msg)
                self._result['WARNING'] = msg

    def deploy(self):
        """
        add event to consul KV store, this functionality will be retired as events are stored
        in postgresSql dti database
        """
        dep_key = "{}/{}/{}".format("dti_events", self.target_type, self.target_name)
        try:
            # update Consul KV store with DTI Event - storing them in a folder for all components
            result = ConsulClient.store_kvs({dep_key: self.event})
        except Exception as e:
            msg = "trying to store Consul history key {}, got exception {}: {!s}".format(key, type(e).__name__, e)
            DTIProcessor.logger.warn(msg)
            self._result['WARNING'] = msg

    def add(self):
        """
        process DTI event that contains a new VNF instance that has to be configured in the collector microservices
        """
        res_dict = None
        try:
            msg = "processing add event for {}/{}".format(self.target_type, self.target_name)
            DTIProcessor.logger.debug(msg)
            # insert add event into dtih_event table
            self.prim_db_event = Event(event=self.event, target_name=self.target_name, target_type=self.target_type,
                                       location_clli=self.event_clli)
            self.db_access.saveDomainObject(self.prim_db_event)
        except Exception as e:
            msg = "trying to store event, got exception {}: {!s}".format(type(e).__name__, e.args)
            DTIProcessor.logger.warn(msg)
            self._result['ERROR'] = msg
            raise Exception(msg)
        else:
            if self.is_notify:
                try:
                    # force the action to add, to avoid bad things later
                    add_action = {"dcae_service_action": "add"}
                    self.event.update(add_action)
                    # mock up data
                    mock_tp11 = (
                        "scn1_k8s", "k8s_deployment_id1", "k8s_container_id1", "k8s_node_id1", "k8s_node_instance_id1",
                        "node_instance_state", "k8s_host", "dti_reconfig_script", "k8s",
                        "dcae-kcdthp-site1-dyh1b-d1.ecomp.idns.cip.att.com", "30996")
                    mock_tp12 = ("scn1_docker", "docker_deployment_id1", "docker_container_id1", "docker_node_id1",
                                 "docker_node_instance_id1",
                                 "node_instance_state", "docker_host", "dti_reconfig_script", "docker",
                                 "dcae-kcdthp-site1-dyh1b-d1.ecomp.idns.cip.att.com", "30996")
                    # tpl_arr = []
                    # tpl_arr.append(mock_tp11)
                    # tpl_arr.append(mock_tp12)
                    # res_dict = dict(self.docker_pool.map(notify_svc, (((self.event, self.db_access, self.prim_db_event, tp) for tp in tpl_arr))))
                    res_dict = dict(self.docker_pool.map(notify_svc,
                                                         ((self.event, self.db_access, self.prim_db_event, tp) for tp in
                                                          CfyClient().iter_components(self.target_type,
                                                                                      dcae_service_location=self.event_clli))
                                                         ))
                except Exception as e:
                    msg = "DTIProcessor.__init__() running pool.map() got exception {}: {!s}".format(type(e).__name__,
                                                                                                     e)
                    DTIProcessor.logger.error(msg)
                    self._result['ERROR'] = msg
        return res_dict

    def add_replay(self):
        """
        convert an update event flow and replay as an add event type since the event acknowledgement is missing
        from application database
        """
        res_dict = None
        try:
            # force the action to add, to avoid bad things later
            add_action = {"dcae_service_action": "add"}
            self.event.update(add_action)
            # mock up data
            mock_tp11 = ("scn1_k8s", "k8s_deployment_id1", "k8s_container_id1", "k8s_node_id1", "k8s_node_instance_id1",
                         "node_instance_state", "k8s_host", "dti_reconfig_script", "k8s",
                         "dcae-kcdthp-site1-dyh1b-d1.ecomp.idns.cip.att.com", "30996")
            mock_tp12 = ("scn1_docker", "docker_deployment_id1", "docker_container_id1", "docker_node_id1",
                         "docker_node_instance_id1",
                         "node_instance_state", "docker_host", "dti_reconfig_script", "docker",
                         "dcae-kcdthp-site1-dyh1b-d1.ecomp.idns.cip.att.com", "30996")
            # tpl_arr = []
            # tpl_arr.append(mock_tp11)
            # tpl_arr.append(mock_tp12)
            # res_dict = dict(self.pool.map(notify_svc, (((self.event, self.db_access, self.prim_db_event, tp) for tp in tpl_arr))))
            res_dict = dict(self.docker_pool.map(notify_svc,
                                                 ((self.event, self.db_access, self.prim_db_event, tp) for tp in
                                                  CfyClient().iter_components(self.target_type,
                                                                              dcae_service_location=self.event_clli))
                                                 ))
        except Exception as e:
            msg = "DTIProcessor._add() running pool.map() got exception {}: {!s}".format(type(e).__name__, e)
            DTIProcessor.logger.error(msg)
            self._result['ERROR'] = msg
        return res_dict

    def delete(self):
        """
        process DTI event that indicates a VNF instance has to be removed from the collector microservices
        """
        res_dict = {}
        res_dict_k8s = {}
        res_dict_docker = {}

        if self.is_notify:
            try:
                self.prim_db_event = self.db_access.query_event_item(self.target_type, self.target_name)
                if self.prim_db_event is None:
                    msg = "processing delete event for {}/{}, but current event info is not found in database".format(
                        self.target_type, self.target_name)
                    DTIProcessor.logger.warn(msg)
                    self._result['WARNING'] = msg
                res_dict_docker = dict(self.docker_pool.map(notify_svc,
                                                            ((self.event, self.db_access, self.prim_db_event, tp) for tp
                                                             in CfyClient().iter_components_for_docker(self.target_type,
                                                                                                       dcae_service_location=self.event_clli))
                                                            ))
            except Exception as e:
                msg = "DTIProcessor.delete() running docker_pool.map() got exception {}: {!s}".format(type(e).__name__,
                                                                                                      e)
                DTIProcessor.logger.error(msg)
                self._result['ERROR'] = msg

            try:
                if self.prim_db_event is not None:
                    result = self.db_access.query_event_data_k8s(self.target_type, self.target_name)
                    res_dict_k8s = dict(self.k8s_pool.map(notify_k8s_pod, (
                        ((self.event, self.db_access, ack_item) for ack_item in result))))
            except Exception as e:
                msg = "DTIProcessor.delete() running k8s_pool.map() got exception {}: {!s}".format(type(e).__name__, e)
                DTIProcessor.logger.error(msg)
                self._result['ERROR'] = msg

            try:
                if self.prim_db_event is not None:
                    self.db_access.deleteDomainObject(self.prim_db_event)
            except Exception as e:
                msg = "trying to delete event, got exception {}: {!s}".format(type(e).__name__, e.args)
                DTIProcessor.logger.warn(msg)
                self._result['ERROR'] = msg

        if res_dict_k8s is not None:
            utils.update_dict(res_dict, res_dict_k8s)

        if res_dict_docker is not None:
            utils.update_dict(res_dict, res_dict_docker)

        return res_dict

    def update(self):
        """
        process DTI event that indicates VNF instance has to be updated in the collector microservices
        """
        res_dict = {}
        res_dict_k8s = {}
        res_dict_docker = {}

        if self.is_notify:
            try:
                self.prim_db_event = self.db_access.query_event_item(self.target_type, self.target_name)
                if self.prim_db_event is not None:
                    self.db_access.update_event_item(self.event, self.target_type, self.target_name)
                    result = self.db_access.query_event_data(self.target_type, self.target_name)
                    if len(result) == 0:
                        msg = "processing update event for {}/{}, but event distribution info is not found in database, " \
                              "replaying this event to cluster if required". \
                            format(self.target_type, self.target_name)
                        DTIProcessor.logger.warn(msg)
                        self._result['WARNING'] = msg
                        res_dict = self.add_replay()
                    else:
                        msg = "DTIProcessor.update() handle update flow for {}/{}, for k8s rediscover scn list and" \
                              "identify new vs update cases".format(self.target_type, self.target_name)
                        DTIProcessor.logger.debug(msg)
                        try:
                            tpl_arr = CfyClient().iter_components(self.target_type,
                                                                  dcae_service_location=self.event_clli)
                            res_dict_docker = dict(self.docker_pool.map(notify_svc,
                                                                        ((
                                                                            self.event, self.db_access,
                                                                            self.prim_db_event,
                                                                            tp)
                                                                            for tp in tpl_arr)))
                        except Exception as e:
                            msg = "DTIProcessor.update() running docker_pool.map() got exception {}: {!s}".format(
                                type(e).__name__, e)
                            DTIProcessor.logger.error(msg)
                            self._result['ERROR'] = msg
                else:
                    # event is new for the handler
                    msg = "processing update event for {}/{}, but current event info is not found in database, " \
                          "executing add event".format(self.target_type, self.target_name)
                    DTIProcessor.logger.warn(msg)
                    self._result['WARNING'] = msg
                    res_dict = self.add()
            except Exception as e:
                msg = "DTIProcessor.update() got exception {}: {!s}".format(type(e).__name__, e)
                DTIProcessor.logger.error(msg)
                self._result['ERROR'] = msg

        if res_dict_k8s is not None:
            utils.update_dict(res_dict, res_dict_k8s)

        if res_dict_docker is not None:
            utils.update_dict(res_dict, res_dict_docker)

        return res_dict

    def notify(self):
        """
        event handler to notify all the pods in the kubernetes cluster whose FQDN is present in the incoming event
        This notification is meant for the cluster failover.
        """
        res_dict = {}
        try:
            self.prim_db_event = self.db_access.query_event_item(self.target_type, self.target_name)
            if self.prim_db_event is not None:
                self.db_access.update_event_item(self.event, self.target_type, self.target_name)
            else:
                self.prim_db_event = Event(event=self.event, target_name=self.target_name, target_type=self.target_type,
                                           location_clli=self.event_clli)
                self.db_access.saveDomainObject(self.prim_db_event)
        except Exception as e:
            msg = "trying to store notify event, got exception {}: {!s}".format(type(e).__name__, e.args)
            DTIProcessor.logger.warn(msg)
            self._result['ERROR'] = msg

        try:
            self.k8s_pool.map(notify_pods, ((self.event, tp) for tp in
                                            CfyClient().query_k8_components(self.target_name)))
            for k, v in notify_response_arr:
                res_dict[k] = v
        except Exception as e:
            msg = "trying to run notify event, got exception {}: {!s}".format(type(e).__name__, e.args)
            DTIProcessor.logger.warn(msg)
            self._result['WARNING'] = msg

        return res_dict

    def get_result(self):
        return self._result

    @classmethod
    def get_k8_raw_events(cls, pod, cluster, namespace):
        """
        Get DTI events for a k8 stateful set pod container

        :param pod: required
            k8s stateful set pod ID that was configured with a specific set of DTI Events
        :param cluster: required
            k8s cluster FQDN where the mS was deployed
        :param namespace: required
            k8s namespace where the stateful set was deployed in that namespace
        :return:
            Dictionary of DTI event(s).
            DTI events will be keyed by vnf_type, sub-keyed by vnf_id.
        """
        db_access = EventDbAccess()
        results = db_access.query_raw_k8_events(cluster, pod, namespace)

        target_types = set([])
        outer_dict = {}

        for evnt_item in results:
            target_types.add(evnt_item.target_type)

        for targ_type in target_types:
            inner_name_evt_dict = {}
            for evnt in results:
                if targ_type == evnt.target_type:
                    inner_name_evt_dict[evnt.target_name] = evnt.event

            outer_dict[targ_type] = inner_name_evt_dict

        return outer_dict

    @classmethod
    def get_docker_raw_events(cls, service_name, service_location):
        """
        Get DTI events for docker container.

        Parameters
        ----------
        service_name : string
            required.  The service component name assigned by dockerplugin to the component that is unique to the
            cloudify node instance and used in its Consul key(s).
        service_location : string
            optional.  allows multiple values separated by commas.  Filters DTI events with dcae_service_location
            in service_location.
            If service_location is not provided, then defaults to dockerhost or k8s cluster master node service Consul
            TAGs if service_name is provided,
            otherwise results are not location filtered.

        Returns
        -------
        dict
            Dictionary of DTI event(s).
            DTI events will be keyed by vnf_type, sub-keyed by vnf_id.

        """

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
                                if "-component-dockerhost-" in node_svc:
                                    want_locs = services[node_svc].get("Tags", [])
                                    break
                except:
                    pass

            try:
                supported_types = ConsulClient.get_value(service_name + ":dti")
            except:
                return r_dict
            else:
                if supported_types:
                    supported_types = [t_type.lower() for t_type in list(supported_types.keys())]
                    give_types = supported_types
                if not give_types or (len(give_types) == 1 and give_types[0] == ''):
                    return r_dict

        db_access = EventDbAccess()
        results = db_access.query_raw_docker_events(give_types, want_locs)

        target_types = set([])
        outer_dict = {}

        for evnt_item in results:
            target_types.add(evnt_item.target_type)

        for targ_type in target_types:
            inner_name_evt_dict = {}
            for evnt in results:
                if targ_type == evnt.target_type:
                    inner_name_evt_dict[evnt.target_name] = evnt.event

            outer_dict[targ_type] = inner_name_evt_dict

        return outer_dict
