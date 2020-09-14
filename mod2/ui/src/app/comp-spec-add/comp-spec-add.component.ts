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
import { MessageService } from 'primeng/api';

interface Type {
    type: string;
}

@Component({
    selector: 'app-comp-spec-add',
    templateUrl: './comp-spec-add.component.html',
    styleUrls: ['./comp-spec-add.component.css']
})

export class CompSpecAddComponent implements OnInit {

    compSpecSelected: any;
    compSpecContent: any;

    policySelected: any;
    policyContent: any;

    csAddForm: FormGroup;
    //  The loggged in user
    username: string;

    //  Input form fields
    type: string;
    labels: string;
    notes: string;
    specFile: any;
    policyJson: any;

    //  Dropdowns
    types: Type[];
    
    //  Build JSON as a string
    csAddString: any;
    //  Return JSON to parent component
    csAddJson: any;
    
    constructor(private fb: FormBuilder, private authService: AuthService, private messageService: MessageService) {
    }

    @Input() visible: boolean;
    @Output() handler: EventEmitter<any> = new EventEmitter();

    ngOnInit() {
        //  The logged in user
        this.username = this.authService.getUser().username;

        this.csAddForm = new FormGroup({
            type: new FormControl(),
            labels: new FormControl(),
            notes: new FormControl(),
            specFile: new FormControl(),
            policyJson: new FormControl()
        });
    
        //  FORM fields and validations
        this.csAddForm = this.fb.group({
            type: ['', [Validators.required]],
            labels: ['', []],
            notes: ['', []],
            specFile: ['', []],
            policyJson: ['', []]
        });
    
        // TYPE Dropdown
        this.types = [
            { type: 'DOCKER' },
            { type: 'K8S' }
        ];
    }

    saveCs() {
        this.createOutputJson();
        this.csAddJson = JSON.stringify(this.csAddString);
        this.handler.emit(this.csAddJson);
        this.closeDialog();
    }

    //  Create the JSON to be sent to the parent component
    //  The "labels" functions below take into account leading/trailing spaces, multiple spaces between labels, and conversion into an array
    createOutputJson() {
        this.validateJsonStructure();

        let policy;
        if(this.policyContent !== undefined){
            policy = JSON.parse(this.policyContent)
        } else {
            policy = null
        }

        this.csAddString = {
            specContent: JSON.parse(this.compSpecContent),
            policyJson: policy,
            type: this.csAddForm.value['type'].type,
            metadata: {
                labels: this.csAddForm.value['labels'].trim().replace(/\s{2,}/g, ' ').split(" "),
                notes: this.csAddForm.value['notes']
            },
            user: this.username
        };
    }

    //  Validate, catch, display JSON structure error, and quit!
    validateJsonStructure() {
        try {
            JSON.parse(this.compSpecContent);
        } catch (error) {
            this.messageService.add({ key: 'jsonError', severity: 'error', summary: 'Invalid Component Spec JSON', detail: error, sticky: true });
            throw new Error('JSON Structure error, quit!');
        }
        
        if(this.policyContent !== undefined){
            try {
                JSON.parse(this.policyContent);
            } catch (error) {
                this.messageService.add({ key: 'jsonError', severity: 'error', summary: 'Invalid Policy JSON', detail: error, sticky: true });
                throw new Error('JSON Structure error, quit!');
            }
        }
    }

    //  Read the selected Component Spec JSON file
    onCompSpecUpload(event) {
        this.compSpecSelected = event.target.files[0];
        this.readCsFileContent(this.compSpecSelected);
    }
    //Read the selected Component Spec JSON file
    onPolicyUpload(event) {
        this.policySelected = event.target.files[0];
        this.readPolicyFileContent(this.policySelected);
    }

    readCsFileContent(file) {
        if (file) {
            let fileReader = new FileReader();
            fileReader.onload = (e) => { this.compSpecContent = fileReader.result; };
            fileReader.readAsText(file);
        }
    }

    readPolicyFileContent(file) {
        if (file) {
            let fileReader = new FileReader();
            fileReader.onload = (e) => { this.policyContent = fileReader.result; };
            fileReader.readAsText(file);
        }
    }

    //  The handler emits 'null' back to parent to close dialog and make it available again when clicked
    closeDialog() {
        this.visible = false;
        this.handler.emit(null);
    }

}
