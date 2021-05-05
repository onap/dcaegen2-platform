# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
# =============================================================================
# Copyright (c) 2021 highstreet technologies GmbH. All rights reserved.
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
import requests

from testing_helpers import get_fixture_path as get_test_file

from aoconversion import docker_gen as aoc_docker_gen
from aoconversion import scanner as aoc_scanner

#
# General mocking
#


class _MockModule:
    """
    Class to mock packages.
    Just a quick way to create an object with specified attributes
    """
    def __init__(self, **kwargs):
        for k, v in kwargs.items():
            setattr(self, k, v)


#
# Mocking for package "docker"
#


class _MockAPIClient:
    """
    Class to mock docker.APIClient class.
    """
    def __init__(self, base_url, version=None, user_agent='xxx'):
        pass

    def build(self, path, rm, tag):
        return [b'some message', b'another message']

    def push(self, repository, auth_config, stream):
        return [b'some message', b'another message']

    def images(self, x):
        return True


def _mock_kwargs_from_env(**kwargs):
    """
    Method to mock docker.utils.kwargs_from_env method.
    """
    return {'base_url': None}


_mockdocker = _MockModule(
    APIClient=_MockAPIClient,
    utils=_MockModule(kwargs_from_env=_mock_kwargs_from_env))


#
# Mocking for requests.get
#


class _r:
    """
    Fake responses for mocking requests.get
    """
    def __init__(self, json=None, file=None, data=None):
        self.jx = json
        self.fx = file
        self.dx = data
        self.status_code = 200

    @property
    def text(self):
        return self._raw().decode()

    def raise_for_status(self):
        pass

    def _raw(self):
        if self.dx is None:
            if self.fx is not None:
                with open(self.fx, 'rb') as f:
                    self.dx = f.read()
            elif self.jx is not None:
                self.dx = json.dumps(self.jx, sort_keys=True).encode()
            else:
                self.dx = b''
        return self.dx

    def iter_content(self, bsize=-1):
        buf = self._raw()
        pos = 0
        lim = len(buf)
        if bsize <= 0:
            bsize = lim
        while pos + bsize < lim:
            yield buf[pos:pos + bsize]
            pos = pos + bsize
        yield buf[pos:]

    def json(self):
        if self.jx is None:
            self.jx = json.loads(self._raw().decode())
        return self.jx


def _mockwww(responses):
    def _op(path, json=None, auth=None, cert=None, verify=None, stream=False):
        return responses[path]
    return _op


_mockpostdata = {
    'https://onboarding/dataformats': _r({'dataFormatUrl': 'https://onboarding/dataformats/somedfid'}),
    'https://onboarding/components': _r({'componentUrl': 'https://onboarding/components/somedxid'}),
}

_mockpatchdata = {
    'https://onboarding/dataformats/somedfid': _r({}),
    'https://onboarding/components/somedxid': _r({}),
}

_mockwebdata = {
    'https://acumos/catalogs': _r({'content': [{'catalogId': 'c1'}]}),
    'https://acumos/solutions?catalogId=c1': _r({'content': [{'solutionId': 's1', 'name': 'example-model', 'ratingAverageTenths': 17}]}),
    'https://acumos/solutions/s1/revisions': _r({'content': [{'revisionId': 'r1'}]}),
    'https://acumos/solutions/s1/revisions/r1': _r({'content': {
        'version': 'v1',
        'modified': '2019-01-01T00:00:00Z',
        'artifacts': [
            {'artifactId': 'a1', 'name': 'xxx.other'},
            {'artifactId': 'a2', 'name': 'xxx.proto'},
            {'artifactId': 'a3', 'name': 'xxx.zip'},
            {'artifactId': 'a4', 'name': 'xxx.json'},
        ]}
    }),
    'https://acumos/artifacts/a2/content': _r(file=get_test_file('models/example-model/model.proto')),
    'https://acumos/artifacts/a3/content': _r(data=b'dummy zip archive data'),
    'https://acumos/artifacts/a4/content': _r(file=get_test_file('models/example-model/metadata.json')),
}


#
# End mocking tools
#


def test_aoconversion(mock_schemas, tmpdir, monkeypatch):
    config = aoc_scanner.Config(dcaeurl='http://dcaeurl', dcaeuser='dcaeuser', onboardingurl='https://onboarding', onboardinguser='obuser', onboardingpass='obpass', acumosurl='https://acumos', certfile=None, dockerregistry='dockerregistry', dockeruser='registryuser', dockerpass='registrypassword', http_proxy='', https_proxy='', no_proxy='')
    monkeypatch.setattr(aoc_docker_gen, 'APIClient', _mockdocker.APIClient)
    monkeypatch.setattr(requests, 'get', _mockwww(_mockwebdata))
    monkeypatch.setattr(requests, 'post', _mockwww(_mockpostdata))
    monkeypatch.setattr(requests, 'patch', _mockwww(_mockpatchdata))
    aoc_scanner.scan(config)
    aoc_scanner.scan(config)


def test__derefconfig():
    config_path = get_test_file('config.yaml')
    assert aoc_scanner._derefconfig('@' + config_path) == 'dcaeurl: https://git.onap.org/dcaegen2/platform/plain/mod'
