#!/bin/ksh

set -o allexport
source /opt/app/dti/bin/dti.cfg
set +o allexport

echo $PGSERVERNAME

if [ $DCAE_ENV -eq "D2" ]
then
   print "Since it is an incremental installation.. it assumes psql client is already installed and skipping the steps to install psql client"
fi

hostname=`hostname`
if [ $hostname = $PRIMARY_VM_NAME ]
then
	print "VM is primary"
	print "Creating DB Objects..."
	cd $DTI/config
    cat 1802_changes.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

 	cat sql/view/rt_views.sql  | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

 	print "Creating DB Objects completed."
fi
print "FINISHED install18.02.sh"