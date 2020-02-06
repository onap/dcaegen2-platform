#!/bin/ksh

set -o allexport
source /opt/app/dti/bin/dti.cfg
set +o allexport

echo $PGSERVERNAME

cp /opt/app/dti/config/dmaap.conf /opt/app/dti/config/dmaap.conf.bkp
cp /etc/dcae/dmaap.conf /opt/app/dti/config/dmaap.conf

if [ $DCAE_ENV -eq "D2" ]
then
   print "This is an incremental installation to install vnodelist only for 17.10 release. It assumes psql client is already installed and skipping the steps to install psql client"
fi

print "Start install_vnodelist_1710.sh..."

hostname=`hostname`
if [ $hostname = $PRIMARY_VM_NAME ]
then
	print "VM is primary"
	print "Creating vNodeList DB Objects..."
	cd $DTI/config
        cat sql/table/vnodelist_tables.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
 	print "Creating DB Objects completed."

	print "Loading data to DB Objects..."
PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.vm_vnfc_map FROM '/opt/app/dti/config/table_export_vm_vnfc_map_csv.txt' delimiter ',' csv;
OMG

PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.vm_fc_vnfc_fc_map FROM '/opt/app/dti/config/table_export_vm_vnfc_vnfc_fc_map_csv.txt' delimiter ',' csv;
OMG

PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.networkelementattributes FROM '/opt/app/dti/config/table_export_nea_csv.txt' delimiter ',' csv;
OMG

	print "Loading data to DB Objects completed"
fi

print "FINISHED install_vnodelist_1710.sh"

