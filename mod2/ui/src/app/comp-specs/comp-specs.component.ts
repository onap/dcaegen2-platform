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
import { compSpecsService } from '../services/comp-specs-service.service';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { DatePipe } from '@angular/common';
import { trigger, state, transition, style, animate } from '@angular/animations';
import { ActivatedRoute } from '@angular/router';
import { DownloadService } from '../services/download.service';

@Component({
  selector: 'app-comp-specs',
  templateUrl: './comp-specs.component.html',
  styleUrls: ['./comp-specs.component.css'],
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
export class CompSpecsComponent implements OnInit {
  @ViewChild(Table, { static: false }) dt: Table;

  csElements: any[] = [];
 
  cols: any[] = [
    { field: 'instanceName', header: 'Instance Name' },
    { field: 'release', header: 'Release', width: '15%' },
    { field: 'type', header: 'Type', width: '15%' },
    { field: 'policy', header: 'Policy', width: '15%' },
    { field: 'status', header: 'Status', width: '15%' }
  ];

  columns: any[];
  loadTable: boolean;
  filteredRows: any;
  summaryRows: any;
  downloadItems: { label: string; command: () => void; }[];

  msInstanceId: string;
  msInstanceName: any;
  msInstanceRelease: any;

  constructor(private csApis: compSpecsService, private messageService: MessageService, 
    private spinnerService: Ng4LoadingSpinnerService, private datePipe: DatePipe, 
    private route: ActivatedRoute, private downloadService: DownloadService) { }

  //create table of comp specs
  ngOnInit() {
    this.loadTable = false;

    this.route.queryParams.subscribe((params) => {
      this.msInstanceId = params['instanceId'];
    });

    this.getAllCs()
  }

  getAllCs() {
    
    this.csElements = [];

    this.csApis.getAllCompSpecs(this.msInstanceId)
      .subscribe((data: any[]) => {
        this.fillTable(data)
      })

    this.columns = this.cols.map(col => ({ title: col.header, dataKey: col.field }));
  }

  //filter table
  onTableFiltered(values) {
    if (values) { this.filteredRows = values; }
    else { this.filteredRows = this.summaryRows; }
  }

  /* * * * Export ms instance table to excel or csv * * * */
  exportTable(exportTo) {
    let downloadElements: any[] = []

    //labels array not handled well by excel download so converted them to a single string
    for (let row of this.filteredRows) {
      let labels;
      let notes;
      if (exportTo === "excel") {
        if (row.metadata.labels !== undefined) {
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
        Release: row.release,
        Type: row.type,
        Status: row.status,
        Created_By: row.metadata.createdBy,
        Created_On: row.metadata.createdOn,
        Updated_By: row.metadata.updatedBy,
        Updated_On: row.metadata.updatedOn,
        Notes: notes,
        Labels: labels
      })
    }

    let arrHeader = []

    if (exportTo === "csv") {
      arrHeader = [
        "Instance_Name",
        "Release",
        "Type",
        "Status",
        "Created_By",
        "Created_On",
        "Updated_By",
        "Updated_On",
        "Notes",
        "Labels"
      ];
    }

    this.downloadService.exportTableData(exportTo, downloadElements, arrHeader)
  }

  //fill object with microservice data, to be used to fill table. 
  //checks if fields are empty and if they are, store 'N/A' as the values
  fillTable(data) {
    for (let elem of data) {
      let policy = '';
      if(elem.policyJson){policy = "Included"}

      let tempCsElement: any = {
        id: elem.id,
        instanceName: elem.msInstanceInfo.name,
        release: elem.msInstanceInfo.release,
        type: elem.type,
        policy: policy,
        status: elem.status,
        specContent: elem.specContent,
        policyJson: elem.policyJson,
        metadata: {
          createdBy: elem.metadata.createdBy,
          createdOn: this.datePipe.transform(elem.metadata.createdOn, 'MM-dd-yyyy HH:mm'),
          updatedBy: elem.metadata.updatedBy,
          updatedOn: this.datePipe.transform(elem.metadata.updatedOn, 'MM-dd-yyyy HH:mm'),
          notes: elem.metadata.notes,
          labels: elem.metadata.labels
        }
      }
      this.csElements.unshift(tempCsElement)
    }
    this.msInstanceName = this.csElements[0]['instanceName']
    this.msInstanceRelease = this.csElements[0]['release']
    this.filteredRows = this.csElements
    this.loadTable = true;
    this.spinnerService.hide();
  }

  showViewCs: boolean = false;
  specContentToView: string;
  showViewCsDialog(data) {
    this.showViewCs = true;
    this.specContentToView = data.specContent;
  }
  
  showViewPolicy: boolean = false;
  policyJsonToView: string;
  showViewPolicyDialog(data) {
    this.showViewPolicy = true;
    this.policyJsonToView = data.policyJson;
  }

  /* * * * Download single spec file or policy * * * */
  download(content, contentType) {
    let fileName = `${this.msInstanceName}_${this.msInstanceRelease}_${contentType}`
    this.downloadService.downloadJSON(content, fileName)
  }
}