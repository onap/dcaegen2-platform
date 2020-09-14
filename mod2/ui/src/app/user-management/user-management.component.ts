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

import { Component, OnInit, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { User } from '../models/User';
import { UserService } from '../services/user.service';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SelectItem } from 'primeng/api';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  users: User[] = [];
  editUser: User = {
    username:'',
    fullName:'',
    roles: []
  };
  editUserFlag: boolean = false;
  editUserForm: FormGroup;
  rolesFromBackend = [];
  selectedRoles : Array<String>= [];

  constructor(private userService: UserService, private router: Router, private fb: FormBuilder, private authService: AuthService) { }

  ngOnInit() {
      this.userService.getUsers().subscribe((res: User[]) => {
       this.users = res;
       this.users.map(user=>{
         let tempRoles = [];
         user.roles.map(role=>{
           tempRoles.push(role.name.substring(5));
         });
         user.roles = tempRoles;
       });
       
      });
      this.editUserForm = this.fb.group({
        username: '',
        fullName: '',
        password: [null, [ Validators.minLength(6)]],
        roles: [this.selectedRoles, [Validators.required]]
      });
      this.userService.getRoles().subscribe(res=>{
        Object.values(res).forEach(ele=>{
         this.rolesFromBackend.push({label:ele.substring(5), value:ele});
        });
       });
  }

  handleDelete(username) {
    const result = window.confirm('Are you sure to delete this user?');
    if(result === true) {
      this.userService.deleteUser(username).subscribe(res=>{
        alert(res.message);
          this.userService.getUsers().subscribe( r => {
             this.users = r; 
             this.users.map(user=>{
              let tempRoles = [];
              user.roles.map(role=>{
                tempRoles.push(role.name.substring(5));
              });
              user.roles = tempRoles;
            });
      });
     });
    } 
  }

  handleEdit(user) {
    this.selectedRoles = [];
    this.editUserFlag = true;
    this.editUser = user;
    this.editUserForm.get('username').setValue(user.username);
    this.editUserForm.get('fullName').setValue(user.fullName);
    user.roles.map(ele => {
       let temp = "ROLE_" + ele;
       this.selectedRoles.push(temp);
    })

     this.editUserForm.get('roles').setValue(this.selectedRoles);

  }

  closeEditDialog() {
    this.editUserForm.reset();
    this.editUserFlag = false;
  }

  generateNewPassword() {
    this.editUserForm.value.password = '';
    const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&';
    var array = new Uint32Array(32);
    window.crypto.getRandomValues(array);
    for(let i=0;i<32;i++) {
      const index = Math.floor(array[i] % chars.length);
      this.editUserForm.value.password += chars.charAt(index);
    }
    this.editUserForm.patchValue({password: this.editUserForm.value.password});
  }

  submitEdit(user) {
    this.editUserFlag = false;
    console.log(this.editUserForm.value.fullName);
    let tempUser = this.editUserForm.value as User;
    console.log(tempUser);
    this.userService.editUser(user.username, this.editUserForm.value as User).subscribe(res=>{
        alert("User information updated successfully.");
        this.userService.getUsers().subscribe( r => {
        this.users = r; 
        this.users.map(user=>{
         let tempRoles = [];
         user.roles.map(role=>{
           tempRoles.push(role.name.substring(5));
         });
         user.roles = tempRoles;
       });
       }, (err)=>{
          // alert(err.error.message);
          alert("Sorry but your credentials are out of date. Please log in again to resolve this.");
          this.authService.logout();
       });   
    }, (err)=>{
      alert(err.error.message);
    });
  }
  
}
