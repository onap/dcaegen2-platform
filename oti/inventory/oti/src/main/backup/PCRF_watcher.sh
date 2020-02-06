#!/bin/bash

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

export SUBCOMP=dti
export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func
export REQUESTID=$(uuid)

THISPROG=$(basename $0)
STARTTIME=$(now_ms)
dcaelogdebug "%s %s %s %s" "INFO" "START" "$THISPROG" "has started... "
dcaelogaudit "script %s started" "$THISPROG"

$JAVA_HOME/bin/java -classpath /opt/app/vcc/config/:/opt/app/vcc/lib/:/opt/app/vcc/classes/:/opt/app/vcc/lib/dti-package-content-final.jar -Dlogback.configurationFile=$DTI/config/logger/logback.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.DMaapDRCANOPI PCRF

dcaelogdebug "%s %s" "INFO" "Completed PCRF watcher Java Process"
dcaelogaudit "script %s completed" "$THISPROG"