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

current_user_process=$(ps -fu "$currentuser")

#cup = current_user_process
cup_wihtout_grep=$(echo "$current_user_process" | grep -v grep)
cup_without_sudo=$(echo "$cup_wihtout_grep" | grep -v 'sudo')
cup_without_vi=$(echo "$cup_without_sudo" | grep -v 'vi ')

#cup_same_process=$(echo "$cup_without_vi" | grep "$temp_process")

cup_without_current_pid=$(echo "$cup_without_vi" | grep -v $process)
process_count=$(echo "$cup_without_current_pid" | grep -c "$temp_process")
if [ $process_count -ge 1 ] 
then
    dcaelogdebug "%s %s %s %s" "WARN" "There is another instance of the process" "$process" "is running. Quitting startAAIRealtimeUpdates.sh Process"
    echo $0 INFO "There is another instance of the process $prog is running. Quitting startAAIRealtimeUpdates.sh Process"
    exit 0
fi


############## Logic to check active DMaaP MR server, to get aai real time event.

set -o allexport
source /opt/app/vcc/config/dti.properties
source /opt/app/vcc/config/consumer.properties
set +o allexport

success=0
set -f                      # avoid globbing (expansion of *).
host_array=(${UEBURL//,/ })
for i in "${!host_array[@]}"
do
    echo "Trying host $i=>${host_array[i]}"
    FILENAME=/tmp/host$i
    curl --user $username:$password http://${host_array[i]}:3904/events/AAI-EVENT/${group}-$$/$id > $FILENAME
    FILESIZE=$(stat -c%s "$FILENAME")
    #### Check the file size to be 2 because sometime the successful response may have only '[]'
    grep cambria.partition /tmp/host$i
    if [[ $? -eq 0 || $FILESIZE -eq 2 ]]
    then
        echo "updating consumer.properties with host ${host_array[i]}"
        sed -i "/^host=/c\host=${host_array[i]}:3904" /opt/app/vcc/config/consumer.properties
        success=1
        break;
    fi
done

if [ $success -eq 0 ]
then
    echo "ALL THE HOST IN THE CLUSTER IS NOT PROVIDING PROPER RESPONSE."
fi

############## End Logic.

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI
#export REMOTESERVER=echo `hostname -f`
export REMOTESERVER=`env | grep POD_IP | cut -c8-`; 
echo $REMOTESERVER
dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
StartRecordEvent "METRIC"
#java com.att.vcc.inventorycollector.InventoryCollector $1
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector UPDATES NO
if [ $? == 0 ]; then
	STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Inventory Collector Updates" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
	dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
	STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Inventory Collector Updates" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
	dcaelogmetrics "The child script %s failed" "$java_program"
	ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
	dcaelogerror "%s %s" "ERROR" "Inventory Collector updates failed"
fi
dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"
