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
import { HttpClient, HttpHandler, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/internal/operators';
import { User } from '../models/User';
import { JwtHelperService } from '@auth0/angular-jwt';
import { AuthResponse } from '../models/AuthResponse';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService{

 private user: User = {
   username: '',
   roles:[]
 };
 authPass=false;
 isAdmin=false;
 reLoginMsg = false;
 
  constructor(
    private http:HttpClient,
    private jwtHelper: JwtHelperService,
    private router: Router
    ) { 
      
    }

  register(user: User): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`http://${environment.api_baseURL}:31003/api/auth/signup`, user);
    // return this.http.post<AuthResponse>(`http://localhost:8082/api/auth/signup`, user);

  }

  login(user: User): Observable<AuthResponse> {
      return this.http.post<AuthResponse>(`http://${environment.api_baseURL}:31003/api/auth/signin`, user)
      .pipe(
       tap((res: AuthResponse) => {
        localStorage.setItem('token', res.token);
        this.setUser();
        this.authPass = true;
       })
      );
  }

  setUser() {
    this.user.username = this.jwtHelper.decodeToken(localStorage.getItem('token')).sub;
    this.user.roles = this.jwtHelper.decodeToken(localStorage.getItem('token')).roles;
    this.user.fullName = this.jwtHelper.decodeToken(localStorage.getItem('token')).fullName;
    
  }

  checkLogin(): Observable<boolean>{
    return this.http.post<boolean>(`http://${environment.api_baseURL}:31003/api/auth/validate-token`, null);
  }

  getJwt() {
    if(localStorage.getItem('token')){
      return localStorage.getItem('token');
    }
    return "";
  }

  logout() {
    localStorage.removeItem('token');
    this.authPass = false;
    this.router.navigate(['/login']);
  }

  getUser(): User {
    return this.user;
  }

}
