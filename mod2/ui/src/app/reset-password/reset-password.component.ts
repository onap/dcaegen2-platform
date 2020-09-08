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

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder, public authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
        id: ['', [Validators.required]],
        password: '',
        newPassword: '',
        confirm_newPassword: ''
    });
  }

  cancel() {
    this.form.reset();
  }

  generateNewPassword() {
    this.form.value.password = '';
    let p = '';
    const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&';
    for(let i=1;i<12;i++) {
      const index = Math.floor(Math.random() * chars.length + 1);
      this.form.value.password += chars.charAt(index);
    }
    this.form.patchValue({password: this.form.value.password});
  }

  submit() {
    const id = this.form.value.id;
    if(!this.authService.isAdmin){
    if(this.form.value.newPassword === this.form.value.confirm_newPassword){
      const password = this.form.value.newPassword;
      console.log(id);
      console.log(password); // toJane: need to call user API
    } else {
      alert('Your passwords do not match, please re-confirm!');
      this.form.patchValue({newPassword: '', confirm_newPassword: ''});
    }
  } else {
     const password = this.form.value.password;
     console.log(id);
     console.log(password);// toJane: need to call user API
  }
}

}
