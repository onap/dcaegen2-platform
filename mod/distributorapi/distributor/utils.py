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
"""Utility functions"""

from urllib.parse import quote
import requests as reqs
from distributor import errors


def urljoin(base, *trailing, **query_params):
    base = base[0:-1] if base[-1] == "/" else base
    full = [base] + list(trailing)
    url = "/".join(full)

    if query_params:
        qp = ["{0}={1}".format(quote(k), quote(str(v))) for k, v in query_params.items()]
        qp = "&".join(qp)
        return "?".join([url, qp])
    else:
        return url


def get_json(url):
    resp = reqs.get(url)

    try:
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        raise errors.DistributorAPIError(e)
