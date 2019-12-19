# ================================================================================
# Copyright (c) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

"""DCAE-Controller dti_handler"""

from setuptools import setup

setup(
    name='otihandler',
    description='DCAE-Controller OTI Handler',
    version="1.0.0",
    author=[''],
    packages=['otihandler'],
    zip_safe=False,
    install_requires=[
        "CherryPy>=18.0.0",
        "docker==4.1.0",
        "enum34>=1.1.6",
        "kubernetes==4.0.0",
        "requests>=2.18.4,<3.0.0",
        "SQLAlchemy==1.3.6",
        "psycopg2-binary==2.8.4",
        "pyOpenSSL==19.1.0",
        "pycrypto==2.6.1",
        "uuid==1.30"
    ],
    keywords='oti dcae controller',
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Programming Language :: Python :: 3.6'
    ]
)
