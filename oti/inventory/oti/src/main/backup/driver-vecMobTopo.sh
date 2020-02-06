#!/bin/bash

export DTI=/home/netman/common/dti
. $DTI/bin/PROFILE

export WRITEDEBUGLOG=1
export SUBCOMP=driver_vecMobTopo
export REQUESTID=$(uuid)

#BASEDIR=$(dirname $0)
THISPROG=$(basename $0)

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

STARTTIME=$(now_ms)
#dcaelog METRICS "Start $THISPROG"
dcaelogdebug "%s %s" "START" "$THISPROG"
StartRecordEvent "AUDIT"

if [ -f /var/lock/netman/vecMobTopo ]; then
    DURATION=$(( $(date +%s) - $(date +%s -r /var/lock/netman/vecMobTopo) ))
    if [ $DURATION -le 1800 ]
    then
        dcaelogdebug "%s %s" "INFO" "Previous process still running. Exiting."
        dcaelogaudit "script %s failed" "$THISPROG"
        exit
    else
    	ERRORCATEGORY=WARN ERRORCODE=300 RESPDESC="Previous process ran for more time." \
		dcaelogerror "Previous process has run more than %s seconds" "$DURATION"
		dcaelogaudit "script %s failed" "$THISPROG"
        exit
    fi
fi

touch /var/lock/netman/vecMobTopo
for i in $(grep -v "^#" /home/netman/mobility/mobDcae.servers|grep ".*:.*lcp")
do
    hst=${i%%:*}
    if [ "$?" = 0 ]
    then
        dcaelogdebug "%s calling upload-vecMobTopo for  %s" "INFO" "$hst"
        $DTI/bin/upload-vecMobTopo.sh $hst > /opt/logs/dcae/dti/driver-vecMobTopo.log 2>&1 &
    else
    	ERRORCATEGORY=ERROR ERRORCODE=100 RESPDESC="Host not reachable" \
  		dcaelogerror "local DCAE: %s not reachable, skip" "$hst"
    fi
done

wait
rm /var/lock/netman/vecMobTopo
dcaelogaudit "script %s completed successfully" "$THISPROG"