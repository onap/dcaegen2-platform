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
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {


  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
   
      return this.authService.checkLogin().pipe(map((e:boolean)=>{
        if(e){
          console.log("role guard in");
          this.authService.setUser();
          if(this.authService.getUser().roles.includes("ROLE_ADMIN")){
            this.authService.isAdmin = true;
          }else{
            this.authService.isAdmin = false;
            this.router.navigate(['/home']);
          }
          return this.authService.getUser().roles.includes("ROLE_ADMIN");
        } 
      }), catchError(err=>{
        this.authService.reLoginMsg = true;
        // window.alert("Your login has expired. Please log in again.");
        this.authService.authPass=false;
        this.router.navigate(['/login']);
        return of(false);
      }))
  
}
}
