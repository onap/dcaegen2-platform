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

from testing_helpers import get_json_fixture, get_fixture_path
from aoconversion import docker_gen, scanner
import test_fed

TEST_META = get_json_fixture("models/example-model/metadata.json")


def test_generate_dockerfile():
    assert (
        docker_gen._generate_dockerfile(TEST_META, "example-model", '', '')
        == """
    FROM python:3.6.8

    ENV MODELNAME example-model
    RUN mkdir /app
    WORKDIR /app

    ADD ./example-model /app/example-model
    ADD ./requirements.txt /app
    ENV https_proxy=
    ENV http_proxy=
    ENV HTTP_PROXY=
    ENV HTTPS_PROXY=
    ENV no_proxy=
    ENV NO_PROXY=
    RUN pip install -r /app/requirements.txt && \
        pip install acumos_dcae_model_runner

    ENV DCAEPORT=10000
    EXPOSE $DCAEPORT

    ENTRYPOINT ["acumos_dcae_model_runner"]
    CMD ["/app/example-model"]
    """
    )


def test_build_and_push_docker(monkeypatch):
    model_repo_path = get_fixture_path('models')
    config = scanner.Config(dcaeurl='http://dcaeurl', dcaeuser='dcaeuser', onboardingurl='https://onboarding', onboardinguser='obuser', onboardingpass='obpass', acumosurl='https://acumos', certfile=None, dockerregistry='dockerregistry', dockeruser='registryuser', dockerpass='registrypassword', http_proxy='', no_proxy='',tmpdir=model_repo_path)
    monkeypatch.setattr(docker_gen, 'APIClient', test_fed._mockdocker.APIClient)
    assert(docker_gen.build_and_push_docker(config, 'example-model', model_version="latest") == 'dockerregistry/example-model:latest')
