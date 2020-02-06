#!/bin/bash
HOME_DIR=/opt/app/vcc
TEMP_JSON=${HOME_DIR}/tmp/configManager/output/temp.json
PARAMETER_TEMP=${HOME_DIR}/tmp/configManager/output/docker.tmp
PARAMETER_FILE=${HOME_DIR}/tmp/configManager/output/POLICY_Config.json


curl -s http://${CONSUL_HOST}:8500/v1/catalog/service/${CONFIG_BINDING_SERVICE} > ${TEMP_JSON}

serviceAddress=`cat ${TEMP_JSON} | jq -r '.[0] .ServiceAddress'`
servicePort=`cat ${TEMP_JSON} | jq '.[0] .ServicePort'`

curl -s http://${serviceAddress}:${servicePort}/service_component_all/${HOSTNAME} > ${PARAMETER_TEMP}

mv ${PARAMETER_TEMP} ${PARAMETER_FILE}

rm -f ${TEMP_JSON}

exit 0
