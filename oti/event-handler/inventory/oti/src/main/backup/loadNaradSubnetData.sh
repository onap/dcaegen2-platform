#!/bin/bash

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport
$JAVA_HOME/bin/java -classpath /opt/app/vcc/config/:/opt/app/vcc/lib/:/opt/app/vcc/classes/:/opt/app/vcc/lib/dti-package-content-final.jar -Dlogback.configurationFile=$DTI/config/logger/logback_naraddb.xml -Dhttps.protocols=TLSv1.2 com.att.vcc.inventorycollector.InventoryCollector NARAD_INIT_SUBNETS https://narad-conexus-prod.ecomp.cci.att.com:8443/narad/v1/nodes/subnets?format=resource_and_url
