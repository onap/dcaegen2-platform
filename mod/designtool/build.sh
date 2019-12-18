# ============LICENSE_START=======================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
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
#!/bin/bash

echo "SCRIPT EXECUTION STARTS"
# This script is meant to be used in the CI jobs where its assumed that this
# project is checked out fresh each time. This script will "setup" the source
# code before kicking off the build process which means grabbing the working
# files for the submodule and applying the design tool patch file.

# Set working directory to where this script lives
echo "SETTING WORKING DIRECTORY"
SCRIPT_DIR=$(dirname $0)
cd $SCRIPT_DIR

# Setup the source code
echo "SETTING UP CODE"
cd ../../
git submodule update --init
cd $SCRIPT_DIR
cd nifi
echo "APPLYING PATCH"
git am ../design-tool-changes.patch

# Build using Maven
echo "STARTING MAVEN BUILD"
mvn -T 2.0C clean install -DskipTests

# TODO: Should only run the below if the maven build succeeded
# Build Docker image
echo "BUILDING DOCKER IMAGE"
NIFI_VERSION_DEFAULT=1.9.3-SNAPSHOT
NIFI_VERSION=${NIFI_VERSION:-$NIFI_VERSION_DEFAULT}
cd nifi-docker/dockermaven
mkdir target
# TODO: Would creating a link here work instead of copying?
cp ../../nifi-assembly/target/nifi-$NIFI_VERSION-bin.zip target/
cp ../../nifi-toolkit/nifi-toolkit-assembly/target/nifi-toolkit-$NIFI_VERSION-bin.zip target/
docker build --build-arg NIFI_BINARY=./target/nifi-$NIFI_VERSION-bin.zip \
    --build-arg NIFI_TOOLKIT_BINARY=./target/nifi-toolkit-$NIFI_VERSION-bin.zip \
    --build-arg NIFI_VERSION=$NIFI_VERSION \
    -t nifi-mod .
    
echo "SCRIPT EXECUTION ENDS"
