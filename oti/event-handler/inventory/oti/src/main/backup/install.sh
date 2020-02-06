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
	print "Creating DB Objects ..."
	cd $DTI/config
 	cat sql/createschema.sql    | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	cat sql/granttables.sql     | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	cat sql/table/rt_tables.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	cat sql/view/rt_views.sql  | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	print "Creating DB Objects completed."
 	
	print "Running startAAIFullSync.sh ..."
	export datevar=`date +\%Y\%m\%d`;bash /opt/app/dti/bin/startAAIFullSync.sh  >> /opt/logs/DCAE/dti/startAAIFullSync.$datevar.log 2>&1
	print "Finished running startAAIFullSync.sh."
	print "Changing file ownership and permissions for logs folder and temp_config folder ..."
	chown -R dti:dcaedti /opt/logs/DCAE/dti
	chmod -R 755 /opt/logs/DCAE/dti
	chown -R dti:dcaedti /opt/app/dti/temp_config
	chmod -R 755 /opt/app/dti/temp_config
	print "Enabling cron ... "	
	crontab -l -u dti | sed "/^#.*/s/^#//" | crontab -u dti - 
fi
fi
print "FINISHED install.sh"
