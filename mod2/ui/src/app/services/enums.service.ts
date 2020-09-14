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
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EnumsService {

  private url: string = `http://${environment.api_baseURL}:31001/api/`

  constructor(private http: HttpClient) { }

  getStatuses(){
    //return this.http.get(this.url + 'enums')
    let states = [
      { field: 'in_dev', label: 'To In Dev', updatedLabel: 'IN_DEV' },
      { field: 'not_needed', label: 'To Not Needed', updatedLabel: 'NOT_NEEDED' },
      { field: 'dev_complete', label: 'To Dev Complete', updatedLabel: 'DEV_COMPLETE' },
      { field: 'in_pst', label: 'To In PST', updatedLabel: 'IN_PST' },
      { field: 'pst_failed', label: 'To PST Failed', updatedLabel: 'PST_FAILED' },
      { field: 'pst_certified', label: 'To PST Certified', updatedLabel: 'PST_CERTIFIED' },
      { field: 'in_ete', label: 'To In ETE', updatedLabel: 'IN_ETE' },
      { field: 'ete_failed', label: 'To ETE Failed', updatedLabel: 'ETE_FAILED' },
      { field: 'ete_certified', label: 'To ETE Certified', updatedLabel: 'ETE_CERTIFIED' },
      { field: 'in_prod', label: 'To In Prod', updatedLabel: 'IN_PROD' },
      { field: 'prod_failed', label: 'To Prod Failed', updatedLabel: 'PROD_FAILED'
      }]
    return states
  }
}
