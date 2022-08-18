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
"""Code for http interface"""

import logging, json
import uuid
from flask import Flask
from flask_cors import CORS
import flask_restplus as frp
from flask_restplus import Api, Resource, fields
from distributor.version import __version__
from distributor import data_access as da
from distributor import config
from distributor import registry_client as rc
from distributor import onboarding_client as oc
from distributor import runtime_client as runc
from distributor import transform as tr


_log = logging.getLogger("distributor.http")

_app = Flask(__name__)
CORS(_app)
# Try to bundle as many errors together
# https://flask-restplus.readthedocs.io/en/stable/parsing.html#error-handling
_app.config["BUNDLE_ERRORS"] = True
_api = Api(
    _app,
    version=__version__,
    title="Distributor HTTP API",
    description="HTTP API to manage distribution targets for DCAE design. Distribution targets are DCAE runtime environments that have been registered and are enabled to accept flow design changes that are to be orchestrated in that environment",
    contact="",
    default_mediatype="application/json",
    prefix="/distributor",
    doc="/distributor",
    default="distributor",
)
# REVIEW: Do I need a namespace?
ns = _api

model_pg = _api.model(
    "ProcessGroup",
    {
        "id": fields.String(required=True, description="Id for this process group", attribute="processGroupId"),
        "version": fields.Integer(required=True, description="Version of the process group"),
        "processed": fields.DateTime(required=True, description="When this process group was processed by this API"),
        "runtimeResponse": fields.String(required=True, description="Full response from the runtime API"),
    },
)

model_dt = _api.model(
    "DistributionTarget",
    {
        "selfUrl": fields.Url("resource_distribution_target", absolute=True),
        "id": fields.String(required=True, description="Id for this distribution target", attribute="dt_id"),
        "name": fields.String(required=True, description="Name for this distribution target", attribute="name"),
        "runtimeApiUrl": fields.String(
            required=True, description="Url to the runtime API for this distribution target", attribute="runtimeApiUrl"
        ),
        "description": fields.String(
            required=False, description="Description for this distribution target", attribute="description"
        ),
        "nextDistributionTargetId": fields.String(
            required=False,
            description="Id to the next distribution target. Distribution targets can be linked together and have a progression order. Specifying the id of the next distribution target defines the next element int the order.",
            attribute="nextDistributionTargetId",
        ),
        "created": fields.String(
            required=True, description="When this distribution target was created in UTC", attribute="created"
        ),
        "modified": fields.String(
            required=True, description="When this distribution target was last modified in UTC", attribute="modified"
        ),
        "processGroups": fields.List(fields.Nested(model_pg)),
    },
)

model_dts = _api.model("DistributionTargets", {"distributionTargets": fields.List(fields.Nested(model_dt))})


parser_dt_req = ns.parser()
parser_dt_req.add_argument(
    "name", required=True, trim=True, location="json", help="Name for this new distribution target"
)
parser_dt_req.add_argument(
    "runtimeApiUrl",
    required=True,
    trim=True,
    location="json",
    help="Url to the runtime API for this distribution target",
)
parser_dt_req.add_argument(
    "description", required=False, trim=True, location="json", help="Description for this distribution target"
)
parser_dt_req.add_argument(
    "nextDistributionTargetId",
    required=False,
    trim=True,
    location="json",
    help="Id of the next distribution target. Distribution targets can be linked together and have a progression order. Specifying the id of the next distribution target defines the next element int the order.",
)


@ns.route("/distribution-targets", endpoint="resource_distribution_targets")
class DistributionTargets(Resource):
    @ns.doc("get_distribution_targets", description="List distribution targets")
    @ns.marshal_with(model_dts)
    def get(self):
        return {"distributionTargets": da.get_distribution_targets()}, 200

    @ns.doc("post_distribution_targets", description="Create a new distribution target")
    @ns.expect(parser_dt_req)
    @ns.marshal_with(model_dt)
    def post(self):
        req = parser_dt_req.parse_args()
        req = da.transform_request(req)
        resp = da.add_distribution_target(req)
        return resp, 200


@ns.route("/distribution-targets/<string:dt_id>", endpoint="resource_distribution_target")
class DistributionTarget(Resource):
    @ns.doc("get_distribution_target", description="Get a distribution target instance")
    @ns.response(404, "Distribution target not found")
    @ns.response(500, "Internal Server Error")
    @ns.marshal_with(model_dt)
    def get(self, dt_id):
        result = da.get_distribution_target(dt_id)

        if result:
            return result, 200
        else:
            frp.abort(code=404, message="Unknown distribution target")

    @ns.doc("put_distribution_target", description="Update an existing distribution target")
    @ns.response(404, "Distribution target not found")
    @ns.response(500, "Internal Server Error")
    @ns.expect(parser_dt_req)
    @ns.marshal_with(model_dt)
    def put(self, dt_id):
        result = da.get_distribution_target(dt_id)

        if not result:
            frp.abort(code=404, message="Unknown distribution target")

        req = parser_dt_req.parse_args()
        updated_dt = da.merge_request(result, req)

        if da.update_distribution_target(updated_dt):
            return updated_dt, 200
        else:
            frp.abort(code=500, message="Problem with storing the update")

    @ns.response(404, "Distribution target not found")
    @ns.response(500, "Internal Server Error")
    @ns.doc("delete_distribution_target", description="Delete an existing distribution target")
    def delete(self, dt_id):
        if da.delete_distribution_target(dt_id):
            return
        else:
            frp.abort(code=404, message="Unknown distribution target")


parser_post_process_group = ns.parser()
parser_post_process_group.add_argument(
    "processGroupId", required=True, trim=True, location="json", help="Process group ID that exists in Nifi"
)


@ns.route("/distribution-targets/<string:dt_id>/process-groups", endpoint="resource_target_process_groups")
class DTargetProcessGroups(Resource):
    @ns.response(404, "Distribution target not found")
    @ns.response(501, "Feature is not supported right now")
    @ns.response(500, "Internal Server Error")
    @ns.expect(parser_post_process_group)
    def post(self, dt_id):
        # TODO: Need bucket ID but for now will simply scan through all buckets
        # TODO: Current impl doesn't take into consideration the last state of
        # the distribution target e.g. what was the last design processed

        req = parser_post_process_group.parse_args()

        # Check existence of distribution target

        dtarget = da.get_distribution_target(dt_id)

        if not dtarget:
            frp.abort(code=404, message="Unknown distribution target")

        runtime_url = dtarget["runtimeApiUrl"]
        pg_id = req["processGroupId"]

        # Find flow from Nifi registry

        try:
            flow = rc.find_flow(config.nifi_registry_url, pg_id)
        except Exception as e:
            # TODO: Switch to logging
            print(e)
            # Assuming it'll be 404
            frp.abort(code=404, message="Process group not found in registry")

        pg_name = flow["name"]

        # Make sure graph is setup in runtime api

        if runc.ensure_graph(runtime_url, pg_id, pg_name) == False:
            frp.abort(code=501, message="Runtime API: Graph could not be created")

        # Graph diffing using Nifi registry

        flow_diff = rc.get_flow_diff_latest(config.nifi_registry_url, flow["selfUrl"])

        if flow_diff:
            # TODO: Not really processing diff right now instead just processing
            # latest. Later process the diffs instead and send out the changes.
            flow_latest = rc.get_flow_version_latest(config.nifi_registry_url, flow["selfUrl"])
        else:
            flow_latest = rc.get_flow_version(config.nifi_registry_url, flow["selfUrl"], 1)

        # Get component data from onboarding API

        components = tr.extract_components_from_flow(flow_latest)

        try:
            components = oc.get_components_indexed(config.onboarding_api_url, components)
        except Exception as e:
            # TODO: Switch to logging
            print(e)
            # Assuming it'll be 404
            frp.abort(code=404, message="Component not found in onboarding API")

        #
        # Put everything together, post to runtime API, save
        #

        actions = tr.make_fbp_from_flow(flow_latest, components)

        resp = dict(req)
        resp["version"] = flow_latest["snapshotMetadata"]["version"]
        resp["runtimeResponse"] = json.dumps(runc.post_graph(runtime_url, pg_id, actions))
        resp = da.add_process_group(dt_id, resp)

        if resp:
            return resp, 200
        else:
            frp.abort(code=500, message="Could not store process group")


def start_http_server():
    config.init()

    def is_debug():
        import os

        if os.environ.get("DISTRIBUTOR_DEBUG", "1") == "1":
            return True
        else:
            return False

    if is_debug():
        _app.run(debug=True)
    else:
        _app.run(host="0.0.0.0", port=8080, debug=False)
