#!/bin/bash
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

if [ -d "/www/data/nifi-jars" ]; then
    nginx -g "daemon off;"
else
    echo "\"/www/data/nifi-jars\" directory missing"
    echo "You must perform a volume mount to this directory in the container"
    exit 1
fi
