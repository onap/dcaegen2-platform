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
import os
from pkg_resources import resource_string
import shutil
import traceback
try:
    from urllib.parse import unquote_plus
    from socketserver import ThreadingMixIn
    from http.server import BaseHTTPRequestHandler, HTTPServer
except ImportError:
    from urllib import unquote_plus
    from SocketServer import ThreadingMixIn
    from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

import requests

from aoconversion import convert


def _derefconfig(value):
    if value.startswith('@'):
        with open(value[1:], 'r') as f:
            return f.readline().strip()
    return value


class Config(object):
    """
    Configuration parameters as attributes, make sure the required ones are there,
    populate defaults.
    """
    def __init__(self, dcaeuser, onboardingurl, onboardinguser, onboardingpass, certfile, dockerregistry, dockeruser, dockerpass, acumosurl=None, interval=900, dockerhost='tcp://localhost:2375', tmpdir='/var/tmp/aoadapter', certverify=True, catalogs=None, port=None, **extras):
        self.dcaeuser = dcaeuser

        def x(fmt, *args, **kwargs):
            return onboardingurl + fmt.format(*args, **kwargs)
        self.oburl = x
        self._onboardingpass = onboardingpass
        self._onboardinguser = onboardinguser
        self.acumosurl = acumosurl
        self.certfile = certfile
        self.certverify = certverify
        self.dockerhost = dockerhost
        self.dockerregistry = dockerregistry
        self.dockeruser = dockeruser
        self._dockerpass = dockerpass
        self.interval = interval
        self.tmpdir = tmpdir
        if catalogs is not None and type(catalogs) is not list:
            catalogs = [catalogs]
        self.catalogs = catalogs
        self.port = port

    def obauth(self):
        return (self._onboardinguser, _derefconfig(self._onboardingpass))

    def dockerpass(self):
        return _derefconfig(self._dockerpass)


class _AcumosAccess(object):
    def __init__(self, config, url):
        self.cert = config.certfile
        self.verify = config.certverify
        self.url = url.strip().rstrip('/')

    def artgetter(self, xrev, matcher):
        for art in xrev['artifacts']:
            if matcher(art):
                def ret():
                    nurl = '{}/artifacts/{}/content'.format(self.url, art['artifactId'])
                    resp = requests.get(nurl, stream=True, cert=self.cert, verify=self.verify)
                    if resp.status_code == 500:
                        resp = requests.get(nurl, stream=True, cert=self.cert, verify=self.verify)
                    resp.raise_for_status()
                    return resp
                return ret
        return None

    def jsonget(self, format, *args, **kwargs):
        nurl = self.url + format.format(*args, **kwargs)
        resp = requests.get(nurl, cert=self.cert, verify=self.verify)
        if resp.status_code == 500:
            resp = requests.get(nurl, cert=self.cert, verify=self.verify)
        resp.raise_for_status()
        return resp.json()['content']


def _x_proto_matcher(art):
    """ Is this artifact the x.proto file? """
    return art['name'].endswith('.proto')


def _x_zip_matcher(art):
    """ Is this artifact the x.zip file? """
    return art['name'].endswith('.zip')


def _md_json_matcher(art):
    """ Is this artifact the metadata.json file? """
    return art['name'].endswith('.json')


def _walk(config):
    """
    Walk the Federation E5 interface of an Acumos instance
    """
    url = config.acumosurl
    callback = _makecallback(config)
    catalogs = config.catalogs
    aa = _AcumosAccess(config, url)
    for catalog in aa.jsonget('/catalogs'):
        if catalogs is not None and catalog['catalogId'] not in catalogs and catalog['name'] not in catalogs:
            continue
        for solution in aa.jsonget('/solutions?catalogId={}', catalog['catalogId']):
            for revision in aa.jsonget('/solutions/{}/revisions', solution['solutionId']):
                onboard(aa, callback, solution, revision['revisionId'])


def onboard(aa, callback, solution, revid):
    xrev = aa.jsonget('/solutions/{}/revisions/{}', solution['solutionId'], revid)
    callback(model_name=solution['name'], model_version=xrev['version'], model_last_updated=xrev['modified'], rating=solution['ratingAverageTenths'] / 10.0, proto_getter=aa.artgetter(xrev, _x_proto_matcher), zip_getter=aa.artgetter(xrev, _x_zip_matcher), metadata_getter=aa.artgetter(xrev, _md_json_matcher))


def _pullfile(source, dest):
    with open(dest, 'wb') as f:
        for chunk in source().iter_content(65536):
            f.write(chunk)


_loadedformats = set()
_loadedcomponents = set()


def scan(config):
    _walk(config)


def _makecallback(config):
    workdir = config.tmpdir
    obauth = config.obauth()
    oburl = config.oburl

    def callback(model_name, model_version, model_last_updated, rating, proto_getter, zip_getter, metadata_getter):
        model_name = model_name.lower()
        model_version = model_version.lower()
        compid = (model_name, model_version)
        if compid in _loadedcomponents:
            print('Skipping component {}: already analyzed'.format(compid))
            return
        if proto_getter is None or zip_getter is None or metadata_getter is None:
            print('Skipping component {}: does not have required artifacts'.format(compid))
            _loadedcomponents.add(compid)
            return
        modeldir = '{}/{}'.format(workdir, model_name)
        shutil.rmtree(modeldir, True)
        os.makedirs(modeldir)
        try:
            _pullfile(proto_getter, '{}/model.proto'.format(modeldir))
            _pullfile(zip_getter, '{}/model.zip'.format(modeldir))
            _pullfile(metadata_getter, '{}/metadata.json'.format(modeldir))
        except Exception:
            print('Skipping component {}: artifact access error'.format(compid))
            _loadedcomponents.add(compid)
            return
        try:
            docker_uri, data_formats, spec = convert.gen_dcae_artifacts_for_model(config, model_name, model_version)
            shutil.rmtree(modeldir)
        except Exception:
            print('Error analyzing artifacts for {}'.format(compid))
            traceback.print_exc()
            return
        for data_format in data_formats:
            fmtid = (data_format['self']['name'], data_format['self']['version'])
            if fmtid in _loadedformats:
                print('Skipping data format {}: already analyzed'.format(fmtid))
                continue
            try:
                resp = requests.post(oburl('/dataformats'), json={'owner': config.dcaeuser, 'spec': data_format}, auth=obauth)
                if resp.status_code == 409:
                    print('Skipping data format {}: previously loaded'.format(fmtid))
                    _loadedformats.add(fmtid)
                    continue
                resp.raise_for_status()
                requests.patch(resp.json()['dataFormatUrl'], json={'owner': config.dcaeuser, 'status': 'published'}, auth=obauth).raise_for_status()
                print('Loaded data format {}'.format(fmtid))
                _loadedformats.add(fmtid)
            except Exception:
                print('Error loading data format {}'.format(fmtid))
                traceback.print_exc()
                raise
        try:
            resp = requests.post(oburl('/components'), json={'owner': config.dcaeuser, 'spec': spec}, auth=obauth)
            if resp.status_code == 409:
                print('Skipping component {}: previously loaded'.format(compid))
                _loadedcomponents.add(compid)
                return
            resp.raise_for_status()
            requests.patch(resp.json()['componentUrl'], json={'owner': config.dcaeuser, 'status': 'published'}, auth=obauth).raise_for_status()
            print('Loaded component {}'.format(compid))
            _loadedcomponents.add(compid)
        except Exception:
            print('Error loading component {}'.format(compid))
            traceback.print_exc()
            raise
    return callback


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    protocol_version = "HTTP/1.1"


class Apihandler(BaseHTTPRequestHandler):
    def doqp(self):
        self.qparams = {}
        if not self.path or '?' not in self.path:
            return
        self.path, qp = self.path.split('?', 1)
        for x in qp.split('&'):
            k, v = x.split('=', 1)
            self.qparams[unquote_plus(k)] = unquote_plus(v)

    def replyjson(self, body, ctype='application/json'):
        self.replyraw(json.dumps(body).encode('utf-8'), ctype)

    def replyraw(self, data, ctype):
        self.send_response(200)
        self.send_header('Content-Type', ctype)
        self.send_header('Content-Length', len(data))
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        self.doqp()
        if self.path == '/' or self.path == '/index.html' or self.path == '/acumos-adapter/' or self.path == '/acumos-adapter/index.html':
            self.replyraw(self.server.index, 'text/html')
            return
        if 'acumos' not in self.qparams:
            self.send_error(400)
            return
        aa = _AcumosAccess(self.server.config, self.qparams['acumos'])
        if self.path == '/acumos-adapter/listCatalogs.js':
            self.replyjson([{'name': x['name'], 'id': x['catalogId']} for x in aa.jsonget('/catalogs')])
            return
        if self.path == '/acumos-adapter/listSolutions.js':
            if 'catalogId' not in self.qparams:
                self.send_error(400)
                return
            self.replyjson([{'name': x['name'], 'id': x['solutionId']} for x in aa.jsonget('/solutions?catalogId={}', self.qparams['catalogId'])])
            return
        if self.path == '/acumos-adapter/listRevisions.js':
            if 'solutionId' not in self.qparams:
                self.send_error(400)
                return
            self.replyjson([{'name': x['version'], 'id': x['revisionId']} for x in aa.jsonget('/solutions/{}/revisions', self.qparams['solutionId'])])
            return
        self.send_error(404)

    def do_POST(self):
        self.doqp()
        if self.path == '/acumos-adapter/onboard.js':
            if 'acumos' not in self.qparams:
                self.send_error(400)
                return
            aa = _AcumosAccess(self.server.config, self.qparams['acumos'])
            callback = self.server.callback
            if 'catalogId' not in self.qparams:
                for catalog in aa.jsonget('/catalogs'):
                    for solution in aa.jsonget('/solutions?catalogId={}', catalog['catalogId']):
                        for revision in aa.jsonget('/solutions/{}/revisions', solution['solutionId']):
                            onboard(aa, callback, solution, revision['revisionId'])
            elif 'solutionId' not in self.qparams:
                for solution in aa.jsonget('/solutions?catalogId={}', self.qparams['catalogId']):
                    for revision in aa.jsonget('/solutions/{}/revisions', solution['solutionId']):
                        onboard(aa, callback, solution, revision['revisionId'])
            elif 'revisionId' not in self.qparams:
                solution = aa.jsonget('/solutions/{}', self.qparams['solutionId'])
                for revision in aa.jsonget('/solutions/{}/revisions', solution['solutionId']):
                    onboard(aa, callback, solution, revision['revisionId'])
            else:
                solution = aa.jsonget('/solutions/{}', self.qparams['solutionId'])
                onboard(aa, callback, solution, self.qparams['revisionId'])
            self.replyraw('OK'.encode('utf-8'), 'text/plain')
            return
        self.send_error(400)


def serve(config):
    server = ThreadedHTTPServer(('', config.port), Apihandler)
    server.config = config
    server.callback = _makecallback(config)
    server.index = resource_string(__name__, 'index.html')
    server.serve_forever()
