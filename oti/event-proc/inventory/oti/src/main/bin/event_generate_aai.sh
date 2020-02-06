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

#############################################################################
# event_generate_aai.sh
#
# this script is used to send a dcae_event for various AAI entity-type values 
# entity-type values can be generic-vnf, vce, vserver, vnfc, pnf & pserver. 
# So we need to run the script SIX times.
# 
# updated for release 20.04
#
############################################################################

#!/bin/bash

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


USAGE="USAGE: event_generate_aai.sh <entity-type> -- Here entity-type is one of generic-vnf, vce, vserver, vnfc, pnf & pserver"

entityType=$1
if [ -z "$entityType" ]
then
	echo $USAGE
	exit 1
fi
StartRecordEvent "METRIC"

dcaelogdebug "%s %s %s %s" "INFO" "START" "`date` $THISPROG" "has started to generate the AAI Event for $entityType ... "
export CLASSPATH=$CLASSPATH:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti.jar:$VCC_HOME/lib/configManager.jar:$VCC_HOME/lib/attMaven/'*'

$JAVA_HOME/bin/java -cp $CLASSPATH -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector GENERATE_AAI_EVENT_FROM_DB $entityType
if [ $? == 0 ]; then
    STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="AAI Event Generate" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The script %s for generate event for $entityType completed successfully" "$java_program"
else
    STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="AAI Event Generate" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
    dcaelogmetrics "The script %s failed for aai event generate of $entityType" "$java_program"
    ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC=" aai event generate script failed" \
    dcaelogerror "%s %s" "ERROR" "Inventory Collector for AAI Event Generate failed"
    exit 1
fi

dcaelogdebug "%s %s" "INFO" "`date` Completed AAI Event Generate Process"
dcaelogaudit "script %s completed" "$THISPROG"

echo "`date` FINISHED event_generate_aai.sh for $entityType"
