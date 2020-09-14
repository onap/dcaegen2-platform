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

import { Component, OnInit, ViewChild, ElementRef, Input, EventEmitter, Output } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { BaseMicroserviceService } from '../services/base-microservice.service';
import { MsAddService } from '../services/ms-add.service';
import { MicroserviceInstanceService } from '../services/microservice-instance.service';
import { AuthService } from '../services/auth.service';
import { DatePipe } from '@angular/common';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { DownloadService } from '../services/download.service';

@Component({
  selector: 'app-microservices',
  templateUrl: './microservices.component.html',
  styleUrls: ['./microservices.component.css'],
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
export class MicroservicesComponent implements OnInit {
    @ViewChild(Table, { static: false }) dt: Table;

    /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
    msElements: any[] = [];
    dataSource = new MatTableDataSource<any>(this.msElements);
    cols: any[] = [
        { field: 'name', header: 'MS Name' },
        { field: 'tag', header: 'MS Tag' },
        { field: 'serviceName', header: 'Service Short Name'},
        { field: 'type', header: 'Type', width: '11%' },
        { field: 'namespace', header: 'Namespace', width: '15%' },
        { field: 'status', header: 'Status', width: '90px' }
    ];
    columns: any[];
    loadTable: boolean;
    filteredRows: any;
    downloadItems: { label: string; command: () => void; }[];
    showAddChangeMsInstance: boolean;
    addInstanceTo: string = "";
    msInstanceStates: { label: string, value: string }[] = [
        { label: 'New',           value: 'new' },
        { label: 'In Dev',        value: 'in-dev' },
        { label: 'Dev Complete',  value: 'dev-complete' },
        { label: 'In Test',       value: 'in-test' },
        { label: 'Certified',     value: 'certified' },
        { label: 'Prod Deployed', value: 'prod-deployed' }
    ]

    // Json to add MS to DB, returned from child
    msAddChangeJson: any;

    showMsAddChangeDialog: boolean = false;
    currentRow: any;
    currentMsRow: string = "";
    addOrChange: string;
    username: string;
    errorMessage: any;
    successMessage: string;

    constructor(private spinnerService: Ng4LoadingSpinnerService, private baseMsService: BaseMicroserviceService,
                private msInstanceApi: MicroserviceInstanceService, private messageService: MessageService,
                private addChangeMsApi: MsAddService, private authService: AuthService, private datePipe: DatePipe,
                private downloadService: DownloadService) { }

    ngOnInit() {
        this.username = this.authService.getUser().username;
        this.getAllMs();
    }

    getAllMs() {
        this.spinnerService.show();
        this.msElements = [];
        this.loadTable = false;
        
        this.baseMsService.getAllBaseMs()
        .subscribe((data: any[]) => {
            this.fillTable(data)
        })

        this.columns = this.cols.map(col => ({ title: col.header, dataKey: col.field }));
    }

    /*checks when table is filtered and stores filtered data in new 
    object to be downloaded when download button is clicked*/
    onTableFiltered(values) {
        if (values !== null) {
            this.filteredRows = values;
        } else {
            this.filteredRows = this.msElements;
        }
    }

    //download table as excel file
    exportTable(exportTo: string) {
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
                MS_Name: row.name,
                MS_Tag: row.tag,
                Service_Short_Name: row.serviceName,
                Type: row.type,
                Location: row.location,
                Namespace: row.namespace,
                Status: row.status,
                Created_By: row.metadata.createdBy,
                Created_On: row.metadata.createdOn,
                Updated_By: row.metadata.updatedBy,
                Updated_On: row.metadata.updatedOn,
                Notes: notes,
                Labels: labels
            })
        }
        
        let csvHeaders = []

        if (exportTo === "csv") {
            csvHeaders = [
                "MS_Name", 
                "MS_Tag", 
                "Service_Short_Name", 
                "Type", 
                "Location", 
                "Namespace", 
                "Status", 
                "Created_By", 
                "Created_On", 
                "Updated_By", 
                "Updated_On", 
                "Notes", 
                "Labels" 
            ];
        }

        this.downloadService.exportTableData(exportTo, downloadElements, csvHeaders)
    }

    // * * * * * Show the Dialog to Add a MS (<app-ms-add-change> tag in the html) * * * * *
    showAddChangeDialog(rowData) {
        this.showMsAddChangeDialog = true;
        this.currentRow = rowData;
        if (this.currentRow) {
            this.addOrChange = "Change";
        } else {
            this.addOrChange = "Add";
        }
    }

    // * * * * * Add or Change a MS * * * * *
    // The response includes the entire MS record that was Added or Changed, (along with ID and audit fields).
    // When Added, the response is added directly to the table.  When Changed, the current record is updated field-by-field.
    addOrChangeMs(jsonFromChildDialog) {
        if (jsonFromChildDialog) {
            this.msAddChangeJson = jsonFromChildDialog;
            if (this.addOrChange == "Change") {
                this.currentMsRow = this.currentRow['id']
            }
            this.addChangeMsApi.addChangeMsToCatalog(this.addOrChange, this.currentMsRow, this.msAddChangeJson).subscribe(
                (response: any) => {
                    if (this.addOrChange == "Add") {
                        this.msElements.unshift(response);
                        this.successMessage = "Microservice Added";
                    } else {
                        this.updateCurrentRow(jsonFromChildDialog);
                        this.successMessage = "Microservice Updated";
                    }
                    this.showMsAddChangeDialog = false;
                    this.messageService.add({ key: 'addChangeSuccess', severity: 'success', summary: 'Success', detail: this.successMessage, life: 5000 });
                    },
                errResponse => {
                    // for testing only - this.updateCurrentRow(jsonFromChildDialog);
                    this.messageService.add({ key: 'msAddChangeError', severity: 'error', summary: 'Error', detail: errResponse.error.message, sticky: true });
                }
            )
        }
        else {
            this.showMsAddChangeDialog = false;
        };
    }

    updateCurrentRow(jsonFromChildDialog) {
        const newRow = JSON.parse(jsonFromChildDialog);
        this.currentRow['name']               = newRow['name'];
        this.currentRow['serviceName']        = newRow['serviceName'];
        this.currentRow['type']               = newRow['type'];
        this.currentRow['location']           = newRow['location'];
        this.currentRow['namespace']          = newRow['namespace'];
        this.currentRow['metadata']['labels'] = newRow['metadata']['labels'];
        this.currentRow['metadata']['notes']  = newRow['metadata']['notes'];
    }

    /* * * * Show pop up for Adding a new MS Instance * * * */
    showAddChangeMsInstanceDialog(data) {
        this.addInstanceTo = data['name']
        this.showAddChangeMsInstance = true
    }

    /* * * * Call API to Add a new MS Instance * * * */
    addMsInstance(body) {
        if (body === null) {
            this.showAddChangeMsInstance = false;
        } else {
            this.msInstanceApi.addChangeMsInstance("ADD", this.addInstanceTo, body).subscribe(
                (data) => {
                    this.messageService.add({ key: 'addChangeSuccess', severity: 'success', summary: 'Success', detail: "MS Instance Added", life: 5000 });
                    this.showAddChangeMsInstance = false;
                },
                (errResponse) => {
                    console.log(errResponse)
                    this.messageService.add({ key: 'instanceAddChangeError', severity: 'error', summary: 'Error', detail: errResponse.error.message, sticky: true });
                }
            )
        }
    }

    //fill object with microservice data, to be used to fill table. 
    //checks if fields are empty and if they are, store 'N/A' as the values
    fillTable(data) {
        for (let elem of data) {
            var tempMsElement: any = {
                id:          elem.id,
                name:        elem.name,
                tag:         elem.tag,
                serviceName: elem.serviceName,
                type:        elem.type,
                location:    elem.location,
                namespace:   elem.namespace,
                status:      elem.status,
                metadata: {
                    createdBy: elem.metadata.createdBy,
                    createdOn: this.datePipe.transform(elem.metadata.createdOn, 'MM-dd-yyyy HH:mm'),
                    updatedBy: elem.metadata.updatedBy,
                    updatedOn: this.datePipe.transform(elem.metadata.updatedOn, 'MM-dd-yyyy HH:mm'),
                    notes:     elem.metadata.notes,
                    labels:    elem.metadata.labels
                },
                msInstances: elem.msInstances
            }
            this.msElements.push(tempMsElement)
        }
        this.filteredRows = this.msElements
        this.loadTable = true;
        this.spinnerService.hide();
    }
}

export interface AddMsInstance{
  name: string,
  release: string, 
  metadata: {
    scrumLead: string,
    scrumLeadId: string,
    systemsEngineer: string,
    systemsEngineerId: string,
    developer: string;
    developerId: string;
    pstDueDate: string,
    pstDueIteration: string,
    eteDueDate: string,
    eteDueIteration: string,
    labels: string[],
    notes: string
  }
  user: string
}
