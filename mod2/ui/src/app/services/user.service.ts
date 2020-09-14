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
import { Observable } from 'rxjs';
import { User } from '../models/User';
import { AuthResponse } from '../models/AuthResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`http://${environment.api_baseURL}:31003/api/users/getAll`);
  }

  editUser(username: string, user: User): Observable<any>{
    return this.http.patch<any>(`http://${environment.api_baseURL}:31003/api/users/admin/${username}`, user);
  }

  editProfile(username: string, user: User): Observable<any>{
    return this.http.patch<any>(`http://${environment.api_baseURL}:31003/api/users/user/${username}`, user);
  }

  deleteUser(username: string): Observable<{message:string}> {
    return this.http.delete<{message: string}>(`http://${environment.api_baseURL}:31003/api/users/${username}`);
  }

  getRoles() {
    return this.http.get(`http://${environment.api_baseURL}:31003/api/roles`);
  }

}
