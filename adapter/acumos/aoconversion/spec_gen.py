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

"""
Generates DCAE component specs
"""

import json
from jsonschema import validate
from aoconversion import utils


def _get_format_version(target_name, data_formats):
    """
    search through the data formats for name, make sure we have it, and retrieve the version
    """
    # the df must exist, since the data formats were generated from the same metadata, or dataformats call blew up.
    # So we don't do error checking here
    for df in data_formats:
        if df["self"]["name"] == target_name:
            return df["self"]["version"]


def _generate_spec(model_name, meta, dcae_cs_schema, data_formats, model_version):
    """
    Function that generates the component spec from the model metadata and docker info
    Broken out to be unit-testable.
    """
    docker_uri = "{}:{}".format(model_name, model_version)
    spec = {
        "self": {
            "version": "1.0.0",  # hopefully we get this from somewhere and not hardcode this
            "name": model_name,
            "description": "Automatically generated from Acumos model",
            "component_type": "docker",
        },
        "services": {"calls": [], "provides": []},
        "streams": {"subscribes": [], "publishes": []},
        "parameters": [],
        "auxilary": {"helm": {"service": {"type": "ClusterIP",
                                          "name": model_name,
                                          "has_internal_only_ports": "true",
                                          "ports": [{"name": "http", "port": 8443, "plain_port": 8080,
                                                     "port_protocol": "http"}]}},
                     "healthcheck": {"type": "HTTP", "interval": "15s", "timeout": "1s", "port": 8080,
                                     "endpoint": "/healthcheck"}},
        "artifacts": [{"type": "docker image", "uri": docker_uri}],
    }

    # from https://pypi.org/project/acumos-dcae-model-runner/
    # each model method gets a subscruber and a publisher, using the methood name
    pstype = "message_router"
    for method in meta["methods"]:
        df_in_name = utils.validate_format(meta, method, "input")
        subscriber = {
            "config_key": "{0}_subscriber".format(method),
            "format": df_in_name,
            "version": _get_format_version(df_in_name, data_formats),
            "type": pstype,
        }

        spec["streams"]["subscribes"].append(subscriber)

        df_out_name = utils.validate_format(meta, method, "output")

        publisher = {
            "config_key": "{0}_publisher".format(method),
            "format": df_out_name,
            "version": _get_format_version(df_out_name, data_formats),
            "type": pstype,
        }

        publisher = {
            "config_key": "{0}_publisher".format(method),
            "format": df_out_name,
            "version": _get_format_version(df_out_name, data_formats),
            "type": pstype,
        }
        parameter_subscriber = {
            "name": "streams_subscribes",
            "value": "{{\"{0}_subscriber\":{{\"dmaap_info\":{{\"topic_url\":\"http://message-router:3904/events/unauthenticated.{1}_In\"}},\"type\":\"message_router\"}}}}".format(
                method, model_name),

            "description": "standard http port collector will open for listening;",
            "sourced_at_deployment": False,
            "policy_editable": False,
            "designer_editable": False
        }
        paramter_publisher = {
            "name": "streams_publishes",
            "value": "{{\"{0}_publisher\":{{\"dmaap_info\":{{\"topic_url\":\"http://message-router:3904/events/unauthenticated.{1}_Out\"}},\"type\":\"message_router\"}}}}".format(
                method, model_name),
            "description": "standard http port collector will open for listening;",
            "sourced_at_deployment": False,
            "policy_editable": False,
            "designer_editable": False
        }
        spec["streams"]["publishes"].append(publisher)
        spec["parameters"].append(parameter_subscriber)
        spec["parameters"].append(paramter_publisher)

    # Validate that we have a valid spec
    validate(instance=spec, schema=dcae_cs_schema)
    return spec


def generate_spec(model_repo_path, model_name, data_formats, model_version):
    """
    Generate and write the component spec to disk
    Returns the spec
    """
    spec = _generate_spec(
        model_name, utils.get_metadata(model_repo_path, model_name), utils.component_schema.get(), data_formats,
        model_version
    )
    fname = "{0}_dcae_component_specification.json".format(model_name)
    with open("{0}/{1}".format(model_repo_path, fname), "w") as f:
        f.write(json.dumps(spec))

    return spec
