#!/bin/bash

set -o allexport
export SUBCOMP=dti
if [ -z $VCC_HOME ]
then
        source /opt/app/dti/bin/dti.cfg
        export LOGFRAMEWORK=/opt/app/dcae-commonlogging/shell
        source /opt/app/dcae-commonlogging/shell/logging.func
else
        if [ ! -f ${VCC_HOME}/bin/dti.cfg ]
        then
                grep export_ ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/export_//" > $VCC_HOME/bin/dti.cfg
                echo "KEYSTORE_PASSWORD=`cat /opt/app/dcae-certificate/.password`" >> $VCC_HOME/bin/dti.cfg
                echo "DTI=${VCC_HOME}" >> $VCC_HOME/bin/dti.cfg
                echo "DTI_CONFIG=${VCC_HOME}/config" >> $VCC_HOME/bin/dti.cfg
                unset http_proxy
                dti2_handler_service_discovery=$(curl http://${CONSUL_HOST}:8500/v1/catalog/service/dti2_handler?pretty | jq '.[0] | .NodeMeta.fqdn')
                dti2_handler_api=$(echo $dti2_handler_service_discovery | tr -d '"')
                echo "ORCH_POST_URL=https://"${dti2_handler_api}":8443/events" >> $VCC_HOME/bin/dti.cfg
                source ${VCC_HOME}/bin/dti.cfg
                sed -i "/^Environment/c\Environment=${Environment}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^username/c\username=${username}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^password/c\password=${password}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^group/c\group=${group}" $VCC_HOME/config/consumerNARAD.properties
                sed -i "/^host=/c\host=${host}" $VCC_HOME/config/consumerNARAD.properties
        fi
        source ${VCC_HOME}/bin/dti.cfg
        export LOGFRAMEWORK=${VCC_HOME}/infrastructure/ecomp/logger/shell
        export LOGBASEDIR=${VCC_HOME}/logs
        source ${VCC_HOME}/infrastructure/ecomp/logger/shell/logging.func
fi
set +o allexport

echo "Check if keystore directory exists or not ..."
if [ ! -d ${VCC_HOME}/keystore ]
then
        echo "keystore directory does not exist. so creating it ..."
        mkdir -p ${VCC_HOME}/keystore
        echo "copy certificate files from /opt/app/dcae-certificate to keystore directory ..."
        cp -rf /opt/app/dcae-certificate/* ${VCC_HOME}/keystore/
fi

echo "Check if policy directory exists or not ..."
if [ ! -d ${VCC_HOME}/config/policy ]
then
        echo "policy directory does not exist. so creating it ..."
        mkdir -p ${VCC_HOME}/config/policy
fi
echo "Copying jl1 & jl2 policy file to config/policy directory..."
cp ${VCC_HOME}/config/pnf_snmp* ${VCC_HOME}/config/policy

echo "FINISHED install.sh for 1902"
