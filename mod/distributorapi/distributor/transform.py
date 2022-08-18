# ============LICENSE_START=======================================================
# Copyright (c) 2019-2022 AT&T Intellectual Property. All rights reserved.
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
"""Transform objects from one form to another"""

import json
from functools import partial


def extract_components_from_flow(flow):
    """Given a versionedFlowSnapshot object, extract out the processors
    and create a list of tuples where each tuple is
    (component name, component version)"""
    extract = lambda p: (p["bundle"]["artifact"], p["bundle"]["version"])
    return [extract(p) for p in flow["flowContents"]["processors"]]


def get_component(flow, components, processor_id):
    def get_component(p):
        bundle = p["bundle"]
        return components.get((bundle["artifact"], bundle["version"]), None)

    cs = [get_component(p) for p in flow["flowContents"]["processors"] if p["identifier"] == processor_id]
    return cs[0] if cs else None


def make_fbp_from_flow(flow, components: "dict of (name, version) to components"):
    """Transform a versionedFlowSnapshot object into a runtime API (FBP) request

    An example of an edge:

    {
        "command": "addedge",
        "payload": {
            "src" : {
                "node": "comp1234",
                "port": "DCAE-HELLO-WORLD-PUB-MR"
            },
            "tgt" : {
                "node": "comp5678",
                "port": "DCAE-HELLO-WORLD-SUB-MR"
            },
        "metadata":{
            "name": "sample_topic_0",
            "data_type": "json",
            "dmaap_type": "MR"
            }
        },
        "target_graph_id": "string"
    }
    """
    _get_component = partial(get_component, flow, components)

    def parse_connection(conn):
        rels = conn["selectedRelationships"]

        if conn["source"]["type"] == "PROCESSOR":
            comp = _get_component(conn["source"]["id"])

            if not comp:
                # REVIEW: Raise error?
                return None

            # Example:
            # publishes:ves_specification:7.30.1:message router:ves-pnfRegistration-secondary
            rels_pubs = [r for r in rels if "publishes" in r]

            if rels_pubs:
                _, _, _, transport_type, config_key = rels_pubs[0].split(":")
                src = {"node": comp["id"], "port": config_key}
            else:
                # REVIEW: This should be an error?
                src = {"node": comp["id"], "port": None}
        else:
            src = {}

        if conn["destination"]["type"] == "PROCESSOR":
            comp = _get_component(conn["destination"]["id"])

            if not comp:
                # REVIEW: Raise error?
                return None

            # Example:
            # subscribes:predictin:1.0.0:message_router:predict_subscriber
            rels_subs = [r for r in rels if "subscribes" in r]

            if rels_subs:
                _, _, _, transport_type, config_key = rels_subs[0].split(":")
                tgt = {"node": comp["id"], "port": config_key}
            else:
                # REVIEW: This should be an error?
                tgt = {"node": comp["id"], "port": None}
        else:
            tgt = {}

        return {
            "command": "addedge",
            "payload": {
                "src": src,
                "tgt": tgt,
                "metadata": {
                    "name": conn["name"]
                    # TODO: Question these hardcoded attributes
                    ,
                    "data_type": "json",
                    "dmaap_type": "MR",
                },
            },
        }

    def parse_processor(p):
        c = components[(p["bundle"]["artifact"], p["bundle"]["version"])]
        return {
            "command": "addnode"
            # TODO: spec is required to be a json string but runtime api
            # changing this soon hopefully
            ,
            "payload": {
                "component_spec": json.dumps(c["spec"]),
                "component_id": c["id"],
                "name": c["name"],
                "processor": p,
            },
        }

    ps = [parse_processor(p) for p in flow["flowContents"]["processors"]]
    cs = [parse_connection(c) for c in flow["flowContents"]["connections"]]
    return ps + cs
