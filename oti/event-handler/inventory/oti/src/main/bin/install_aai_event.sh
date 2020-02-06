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

set -o allexport
export SUBCOMP=dti
if [ -z $VCC_HOME ]
then
        source /opt/app/vcc/bin/dti.cfg
        export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
        source $LOGFRAMEWORK/logging.func
else
        if [ ! -f ${VCC_HOME}/bin/dti.cfg ]
        then
                grep export_ ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | sed 's/,*\r*$//' | sed -e "s/export_//" > $VCC_HOME/bin/dti.cfg
                echo "DTI=${VCC_HOME}" >> $VCC_HOME/bin/dti.cfg
                echo "DTI_CONFIG=${VCC_HOME}/config" >> $VCC_HOME/bin/dti.cfg

                echo "DCAE_ENV=D2" >> $VCC_HOME/bin/dti.cfg
                echo "PATH=\$PATH:/opt/app/vcc/bin" >> $VCC_HOME/bin/dti.cfg

                unset http_proxy

				#Not need for mS need only for VM - 1908 Prod fix
                #PROD_TARGET=`grep \"export_Environment\" /opt/app/vcc/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e  "s/export_Environment=//"`
               
                source ${VCC_HOME}/bin/dti.cfg

        fi
        source ${VCC_HOME}/bin/dti.cfg
        export LOGFRAMEWORK=${VCC_HOME}/infrastructure/ecomp/logger/shell
        export LOGBASEDIR=${VCC_HOME}/logs
        source ${VCC_HOME}/infrastructure/ecomp/logger/shell/logging.func
fi
set +o allexport

#${VCC_HOME}/bin/checkCertificateFiles_1908.sh
#${VCC_HOME}/bin/dmd_password_update.sh
if [ -f /opt/app/aafcertman/.password ]
then
	echo "KEYSTORE_PASSWORD=`cat /opt/app/aafcertman/.password`" >> $VCC_HOME/bin/dti.cfg
fi

echo "Check if policy directory exists or not ..."
if [ ! -d ${VCC_HOME}/config/policy ]
then
        echo "policy directory does not exist. so creating it ..."
        mkdir -p ${VCC_HOME}/config/policy
fi


echo "FINISHED install.sh for aai event process"
