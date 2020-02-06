#!/bin/bash
set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport


usage()
{
    echo
    echo "Usage:        $0 Datetime Servive Type ID"
    echo "              where   Datetime = Current Date and Time          - required"
    echo "                      Service  = ALL or vUSP or Trinity or FULL - required"
    echo "                      Type     = generif-vnf or vserver         - optional"
    echo "                      ID       = vnf_id      or vserver_id      - optional"
    echo "                      "
    echo "                      If Service = ALL/vUSP/Trinity, then publish delta file and snapshot file to DMD"
    echo "                      If Service = FULL then publish full file, delta file and snapshot file to DMD"
    echo "                      "
    echo "                      "
    exit 17
}


if [ "$#" -eq 2 ] ; 
then
	DATETIME=$1
	SERVICE_DESCRIPTION=$2
	ENTITY_TYPE=NO_INPUT_TYPE
	ENTITY_VALUE=NO_INPUT_VALUE
elif [ "$#" -eq 4 ] ; 
then
	DATETIME=$1
	SERVICE_DESCRIPTION=$2
	ENTITY_TYPE=$3
	ENTITY_VALUE=$4
else
	usage
fi


export datetimevar=$(date +\%Y\%m\%d\%H\%M\%S)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

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

#process_count=$(ps -fu $currentuser | grep -v grep | grep -v 'vi ' | grep -v 'sudo' | grep "$temp_process" | grep -v $process |  grep -c $temp_process)
if [ $process_count -ge 1 ]
then
    dcaelogerror "%s %s %s %s" "WARN" "There is another instance of the process" "$process" "is running. Quitting startAAIRealtimeUpdates.sh Process"
    echo $0 INFO "There is another instance of the process $prog is running. Quitting ProcessVNodeList.sh Process"
    exit 0
fi


echo $0 INFO "Starting processing ..."

CLASSPATH=$CLASSPATH:$DTI_CONFIG:$DTI/classes:$DTI/lib:$DTI/
CLASSPATH=$CLASSPATH:$DTI/lib/VNodeList-0.0.1-final.jar:$DTI/lib/dti-package-content-final.jar
echo $CLASSPATH
export CLASSPATH
PATH=$JAVA_HOME/bin:$PATH
export PATH
echo $PATH
cd $DTI

#CLASSPATH=$CLASSPATH:$CTI3RD/jdb/lib/log4j-1.2.16.jar:$CTI3RD/weblogic/wlserver_12.1/server/adr/ojdbc6.jar:$CTI/web/lib/mail.jar:$CTI/deploy/VNodeList/classes/VNodeList-0.0.1-final.jar
#export CLASSPATH

echo $0 INFO "Calling VNodeList Java Process"
#$JAVA_HOME/bin/java -Dhttps.protocols=TLSv1.1,TLSv1.2 com.att.vcc.VNodeList.VNodeList $DATETIME $SERVICE_DESCRIPTION $ENTITY_TYPE $ENTITY_VALUE 
$JAVA_HOME/bin/java -Dhttps.protocols=TLSv1.1,TLSv1.2 vnodelist.src.main.java.com.att.vcc.VNodeList.VNodeList $DATETIME $SERVICE_DESCRIPTION $ENTITY_TYPE $ENTITY_VALUE 
STATUS=$?
echo $0 INFO "Completed VNodeList Java Process, STATUS=$STATUS"

if [ $STATUS == 0 ]
then
	# Project 288349 to call DeviceInterfaceMapping.sh
	# cd $CTI/hlc/shells
	# ./DeviceInterfaceMapping.sh
	echo "STATUS=$STATUS"

fi

exit $STATUS


