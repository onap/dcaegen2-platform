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
"""Onboarding API client"""

from distributor.utils import urljoin, get_json
from distributor import errors


def get_component(onboarding_url, name, version):
    url = urljoin(onboarding_url, "components", **{"name": name, "version": version})
    result = get_json(url)["components"]

    if result:
        return get_json(result[0]["componentUrl"])
    else:
        raise errors.DistributorAPIResourceNotFound("Onboarding API: Component not found")


def get_components_indexed(onboarding_url, list_name_version):
    return dict([((c[0], c[1]), get_component(onboarding_url, c[0], c[1])) for c in list_name_version])
