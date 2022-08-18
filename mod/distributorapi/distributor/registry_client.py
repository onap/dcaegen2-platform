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
"""Sophisticated Nifi registry client"""

from distributor.utils import urljoin as _urljoin
from distributor.utils import get_json as _get_json


def _add_url_from_link(registry_url, obj):
    result = {}

    for k, v in obj.items():
        if k == "link":
            result["selfUrl"] = _urljoin(registry_url, v["href"])
            result[k] = v
        elif type(v) == dict:
            result[k] = _add_url_from_link(registry_url, v)
        else:
            result[k] = v

    return result


def get_buckets(registry_url):
    buckets = _get_json(_urljoin(registry_url, "buckets"))
    return [_add_url_from_link(registry_url, b) for b in buckets]


def get_flows(registry_url, bucket_url):
    flows = _get_json(_urljoin(bucket_url, "flows"))
    return [_add_url_from_link(registry_url, f) for f in flows]


def find_flow(registry_url, flow_id):
    buckets = get_buckets(registry_url)

    def is_match(flow):
        return flow["identifier"] == flow_id

    for bucket in buckets:
        result = [f for f in get_flows(registry_url, bucket["selfUrl"]) if is_match(f)]

        if result:
            return result.pop()

    return None


def get_flow_versions(flow_url):
    """Returns list of versions from greatest to least for a given flow"""
    versions_url = _urljoin(flow_url, "versions")
    # List of versions will be greatest to least
    return list(reversed(sorted([v["version"] for v in _get_json(versions_url)])))


def get_flow_diff(registry_url, flow_url, version_one, version_two):
    diff_url = _urljoin(flow_url, "diff", str(version_one), str(version_two))
    return _get_json(diff_url)


def get_flow_diff_latest(registry_url, flow_url):
    versions = get_flow_versions(flow_url)

    if len(versions) == 0:
        # Should not happen, should this be an error?
        return None
    elif len(versions) == 1:
        return None
    else:
        # Example in gitlab wiki shows that lower version is first
        return _add_url_from_link(registry_url, get_flow_diff(registry_url, flow_url, versions[1], versions[0]))


def get_flow_version(registry_url, flow_url, version):
    version_url = _urljoin(flow_url, "versions", str(version))
    return _add_url_from_link(registry_url, _get_json(version_url))


def get_flow_version_latest(registry_url, flow_url):
    return get_flow_version(registry_url, flow_url, "latest")
