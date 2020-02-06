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

#######################################
#
# Replay Event script is used  to send a dcae_event taking  DCAE Target Type 
# as argument and replay the event from DTI -- DTI2Handler -- Collector   
#
# release 19.11 
#
########################################

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=dti
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

set -o allexport
source $VCC_HOME/bin/dti.cfg
set +o allexport

USAGE="USAGE: replayEvent.sh"

dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started to send DTI event ... "

#datevar=`date +\%Y\%m\%d\%H\%M\%S`
datevar=`date`
process="${THISPROG%.*}"
prog=`basename $0`
#set -x
currentuser=$(id -un)
temp_process=$prog
process=$$

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
#echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
#echo $PATH
cd $DTI
export REMOTESERVER=`hostname -f`
dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
StartRecordEvent "METRIC"

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
#echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
#echo $PATH
cd $DTI
export REMOTESERVER=`hostname -f`
export java_program="com.att.vcc.inventorycollector.InventoryCollector"
StartRecordEvent "METRIC"

declare -a my_array
dcae_target_type=$1
dcae_target_type=${dcae_target_type^^}

echo "`date` Checking the event for $dace_target_type if exists in dcae_event PGDB  table ..."
dcaelogdebug "%s %s" "INFO" "`date` Checking the event $dcae_target_type  if exists in PGDB event table ..."

#Step 1) check the DB to verify whether any record is already present for the cluster.


sql_rec="select dcae_target_name  from dti.dcae_event  where upper(dcae_target_type)='$dcae_target_type' and ( upper(dcae_service_action) = 'DEPLOY' or upper(dcae_service_action) = 'UPDATE' or upper(dcae_service_action) ='ADD' );"

my_array=($(echo $sql_rec | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t ))
len=${#my_array[@]}
echo "There are  $len nodes matching the given target type: $dcae_target_type"
if [ $len -gt "0" ]
then
        for i  in "${my_array[@]}"
        do
                echo $i
              # send update sql
              dcaelogdebug "%s %s" "INFO" "`date` $dcae_target_name already exists in PGDB event table, send update "
            update_sql="update dti.dcae_event SET  dcae_event_sent_flag = 'N', dcae_event_status = 'NEW', updated_on = to_char(now(), 'yyyymmddhhmmss') where dcae_target_name='$i' and upper(dcae_target_type)='$dcae_target_type';"
            echo  $update_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

        done
else
        dcaelogdebug "%s %s" "ERROR" "`date` No records of $dcae_target_name found in PGDB event table."
fi

dcaelogdebug "%s %s" "INFO" "`date` Calling DTI Java Process to send the dace_event to DTI2Handler...."

echo "`date` Running static_dcae_events.sh to process the switchover event ..."
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector LOAD_STATIC_EVENTS
if [ $? == 0 ]; then
    STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Inventory Collector static load" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
    STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Inventory Collector statuc load" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The child script %s failed" "$java_program"
    ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
    dcaelogerror "%s %s" "ERROR" "Inventory Collector static dcae event script failed"
fi
dcaelogdebug "%s %s" "INFO" "`date` Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"

echo "`date` FINISHED static_dcae_events.sh to process the dcae_target_type event."
