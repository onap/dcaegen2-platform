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

export SUBCOMP=dti
export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)
dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "

datevar=`date +\%Y\%m\%d\%H\%M\%S`
process="${THISPROG%.*}"
prog=$(basename $0)
set -x
currentuser=$(id -un)
temp_process=$prog
process=$$

process_count=$(ps -fu $currentuser | grep -v grep | grep -v 'vi ' | grep -v 'sudo' | grep "$temp_process" | grep -v $process |  grep -c $temp_process)
if [ $process_count -ge 1 ]
then
    dcaelogerror "%s %s %s %s" "WARN" "There is another instance of the process" "$process" "is running. Quitting getSeedRediscovery.sh Process"
    echo $0 INFO "There is another instance of the process $prog is running. Quitting getSeedRediscovery.sh Process"
    exit 0
fi

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI
export REMOTESERVER=$(hostname -f)
dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
StartRecordEvent "METRIC"
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logger/logback_sm.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector SEEDREDISCOVERY
if [ $? == 0 ]; then
        STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Seed Rediscovery" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
        dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
        STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Seed Rediscovery" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
        dcaelogmetrics "The child script %s failed" "$java_program"
        ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
        dcaelogerror "%s %s" "ERROR" "Inventory Collector updates failed"
fi
dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"
