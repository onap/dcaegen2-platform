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

@Injectable({
  providedIn: 'root'
})
export class GlobalFiltersService {

  //global filter values
  instanceName: string;
  instanceTag: string;
  release: string;
  status: string;

  shouldFilter: boolean = false;

  constructor() { }

  setFilters(filters){
    this.instanceName = filters.instanceName
    this.instanceTag = filters.instanceTag
    this.release = filters.release
    this.status = filters.status
  }

  getFilters(){
    let globalFilters = {instanceName: this.instanceName, instanceTag: this.instanceTag, release: this.release, status: this.status}
    return globalFilters
  }

  checkShouldFilter(){
    return this.shouldFilter
  }

  setShouldFilter(){
    this.shouldFilter = !this.shouldFilter
  }
}
