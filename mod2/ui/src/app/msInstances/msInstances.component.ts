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

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { MicroserviceInstanceService } from '../services/microservice-instance.service';
import { DatePipe } from '@angular/common';
import { DeploymentArtifactService } from '../services/deployment-artifact.service';
import { CompSpecAddService } from '../services/comp-spec-add.service';
import { BreadcrumbService } from '../services/breadcrumb.service';
import { Router } from '@angular/router';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { DownloadService } from '../services/download.service';

@Component({
  selector: 'app-msInstances',
  templateUrl: './msInstances.component.html',
  styleUrls: ['./msInstances.component.css'],
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
  providers: [DatePipe]
})
export class MsInstancesComponent implements OnInit {
  @ViewChild(Table, { static: false }) dt: Table;
  @ViewChild('myInput', { static: false }) myInputVariable: ElementRef;
  

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  msInstances: msInstance[] = [];
  expandedItems: Array<any> = new Array<any>();
  dataSource = new MatTableDataSource<msInstance>(this.msInstances);
  cols: any[] = [
    { field: 'baseMsName', header: 'MS Name' },
    { field: 'tag', header: 'MS Tag' },
    { field: 'release', header: 'Release', width: '7%' },
    { field: 'pstDueDate', header: 'PST Date', width: '9%' },
    { field: 'pstDueIteration', header: 'PST Iteration', width: '6.5%' },
    { field: 'eteDueDate', header: 'ETE Date', width: '9%' },
    { field: 'eteDueIteration', header: 'ETE Iteration', width: '6.5%' },
    { field: 'status', header: 'Status', width: '125px' }
  ];
  selectedMsInstances: msInstance[] = [];
  columns: any[];
  loadTable: boolean;
  filteredRows: any;
  downloadItems: { label: string; command: () => void; }[];
  showAddChangeMsInstance: boolean;
  currentRow: any;
  msInstanceChange: string = "change";
  generatedBPs: any[] = [];
  canGenerateSelectedBPs: boolean = false;
  generateSelectedBPsTooltip: string = '';

  // Json to add CS (Component Spec) to DB, returned from child
  csAddJson: any;

  showCsAddDialog: boolean = false;

  showViewCs: boolean =false;
  msInstanceId: string = '';
  errorList: string[];

  constructor(private spinnerService: Ng4LoadingSpinnerService, private msInstanceApi: MicroserviceInstanceService,
              private bpApis: DeploymentArtifactService, private addCsApi: CompSpecAddService, private messageService: MessageService,
              private datePipe: DatePipe, private router: Router, private downloadService: DownloadService, private bread: BreadcrumbService) { }

  ngOnInit() {

    this.getAllInstances();
    
  }

  getAllInstances() {
    this.spinnerService.show();
    this.msInstances = [];
    this.loadTable = false;
    
    this.msInstanceApi.getAllMsInstances()
      .subscribe((data: any[]) => {
        this.fillTable(data)
      })

    this.columns = this.cols.map(col => ({ title: col.header, dataKey: col.field }));
  }


  // * * * * * Show the Dialog to Change an MS Instance (<app-ms-instance-add> tag in the html) * * * * *
  showAddChangeDialog(rowData) {
      this.msInstanceId = rowData['id']  
      this.showAddChangeMsInstance = true;
      this.currentRow = rowData;
  }

    /* * * * Call API to Change an MS Instance * * * */
  addChangeMsInstance(jsonFromChildDialog) {
    if (jsonFromChildDialog === null) {
          this.showAddChangeMsInstance = false;
      } else {
          this.msInstanceApi.addChangeMsInstance("CHANGE", this.msInstanceId, jsonFromChildDialog).subscribe(
              (data) => {
                this.updateCurrentRow(data);  
                this.messageService.add({ key: 'changeSuccess', severity: 'success', summary: 'Success', detail: "MS Instance Updated", life: 5000 });
                this.showAddChangeMsInstance = false;
              },
              (errResponse) => {
                if (errResponse.error.message) {
                  this.messageService.add({ key: 'instanceAddChangeError', severity: 'error', summary: 'Error', detail: errResponse.error.message, sticky: true });
                } else {
                  this.messageService.add({ key: 'instanceAddChangeError', severity: 'error', summary: 'Error', detail: errResponse.error.status, sticky: true });
                }
              }
          )
      }
  }

  updateCurrentRow(responseData) {
    const newRow = responseData;
    this.currentRow['release']                       = newRow['release'];
    this.currentRow['metadata']['scrumLead']         = newRow['metadata']['scrumLead'];
    this.currentRow['metadata']['scrumLeadId']       = newRow['metadata']['scrumLeadId'];
    this.currentRow['metadata']['systemsEngineer']   = newRow['metadata']['systemsEngineer'];
    this.currentRow['metadata']['systemsEngineerId'] = newRow['metadata']['systemsEngineerId'];
    this.currentRow['metadata']['developer']         = newRow['metadata']['developer'];
    this.currentRow['metadata']['developerId']       = newRow['metadata']['developerId'];
    this.currentRow['pstDueDate']                    = this.datePipe.transform(newRow['metadata']['pstDueDate'], 'yyyy-MM-dd');
    this.currentRow['pstDueIteration']               = newRow['metadata']['pstDueIteration'];
    this.currentRow['eteDueDate']                    = this.datePipe.transform(newRow['metadata']['eteDueDate'], 'yyyy-MM-dd');
    this.currentRow['eteDueIteration']               = newRow['metadata']['eteDueIteration'];
    this.currentRow['metadata']['labels']            = newRow['metadata']['labels'];
    this.currentRow['metadata']['notes']             = newRow['metadata']['notes'];
    this.currentRow['metadata']['updatedBy']         = newRow['metadata']['updatedBy'];
    this.currentRow['metadata']['updatedOn']         = this.datePipe.transform(newRow['metadata']['updatedOn'], 'MM-dd-yyyy HH:mm');
  }

  // * * * * * Show the Dialog to Add a CS (in the html) * * * * *
  // * * * * * Store the MS Instance ID for the URL and the "current row" to update when a CS is saved * * * * *
  showAddCSDialog(rowData) {
    this.showCsAddDialog = true;
    this.msInstanceId = rowData['id'];
    this.currentRow = rowData;
  }

  // * * * * * Add a CS * * * * *
  addNewCs(jsonFromChildDialog) {
    let compSpecAddMessage = '';
    if (jsonFromChildDialog) {
      this.csAddJson = jsonFromChildDialog;
      if((JSON.parse(this.csAddJson)).policyJson === null){
        compSpecAddMessage = 'Component Spec Added';
      } else {
        console.log("here")
        compSpecAddMessage = 'Component Spec and Policy added '
      }
      
      this.addCsApi.addCsToCatalog(this.msInstanceId, this.csAddJson).subscribe(
        (response: any) => {
          this.messageService.add({ key: 'compSpecAdded', severity: 'success', summary: 'Success', detail: compSpecAddMessage, life: 5000 });
          this.showCsAddDialog = false;
          this.currentRow['activeSpec'] = true;
        },
        errResponse => {
            if (errResponse.error.errors) {
                this.messageService.add({ key: 'errorOnCsAdd', severity: 'error', summary: errResponse.error.message, detail: errResponse.error.errors.join('\n'), sticky: true});
            } else {
                let summary = errResponse.error.status + " - " + errResponse.error.error;
                this.messageService.add({ key: 'errorOnCsAdd', severity: 'error', summary: summary, detail: errResponse.error.message, sticky: true});
            }
         });
    } else {
        this.showCsAddDialog = false
    };
  }

/* * * * View Component Specs 
msName: string;
msRelease: string;
  showViewCsDialog(data){
    this.msInstanceId = data['id']
    this.msName = data['name']
    this.msRelease = data['release']
    this.showViewCs = true;
  }
  csView(data){
    if(data===null){
      this.showViewCs = false
    } else {
      this.showViewCs = false
      this.messageService.add({ key: 'csViewError', severity: 'error', summary: 'Error Message', detail: data.error.message, sticky: true });    
    }
  } 
  * * * */

  /* * * * Generate single blueprint * * * */
  generateBlueprints(data){
    this.bpApis.postBlueprint(data['id']).subscribe((response) => { 
      this.messageService.add({ key: 'bpGenMessage', severity: 'success', summary: 'Success Message', detail: 'Blueprint Generated', life: 5000 });          
    }, (errResponse) => { 
      this.messageService.add({ key: 'bpGenMessage', severity: 'error', summary: 'Error Message', detail: errResponse.error.message, life: 15000 });          
    })
  }

  /* * * * Check if generate selected blueprints button should be disabled and set tooltip message * * * */
  checkCanGenerateBp() {
    if (this.selectedMsInstances.length > 0) {

      let noActiveSpecs: string[] = [];
      let checkReleases: boolean = true;
      let firstRelease = this.selectedMsInstances[0]['release'];
      for (let elem of this.selectedMsInstances) {
        if (elem.release !== firstRelease){
          checkReleases = false
          this.canGenerateSelectedBPs = false
          this.generateSelectedBPsTooltip = 'Cannot Generate Blueprints For Different Releases'
          break
        }
        if (elem.activeSpec === null) {
          noActiveSpecs.push(elem.name)
          this.generateSelectedBPsTooltip += elem.name
        }
      }

      if (noActiveSpecs.length < 1 && checkReleases) {
        this.canGenerateSelectedBPs = true
      } else if (noActiveSpecs.length > 0 && checkReleases){
        this.canGenerateSelectedBPs = false
        this.generateSelectedBPsTooltip = 'No Active Specs For :  '
        let i: number = 1;
        for (let elem of noActiveSpecs) {
          if (i === noActiveSpecs.length) {
            this.generateSelectedBPsTooltip += '{' + elem + '}'
          } else {
            this.generateSelectedBPsTooltip += '{' + elem + '}, '
          }
          i++
        }
      }
    } else  {
      this.canGenerateSelectedBPs = false
      this.generateSelectedBPsTooltip = "No Instances Selected"
    }
  }
  
  /* * * * Generate multiple blueprint * * * */
  successfulBpGens: number;
  generateSelectedBlueprints(){
    this.successfulBpGens = 0;
    this.count = 0;
    this.selectionLength = this.selectedMsInstances.length;

    this.spinnerService.show();

    (async () => {
      for (let instance of this.selectedMsInstances) {
        this.bpApis.postBlueprint(instance.id).subscribe((response) => {
          this.bpGenSuccess()          
        }, (errResponse) => {
            this.bpGenError(errResponse.error.message)
        })
        await timeout(1500);
      }
    })();

    function timeout(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }

    this.selectedMsInstances = []
  }

  /* * * * For BP Gen Successes * * * */
  selectionLength: number;
  count: number;
  bpGenSuccess(){
    this.successfulBpGens++;
    this.count++;
    if (this.count === this.selectionLength){
      if(this.successfulBpGens > 0){
        this.messageService.add({ key: 'bpGenMessage', severity: 'success', summary: 'Success Message', detail: 'Blueprints Generated', life: 5000 });
        this.spinnerService.hide()
      }
    }
  }

  /* * * * For BP Gen Errors * * * */
  bpGenError(err){
    this.count++;
    if (this.count === this.selectionLength) {
      if (this.successfulBpGens > 0) {
        this.messageService.add({ key: 'bpGenMessage', severity: 'success', summary: 'Success Message', detail: 'Blueprints Generated', life: 5000 });
        this.spinnerService.hide()
      }
    }
    this.messageService.add({ key: 'bpGenMessage', severity: 'error', summary: 'Error Message', detail: err, life: 15000 });
  }

  /* * * * View the Blueprints for the selected MS Instance * * * */
  viewBlueprints(rowData) {
    this.router.navigate(["blueprints"], {queryParams:{tag: rowData['tag'], release:rowData['release'] }});
    this.bread.setBreadcrumbs("Blueprints", "add");
  }

  /* * * * View the Component Spec for the selected MS Instance * * * */
  viewCompSpecs(rowData) {
    this.router.navigate(["CompSpecs"], { queryParams: { instanceId: rowData['id'] }});
    this.bread.setBreadcrumbs("Component Specs", "add");
  }

  /* * * * Stores filtered data in new array * * * */
  onTableFiltered(values) {
    if (values) { this.filteredRows = values; }
    else { this.filteredRows = this.msInstances; }
  }

  /* * * * Export ms instance table to excel or csv * * * */
  exportTable(exportTo) {
    let downloadElements: any[] = []

    //labels array not handled well by excel download so converted them to a single string
    for(let row of this.filteredRows){
      let labels;
      let notes;
      if(exportTo === "excel"){
        if(row.metadata.labels !== undefined){
          labels = row.metadata.labels.join(",")
        }
      } else {
        labels = row.metadata.labels
      }

      if (row.metadata.notes !== null && row.metadata.notes !== undefined && row.metadata.notes !== '') {
        notes = encodeURI(row.metadata.notes).replace(/%20/g, " ").replace(/%0A/g, "\\n")
      }

      downloadElements.push({
        MS_Name: row.name,
        MS_Tag: row.tag,
        Release: row.release,
        PST_Due_Date: row.pstDueDate,
        PST_Due_Iteration: row.pstDueIteration,
        ETE_Due_Date: row.eteDueDate,
        ETE_Due_Iteration: row.eteDueIteration,
        Status: row.status,
        Created_By: row.metadata.createdBy,
        Created_On: row.metadata.createdOn,
        Updated_By: row.metadata.updatedBy,
        Updated_On: row.metadata.updatedOn,
        Scrum_Lead: row.metadata.scrumLead,
        Scrum_Lead_Id: row.metadata.scrumLeadId,
        Systems_Engineer: row.metadata.systemsEngineer,
        Systems_Engineer_Id: row.metadata.systemsEngineerId,
        Developer: row.metadata.developer,
        Developer_Id: row.metadata.developerId,
        Notes: notes,
        Labels: labels
      })
    }

    let csvHeaders = [];

    if (exportTo === "csv") {
      csvHeaders = [
        "MS_Name", 
        "MS_Tag", 
        "Release", 
        "PST_Due_Date", 
        "PST_Due_Iteration", 
        "ETE_Due_Date", 
        "ETE_Due_Iteration", 
        "Status", 
        "Created_By", 
        "Created_On", 
        "Updated_By", 
        "Updated_On", 
        "Scrum_Lead", 
        "Scrum_Lead_Id", 
        "Systems_Engineer", 
        "Systems_Engineer_Id", 
        "Developer", 
        "Developer_Id", 
        "Notes", 
        "Labels"
      ];
    }

    this.downloadService.exportTableData(exportTo, downloadElements, csvHeaders)
  }
  
    /* * * * Fill ms instance table * * * */
    fillTable(data) {

        for (let elem of data) {

          /* * * Now storing as dates (not strings) on DB, so need to convert old data (mm-dd-yyyy and m-d-yyyy) * * */
          let pstDueDate: any;
          if  (elem.metadata.pstDueDate && (elem.metadata.pstDueDate.length <= 11 &&
                                                        elem.metadata.pstDueDate.length > 7)) {
              pstDueDate = new Date(elem.metadata.pstDueDate.replace(/-/g, '/'))  // dash is invalid date format, FF fails
              pstDueDate = this.datePipe.transform(pstDueDate, 'yyyy-MM-dd')
          } else if (elem.metadata.pstDueDate) {
              pstDueDate = this.datePipe.transform(elem.metadata.pstDueDate, 'yyyy-MM-dd')
          } else {
              pstDueDate = elem.metadata.pstDueDate
          }

          let eteDueDate: any;
          if  (elem.metadata.eteDueDate && (elem.metadata.eteDueDate.length <= 11 &&
                                                        elem.metadata.eteDueDate.length > 7)) {
              eteDueDate = new Date(elem.metadata.eteDueDate.replace(/-/g, '/'))  // dash is invalid date format, FF fails
              eteDueDate = this.datePipe.transform(eteDueDate, 'yyyy-MM-dd')
          } else if (elem.metadata.eteDueDate) {
              eteDueDate = this.datePipe.transform(elem.metadata.eteDueDate, 'yyyy-MM-dd')
          } else {
              eteDueDate = elem.metadata.eteDueDate
          }

            var tempElem: msInstance = {
                id:         elem.id,
                name:       elem.name,
                tag:        elem.msInfo.tag,
                release:    elem.release,
                version:    elem.version,
                status:     elem.status,
                baseMsId:   elem.msInfo.id,
                baseMsName: elem.msInfo.name,
                metadata: {
                    scrumLead:         elem.metadata.scrumLead,
                    scrumLeadId:       elem.metadata.scrumLeadId,
                    systemsEngineer:   elem.metadata.systemsEngineer,
                    systemsEngineerId: elem.metadata.systemsEngineerId,
                    developer:         elem.metadata.developer,
                    developerId:       elem.metadata.developerId,
                    createdBy:         elem.metadata.createdBy,
                    createdOn:         this.datePipe.transform(elem.metadata.createdOn, 'MM-dd-yyyy HH:mm'),
                    updatedBy:         elem.metadata.updatedBy,
                    updatedOn:         this.datePipe.transform(elem.metadata.updatedOn, 'MM-dd-yyyy HH:mm'),
                    notes:             elem.metadata.notes,
                    labels:            elem.metadata.labels,
                },
                pstDueDate:      pstDueDate,
                pstDueIteration: elem.metadata.pstDueIteration,
                eteDueDate:      eteDueDate,
                eteDueIteration: elem.metadata.eteDueIteration,
                activeSpec:      elem.activeSpec
            }
            this.msInstances.push(tempElem)
        }
        
        this.filteredRows = this.msInstances
        this.loadTable = true;    
        this.spinnerService.hide()
    }
}

export interface msInstance{
  id: string,
  name: string,
  tag: string,
  release: string,
  version: string,
  status: string,
  baseMsId: string, 
  baseMsName: string ,
  metadata: {
    scrumLead: string,
    scrumLeadId: string,
    systemsEngineer: string,
    systemsEngineerId: string,
    developer: string,
    developerId: string,
    createdBy: string,
    createdOn: string,
    updatedBy: string,
    updatedOn: string,
    notes: string,
    labels: string[],
  }
  pstDueDate: string,
  pstDueIteration: string,
  eteDueDate: string,
  eteDueIteration: string,
  activeSpec: any
}