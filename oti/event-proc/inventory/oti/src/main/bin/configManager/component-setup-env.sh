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

#Sample contents
#TODO replace with concrete implementation of ConfigManager or its subclass
#Edit and Uncomment following line to export name of Class. 
# There is no need to invoke java explicitly since its done in common start-config-manager.sh 
# It assumes that jar containing this class is in $VCC_HOME/lib .
# If it is anywhere other than $VCC_HOME/lib then uncomment following CLASSPATH
 
CLASSPATH=$CLASSPATH:$VCC_HOME:$VCC_HOME/config:$VCC_HOME/config/dtiproc/:$VCC_HOME/lib/dti.jar:$VCC_HOME/lib/configManager.jar
export CLASSPATH=$CLASSPATH:$VCC_HOME/lib/common.jar:$VCC_HOME/lib/attMaven/*

#export CLASSPATH=${CLASSPATH}:${VCC_HOME}/lib/DtiProc.jar

export CONFIGMGR_JAVACLASS=com.att.vcc.dtiProcConfigManager.DtiProcConfigManager
#export CONFIGMGR_JAVACLASS=com.att.vcc.configmanager.SingleVnfTypeTaskConfigManager

mkdir -p $VCC_HOME/logs/DCAE/postProcessDaemon
#mkdir -p $VCC_HOME/logs/DCAE/publisherDaemon
mkdir -p $VCC_HOME/logs/DCAE/dti

outDir1="$VCC_HOME/data/input/com.att.ecomp.aai.NARAD-EVENT"
mkdir -p $outDir1

mkdir -p $VCC_HOME/data/input/archive


