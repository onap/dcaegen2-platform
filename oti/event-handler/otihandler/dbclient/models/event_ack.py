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

""" ORM - mapping class for dtih_event_ack table """

import datetime
from sqlalchemy import Column, String, Integer, ForeignKey, func
from sqlalchemy.dialects.postgresql import JSONB, TIMESTAMP
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
from ..models import Event

Base = declarative_base()

class EventAck(Base):
    __tablename__ = 'dtih_event_ack'
    __table_args__ = {'schema': 'dti'}
    dtih_event_ack_id = Column(Integer, primary_key=True)
    create_ts = Column(TIMESTAMP(timezone=True), default=func.now())
    last_modified_ts = Column(TIMESTAMP(timezone=True), default=func.now())
    action = Column(String)
    k8s_namespace = Column(String)
    k8s_service_name = Column(String)
    k8s_service_port = Column(String)
    k8s_cluster_fqdn = Column(String)
    k8s_proxy_fqdn = Column(String)
    k8s_pod_id = Column(String)
    service_component = Column(String)
    deployment_id = Column(String)
    container_type = Column(String)
    docker_host = Column(String)
    container_id = Column(String)
    reconfig_script = Column(String)
    dtih_event_id = Column(Integer, ForeignKey(Event.dtih_event_id))
    event = relationship(Event)

    def update_action(self, action):
        setattr(self, 'action', action)
