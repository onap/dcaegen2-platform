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

import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { User } from '../models/User';
import { AuthResponse } from '../models/AuthResponse';
import {SelectItem} from 'primeng/api';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  user: User = {
    username:'',
    fullName:'',
    password:'',
    roles: []
  };
  form: FormGroup;
  roleOptions: SelectItem[];
  rolesFromBackend = [];

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private userService: UserService) { }

  ngOnInit() {
    this.form = this.fb.group({
        username: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(10)]],
        fullName: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        roles:['', [Validators.required]]
    });
    // this.roleOptions = [{label:'Admin', value:{name:'ROLE_ADMIN'}}, {label:'User', value:{name:'ROLE_USER'}}, {label:'ScrumLead', value:{name:'ROLE_SCRUMLEAD'}}, {label:'Developer', value:{name:'ROLE_DEVELOPER'}}, {label:'PST', value:{name:'ROLE_PST'}}, {label:'ETE', value:{name:'ROLE_ETE'}}, {label:'Ops', value:{name:'ROLE_OPS'}}];
    this.userService.getRoles().subscribe(res=>{
     Object.values(res).forEach(ele=>{
      this.rolesFromBackend.push({label:ele.substring(5), value:ele});
     });
    });
  }

  generateNewPassword() {
    this.form.value.password = '';
    const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&';
    var array = new Uint32Array(32);
    window.crypto.getRandomValues(array);
    for(let i=0;i<32;i++) {
      const index = Math.floor(array[i] % chars.length);
      this.form.value.password += chars.charAt(index);
    }
    this.form.patchValue({password: this.form.value.password});
  }

  cancel() {
    const result = window.confirm("Are you sure to quit registering current user and go back to user management?");
    if(result === true){
      this.router.navigate(['/users']);
    }  
  }

  submit() {
    this.user.fullName = this. form.value.fullName;
    this.user.username = this.form.value.username;
    this.user.password = this.form.value.password;
    this.user.roles = this.form.value.roles;
    console.log(this.user.roles);
    this.authService.register(this.user) 
    .subscribe( (res: AuthResponse) => {
          alert(res.message);
          this.router.navigate(['/users']);
      }, (err) => {
        console.log(err);
        alert(err.error.message);
        this.form.reset();
      }
  ); 
  }

}
