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

# Remove more than 5 days old files from archive dir
find $VCC_HOME/archive/* -type f -mtime +4 -delete

# Remove more than 5 days old files from log dir
#find $VCC_HOME/logs/* -type f -mtime +4 -delete
find $VCC_HOME/logs/DCAE/*/VCC_* -type f -mtime +4 -delete

# Remove more than 5 days old files from data/input/vendor dir
find $VCC_HOME/data/input/vendor/* -type f -mtime +4 -delete

# Remove more than 5 days old files from data/output/vendor dir
find $VCC_HOME/data/output/vendor/* -type f -mtime +4 -delete
