#!/bin/bash

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
