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

#################################################################################
# event_simulator.sh
#
# This is the simulate the realtime event from AAI or NARAD
# by placing a aaievent or naradevent json file under the temp_config folder
# This aaievent or naradevent file will be similar to the realtime event
#
# It will call the java process to generate the dcae_event and send to DTI-Handler
#
################################################################################

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=dti
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

USAGE="USAGE: simulate_event.sh AAIEVENT/NARADEVENT <Yes/No> -- Here 1st parameter is what event, the 2nd parameter is whether to send dcae_event to DTI Handler, default is Yes. No is just update PGDB only. "
#processAAIEvent
#	topEntityType: CLOUD_REGION_ENTITY, CUSTOMER_ENTITY, CUSTOMER_ENTITY, PNF_ENTITY, GENERIC_VNF_ENTITY, VNFC_ENTITY, VPLS_PE_ENTITY
#	entityType: COMPLEX_ENTITY, FORWARDING_PATH_ENTITY, MODEL_VER_ENTITY, SERVICE_CAPABILITY_ENTITY, L3_NETWORK_ENTITY, LAG_LINK_ENTITY, LINE_OF_BUSINESS_ENTITY, 
#				LOGICAL_LINK_ENTITY, NETWORK_PROFILE_ENTITY, OPERATIONAL_ENVIRONMENT_ENTITY, OWNING_ENTITY_ENTITY, PHYSICAL_LINK_ENTITY, PLATFORM_ENTITY,
#				PROJECT_ENTITY, SERVICE_ENTITY, VCE_ENTITY, VIRTUAL_DATA_CENTER_ENTITY, VNF_IMAGE_ENTITY, ZONE_ENTITY
#	do nothing: FORWARDER_ENTITY, MODEL_ENTITY, SUBNET_ENTITY, PORT_GROUP_ENTITY

eventTypeInput=$1
eventType=${eventTypeInput^^}
sendEvent=$2
if [ -z "$eventType" ]
then
	echo $USAGE
	exit 1
elif [ "$eventType" == "AAIEVENT" ]
then
	echo "simulating AAIEVENT" 
	eventFile="/opt/app/vcc/temp_config/aaievent.json"
	if [ -s $eventFile ]
	then
		echo "the aaievent.json file exists"
	else
		echo "the aaievent.json file does not exist under /opt/app/vcc/temp_config folder."
		exit 1
	fi
elif [ "$eventType" == "NARADEVENT" ]
then
	echo "simulating NARADEVENT" 
	eventFile="/opt/app/vcc/temp_config/naradevent.json"
	if [ -s $eventFile ]
	then
		echo "the naradevent.json file exists"
	else
		echo "the naradevent.json file does not exist under /opt/app/vcc/temp_config folder."
		exit 1
	fi	
fi
if [ -z "$sendEvent" ]
then
	sendEvent="Yes"
fi

StartRecordEvent "METRIC"
dcaelogdebug "Start $THISPROG at the time: `date`"
dcaelogaudit "script %s started" "$THISPROG"

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

CLASSPATH=$CLASSPATH:$VCC_HOME:$VCC_HOME/config:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti.jar:$VCC_HOME/lib/configManager.jar:$VCC_HOME/lib/attMaven/'*'

if [ $sendevent == "Yes" ]
then
	dcaelogdebug "To simulate the $eventType, pass in as $sendevent, send event to DTI-Handler"
    $JAVA_HOME/bin/java -cp $CLASSPATH -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector $eventType $sendEvent 
else
    # update the DB only
    dcaelogdebug "To simulate the $eventType, pass in as $sendevent, update PGDB only, not send event to DTI-Handler"
    $JAVA_HOME/bin/java -cp $CLASSPATH -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector $eventType 
fi
if [ $? == 0 ]; then
    STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Simulate $eventType" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The script %s for simulate $eventType completed successfully" "$java_program"
else
    STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="Simulate $eventType" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The script %s failed for simulate $eventType" "$java_program"
    ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="simulate event script failed" \
    dcaelogerror "%s %s" "ERROR" "Inventory Collector for simulate $eventType failed"
    exit 1
fi
dcaelogdebug "%s %s" "INFO" "Completed Simulate Event Process"
dcaelogaudit "script %s completed" "$THISPROG"
exit 0