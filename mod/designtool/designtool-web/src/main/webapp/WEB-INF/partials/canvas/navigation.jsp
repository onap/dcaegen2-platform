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
<div id="import-model" class="hidden medium-dialog import-group">
  <div id="import-model-header-text" class="import-header-text">Import Model</div>
  <div class="import-container">
    <select id="model-type" onchange="setModelType()">
      <option value="-" selected="true">-- Choose Model Type --</option>
      <option value="mtAcumos">Acumos</option>
    </select>
    <div id="mt-acumos" class="hidden">
      <hr/>
      <label for="furl"><b>Acumos Federation URL</b></label> <input id="furl" type="text" name="furl" placeholder="https://server:9084" required/>
      <button type="button" class="onap-action" onclick="lookupCatalogs()">Lookup</button>
    </div>
    <div id="c-acumos" class="hidden">
      <hr/>
      <label for="cat-menu"><b>Select Catalog</b></label> <select id="cat-menu" onchange="chooseCatalog()">
        <option value="*">All Catalogs</option>
      </select>
    </div>
    <div id="ac-sols" class="hidden">
      <hr/>
      <label for="sol-menu"><b>Select Solution</b></label> <select id="sol-menu" onchange="chooseSolution()">
        <option value="*">All Solutions</option>
      </select>
    </div>
    <div id="ac-revs" class="hidden">
      <hr/>
      <label for="rev-menu"><b>Select Revision</b></label> <select id="rev-menu">
        <option value="*">All Revisions</option>
      </select>
    </div>
    <hr/>
    <button id="onboard" class="hidden onap-action" type="button" onclick="onBoard()">Onboard</button><br>
    <b id="onboarding-in-progress" class="hidden">Onboarding - Please Wait ...</b>
  </div>
</div>

<div id="import-component" class="hidden medium-dialog import-group">
  <div id="import-component-header-text" class="import-header-text">Import Component Specification</div>
  <div class="import-container">
    <label for="cspec"><b>Component Specification File</b></label> <input id="cspec" type="file" name="file" placeholder="Component-Spec.json" accept=".json,application/json" required>
    <br><button id="uploadComponent" type="button" class="onap-action" onclick="onBoardComponent()">Upload</button>
  </div>
</div>
<div id="import-data-format" class="hidden medium-dialog import-group">
  <div id="import-data-format-header-text" class="import-header-text">Import Data Format</div>
  <div class="import-container">
    <label for="dfspec"><b>Data Format File</b></label> <input id="dfspec" type="file" name="file" placeholder="Data-Format.json" accept=".json,application/json" required>
    <br><button id="uploadDataFormat" type="button" class="onap-action" onclick="onBoardDataFormat()">Upload</button>
  </div>
</div>
