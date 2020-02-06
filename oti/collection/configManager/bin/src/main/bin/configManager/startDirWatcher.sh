#!/bin/bash

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=DirWatcher
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)

localhost=`hostname -s`

today=`date +%Y-%m-%d`
now=`date "+%Y-%m-%d %H:%M:%S"`

LOGDIR=$VCC_HOME/logs/DCAE/$SUBCOMP
LOGFILE=$LOGDIR/VCC_startDirWatcher_$today.log
mkdir -p $LOG_DIR 2>/dev/null

if [[ ! $DTI_DATA_DIR ]]; then          # DTI_DATA_DIR is NOT set, then it is NOT in k8s, exit
        echo "$now|DTI_DATA_DIR is NOT set, it is NOT in k8s, do not start DirWatcher" >> $LOGFILE
        exit
fi

echo "$now|DTI_DATA_DIR is set, it is in k8s, DTI_DATA_DIR=$DTI_DATA_DIR" >> $LOGFILE

export CLASSPATH=${CLASSPATH}:${VCC_HOME}/lib/configManager.jar
export CLASSPATH=${CLASSPATH}:${VCC_HOME}/lib/attMaven/*
export CLASSPATH=${CLASSPATH}:${VCC_HOME}/config/configManager

export TASK_FILE=${VCC_HOME}/config/tasks/Task.json
export K8SCLUSTER_STATUS_FILE=${VCC_HOME}/K8SCLUSTER_STATUS.env

pid=`ps -aefu $LOGNAME | grep "com.att.vcc.configmanager.watcher.DirWatcher" | awk '{if ($8!="grep") print $2}'`

if test -n "$pid"
then
        echo "$now|startDirWatcher::DirWatcher is already running, pid=$pid" >> $LOGFILE
else
        dir=$DTI_DATA_DIR/process
        LOG4J_OPTIONS="-Dlogback.configurationFile=${VCC_HOME}/config/logger/logback_vcc.xml -Dsubcomponent=$SUBCOMP -DisThreadContextMapInheritable=true"
java $LOG4J_OPTIONS -DVCC_HOME=${VCC_HOME} -cp $CLASSPATH -Dcurrent_machine=$localhost com.att.vcc.configmanager.watcher.DirWatcher $dir & >> $LOGFILE 2>&1 &
        sleep 2
        pid=`ps -aefu $LOGNAME | grep "com.att.vcc.configmanager.watcher.DirWatcher" | awk '{if ($8!="grep") print $2}'`
        echo "$now|startDirWatcher::Started DirWatcher pid=$pid, directory=$dir" >> $LOGFILE
fi
