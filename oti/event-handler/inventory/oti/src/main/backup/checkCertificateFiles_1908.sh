#!/bin/bash

set -o allexport

echo "Check if keystore directory exists or not ..."
if [ ! -d ${VCC_HOME}/keystore ]
then
        echo "keystore directory does not exist. so creating it ..."
        mkdir -p ${VCC_HOME}/keystore
        echo "copy certificate files from /opt/app/dcae-certificate to keystore directory ..."

	cp -rf /opt/app/aafcertman/*  ${VCC_HOME}/keystore/
        cat /opt/app/aafcertman/.password >> ${VCC_HOME}/keystore/.password

        echo "copy password"
        echo "KEYSTORE_PASSWORD=`cat /opt/app/vcc/keystore/.password`" >> $VCC_HOME/bin/dti.cfg
fi
