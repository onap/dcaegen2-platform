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

""" ORM - mapping class for dtih_event table """

from sqlalchemy import Column, String, Integer, ForeignKey, func
from sqlalchemy.dialects.postgresql import JSONB, TIMESTAMP
from sqlalchemy.ext.declarative import declarative_base
import datetime


Base = declarative_base()

class Event(Base):
    __tablename__ = 'dtih_event'
    __table_args__ = {'schema': 'dti'}
    dtih_event_id = Column(Integer, primary_key=True)
    event = Column(JSONB)
    create_ts = Column(TIMESTAMP(timezone=True), default=func.now())
    last_modified_ts = Column(TIMESTAMP(timezone=True), default=func.now())
    target_name = Column(String)
    target_type = Column(String)
    location_clli = Column(String)
    # def __repr__(self):
    #     return "<Event(event_id='%s', target_type='%s', target_name='%s')" % (
    #         self.event_id, self.target_type, self.target_name
    #     )
