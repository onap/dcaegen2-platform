# ================================================================================
# Copyright (c) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
#
# ECOMP is a trademark and service mark of AT&T Intellectual Property.

FROM python:3.8.0

RUN apt-get update 
RUN apt-get -y --allow-unauthenticated install vim-tiny
RUN apt-get -y --allow-unauthenticated install dos2unix -f
RUN apt-get -y --allow-unauthenticated install postgresql

ENV INSROOT /opt/app
ENV APPUSER dti_handler
ENV HOME ${INSROOT}/${APPUSER}

RUN useradd -d ${HOME} ${APPUSER}

WORKDIR ${HOME}

EXPOSE 9443

COPY ./*.py ./
COPY ./*.in ./
COPY ./*.txt ./
COPY ./*.sh ./
COPY ./dtihandler/ ./dtihandler/
COPY ./etc/ ./etc/
COPY create_schema.sql /tmp/create_schema.sql
RUN dos2unix /tmp/create_schema.sql \
 && chmod 777 /tmp/create_schema.sql

RUN mkdir -p ${HOME}/logs \
 && chown -R ${APPUSER}:${APPUSER} ${HOME} \
 && chmod 500 ${HOME}/etc \
 && chmod 500 ${HOME}/run.sh \
 && sleep 5 \
 && pip install -r requirements.txt

USER ${APPUSER}

ENTRYPOINT ["./run.sh"]
