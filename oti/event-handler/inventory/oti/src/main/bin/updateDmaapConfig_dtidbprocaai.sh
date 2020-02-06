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

set -o allexport
export SUBCOMP=dti

echo "Start to update dmaap.conf"

cd /opt/app/vcc/tmp/configManager/output/

password_list=`grep password KV_Config.json | grep -v export | sed -e "s/\"password\": //"`
echo $password_list

username_list=`grep username KV_Config.json | grep -v export | sed -e "s/\"username\": //"`
echo $username_list

publish_url_list=`grep publish_url KV_Config.json | sed -e "s/\"publish_url\": //"`
echo $publish_url_list

count=0
for i in $password_list
do
        count=$(($count+1))
	echo $i > /tmp/tmp_password_$count
done
password_1=`cat /tmp/tmp_password_1`
password_2=`cat /tmp/tmp_password_2`

count=0
for i in $username_list
do
        count=$(($count+1))
        echo $i > /tmp/tmp_username_$count
done
username_1=`cat /tmp/tmp_username_1`
username_2=`cat /tmp/tmp_username_2`

count=0
for i in $publish_url_list
do
        count=$(($count+1))
        echo $i > /tmp/tmp_publish_url_$count
done
publish_url_1=`cat /tmp/tmp_publish_url_1`
publish_url_2=`cat /tmp/tmp_publish_url_2`


echo "[{" > /tmp/tmp_dcae_conf
echo "  \"\": \"com.att.ecomp.dcae.controller.core.stream.DmaapStream\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapDataType\": \"file\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapAction\": \"publish\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapUrl\": $publish_url_1 " >> /tmp/tmp_dcae_conf
echo "  \"dmaapUserName\": $username_1" >> /tmp/tmp_dcae_conf
echo "  \"dmaapPassword\": $password_1" >> /tmp/tmp_dcae_conf
echo "  \"dmaapStreamId\": \"dti\"" >> /tmp/tmp_dcae_conf
echo "}," >> /tmp/tmp_dcae_conf

echo "{" >> /tmp/tmp_dcae_conf
echo "  \"\": \"com.att.ecomp.dcae.controller.core.stream.DmaapStream\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapDataType\": \"file\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapAction\": \"publish\"," >> /tmp/tmp_dcae_conf
echo "  \"dmaapUrl\": $publish_url_2, " >> /tmp/tmp_dcae_conf
echo "  \"dmaapUserName\": $username_2" >> /tmp/tmp_dcae_conf
echo "  \"dmaapPassword\": $password_2" >> /tmp/tmp_dcae_conf
echo "  \"dmaapStreamId\": \"voip_network_inv\"" >> /tmp/tmp_dcae_conf
echo "}]" >> /tmp/tmp_dcae_conf
echo "" >> /tmp/tmp_dcae_conf


cp /tmp/tmp_dcae_conf /opt/app/vcc/config/dmaap.conf
rm /tmp/tmp_*

echo "FINISHED updating dmaap.conf"

set +o allexport


