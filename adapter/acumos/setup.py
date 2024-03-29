# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

from setuptools import setup, find_packages

setup(
    name="aoconversion",
    version="1.0.7",
    packages=find_packages(exclude=["tests.*", "tests"]),
    author="Tommy Carpenter, Andrew Gauld",
    author_email="tommy@research.att.com, agauld@att.com",
    description="Service to create DCAE artifacts from acumos models",
    url="",
    install_requires=["docker>=4.0.0,<5.0.0", "jsonschema", "PyYAML", "requests"],
    package_data={'aoconversion': ['index.html']},
    entry_points={
      "console_scripts": [
        "acumos-adapter=aoconversion.adapter:adapter"
      ]
    }
)
