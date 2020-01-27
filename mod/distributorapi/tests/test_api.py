# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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

from distributor.http import _app as app
from distributor import config
import pytest
import requests

class _resp(object):
    def __init__(self, code, json = None):
        self.status_code = code
        if json is not None:
            self._json = json
    
    def json(self):
        return self._json

    def raise_for_status(self):
        if self.status_code < 200 or self.status_code >= 300:
            raise Exception('Error response {}'.format(self.status_code))

class _req(object):
    def __init__(self, op, url, resp):
        self.op = op
        self.url = url;
        self.resp = resp

    def check(self, op, url):
        if op != self.op or url != self.url:
            return None
        return self.resp

def _match(answers, op, url):
    for choice in answers:
        ret = choice.check(op, url)
        if ret is not None:
            return ret
    message = 'Unexpected request {} {}'.format(op, url)
    print(message)
    raise Exception(message)

@pytest.fixture
def mockrequests(monkeypatch):
    answers = []
    def get(url, headers = None):
        return _match(answers, 'GET', url)
    
    def post(url, json, headers = None):
        return _match(answers, 'POST', url)

    def put(url, json, headers = None):
        return _match(answers, 'PUT', url)

    def delete(url, headers = None):
        return _match(answers, 'DELETE', url)

    monkeypatch.setattr(requests, 'get', get)
    monkeypatch.setattr(requests, 'post', post)
    monkeypatch.setattr(requests, 'put', put)
    monkeypatch.setattr(requests, 'delete', delete)
    return answers

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

config.init()

def test_api(client, mockrequests):
    dummyflow = {'link': {'href': 'buckets/link1/flows/flow1'}, 'name': 'flowname'}
    mockrequests.extend([
        _req('GET', 'http://nifi-registry:18080/nifi-registry-api/buckets',
            _resp(200, [{'link': {'href':'buckets/link1'}}])),
        _req('GET', 'http://nifi-registry:18080/nifi-registry-api/buckets/link1/flows',
            _resp(200, [dummyflow])),
	_req('POST', 'http://newtarget1/url/api/graph/main',
	    _resp(200, {'id':'group1'}))
    ])
    for rule in app.url_map.iter_rules():
      print(rule)
    url = '/distributor/distribution-targets'
    url2 = url + '/notfound'
    url3 = url2 + '/process-groups'
    assert(len(client.get(url).get_json()['distributionTargets']) == 0)
    assert(client.get(url2).status_code == 404)
    assert(client.put(url2, json={'name': 'notfound1', 'runtimeApiUrl': 'http://notfound/url'}).status_code == 404)
    assert(client.delete(url2).status_code == 404)
    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 404)
    resp = client.post(url, json={'name': 'target1', 'runtimeApiUrl': 'http://target/url'})
    assert(resp.status_code == 200)
    print(resp.get_json())
    url2 = '/distributor/distribution-targets/' + resp.get_json()['id']
    url3 = url2 + '/process-groups'
    assert(len(client.get(url).get_json()['distributionTargets']) == 1)
    assert(client.get(url2).status_code == 200)
    assert(client.put(url2, json={'name': 'newtarget1', 'runtimeApiUrl': 'http://newtarget1/url'}).status_code == 200)
    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 404)
    dummyflow['identifier'] = 'group1'
    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 501)
    assert(client.delete(url2).status_code == 200)
    assert(client.delete(url2).status_code == 404)
