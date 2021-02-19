# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
'''
Unit tests for convert.py
should return docker_uri, dataformat and spec
'''

import aoconversion
from testing_helpers import get_fixture_path


def test_gen_dcae_artifacts_for_model(monkeypatch):
    model_repo_path = get_fixture_path('models')
    model_name = 'example-model'
    config = aoconversion.scanner.Config(dcaeurl='http://dcaeurl', dcaeuser='dcaeuser', onboardingurl='https://onboarding', onboardinguser='obuser', onboardingpass='obpass', acumosurl='https://acumos', certfile=None, dockerregistry='dockerregistry', dockeruser='registryuser', dockerpass='registrypassword', tmpdir=model_repo_path)
    spec = {
        "self": {
            "version": "1.0.0",
            "name": "example-model",
            "description": "Automatically generated from Acumos model",
            "component_type": "docker",
        },
        "services": {"calls": [], "provides": []},
        "streams": {
            "subscribes": [
                {"config_key": "add_subscriber", "format": "NumbersIn", "version": "1.0.0", "type": "message_router"}
            ],
            "publishes": [
                {"config_key": "add_publisher", "format": "NumberOut", "version": "1.0.0", "type": "message_router"}
            ],
        },
        "parameters": [],
        "auxilary": {"healthcheck": {"type": "http", "endpoint": "/healthcheck"}},
        "artifacts": [{"type": "docker image", "uri": "nexus01.fake.com:18443/example-model:latest"}],
    }
    dataformat = [
        {
            "self": {"name": "NumbersIn", "version": "1.0.0"},
            "dataformatversion": "1.0.1",
            "jsonschema": {
                "title": "NumbersIn",
                "type": "object",
                "properties": {
                    "x": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991},
                    "y": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991},
                },
                "$schema": "http://json-schema.org/draft-04/schema#",
                "definitions": {},
            },
        },
        {
            "self": {"name": "NumberOut", "version": "1.0.0"},
            "dataformatversion": "1.0.1",
            "jsonschema": {
                "title": "NumberOut",
                "type": "object",
                "properties": {"result": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991}},
                "$schema": "http://json-schema.org/draft-04/schema#",
                "definitions": {},
            },
        },
    ]
    dockeruri = 'dockerregistry/example-model:latest'

    def mockreturn_dockeruri(config, model_name, model_version='latest'):
        return dockeruri

    def mockreturn_dataformats(model_repo_path, model_name):
        return dataformat

    def mockreturn_spec(model_repo_path, model_name, dataformat, dockeruri):
        return spec

    monkeypatch.setattr(aoconversion.docker_gen, 'build_and_push_docker', mockreturn_dockeruri)
    monkeypatch.setattr(aoconversion.dataformat_gen, 'generate_dcae_data_formats', mockreturn_dataformats)
    monkeypatch.setattr(aoconversion.spec_gen, 'generate_spec', mockreturn_spec)
    assert aoconversion.convert.gen_dcae_artifacts_for_model(config, model_name, 'latest') == (dockeruri, dataformat, spec)
