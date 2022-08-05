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
import distributor
from distributor import registry_client as rc


def test_add_url_from_link():
    test = {"link": {"href": "bar"}, "name": "jane", "age": 33, "innerTest": {"link": {"href": "baz"}, "name": "bob"}}
    result = rc._add_url_from_link("http://foo", test)

    assert result["selfUrl"] == "http://foo/bar"
    assert result["innerTest"]["selfUrl"] == "http://foo/baz"


def test_get_buckets(monkeypatch):
    def fake_get_json(url):
        if url == "http://registry/buckets":
            return []
        return None

    monkeypatch.setattr(distributor.registry_client, "_get_json", fake_get_json)

    assert [] == rc.get_buckets("http://registry")
    assert [] == rc.get_buckets("http://registry/")


def test_find_flow(monkeypatch):
    def fake_get_buckets(url):
        return [{"selfUrl": "{0}/buckets/123".format(url)}]

    monkeypatch.setattr(distributor.registry_client, "get_buckets", fake_get_buckets)

    def fake_get_flows(registry_url, url):
        if url == "http://registry/buckets/123":
            return [{"identifier": "abc"}]
        return None

    monkeypatch.setattr(distributor.registry_client, "get_flows", fake_get_flows)

    assert rc.find_flow("http://registry", "abc")["identifier"] == "abc"


def test_flow_versions(monkeypatch):
    def fake_get_json_many(url):
        if url == "http://registry/buckets/123/flows/abc/versions":
            return [{"version": 1}, {"version": 3}, {"version": 2}]
        print(url)
        return []

    monkeypatch.setattr(distributor.registry_client, "_get_json", fake_get_json_many)

    assert [3, 2, 1] == rc.get_flow_versions("http://registry/buckets/123/flows/abc/")


def test_get_flow_diff_latest(monkeypatch):
    def fake_get_flow_versions(url):
        return ["1"]

    monkeypatch.setattr(distributor.registry_client, "get_flow_versions", fake_get_flow_versions)

    assert None == rc.get_flow_diff_latest("http://registry", "http://registry/buckets/123/flows/abc/")
