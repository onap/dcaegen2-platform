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
from aoconversion import dataformat_gen, spec_gen

TEST_META = get_json_fixture("models/example-model/metadata.json")
DRAFT_4_SCHEMA = get_json_fixture("jsdraft4schema.json")
DF_101 = get_json_fixture("dataformat_101.json")
CS_SCHEMA = get_json_fixture("dcae-cli-v2_component-spec-schema.json")


def test_generate_spec():
    """
    Test generating data formats from the protobuf
    """
    test_proto_path = get_fixture_path("models/example-model/model.proto")
    data_formats = dataformat_gen._generate_dcae_data_formats(test_proto_path, TEST_META, DF_101, DRAFT_4_SCHEMA)
    assert spec_gen._generate_spec(
        "example-model", TEST_META, CS_SCHEMA, data_formats, "nexus01.fake.com:18443/example-model:latest"
    ) == {
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
