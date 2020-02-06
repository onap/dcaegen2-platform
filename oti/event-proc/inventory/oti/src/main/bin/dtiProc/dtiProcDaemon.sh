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

##########################################################################
# filename: dtiProcDaemon.sh
#
# This is the main starting point for all the DTI instance in deployment
# It is started by the crontab process
# Each interval, it will check if DTI instance is deployed
# It will generate the dti.cfg and other property files
# Then it will start the realtime process for its specific instance.
##########################################################################


##################################
# functions for installing each mS 
##################################

installSchemaUpdate()
{ 
	#in case of DB update, need to make sure if DB schema is updated
	dti_db_release="2020_04"  

	dcaelogdebug "`date` This is to update the DB schema for $dti_db_release changes, it only needs to run once to update the PGDB tables ..."
	#Step 1) check the DB to verify whether any record is already present for the cluster.
	check_sql="select count(*) from dti.dcae_db_installation where status='complete' and dti_db_version='$dti_db_release';"
	retun_cnt=`echo  $check_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t`

	if [ $retun_cnt -eq "1" ]
	then
		#Step 2) if record exists.
		dcaelogdebug "`date` schema update for $dti_db_release already completed."
	else
		#Step 3) if record does not exist, install it.	
		cd $DTI/config
		if [ -f $DTI/config/sql/table/2004_changes.sql ]
		then
			cat sql/table/2004_changes.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
		fi
		# insert the dcae_db_installation to complete status
		insert_sql="INSERT INTO  dti.dcae_db_installation (status, updated_on, dti_db_version) VALUES ('complete', to_char(now(), 'yyyymmddhhmmss'), '$dti_db_release');"
		echo  $insert_sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
		dcaelogdebug "`date` Updating DB Objects for $dti_db_release completed."
	fi
}

generateDTIConfig()
{ 
	grep export_ ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | sed 's/,*\r*$//' | sed -e "s/export_//" > $VCC_HOME/bin/dti.cfg
	#generate the PGJDBC_URL based on the input of PGSERVERNAME
	pgservername=$(grep -i export_PGSERVERNAME ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | sed 's/,*\r*$//' | cut -d "=" -f 2)
	echo "PGJDBC_URL=jdbc:postgresql://${pgservername}/dti" >> $VCC_HOME/bin/dti.cfg                
	echo "DTI=${VCC_HOME}" >> $VCC_HOME/bin/dti.cfg
	echo "DTI_CONFIG=${VCC_HOME}/config" >> $VCC_HOME/bin/dti.cfg
	echo "DCAE_ENV=D2" >> $VCC_HOME/bin/dti.cfg
	echo "PATH=\$PATH:/opt/app/vcc/bin" >> $VCC_HOME/bin/dti.cfg

	if [ -f /opt/app/aafcertman/.password ]
	then
		echo "KEYSTORE_PASSWORD=`cat /opt/app/aafcertman/.password`" >> $VCC_HOME/bin/dti.cfg
	fi
	unset http_proxy
             
	set -o allexport
	source ${VCC_HOME}/bin/dti.cfg
	set +o allexport
}

# function used to check if KV_Config or other files updated
checkFileUpdate() {
file="$1"
fingerprintfile="$2"

if [ ! -f $file ]
    then
        echo "ERROR: $file does not exist - aborting" >> $LOG
    	exit 1
fi

# create the md5sum from the file to check
filemd5=`md5sum $file | cut -d " " -f1`

# check the md5 and
# show an error when we check an empty file
if [ -z $filemd5 ]
    then
        echo "The md5sum for $file  is empty " >> $LOG
        exit 1
    else
        # pass silent
        :
fi

# do we have already an saved fingerprint of this file?
if [ -f $fingerprintfile ]
then
	#yup - get the saved md5 $fingerprintfile
    savedmd5=`cat $fingerprintfile`

    # check again if its empty
    if [ -z $savedmd5 ]
    then
    	echo "The savedmd5 in $fingerprintfile  is empty " >> $LOG
        #compare the saved md5 with the one we have now
	else if [ "$savedmd5" = "$filemd5" ]
    then
        # pass silent
        :
    else
    	echo "$file"
    fi
	fi
fi

# save the current md5
echo $filemd5 > $fingerprintfile
}

checkInstallNaradEvent()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
	then
		dcaelogdebug "Installation of Narad Event Proc mS has completed already."
	else
		dcaelogdebug "Installation of Narad Event Proc mS has not started before, starting now..."
                #source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARAD.properties

		echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
	set -o allexport
	source ${VCC_HOME}/config/consumerNARAD.properties
	set +o allexport
}

checkInstallNaradDB()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
	then
		dcaelogdebug "Installation of Narad DB Proc mS has completed already."
	else
		dcaelogdebug "Installation of Narad DB Proc mS has not started before, starting now..."
                #source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARADDB.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARADDB.properties

		echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
	set -o allexport
	source 	$VCC_HOME/config/consumerNARADDB.properties
	set +o allexport
}

checkInstallAAIEvent()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
    then
		dcaelogdebug "Installation of AAI Event Proc mS has completed already."
	else
		dcaelogdebug "Installation of AAI Event Proc mS has not started before, starting now..."
        ###/opt/app/vcc/bin/install_aai_event.sh

                #source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumer.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumer.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumer.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumer.properties
                sed -i "/^UEBURL/c\UEBURL=${host}" $VCC_HOME/config/dti.properties

		echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
	set -o allexport
	source $VCC_HOME/config/consumer.properties
	source $VCC_HOME/config/dti.properties
	set +o allexport
}

checkInstallAAIDB()
{
	INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`

	if [ $INSTALL_DONE ]
	then
		dcaelogdebug "Installation of AAI DB Proc mS has completed already."
	else
		dcaelogdebug "Installation of AAI DB Proc mS has not started before, starting now..."
		####/opt/app/vcc/bin/install_aai.sh

                #source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
                #source $VCC_HOME/bin/dtiProc/export_dtiProc_EOM.cfg

                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerDB.properties
                sed -i "/^UEBURL/c\UEBURL=${host}" $VCC_HOME/config/dti.properties

		echo "install_complete" > /opt/app/vcc/bin/dtiProc/install_complete
	fi
	set -o allexport
	source $VCC_HOME/config/consumerDB.properties
	source $VCC_HOME/config/dti.properties
	set +o allexport
}

######################################################################
# Starting the main process of DTI Daemon, triggered from cron process
######################################################################

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=dti
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

StartRecordEvent "METRICS"
dcaelogdebug "Start $THISPROG at the time: `date`"
dcaelogaudit "script %s started" "$THISPROG"
LOG=$VCC_HOME/logs/DCAE/dti/dtiProcDaemon.`date +\%F`.log

#HOSTNAME=`getMyVecLogicalSystemName`
HOSTNAME=`hostname -s`

DTIPROC_HOME=$VCC_HOME
DTIPROC_CONF_DIR=$VCC_HOME/config/dtiproc
DTIPROC_OUTPUT_DIR=$VCC_HOME/data/output
DTIPROC_DM_BIN_DIR=$VCC_HOME/bin/common
mkdir -p $DTIPROC_OUTPUT_DIR 2>/dev/null

#dcaelogdebug "Setup ENV for different DTI image instance" 
EOM_ENV=`env | grep -i HOSTNAME`
#EOM_ENV=comattdcaedtidb-proc
dcaelogdebug "Setup ENV for different DTI image instance, this is for $EOM_ENV" 

if [ "$EOM_ENV" ]
then
	eventProcAAI=`echo $EOM_ENV | grep -i event-proc-aai`
	eventProc=`echo $EOM_ENV | grep -i event-proc`
	dbProcAAI=`echo $EOM_ENV | grep -i db-proc-aai`
	dbProc=`echo $EOM_ENV | grep -i db-proc`
else
	dcaelogdebug "The deployment is not on EOM env, exit."
	ERRORCATEGORY=FATAL ERRORCODE=100 RESPDESC="The deployment is not on EOM env, exit" \
    dcaelogerror "Not an EOM env for DTI to deploy the current instance!"
    exit 1
fi
       	       	        
KV_Config="/opt/app/vcc/tmp/configManager/output/KV_Config.json"
if [ ! -s $KV_Config ]
then
	ERRORCATEGORY=FATAL ERRORCODE=101 RESPDESC="The $KV_Config file is not found, deployment is not ready, DTI can not proceed. Exit!" \
    dcaelogerror "No KV_Config.json found in the current instance!"
	exit 1
fi

INSTALL_DONE1=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`
if [ $INSTALL_DONE1 ]
then	
	#if DTI is installed, need to check if any changes happened
	# 1. check if dti.cfg exists
	dti_cfg="/opt/app/vcc/bin/dti.cfg"
	dcaelogdebug "check if dti.cfg is empty"
	if [ ! -s $dti_cfg ]
	then
    	dcaelogdebug "No dti.cfg found even the installation is done. clean up install_complete"
    	rm -f /opt/app/vcc/bin/dtiProc/install_complete
    else
    	source $dti_cfg
    	# need to check if PGDB is running ok
    	dcaeEventCount=`echo "select count(*) from dti.dcae_event;" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
		if [ $dcaeEventCount -eq $dcaeEventCount 2>/dev/null ]
		then
        	if [ $dcaeEventCount -ge "0" ];then
                dcaelogdebug "Checked the PGDB Connection is successful" >>$LOG
			else
                dcaelogdebug "Error with Database Connection. Please check dti.cfg to verify the settings: PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti"
				ERRORCATEGORY=FATAL ERRORCODE=102 RESPDESC="The PGDB connection check failed!" \
    			dcaelogerror "PGDB settings not working: PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti"
    			exit 1
			fi
		fi
    	
    	# 2. check if DMaaP MR active host changed for AAI
		# check the DMaaP MR Host if still active for AAI event or DB, if not, clean out install_complete
		if [ "$eventProcAAI" ]
		then
			propertyFile=/opt/app/vcc/config/consumer.properties
		elif [ "$dbProcAAI" ]
		then
			propertyFile=/opt/app/vcc/config/consumerDB.properties
		fi

		if [ ! -z $propertyFile ]
		then    
			activeMR=$(grep -i "host=" $propertyFile |cut -d "=" -f 2)
    		tempFile=/tmp/checkActiveMR
		    curl --user $username:$password http://$activeMR/events/AAI-EVENT/${group}-$$/$id > $tempFile
    		FILESIZE=$(stat -c%s "$tempFile")
    		#### Check the file size to be 2 because sometime the successful response may have only '[]'
    		grep cambria.partition /tmp/checkActiveMR
    		if [[ $? -eq 0 || $FILESIZE -eq 2 ]]
    		then
        		dcaelogdebug "the DMaaP MR host as $activeMR is still active."
    		else
    			dcaelogdebug "the DMaaP MR host as $activeMR is changed, need to update."
    			rm -f /opt/app/vcc/bin/dtiProc/install_complete
    		fi 		   
    	fi
    fi
	
	# 3. check KV_Config
	kvConfigUpdated=$(checkFileUpdate "$KV_Config" "$VCC_HOME/config/watcher/.kv_config.md5") || exit 1
	dcaelogdebug "check if KV_Config.json is updated"
	if [ ! -z "$kvConfigUpdated" ] 
	then
		dcaelogdebug "KV_Config.json is updated, regenerate the configuration files by remove the install_complete."
		rm -f /opt/app/vcc/bin/dtiProc/install_complete
	else
		dcaelogdebug "No change for KV_Config.json."
	fi

	# 4. check if policy is updated for instances using policy
	Policy_Config="/opt/app/vcc/tmp/configManager/output/Policy_Config.json"
	if [ -s $Policy_Config ]
	then 
		policyConfigUpdated=$(checkFileUpdate "$Policy_Config" "$VCC_HOME/config/watcher/.policy_config.md5") || exit 1
		dcaelogdebug "check if Policy_Config.json is updated"
		if [ ! -z "$policyConfigUpdated" ] 
		then
			dcaelogdebug "Policy_Config.json is updated, regenerate the policy files by remove the install_complete."
			rm -f /opt/app/vcc/bin/dtiProc/install_complete
		else
			dcaelogdebug "No change for KV_Config.json."
		fi
	fi

	# 5. check if daemon is running
	# check if the Daemon process is running, if not, clean out install_complete and restart it
	pid=$(ps -ef | grep java | grep "InventoryCollector" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')    
	if [ -z "$pid" ]
	then
		dcaelogdebug "No Process Daemon running, cleanout install_complete"
		rm -f /opt/app/vcc/bin/dtiProc/install_complete
	else
		dcaelogdebug "current event process daemon is running as $pid"
	fi
else
        dcaelogdebug "INSTALL_DONE1 is empty, install is not complete"
fi
	
INSTALL_DONE=`ls /opt/app/vcc/bin/dtiProc | grep -i install_complete`
if [ $INSTALL_DONE ]
then
	dcaelogdebug "Installation of this DTI mS has completed already."
else
	dcaelogdebug "install_complete file not found, regenerate the config and start the daemon process."

	# comment out in 20.04, same out put as dti.cfg, with export	
	#grep export_ $KV_Config | sed -e "s/\":/=/" | tr -d \" | tr -d \ | sed 's/,*\r*$//' | sed -e "s/export_/export /" > $VCC_HOME/bin/dtiProc/export_dtiProc.cfg
	#source $VCC_HOME/bin/dtiProc/export_dtiProc.cfg

	dcaelogdebug "Generate the dti.cfg" 
	generateDTIConfig
	
	dcaelogdebug "Install PGDB schema update" 
	installSchemaUpdate
	

	dcaelogdebug "Check if policy directory exists or not ..."
	if [ ! -d ${VCC_HOME}/config/policy ]
	then
        dcaelogdebug "policy directory does not exist. so creating it ..."
        mkdir -p ${VCC_HOME}/config/policy
	fi

	dcaelogdebug "Set the CLASSPATH" 
	export CLASSPATH=$CLASSPATH:$VCC_HOME:$VCC_HOME/config:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti.jar:$VCC_HOME/lib/VNodeList.jar:$VCC_HOME/lib/configManager.jar:$VCC_HOME/lib/attMaven/'*'

	dcaelogdebug "Setup ENV for different DTI image instance, this is for $EOM_ENV" 
	if [ "$eventProcAAI" ]
	then
		dcaelogdebug "In EOM env, current microservice is deployed in (1) "$eventProcAAI

        # Calling transform_policy.py to transform the policy details to Java properties file under ${VCC_HOME}/config/policy/
		# $VCC_HOME/bin/configManager/reconfigure.sh
		# $VCC_HOME/bin/transform_policy.py

		# get AAI events for DTI-EVENT-PROC-AAI
		checkInstallAAIEvent

		############## Logic to check active DMaaP MR server, to get aai real time event.
		success=0
		# avoid globbing (expansion of *).
		set -f                      
		host_array=(${UEBURL//,/ })
		for i in "${!host_array[@]}"
		do
    		dcaelogdebug "Trying host $i=>${host_array[i]}"
    		FILENAME=/tmp/host$i
    		curl --user $username:$password http://${host_array[i]}:3904/events/AAI-EVENT/${group}-$$/$id > $FILENAME
    		FILESIZE=$(stat -c%s "$FILENAME")
    		#### Check the file size to be 2 because sometime the successful response may have only '[]'
    		grep cambria.partition /tmp/host$i
    		if [[ $? -eq 0 || $FILESIZE -eq 2 ]]
    		then
        		dcaelogdebug "updating consumer.properties with host ${host_array[i]}"
        		sed -i "/^host=/c\host=${host_array[i]}:3904" /opt/app/vcc/config/consumer.properties
       			success=1
        		break;
    		fi
		done

		if [ $success -eq 0 ]
		then
    		dcaelogdebug "ALL THE HOST IN THE DMAAP MR CLUSTER IS NOT PROVIDING PROPER RESPONSE."
    		ERRORCATEGORY=FATAL ERRORCODE=103 RESPDESC="No active DMAAP MR CLUSTER" \
    		dcaelogerror "No active DMAAP MR CLUSTER from $UEBURL for DTI to subscribe the events, start DTI Event Process Daemon Failed!" 
    		rm -f /opt/app/vcc/bin/dtiProc/install_complete
    		exit 1
		fi
		set -o allexport
		source /opt/app/vcc/config/consumer.properties
		set +o allexport

		pid=$(ps -ef |grep -i java| grep "InventoryCollector UPDATES NO" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
		if [ ! -z $pid ]
		then
			kill -9 $pid
		fi
		java_input_variable="UPDATES NO"
		#dcaelogdebug "Before starting AAI EVENT Process Daemon"
		#export datevar=`date +\%Y\%m\%d`; bash /opt/app/vcc/bin/startAAIRealtimeUpdates.sh >> /opt/app/vcc/logs/DCAE/dti/startAAIRealtimeUpdates.$datevar.log 2>&1 &
		#$VCC_HOME/bin/startRealtimeEventDaemon.sh AAIEVENT
		#dcaelogdebug "After calling startRealtimeEventDaemon for AAI EVENT Proccess"

	elif [ "$eventProc" ]
	then
		dcaelogdebug "In EOM env, current microservice is deployed in (2) "$eventProc

        # Calling transform_policy.py to transform the policy details to Java properties file under ${VCC_HOME}/config/policy/
        # $VCC_HOME/bin/configManager/reconfigure.sh
        $VCC_HOME/bin/transform_policy.py

        # get NARAD events for DTI-EVENT-PROC
        checkInstallNaradEvent

		pid=$(ps -ef | grep -i java |grep "InventoryCollector NARAD" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
		if [ ! -z $pid ]
		then
			kill -9 $pid
		fi
		java_input_variable="NARAD"
        #dcaelogdebug "Before calling startRealtimeEventDaemon for NARAD EVENT Process"
        #export datevar=`date +\%Y\%m\%d`;bash /opt/app/vcc/bin/getNARADRealTimeEvents.sh >> /opt/app/vcc/logs/DCAE/dti/getNARADRealTimeEvents.$datevar.log 2>&1 &
        #$VCC_HOME/bin/startRealtimeEventDaemon.sh NARADEVENT
        #dcaelogdebug "After calling startRealtimeEventDaemon for NARAD EVENT Proccess"

	elif [ "$dbProcAAI" ]
    then
		dcaelogdebug "In EOM env, current microservice is deployed in (3) "$dbProcAAI

		# get AAI events for DTI-DB-PROC-AAI
		checkInstallAAIDB

		############## Logic to check active DMaaP MR server, to get aai real time event.
		success=0
		# avoid globbing (expansion of *).
		set -f                      
		host_array=(${UEBURL//,/ })
		for i in "${!host_array[@]}"
		do
    		dcaelogdebug "Trying host $i=>${host_array[i]}"
    		FILENAME=/tmp/host$i
    		curl --user $username:$password http://${host_array[i]}:3904/events/AAI-EVENT/${group}-$$/$id > $FILENAME
    		FILESIZE=$(stat -c%s "$FILENAME")
    		#### Check the file size to be 2 because sometime the successful response may have only '[]'
    		grep cambria.partition /tmp/host$i
    		if [[ $? -eq 0 || $FILESIZE -eq 2 ]]
    		then
        		dcaelogdebug "updating consumer.properties with host ${host_array[i]}"
        		sed -i "/^host=/c\host=${host_array[i]}:3904" /opt/app/vcc/config/consumerDB.properties
       			success=1
        		break;
    		fi
		done

		if [ $success -eq 0 ]
		then
    		dcaelogdebug "ALL THE HOST IN THE DMAAP MR CLUSTER IS NOT PROVIDING PROPER RESPONSE."
    		ERRORCATEGORY=FATAL ERRORCODE=103 RESPDESC="No active DMAAP MR CLUSTER" \
    		dcaelogerror "No active DMAAP MR CLUSTER from $UEBURL for DTI to subscribe the events, start DTI Event Process Daemon Failed!" 
    		rm -f /opt/app/vcc/bin/dtiProc/install_complete
    		exit 1
		fi
		set -o allexport
		source /opt/app/vcc/config/consumerDB.properties
		set +o allexport
		
		pid=$(ps -ef |grep -i java | grep "InventoryCollector UPDATES YES" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
		if [ ! -z $pid ]
		then
			kill -9 $pid
		fi
        java_input_variable="UPDATES YES"
		#dcaelogdebug "Before calling startRealtimeEventDaemon for AAI DB Process"
		#export datevar=`date +\%Y\%m\%d`; bash /opt/app/vcc/bin/startAAIRealtimeUpdatesForDB.sh  >> /opt/app/vcc/logs/DCAE/dti/startAAIRealtimeUpdatesForDB.$datevar.log 2>&1 &
		#dcaelogdebug "After calling startRealtimeEventDaemon for AAI DB Proccess"

	else [ "$dbProc" ]
		dcaelogdebug "In EOM env, current microservice is deployed in (4) "$dbProc

		# get AAI events for DTI-DB-PROC
		checkInstallNaradDB
		pid=$(ps -ef |grep -i java | grep "InventoryCollector NARAD yes" | grep -v 'grep' | grep -v 'vi ' | awk '{ print $2}')
		if [ ! -z $pid ]
		then
			kill -9 $pid
		fi
        java_input_variable="NARAD yes"

        # dcaelogdebug "Before calling startRealtimeEventDaemon for NARAD DB Process"
        # export datevar=`date +\%Y\%m\%d`;bash /opt/app/vcc/bin/getNARADRealTimeEventsDB.sh >> /opt/app/vcc/logs/DCAE/dti/getNARADRealTimeEventsDB.$datevar.log 2>&1 &
        # dcaelogdebug "After calling startRealtimeEventDaemon for NARAD DB Process"
	fi
	
	export REMOTESERVER=`env | grep POD_IP | cut -c8-`; 
	dcaelogdebug "the REMOTESERVER IP is $REMOTESERVER"

	dcaelogdebug "%s %s" "INFO" "Calling DTI Java Process"
	export java_program="com.att.vcc.inventorycollector.InventoryCollector"
	StartRecordEvent "METRIC"
	$JAVA_HOME/bin/java -cp $CLASSPATH -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector $java_input_variable
	if [ $? == 0 ]; then
		STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="Inventory Collector Daemon" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The java daemon process %s completed successfully" "$java_program"
	else
		STATUSCODE=FAILURE RESPCODE=104 RESPDESC="Operation failed" TARGETENTITY="Inventory Collector Daemon" TARGETSVCNAME="$java_program" TARGETVENTITY=$REMOTESERVER \
		dcaelogmetrics "The java daemon process %s failed" "$java_program"
		ERRORCATEGORY=ERROR ERRORCODE=104 RESPDESC="java daemon process failed" \
		dcaelogerror "%s %s" "ERROR" "Inventory Collector Daemon Process failed"
	fi
fi
 
RUNTIME1=$(expr $(now_ms) - $STARTTIME)
dcaelogmetrics "End $THISPROG, elapsed time = $RUNTIME1"
dcaelogdebug "End $THISPROG, elapsed time = $RUNTIME1"

exit 0
