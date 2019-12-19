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

"""read and use the config"""

import copy
import json
import logging
import logging.config
import os

from dtihandler.consul_client import ConsulClient

logging.basicConfig(
    filename='logs/dti_handler.log', \
    format='%(asctime)s.%(msecs)03d %(levelname)+8s ' + \
           '%(threadName)s %(name)s.%(funcName)s: %(message)s', \
    datefmt='%Y%m%d_%H%M%S', level=logging.DEBUG)

class Config(object):
    """main config of the application"""

    CONFIG_FILE_PATH = "etc/config.json"
    LOGGER_CONFIG_FILE_PATH = "etc/common_logger.config"
    SERVICE_NAME = "dti_handler"

    FIELD_SYSTEM = "system"
    FIELD_WSERVICE_PORT = "wservice_port"
    FIELD_TLS = "tls"

    _logger = logging.getLogger("dti_handler.config")
    config = None

    cloudify_proto = None
    cloudify_addr = None
    cloudify_port = None
    cloudify_user = None
    cloudify_pass = None
    cloudify = None
    consul_url = "http://consul:8500"
    tls_cacert_file = None
    tls_server_cert_file = None
    tls_private_key_file = None
    tls_server_ca_chain_file = None
    wservice_port = 9443

    @staticmethod
    def _get_tls_file_path(tls_config, cert_directory, tls_name):
        """calc file path and verify its existance"""

        file_name = tls_config.get(tls_name)
        if not file_name:
            return None
        tls_file_path = os.path.join(cert_directory, file_name)
        if not os.path.isfile(tls_file_path) or not os.access(tls_file_path, os.R_OK):
            Config._logger.error("invalid %s: %s", tls_name, tls_file_path)
            return None
        return tls_file_path

    @staticmethod
    def _set_tls_config(tls_config):
        """verify and set tls certs in config"""

        try:
            Config.tls_cacert_file = None
            Config.tls_server_cert_file = None
            Config.tls_private_key_file = None
            Config.tls_server_ca_chain_file = None

            if not (tls_config and isinstance(tls_config, dict)):
                Config._logger.info("no tls in config: %s", json.dumps(tls_config))
                return

            cert_directory = tls_config.get("cert_directory")

            if not (cert_directory and isinstance(cert_directory, str)):
                Config._logger.warning("unexpected tls.cert_directory: %r", cert_directory)
                return

            cert_directory = os.path.join(
                os.path.dirname(os.path.dirname(os.path.realpath(__file__))), str(cert_directory))
            if not (cert_directory and os.path.isdir(cert_directory)):
                Config._logger.warning("ignoring invalid cert_directory: %s", cert_directory)
                return

            Config.tls_cacert_file = Config._get_tls_file_path(tls_config, cert_directory, "cacert")
            Config.tls_server_cert_file = Config._get_tls_file_path(tls_config, cert_directory,
                                                                    "server_cert")
            Config.tls_private_key_file = Config._get_tls_file_path(tls_config, cert_directory,
                                                                    "private_key")
            Config.tls_server_ca_chain_file = Config._get_tls_file_path(tls_config, cert_directory,
                                                                        "server_ca_chain")

        finally:
            Config._logger.info("tls_cacert_file = %s", Config.tls_cacert_file)
            Config._logger.info("tls_server_cert_file = %s", Config.tls_server_cert_file)
            Config._logger.info("tls_private_key_file = %s", Config.tls_private_key_file)
            Config._logger.info("tls_server_ca_chain_file = %s", Config.tls_server_ca_chain_file)

    @staticmethod
    def merge(new_config):
        """merge the new_config into current config - override the values"""

        if not new_config:
            return

        if not Config.config:
            Config.config = new_config
            return

        new_config = copy.deepcopy(new_config)
        Config.config.update(new_config)

    @staticmethod
    def get_system_name():
        """find the name of the dti_handler system
        to be used as the key in consul-kv for config of dti_handler
        """

        return (Config.config or {}).get(Config.FIELD_SYSTEM, Config.SERVICE_NAME)

    @staticmethod
    def discover():
        """bring and merge the config settings from Consul"""

        system_key = Config.get_system_name()
        new_config = ConsulClient.get_value(system_key)

        if not new_config or not isinstance(new_config, dict):
            Config._logger.warn("unexpected config from Consul: %s", new_config)
            return

        Config._logger.debug("loaded config from Consul(%s): %s",
            system_key, json.dumps(new_config))
        Config._logger.debug("config before merge from Consul: %s", json.dumps(Config.config))
        Config.merge(new_config.get(Config.SERVICE_NAME))
        Config._logger.debug("merged config from Consul: %s", json.dumps(Config.config))

    @staticmethod
    def load_from_file(file_path=None):
        """read and store the config from config file"""

        if not file_path:
            file_path = Config.CONFIG_FILE_PATH

        loaded_config = None
        if os.access(file_path, os.R_OK):
            with open(file_path, 'r') as config_json:
                loaded_config = json.load(config_json)

        if not loaded_config:
            Config._logger.info("config not loaded from file: %s", file_path)
            return

        Config._logger.info("config loaded from file: %s", file_path)
        logging_config = loaded_config.get("logging")
        if logging_config:
            logging.config.dictConfig(logging_config)

        Config.wservice_port = loaded_config.get(Config.FIELD_WSERVICE_PORT, Config.wservice_port)

        local_config = loaded_config.get(Config.SERVICE_NAME, {})
        Config._set_tls_config(local_config.get(Config.FIELD_TLS))

        Config.merge(loaded_config.get(Config.SERVICE_NAME))
        return True
