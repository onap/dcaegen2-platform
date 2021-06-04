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

import json


class FetchSchemaError(RuntimeError):
    pass


class _Schema:
    def __init__(self, path):
        self.ret = None
        self.path = path

    def get(self):
        try:
            if self.ret is None:
                with open(self.path, 'r') as f:
                    self.ret = json.loads(f.read())
            return self.ret
        except Exception as e:
            raise FetchSchemaError("Unexpected error from fetching schema", e)


schema_schema = _Schema('schemas/schema.json')
component_schema = _Schema('schemas/compspec.json')
dataformat_schema = _Schema('schemas/dataformat.json')


def get_metadata(model_repo_path, model_name):
    # for now, assume it's called "metadata.json"
    return json.loads(open("{0}/{1}/metadata.json".format(model_repo_path, model_name), "r").read())


def validate_format(meta, method, type):
    try:
        df_name = meta["methods"][method][type]["name"]

    except TypeError:
        df_name = meta["methods"][method][type]
    return df_name
