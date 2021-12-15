# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
# Copyright (c) 2021 highstreet technologies GmbH. All rights reserved.
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
from aoconversion import dataformat_gen, spec_gen, utils

TEST_META = get_json_fixture("models/example-model/metadata.json")


def test_generate_spec(mock_schemas):
    """
    Test generating data formats from the protobuf
    """
    test_proto_path = get_fixture_path("models/example-model/model.proto")
    data_formats = dataformat_gen._generate_dcae_data_formats(test_proto_path, TEST_META, utils.dataformat_schema.get(),
                                                              utils.schema_schema.get())
    assert spec_gen._generate_spec(
        "example-model", TEST_META, utils.component_schema.get(), data_formats,
        "latest"
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
                       {"config_key": "add_subscriber", "format": "NumbersIn", "version": "1.0.0",
                        "type": "message_router"}
                   ],
                   "publishes": [
                       {"config_key": "add_publisher", "format": "NumberOut", "version": "1.0.0",
                        "type": "message_router"}
                   ],
               },
               "parameters": [
                   {
                       "name": "streams_subscribes",
                       "value": "{\"add_subscriber\":{\"dmaap_info\":{\"topic_url\":\"http://message-router:3904/events/unauthenticated.example-model_In\"},\"type\":\"message_router\"}}",
                       "description": "standard http port collector will open for listening;",
                       "sourced_at_deployment": False,
                       "policy_editable": False,
                       "designer_editable": False
                   },
                   {
                       "name": "streams_publishes",
                       "value": "{\"add_publisher\":{\"dmaap_info\":{\"topic_url\":\"http://message-router:3904/events/unauthenticated.example-model_Out\"},\"type\":\"message_router\"}}",
                       "description": "standard http port collector will open for listening;",
                       "sourced_at_deployment": False,
                       "policy_editable": False,
                       "designer_editable": False
                   }
               ],
               "auxilary": {
                   "helm": {
                       "service": {
                           "type": "ClusterIP",
                           "name": "example-model",
                           "has_internal_only_ports": "true",
                           "ports": [{
                               "name": "http",
                               "port": 8443,
                               "plain_port": 8080,
                               "port_protocol": "http"
                           }
                           ]
                       }
                   },
                   "healthcheck": {"type": "HTTP", "interval": "15s", "timeout": "1s", "port": 8080,
                                   "endpoint": "/healthcheck"}},
               "artifacts": [{"type": "docker image", "uri": "example-model:latest"}],
           }
