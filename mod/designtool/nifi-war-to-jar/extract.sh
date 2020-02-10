#!/bin/bash
# ==============================================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ==============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=======================================================

#
# The nifi web api is available as a war file but not as a jar file.
# We need to compile patches against the classes in it, so we need
# a jar file.  This shell extracts the class files from the war and copies
# them to target/classes so maven can package them into a jar file.
# The jar is then used as a "provided" dependency for compiling the
# design tool patches
#

set -euf -o pipefail
echo Extracting classes from "$1"
cd target
rm -rf WEB-INF classes
jar xf $1 WEB-INF/classes/org/apache/nifi
mv WEB-INF/classes .
rmdir WEB-INF
