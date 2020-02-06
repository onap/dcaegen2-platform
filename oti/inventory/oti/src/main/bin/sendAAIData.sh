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
STARTTIME=$(now_ms)
process="${THISPROG%.*}"
set -x
currentuser=$(id -un)
process=$$

dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "
StartRecordEvent "AUDIT"

export REMOTESERVER=$(hostname -f)
CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/dti-package-content-final.jar
echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI

if [ $DCAE_ENV == "D2" ]
then
	dcaelogdebug "%s %s" "INFO" "Sending feed to VETL"
	StartRecordEvent "METRIC"
	export java_program="com.att.vcc.inventorycollector.DmaapDRPub"
	java com.att.vcc.inventorycollector.DmaapDRPub "full"
	if [ $? == 0 ]; then
		STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="DMaap DR publish" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s completed successfully" "$java_program"
		dcaelogdebug "%s %s" "INFO" "Finished Sending feed to VETL"
	else
		STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="DMaap DR publish" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The child script %s failed" "$java_program"
		ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
		dcaelogerror "%s %s" "ERROR" "Failed Sending feed to VETL"
	fi
fi
dcaelogaudit "script %s completed" "$THISPROG"
