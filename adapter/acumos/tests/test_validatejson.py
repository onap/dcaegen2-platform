# ============LICENSE_START====================================================
# org.onap.dcae
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

"""
Test the compatibility of Adapter while reading metadata from both previous (upto Clio) Acumos releases
and the new Demeter release.
"""

import aoconversion
import testing_helpers


def test_validate_format_Clio():
    """
    Given the metadata as per Acumos Clio's tree structure, check if the validate_json method
    reads the correct input and output name
    """
    model_repo_path = testing_helpers.get_fixture_path('models')
    model_name = 'example-model'
    meta = aoconversion.utils.get_metadata(model_repo_path, model_name)

    for method in meta["methods"]:
        assert (aoconversion.utils.validate_format(meta, method, "input")) == "NumbersIn"
        assert (aoconversion.utils.validate_format(meta, method, "output")) == "NumberOut"


def test_validate_format_Demeter():
    """
        Given the metadata as per Acumos Clio's tree structure, check if the validate_json method
        can also read the correct input and output name without fail
        """
    model_repo_path = testing_helpers.get_fixture_path('models')
    model_name = 'example-model-demeter'
    meta = aoconversion.utils.get_metadata(model_repo_path, model_name)

    for method in meta["methods"]:
        assert (aoconversion.utils.validate_format(meta, method, "input")) == "NumbersIn"
        assert (aoconversion.utils.validate_format(meta, method, "output")) == "NumbersOut"
