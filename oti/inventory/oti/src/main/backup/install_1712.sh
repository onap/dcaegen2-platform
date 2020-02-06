#!/bin/ksh

set -o allexport
source /opt/app/dti/bin/dti.cfg
set +o allexport

if [ $DCAE_ENV != "D1" ]
then

hostname=`hostname`
#if [ $hostname = $PRIMARY_VM_NAME ]
if test "${hostname#*$PRIMARY_VM_NAME}" != "$hostname"
then
	print "VM is primary"
	print "Applying DB changes for 1712 for upgrading..."
	cd $DTI/config
 	cat sql/table/1712_changes.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	
fi
fi
print "FINISHED install.sh for 1712"
