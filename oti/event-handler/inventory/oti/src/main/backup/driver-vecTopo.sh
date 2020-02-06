#!/bin/bash

export DTI=/home/netman/common/dti
. $DTI/bin/PROFILE

export WRITEDEBUGLOG=1
export SUBCOMP=driver_vecTopo
export REQUESTID=$(uuid)

#BASEDIR=$(dirname $0)
THISPROG=$(basename $0)

export LOGFRAMEWORK=/opt/app/vcc/infrastructure/ecomp/logger/shell
source $LOGFRAMEWORK/logging.func

dcaelogdebug "%s %s" "START" "$THISPROG"
StartRecordEvent "AUDIT"

if [ -f /var/lock/netman/vecTopo ]; then
    DURATION=$(( $(date +%s) - $(date +%s -r /var/lock/netman/vecTopo) ))
    if [ $DURATION -le 1800 ]
    then
        dcaelogdebug "%s %s" "INFO" "Previous process still running. Exiting."
        exit
    else
        ERRORCATEGORY=WARN ERRORCODE=300 RESPDESC="Previous process ran for more time." \
		dcaelogerror "Previous process has run more than %s seconds" "$DURATION"
        exit
    fi
fi

touch /var/lock/netman/vecTopo
for i in $(grep -v "^#" /home/netman/gamma/dcae.servers|grep ".*:.*lcp")
do
    hst=${i%%:*}
    #ping -c 1 $hst &> /dev/null
#    wget -T 20 -t 2 -O /dev/null http://$hst:22>/dev/null
    if [ "$?" = 0 ]
    then
    	dcaelogdebug "%s %s" "INFO" "calling upload-vecTopo for $hst"
        $DTI/bin/upload-vecTopo.sh $hst > /opt/logs/dcae/dti/driver-vecTopo.log 2>&1 &
    else
    	ERRORCATEGORY=ERROR ERRORCODE=400 RESPDESC="Host not reachable" \
  		dcaelogerror "local DCAE: %s not reachable, skip" "$hst"
    fi
done

wait
rm /var/lock/netman/vecTopo
dcaelogaudit "script %s completed successfully" "$THISPROG"