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

checkInstallNaradEvent()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
	then
		echo "Install to narad event is completed already."
	else
		echo "Install to narad event is not started"
	        /opt/app/vcc/bin/install_narad_event.sh

                source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARAD.properties

                echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
}

checkInstallNaradDB()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
	then
		echo "Install to narad database is completed already."
	else
		echo "Install to narad database is not started"
                /opt/app/vcc/bin/install_narad.sh

                source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARADDB.properties

                echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
}

checkInstallAAIEvent()
{
       echo "checkInstallAAIEvent()"
       INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

        if [ $INSTALL_DONE ]
        then
                echo "Install to aai event is completed already."
        else
                echo "Install to aai event is not started"
                /opt/app/vcc/bin/install_aai_event.sh

                source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumer.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumer.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumer.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumer.properties
                sed -i "/^UEBURL/c\UEBURL=${host}" $VCC_HOME/config/dti.properties

                echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
        fi

}

checkInstallAAIDB()
{
        echo "checkInstallAAIDB()"

        INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

        if [ $INSTALL_DONE ]
        then
                echo "Install to narad database is completed already."
        else
                echo "Install to narad database is not started"
                /opt/app/vcc/bin/install_aai.sh

                source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^UEBURL/c\UEBURL=${host}" $VCC_HOME/config/dti.properties

                echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
        fi
}


while
  getopts i:f:u:s: ARG
do
  case ${ARG} in
  i)
    INTERVAL=${OPTARG};
    ;;
  f)
    TASKFILE=${OPTARG};
    ;;
  u)
    LOGUNIT=${OPTARG};
    ;;
  s)
    SEND=${OPTARG};
    ;;
  esac
done

#HOSTNAME=`getMyVecLogicalSystemName`
HOSTNAME=`hostname -s`

DTIPROC_HOME=$VCC_HOME
DTIPROC_CONF_DIR=$VCC_HOME/config/dtiproc

DTIPROC_OUTPUT_DIR=$VCC_HOME/data/output
DTIPROC_DM_BIN_DIR=$VCC_HOME/bin/common

mkdir -p $DTIPROC_OUTPUT_DIR 2>/dev/null

export SUBCOMP=dti

ENV="-DHOSTNAME=$HOSTNAME -DVCC_HOME=$VCC_HOME -DDTIPROC_HOME=$DTIPROC_HOME -DDTIPROC_CONF_DIR=$DTIPROC_CONF_DIR -DDTIPROC_OUTPUT_DIR=$DTIPROC_OUTPUT_DIR -DDTIPROC_DM_BIN_DIR=$DTIPROC_DM_BIN_DIR -Djavax.net.ssl.trustStore=$DTIPROC_CONF_DIR/cacerts.jdk.1.8"

LOG4J_OPTIONS="-Dlogback.configurationFile=${VCC_HOME}/config/logger/logback_vcc.xml -Dsubcomponent=$SUBCOMP -DisThreadContextMapInheritable=true"

grep export_ /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | sed 's/,*\r*$//' | sed -e "s/export_/export /" > $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg

# EOM K8s deployment
grep aaf_username /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/aaf_username/export username/" > $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep aaf_password /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/aaf_password/export password/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

grep topic_url /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/topic_url/export dmaap_topic_url/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

grep topic_url /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | cut -d"/" -f3 | sed -e "s/^/export host=/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

grep client_role /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/client_role/export dmaap_client_role/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep location /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/location/export dmaap_location/" | uniq >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep client_id /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/client_id/export dmaap_client_id/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

grep \"username\" /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/username/export dmaap_username/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep \"password\" /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/password/export dmaap_password/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

grep log_url /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/log_url/export dmaap_log_url/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep publisher_id /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/publisher_id/export dmaap_publisher_id/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg
grep publish_url /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/publish_url/export dmaap_publish_url/" >> $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

#comment out DtiMain java process below, it may not be used
#CLASSPATH=$CLASSPATH:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti-package-content-final.jar:$VCC_HOME/lib/VNodeList-0.0.1-final.jar:$VCC_HOME/lib/configManager.jar:$VCC_HOME/lib/common.jar:$VCC_HOME/lib/attMaven/*
#java -cp $CLASSPATH $ENV $LOG4J_OPTIONS com.att.vcc.inventorycollector.DtiMain


EOM_ENV=`env | grep -i HOSTNAME`
#EOM_ENV=comattdcaedtidb-proc
if [ "$EOM_ENV" ]
then
       	eventProcAAI=`echo $EOM_ENV | grep -i event-proc-aai`
       	eventProc=`echo $EOM_ENV | grep -i event-proc`
       	dbProcAAI=`echo $EOM_ENV | grep -i db-proc-aai`
       	dbProc=`echo $EOM_ENV | grep -i db-proc`

        if [ "$eventProcAAI" ]
        then
	        echo "In EOM env, current microservice is deployed in (1) "$eventProcAAI

                # Calling transform_policy.py to transform the policy details to Java properties file under ${VCC_HOME}/config/policy/
               # $VCC_HOME/bin/configManager/reconfigure.sh
               # $VCC_HOME/bin/transform_policy.py

                # get AAI events for DTI-EVENT-PROC-AAI
                checkInstallAAIEvent

                pid=$(ps -ef | grep "InventoryCollector UPDATES NO" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
                kill -9 $pid
                echo "Before calling startAAIRealtimeUpdates.sh"
                export datevar=`date +\%Y\%m\%d`; bash /opt/app/vcc/bin/startAAIRealtimeUpdates.sh  >> /opt/app/vcc/logs/DCAE/dti/startAAIRealtimeUpdates.$datevar.log 2>&1 &
                echo "After calling startAAIRealtimeUpdates.sh"

        elif [ "$eventProc" ]
        then
                echo "In EOM env, current microservice is deployed in (2) "$eventProc

                # Calling transform_policy.py to transform the policy details to Java properties file under ${VCC_HOME}/config/policy/
                $VCC_HOME/bin/configManager/reconfigure.sh
                $VCC_HOME/bin/transform_policy.py

                # get NARAD events for DTI-EVENT-PROC
                checkInstallNaradEvent

                echo "Before calling getNARADRealTimeEvents.sh"
                export datevar=`date +\%Y\%m\%d`;bash /opt/app/vcc/bin/getNARADRealTimeEvents.sh >> /opt/app/vcc/logs/DCAE/dti/getNARADRealTimeEvents.$datevar.log 2>&1 &
                echo "After calling getNARADRealTimeEvents.sh"

        elif [ "$dbProcAAI" ]
        then
                echo "In EOM env, current microservice is deployed in (3) "$dbProcAAI

                # get AAI events for DTI-DB-PROC-AAI
                checkInstallAAIDB

                pid=$(ps -ef | grep "InventoryCollector UPDATES YES" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
                kill -9 $pid
                echo "Before calling startAAIRealtimeUpdatesForDB.sh"
                export datevar=`date +\%Y\%m\%d`; bash /opt/app/vcc/bin/startAAIRealtimeUpdatesForDB.sh  >> /opt/app/vcc/logs/DCAE/dti/startAAIRealtimeUpdatesForDB.$datevar.log 2>&1 &
                echo "After calling startAAIRealtimeUpdatesForDB.sh"

        else [ "$dbProc" ]

                echo "In EOM env, current microservice is deployed in (4) "$dbProc

                # get AAI events for DTI-DB-PROC
                checkInstallNaradDB

                echo "Before calling getNARADRealTimeEventsDB.sh"
                export datevar=`date +\%Y\%m\%d`;bash /opt/app/vcc/bin/getNARADRealTimeEventsDB.sh >> /opt/app/vcc/logs/DCAE/dti/getNARADRealTimeEventsDB.$datevar.log 2>&1 &
                echo "After calling getNARADRealTimeEventsDB.sh"
       	fi

else
        echo "The deployment is not on EOM env"
fi
 


