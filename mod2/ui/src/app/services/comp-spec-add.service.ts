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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class CompSpecAddService {

  private URL: string = `http://${environment.api_baseURL}:31001/api/specification/`;

  constructor(private http: HttpClient) { }

  addCsToCatalog(msInstanceId: string, addCsJson: any): Observable<any> {
    let url = this.URL + msInstanceId;
    let body = addCsJson;
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    let options = {headers:headers}
    return this.http.post<any>(url, body, options);
  }

}
