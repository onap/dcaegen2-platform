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
Module that converts acumos models to dcae artifacts
"""
from aoconversion import docker_gen, dataformat_gen, spec_gen


def gen_dcae_artifacts_for_model(config, model_name, model_version="latest"):
    """
    Generate all dcae artifacts given an acumos model
    """
    model_repo_path = config.tmpdir
    docker_uri = docker_gen.build_and_push_docker(config, model_name, model_version)
    data_formats = dataformat_gen.generate_dcae_data_formats(model_repo_path, model_name)
    spec = spec_gen.generate_spec(model_repo_path, model_name, data_formats, model_version)
    return docker_uri, data_formats, spec
