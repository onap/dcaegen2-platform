#!/bin/bash
# ================================================================================
# Copyright (c) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

#
#	This script sets up the internal certificate authority as a
#	recognized certificate authority.
#
#	This script must be run as root.
#
#	Works on both CentOS and Ubuntu.
#
set -x
cat >/tmp/aafcacert.crt <<'!EOF'
-----BEGIN CERTIFICATE-----
MIIDgTCCAmmgAwIBAgIQVl7wWIou4rBL8vktveMCJDANBgkqhkiG9w0BAQUFADBT
MQswCQYDVQQGEwJVUzEMMAoGA1UEChMDQVRUMQwwCgYDVQQLEwNDU08xKDAmBgNV
BAMTH0FUVCBFbnRlcnByaXNlIEludGVybmFsIFJvb3QgQ0EwHhcNMTMxMTIxMDM1
ODI1WhcNMzgxMTIxMDQwODI1WjBTMQswCQYDVQQGEwJVUzEMMAoGA1UEChMDQVRU
MQwwCgYDVQQLEwNDU08xKDAmBgNVBAMTH0FUVCBFbnRlcnByaXNlIEludGVybmFs
IFJvb3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCajoYMopUS
RBjFTOfgZN4Y0n5cIWNHTQsV1pAJh7k1mOmpT9aEIXzAtUbF4epFp/YJWax79eex
ODqNHAEb29IcHMGXINYU1Fm+FkmZFR/IO0EnG51j2g23lVIXIuNyyj79EnIfBehp
E38lLXcU2N5fIQBB5GIxJU+iAmN1qzZ1aPg5pwKPw2rJPdTZL8pXeEf5rFw7njEA
oAxwwyFqOarx5M9LCvZiXw9hGDv2Jc2/94caP/ruWryasV2wWYH5ofdC572z8A44
oIDMGvAIcKYGHlRA4VjrsOK56E5PzbakBYxDtYpdL81H3FegkuDppCC0Xtltatdn
FaWhxMAiBHhVAgMBAAGjUTBPMAsGA1UdDwQEAwIBhjAPBgNVHRMBAf8EBTADAQH/
MB0GA1UdDgQWBBSZHo0oHLdK+j4K6CCoCpuk2wBs5TAQBgkrBgEEAYI3FQEEAwIB
ADANBgkqhkiG9w0BAQUFAAOCAQEAhvYjsL9yUSQwyFMSOOXrvt7P05gTdJTBL6CZ
vtV/BhCKyN6hoJnXrrSQK9xNRomK2fSC9orbReMSzQBec1w0u28T9K0dV9x5Sez2
Uf6ZJVUslvf5g9jSG9+aL/p2xqx67RGNbwO6xLt63Wd5J1SkUbMYhtTFL05T/c9w
jQMUC0y2eQ4sXPFoGNUpFGOSlZ0fG7ou5eOgW+Djd1fWciPvWYap4Ptmh3tCLeTc
logfyXeb8P/ZuV8R/euM2m+ZZqacLmsa6a4uwPGfDN2GkFebxc9fsuNXqTJM2qPl
kLh7y6LPDDkcmtzByDmGHKNcNvbuZjQ1WNLwcI4Ucg/UyWfC1Q==
-----END CERTIFICATE-----
!EOF
chmod 444 /tmp/aafcacert.crt
if [ -f /etc/redhat-release ]
then
	mv /tmp/aafcacert.crt /etc/pki/ca-trust/source/anchors/aafcacert.pem
	update-ca-trust
else
	mv /tmp/aafcacert.crt /usr/local/share/ca-certificates/aafcacert.crt
	update-ca-certificates
fi
