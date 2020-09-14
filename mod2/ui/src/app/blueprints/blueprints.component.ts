/* 
 *  # ============LICENSE_START=======================================================
 *  # Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  # ================================================================================
 *  # Licensed under the Apache License, Version 2.0 (the "License");
 *  # you may not use this file except in compliance with the License.
 *  # You may obtain a copy of the License at
 *  #
 *  #      http://www.apache.org/licenses/LICENSE-2.0
 *  #
 *  # Unless required by applicable law or agreed to in writing, software
 *  # distributed under the License is distributed on an "AS IS" BASIS,
 *  # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  # See the License for the specific language governing permissions and
 *  # limitations under the License.
 *  # ============LICENSE_END=========================================================
 */

import { Component, OnInit, ViewChild, ElementRef, Input, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { trigger, state, style, transition, animate } from '@angular/animations';
import * as saveAs from 'file-saver';
import * as JSZip from 'jszip';
import { AuthService } from '../services/auth.service';
import { DatePipe } from '@angular/common';
import { DeploymentArtifactService } from '../services/deployment-artifact.service';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { Toast } from 'primeng/toast'
import { ActivatedRoute } from '@angular/router';
import { DownloadService } from '../services/download.service';

@Component({
  selector: 'app-blueprints',
  templateUrl: './blueprints.component.html',
  styleUrls: ['./blueprints.component.css'],
  animations: [
    trigger('rowExpansionTrigger', [
      state('void', style({
        transform: 'translateX(-10%)',
        opacity: 0
      })),
      state('active', style({
        transform: 'translateX(0)',
        opacity: 1
      })),
      transition('* <=> *', animate('400ms cubic-bezier(0.86, 0, 0.07, 1)'))
    ])
  ],
  providers: [DatePipe, MessageService]
})
export class BlueprintsComponent implements OnInit {
  @ViewChild(Table, { static: false }) dt: Table;
  @ViewChild(Toast, { static: false }) toast: Toast;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. **/
  bpElements: BlueprintElement[] = [];
  cols: any[] = [
    { field: 'instanceName', header: 'Instance Name' },
    { field: 'instanceRelease', header: 'Instance Release', width: '7%' },
    { field: 'tag', header: 'Tag' },
    { field: 'type', header: 'Type', width: '7%' },
    { field: 'version', header: 'Version', width: '6%' },
    { field: 'status', header: 'Status', width: '125px' }];
  states: {field: string, label: string}[] = [];
  columns: any[];
  filteredRows: any;
  downloadItems: { label: string; command: () => void; }[];
  username: string;
  showBpContentDialog: boolean = false;
  selectedBPs: BlueprintElement[] = [];
  //  Hides the BP list until the rows are retrieved and filtered
  visible = "hidden";
  // These 2 fields are passed from MS Instance to filter the BP list
  tag: string;
  release: string;

  filteredName:    string;
  filteredRelease: string;
  filteredTag:     string;
  filteredType:    string;
  filteredVersion: string;
  filteredStatus:  string;

  constructor(private change: ChangeDetectorRef, private messageService: MessageService, private authService: AuthService,
              private datePipe: DatePipe, private bpApis: DeploymentArtifactService, private spinnerService: Ng4LoadingSpinnerService,
              private route: ActivatedRoute, private downloadService: DownloadService) { }

  ngOnInit() {
    
    this.username = this.authService.getUser().username;
    
    this.getStates();
    this.getAllBPs();

    this.change.markForCheck();

    this.route.queryParams.subscribe((params) => {
      this.filteredTag     = params['tag'];
      this.filteredRelease = params['release']});
  }

  //gets statuses for status updates
  getStates(){
    this.states = []
    this.bpApis.getStatuses().subscribe((response) => {this.setMenuStates(response)})
  }

  //fills actions menu with states
  setMenuStates(states){
    for(let item of states){
      this.states.push({
        field: item,
        label: 'To  ' + item
      })
    }
  }

  canDelete: boolean = false;
  canDownload: boolean = false;
  canUpdate: boolean = false;
  deleteTooltip: string;
  enableButtonCheck(){
    if(this.selectedBPs.length > 0){
      this.canDownload = true;
      this.canUpdate = true;
      
      for(let item of this.selectedBPs){
        if (item.status !== 'IN_DEV' && item.status !== 'NOT_NEEDED' && item.status !== 'DEV_COMPLETE'){
          this.canDelete = false;
          this.deleteTooltip = 'Only blueprints that are in a status of "In Dev", "Not Needed" or "Dev Complete" can be deleted'
          break
        } else {
          this.canDelete = true;
        }
      }

    } else {
      this.canDownload = false;
      this.canUpdate = false;
      this.canDelete = false;
      this.deleteTooltip = 'No Blueprints Selected'
    }
  }

  updateStateTo: string = ''; //selected state to update blueprint to
  //checks if there are different releases/statuses selected
  updateSelectedStatusesCheck(state){
    this.updateStateTo = state.field
    let multipleStates: boolean = false
    let multipleReleases: boolean = false
    let firstStatus = this.selectedBPs[0]['status']
    let firstRelease = this.selectedBPs[0]['instanceRelease']

    for(let bp of this.selectedBPs){
      if(bp.instanceRelease !== firstRelease){
        multipleReleases = true
      }
      if (bp.status !== firstStatus) {
        multipleStates = true
      }
    }

    if(multipleReleases && multipleStates){
      this.messageService.add({ key: 'confirmToast', sticky: true, severity: 'warn', summary: 'Are you sure?', detail: 'You are about to update blueprints for different releases and statuses. Confirm to proceed.' });
    } else if (multipleReleases && !multipleStates) {
      this.messageService.add({ key: 'confirmToast', sticky: true, severity: 'warn', summary: 'Are you sure?', detail: 'You are about to update blueprints for different releases. Confirm to proceed.' });
    } else if (!multipleReleases && multipleStates) {
      this.messageService.add({ key: 'confirmToast', sticky: true, severity: 'warn', summary: 'Are you sure?', detail: 'You are about to update blueprints for different statuses. Confirm to proceed.' });
    } else if (!multipleReleases && !multipleStates){
      this.updateSelectedStatuses()
    }
  }
  onConfirm() {
    this.messageService.clear('confirmToast')
    this.updateSelectedStatuses()
  }
  onReject() {
    this.messageService.clear('confirmToast')
  }

  /* * * * Update status for multiple blueprints * * * */
  successfulStatusUpdates: number = 0 //keeps track of how many status updates were successful
  selectionLength: number = 0 //length of array of blueprints with different statuses than update choice
  statusUpdateCount: number = 0 //keeps track of how many api calls have been made throughout a loop
  statusUpdateErrors: string[] = [] //keeps list of errors
  updateSelectedStatuses(){
    this.successfulStatusUpdates = 0      
    this.statusUpdateErrors = []          
    this.statusUpdateCount = 0

    let bpsToUpdate = this.selectedBPs.filter(bp => bp.status !== this.updateStateTo) //array of blueprints with different statuses than update choice
    this.selectionLength = bpsToUpdate.length;

    if (this.selectionLength === 0) { this.selectedBPs = [] } else {
      this.spinnerService.show();
      this.updateState(this.updateStateTo, bpsToUpdate, true)
    }
  }

  /* * * * Update Statuses * * * */
  //state is the state to update to
  //data is the bp data from selection
  //multiple is whether updates were called for single blueprint or multiple selected blueprints
  updateState(state, data, multiple){
    //single status update
    if(!multiple){
      this.bpApis.patchBlueprintStatus(state.field, data['id']).subscribe(
        (response: string) => {
          data.status = state.field
          this.messageService.add({ key: 'statusUpdate', severity: 'success', summary: 'Status Updated' });
        }, errResponse => {
          this.statusUpdatesResponseHandler(errResponse, false)
        }
      )
    } 
    
    //multiple status updates
    if(multiple){
      (async () => {
        for (let bp of data) {
          this.bpApis.patchBlueprintStatus(this.updateStateTo, bp.id).subscribe(
            (response: string) => {
              bp.status = this.updateStateTo
              this.statusUpdatesResponseHandler(null, true)
            }, errResponse => {
              this.statusUpdatesResponseHandler(errResponse, true)
            }
          )
          await timeout(1500);
        }
      })();

      function timeout(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
      }
    }
  }

  /* * * * Handles errors and messages for status updates * * * */
  statusUpdatesResponseHandler(response, multiple){
    if(!multiple){
      if(response !== null){
        if (response.error.message.includes('Only 1 blueprint can be in the DEV_COMPLETE state.')) {
          let message = response.error.message.replace('Only 1 blueprint can be in the DEV_COMPLETE state.  ', '\n\nOnly 1 blueprint can be in the DEV_COMPLETE state.\n')
          this.messageService.add({ key: 'statusUpdate', severity: 'error', summary: 'Status Not Updated', detail: message, sticky: true });
        } else {
          this.messageService.add({ key: 'statusUpdate', severity: 'error', summary: 'Error Message', detail: response.error.message, sticky: true });
        }
      }
    }

    if(multiple){
      this.statusUpdateCount++
      if (response === null) {
        this.successfulStatusUpdates++
      } else {
        if (response.error.message.includes('Only 1 blueprint can be in the DEV_COMPLETE state.')) {
          let error = response.error.message.split('Only 1 blueprint can be in the DEV_COMPLETE state.')[0]
          this.statusUpdateErrors.push(error)
        } else { 
          this.messageService.add({ key: 'statusUpdate', severity: 'error', summary: 'Error Message', detail: response.error.message, sticky: true });
        }
      }

      if (this.statusUpdateCount === this.selectionLength) {
        if (this.successfulStatusUpdates > 0) {
          this.messageService.add({ key: 'statusUpdate', severity: 'success', summary: `(${this.successfulStatusUpdates} of ${this.selectionLength}) Statuses Updated`, life: 5000 });
        }
        if (this.statusUpdateErrors.length > 0) {
          let message: string = ''
          for (let elem of this.statusUpdateErrors) {
            message += '- ' + elem + '\n'
          }
          message += '\nOnly 1 blueprint can be in the DEV_COMPLETE state.\nChange the current DEV_COMPLETE blueprint to NOT_NEEDED or IN_DEV before changing another to DEV_COMPLETE.'
          this.messageService.add({ key: 'statusUpdate', severity: 'error', summary: 'Statuses Not Updated', detail: message, sticky: true });
        }
        this.spinnerService.hide()
        this.selectedBPs = []
      }
    }
  }

  bpToDelete: any;
  deleteSingle: boolean = false;
  rowIndexToDelete;
  rowIndexToDeleteFiltered;
  warnDeleteBlueprint(data){
    if(data !== null){
      this.deleteSingle = true;
      this.rowIndexToDeleteFiltered = this.filteredRows.map(function (x) { return x.id; }).indexOf(data['id']);
      this.rowIndexToDelete = this.bpElements.map(function (x) { return x.id; }).indexOf(data['id']);
      this.bpToDelete = data;
      this.messageService.add({ key: 'confirmDeleteToast', sticky: true, severity: 'warn', summary: 'Are you sure?', detail: `- ${data.instanceName} (v${data.version}) for ${data.instanceRelease}` });
    } else {
      this.deleteSingle = false;
      this.selectionLength = this.selectedBPs.length;
      let warnMessage: string = ''
      for(let item of this.selectedBPs){
        warnMessage += `- ${item.instanceName} (v${item.version}) for ${item.instanceRelease}\n`
      }
      this.messageService.add({ key: 'confirmDeleteToast', sticky: true, severity: 'warn', summary: 'Are you sure?', detail: warnMessage });
    }    
  }

  resetFilter = false;
  onConfirmDelete() {
    this.messageService.clear('confirmDeleteToast')

    if (this.filteredName !== '' || this.filteredRelease !== '' || this.filteredTag !== '' || this.filteredType !== '' || this.filteredVersion !== '' || this.filteredStatus !== ''){
      this.resetFilter = true;
    } else {this.resetFilter = false}
    
    if(this.deleteSingle){
      this.bpApis.deleteBlueprint(this.bpToDelete['id']).subscribe(response => {
        this.checkBpWasSelected(this.bpToDelete['id'])
        this.bpElements.splice(this.rowIndexToDelete, 1)
        if (this.resetFilter) {
          this.resetFilters()
        }
        this.messageService.add({ key: 'bpDeleteResponse', severity: 'success', summary: 'Success Message', detail: 'Deployment Artifact Deleted' });
      }, error => {
        this.messageService.add({ key: 'bpDeleteResponse', severity: 'error', summary: 'Error Message', detail: error.error.message });
      })
    } else {
      for(let item of this.selectedBPs){
        this.bpApis.deleteBlueprint(item.id).subscribe(response => {
          this.deleteResponseHandler(true, item.id)
        }, error => {
          this.messageService.add({ key: 'bpDeleteResponse', severity: 'error', summary: 'Error Message', detail: error.error.message });
        })
      }
    }
  }
  onRejectDelete() {
    this.messageService.clear('confirmDeleteToast')
  }

  checkBpWasSelected(id){
    if(this.selectedBPs.length > 0){
      for(let item of this.selectedBPs){
        if(item.id === id){
          let indexToDelete = this.selectedBPs.map(function (x) { return x.id; }).indexOf(item['id']);
          this.selectedBPs.splice(indexToDelete, 1)
        }
      }
    }
  }

  bpsToDelete: string[] = [];
  deleteBpCount = 0;
  deleteResponseHandler(success, bpToDeleteId){
    this.deleteBpCount++
    if(success){
      this.bpsToDelete.push(bpToDeleteId)
    }
    if(this.deleteBpCount === this.selectionLength){
      for(let item of this.bpsToDelete){
        
        let indexToDelete = this.bpElements.map(function (x) { return x.id; }).indexOf(item);
        this.bpElements.splice(indexToDelete, 1)
      }

      if(this.resetFilter){
        this.resetFilters()
      }

      this.selectedBPs = [];
      this.bpsToDelete = [];
      this.deleteBpCount = 0;
      this.messageService.add({ key: 'bpDeleteResponse', severity: 'success', summary: 'Success Message', detail: 'Deployment Artifacts Deleted' });
    }
  }

  resetFilters(){
    let filters: {field: string, value: string}[] = [];
      filters.push({field: 'instanceName', value: this.filteredName})
      filters.push({ field: 'instanceRelease', value: this.filteredRelease })
      filters.push({ field: 'tag', value: this.filteredTag })
      filters.push({ field: 'type', value: this.filteredType })
      filters.push({ field: 'version', value: this.filteredVersion })
      filters.push({ field: 'status', value: this.filteredStatus })
    
    for(let item of filters){
      this.dt.filter(item.value, item.field, 'contains')
    }
  }

  /* * * * Gets all blueprints * * * */
  getAllBPs() {
    this.spinnerService.show();
    this.bpElements = [];
    this.columns = this.cols.map(col => ({ title: col.header, dataKey: col.field }));

    this.visible = "hidden";

    this.bpApis.getAllBlueprints()
      .subscribe((data: any[]) => {
        this.fillTable(data)
      })

  }
  
  /* * * *  Checks when table is filtered and stores filtered data in new object to be downloaded when download button is clicked * * * */
  onTableFiltered(values) {
    if (values) {
      this.filteredRows = values;
    } else {
      this.filteredRows = this.bpElements
    }
  }

  /* * * * Download table as excel file * * * */
  exportTable(exportTo) {
    let downloadElements: any[] = []

    for (let row of this.filteredRows) {
      let labels;
      let notes;
      if (exportTo === "excel") {
        if (row.metadata.labels !== undefined && row.metadata.labels !== null ) {
          labels = row.metadata.labels.join(",")
        }
      } else {
        labels = row.metadata.labels
      }

      if (row.metadata.notes !== null && row.metadata.notes !== undefined && row.metadata.notes !== '') {
        notes = encodeURI(row.metadata.notes).replace(/%20/g, " ").replace(/%0A/g, "\\n")
      }
    
      downloadElements.push({ 
        Instance_Name: row.instanceName, 
        Instance_Release: row.instanceRelease, 
        Tag: row.tag, 
        Type: row.type, 
        Version: row.version, 
        Status: row.status, 
        Created_By: row.metadata.createdBy,
        Created_On: row.metadata.createdOn,
        Updated_By: row.metadata.updatedBy,
        Updated_On: row.metadata.updatedOn,
        Failure_Reason: row.metadata.failureReason,
        Notes: notes,
        Labels: labels
      })
    }
    
    let csvHeaders = []

    if (exportTo === "csv") {
      csvHeaders = [
        "Instance_Name",
        "Instance_Release",
        "Tag",
        "Type",
        "Version",
        "Status",
        "Created_By",
        "Created_On",
        "Updated_By",
        "Updated_On",
        "Failure_Reason",
        "Notes",
        "Labels"];

    }
    
    this.downloadService.exportTableData(exportTo, downloadElements, csvHeaders)
  }

  /* * * * Fills object with blueprint data to be used to fill table * * * */
  fillTable(data) {
    let fileName: string;
    let tag: string;
    let type: string;

    for (let elem of data) {
      fileName = elem.fileName;
      if(fileName.includes('docker')){
        type = 'docker'
        if(fileName.includes('-docker')){
          tag = fileName.split('-docker')[0]
        } else if (fileName.includes('_docker')){
          tag = fileName.split('_docker')[0]
        }
      } else if (fileName.includes('k8s')){
        type = 'k8s'
        if (fileName.includes('-k8s')) {
          tag = fileName.split('-k8s')[0]
        } else if (fileName.includes('_k8s')) {
          tag = fileName.split('_k8s')[0]
        }
      }
      
      //create temporary bp element to push to array of blueprints
      var tempBpElement: BlueprintElement = {
        instanceId:      elem.msInstanceInfo.id,
        instanceName:    elem.msInstanceInfo.name,
        instanceRelease: elem.msInstanceInfo.release,
        id:              elem.id,
        version:         elem.version,
        content:         elem.content,
        status:          elem.status,
        fileName:        fileName,
        tag:             tag, 
        type:            type,
        metadata: {
          failureReason: elem.metadata.failureReason,
          notes:         elem.metadata.notes,
          labels:        elem.metadata.labels,
          createdBy:     elem.metadata.createdBy,
          createdOn:     this.datePipe.transform(elem.metadata.createdOn, 'MM-dd-yyyy HH:mm'),
          updatedBy:     elem.metadata.updatedBy,
          updatedOn:     this.datePipe.transform(elem.metadata.updatedOn, 'MM-dd-yyyy HH:mm')
        },
        specification: {
          id:            elem.specificationInfo.id
        }
      }

      this.bpElements.push(tempBpElement)
    }
    this.bpElements.reverse();
    this.filteredRows = this.bpElements;

    this.resetFilters();

    this.visible = "visible";
    this.spinnerService.hide();
  }

  /* * * * Define content to show in bp view dialog pop up * * * */
  BpContentToView: string;
  viewBpContent(data){
    this.BpFileNameForDownload = `${data['tag']}_${data['type']}_${data['instanceRelease']}_${data['version']}`
    this.BpContentToView = data['content']
    this.showBpContentDialog = true
  }

  /* * * * Download single blueprint * * * */
  BpFileNameForDownload: string;
  download() {
    let file = new Blob([this.BpContentToView], { type: 'text;charset=utf-8' });
    let name: string = this.BpFileNameForDownload + '.yaml'
    saveAs(file, name)
  }

/* * * * Download selected blueprints * * * */
  downloadSelectedBps() {
    let canDownloadBps: boolean = true;
    
    //checks if blueprints for multiple releases are selected
    let selectedBpRelease: string = this.selectedBPs[0]['instanceRelease'];
    for (let bp in this.selectedBPs) {
      if (this.selectedBPs[bp]['instanceRelease'] !== selectedBpRelease) {
        canDownloadBps = false
        break
      }
    }

    //downloads blueprints to zip file if all selected blueprints are for one release
    if (canDownloadBps) {
      var zip = new JSZip();
      for (var i in this.selectedBPs) {
        zip.file(`${this.selectedBPs[i]['tag']}_${this.selectedBPs[i]['type']}_${this.selectedBPs[i]['instanceRelease']}_${this.selectedBPs[i]['version']}.yaml`, this.selectedBPs[i]['content'])
      }
      zip.generateAsync({ type: "blob" }).then(function (content) {
        saveAs(content, 'Blueprints.zip');
      });
    } else {
      this.messageService.add({ key: 'multipleBpReleasesSelected', severity: 'error', summary: 'Error Message', detail: "Cannot download blueprints for different releases" });
    }    

    this.selectedBPs = []
  }
}

export interface BlueprintElement{
  instanceId: string
  instanceName: string
  instanceRelease: string
  id: string
  version: string
  content: string
  status: string
  fileName: string
  tag: string
  type: string
  metadata: {
    failureReason: string
    notes: string
    labels: string[]
    createdBy: string
    createdOn: string
    updatedBy: string
    updatedOn: string
  },
  specification: {
    id: string
  }
}