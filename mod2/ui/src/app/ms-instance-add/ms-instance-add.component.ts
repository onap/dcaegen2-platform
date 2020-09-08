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

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { MicroserviceInstanceService } from '../services/microservice-instance.service';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-ms-instance-add',
  templateUrl: './ms-instance-add.component.html',
  styleUrls: ['./ms-instance-add.component.css']
})
export class MsInstanceAddComponent implements OnInit {

  guiHeader: string = "Microservice Instance ADD";
  // Used for the Add/Update button label
  addOrUpdate: string = "Add";

  msInstanceAddForm: FormGroup;
  msInstanceToAdd: AddMsInstance;
  addInstanceTo: string = "";
  username: string;
  msInstanceReleases: { label: string, value: string }[] = [
    { label: '2004', value: '2004' },
    { label: '2006', value: '2006' },
    { label: '2008', value: '2008' },
    { label: '2009', value: '2009' },
    { label: '2010', value: '2010' },
    { label: '2011', value: '2011' },
    { label: '2012', value: '2012' }
  ]

  @Input() visible: boolean;
  @Input() msName: string;
  @Input() msInstanceChange: string;     // Use to differentiate Add from Change, since currentRow can be problematic
  @Input() currentRow: any;
  @Output() handler: EventEmitter<any> = new EventEmitter();

  constructor(private addChangeMsInstanceApi: MicroserviceInstanceService, private messageService: MessageService, private fb: FormBuilder, private authService: AuthService, private datePipe: DatePipe) { }

  ngOnInit() {
    this.username = this.authService.getUser().username;

    this.msInstanceAddForm = new FormGroup({
      name: new FormControl(),
      release: new FormControl(),
      scrumLead: new FormControl(),
      scrumLeadId: new FormControl(),
      systemsEngineer: new FormControl(),
      systemsEngineerId: new FormControl(),
      developer: new FormControl(),
      developerId: new FormControl(),
      status: new FormControl(),
      pstDueDate: new FormControl(),
      pstDueIteration: new FormControl(),
      eteDueDate: new FormControl(),
      eteDueIteration: new FormControl(),
      labels: new FormControl(),
      notes: new FormControl()
    });

    this.msInstanceAddForm = this.fb.group({
      name: ['', []],
      release: ['', [Validators.required]],
      scrumLead: ['', []],
      scrumLeadId: ['', []],
      systemsEngineer: ['', []],
      systemsEngineerId: ['', []],
      developer: ['', [Validators.required]],
      developerId: ['', [Validators.required]],
      status: ['', []],
      pstDueDate: ['', []],
      pstDueIteration: ['', []],
      eteDueDate: ['', []],
      eteDueIteration: ['', []],
      labels: ['', []],
      notes: ['', []]
    });
    
    if (this.msInstanceChange) {
        this.guiHeader   = "Microservice Instance Update";
        this.addOrUpdate = "Update";
        this.populateFields();
    }
  }

  populateFields() {
    this.msName = this.currentRow['name'];

    let labelsStr: string;
    if (this.currentRow['metadata']['labels']) {
        labelsStr = this.currentRow['metadata']['labels'].join(' ')
    }
    
    this.msInstanceAddForm.patchValue({
        release:           this.currentRow['release'],
        scrumLead:         this.currentRow['metadata']['scrumLead'],
        scrumLeadId:       this.currentRow['metadata']['scrumLeadId'],
        systemsEngineer:   this.currentRow['metadata']['systemsEngineer'],
        systemsEngineerId: this.currentRow['metadata']['systemsEngineerId'],
        developer:         this.currentRow['metadata']['developer'],
        developerId:       this.currentRow['metadata']['developerId'],
        pstDueDate:        this.currentRow['pstDueDate'],
        pstDueIteration:   this.currentRow['pstDueIteration'],
        eteDueDate:        this.currentRow['eteDueDate'],
        eteDueIteration:   this.currentRow['eteDueIteration'],
        labels:            labelsStr,
        notes:             this.currentRow['metadata']['notes']
    })
}

  /* * * * On click of cancel * * * */
  closeDialog() {
    this.visible = false;
    this.handler.emit(null)
  }

  /* * * * On click of add * * * */
  submitMsInstance() {
    //  Prevent error on "split" if record existed before "labels" were implemented
    let labels: string[] = []
    if (!this.msInstanceAddForm.value['labels']){
       labels = []
    } else {
        labels = this.msInstanceAddForm.value['labels'].trim().replace(/\s{2,}/g, ' ').split(" ")
    }

    //build request body
    this.msInstanceToAdd = {
      name:    this.msName,
      release: this.msInstanceAddForm.value['release'],
      metadata: {
        scrumLead:         this.msInstanceAddForm.value['scrumLead'],
        scrumLeadId:       this.msInstanceAddForm.value['scrumLeadId'],
        systemsEngineer:   this.msInstanceAddForm.value['systemsEngineer'],
        systemsEngineerId: this.msInstanceAddForm.value['systemsEngineerId'],
        developer:         this.msInstanceAddForm.value['developer'],
        developerId:       this.msInstanceAddForm.value['developerId'],
        pstDueDate:        this.msInstanceAddForm.value['pstDueDate'],
        pstDueIteration:   this.msInstanceAddForm.value['pstDueIteration'],
        eteDueDate:        this.msInstanceAddForm.value['eteDueDate'],
        eteDueIteration:   this.msInstanceAddForm.value['eteDueIteration'],
        labels:            labels,
        notes:             this.msInstanceAddForm.value['notes']
      },
      user: this.username
    }

    this.handler.emit(this.msInstanceToAdd) //return request body back to parent
  }
}

export interface AddMsInstance {
  name: string,
  release: string,
  metadata: {
    scrumLead: string,
    scrumLeadId: string,
    systemsEngineer: string,
    systemsEngineerId: string,
    developer: string,
    developerId: string,
    pstDueDate: any,
    pstDueIteration: string,
    eteDueDate: any,
    eteDueIteration: string,
    labels: string[],
    notes: string
  }
  user: string
}