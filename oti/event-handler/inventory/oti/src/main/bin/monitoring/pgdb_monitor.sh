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

export WRITEDEBUGLOG=1
export SUBCOMP=driver_vecTopo

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

LOG=/opt/app/vcc/logs/DCAE/dti/pdgb_monitor_$date.log
set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

cd $DTI/config

echo "`date` PGDN Monitor script starting up." >> $LOG
echo "`date Starting moniring the postgre db to make sure it is working." >> $LOG

echo "select count(*) from dti.narad_pnf;" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

--> check login
--> check PGDB URL
--> check successful 

echo "FINISHED  checking ." >> $LOG
