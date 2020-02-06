#!/usr/bin/env ksh

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

#############################################################
#
# Script Name: vccmgr.sh
# Description: This script is used to bounce vcc parser daemon
#
# Notes:
#
#############################################################
#
# Created: 05/18/2015   Release: 15.10  Project: 277170m  By: Hong Wang
# Revised: 04/01/2006   Release: 16.05  Project: 285867a  By: Hong Wang
#
#############################################################

# make sure only pec can run this script
#grep "$LOGNAME:" /etc/passwd|cut -d ":" -f1|grep 'vcc' >/dev/null 2>&1
#rc=$?
#if test "$rc" != 0
#then
#  echo "$LOGNAME is not allow to run this script"
#  exit 1
#fi

while true
do
	menu_vccmgr.sh
	rc=$?
	case $rc in
	10)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh ERIC start >> $VCC_HOME/log/error/vccParserEricDaemon.log 2>&1 
		;;
	20)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh ERIC stop >> $VCC_HOME/log/error/vccParserEricDaemon.log 2>&1 
		;;
	30)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh ALU start >> $VCC_HOME/log/error/vccParserAluDaemon.log 2>&1 
		;;
	40)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh ALU stop >> $VCC_HOME/log/error/vccParserAluDaemon.log 2>&1 
		;;
	50)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh MOB start >> $VCC_HOME/log/error/vccParserMobDaemon.log 2>&1 
		;;
	60)
		$VCC_HOME/bin/pmParser/vccParserDaemon.sh MOB stop >> $VCC_HOME/log/error/vccParserMobDaemon.log 2>&1 
		;;
	70)
		$VCC_HOME/bin/postProcess/postProcessDaemon.sh start >> $VCC_HOME/log/error/postProcessDaemon.log 2>&1
		;;
	80)
		$VCC_HOME/bin/postProcess/postProcessDaemon.sh stop >> $VCC_HOME/log/error/postProcessDaemon.log 2>&1
		;;	
	0)
		echo "Thank you for using vpecmgr, see you later"
		exit 0
		;;
	1)
		rc=`$VCC_HOME/bin/pmParser/vccParserDaemon.sh ERIC ps`
		if test "$rc" -gt 0
		then
			echo "vccParserDaemon ERIC is running on pid=$rc"
		else
			echo "vccParserDaemon ERIC is NOT running"
		fi	
		rc=`$VCC_HOME/bin/pmParser/vccParserDaemon.sh ALU ps`
		if test "$rc" -gt 0
		then
			echo "vccParserDaemon ALU is running on pid=$rc"
		else
			echo "vccParserDaemon ALU is NOT running"
		fi	
		rc=`$VCC_HOME/bin/pmParser/vccParserDaemon.sh MOB ps`
		if test "$rc" -gt 0
		then
			echo "vccParserDaemon Mobility is running on pid=$rc"
		else
			echo "vccParserDaemon Mobility is NOT running"
		fi
		rc=`$VCC_HOME/bin/postProcess/postProcessDaemon.sh ps`
		if test "$rc" -gt 0
		then
			echo "postProcessDaemon is running on pid=$rc"
		else
			echo "postProcessDaemon is NOT running"
		fi	
		;;
	*)
		clear
		echo "Invalid choice, try again"
		;;
	esac	
done
