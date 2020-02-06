#!/bin/bash

USAGE="updateK8sCluster Status.sh status[ACTIVE|INACTIVE] isNofityEvent[TRUE|FALSE]"

. $HOME/bin/common/vcc_env

status=$1
isNofityEvent=$2

# Do not use the initial value from the deployment if there is already a “notify” event from DTI.
# The status from “notify” event will take precedence. 

if [ $isNofityEvent = "TRUE" ]
then
	sed -i.backup`date '+%Y%m%d%H%M%S'` "s/K8S_CLUSTER_STATUS=.*/K8S_CLUSTER_STATUS=${status}/g" $VCC_HOME/bin/common/vcc_env
	sed -i.backup`date '+%Y%m%d%H%M%S'` "s/NOTIFY_EVENT=.*/NOTIFY_EVENT=TRUE/g" $VCC_HOME/bin/common/vcc_env
elif [ $NOTIFY_EVENT = "FALSE" ]
then
	sed -i.backup`date '+%Y%m%d%H%M%S'` "s/K8S_CLUSTER_STATUS=.*/K8S_CLUSTER_STATUS=${status}/g" $VCC_HOME/bin/common/vcc_env
fi
