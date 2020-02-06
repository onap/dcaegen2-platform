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

. $HOME/bin/common/vcc_env

export WRITEDEBUGLOG=1
export SUBCOMP=DirWatcher
export DCAELOGBASEDIR=$VCC_HOME/logs/DCAE
export LOGFRAMEWORK=${LOGFRAMEWORK:-$VCC_HOME/infrastructure/ecomp/logger/shell}
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
localhost=`hostname -s`
today=`date +%Y-%m-%d`
now=`date "+%Y-%m-%d %H:%M:%S"`

LOGDIR=$VCC_HOME/logs/DCAE/$SUBCOMP
LOGFILE=$LOGDIR/VCC_stopDirWatcher_$today.log
mkdir -p $LOGDIR 2>/dev/null

pid=`ps -aefu $LOGNAME | grep "com.att.vcc.configmanager.watcher.DirWatcher" | awk '{if ($8!="grep") print $2}'`

#if test "$pid" -gt 0
if test -n "$pid"
then
        echo "$now|stopDirWatcher::Kill pid=$pid" >> $LOGFILE
        kill -9 $pid
else
        echo "$now|stopDirWatcher::DirWatcher process is not running" >> $LOGFILE
fi
