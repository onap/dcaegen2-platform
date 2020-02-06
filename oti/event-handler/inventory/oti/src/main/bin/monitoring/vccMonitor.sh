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
# vccMonitor script is used  to send a dcae_event with vcc-mon as DCAE Target Type 
# to verify the flow from DTI -- DTI2Handler -- Collector (helloword-vcc-mon)  
#
# It is triggered hourly in the cron and contains
# testing TASKID: VCCMON9999
# evnet timestamp
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

USAGE="USAGE: vccMonitor.sh"

dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started to send verification event ... "

#datevar=`date +\%Y\%m\%d\%H\%M\%S`
datevar=`date`
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

dcae_target_name="vcc-mon"
dcae_target_type="vcc-mon"
dcae_service_action="notify"
dcae_service_location="LSLEILAA"
dcae_target_collection="false"
dcae_target_collection_ip="na"
#dcae_target_service_description=$datevar

# add certificate and password for keystore verification
/opt/app/vcc/bin/monitoring/keystoreMonitoring.sh


# add NPM interface verification call


# the follwing will do PGDB verification, need to write out the result for monitoring


echo "`date` Checking the event for $dcae_target_name if exists in PGDB event table ..."
dcaelogdebug "%s %s" "INFO" "`date` Checking the event $dcae_target_name if exists in PGDB event table ..."

#Step 1) check the DB to verify whether any record is already present for the cluster.
check_sql="select count(*) from dti.dcae_event where dcae_target_name='$dcae_target_name' and dcae_target_type='$dcae_target_type';"
retun_cnt=`echo  $check_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t`

if [ $retun_cnt -eq "1" ]
then
	#Step 2) if record exist, use update statement else use insert statement.
	# the vcc-mon already exists in the table
	# send update sql
	echo "`date` record for this  $dcae_target_name already exists, send update ..."
	dcaelogdebug "%s %s" "INFO" "`date` $dcae_target_name already exists in PGDB event table, send update "
	update_sql="update dti.dcae_event SET dcae_service_location = '$dcae_service_location', dcae_target_service_description = '$datevar', dcae_event_sent_flag = 'N', dcae_event_status = 'NEW', updated_on = to_char(now(), 'yyyymmddhhmmss') where dcae_target_name='$dcae_target_name' and dcae_target_type='$dcae_target_type';"
	echo  $update_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

elif [ $retun_cnt -eq "0" ]
then 
	#Step 3) if record not exist, use insert statement.

	echo "`date` inserting switch over events to Database..."
	dcaelogdebug "%s %s" "INFO" "`date` insert new record $dcae_target_name into PGDB event table"
	insert_sql="INSERT INTO dti.dcae_event (dcae_target_name, dcae_target_type, dcae_service_location, dcae_service_action, dcae_target_prov_status, \
dcae_service_type, dcae_target_in_maint, dcae_target_is_closed_loop_disabled, dcae_service_instance_model_invariant_id, \
dcae_service_instance_model_version_id, dcae_generic_vnf_model_invariant_id, dcae_generic_vnf_model_version_id, dcae_target_service_description, \
dcae_target_collection, dcae_target_collection_ip, dcae_snmp_community_string, dcae_snmp_version, \
dcae_target_cloud_region_id, dcae_target_cloud_region_version, event, aai_additional_info, dcae_event_sent_flag, dcae_event_status, dcae_event_retry_interval, dcae_event_retry_number, updated_on) \
 VALUES ('$dcae_target_name', '$dcae_target_type', '$dcae_service_location', 'deploy', 'PROV', '', 'false', 'false', '','', '', '', '$datevar', 'true', '$dcae_target_collection_ip', '', '','','','{}', '{}', \
 'N', 'NEW', '60', '0', to_char(now(), 'yyyymmddhhmmss'));"
 
 	echo  $insert_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
else
	dcaelogdebug "%s %s" "ERROR" "`date` Multiple records of $dcae_target_name found in PGDB event table, please clean up the records." 
 	exit 1
fi

dcaelogdebug "%s %s" "INFO" "`date` Calling DTI Java Process to send the dace_event to DTI2Handler...."

echo "`date` Running static_dcae_events.sh to process the switchover event ..."
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
dcaelogdebug "%s %s" "INFO" "`date` Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"

echo "`date` FINISHED static_dcae_events.sh to process the vcc-mon event."
