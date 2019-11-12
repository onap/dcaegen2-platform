# ============LICENSE_START=======================================================
# org.onap.dcae
# ================================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

import json
from datetime import datetime
from flask import Flask, request
from flask_restplus import Api, Resource, fields, abort
from dcae_cli._version import __version__
from dcae_cli.commands import util
from dcae_cli.util.logger import get_logger
from dcae_cli.util.exc import DcaeException
from dcae_cli.util import config as cli_config
from dcae_cli.catalog.exc import MissingEntry, CatalogError, DuplicateEntry, FrozenEntry, ForbiddenRequest
from dcae_cli.catalog.mock.catalog import MockCatalog

_log = get_logger("http")

_app = Flask(__name__)
# Try to bundle as many errors together
# https://flask-restplus.readthedocs.io/en/stable/parsing.html#error-handling
_app.config['BUNDLE_ERRORS'] = True
_api = Api(_app, version=__version__, title="DCAE Onboarding HTTP API", description=""
           , contact="mhwangatresearch.att.com", default_mediatype="application/json"
           , prefix="/onboarding", doc="/onboarding", default="onboarding"
       )

compSpecPath = cli_config.get_server_url() + cli_config.get_path_component_spec()
component_fields_request = _api.schema_model('Component Spec',
                        {'properties': {'owner': {'type': 'string'},
                                         'spec': {'type': 'object', \
                                                  'description': 'The Component Spec schema is here -> ' + compSpecPath}
                                       }
                        })

component_fields_get = _api.model('component fields', {
                        'id':            fields.String(required=True, description='. . . . ID of the component'),
                        'name':          fields.String(required=True, description='. . . . Name of the component'),
                        'version':       fields.String(required=True, description='. . . . Version of the component'),
                        'owner':         fields.String(required=True, description='. . . . ID of who added the component'),
                        'whenAdded':     fields.DateTime(required=True, dt_format='iso8601', description='. . . . When component was added to the Catalog'),
                        'modified':      fields.DateTime(required=True, dt_format='iso8601', description='. . . . When component was last modified'),
                        'status':        fields.String(required=True, description='. . . . Status of the component'),
                        'description':   fields.String(required=True, description='. . . . Description of the component'),
                        'componentType': fields.String(required=True, description='. . . . only "docker"'),
                        'componentUrl':  fields.String(required=True, description='. . . . Url to the Component Specification')
                        })
components_get = _api.model('Component List', {'components': fields.List(fields.Nested(component_fields_get))})

component_fields_by_id = _api.inherit('component fields by id', component_fields_get, {
                        'spec': fields.Raw(required=True, description='The Component Specification (json)')
                        })

component_post = _api.model('Component post', {'componentUrl': fields.String(required=True, description='. . . . Url to the Component Specification')})

dataformatPath = cli_config.get_server_url() + cli_config.get_path_data_format()
dataformat_fields_request = _api.schema_model('Data Format Spec',
                        {'properties': {'owner': {'type': 'string'},
                                         'spec': {'type': 'object', \
                                                  'description': 'The Data Format Spec schema is here -> ' + dataformatPath}
                                       }
                        })

dataformat_fields_get = _api.model('dataformat fields', {
                        'id':            fields.String(required=True, description='. . . . ID of the data format'),
                        'name':          fields.String(required=True, description='. . . . Name of the data format'),
                        'version':       fields.String(required=True, description='. . . . Version of the data format'),
                        'owner':         fields.String(required=True, description='. . . . ID of who added the data format'),
                        'whenAdded':     fields.DateTime(required=True, dt_format='iso8601', description='. . . . When data format was added to the Catalog'),
                        'modified':      fields.DateTime(required=True, dt_format='iso8601', description='. . . . When data format was last modified'),
                        'status':        fields.String(required=True, description='. . . . Status of the data format'),
                        'description':   fields.String(required=True, description='. . . . Description of the data format'),
                        'dataFormatUrl': fields.String(required=True, description='. . . . Url to the Data Format Specification')
                        })
dataformats_get = _api.model('Data Format List', {'dataFormats': fields.List(fields.Nested(dataformat_fields_get))})

dataformat_fields_by_id = _api.inherit('dataformat fields by id', dataformat_fields_get, {
                        'spec': fields.Raw(required=True, description='The Data Format Specification (json)')
                        })

dataformat_post = _api.model('Data Format post', {'dataFormatUrl': fields.String(required=True, description='. . . . Url to the Data Format Specification')})


patch_fields = _api.model('Patch Spec', {'owner':  fields.String(required=True, description='User ID'),
                                         'status': fields.String(required=True, enum=['published', 'revoked'], \
                                                       description='. . . . .[published] is the only status change supported right now')
                        } )

error_message = _api.model('Error message', {'message': fields.String(description='. . . . .Details about the unsuccessful API request')})


parser_components = _api.parser()
parser_components.add_argument("name", type=str, trim=True,
        location="args", help="Name of component to filter for")
parser_components.add_argument("version", type=str, trim=True,
        location="args", help="Version of component to filter for")

################
## Component  ##
################
@_api.route("/components", endpoint="resource_components")
class Components(Resource):
    """Component resource"""
    @_api.doc("get_components", description="Get list of Components in the catalog")
    @_api.marshal_with(components_get)
    @_api.response(200, 'Success, Components retrieved')
    @_api.response(500, 'Internal Server Error')
    @_api.expect(parser_components)
    def get(self):
        only_latest = False
        only_published = False

        args = parser_components.parse_args()

        mockCat = MockCatalog()
        comps = mockCat.list_components(latest=only_latest, only_published=only_published)

        def format_record_component(obj):
            def format_value(v):
                if type(v) == datetime:
                    return v.isoformat()
                else:
                    return v
            def to_camel_case(snake_str):
                components = snake_str.split('_')
                # We capitalize the first letter of each component except the first one
                # with the 'title' method and join them together.
                return components[0] + ''.join(x.title() for x in components[1:])

            return dict([(to_camel_case(k), format_value(v)) \
                            for k,v in obj.items()])

        def add_self_url(comp):
            comp["componentUrl"] = fields.Url("resource_component", absolute=True) \
                                       .output(None, {"component_id": comp["id"]})
            return comp

        def add_status(comp):
            # "whenRevoked" and "whenPublished" are used to get status 
            comp["status"] = util.get_status_string_camel(comp)
            return comp

        def should_keep(comp):
            """Takes args to be used to filter the list of components"""
            ok_name = args["name"] == None or args["name"] == comp["name"]
            ok_version = args["version"] == None or args["version"] == comp["version"]
            return ok_name and ok_version

        comps = [ add_self_url(add_status(format_record_component(comp)))
                for comp in comps if should_keep(comp) ]

        return  { "components": comps }, 200


    @_api.doc("post_component", description="Add a Component to the Catalog", body=component_fields_request)
    @_api.marshal_with(component_post)
    @_api.response(200, 'Success, Component added')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(409, 'Component already exists', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(component_fields_request)
    def post(self):
        resp = None
        try:
            http_body = request.get_json()

            user = http_body['owner']
            spec = http_body['spec']
            try:
                name    = spec['self']['name']
                version = spec['self']['version']
            except Exception:
                raise DcaeException("(Component) Spec needs to have a 'self' section with 'name' and 'version'")
                
            mockCat = MockCatalog()
            ''' Pass False to do an add vs update '''
            mockCat.add_component(user, spec, False)

            component_id = mockCat.get_component_id(name, version)
            componentUrl = fields.Url("resource_component", absolute=True) \
                               .output(None, {"component_id": component_id})
            resp = {"componentUrl": componentUrl}

        except KeyError as e:
            abort(code=400, message="Request field missing: {}".format(e))
        except DuplicateEntry as e:
            resp = e.message.replace("name:version", name + ":" + version)
            # We abort flask_restplus so our error message will override "marshal_with()" in response body
            abort(code=409, message=resp)
        except (CatalogError, DcaeException) as e:
            abort(code=400, message=e)

        return resp, 200


######################
## Component by ID  ##
######################
@_api.route("/components/<string:component_id>", endpoint="resource_component")
class Component(Resource):
    @_api.doc("get_component", description="Get a Component")
    @_api.marshal_with(component_fields_by_id)
    @_api.response(200, 'Success, Component retrieved')
    @_api.response(404, 'Component not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    def get(self, component_id):
        resp = None
        try:
            mockCat = MockCatalog()
            comp = mockCat.get_component_by_id(component_id)
            status = util.get_status_string(comp)
                        
            resp = { "id":             comp["id"]
                    , "name":          comp['name']
                    , "version":       comp['version']
                    , "whenAdded":     comp['when_added'].isoformat()
                    , "modified":      comp["modified"].isoformat()
                    , "owner":         comp["owner"]
                    , "description":   comp['description']
                    , "componentType": comp['component_type']
                    , "spec":          json.loads(comp["spec"])
                    , "componentUrl":  fields.Url("resource_component", absolute=True)
                                          .output(None, {"component_id": comp["id"]})
                    , "status":        status
                    }

        except MissingEntry as e:
            abort(code=404, message=e)

        return resp, 200


    @_api.doc("put_component", description="Replace a Component Spec in the Catalog", body=component_fields_request)
    @_api.response(200, 'Success, Component replaced')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(404, 'Component not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(component_fields_request)
    def put(self, component_id):
        resp = None
        try:
            http_body = request.get_json()
            user = http_body['owner']
            spec = http_body['spec']
            mockCat = MockCatalog()
            ''' Pass True to do an update vs add '''
            mockCat.add_component(user, spec, True)

        except MissingEntry as e:
            abort(code=404, message=e)
        except (FrozenEntry, CatalogError, DcaeException) as e:
            abort(code=400, message=e)

        return resp, 200
    

    @_api.doc("patch_component", description="Update a Component's status in the Catalog", body=patch_fields)
    @_api.response(200, 'Success, Component status updated')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(403, 'Forbidden Request', model=error_message)    
    @_api.response(404, 'Component not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(patch_fields)
    def patch(self, component_id):
        resp = None
        try:
            http_body = request.get_json()
            user  = http_body['owner']
            field = http_body['status']
            if field not in ['published', 'revoked']:
                raise DcaeException("Unknown status in request: '{}'".format(field))
            if field == 'revoked':
                raise DcaeException("This status is not supported yet: '{}'".format(field))
            
            mockCat = MockCatalog()
            comp         = mockCat.get_component_by_id(component_id)
            comp_name    = comp['name']
            comp_version = comp['version']

            mockCat.publish_component(user, comp_name, comp_version)

        except MissingEntry as e:
            abort(code=404, message=e)
        except ForbiddenRequest as e:
            abort(code=403, message=e)
        except (CatalogError, DcaeException) as e:
            abort(code=400, message=e)

        return resp, 200
    
    
###################
##  Data Format  ##
###################
@_api.route("/dataformats", endpoint="resource_formats")
class DataFormats(Resource):
    """Data Format resource"""
    @_api.doc("get_dataformats", description="Get list of Data Formats in the catalog")
    @_api.marshal_with(dataformats_get)
    @_api.response(200, 'Success, Data Formats retrieved')
    @_api.response(500, 'Internal Server Error')
    def get(self):
        only_latest = False
        only_published = False

        mockCat = MockCatalog()
        formats = mockCat.list_formats(latest=only_latest, only_published=only_published)

        def format_record_dataformat(obj):

            def format_value(v):
                if type(v) == datetime:
                    return v.isoformat()
                else:
                    return v

            def to_camel_case(snake_str):
                components = snake_str.split('_')
                # We capitalize the first letter of each component except the first one
                # with the 'title' method and join them together.
                return components[0] + ''.join(x.title() for x in components[1:])

            return dict([(to_camel_case(k), format_value(v)) \
                            for k,v in obj.items()])

        formats = [ format_record_dataformat(format) for format in formats ]

        def add_self_url(format):
            format["dataFormatUrl"] = fields.Url("resource_format", absolute=True) \
                                       .output(None, {"dataformat_id": format["id"]})
            return format

        formats = [ add_self_url(format) for format in formats ]

        def add_status(format):
            # "whenRevoked" and "whenPublished" are used to get status 
            format["status"] = util.get_status_string_camel(format)

            return format

        formats = [ add_status(format) for format in formats ]

        return  { "dataFormats": formats }, 200
    
    
    @_api.doc("post_dataformat", description="Add a Data Format to the Catalog", body=dataformat_fields_request)
    @_api.marshal_with(dataformat_post)
    @_api.response(200, 'Success, Data Format added')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(409, 'Data Format already exists', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(dataformat_fields_request)
    def post(self):
        resp = None
        try:
            http_body = request.get_json()
            user = http_body['owner']
            spec = http_body['spec']
            try:
                name    = spec['self']['name']
                version = spec['self']['version']
            except Exception:
                raise DcaeException("(Data Format) Spec needs to have a 'self' section with 'name' and 'version'")
            
            mockCat = MockCatalog()
            ''' Pass False to do an add vs update '''
            mockCat.add_format(spec, user, False)

            dataformat_id = mockCat.get_dataformat_id(name, version)
            dataformatUrl = fields.Url("resource_format", absolute=True) \
                                .output(None, {"dataformat_id": dataformat_id})

            resp = {"dataFormatUrl": dataformatUrl}

        except KeyError as e:
            abort(code=400, message="Request field missing: {}".format(e))
        except DuplicateEntry as e:
            resp = e.message.replace("name:version", name + ":" + version)
            abort(code=409, message=resp)
        except (CatalogError, DcaeException) as e:
            abort(code=400, message=e)
            
        return resp, 200


#########################
##  Data Format by ID  ##
#########################
@_api.route("/dataformats/<string:dataformat_id>", endpoint="resource_format")
class DataFormat(Resource):
    @_api.doc("get_dataformat", description="Get a Data Format")
    @_api.marshal_with(dataformat_fields_by_id)
    @_api.response(200, 'Success, Data Format retrieved')
    @_api.response(404, 'Data Format not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    def get(self, dataformat_id):
        resp = None
        try:
            mockCat = MockCatalog()
            format = mockCat.get_dataformat_by_id(dataformat_id)
            status = util.get_status_string(format)

            resp = { "id": format["id"]
                    , "name":          format['name']
                    , "version":       format['version']
                    , "whenAdded":     format["when_added"].isoformat()
                    , "modified":      format["modified"].isoformat()
                    , "owner":         format["owner"]
                    , "description":   format["description"]
                    , "spec":          json.loads(format["spec"])
                    , "dataFormatUrl": fields.Url("resource_format", absolute=True)
                                        .output(None, {"dataformat_id": format["id"]})
                    , "status":   status
                    }

        except MissingEntry as e:
            abort(code=404, message=e)

        return resp, 200
    
    
    @_api.doc("put_dataformat", description="Replace a Data Format Spec in the Catalog", body=dataformat_fields_request)
    @_api.response(200, 'Success, Data Format added')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(404, 'Data Format not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(dataformat_fields_request)
    def put(self, dataformat_id):
        resp = None
        try:
            http_body = request.get_json()
            user = http_body['owner']
            spec = http_body['spec']
            mockCat = MockCatalog()
            ''' Pass True to do an update vs add '''
            mockCat.add_format(spec, user, True)

        except MissingEntry as e:
            abort(code=404, message=e)
        except (CatalogError, FrozenEntry, DcaeException) as e:
            abort(code=400, message=e)

        return resp, 200


    @_api.doc("patch_dataformat", description="Update a Data Format's status in the Catalog", body=patch_fields)
    @_api.response(200, 'Success, Data Format status updated')
    @_api.response(400, 'Bad Request', model=error_message)
    @_api.response(403, 'Forbidden Request', model=error_message)
    @_api.response(404, 'Data Format not found in Catalog', model=error_message)
    @_api.response(500, 'Internal Server Error')
    @_api.expect(patch_fields)
    def patch(self, dataformat_id):
        resp = None
        try:
            http_body = request.get_json()
            user  = http_body['owner']
            field = http_body['status']
            if field not in ['published', 'revoked']:
                raise DcaeException("Unknown status in request: '{}'".format(field))
            if field == 'revoked':
                raise DcaeException("This status is not supported yet: '{}'".format(field))
            
            mockCat = MockCatalog()
            dataformat         = mockCat.get_dataformat_by_id(dataformat_id)
            dataformat_name    = dataformat['name']
            dataformat_version = dataformat['version']

            mockCat.publish_format(user, dataformat_name, dataformat_version)

        except MissingEntry as e:
            abort(code=404, message=e)
        except ForbiddenRequest as e:
            abort(code=403, message=e)
        except (CatalogError, DcaeException) as e:
            abort(code=400, message=e)

        return resp, 200


def start_http_server(catalog, debug=True):
    if debug:
        _app.run(debug=True)
    else:
        _app.run(host="0.0.0.0", port=80, debug=False)
