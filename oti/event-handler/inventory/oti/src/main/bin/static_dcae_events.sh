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

release=''
if [ "$1" != "" ]; then
    release=$1
else
    echo "you chose to skip the release info..."
fi

environment=''
if [ "$2" != "" ]; then
    environment=$2
else 
    echo "you chose to skip the environment info..."
fi

export SUBCOMP=dti

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func
        
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)
dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "

datevar=`date +\%Y\%m\%d\%H\%M\%S`
process="${THISPROG%.*}"
prog=`basename $0`
set -x
currentuser=$(id -un)
temp_process=$prog
process=$$

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI
export REMOTESERVER=`hostname -f`
dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
StartRecordEvent "METRIC"

echo "inserting DTI events to Database..."
cd $DTI/config
if [ "$release" != "" ]; then
    if [ $environment == "st" ]
    then
        filename=st_nonprod_${release}_static_dti_events.sql
        echo "Loading st_nonprod static dcae events details for release: $release"
    elif [ $environment == "prod" ]
    then
        filename=prod_${release}_static_dti_events.sql
        echo "Loading prod static dcae events details for release: $release"
    else 
        filename=nonprod_${release}_static_dti_events.sql
        echo "Loading nonprod static dcae events details for release: $release"
    fi
    cat sql/table/$filename | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
fi

echo "Running static_dcae_events.sh ..."
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector LOAD_STATIC_EVENTS
if [ $? == 0 ]; then
    STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Inventory Collector static load" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
    STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Inventory Collector statuc load" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The child script %s failed" "$java_program"
    ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
    dcaelogerror "%s %s" "ERROR" "Inventory Collector static dcae event script failed"
fi
dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"
echo "FINISHED static_dcae_events.sh"
