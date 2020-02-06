#!/bin/bash

# SWM can only store a finite amount of packages in its repository, so this script deletes the oldest package.
# This script is run by Jenkins after the build is finished (post SWM upload).

SWM_COMPONENT="com.att.vcc:dti"

SWM_PKGS=`/opt/app/swm/aftswmcli/bin/swmcli "component pkglist -c $SWM_COMPONENT -df -dh -dj -sui"`
SWM_PKGS_COUNT=`echo "$SWM_PKGS" | wc -l`
SWM_PKGS_OLDEST=`echo "$SWM_PKGS" | head -1`
SWM_PKGS_MAX_COUNT=10

if [ $SWM_PKGS_COUNT > $SWM_PKGS_MAX_COUNT ]
then
	SWM_PKG_OLDEST_VERSION=`echo $SWM_PKGS_OLDEST | awk '{print $2}'`

	# Delete the oldest package for this component from the SWM repository
	/opt/app/swm/aftswmcli/bin/swmcli "component pkgdelete -c $SWM_COMPONENT:$SWM_PKG_OLDEST_VERSION"
else
	echo "No need to clean up SWM, package count ($SWM_PKGS_COUNT) is below threshold ($SWM_PKGS_MAX_COUNT)"
fi