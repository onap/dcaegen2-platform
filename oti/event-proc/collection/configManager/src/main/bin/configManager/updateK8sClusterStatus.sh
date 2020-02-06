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
