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

#source ${VCC_HOME}/bin/configManager/component-start-all-processes.sh
#sleep 10

# Exit with status message
function exit_with_status
{
	echo "$1"
	exit 0
}

# Check file last modified time. If the last modified time >= THRESHOLD_IN_SECONDS, return WARN message
# Default THRESHOLD_IN_SECONDS is 900 (15 mins)
function check_file_last_modify_time
{
	CHECK_DIR=$1
	CHECK_FILE=$2

	STAT_CMD="stat -c %Y"
	THRESHOLD_IN_SECONDS=900

	# Now is seconds
	now=`date +%s`
	last_modify_time=0

	if test -d "$CHECK_DIR"
	then
		if test -f "$CHECK_DIR/$CHECK_FILE"
		then
			last_modify_time=`$STAT_CMD $CHECK_DIR/$CHECK_FILE`
		else
			echo "WARN|$CHECK_DIR/$CHECK_FILE does not exist"
			return
		fi
	else
		echo "WARN|$CHECK_DIR does not exist"
		return
	fi

	diff=$(expr $now - $last_modify_time)

	if [ $diff -ge $THRESHOLD_IN_SECONDS ]
	then
		m=$(expr $diff / 60)
		echo "WARN|$CHECK_DIR/$CHECK_FILE is not updated in last $m minutes"
	fi

	echo ""
}

# Check if the process is running or not
# If process is not running, exit with error message
function check_process
{
	PROCESS_NAME=$1
	pid=`ps -fu $LOGNAME | egrep "$PROCESS_NAME" | egrep -v 'grep' | awk '{print $2}'`

	if test -z "$pid"
	then
		RETURN_MSG="ERROR|$PROCESS_NAME is not running"
		exit_with_status "$RETURN_MSG"
	fi
}

