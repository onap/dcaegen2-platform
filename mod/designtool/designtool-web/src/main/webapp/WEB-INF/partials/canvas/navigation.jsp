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
<nf-breadcrumbs
        breadcrumbs="appCtrl.serviceProvider.breadcrumbsCtrl.getBreadcrumbs();"
        click-func="appCtrl.nf.CanvasUtils.getComponentByType('ProcessGroup').enterGroup"
        highlight-crumb-id="appCtrl.nf.CanvasUtils.getGroupId();"
        separator-func="appCtrl.nf.Common.isDefinedAndNotNull"
        is-tracking="appCtrl.serviceProvider.breadcrumbsCtrl.isTracking"
        get-version-control-class="appCtrl.serviceProvider.breadcrumbsCtrl.getVersionControlClass"
        get-version-control-tooltip="appCtrl.serviceProvider.breadcrumbsCtrl.getVersionControlTooltip">
</nf-breadcrumbs>
<div id="graph-controls">
    <div id="navigation-control" class="graph-control">
        <div class="graph-control-docked pointer fa fa-compass" title="Navigate"
             ng-click="appCtrl.serviceProvider.graphControlsCtrl.undock($event)">
        </div>
        <div class="graph-control-header-container hidden pointer"
             ng-click="appCtrl.serviceProvider.graphControlsCtrl.expand($event)">
            <div class="graph-control-header-icon fa fa-compass">
            </div>
            <div class="graph-control-header">Navigate</div>
            <div class="graph-control-header-action">
                <div class="graph-control-expansion fa fa-plus-square-o pointer"></div>
            </div>
            <div class="clear"></div>
        </div>
        <div class="graph-control-content hidden">
            <div id="navigation-buttons">
                <div id="naviagte-zoom-in" class="action-button" title="Zoom In"
                     ng-click="appCtrl.serviceProvider.graphControlsCtrl.navigateCtrl.zoomIn();">
                    <button><div class="graph-control-action-icon fa fa-search-plus"></div></button>
                </div>
                <div class="button-spacer-small">&nbsp;</div>
                <div id="naviagte-zoom-out" class="action-button" title="Zoom Out"
                     ng-click="appCtrl.serviceProvider.graphControlsCtrl.navigateCtrl.zoomOut();">
                    <button><div class="graph-control-action-icon fa fa-search-minus"></div></button>
                </div>
                <div class="button-spacer-large">&nbsp;</div>
                <div id="naviagte-zoom-fit" class="action-button" title="Fit"
                     ng-click="appCtrl.serviceProvider.graphControlsCtrl.navigateCtrl.zoomFit();">
                    <button><div class="graph-control-action-icon icon icon-zoom-fit"></div></button>
                </div>
                <div class="button-spacer-small">&nbsp;</div>
                <div id="naviagte-zoom-actual-size" class="action-button" title="Actual"
                     ng-click="appCtrl.serviceProvider.graphControlsCtrl.navigateCtrl.zoomActualSize();">
                    <button><div class="graph-control-action-icon icon icon-zoom-actual"></div></button>
                </div>
                <div class="clear"></div>
            </div>
            <div id="birdseye"></div>
        </div>
    </div>
    <div id="operation-control" class="graph-control">
        <div class="graph-control-docked pointer fa fa-hand-o-up" title="Operate"
             ng-click="appCtrl.serviceProvider.graphControlsCtrl.undock($event)">
        </div>
        <div class="graph-control-header-container hidden pointer"
             ng-click="appCtrl.serviceProvider.graphControlsCtrl.expand($event)">
            <div class="graph-control-header-icon fa fa-hand-o-up">
            </div>
            <div class="graph-control-header">Operate</div>
            <div class="graph-control-header-action">
                <div class="graph-control-expansion fa fa-plus-square-o pointer"></div>
            </div>
            <div class="clear"></div>
        </div>
        <div class="graph-control-content hidden">
            <div id="operation-context">
                <div id="operation-context-logo">
                    <i class="icon" ng-class="appCtrl.serviceProvider.graphControlsCtrl.getContextIcon()"></i>
                </div>
                <div id="operation-context-details-container">
                    <div id="operation-context-name"><strong> {{appCtrl.serviceProvider.graphControlsCtrl.getContextName()}} </strong></div>
                    <div id="operation-context-type" ng-class="appCtrl.serviceProvider.graphControlsCtrl.hide()">{{appCtrl.serviceProvider.graphControlsCtrl.getContextType()}}</div>
                </div>
                <div class="clear"></div>
                <div id="operation-context-id" ng-class="appCtrl.serviceProvider.graphControlsCtrl.hide()">{{appCtrl.serviceProvider.graphControlsCtrl.getContextId()}}</div>
            </div>            <div id="operation-buttons">
                <div>

                  <div id="operation-context-type">Distribute for deployment:</div>
                  <br>
                  <div>
                    <select name="environment" id="environmentType" class="combo" onchange="onEnvironmentSelect()">
                      <option class="combo-option-text" disabled selected>Select Environment</option>
                    </select>
                  </div>

                    <br>
                   <div class="button-spacer-large">&nbsp;</div>
                   <div id="operate-refresh" class="action-button" title="Refresh Environments">
                        <button id="refresh-env-btn" onclick="refreshEnvironments()" >
                        <div class="graph-control-action-icon fa fa-refresh"></div><span></span></button>
                    </div>
                    <div class="button-spacer-large">&nbsp;</div>
                    <div id="operate-delete" class="action-button" title="Delete">
                        <button ng-click="appCtrl.nf.Actions['delete'](appCtrl.nf.CanvasUtils.getSelection());"
                                ng-disabled="!appCtrl.nf.CanvasUtils.areDeletable(appCtrl.nf.CanvasUtils.getSelection());">
                            <div class="graph-control-action-icon fa fa-trash"></div><span></span></button>
                    </div>
                     <div class="button-spacer-large">&nbsp;</div>
                     <div id="operate-submit" class="action-button" title="Submit">
                            <button id="operate-submit-btn" onclick="distributeGraph()" >
                             <div class="graph-control-action-icon fa fa-check"></div><span></span></button>
                      </div>
                      <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
</div>
