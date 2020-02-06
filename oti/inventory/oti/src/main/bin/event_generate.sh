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


set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

# Usage:event_generate.sh <entity-type>
# entity-type values can be generic-vnf, vce, vserver, vnfc, pnf & pserver. So we need to run the script SIX times.

$JAVA_HOME/bin/java -classpath /opt/app/vcc/config/:/opt/app/vcc/lib/:/opt/app/vcc/classes/:/opt/app/vcc/lib/dti-package-content-final.jar -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector GENERATE_AAI_EVENT_FROM_DB $1
