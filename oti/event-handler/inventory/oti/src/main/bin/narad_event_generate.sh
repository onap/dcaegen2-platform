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

# 
# This script to to generate events using the pnf records in the PGDB
# USAGE="USAGE: narad_event_generate.sh
# the parameters are passed in directly
# 

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

export SUBCOMP=dti
export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)
dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "
dcaelogaudit "script %s started" "$THISPROG"

$JAVA_HOME/bin/java -classpath /opt/app/vcc/config/:/opt/app/vcc/lib/:/opt/app/vcc/classes/:/opt/app/vcc/lib/dti-package-content-final.jar -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector GENERATE_NARAD_EVENT_FROM_DB pnf

dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"
exit 0