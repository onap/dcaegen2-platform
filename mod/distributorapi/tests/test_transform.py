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
import os, json
from distributor import transform as tr

TEST_DIR = os.path.dirname(__file__)


def _load_data(filename):
    path = os.path.join(TEST_DIR, filename)
    with open(path) as f:
        return json.load(f)


def _setup():
    flow = _load_data("flow.json")
    components = _load_data("components.json")
    components = dict([((c["name"], c["version"]), c) for c in components])
    return (flow, components)


def test_get_component():
    flow, components = _setup()
    c = tr.get_component(flow, components, "a8134467-b4b4-348f-8a1c-8d732fe4fcad")
    assert "3fadb641-2079-4ca9-bb07-0df5952967fc" == c["id"]


def test_make_fbp_from_flow():
    flow, components = _setup()

    fbp = tr.make_fbp_from_flow(flow, components)
    assert len(fbp) == 4

    def check_node(n):
        n["payload"]["component_id"]

    expected = ["75c9a179-b36b-4985-9445-d44c8768d6eb", "3fadb641-2079-4ca9-bb07-0df5952967fc"]
    actual = [n["payload"]["component_id"] for n in fbp if n["command"] == "addnode"]
    assert list(sorted(expected)) == list(sorted(actual))

    # Test processor to processor scenario
    expected = {
        "metadata": {"data_type": "json", "dmaap_type": "MR", "name": "foo-conn"},
        "src": {"node": "75c9a179-b36b-4985-9445-d44c8768d6eb", "port": "ves-pnfRegistration-secondary"},
        "tgt": {"node": "3fadb641-2079-4ca9-bb07-0df5952967fc", "port": "predict_subscriber"},
    }
    actual = [e["payload"] for e in fbp if e["command"] == "addedge"]
    assert actual[0] == expected or actual[1] == expected

    # Test input port to processor scenario
    expected = {
        "metadata": {"data_type": "json", "dmaap_type": "MR", "name": "ves-data-conn"},
        "src": {},
        "tgt": {"node": "75c9a179-b36b-4985-9445-d44c8768d6eb", "port": "ves-notification"},
    }
    assert actual[0] == expected or actual[1] == expected
