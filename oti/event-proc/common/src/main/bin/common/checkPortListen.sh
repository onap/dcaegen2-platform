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

###########################################################################
# Script Name: checkPortListen.sh
# Description: This script is used to check a port in LISTEN mode or not
#
#  Created: 10/18/2019  By: Hong Wang
###########################################################################

USAGE="USAGE: checkPortListen.sh -p <portNumber>"

while
	getopts :p:h: input
do
	case $input in
	p)
		portNum=$OPTARG
		;;
	h)
		echo "$USAGE"
		exit 1
		;;
	*)
		echo "$USAGE"
		;;
	esac
done
if test $OPTIND = 1
then
	echo $USAGE
	exit 1
fi

. $VCC_HOME/bin/common/vcc_env

netstat -a | grep $portNum | grep LISTEN > /dev/null
rc=$?

if test $rc -ne 0
then
	if test $portNum -eq $PM_PARSER_PORT
	then
		pName=pmParserDaemon
		echo "`date` kill -9 `cat $VCC_HOME/pid/${pName}.pid`"
		kill -9 `cat $VCC_HOME/pid/${pName}.pid`
	fi

	if test $portNum -eq $POST_PROCESS_PORT
	then
		pName=postProcessDaemon
		echo "`date` kill -9 `cat $VCC_HOME/pid/${pName}.pid`"
		kill -9 `cat $VCC_HOME/pid/${pName}.pid`
	fi
fi
