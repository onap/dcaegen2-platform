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
import { compSpecsService } from '../services/comp-specs-service.service';
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { JsonPipe } from '@angular/common';

@Component({
  selector: 'app-view-comp-specs',
  templateUrl: './view-comp-specs.component.html',
  styleUrls: ['./view-comp-specs.component.css']
})
export class ViewCompSpecsComponent implements OnInit {

  @Input() msInstanceId: string;
  @Input() msName: string;
  @Input() msRelease: string;
  @Output() handler: EventEmitter<any> = new EventEmitter();

  componentSpecs: any[] = []
  visible: boolean = false;

  constructor(private csApis: compSpecsService, private spinnerService: Ng4LoadingSpinnerService) { }

  ngOnInit() {
    this.spinnerService.show()
    this.getCompSpecs(this.msInstanceId)
  }

  getCompSpecs(msInstanceId){
    this.componentSpecs = []

    this.csApis.getAllCompSpecs(msInstanceId).subscribe((response) => {
      this.fillCompSpecsObject(response)
    }, (errResponse) => {
      this.handler.emit(errResponse)
      this.spinnerService.hide()
    })
  }

  fillCompSpecsObject(compSpecs){
    for(let elem of compSpecs){

      this.componentSpecs.unshift({
        id: elem.id,
        metadata: elem.metadata,
        msInstanceInfo: elem.msInstanceInfo,
        specContent: elem.specContent,
        policyJson: elem.policyJson,
        status: elem.status,
        type: elem.type,
        user: elem.user
      })
    }
    this.visible=true;
    this.spinnerService.hide()
  }

  /* * * * On click of cancel * * * */
  closeDialog() {
    this.visible = false;
    this.handler.emit(null)
  }
}
