# ============LICENSE_START=======================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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
import os
from setuptools import setup, find_packages

# extract __version__ from version file. importing distributor will lead to install failures
setup_dir = os.path.dirname(__file__)
with open(os.path.join(setup_dir, 'distributor', 'version.py')) as file:
    globals_dict = dict()
    exec(file.read(), globals_dict)
    __version__ = globals_dict['__version__']

setup(
        name = "distributor-api",
        version = __version__,
        packages = find_packages(),
        author = "Michael Hwang",
        description = ("API that manages distribution targets"),
        entry_points="""
        [console_scripts]
        start-distributor-api=distributor.http:start_http_server
        """,
        install_requires=[
            "Werkzeug==0.16.1",
            "flask-restplus"
            , "Flask-Cors"
            , "requests"
	    , "MarkupSafe==2.0.1"
            ],
        zip_safe = False
        )
