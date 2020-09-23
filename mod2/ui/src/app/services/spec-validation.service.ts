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

import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SpecValidationService {

  private URL: string = 'http://zlecdyh2adcc1s2dokr04.1463c9.dyh2a.tci.att.com:31002/';

  constructor(private http: HttpClient) { }

  sendSpecFile(specContent, type, release){

    let url = ''

    if(release === "2007"){
      url = `${this.URL}v6/api/comp_spec_validator/`
    }

    let body = {
      compspec_content: specContent,
      type: type
    }

    return this.http.post(url, body)
  }

  getSchema(type){
    let url = `${this.URL}v6/api/schema/${type}`

    return this.http.get(url)
  }
}
