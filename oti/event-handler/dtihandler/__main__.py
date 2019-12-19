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

"""run as server: python -m dtihandler"""

import logging
import os
import sys
import json
import datetime

from dtihandler.config import Config
from dtihandler.onap.audit import Audit
from dtihandler.web_server import DTIWeb
from dtihandler.dbclient import DaoBase
from dtihandler.dbclient.apis import DbAccess
from dtihandler.consul_client import ConsulClient


class LogWriter(object):
    """redirect the standard out + err to the logger"""

    def __init__(self, logger_func):
        self.logger_func = logger_func

    def write(self, log_line):
        """actual writer to be used in place of stdout or stderr"""

        log_line = log_line.rstrip()
        if log_line:
            self.logger_func(log_line)

    def flush(self):
        """no real flushing of the buffer"""

        pass


def run_event_handler():
    """main run function for event_handler"""

    Config.load_from_file()
    # Config.discover()

    logger = logging.getLogger("event_handler")
    sys.stdout = LogWriter(logger.info)
    sys.stderr = LogWriter(logger.error)

    logger.info("========== run_event_handler ==========")
    app_version = os.getenv("APP_VER")
    logger.info("app_version %s", app_version)
    Audit.init(Config.get_system_name(), app_version, Config.LOGGER_CONFIG_FILE_PATH)

    logger.info("starting event_handler with config:")
    logger.info(Audit.log_json_dumps(Config.config))

    audit = Audit(req_message="start event_handler")

    audit = Audit(req_message="DB init start")
    DaoBase.init_db(os.environ.get("DB_CONN_URL"))

    DTIWeb.run_forever(audit)

if __name__ == "__main__":
    run_event_handler()
