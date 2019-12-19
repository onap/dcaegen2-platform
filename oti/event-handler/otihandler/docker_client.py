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

"""client interface to docker"""

import docker
import json
import logging
import time

from otihandler.config import Config
from otihandler.consul_client import ConsulClient
from otihandler.utils import decrypt


# class DockerClientError(RuntimeError):
#     pass

class DockerClientConnectionError(RuntimeError):
    pass


class DockerClient(object):
    """
    All Docker logins are in Consul's key-value store under
    "docker_plugin/docker_logins" as a list of json objects where
    each object is a single login:

        [{ "username": "XXXX", "password": "yyyy",
           "registry": "hostname.domain:18443" }]
    """

    _logger = logging.getLogger("oti_handler.docker_client")

    def __init__(self, docker_host, reauth=False):
        """Create Docker client

        Args:
        -----
        reauth: (boolean) Forces reauthentication, e.g., Docker login
        """

        (fqdn, port) = ConsulClient.get_service_fqdn_port(docker_host, node_meta=True)
        base_url = "https://{}:{}".format(fqdn, port)

        try:
            tls_config = docker.tls.TLSConfig(
                client_cert=(
                    Config.tls_server_ca_chain_file,
                    Config.tls_private_key_file
                )
            )
            self._client = docker.APIClient(base_url=base_url, tls=tls_config, version='auto', timeout=60)

            for dcl in ConsulClient.get_value("docker_plugin/docker_logins"):
                dcl['password'] = decrypt(dcl['password'])
                dcl["reauth"] = reauth
                self._client.login(**dcl)

        # except requests.exceptions.RequestException as e:
        except Exception as e:
            msg = "DockerClient.__init__({}) attempt to {} with TLS got exception {}: {!s}".format(
                      docker_host, base_url, type(e).__name__, e)

            # Then try connecting to dockerhost without TLS
            try:
                base_url = "tcp://{}:{}".format(fqdn, port)
                self._client = docker.APIClient(base_url=base_url, tls=False, version='auto', timeout=60)

                for dcl in ConsulClient.get_value("docker_plugin/docker_logins"):
                    dcl['password'] = decrypt(dcl['password'])
                    dcl["reauth"] = reauth
                    self._client.login(**dcl)

            # except requests.exceptions.RequestException as e:
            except Exception as e:
                msg = "{}\nDockerClient.__init__({}) attempt to {} without TLS got exception {}: {!s}".format(
                          msg, docker_host, base_url, type(e).__name__, e)
                DockerClient._logger.error(msg)
                raise DockerClientConnectionError(msg)

    @staticmethod
    def build_cmd(script_path, use_sh=True, msg_type="dti", **kwargs):
        """Build command to execute"""

        data = json.dumps(kwargs or {})

        if use_sh:
            return ['/bin/sh', script_path, msg_type, data]
        else:
            return [script_path, msg_type, data]

    def notify_for_reconfiguration(self, container_id, cmd):
        """Notify Docker container that reconfiguration occurred

        Notify the Docker container by doing Docker exec of passed-in command

        Args:
        -----
        container_id: (string)
        cmd: (list) of strings each entry being part of the command
        """

        for attempts_remaining in range(11,-1,-1):
            try:
                result = self._client.exec_create(container=container_id, cmd=cmd)
            except docker.errors.APIError as e:
                # e  #  500 Server Error: Internal Server Error ("{"message":"Container 624108d1ab96f24b568662ca0e5ffc39b59c1c57431aec0bef231fb62b04e166 is not running"}")
                DockerClient._logger.debug("exec_create() returned APIError: {!s}".format(e))

                # e.message               # 500 Server Error: Internal Server Error
                # DockerClient._logger.debug("e.message: {}".format(e.message))
                # e.response.status_code  # 500
                # DockerClient._logger.debug("e.response.status_code: {}".format(e.response.status_code))
                # e.response.reason       # Internal Server Error
                # DockerClient._logger.debug("e.response.reason: {}".format(e.response.reason))
                # e.explanation           # {"message":"Container 624108d1ab96f24b568662ca0e5ffc39b59c1c57431aec0bef231fb62b04e166 is not running"}
                # DockerClient._logger.debug("e.explanation: {}".format(e.explanation))

                # docker container restarting can wait
                if e.explanation and 'is restarting' in e.explanation.lower():
                    DockerClient._logger.debug("notification exec_create() experienced: {!s}".format(e))
                    if attempts_remaining == 0:
                        result = None
                        break
                    time.sleep(10)
                # elif e.explanation and 'no such container' in e.explanation.lower():
                # elif e.explanation and 'is not running' in e.explanation.lower():
                else:
                    DockerClient._logger.warn("aborting notification exec_create() because exception {}: {!s}".format(type(e).__name__, e))
                    return str(e)  # don't raise or CM will retry usually forever
                    # raise DockerClientError(e)
            except Exception as e:
                DockerClient._logger.warn("aborting notification exec_create() because exception {}: {!s}".format(
                    type(e).__name__, e))
                return str(e)  # don't raise or CM will retry usually forever
                # raise DockerClientError(e)
            else:
                break
        if not result:
            DockerClient._logger.warn("aborting notification exec_create() because docker exec failed")
            return "notification unsuccessful"  # failed to get an exec_id, perhaps trying multiple times, so don't raise or CM will retry usually forever
        DockerClient._logger.debug("notification exec_create() succeeded")

        for attempts_remaining in range(11,-1,-1):
            try:
                result = self._client.exec_start(exec_id=result['Id'])
            except Exception as e:
                DockerClient._logger.debug("notification exec_start() got exception {}: {!s}".format(type(e).__name__, e))
                if attempts_remaining == 0:
                    DockerClient._logger.warn("aborting notification exec_start() because exception {}: {!s}".format(type(e).__name__, e))
                    return str(e)  # don't raise or CM will retry usually forever
                    # raise DockerClientError(e)
                time.sleep(10)
            else:
                break
        DockerClient._logger.debug("notification exec_start() succeeded")

        DockerClient._logger.info("Pass to docker exec {} {} {}".format(
            container_id, cmd, result))

        return result
