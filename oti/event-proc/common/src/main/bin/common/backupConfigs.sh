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

echo `date`
echo SERVICE_TAGS=$SERVICE_TAGS

# copy (backup) config files, tasks file, collection history files, etc
if [ -d $VCC_HOME/config/tasks ] 
then
	mkdir -p $VCC_HOME/archive/backup/config/tasks
	cp $VCC_HOME/config/tasks/Task.json $VCC_HOME/archive/backup/config/tasks
	cp $VCC_HOME/config/tasks/Task_FOI_Properties.json $VCC_HOME/archive/backup/config/tasks
fi	

if [ -d $VCC_HOME/config/snmp ] 
then
	mkdir -p $VCC_HOME/archive/backup/config/snmp
	cp $VCC_HOME/config/snmp/*.CommStr $VCC_HOME/archive/backup/config/snmp
	cp $VCC_HOME/config/snmp/*.outputIndex $VCC_HOME/archive/backup/config/snmp
fi

if [ -d $VCC_HOME/tmp/FOI/repository ] 
then
	mkdir -p $VCC_HOME/archive/backup/FOI/repository
	cp $VCC_HOME/tmp/FOI/repository/* $VCC_HOME/archive/backup/FOI/repository
fi

if [ -d $VCC_HOME/config/ceilo ] 
then
	mkdir -p $VCC_HOME/archive/backup/config/ceilo
	cp $VCC_HOME/config/ceilo/CeiloProfile.cfg $VCC_HOME/archive/backup/config/ceilo
fi
	