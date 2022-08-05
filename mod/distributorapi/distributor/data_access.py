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
"""Data layer"""

from datetime import datetime
import uuid

# TODO: Use real storage
_cache = []


def get_distribution_targets():
    global _cache
    return _cache


def get_distribution_target(ds_id):
    global _cache
    result = [i for i in _cache if i["dt_id"] == ds_id]
    return result[0] if result else {}


def transform_request(req):
    """Transform request to object to store

    NOTE: This method is not safe
    """
    ts = datetime.utcnow().isoformat()
    req["created"] = ts
    req["modified"] = ts
    req["dt_id"] = str(uuid.uuid4())
    req["processGroups"] = []
    return req


def add_distribution_target(dt):
    global _cache
    _cache.append(dt)
    return dt


def merge_request(dt, req):
    dt["name"] = req["name"]
    dt["runtimeApiUrl"] = req["runtimeApiUrl"]
    dt["description"] = req.get("description", None)
    dt["nextDistributionTargetId"] = req.get("nextDistributionTargetId", None)
    dt["modified"] = datetime.utcnow().isoformat()
    return dt


def update_distribution_target(updated_dt):
    dt_id = updated_dt["dt_id"]
    global _cache
    # Did not use list comprehension blah blah because could not do the "return
    # True" easily
    for i, dt in enumerate(_cache):
        if dt["dt_id"] == dt_id:
            _cache[i] = updated_dt
            return True
    return False


def delete_distribution_target(dt_id):
    global _cache
    num_prev = len(_cache)
    _cache = list(filter(lambda e: e["dt_id"] != dt_id, _cache))
    return len(_cache) < num_prev


def add_process_group(ds_id, process_group):
    global _cache
    for dt in _cache:
        if dt["dt_id"] == ds_id:
            process_group["processed"] = datetime.utcnow().isoformat()
            dt["processGroups"].append(process_group)
            return process_group
    return None
