#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

#Script Name: KeystoreMonitoring.sh
#
#This script accepts optional arguments for FilePath, dti.cfg file path, .password file path and Keystore File Path
#This script checks if any changes have been made to the keystore.jks file
#Verifies if the password stored in .password and dti.cfg are same
#Checks the validity of the keystore

#Define Local Variables used in this script
fileFlag=""
dtiFilePathFlag=""
passwordFilePathFlag=""
keyStorePathFlag=""
passwordStatus=""
keystoreStatus=""
currentDate=`date +%Y%m%d`
logDate=`date +%Y%m%d-%H:%M:%S`
reportDate=`date +%Y%m%d%H%M%S`
LOG=/opt/app/vcc/logs/DCAE/dti/keystore_monitoring_$currentDate.log
echo "KeystoreMonitoring Log at: $LOG"

#Get Optional Arguments as parameters for the script
while getopts "f:F:d:D:p:P:k:K" option;
do
    case $option
        in
        f|F) fileFlag=${OPTARG}
        ;;
        d|D) dtiFilePathFlag=${OPTARG}
        ;;
        p|P) passwordFilePathFlag=${OPTARG}
        ;;
        k|K) keyStorePathFlag=${OPTARG}
        ;;
        \? ) echo -e "\n Usage: ./KeystoreMonitoring.sh (Optional) [-f] [-d] [-p] [-k]"
             echo -e "\n\t Optional Parameters Used are:"
             echo -e "\t\t -f|F: Complete File Path for verification of any changes to the file"
             echo -e "\t\t -d|D: Complete dti.cfg File Path for password verification"
             echo -e "\t\t -p|P: Complete .password File Path for password verification"
             echo -e "\t\t -k|K: Complete Keystore File Path for checking the validity \n"
             exit 1
             ;;
   esac
done

#Define Default Values for the variables used
file="/opt/app/aafcertman/keystore.jks"
fingerPrintFile="/opt/app/aafcertman/keystoreFingerPrint"
dtiFilePath="/opt/app/vcc/bin/dti.cfg"
passwordFilePath="/opt/app/aafcertman/.password"
keyStorePath="/opt/app/aafcertman/keystore.jks"
currentDateInSeconds=`date +%s`

#Check if no Optional Argument given, then take the default value
#File Path Initialization
if [ -z $fileFlag ]
then
    file=$file
else
    file=$fileFlag
fi

#DTI File Path initialization
if [ -z $dtiFilePathFlag ]
then
    dtiFilePath=$dtiFilePath
else
    dtiFilePath=$dtiFilePathFlag
fi

#Password File Path initialization
if [ -z $passwordFilePathFlag ]
then
    passwordFilePath=$passwordFilePath
else
    passwordFilePath=$passwordFilePathFlag
fi

#Keystore Path initialization
if [ -z $keyStorePathFlag ]
then
    keyStorePath=$keyStorePath
else
    keyStorePath=$keyStorePathFlag
fi
currentDateInSeconds=`date +%s`
if [ ! -f $file ]
    then
        echo "$logDate: ERROR: $file does not exist - aborting" >> $LOG
        exit 1
fi

# Create and print the md5Sum result of the file
filemd5=`md5sum $file | cut -d " " -f1`
echo "$logDate: File: $file - md5Sum result: $filemd5" >> $LOG

# Check if the md5Sum result of the file is empty or not
if [ -z $filemd5 ]
    then
        echo "$logDate: The md5sum for $file is empty " >> $LOG
        exit 1
    else
        # Do nothing
        :
fi

# Check if fingerPrintFile is already present
if [ -f $fingerPrintFile ]
then
    savedmd5=`cat $fingerPrintFile`
    echo "$logDate: FingerPrintFile: $fingerPrintFile - md5Sum result: $savedmd5" >> $LOG
    # Check if the md5Sum result of the fingerPrintFile is empty or not
    if [ -z $savedmd5 ]
    then
        echo "$logDate: The savedmd5Sum in $fingerPrintFile  is empty " >> $LOG
        #Compare the md5Sum results of File and FingerPrintFile
    else if [ "$savedmd5" = "$filemd5" ]
        then
            echo "$logDate: No changes in the file observed" >> $LOG
        else
            echo "$logDate: [WARNING] There are changes in the file: $file" >> $LOG
        fi
    fi
else
    cat $fingerPrintFile > $fingerPrintFile
    echo "$logDate: $fingerPrintFile Not found. Storing the md5sum in a new file" >> $LOG
fi

# Save the current md5Sum
echo $filemd5 > $fingerPrintFile
echo "$logDate: Updating the fingerPrintFile with current file md5Sum" >> $LOG

#Extracting the keystore password from dti.cfg file
source $dtiFilePath
keystorePassword=$KEYSTORE_PASSWORD
echo "$logDate: Keystore Password from $dtiFilePath file: $keystorePassword" >> $LOG

#Extracting the keystore password from .password file
password="$(cat $passwordFilePath)"
echo "$logDate: Keystore Password from $passwordFilePath file: $password" >> $LOG

#Compare if the Keystore password in dti.cfg and .password are equal
if [ "$keystorePassword" = "$password" ]
then
    echo "$logDate: Both the keystore passwords in $dtiFilePath and $passwordFilePath file are same" >> $LOG
    passwordStatus="pass"
else
    echo "$logDate: Password mismatch in $dtiFilePath and $passwordFilePath files" >> $LOG
    passwordStatus="fail"
fi

#Get the keystore certificate validity
validity=`keytool -list -v -keystore $keyStorePath -storepass $keystorePassword | grep 'Valid from' |head -1`
validity=${validity#*"until: "}
echo "$logDate: Validity of the keystore certificate: $validity" >> $LOG
monthName="${validity:4:3}"
month=""
case $monthName in
        Jan)
        month="01"
        ;;
        Feb)
        month="02"
        ;;
        Mar)
        month="03"
        ;;
        Apr)
        month="04"
        ;;
        May)
        month="05"
        ;;
        Jun)
        month="06"
        ;;
        Jul)
        month="07"
        ;;
        Aug)
        month="08"
        ;;
        Sep)
        month="09"
        ;;
        Oct)
        month="10"
        ;;
        Nov)
        month="11"
        ;;
        Dec)
        month="12"
        ;;
esac
dateNumber="${validity:8:2}"
year="${validity:24:4}"
timePeriod=${validity:11:8}
validDate="$year.$month.$dateNumber-$timePeriod"
echo "$logDate: Converting the Keystore validity into Busybox date format: $validDate" >> $LOG
validityInSeconds=`date -d "$validDate" +%s`
remainingDays=$(( ($validityInSeconds - $currentDateInSeconds) / 60 / 60 / 24 ))
if [ $currentDateInSeconds -le $validityInSeconds ]; then
    echo "$logDate: [OK]      Certificate $ALIAS expires in '$validity' ($remainingDays day(s) remaining)." >> $LOG
    keystoreStatus="pass"
elif [ $remainingDays -le 0 ]; then
    echo "$logDate: [CRITICAL] Ceritficate has expired! $((-$remainingDays)) day(s) past due)" >> $LOG
    keystoreStatus="fail"
else
    echo "$logDate: [WARNING] Certificate $ALIAS expires in '$validity' ($remainingDays day(s) remaining)." >> $LOG
    keystoreStatus="pass"
fi

set -o allexport
source /opt/app/vcc/bin/dti.cfg
set +o allexport

#Check PostGres DB Connecitvity and Volume Analysis for VCC Monitoring
dbStatus=""
echo "$logDate: Postgres database monitoring started" >>$LOG
count=`echo "select count(*) from dti.rt_vserver where (to_date(updated_on,'YYYYMMDDHH24MISS') > (CURRENT_DATE - INTERVAL '30 days'));" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
vserverCount=`echo "select count(*) from dti.narad_pnf;" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
genericVnfCount=`echo "select count(*) from dti.rt_generic_vnf where (to_date(updated_on,'YYYYMMDDHH24MISS') > (CURRENT_DATE - INTERVAL '30 days'));" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
vnfcCount=`echo "select count(*) from dti.rt_vnfc where (to_date(updated_on,'YYYYMMDDHH24MISS') > (CURRENT_DATE - INTERVAL '30 days'));" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
dcaeEventCount=`echo "select count(*) from dti.dcae_event where (to_date(updated_on,'YYYYMMDDHH24MISS') > (CURRENT_DATE - INTERVAL '30 days'));" | PGPASSWORD=$PGADMINPASSWORD psql -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -t 2>>$LOG`
if [ $count -eq $count 2>/dev/null ];then
        if [ $count -eq "0" ];then
                echo "$logDate: PSQL DB Connection is successful" >>$LOG
                dbStatus="fail"
                echo "$logDate: No data is present inside dti.narad_pnf table" >>$LOG
        elif [ $count -gt "0" ];then
                echo "$logDate: PSQL DB Connection is successful" >>$LOG
                dbStatus="pass"
                echo "$logDate: $count data is present inside dti.narad_pnf table" >>$LOG
        else
                echo "Error with Database Connection. Please check log for more details"
        fi
fi

#Get Distinct Cloud Region Id's for VCC Monitoring
cloudRegionIdFile=/tmp/VCCMON_${reportDate}_dti-cloud-region-ids.txt
cloudRegionIds=(`PGPASSWORD=$PGADMINPASSWORD psql -t -h $PGSERVERNAME -U $PGADMINUSERNAME -d dti -c "select distinct(cloud_region_id) from dti.rt_vserver;"`)
for i in "${cloudRegionIds[@]}"
do
   echo "$i" >> $cloudRegionIdFile
done
echo "$logDate: Successfully generated cloudRegionIdFile" >>$LOG

# Creation of JSON Output file to publish for VCC Monitoring
outputFile=/tmp/VCCMON_${reportDate}_dti-connectivity-check_status.json
echo "{\"PasswordMonitoring\":{" >> $outputFile
echo "\"item\":\"PasswordMonitoring\"," >> $outputFile
echo "\"status\":\"$passwordStatus\"," >> $outputFile
echo "\"verification_date_time\":\"$reportDate\"" >> $outputFile
echo "}," >> $outputFile
echo "\"DatabaseMonitoring\":{" >> $outputFile
echo "\"item\":\"DatabaseMonitoring\"," >> $outputFile
echo "\"status\":\"$dbStatus\"," >> $outputFile
echo "\"verification_date_time\":\"$reportDate\"" >> $outputFile
echo "}," >> $outputFile
echo "\"KeystoreMonitoring\":{" >> $outputFile
echo "\"item\":\"KeystoreMonitoring\"," >> $outputFile
echo "\"status\":\"$keystoreStatus\"," >> $outputFile
echo "\"remainingDays\":\"$remainingDays\"," >> $outputFile
echo "\"verification_date_time\":\"$reportDate\"" >> $outputFile
echo "}," >> $outputFile
echo "\"VolumeAnalysis\":{" >> $outputFile
echo "\"item\":\"VolumeAnalysis\"," >> $outputFile
echo "\"vserverCount\":\"$vserverCount\"," >> $outputFile
echo "\"genericVnfCount\":\"$genericVnfCount\"," >> $outputFile
echo "\"vnfcCount\":\"$vnfcCount\"," >> $outputFile
echo "\"dcaeEventCount\":\"$dcaeEventCount\"," >> $outputFile
echo "\"verification_date_time\":\"$reportDate\"" >> $outputFile
echo "}}" >> $outputFile
echo "$logDate: Successfully generated VCCMON_${reportDate}_dti-connectivity-check_status.json" >>$LOG

#Move the Output Files for publishing 
mv $cloudRegionIdFile /opt/app/vcc/data/input/publisher/VCC_MONITORING_DATA/
mv $outputFile /opt/app/vcc/data/input/publisher/VCC_MONITORING_DATA/
echo "Script executed successfully"
echo "$logDate: Completed Successfully" >> $LOG
