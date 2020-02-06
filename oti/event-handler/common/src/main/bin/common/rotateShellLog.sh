#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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

# mv the files > than 10M to 
find /opt/app/vcc/logs/DCAE/MY_SHELL_FOLDER_1/*.log -type f -size +10000k -exec sh -c 'y="`date +%Y%m%d%H%M%S`" x="{}"; mv "$x" "${x}_${y}"; touch "$x"' \;

find /opt/app/vcc/logs/DCAE/MY_SHELL_FOLDER_2/*.log -type f -size +10000k -exec sh -c 'y="`date +%Y%m%d%H%M%S`" x="{}"; mv "$x" "${x}_${y}"; touch "$x"' \;
