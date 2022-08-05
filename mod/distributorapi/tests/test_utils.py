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
from distributor import utils


# more tests are in test_api.py

def test_urljoin():
    assert "http://foo/bar/baz" == utils.urljoin("http://foo", "bar", "baz")
    assert "http://foo/bar/baz" == utils.urljoin("http://foo/", "bar", "baz")
    assert "http://foo/bar/baz?name=some-name&version=1.5.0" \
        == utils.urljoin("http://foo", "bar", "baz", **{"name": "some-name",
            "version": "1.5.0"})
