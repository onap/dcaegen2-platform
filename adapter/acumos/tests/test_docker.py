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

from testing_helpers import get_json_fixture
from aoconversion import docker_gen

TEST_META = get_json_fixture("models/example-model/metadata.json")


def test_generate_dockerfile():
    assert (
        docker_gen._generate_dockerfile(TEST_META, "example-model")
        == """
    FROM python:3.6.8

    ENV MODELNAME example-model
    RUN mkdir /app
    WORKDIR /app

    ADD ./example-model /app/example-model
    ADD ./requirements.txt /app

    RUN pip install -r /app/requirements.txt && \
        pip install acumos_dcae_model_runner

    ENV DCAEPORT=10000
    EXPOSE $DCAEPORT

    ENTRYPOINT ["acumos_dcae_model_runner"]
    CMD ["/app/example-model"]
    """
    )
