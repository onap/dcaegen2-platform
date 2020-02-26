#!/bin/bash
# ============LICENSE_START=====================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ==============================================================================
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
# ============LICENSE_END=======================================================

set -eufx -o pipefail

PATCH_BINARY=/tmp/patches/designtool-web-$1.war
NIFI_VERSION=$2
PATCHES=/tmp/patches
TARGETS=/tmp/targets
mkdir -p $PATCHES $TARGETS
# extract patches
cd $PATCHES
jar xf $PATCH_BINARY
rm $PATCH_BINARY
# extract jars and wars to be patched
cd $TARGETS
jar xf $NIFI_BASE_DIR/nifi-current/lib/nifi-framework-nar-$NIFI_VERSION.nar \
    META-INF/bundled-dependencies/nifi-framework-nar-loading-utils-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-jetty-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-web-api-$NIFI_VERSION.war \
    META-INF/bundled-dependencies/nifi-web-ui-$NIFI_VERSION.war
# patch jar files
cd $PATCHES/WEB-INF/classes
set +f
jar uf $TARGETS/META-INF/bundled-dependencies/nifi-jetty-$NIFI_VERSION.jar \
    org/apache/nifi/web/server/JettyServer*.class
jar uf $NIFI_BASE_DIR/nifi-current/lib/nifi-properties-$NIFI_VERSION.jar \
    org/apache/nifi/util/NiFiProperties*.class
jar uf $NIFI_BASE_DIR/nifi-toolkit-current/lib/nifi-framework-core-api-$NIFI_VERSION.jar \
    org/apache/nifi/controller/AbstractPort*.class
jar uf $TARGETS/META-INF/bundled-dependencies/nifi-framework-nar-loading-utils-$NIFI_VERSION.jar \
    org/apache/nifi/nar/DCAEClassLoaders*.class \
    org/apache/nifi/nar/DCAEAutoLoader*.class
# patch war files
cd $PATCHES
jar uf $TARGETS/META-INF/bundled-dependencies/nifi-web-api-$NIFI_VERSION.war \
    WEB-INF/classes/org/apache/nifi/web/api/dto/DtoFactory*.class \
    WEB-INF/classes/org/apache/nifi/web/dao/impl/StandardConnectionDAO*.class
set -f
jar xf $TARGETS/META-INF/bundled-dependencies/nifi-web-ui-$NIFI_VERSION.war \
    css/nf-canvas-all.css \
    js/nf/canvas/nf-canvas-all.js \
    js/nf/summary/nf-summary-all.js
rm -f \
    css/nf-canvas-all.css.gz \
    js/nf/canvas/nf-canvas-all.js.gz \
    js/nf/summary/nf-summary-all.js.gz
sed -i \
    -e '/graph-controls/{r navigation-min.css' -e 'd}' \
    css/nf-canvas-all.css
sed -i \
    -e '/process-group-up-to-date/{r nf-process-group-min.js' -e 'd}' \
    -e '/div.available-relationship/{r nf-connection-configuration-min.js' -e 'd}' \
    -e '/nf.FlowVerison/{r nf-flow-version-min.js' -e 'd}' \
    -e '/controllerConfig/{r nf-settings-min.js' -e 'd}' \
    -e '/this.breadcrumbs/{r nf-ng-breadcrumbs-controller-min.js' -e 'd}' \
    -e '/Canvas.GlobalMenuCtrl=/{r nf-ng-canvas-global-menu-controller-min.js' -e 'd}' \
    -e '/processor-types-table/{r nf-ng-processor-component-min.js' -e 'd}' \
    js/nf/canvas/nf-canvas-all.js
sed -i \
    -e '/controllerConfig/{r nf-settings-min.js' -e 'd}' \
    js/nf/summary/nf-summary-all.js
gzip -k \
    css/nf-canvas-all.css \
    js/nf/canvas/nf-canvas-all.js \
    js/nf/summary/nf-summary-all.js
jar uf $TARGETS/META-INF/bundled-dependencies/nifi-web-ui-$NIFI_VERSION.war \
    $(find WEB-INF/classes/org/apache/jsp/WEB_002dINF WEB-INF/pages WEB-INF/partials css js images fonts -type f -print)
# patch scripts
cp common.sh start.sh $NIFI_BASE_DIR/scripts/
# patch nar files
cd $TARGETS
cp $NIFI_BASE_DIR/nifi-toolkit-current/lib/nifi-framework-core-api-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-framework-core-api-$NIFI_VERSION.jar
jar uf $NIFI_BASE_DIR/nifi-current/lib/nifi-framework-nar-$NIFI_VERSION.nar \
    META-INF/bundled-dependencies/nifi-framework-core-api-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-framework-nar-loading-utils-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-jetty-$NIFI_VERSION.jar \
    META-INF/bundled-dependencies/nifi-web-api-$NIFI_VERSION.war \
    META-INF/bundled-dependencies/nifi-web-ui-$NIFI_VERSION.war
cp $NIFI_BASE_DIR/nifi-current/lib/nifi-properties-$NIFI_VERSION.jar \
    $NIFI_BASE_DIR/nifi-toolkit-current/lib/nifi-properties-$NIFI_VERSION.jar
echo Success
exit 0
