#!/bin/bash
# This script to create the dynamic password and put it into dmd.properties file
set -o allexport
echo "$0 started at $(date)"
source /opt/app/dmd/bin/dmdenv.sh

filename=/opt/app/aafcertman/.password
 if [ ! -f "$filename" ]
 then
         echo "password file $filename is not found."
         exit 1
 fi
 
password=`/opt/app/dmd/bin/keystorepasswordencrypt.sh < /opt/app/aafcertman/.password 2>/dev/null | sed -n 's/.*ObfuscatedKeyStorePassword://p'`

# to remove the whitespace in front of the password
password=`echo $password`

sed -i 's/DataRouterKeyStorePassword=OBF:/'DataRouterKeyStorePassword=OBF:"$password"'/g' /opt/app/dmd/config/dmd.properties

sed -i 's/DataRouterKeyPassword=OBF:/'DataRouterKeyPassword=OBF:"$password"'/g' /opt/app/dmd/config/dmd.properties

echo "$0 started at $(date)"