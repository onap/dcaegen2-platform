# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
# =============================================================================
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
# ============LICENSE_END======================================================

FROM python:3.7
ARG UID=1000
ARG GID=1000
COPY setup.py /tmp/build/
COPY aoconversion/ /tmp/build/aoconversion/
RUN apt-get update && \
    apt-get install -y npm nodejs && \
    npm -v && \
    npm install -g protobuf-jsonschema && \
    cd /tmp/build/ && \
    python setup.py install && \
    cd / && \
    rm -rf /tmp/* && \
    groupadd -g $GID dcaemod && \
    useradd -s /bin/bash -u $UID -g $GID -m dcaemod

EXPOSE 9000
ENV PYTHONUNBUFFERED TRUE
USER dcaemod
ENTRYPOINT [ "/usr/local/bin/acumos-adapter" ]
CMD [ "/run/config/config.yaml" ]
