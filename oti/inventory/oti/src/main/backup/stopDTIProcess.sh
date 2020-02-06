#!/bin/ksh
DTI=<installdir>
. $DTI/bin/PROFILE
set -x
MACHINE=$(uname -n)
PROCESS=startAAIRealtimeUpdates
while true
do
	ps -ef | grep $PROCESS | awk '{if ($8=="/bin/bash") print $2}' > /opt/logs/dcae/dti/$PROCESS.$MACHINE.id
	if [ -s /opt/logs/dcae/dti/$PROCESS.$MACHINE.id ]
	then
		echo "$PROCESS is still running $(date)"
		print -n "Killing $PROCESS "
		cat /opt/logs/dcae/dti/$PROCESS.$MACHINE.id
		pkill -P `cat /opt/logs/dcae/dti/$PROCESS.$MACHINE.id`
	else
		echo "$PROCESS is already stopped $(date)"
		exit 1
	fi
	sleep 5
done
