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

if [[ $DTI_DATA_DIR ]]; then    # in k8s, DTI_DATA_DIR should be defined, default to INACTIVE
	echo "export K8S_CLUSTER_STATUS=INACTIVE" >> $VCC_HOME/bin/common/vcc_env
	echo "export NOTIFY_EVENT=FALSE" >> $VCC_HOME/bin/common/vcc_env
fi

CONFIG_MGR_BIN=${VCC_HOME}/bin/configManager

source $CONFIG_MGR_BIN/get-configs.sh

# install crontab if crontab.cfg existing
CRONFILE=$VCC_HOME/etc/crontab/crontab.cfg
if [ -f $CRONFILE ]
then
	tmp=`cat /etc/crontabs/root | grep "# BEGIN VCC" `
	if [ -z "$tmp" ]
	then
        # tmp is empty, no VCC cron is installed before
        # install VCC cron
		cat $CRONFILE >> /etc/crontabs/root
	fi
	
	mkdir -p $VCC_HOME/logs/DCAE/common
	/usr/sbin/crond start > $VCC_HOME/logs/DCAE/common/VCC_cron_startcrond.log 2>&1
fi

test -f $CONFIG_MGR_BIN/component-start-all-processes.sh && source $CONFIG_MGR_BIN/component-start-all-processes.sh

#Keep container alive for debugging, even if start-config-manager fails

while :
do	
	sleep 1
done
