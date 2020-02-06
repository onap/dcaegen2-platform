#!/bin/bash


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
