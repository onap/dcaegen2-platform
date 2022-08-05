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
"""Runtime API client"""

import requests as reqs
from distributor import errors
from distributor.utils import urljoin, get_json


def get_graph(runtime_url, graph_id):
    # REVIEW: There's only support for one graph right now..
    url = urljoin(runtime_url, "api/graph/main")
    return get_json(url)


def create_graph(runtime_url, graph_id, graph_name):
    url = urljoin(runtime_url, "api/graph/main")

    resp = reqs.post(url, json={"name": graph_name, "id": graph_id, "description": "", "main": True})

    try:
        resp.raise_for_status()
    except Exception as e:
        raise errors.DistributorAPIError(e)


def delete_graph(runtime_url):
    url = urljoin(runtime_url, "api/graph/main")

    try:
        reqs.delete(url).raise_for_status()
    except Exception as e:
        raise errors.DistributorAPIError(e)


def post_graph(runtime_url, graph_id, actions):
    url = urljoin(runtime_url, "api/graph", graph_id, "distribute")
    graph_request = {"actions": actions}

    resp = reqs.post(url, json=graph_request)

    try:
        resp.raise_for_status()
        # REVIEW: Should be blueprint
        return resp.json()
    except Exception as e:
        with open("runtime-request-failed.json", "w+") as f:
            import json

            json.dump(graph_request, f)
        raise errors.DistributorAPIError(e)


def ensure_graph(runtime_url, pg_id, pg_name, max_attempts=6):
    """Ensures the graph with the specified id will exist"""
    # TODO: Remove this when runtime API more mature
    # Added this attempted delete call here to make sure repeated calls to post
    # flows works by making sure the runtime API main graph is always empty
    try:
        delete_graph(runtime_url)
    except:
        # Probably a 404, doesn't matter
        pass

    # The attempts are not *really* attempts because attempts equates to looping
    # twice
    for i in range(0, max_attempts):
        resp = None

        try:
            resp = get_graph(runtime_url, pg_id)
        except Exception as e:
            # Assuming you are here because graph needs to be created
            create_graph(runtime_url, pg_id, pg_name)

        # TODO: Runtime api only supports 1 graph which is why this check is
        # here. Make sure it will support many graphs and remove this

        if resp == None:
            # You are here because the graph was created for first time or
            # the graph was deleted then created. Anyways next loop should
            # check if it got created ok
            continue
        elif resp != None and resp["id"] != pg_id:
            delete_graph(runtime_url)
        elif resp != None and resp["id"] == pg_id:
            return True

    return False
