#!/bin/bash
# ================================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
#
# ECOMP is a trademark and service mark of AT&T Intellectual Property.

mkdir -p logs
LOG_FILE=logs/oti_handler.log
exec &>> >(tee -a ${LOG_FILE})
echo "---------------------------------------------"
STARTED=$(date +%Y-%m-%d_%T.%N)
echo "${STARTED}: running ${BASH_SOURCE[0]}"
export APP_VER=$(python setup.py --version)
echo "APP_VER=${APP_VER}"
echo "HOSTNAME=${HOSTNAME}"
echo "CONSUL_URL=${CONSUL_URL}"
(pwd; uname -a; id; echo "ls -lanR:"; ls -lanR; echo "/etc/hosts:"; cat /etc/hosts; openssl version -a)
echo "---------------------------------------------"

export REQUESTS_CA_BUNDLE="/etc/ssl/certs/ca-certificates.crt"

# create the database tables
export PGPASSWORD=$postgres_password
psql -h $postgres_ip -U $postgres_user $postgres_db_name -f /tmp/create_schema.sql

python -m otihandler 2>&1 &
PID=$!

function finish {
  echo "killing oti_handler ${PID}" $(date +%Y_%m%d-%H:%M:%S.%N)
  kill -9 ${PID}
  echo "killed oti_handler ${PID}" $(date +%Y_%m%d-%H:%M:%S.%N)
}
trap finish SIGHUP SIGINT SIGTERM

echo "running oti_handler as" ${PID} "log" ${LOG_FILE}
#(free -h; df -h; ps afxvw; ss -aepi)

wait ${PID}
exec &>> >(tee -a ${LOG_FILE})
echo "---------------------------------------------"
rm ${LOG_FILE}.2[0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]_[0-9][0-9][0-9][0-9][0-9][0-9]
mv ${LOG_FILE} ${LOG_FILE}.$(date +%Y-%m-%d_%H%M%S)
