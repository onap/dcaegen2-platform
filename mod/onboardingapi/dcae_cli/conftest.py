# ============LICENSE_START=======================================================
# org.onap.dcae
# ================================================================================
# Copyright (c) 2018-2020 AT&T Intellectual Property. All rights reserved.
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
#
# ECOMP is a trademark and service mark of AT&T Intellectual Property.
"""
This module is actually for pytesting. This contains fixtures.
"""

import pytest
import dcae_cli

# REVIEW: Having issues trying to share this amongst all the tests. Putting this
# fixture here allows it to be shared when running tests over the entire project.
# The pytest recommendation was to place this file high up in the project.

@pytest.fixture
def mock_schemas(monkeypatch):
    import os
    cwd = os.getcwd()
    schemadir = cwd[:cwd.find('/onboardingapi')] + '/component-json-schemas'
    monkeypatch.setattr(dcae_cli.catalog.mock.schema.component_schema, 'path', schemadir + '/component-specification/dcae-cli-v2/component-spec-schema.json')
    monkeypatch.setattr(dcae_cli.catalog.mock.schema.dataformat_schema, 'path', schemadir + '/data-format/dcae-cli-v1/data-format-schema.json')
    
@pytest.fixture
def mock_cli_config(mock_schemas, monkeypatch):
    """Fixture to provide a mock dcae-cli configuration and profiles

    This fixture monkeypatches the respective get calls to return mock objects
    """
    # NOTE: The component spec and data format in gerrit moved once already.
    # Might move again..
    fake_config = { "active_profile": "default",
            "server_url": "https://git.onap.org/dcaegen2/platform/cli/plain",
            "db_url": "postgresql://postgres:abc123@localhost:5432/dcae_onboarding_db",
            "path_component_spec": "/component-json-schemas/component-specification/dcae-cli-v2/component-spec-schema.json",
            "path_data_format": "/component-json-schemas/data-format/dcae-cli-v1/data-format-schema.json"
            }

    fake_profiles = { "default": { "consul_host": "consul",
        "cdap_broker": "cdap_broker",
        "config_binding_service": "config_binding_service",
        "docker_host": "docker_host" }
        }
    fake_profiles["active"] = fake_profiles["default"]

    def fake_get_config():
        return fake_config

    def fake_get_profiles(user_only=False, include_active=True):
        return fake_profiles

    from dcae_cli.util import config, profiles
    monkeypatch.setattr(dcae_cli.util.config, "get_config", fake_get_config)
    monkeypatch.setattr(dcae_cli.util.profiles, "get_profiles", fake_get_profiles)


@pytest.fixture
def mock_db_url(tmpdir):
    """Fixture to provide mock db url

    This url is intended to be the location of where to place the local sqlite
    databases for each unit test"""
    dbname="dcae_cli.test.db"
    config_dir = tmpdir.mkdir("config")
    return "/".join(["sqlite://", str(config_dir), dbname])
