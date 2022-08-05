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
"""Configuration for distributor api"""
import os, tempfile, six, inspect
from datetime import datetime
from distributor import errors


def _grab_env(name, default=None):
    try:
        if default:
            return os.environ.get(name, default)
        else:
            return os.environ[name]
    except KeyError:
        raise errors.DistributorAPIConfigError("Required environment variable missing: {0}".format(name))


def init():
    global nifi_registry_url
    nifi_registry_url = _grab_env("NIFI_REGISTRY_URL", default="http://nifi-registry:18080/nifi-registry-api")

    global onboarding_api_url
    onboarding_api_url = _grab_env("ONBOARDING_API_URL", default="http://onboarding-api:8080/onboarding")
