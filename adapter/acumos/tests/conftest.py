# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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

import os
import pytest
import requests
import aoconversion


@pytest.fixture
def mock_schemas(monkeypatch):
    cwd = os.getcwd()
    schemadir = cwd[:cwd.find('/adapter/acumos')] + '/mod/component-json-schemas'
    monkeypatch.setattr(aoconversion.utils.component_schema, 'path', schemadir + '/component-specification/dcae-cli-v2/component-spec-schema.json')
    monkeypatch.setattr(aoconversion.utils.dataformat_schema, 'path', schemadir + '/data-format/dcae-cli-v1/data-format-schema.json')
    monkeypatch.setattr(aoconversion.utils.schema_schema, 'ret', requests.get('https://json-schema.org/draft-04/schema#').json())
