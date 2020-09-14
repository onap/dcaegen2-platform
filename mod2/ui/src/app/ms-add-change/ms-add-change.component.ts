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
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { AuthService } from '../services/auth.service';

interface Type {
    type: string;
}
interface Location {
    location: string;
}

@Component({
    selector: 'app-ms-add-change',
    templateUrl: './ms-add-change.component.html',
    styleUrls: ['./ms-add-change.component.css']
})

export class MsAddChangeComponent implements OnInit {

    guiHeader:    string = "Microservice ADD"
    // Used for the Add/Update button label
    addOrUpdate: string = "Add";

    msAddForm: FormGroup;
    // The loggged in user
    username: string;

    // Input form fields
    name:        string;
    tag:         string;
    serviceName: string = "";
    type:        string;
    location:    string;
    namespace:   string;
    labels:      string;
    notes:       string;

    // Dropdowns
    types:     Type[];
    locations: Location[];

    // Return JSON to parent component
    msAddChangeString: any;
    msAddChangeJson:   any;

    constructor(private fb: FormBuilder, private authService: AuthService) {
    }

    @Input() visible: boolean;
    @Input() currentRow: any;
    @Output() handler: EventEmitter<any> = new EventEmitter();

    ngOnInit() {
        // The logged in user
        this.username = this.authService.getUser().username;

        this.msAddForm = new FormGroup({
            name:        new FormControl(),
            tag:         new FormControl(),
            serviceName: new FormControl(),
            type:        new FormControl(),
            //location:    new FormControl(),
            namespace:   new FormControl(),
            labels:      new FormControl(),
            notes:       new FormControl()
        }); 

        // FORM fields and validations
        this.msAddForm = this.fb.group({
            name:        ['', [Validators.required]],
            tag:         ['', [Validators.required, Validators.pattern('^([a-z0-9](-[a-z0-9])*)+$'), Validators.minLength(5), Validators.maxLength(50)]],
            serviceName: ['', [Validators.pattern('^([a-z](-[a-z])*)+$'), Validators.maxLength(25)]],
            type:        ['', [Validators.required]],
            //location:    ['', [Validators.required]],
            namespace:   ['', [Validators.pattern('^([a-z0-9](-[a-z0-9])*)+$')]],
            labels:      ['', []],
            notes:       ['', []]
            },
            {updateOn: "blur"}
        );

        // TYPE Dropdown
        this.types = [
            { type: 'FM_COLLECTOR' },
            { type: 'PM_COLLECTOR' },
            { type: 'ANALYTIC' },
            { type: 'TICK' },
            { type: 'OTHER' }
        ];
        // LOCATION Dropdown
        this.locations = [
            { location: 'EDGE' },
            { location: 'CENTRAL' },
            { location: 'UNSPECIFIED' }
        ];

        // "Update" was selected, so populate the current row data in the GUI
        if (this.currentRow) {
            this.guiHeader = "Microservice Update";
            this.addOrUpdate = "Update";
            this.populateFields();
        }

    }

    populateFields() {
        let labelsStr: string;
        if (this.currentRow['metadata']['labels']) {
            labelsStr = this.currentRow['metadata']['labels'].join(' ')
        }

        // Prevent validation (length check) from failing in the html
        if (this.currentRow['serviceName']) {
             this.serviceName = this.currentRow['serviceName']
        }

        this.msAddForm.patchValue({
            name:        this.currentRow['name'],
            tag:         this.currentRow['tag'],
            serviceName: this.serviceName,
            type:        {type: this.currentRow['type']},
            //location:    {location: this.currentRow['location']},
            namespace:   this.currentRow['namespace'],
            labels:      labelsStr,
            notes:       this.currentRow['metadata']['notes']
        })
    }

    // The handler emits 'null' back to parent to close dialog and make it available again when clicked
    closeDialog() {
        this.visible = false;
        this.handler.emit(null);
    }

    // Create the JSON to be sent to the parent component
    // The "labels" functions below take into account leading/trailing spaces, multiple spaces between labels, and conversion into an array
    createOutputJson() {
        this.msAddChangeString = {
        name:        this.msAddForm.value['name'].trim(),
        tag:         this.msAddForm.value['tag'],
        serviceName: this.msAddForm.value['serviceName'],
        type:        this.msAddForm.value['type'].type,
        location:    'UNSPECIFIED',
        //location:    this.msAddForm.value['location'].location,
        namespace:   this.msAddForm.value['namespace'].trim(),
        metadata: {
            labels: this.msAddForm.value['labels'].trim().replace(/\s{2,}/g, ' ').split(" "),
            notes:  this.msAddForm.value['notes']
        },
        user: this.username
        };
    }

    saveMs() {
        this.createOutputJson();
        this.msAddChangeJson = JSON.stringify(this.msAddChangeString);
        this.handler.emit(this.msAddChangeJson);
    }

}
