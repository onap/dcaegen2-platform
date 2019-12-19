# ================================================================================
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

import base64
import collections
import copy
import os

from Crypto import Random
from Crypto.Cipher import PKCS1_v1_5
from Crypto.Hash import SHA
from Crypto.PublicKey import RSA


def update_dict(d, u):
    """Recursively updates dict

    Update dict d with dict u
    """
    for k, v in u.items():
        if isinstance(v, collections.Mapping):
            r = update_dict(d.get(k, {}), v)
            d[k] = r
        else:
            d[k] = u[k]
    return d

def replace_token(configure_content):
    try:
        with open("/opt/app/config-map/dcae-k8s-cluster-token",'r') as fh:
            dcae_token = fh.readline().rstrip('\n')

        new_config = copy.deepcopy(configure_content)

        # override the default-user token 
        ix=0
        for user in new_config['users'][:]:
            if user['name'] == "default-user":
                new_config['users'][ix] = {
                    "name": "default-user",
                    "user": {
                        "token": dcae_token
                    }
                }
            ix += 1

        return new_config

    except Exception as e:
        return configure_content

def decrypt(b64_ciphertext):
    """returns decrypted b64_ciphertext that was encoded like this:

    echo "cleartext" | openssl pkeyutl -encrypt -pubin -inkey rsa.pub | base64 --wrap=0

    requires private key in environment variable EOMUSER_PRIVATE
    """

    if len(b64_ciphertext) <= 30:  # For transition, assume short values are not encrypted
        return b64_ciphertext
    
    try:
        ciphertext = base64.b64decode(b64_ciphertext)
        key = RSA.importKey(os.getenv('EOMUSER_PRIVATE'))
        cleartext = PKCS1_v1_5.new(key).decrypt(ciphertext, Random.new().read(15+SHA.digest_size))
    except Exception as e:
        return b64_ciphertext

    return cleartext
