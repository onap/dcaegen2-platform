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
import { User } from '../models/User';
import { Router } from '@angular/router';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {

  form: FormGroup;
  hide: boolean=true;

    constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) { }

    ngOnInit() {
        this.form = this.fb.group({
            username: ['', [Validators.required]],
            // uid: ['', [Validators.required]],
            password: ['', [Validators.required]]
        });
    }

    submit() {
        this.authService.login(this.form.value as User).subscribe(
            res => {
                // if (this.authService.getUser().roles && this.authService.getUser().roles.includes("ROLE_USER")) {
                //     this.authService.isAdmin = false;
                // }
                if(this.authService.getUser().roles &&this.authService.getUser().roles.includes("ROLE_ADMIN")) {
                    this.authService.isAdmin = true;
                } else {
                    this.authService.isAdmin = false;
                }
                this.router.navigate(['/home']);
            }, 
            (err) => {
                alert('User or Password is not correct, please re-enter');
                this.form.reset();
            }
        );
    }
}
