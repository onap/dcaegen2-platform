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

CONFIG_MGR_BIN=${VCC_HOME}/bin/configManager
source $CONFIG_MGR_BIN/setup-env.sh

#CONFIGMGR_JAVACLASS is env var set up in the setup-env.sh . It is to be customized for each component
#call java class to get app configuration from consul
libDir=${VCC_HOME}/lib
logSaveDir=${VCC_HOME}/logs/DCAE/${SUBCOMP}
CP=${CLASSPATH}:${libDir}/*:${libDir}/attMaven/*

mkdir -p ${logSaveDir}

java -cp ${CP} $LOG4J_OPTIONS ${CONFIGMGR_JAVACLASS} >${logSaveDir}/VCC_start-config-manager.log 2>&1


