#!/bin/bash

export WRITEDEBUGLOG=1
export SUBCOMP=driver_vecMobTopo

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

. /home/netman/common/bin/util.func

RSYNC_OPTS="-aqz --chmod=g+w"

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

SERVER=$1
HOSTNAME=$(echo $SERVER | cut -d'.' -f1)
#Added the below if then to serve ip address also
if echo $HOSTNAME | egrep -q '^[0-9]+$';
then
    HOSTNAME=$SERVER
fi
echo "hostname is $HOSTNAME"
dcaelogdebug "%s %s %s %s" "Start" "$THISPROG" ", localdcae=" "$HOSTNAME"
centralHost=$(hostname| cut -d'.' -f1)
centralCLLI=$(echo $centralHost| cut -c1-5 )

BASEDIR=/spool/mobility/vec/$HOSTNAME/
READY_FOR_PROC=/spool/mobility/vec/$HOSTNAME/
VEC_HOME=/opt/app/vec
REMOTEDIR=${VEC_HOME}/data/input/VEC/
REMOTETASKDIR=${VEC_HOME}/config/tasks/
LOGFILE=/home/netman/log/vecMobTopo-transfer
ARCHIVEDIR=/spool/mobility/archive/
##############################################################

# test connection first
test_connection $SERVER
ret=$?
if [ "$ret" -ne 0 ]
then
    echo  "ERROR local DCAE: $SERVER not reachable, error= $ret, return"
    ERRORCATEGORY=ERROR ERRORCODE=100 RESPDESC="Host not reachable" \
  	dcaelogerror "%s %s" "ERROR" "local DCAE: $SERVER not reachable, error= $ret, return"
  	dcaelogaudit "script %s failed" "$THISPROG"
    exit 1
fi

if [ ! -d $READY_FOR_PROC ]; then
    mkdir -p $READY_FOR_PROC
fi

cd ${BASEDIR}
for k in topology
do
    if [ ! -d ${BASEDIR}${k} ]; then
        mkdir -p ${BASEDIR}${k}
    fi
    cd ${BASEDIR}${k}
    dcaelogdebug "%s %s" "DEBUG" "server=$HOSTNAME,feed=${k}:check files"
    NUM_FILES=$(ls|wc -l)
    echo "NUM_FILES=$NUM_FILES for $k"
    if [[ $NUM_FILES -gt 0 ]]
    then
        dcaelogdebug "%s %s" "DEBUG" "server=$HOSTNAME,feed=${k}:transfer start"
        #rsync ${RSYNC_OPTS} . vec@${SERVER}:${REMOTEDIR}${k}/
        # rsync .txt files to /opt/app/vec/data/input/VEC/topology on l-dcae
        StartRecordEvent "METRIC"
        rsync ${RSYNC_OPTS} *.txt vec@${SERVER}:${REMOTEDIR}${k}/
        if [ $? == 0 ]; then
			STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="rsync txt files" TARGETSVCNAME="rsync" TARGETVENTITY=${SERVER} \
			dcaelogmetrics "The child script %s completed successfully" "rsync"
		else
			STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="rsync txt files" TARGETSVCNAME="rsync" TARGETVENTITY=${SERVER} \
			dcaelogmetrics "The child script %s failed" "rsync"
			ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
			dcaelogerror "%s %s" "ERROR" "rsync of json files failed"
		fi
        StartRecordEvent "METRIC"
        # rsync .json files to /opt/app/vec/config/tasks on l-dcae
        rsync ${RSYNC_OPTS} *.json vec@${SERVER}:${REMOTETASKDIR}
        if [ $? == 0 ]; then
			STATUSCODE=COMPLETE RESPCODE=200 RESPDESC="Operation completed successfully" TARGETENTITY="rsync json files" TARGETSVCNAME="rsync" TARGETVENTITY=${SERVER} \
			dcaelogmetrics "The child script %s completed successfully" "rsync"
		else
			STATUSCODE=FAILURE RESPCODE=101 RESPDESC="Operation failed" TARGETENTITY="rsync json files" TARGETSVCNAME="rsync" TARGETVENTITY=${SERVER} \
			dcaelogmetrics "The child script %s failed" "rsync"
			ERRORCATEGORY=ERROR ERRORCODE=102 RESPDESC="child script failed" \
			dcaelogerror "%s %s" "ERROR" "rsync of json files failed"
		fi
        dcaelogdebug "%s %s" "DEBUG" "server=$HOSTNAME,feed=${k}:transfer complete"
        fileSent=$NUM_FILES
        if [ ! -d $ARCHIVEDIR${k}/$HOSTNAME ]; then
            mkdir -p $ARCHIVEDIR${k}/$HOSTNAME
        fi
        timestamp=$(date +%Y%m%d%H%M%S)
        for f in $(ls {configs_*.txt,Task_*.json})
        do
            echo "file is $f"
            mv $f ${ARCHIVEDIR}${k}/$HOSTNAME/"${timestamp}_$f"
        done

        dcaelogdebug "%s %s" "DEBUG" "feed=${k}: ${NUM_FILES} files sent to vec server at $HOSTNAME"
    else
        dcaelogdebug "%s %s" "DEBUG" "feed=${k}: ${NUM_FILES} files sent to vec server at $HOSTNAME"
    fi
done

dcaelogaudit "script %s completed successfully" "$THISPROG"