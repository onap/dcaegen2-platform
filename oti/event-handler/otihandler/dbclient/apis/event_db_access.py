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

""" DB APIs to interact with application database using sqlAlchemy ORM lib and postgresSql driver"""

from sqlalchemy import and_
from sqlalchemy.orm.exc import NoResultFound

from .db_access import DbAccess
from ..models import Event, EventAck


class EventDbAccess(DbAccess):

    def __init__(self):
        DbAccess.__init__(self)

    def query_event_item(self, target_type, target_name):
        try:
            query = self.session.query(Event).filter(Event.target_type == target_type).\
                filter(Event.target_name == target_name)
            evt = query.one()
        except NoResultFound:
            return None
        else:
            return evt

    def query_event_data(self, target_type, target_name):
        try:
            query = self.session.query(Event).filter(Event.target_type == target_type).\
                filter(Event.target_name == target_name)
            evt = query.one()
        except NoResultFound:
            return []
        else:
            try:
                ack_result = self.session.query(EventAck).filter(EventAck.event == evt).all()
            except NoResultFound:
                return []
            else:
                return ack_result

    def query_event_data_k8s(self, target_type, target_name):
        try:
            query = self.session.query(Event).filter(Event.target_type == target_type).\
                filter(Event.target_name == target_name)
            evt = query.one()
        except NoResultFound:
            return []
        else:
            try:
                ack_result = self.session.query(EventAck).filter(EventAck.event == evt).\
                    filter(EventAck.container_type != 'docker').all()
            except NoResultFound:
                return []
            else:
                return ack_result

    def query_event_info_docker(self, prim_evt, service_component, deployment_id, container_id):
        try:
            query = self.session.query(EventAck).filter(EventAck.event == prim_evt).filter(
                and_(EventAck.service_component == service_component,
                     EventAck.deployment_id == deployment_id,
                     EventAck.container_id == container_id,
                     EventAck.container_type == 'docker'))
            evt = query.one()
        except NoResultFound as nrf:
            raise nrf
        else:
            return evt

    def update_event_item(self, dti_event, target_type, target_name):
        self.session.query(Event).filter(Event.target_type == target_type). \
            filter(Event.target_name == target_name).update({Event.event:dti_event})
        self.session.commit()

    def query_raw_k8_events(self, cluster, pod, namespace):
        """
        run an inner JOIN query to dtih_event and dtih_event_ack tables using supplied query predicates

        :param cluster:
        :param pod:
        :param namespace:
        :return:
            Set of event objects related to k8s pods
        """
        try:
            return self.session.query(Event).filter(Event.dtih_event_id.in_(
                self.session.query(EventAck.dtih_event_id).filter(and_(EventAck.k8s_cluster_fqdn == cluster,
                                                                       EventAck.k8s_pod_id == pod,
                                                                       EventAck.k8s_namespace == namespace)))).all()
        except NoResultFound:
            print("invalid query or no data")
            return ()

    def query_raw_docker_events(self, target_types, locations):
        """
        run a query to dtih_event table using supplied query predicates

        :param target_types: required
        :param locations: optional
        :return:
            set of event objects related to docker container
        """
        try:
            if not locations or (len(locations) == 1 and locations[0] == ''):
                return self.session.query(Event).filter(Event.target_type.in_(target_types)).all()
            else:
                return self.session.query(Event).filter(Event.target_type.in_(target_types)).filter(
                    Event.location_clli.in_(locations)).all()
        except NoResultFound:
            print("invalid query or no data")
            return ()

    def query_pod_info2(self, cluster):
        try:
            return self.session.query(EventAck).filter(EventAck.k8s_cluster_fqdn == cluster).all()
        except NoResultFound:
            print("invalid query or no data")
            return ()

    def query_pod_info(self, cluster):
        try:
            return self.session.query(EventAck.k8s_pod_id, EventAck.k8s_namespace,
                                      EventAck.k8s_proxy_fqdn, EventAck.k8s_service_name,
                                      EventAck.k8s_service_port)\
                .filter(EventAck.k8s_cluster_fqdn == cluster) \
                .distinct().order_by(EventAck.k8s_cluster_fqdn).all()
        except NoResultFound:
            print("invalid query or no data")
            return ()

    def query_event_data_k8s_pod(self, prim_evt, scn):
        try:
            query = self.session.query(EventAck).filter(EventAck.event == prim_evt).filter(
                and_(EventAck.service_component == scn))
            event_info = query.one()
        except NoResultFound:
            return None
        else:
            return event_info
