# ============LICENSE_START=======================================================
# Copyright 2020 Deutsche Telekom. All rights reserved.
# Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
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
FROM nexus3.onap.org:10001/onap/integration-python:7.0.1

ENV SERVER_URL=https://git.onap.org/dcaegen2/platform/plain/mod
ENV PATH_COMPONENT_SPEC=/component-json-schemas/component-specification/dcae-cli-v3/component-spec-schema.json
ENV PATH_DATA_FORMAT=/component-json-schemas/data-format/dcae-cli-v1/data-format-schema.json
ENV PG_CONN=

COPY --chown=onap:onap . /app

USER root

RUN apk update && \
    apk add --virtual build-deps gcc python3-dev musl-dev && \
    apk add postgresql-dev bash && \
    mkdir schemas && \
    wget -O schemas/compspec.json $SERVER_URL$PATH_COMPONENT_SPEC && \
    wget -O schemas/dataformat.json $SERVER_URL$PATH_DATA_FORMAT && \
    pip install .

USER onap

EXPOSE 8080

CMD /app/start.sh
