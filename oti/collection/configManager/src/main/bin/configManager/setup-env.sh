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

export SUBCOMP="configManager"

logDir=${VCC_HOME}/config/logger
export LOG4J_OPTIONS="-Dlogback.configurationFile=${logDir}/logback_vcc.xml -DisThreadContextMapInheritable=true -Dsubcomponent=${SUBCOMP}"

configOutput=${VCC_HOME}/tmp/configManager/output
mkdir -p ${configOutput}

export APP_CONFIG_FILE=${configOutput}/KV_Config.json
export DTI_CONFIG_FILE=${configOutput}/DTI_Config.json
export POLICY_CONFIG_FILE=${configOutput}/POLICY_Config.json

taskDir=${VCC_HOME}/config/tasks
yamlDir=${VCC_HOME}/config/configManager

mkdir -p ${taskDir}
mkdir -p ${yamlDir}

export TASK_FILE=${taskDir}/Task.json
export YAML_RESOURCE_DIR=${yamlDir}

if [ -x /usr/bin/python3.6 -a ! -f /usr/bin/python ]; then
        ln -s /usr/bin/python3.6 /usr/bin/python
fi

if [ -z "$NODE_NAME" ]; then
        nodename=`curl --noproxy "*" --connect-timeout 5 -s http://$CONSUL_HOST:8500/v1/agent/self?pretty | grep -m1 ''"NodeName"'' | cut -d\" -f4 `
else
        nodename=$NODE_NAME
fi

envChar=`echo $nodename |cut -c3`

case "$envChar" in 
	d) runenv="DEV" ;; 
	t) runenv="PST" ;; 
	e) runenv="E2E" ;; 
	l) runenv="PSL" ;; 
	p) runenv="PROD" ;; 
	*) runenv="DEV" ;;
esac
# echo "nodename=$nodename, envChar=$envChar, runenv=$runenv"
vccenvfile=${VCC_HOME}/bin/common/vcc_env
grep "export VCC_RUN_ENV=" $vccenvfile >/dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "export VCC_RUN_ENV=$runenv" >> $vccenvfile
	echo "export VCC_RUN_SERVER=$nodename" >> $vccenvfile
fi

# docker version does not have /opt/app/aafcertman, create a link
if [ ! -d /opt/app/aafcertman -a -d /opt/app/dcae-certificate ]; then
        ln -s /opt/app/dcae-certificate /opt/app/aafcertman
fi

# for snmppoller, copy the env specific commstr file to TargetTypeCommStr.cfg
if [ "$envChar" == "p" ]; then
	if [ -f ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.prod ]; then
		echo "cp ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.prod ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg"
		cp ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.prod ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg
	fi
else
	if [ -f ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.lab ]; then
		echo "cp ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.lab ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg"
		cp ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg.lab ${VCC_HOME}/config/snmp/TargetTypeCommStr.cfg
	fi
fi


# For k8s, MYHOSTNAM=COMPONENT_NAME
# For docker, MYHOSTNAM=HOSTNAME
#export MYHOSTNAME=${COMPONENT_NAME:-$HOSTNAME}

# /service_component_all/<srvc_name>?pod=<pod name>&cluster=<k8s fqdn>&namespace=<k8s namespace>
# http://dcae-cbs-site1-dyh1b-d11.ecomp.idns.cip.att.com:30104/service_component_all/zldcdyh1bdcc4-snmp-vdbe-vusp-0912v3?pod=sts-zldcdyh1bdcc4-snmp-vdbe-vusp-0912v3-0&cluster=zldcdyh1bdcc4kcma00.d54852.dyh1b.tci.att.com&namespace=com-att-dcae-onboarding-dev

if [[ $COMPONENT_NAME ]]; then    # in k8s, COMPONENT_NAME should be defined
	export MYHOSTNAME="$COMPONENT_NAME?pod=$HOSTNAME&cluster=$KUBE_CLUSTER_FQDN&namespace=$POD_NAMESPACE"
else
	export MYHOSTNAME=$HOSTNAME
fi

echo "MYHOSTNAME=$MYHOSTNAME"

export K8SCLUSTER_STATUS_FILE=${VCC_HOME}/K8SCLUSTER_STATUS.env
export K8SCLUSTER_NAME=$KUBE_CLUSTER_FQDN

CONFIG_MGR_BIN=${VCC_HOME}/bin/configManager
test -f $CONFIG_MGR_BIN/component-setup-env.sh && source $CONFIG_MGR_BIN/component-setup-env.sh
