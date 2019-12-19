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

"""
Base class for APIs to interact with application database using sqlAlchemy ORM lib and postgresSql driver
"""

from sqlalchemy.orm import sessionmaker
from ..db_dao import DaoBase
import psycopg2
from psycopg2.extras import execute_values
import os
import logging


class DbAccess(object):
    logger = logging.getLogger("dti_handler.DbAccess")
    engine = None
    session = None

    def __init__(self):
        self.engine = DaoBase.getDbEngine()
        # create a configured "Session" class
        Session = sessionmaker(bind=self.engine)

        # create a Session
        self.session = Session()

    def saveDomainObject(self, obj):
        self.session.add(obj)
        self.session.commit()
        self.session.close()

    def deleteDomainObject(self,obj):
        self.session.delete(obj)
        self.session.commit()
        self.session.close()
