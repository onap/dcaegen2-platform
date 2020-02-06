#!/bin/bash

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=FileMonitor
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
localhost=`hostname -s`
today=`date +%Y-%m-%d`
now=`date "+%Y-%m-%d %H:%M:%S"`

LOGDIR=$VCC_HOME/logs/DCAE/$SUBCOMP
LOGFILE=$LOGDIR/VCC_stopFileMonitor_$today.log
mkdir -p $LOGDIR 2>/dev/null

pid=`ps -aefu $LOGNAME | grep "com.att.vcc.configmanager.watcher.FileMonitor" | awk '{if ($8!="grep") print $2}'`

#if test "$pid" -gt 0
if test -n "$pid"
then
        echo "$now|stopFileMonitor::Kill pid=$pid" >> $LOGFILE
        kill -9 $pid
else
        echo "$now|stopFileMonitor::FileMonitor process is not running" >> $LOGFILE
fi
