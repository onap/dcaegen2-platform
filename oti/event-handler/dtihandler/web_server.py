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

"""web-service for dti_handler"""

import json
import logging
import os
import time
from datetime import datetime

import cherrypy

from dtihandler.cbs_rest import CBSRest
from dtihandler.config import Config
from dtihandler.dti_processor import DTIProcessor
from dtihandler.onap.audit import Audit


class DTIWeb(object):
    """run REST API of DTI Handler"""

    logger = logging.getLogger("dti_handler.web_server")
    HOST_INADDR_ANY = ".".join("0"*4)

    @staticmethod
    def run_forever(audit):
        """run the web-server of DTI Handler forever"""

        cherrypy.config.update({"server.socket_host": DTIWeb.HOST_INADDR_ANY,
                                "server.socket_port": Config.wservice_port})

        protocol = "http"
        tls_info = ""
        if Config.tls_server_cert_file and Config.tls_private_key_file:
            tm_cert = os.path.getmtime(Config.tls_server_cert_file)
            tm_key  = os.path.getmtime(Config.tls_private_key_file)
            cherrypy.server.ssl_module = 'builtin'
            cherrypy.server.ssl_certificate = Config.tls_server_cert_file
            cherrypy.server.ssl_private_key = Config.tls_private_key_file
            if Config.tls_server_ca_chain_file:
                cherrypy.server.ssl_certificate_chain = Config.tls_server_ca_chain_file
            protocol = "https"
            tls_info = "cert: {} {} {}".format(Config.tls_server_cert_file,
                                               Config.tls_private_key_file,
                                               Config.tls_server_ca_chain_file)

        cherrypy.tree.mount(_DTIWeb(), '/')

        DTIWeb.logger.info(
            "%s with config: %s", audit.info("running dti_handler as {}://{}:{} {}".format(
                protocol, cherrypy.server.socket_host, cherrypy.server.socket_port, tls_info)),
            json.dumps(cherrypy.config))
        cherrypy.engine.start()

        # If HTTPS server certificate changes, exit to let kubernetes restart us
        if Config.tls_server_cert_file and Config.tls_private_key_file:
            while True:
                time.sleep(600)
                c_tm_cert = os.path.getmtime(Config.tls_server_cert_file)
                c_tm_key  = os.path.getmtime(Config.tls_private_key_file)
                if c_tm_cert > tm_cert or c_tm_key > tm_key:
                    DTIWeb.logger.info("cert or key file updated")
                    cherrypy.engine.stop()
                    cherrypy.engine.exit()
                    break


class _DTIWeb(object):
    """REST API of DTI Handler"""

    VALID_EVENT_TYPES = ['deploy', 'undeploy', 'add', 'delete', 'update', 'ndtify']

    @staticmethod
    def _get_request_info(request):
        """Returns info about the http request."""

        return "{} {}{}".format(request.method, request.script_name, request.path_info)


    #----- Common endpoint methods

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def healthcheck(self):
        """Returns healthcheck results."""

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)

        DTIWeb.logger.info("%s", req_info)

        result = Audit.health()

        DTIWeb.logger.info("healthcheck %s: result=%s", req_info, json.dumps(result))

        audit.audit_done(result=json.dumps(result))
        return result

    @cherrypy.expose
    def shutdown(self):
        """Shutdown the web server."""

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)

        DTIWeb.logger.info("%s: --- stopping REST API of DTI Handler ---", req_info)

        cherrypy.engine.exit()

        health = json.dumps(Audit.health())
        audit.info("dti_handler health: {}".format(health))
        DTIWeb.logger.info("dti_handler health: %s", health)
        DTIWeb.logger.info("%s: --------- the end -----------", req_info)
        result = str(datetime.now())
        audit.info_requested(result)
        return "goodbye! shutdown requested {}".format(result)

    # ----- DTI Handler mock endpoint methods
    @cherrypy.expose
    @cherrypy.tools.json_out()
    @cherrypy.tools.json_in()
    def mockevents(self):

        result = {"KubeNamespace":"com-my-dcae-test", "KubePod":"pod-0", "KubeServiceName":"pod-0.service.local", "KubeServicePort":"8880", "KubeClusterFqdn":"fqdn-1"}

        return result

    #----- DTI Handler endpoint methods

    @cherrypy.expose
    @cherrypy.tools.json_out()
    @cherrypy.tools.json_in()
    def events(self, ndtify="y"):
        """
        Run dti reconfig script in service component instances configured to accept the DTI Event.

        POST /events < <dcae_event>

        POST /events?ndtify="n" < <dcae_event>

        where <dcae_event> is the entire DTI Event passed as a JSON object and contains at least these top-level keys:
            dcae_service_action : string
                required, 'deploy' or 'undeploy'
            dcae_target_name : string
                required, VNF Instance ID
            dcae_target_type : string
                required, VNF Type of the VNF Instance
            dcae_service_location : string
                optional, CLLI location.  Not provided or '' infers all locations.

        Parameters
        ----------
        ndtify : string
            optional, default "y", any of these will not ndtify components: [ "f", "false", "False", "FALSE", "n", "no" ]
            When "n" will **not** ndtify components of this DTI Event update to Consul.

        Returns
        -------
        dict
            JSON object containing success or error of executing the dti reconfig script on
            each component instance's docker container, keyed by service_component_name.

        """

        if cherrypy.request.method != "POST":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        if DTIWeb.use_service_activator() and not ServiceActivator.is_site_active():
            cherrypy.response.status = 503
            return "503 Service Unavailable"

        dti_event = cherrypy.request.json or {}
        str_dti_event = json.dumps(dti_event)

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message="{}: {}".format(req_info, str_dti_event), \
            headers=cherrypy.request.headers)
        DTIWeb.logger.info("%s: dti_event=%s headers=%s", \
            req_info, str_dti_event, json.dumps(cherrypy.request.headers))

        dcae_service_action = dti_event.get('dcae_service_action')
        if not dcae_service_action:
            msg = 'dcae_service_action is missing'
            DTIWeb.logger.error(msg)
            raise cherrypy.HTTPError(400, msg)
        elif dcae_service_action.lower() not in self.VALID_EVENT_TYPES:
            msg = 'dcae_service_action is invalid'
            DTIWeb.logger.error(msg)
            raise cherrypy.HTTPError(400,msg)

        dcae_target_name = dti_event.get('dcae_target_name')
        if not dcae_target_name:
            msg = 'dcae_target_name is missing'
            DTIWeb.logger.error(msg)
            raise cherrypy.HTTPError(400, msg)

        dcae_target_type = dti_event.get('dcae_target_type', '')
        if not dcae_target_type:
            msg = 'dcae_target_type is missing'
            DTIWeb.logger.error(msg)
            raise cherrypy.HTTPError(400, msg)

        send_ndtification = True
        if (isinstance(ndtify, bool) and not ndtify) or \
           (isinstance(ndtify, str) and ndtify.lower() in [ "f", "false", "n", "no" ]):
            send_ndtification = False

        prc = DTIProcessor(dti_event, send_ndtification=send_ndtification)
        result = prc.get_result()

        DTIWeb.logger.info("%s: dti_event=%s result=%s", \
            req_info, str_dti_event, json.dumps(result))

        success, http_status_code, _ = audit.audit_done(result=json.dumps(result))
        if not success:
            cherrypy.response.status = http_status_code

        return result

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def dti_k8_events(self, **params):
        """
        Retrieve raw JSON events from application events database

        GET /dti_k8_events?pod=<sts-1>&namespace=<ns1>&cluster=<cluster1>

        Parameters
        ----------
        pod ID : string
            POD ID of the stateful set POD
        namespace: string
            kubernetes namespace
        cluster: string
            kubernetes cluster FQDN

        Returns
        -------
        dict
            JSON object containing the fully-bound configuration.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)

        pod = cherrypy.request.params['pod']
        namespace = cherrypy.request.params['namespace']
        cluster = cherrypy.request.params['cluster']

        return DTIProcessor.get_k8_raw_events(pod, cluster, namespace)

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def dti_docker_events(self, service, location=None):
        """
        Retrieve raw JSON events from application events database related to docker deployments

        GET /dti_docker_events?service=<svc>&location=<location>

        Parameters
        ----------
        service : string
            The service component name assigned by dockerplugin to the component
            that is unique to the cloudify node instance and used in its Consul key(s).
        location : string
            optional.  allows multiple values separated by commas.  Filters DTI events with dcae_service_location in service_location.
            If service_location is not provided, then defaults to dockerhost or k8s cluster master node service Consul TAGs if service_name is provided,
            otherwise results are not location filtered.

        Returns
        -------
        dict
            JSON object containing the fully-bound configuration.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)

        return DTIProcessor.get_docker_raw_events(service, location)


    #----- Config Binding Service (CBS) endpoint methods

    @cherrypy.expose
    @cherrypy.popargs('service_name')
    @cherrypy.tools.json_out()
    def service_component(self, service_name):
        """
        Retrieve fully-bound configuration for service_name from Consul KVs.

        GET /service_component/<service_name>

        Parameters
        ----------
        service_name : string
            The service component name assigned by dockerplugin to the component
            that is unique to the cloudify node instance and used in its Consul key(s).

        Returns
        -------
        dict
            JSON object containing the fully-bound configuration.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)
        DTIWeb.logger.info("%s: service_name=%s headers=%s", \
            req_info, service_name, json.dumps(cherrypy.request.headers))

        try:
            result = CBSRest.get_service_component(service_name)
        except Exception as e:
            result = {"ERROR": "exception {}: {!s}".format(type(e).__name__, e)}
            audit.set_http_status_code(404)

        DTIWeb.logger.info("%s: service_name=%s result=%s", \
            req_info, service_name, json.dumps(result))

        success, http_status_code, _ = audit.audit_done(result=json.dumps(result))
        if not success:
            cherrypy.response.status = http_status_code

        return result

    @cherrypy.expose
    @cherrypy.popargs('service_name')
    @cherrypy.tools.json_out()
    def service_component_all(self, service_name, service_location=None, policy_ids="y"):
        """
        Retrieve all information for service_name (config, dti, dti_events, and policies) from Consul KVs.

        GET /service_component_all/<service_name>

        GET /service_component_all/<service_name>?service_location=<service_location>

        GET /service_component_all/<service_name>?service_location=<service_location>;policy_ids=n

        Parameters
        ----------
        service_name : string
            The service component name assigned by dockerplugin to the component
            that is unique to the cloudify node instance and used in its Consul key(s).
        service_location : string
            optional, allows multiple values separated by commas.
            Filters DTI events with dcae_service_location in service_location.
        policy_ids : string
            optional, default "y", any of these will unset: [ "f", "false", "False", "FALSE", "n", "no" ]
            When unset, formats policies items as a list (without policy_ids) rather than as an object indexed by policy_id.

        Returns
        -------
        dict
            JSON object containing all information for component service_name.
            The top-level keys may include the following:
            config : dict
                The cloudify node's application_config property from when the start workflow was executed.
            dti : dict
                Keys are VNF Types that the component currently is assigned to monitor.  Policy can change them.
            dti_events : dict
                The latest deploy DTI events, keyed by VNF Type and sub-keyed by VNF Instance ID.
            policies : dict
                event : dict
                    Contains information about when the policies folder was last written.
                items : dict
                    Contains all policy bodies for the service_name component, keyed by policy_id.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)
        DTIWeb.logger.info("%s: service_name=%s headers=%s", \
            req_info, service_name, json.dumps(cherrypy.request.headers))

        policies_as_list = False
        if (isinstance(policy_ids, bool) and not policy_ids) or \
           (isinstance(policy_ids, str) and policy_ids.lower() in [ "f", "false", "n", "no" ]):
            policies_as_list = True
        try:
            result = CBSRest.get_service_component_all(service_name, service_location=service_location, policies_as_list=policies_as_list)
        except Exception as e:
            result = {"ERROR": "exception {}: {!s}".format(type(e).__name__, e)}
            audit.set_http_status_code(404)

        DTIWeb.logger.info("%s: service_name=%s result=%s", \
            req_info, service_name, json.dumps(result))

        success, http_status_code, _ = audit.audit_done(result=json.dumps(result))
        if not success:
            cherrypy.response.status = http_status_code

        return result

    @cherrypy.expose
    @cherrypy.popargs('service_name')
    @cherrypy.tools.json_out()
    def dti(self, service_name=None, vnf_type=None, vnf_id=None, service_location=None):
        """
        Retrieve current (latest, not undeployed) DTI events from Consul KVs.

        GET /dti/<service_name>

        GET /dti/<service_name>?vnf_type=<vnf_type>;vnf_id=<vnf_id>;service_location=<service_location>

        GET /dti

        GET /dti?vnf_type=<vnf_type>;vnf_id=<vnf_id>;service_location=<service_location>

        Parameters
        ----------
        service_name : string
            optional.  The service component name assigned by dockerplugin to the component
            that is unique to the cloudify node instance and used in its Consul key(s).
        vnf_type : string
            optional, allows multiple values separated by commas.  Gets DTI events for these vnf_type(s).
        vnf_id : string
            optional.  Requires vnf_type also.  Gets DTI event for this vnf_id.
        service_location : string
            optional, allows multiple values separated by commas.
            Filters DTI events with dcae_service_location in service_location.

        Returns
        -------
        dict
            Dictionary of DTI event(s).
            If one vnf_type and vnf_id are both specified, then object returned will be just the one DTI event.
            If one vnf_type is specified but not vnf_id, then DTI events will be keyed by vnf_id.
            Otherwise the DTI events will be keyed by vnf_type, sub-keyed by vnf_id.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)
        DTIWeb.logger.info("%s: service_name=%s headers=%s", \
            req_info, service_name, json.dumps(cherrypy.request.headers))

        try:
            result = CBSRest.get_dti(service_name=service_name, vnf_type=vnf_type, vnf_id=vnf_id, service_location=service_location)
        except Exception as e:
            result = {"ERROR": "exception {}: {!s}".format(type(e).__name__, e)}
            audit.set_http_status_code(404)

        DTIWeb.logger.info("%s: service_name=%s result=%s", \
            req_info, service_name, json.dumps(result))

        success, http_status_code, _ = audit.audit_done(result=json.dumps(result))
        if not success:
            cherrypy.response.status = http_status_code

        return result

    @cherrypy.expose
    @cherrypy.popargs('service_name')
    @cherrypy.tools.json_out()
    def policies(self, service_name, policy_id=None):
        """
        Retrieve policies for service_name from Consul KVs.

        GET /policies/<service_name>

        GET /policies/<service_name>?policy_id=<policy_id>

        Parameters
        ---------- 
        service_name : string
            The service component name assigned by dockerplugin to the component
            that is unique to the cloudify node instance and used in its Consul key(s).
        policy_id : string
            optional.  Limits returned policy to this policy_id.

        Returns
        -------
        dict
            JSON object containing policy bodies for the service_name component.
            If policy_id is specified, then object returned will be just the one policy body.
            If policy_id is not specified, then object will contain all policy bodies, keyed by policy_id.

        """

        if cherrypy.request.method != "GET":
            raise cherrypy.HTTPError(404, "unexpected method {}".format(cherrypy.request.method))

        req_info = _DTIWeb._get_request_info(cherrypy.request)
        audit = Audit(req_message=req_info, headers=cherrypy.request.headers)
        DTIWeb.logger.info("%s: service_name=%s headers=%s", \
            req_info, service_name, json.dumps(cherrypy.request.headers))

        try:
            result = CBSRest.get_policies(service_name, policy_id=policy_id)
        except Exception as e:
            result = {"ERROR": "exception {}: {!s}".format(type(e).__name__, e)}
            audit.set_http_status_code(404)

        DTIWeb.logger.info("%s: service_name=%s result=%s", \
            req_info, service_name, json.dumps(result))

        success, http_status_code, _ = audit.audit_done(result=json.dumps(result))
        if not success:
            cherrypy.response.status = http_status_code

        return result
