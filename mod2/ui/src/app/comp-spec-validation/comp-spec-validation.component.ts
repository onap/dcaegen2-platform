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
import { SpecValidationService } from '../services/spec-validation.service';
import { DownloadService } from '../services/download.service';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';

@Component({
  selector: 'app-comp-spec-validation',
  templateUrl: './comp-spec-validation.component.html',
  styleUrls: ['./comp-spec-validation.component.css']
})
export class CompSpecValidationComponent implements OnInit {

  @ViewChild('myFile', {static: false})
  myInputVariable: ElementRef;

  spec_validator_action;
  release = '';
  type = '';
  shouldValidate = false;
  shouldDownload = false;
  validCompSpec = false;
  specValidated = false;
  specValidationOutputSummary: any;
  
  constructor(private specValidator: SpecValidationService, private downloadService: DownloadService, private spinnerService: Ng4LoadingSpinnerService ) { }

  ngOnInit() {

  }

  compSpecSelected: any;
  onCompSpecUpload(event){
    this.compSpecSelected = event.target.files[0];
    this.readCsFileContent(this.compSpecSelected);
  }

  compSpecContent: any = null;
  readCsFileContent(file) {
    if (file) {
      let fileReader = new FileReader();
      fileReader.onload = (e) => { this.compSpecContent = fileReader.result; };
      fileReader.readAsText(file);
    }
  }

  validateRadioButton(){
    this.shouldValidate = true;
    this.shouldDownload = false;
  }

  downloadRadioButton(){
    this.shouldValidate = false;
    this.shouldDownload = true;
    this.compSpecContent = null;
    this.specValidated = false
  }

  resetFile(){
    this.myInputVariable.nativeElement.value = "";
    this.compSpecContent = null
    this.compSpecSelected = null
    this.specValidated = false
  }

  specValidationOutputHeader = ''
  specValidationOutputMessage = '';
  validateSpec(){
    this.specValidationOutputHeader = ""
    this.specValidationOutputMessage = ""
    this.specValidationOutputSummary = ""

    this.spinnerService.show()
    this.validateJsonStructure()
    this.specValidator.sendSpecFile(this.compSpecContent, this.type, this.release).subscribe(
      res => {}, err => {
        this.setSpecValidationMessage(err)
      }
    )
  }

  setSpecValidationMessage(res){
    if(res.status === 200){
      this.specValidationOutputHeader = "Success: Valid Component Spec"
      this.specValidationOutputMessage = ""
      this.validCompSpec = true
    } else {
      this.specValidationOutputHeader = `${res.status} Error: Invalid Component Spec`
      this.specValidationOutputSummary = res.error.summary
      
      for(let item of res.error.errors){
        this.specValidationOutputMessage += `- ${item}\n\n`
      }

      this.validCompSpec = false
    }

    this.specValidated = true;
    this.spinnerService.hide()
  }

  validateJsonStructure() {
    try {
      JSON.parse(this.compSpecContent);
    } catch (error) {
      this.specValidationOutputHeader = "Error: Invalid Component Spec"
      this.specValidationOutputSummary = "JSON Structure Error"
      this.specValidationOutputMessage = error
      this.validCompSpec = false
      this.specValidated = true
      this.spinnerService.hide()
      throw new Error('JSON Structure error, quit!');
    }
  }

  downloadSchema(){
    this.spinnerService.show()
    this.specValidator.getSchema(this.type).subscribe(
      res => {
        this.downloadService.downloadJSON(res, `${this.release}+_${this.type}_Schema`)
        this.spinnerService.hide()
      }, err => {
        console.log(err)
        this.spinnerService.hide()
      }
    )
  }
}
