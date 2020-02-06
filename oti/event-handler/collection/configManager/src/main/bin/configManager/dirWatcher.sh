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
STARTTIME=$(now_ms)

export CLASSPATH=${CLASSPATH}:${VCC_HOME}/lib/configManager.jar
export CLASSPATH=${CLASSPATH}:${VCC_HOME}/lib/attMaven/*
	
export TASK_FILE=${VCC_HOME}/config/tasks/Task.json
export K8SCLUSTER_STATUS_FILE=${VCC_HOME}/K8SCLUSTER_STATUS.env
														
USAGE="USAGE: dirWatcher.sh -d <fullPathDir> "

while
getopts :d: input
do
  case $input in
	d)    
		dir=$OPTARG
		;;
  h)
    echo $USAGE
    exit 1
    ;;
  *)
    echo $USAGE
    ;;
  esac
done

if test $OPTIND = 1
then
  echo $USAGE
  exit 1
fi

localhost=`hostname -s`

LOG_DIR=$VCC_HOME/logs/DCAE/$SUBCOMP
mkdir -p $LOG_DIR 2>/dev/null

LOG4J_OPTIONS="-Dlogback.configurationFile=${VCC_HOME}/config/logger/logback_vcc.xml -Dsubcomponent=$SUBCOMP -DisThreadContextMapInheritable=true"

java $LOG4J_OPTIONS -DVCC_HOME=${VCC_HOME} -cp $CLASSPATH -Dcurrent_machine=$localhost com.att.vcc.configmanager.watcher.DirWatcher $dir &
