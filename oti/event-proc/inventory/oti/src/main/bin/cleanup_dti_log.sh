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

# Remove more than 7 days old files from log dir
find $VCC_HOME/logs/DCAE/dti/ProcessVNodeList* -type f -mtime +7 -delete

find $VCC_HOME/logs/DCAE/dti/startAAIRealtimeUpdatesForDB* -type f -mtime +7 -delete

# daily cron log
find $VCC_HOME/logs/DCAE/dti/sendFeedsVETL* -type f -mtime +30 -delete

# weekly cron log
find $VCC_HOME/logs/DCAE/dti/startAAIFullSync* -type f -mtime +60 -delete

#Remove more than 4 days old files from archive dir
find $VCC_HOME/feeds/outgoing/dmaap/archive/vNodelist* -type f -mtime +4 -delete
find $VCC_HOME/feeds/outgoing/dmaap/archive/NetworkElementAttributes* -type f -mtime +4 -delete
find $VCC_HOME/feeds/outgoing/tmp/NetworkElementAttributes* -type f -mtime +4 -delete
find $VCC_HOME/feeds/outgoing/tmp/vNodelist* -type f -mtime +4 -delete


#Remove more than 4 days old files from  VEC dir
find $VCC_HOME/feeds/outgoing/dmaap/VEC/vNodelist* -type f -mtime +4 -delete