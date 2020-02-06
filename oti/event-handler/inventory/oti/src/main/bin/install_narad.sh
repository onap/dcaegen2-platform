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
                grep export_ ${VCC_HOME}/tmp/configManager/output/KV_Config.json | sed -e "s/\":/=/" | tr -d \" | tr -d \ | tr -d , | sed -e "s/export_//" > $VCC_HOME/bin/dti.cfg
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
if [ -f /opt/app/aafcertman/.password ]
then
	echo "KEYSTORE_PASSWORD=`cat /opt/app/aafcertman/.password`" >> $VCC_HOME/bin/dti.cfg
fi

# this is for publishing files using DMaaP API
${VCC_HOME}/bin/updateDmaapConfig_dtidbproc.sh


#echo "Applying DB changes for 1908 release ..."
#cd ${DTI_CONFIG}/sql/narad/table
#cat narad_tables.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
#cd ${DTI_CONFIG}/sql/narad/view
#cat narad_views.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
#echo "FINISHED install.sh for 1908"

# Note: the flowing update PGDB is for 20.02, for NARAD tables
# It only need to install once, DTI will use this dti-db-proc for narad to do it, not other mS need this
# For future releases, comment out the following step 

echo "`date` This is to update the DB schema for 20.02 changes, it only needs to run once to update the PGDB tables ..."
cd $DTI/config
cat sql/table/2002_changes.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
echo "`date` Updating DB Objects for 2002 completed."

