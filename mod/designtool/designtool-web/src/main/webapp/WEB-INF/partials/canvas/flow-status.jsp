<%--
 Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  Modifications to the original nifi code for the ONAP project are made
  available under the Apache License, Version 2.0
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<div id="flow-status" flex layout="row" layout-align="space-between center">
      <div id="flow-status-container" layout="row" layout-align="space-around center">
      </div>

      <div layout="row" layout-align="end center">
          <div id="search-container">
              <button id="search-button" ng-click="appCtrl.serviceProvider.headerCtrl.flowStatusCtrl.search.toggleSearchField();"><i class="fa fa-search"></i></button>
              <input id="search-field" type="text" placeholder="Search"/>
          </div>
          <button id="bulletin-button"><i class="fa fa-sticky-note-o"></i></button>
      </div>
   </div>
<div id="search-flow-results"></div>
