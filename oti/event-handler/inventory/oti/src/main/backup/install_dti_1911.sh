#!/bin/bash

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

cd $DTI/config
echo "`date` This is to update the DB schema for 19.11 changes ..."

#echo "`date` 1. Creating DB Objects ..."
#cat sql/createschema.sql    | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti
#cat sql/granttables.sql     | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

echo "`date` Creating database Tables ..."

cat sql/table/1911_changes.sql | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti

echo "`date` Creating DB Objects completed."
echo "`date` FINISHED install_dti_1911.sh"
