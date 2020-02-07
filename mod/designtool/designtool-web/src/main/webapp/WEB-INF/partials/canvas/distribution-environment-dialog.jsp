<%--
================================================================================
Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
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
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<div id="distribution-environment-dialog" layout="column" class="hidden medium-dialog">
    <div class="dialog-content">
        <div class="setting">
            <div class="setting-name">Name</div>
            <div class="setting-field">
                <span id="distribution-environment-id" class="hidden"></span>
                <input type="text" id="distribution-environment-name" class="setting-input"/>
            </div>
        </div>
        <div class="setting">
            <div class="setting-name">Runtime API URL</div>
            <div class="setting-field">
                <input type="text" id="distribution-environment-location" class="setting-input" placeholder="http://runtime-host:port"/>
            </div>
        </div>
        <div class="setting">
            <div class="setting-name">Description</div>
            <div class="setting-field">
                <textarea id="distribution-environment-description" class="setting-input"></textarea>
            </div>
        </div>

    </div>
</div>
