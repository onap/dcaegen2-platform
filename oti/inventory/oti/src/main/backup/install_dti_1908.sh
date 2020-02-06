#!/bin/bash

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

cd $DTI/config
echo "Creating DB Objects ..."
cat sql/createschema.sql    | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
cat sql/granttables.sql     | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
echo "Creating database Tables ..."
cat sql/table/rt_tables.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
echo "Creating database views ..."
cat sql/view/rt_views.sql  | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
echo "Creating vnodelist tables ..."
cat sql/table/vnodelist_tables.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
echo "Loading data to DB Objects..."
PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.vm_vnfc_map FROM '/opt/app/vcc/config/table_export_vm_vnfc_map_csv.txt' delimiter ',' csv;
OMG

PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.vm_fc_vnfc_fc_map FROM '/opt/app/vcc/config/table_export_vm_vnfc_vnfc_fc_map_csv.txt' delimiter ',' csv;
OMG

PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti <<OMG
\COPY dti.networkelementattributes FROM '/opt/app/vcc/config/table_export_nea_csv.txt' delimiter ',' csv;
OMG

echo "Creating DB Objects completed."
echo "FINISHED install_dti_1908.sh"
