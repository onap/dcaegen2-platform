#!/bin/bash

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
