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

# Uncomment for D1
#export DTI=/home/netman/common/dti
#. $DTI/bin/PROFILE

export SUBCOMP=dti

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

export REQUESTID=$(uuid)

export WRITEDEBUGLOG=1
THISPROG=$(basename $0)
export REMOTESERVER=$(hostname -f)
STARTTIME=$(now_ms)
datevar=`date +\%Y\%m\%d\%H\%M\%S`
#process="${THISPROG%.*}"
set -x
currentuser=$(id -un)
process=$$

current_user_process=$(ps -fu "$currentuser")

#cup = current_user_process
cup_wihtout_grep=$(echo "$current_user_process" | grep -v grep)
cup_without_sudo=$(echo "$cup_wihtout_grep" | grep -v 'sudo')
cup_without_vi=$(echo "$cup_without_sudo" | grep -v 'vi ')

#cup_same_process=$(echo "$cup_without_vi" | grep "$temp_process")

cup_without_current_pid=$(echo "$cup_without_vi" | grep -v $process)
process_count=$(echo "$cup_without_current_pid" | grep -c "$THISPROG")

dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "
StartRecordEvent "AUDIT"

#process_count=$(ps -fu $currentuser | grep -v 'grep ' | grep -v 'vi ' | grep -v 'sudo' | grep "startAAIFullSync.sh" | grep -v $process |  grep -c "startAAIFullSync.sh")
if [ $process_count -ge 1 ]
then
    echo $0 INFO "There is another instance of the process $prog is running. Quitting startAAIFullSync.sh Process"
	dcaelogdebug "%s %s %s %s" "WARN" "There is another instance of the process" "$THISPROG" "is running. Quitting startAAIFullSync.sh Process"
	dcaelogaudit "script %s failed" "$THISPROG"
    exit 0
fi

process_count=$(ps -fu $currentuser | grep -v 'grep ' | grep -v 'vi ' | grep "startAAIRealtimeUpdates.sh" | grep -c "startAAIRealtimeUpdates.sh")
if [ $process_count -ge 1 ]
then
	export AAIREALTIME_SCRIPT="startAAIRealtimeUpdates.sh"
    dcaelogdebug "%s %s %s %s %s" "INFO" "$AAIREALTIME_SCRIPT" "is running. Stopping" "$AAIREALTIME_SCRIPT" "Process"
    pid=$(ps -ef | grep "InventoryCollector UPDATES" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
    dcaelogdebug "%s %s %s" "INFO" "Killing Process pid: " "$pid"
    kill -9 $pid
fi
#CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
#CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
CLASSPATH=$CLASSPATH:$VCC_HOME:$VCC_HOME/config:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti.jar:$VCC_HOME/lib/configManager.jar:$VCC_HOME/lib/attMaven/'*'

echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI
dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
StartRecordEvent "METRIC"
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.1,TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector SYNC $datevar
if [ $? == 0 ]; then
	STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Inventory Collector sync" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
	dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
	STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Inventory Collector sync" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
	dcaelogmetrics "The child script %s failed" "$java_program"
	ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
	dcaelogerror "%s %s" "ERROR" "Inventory Collector Sync failed"
fi

#$JAVA_BIN/java -jar $DTI_JAR SYNC $datevar
echo $0 INFO "Completed DTI Java Process"
dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"

# Uncomment for D1
if [ "$DCAE_ENV" == "D1" ]
then
	export DRIVERVECTOPO="driver-vecTopo.sh"
    dcaelogdebug "%s %s %s %s" "INFO" "Calling" "$DRIVERVECTOPO" "to transfer gamma config files to local DCAE"
    StartRecordEvent "METRIC"
    $DTI/bin/$DRIVERVECTOPO > /opt/logs/dcae/dti/run_driver-vecTopo.log 2>&1
    if [ $? == 0 ]; then
		STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="$DRIVERVECTOPO" TARGETSVCNAME="$DRIVERVECTOPO" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s completed successfully" "$DRIVERVECTOPO"
		dcaelogdebug "%s %s" "INFO" "Finished transferring gamma config files to local DCAE"
	else
		STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="$DRIVERVECTOPO" TARGETSVCNAME="$DRIVERVECTOPO" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s failed" "$DRIVERVECTOPO"
		ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
		dcaelogerror "%s %s" "ERROR" "Failed to transfer gamma config files to local DCAE"
	fi
    export DRIVERVECMOBTOPO="driver-vecMobTopo.sh"
    StartRecordEvent "METRIC"
    dcaelogdebug "%s %s %s %s" "INFO" "Calling" "$DRIVERVECMOBTOPO" "to transfer mobility config or task files to local DCAE"
    $DTI/bin/$DRIVERVECMOBTOPO > /opt/logs/dcae/dti/run_driver-vecMobTopo.log 2>&1
        if [ $? == 0 ]; then
		STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="$DRIVERVECMOBTOPO" TARGETSVCNAME="$DRIVERVECMOBTOPO" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s completed successfully" "$DRIVERVECMOBTOPO"
		dcaelogdebug "%s %s" "INFO" "Finished transferring mobility config or task files to local DCAE"
	else
		STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="$DRIVERVECMOBTOPO" TARGETSVCNAME="$DRIVERVECMOBTOPO" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s failed" "$DRIVERVECMOBTOPO"
		ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
		dcaelogerror "%s %s" "ERROR" "Failed to transfer mobility config or task files to local DCAE"
	fi
else
    dcaelogdebug "%s %s" "INFO" "Config files not created."
fi
dcaelogaudit "script %s completed successfully" "$THISPROG"
