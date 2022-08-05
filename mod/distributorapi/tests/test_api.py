# ============LICENSE_START=======================================================
# Copyright (c) 2020-2022 AT&T Intellectual Property. All rights reserved.
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

"""
Tests that require a mock requests module, plus a few more
that didn't fit cleanly elsewhere.
"""

import copy
import os
import re
import pytest
import requests
from distributor.http import _app as app
from distributor import config
from distributor import onboarding_client
from distributor import utils
from distributor import errors
from distributor import data_access
from distributor import transform

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
    # in the test code, you can set
    #    _req.SHOWMATCHES = True
    # and the match results will be displayed
    SHOWMATCHES = False

    def __init__(self, op, url, resp):
        self.op = op
        self.url = url;
        self.resp = resp

    def check(self, op, url):
        if _req.SHOWMATCHES:
            print(f"_req.check(op={op} vs {self.op}, url={url} vs {self.url})")
        return self.resp if op == self.op and url == self.url else None

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


def isdate(dt):
    """ verify that a string looks like an iso8901 date/time string YYYY-MM-DDTHH:MM:SS.MS """
    return re.match(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}[.]\d+$", dt)


def isuuid(gu):
    """ verify that a string looks like a guid  """
    return re.match(r"[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$", gu)


config.init()

def test_api(client, mockrequests):
    env_name = "TEST_API_GRAB_ENVIRON"
    os.environ[env_name] = "xyz"
    assert(config._grab_env(env_name, "foo") == "xyz")
    assert(config._grab_env(env_name) == "xyz")
    del os.environ[env_name]
    assert(config._grab_env(env_name, "foo") == "foo")
    try:
        config._grab_env(env_name)
        assert(not "config._grab_env(env_name) should throw errors.DistributorAPIConfigError")
    except errors.DistributorAPIConfigError as e:
        # expected result
        pass

    dummyflow = {'link': {'href': 'buckets/link1/flows/flow1'}, 'name': 'flowname'}

    nifi_url = 'http://nifi-registry:18080/nifi-registry-api'
    mockrequests.extend([
        _req('GET', nifi_url + '/buckets',
             _resp(200, [{'link': {'href':'buckets/link1'}}])),
        _req('GET', nifi_url + '/buckets/link1/flows',
             _resp(200, [dummyflow])),
	_req('POST', 'http://newtarget1/url/api/graph/main',
	     _resp(200, {'id':'group1'})),
        _req('GET', '/does/not/exist',
             _resp(404, [{'link': {'href':'does/not/exist'}}])),
        _req('GET', '/distributor/distribution-targets/components?name=foo&version=bar',
             _resp(200, {'id':'groupd', 'components':[{
                 "componentUrl": "COMPONENTURL"}]
             })),
        _req('GET', 'COMPONENTURL',
             _resp(200, {'id': 'groupComponentUrl'})),
        _req('GET', '/distributor/distribution-targets/components?name=foo&version=bar2',
             _resp(200, {'id':'groupd', 'components':None
             })),
        
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

    # print(resp.get_json())
    url2 = '/distributor/distribution-targets/' + resp.get_json()['id']
    url3 = url2 + '/process-groups'
    assert(len(client.get(url).get_json()['distributionTargets']) == 1)

    assert(client.get(url2).status_code == 200)
    assert(client.put(url2, json={'name': 'newtarget1', 'runtimeApiUrl': 'http://newtarget1/url'}).status_code == 200)
    assert(client.put(url2, json={'name': 'newtarget1', 'runtimeApiUrl': 'http://newtarget1/url'}).status_code == 200)

    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 404)
    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 404)
    dummyflow['identifier'] = 'group1'
    assert(client.post(url3, json={'processGroupId': 'group1'}).status_code == 501)

    assert(client.delete(url2).status_code == 200)
    assert(client.delete(url2).status_code == 404)
    url4 = '/does/not/exist'

    # the following tests do not require an http client but do use requests lib

    # test get_json() exception case
    try:
        utils.get_json(url4)
        assert(not "utils.get_json(url4) should throw errors.DistributorAPIError")
    except errors.DistributorAPIError as e:
        # expected result
        pass

    # _req.SHOWMATCHES = True
    ret = onboarding_client.get_components_indexed(url, [("foo", "bar")])
    assert ret == {('foo', 'bar'): {'id': 'groupComponentUrl'}}

    # 
    try:
        ret = onboarding_client.get_components_indexed(url, [("foo", "bar2")])
        assert(not "onboarding_client.get_components_indexed(...foo,bar2) should throw errors.DistributorAPIResourceNotFound")
    except errors.DistributorAPIResourceNotFound as e:
        # expected result
        pass


def test_data_access():
    # various tests for data_access.py

    saved_cache = copy.deepcopy(data_access.get_distribution_targets())
    ret = data_access.get_distribution_target("ds")
    assert(ret == {})

    # new transform_request()
    req1 = {"name": "req1", "runtimeApiUrl": "rtau1", "nextDistributionTargetId": "ndti1"}
    treq1 = data_access.transform_request(req1)
    assert(isdate(treq1['created']))
    assert(isdate(treq1['modified']))
    assert(isuuid(treq1['dt_id']))
    assert(treq1['processGroups'] == [])
    

    # new transform_request()
    req2 = {"name": "req2", "runtimeApiUrl": "rtau2", "nextDistributionTargetId": "ndti1"}
    treq2 = data_access.transform_request(req2)
    assert(isdate(treq2['created']))
    assert(isdate(treq2['modified']))
    assert(isuuid(treq2['dt_id']))
    assert(treq2['processGroups'] == [])

    # merge_request() should copy certain values from 2nd arg into 1st arg
    ret = data_access.merge_request(treq1, treq2)
    assert(ret["name"] == treq2["name"])
    assert(ret["runtimeApiUrl"] == treq2["runtimeApiUrl"])
    assert(ret["description"] is None)
    assert(ret["nextDistributionTargetId"] == treq2["nextDistributionTargetId"])

    # add_distribution_target() adds to the cache
    ret = data_access.add_distribution_target({"dt_id": "dt1", "val": "1", "processGroups": []})
    assert(data_access.get_distribution_target('dt1')["val"] == "1")

    # update_distribution_target() updates an existing element of the cache
    # If the element exists, it returns True
    ret = data_access.update_distribution_target({"dt_id": "dt1", "val": "1b", "processGroups": []})
    assert(ret)
    assert(data_access.get_distribution_target('dt1')["val"] == "1b")

    # update_distribution_target() updates an existing element of the cache
    # If the element does not exist, it returns False
    ret = data_access.update_distribution_target({"dt_id": "dt2", "val": "2", "processGroups": []})
    assert(not ret)


    # add_process_group adds an element to the processGroups array of the distribution target
    # if the element exists, returns true, else false
    assert(data_access.add_process_group("dt1", {"processed": "p1"}))
    assert(isdate(data_access.get_distribution_target('dt1')["processGroups"][0]["processed"]))
    assert(not data_access.add_process_group("dt2", {"processed": "p1"}))

    # clean up the cache
    # if the element exists, 
    assert(data_access.delete_distribution_target("dt1"))
    assert(not data_access.delete_distribution_target("dt2"))
    
    assert(data_access.get_distribution_targets() == saved_cache)

def test_transform():
    # various tests for transform.py
    flow1 = {
        "flowContents": {
            "processors": [
            ]
        }
    }
    flow2 = {
        "flowContents": {
            "processors": [
                {
                    "bundle": {
                        "artifact": "artifact1",
                        "version": "version1"
                    },
                }
            ]
        }
    }
    flow3 = {
        "flowContents": {
            "processors": [
                {
                    "bundle": {
                        "artifact": "artifact1",
                        "version": "version1"
                    },
                }, {
                    "bundle": {
                        "artifact": "artifact2",
                        "version": "version2"
                    }
                }
            ]
        }
    }
    assert(transform.extract_components_from_flow(flow1) == [])
    assert(transform.extract_components_from_flow(flow2) == [('artifact1', 'version1')])
    assert(transform.extract_components_from_flow(flow3) == [('artifact1', 'version1'), ('artifact2', 'version2')])
