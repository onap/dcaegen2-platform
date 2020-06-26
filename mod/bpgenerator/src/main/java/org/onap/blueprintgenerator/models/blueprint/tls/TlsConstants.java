/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia Intellectual Property. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.models.blueprint.tls;

public class TlsConstants {

    public static final String EXTERNAL_CERT_DIRECTORY_FIELD = "external_cert_directory";
    public static final String CERT_DIRECTORY_FIELD = "cert_directory";
    public static final String INPUT_PREFIX = "external_cert_";
    public static final String USE_EXTERNAL_TLS_FIELD = "use_external_tls";
    public static final String CA_NAME_FIELD = "ca_name";
    public static final String EXTERNAL_CERTIFICATE_PARAMETERS_FIELD = "external_certificate_parameters";
    public static final String COMMON_NAME_FIELD = "common_name";
    public static final String SANS_FIELD = "sans";
    public static final String CERT_TYPE_FIELD = "cert_type";

    public static final String DEFAULT_CA = "RA";
    public static final Object DEFAULT_CERT_TYPE = "P12";
    public static final String DEFAULT_COMMON_NAME = "sample.onap.org";
    public static final String DEFAULT_SANS = "sample.onap.org:component.sample.onap.org";
}
