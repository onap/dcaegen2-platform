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

#Arg trigger_type = “dti” and 
#Arg updated_dti  is a json that has current dti event eg deploy/undeploy

trigger_type=$1
updated_dti=$2

datevar=`date +\%Y\%m\%d\%H\%M\%S`
LOG=${VCC_HOME}/logs/DCAE/configManager/VCC_reconfigure_${datevar}.log

echo "trigger_type=$trigger_type" > ${LOG}
echo "updated_dti=$updated_dti" >> ${LOG}

CONFIG_MGR_BIN=${VCC_HOME}/bin/configManager

#Call api to fetch the app config again.

source $CONFIG_MGR_BIN/get-configs.sh


# Each component reconfigures itself based on new configs
test -f $CONFIG_MGR_BIN/component-reconfigure.sh && source $CONFIG_MGR_BIN/component-reconfigure.sh
