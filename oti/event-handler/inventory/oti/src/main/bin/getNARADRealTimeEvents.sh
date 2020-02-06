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
export SUBCOMP=dti
if [ -z $VCC_HOME ]
then
        source /opt/app/vcc/bin/dti.cfg
        export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
        source $LOGFRAMEWORK/logging.func
else
        if [ ! -f ${VCC_HOME}/bin/dti.cfg ]
        then
                grep export_ ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/export_//" > $VCC_HOME/bin/dti.cfg
                echo "KEYSTORE_PASSWORD=`cat /opt/app/dcae-certificate/.password`" >> $VCC_HOME/bin/dti.cfg
                echo "DTI=${VCC_HOME}" >> $VCC_HOME/bin/dti.cfg
                echo "DTI_CONFIG=${VCC_HOME}/config" >> $VCC_HOME/bin/dti.cfg
                unset http_proxy
                dti2_handler_service_discovery=$(curl http://${CONSUL_HOST}:8500/v1/catalog/service/dti2_handler?pretty | jq '.[0] | .NodeMeta.fqdn')
                dti2_handler_api=$(echo $dti2_handler_service_discovery | tr -d '"')
                echo "ORCH_POST_URL=https://"${dti2_handler_api}":8443/events" >> $VCC_HOME/bin/dti.cfg
                source ${VCC_HOME}/bin/dti.cfg
                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARAD.properties
        fi
        source ${VCC_HOME}/bin/dti.cfg
        export LOGFRAMEWORK=${VCC_HOME}/infrastructure/ecomp/logger/shell
        export LOGBASEDIR=${VCC_HOME}/logs
        source ${VCC_HOME}/infrastructure/ecomp/logger/shell/logging.func
fi
set +o allexport

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
process_count=$(echo $cup_without_current_pid | grep -c "$temp_process")
java_process=" NARAD"
java_process_count=$(echo "$cup_without_vi" | grep -c "$java_process")
if [ $process_count -ge 1 ] || [ $java_process_count -ge 1 ]
then
    dcaelogdebug "%s %s %s %s" "WARN" "There is another instance of the process" "$process" "is running. Quitting getNARADRealTimeEvents.sh Process"
    echo $0 INFO "There is another instance of the process $prog is running. Quitting getNARADRealTimeEvents.sh Process"
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
$JAVA_HOME/bin/java -Dlogback.configurationFile=$DTI/config/logger/logback_narad.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector NARAD
if [ $? == 0 ]; then
        STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="NARAD" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
        dcaelogmetrics "The child script %s completed successfully" "$java_program"
else
        STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="NARAD" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
        dcaelogmetrics "The child script %s failed" "$java_program"
        ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
        dcaelogerror "%s %s" "ERROR" "Inventory Collector updates failed"
fi
dcaelogdebug "%s %s" "INFO" "Completed DTI Java Process"
dcaelogaudit "script %s completed" "$THISPROG"
