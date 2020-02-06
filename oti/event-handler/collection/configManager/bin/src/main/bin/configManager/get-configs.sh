#!/bin/bash

CONFIG_MGR_BIN=${VCC_HOME}/bin/configManager
source $CONFIG_MGR_BIN/setup-env.sh

#CONFIGMGR_JAVACLASS is env var set up in the setup-env.sh . It is to be customized for each component
#call java class to get app configuration from consul
libDir=${VCC_HOME}/lib
logSaveDir=${VCC_HOME}/logs/DCAE/${SUBCOMP}
CP=${CLASSPATH}:${libDir}/*:${libDir}/attMaven/*

mkdir -p ${logSaveDir}

java -cp ${CP} $LOG4J_OPTIONS ${CONFIGMGR_JAVACLASS} >${logSaveDir}/VCC_start-config-manager.log 2>&1


