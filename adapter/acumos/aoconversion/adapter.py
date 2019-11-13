# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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

"""
Command line for Acumos -> ONAP adapter
"""

import os
import shutil
import sys
import yaml


def adapter():
    """
    Run the adapter
    """
    import warnings
    warnings.simplefilter('ignore')
    with open(sys.argv[1], 'r') as f:
        ycfg = yaml.safe_load(f.read())
    from aoconversion import scanner
    config = scanner.Config(**ycfg)
    try:
        shutil.rmtree(config.tmpdir)
    except Exception:
        pass
    os.makedirs(config.tmpdir)
    if config.port:
        print('Starting web server')
        scanner.serve(config)
    else:
        print('Starting scan')
        scanner.scan(config)
        print('Scan complete.    Sleeping')
