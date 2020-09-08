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

import { Component, ViewEncapsulation, ViewChild, ElementRef, PipeTransform, Pipe, OnInit } from '@angular/core';
import { DomSanitizer } from "@angular/platform-browser";
import { Ng4LoadingSpinnerService } from 'ng4-loading-spinner';
import { environment } from '../../environments/environment';

@Pipe({ name: 'safe' })
export class SafePipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) { }
  transform(url) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
}

@Component({
  selector: 'app-onboarding-tools',
  templateUrl: './onboarding-tools.component.html',
  styleUrls: ['./onboarding-tools.component.css']
})

export class OnboardingToolsComponent implements OnInit {

  title = 'Onboarding Tools';

  //video: string = `http://${environment.api_baseURL}:30991/onboarding-toolbox/blueprint-generator`
  video: string = 'http://dcae-onboarding-toolbox-fe.ecomp.idns.cip.att.com:30991/onboarding-toolbox/blueprint-generator'

  constructor(private spinnerService: Ng4LoadingSpinnerService) { 
    this.spinnerService.show();
  }

  ngOnInit() {
  }

}
