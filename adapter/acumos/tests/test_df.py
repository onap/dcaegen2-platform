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

from testing_helpers import get_json_fixture, get_fixture_path
from aoconversion import dataformat_gen, utils

TEST_META = get_json_fixture("models/example-model/metadata.json")


def test_get_needed_formats():
    assert dataformat_gen._get_needed_formats(TEST_META) == ["NumbersIn", "NumberOut"]


def test_generate_dcae_data_formats(mock_schemas):
    """
    Test generating data formats from the protobuf
    """
    test_proto_path = get_fixture_path("models/example-model/model.proto")
    assert dataformat_gen._generate_dcae_data_formats(test_proto_path, TEST_META, utils.dataformat_schema.get(), utils.schema_schema.get()) == [
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


def test_generate_dcae_data_formats_listofm(mock_schemas):
    """
    Test generating data formats from the protobuf
    This one tests the case where definitions needs to be populated in one of the data formats because it's referenced in a "top level" message
    """
    test_meta = get_json_fixture("models/example-model-listofm/metadata.json")
    test_proto_path = get_fixture_path("models/example-model-listofm/model.proto")
    assert dataformat_gen._generate_dcae_data_formats(test_proto_path, test_meta, utils.dataformat_schema.get(), utils.schema_schema.get()) == [
        {
            "self": {"name": "ArgsList", "version": "1.0.0"},
            "dataformatversion": "1.0.1",
            "jsonschema": {
                "title": "ArgsList",
                "type": "object",
                "properties": {"args": {"type": "array", "items": {"$ref": "#/definitions/Args"}}},
                "$schema": "http://json-schema.org/draft-04/schema#",
                "definitions": {
                    "Args": {
                        "title": "Args",
                        "type": "object",
                        "properties": {
                            "x": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991},
                            "y": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991},
                        },
                    }
                },
            },
        },
        {
            "self": {"name": "SumOut", "version": "1.0.0"},
            "dataformatversion": "1.0.1",
            "jsonschema": {
                "title": "SumOut",
                "type": "object",
                "properties": {
                    "value": {
                        "type": "array",
                        "items": {"type": "integer", "minimum": -9007199254740991, "maximum": 9007199254740991},
                    }
                },
                "$schema": "http://json-schema.org/draft-04/schema#",
                "definitions": {},
            },
        },
    ]
