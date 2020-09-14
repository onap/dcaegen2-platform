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

import { Component, HostBinding, OnInit } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { FlatTreeControl } from '@angular/cdk/tree';
import { Event, Router, RouterOutlet, NavigationStart, NavigationEnd, RouterEvent } from '@angular/router';
import { slider } from './route-animations';
import { AuthService } from './services/auth.service';
import { BreadcrumbService } from './services/breadcrumb.service';
import { environment } from '../environments/environment';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { UserService } from './services/user.service';
import { User } from './models/User';
import { Authority } from './models/Authority.enum';

interface TreeNode {
  name: string;
  children?: TreeNode[];
}

const TREE_DATA: TreeNode[] = [
  {
    name: 'Microservices',
    children: [
      { name: 'Microservices' },
      { name: 'MS Instances' },
      { name: 'Blueprints' },
      { name: 'MOD APIs' }
    ]
  }
];

interface ExampleFlatNode {
  expandable: boolean;
  name: string;
  level: number;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [
    slider
  ]
})
export class AppComponent implements OnInit{

  @HostBinding('@.disabled')
  public animationsDisabled = true;
  activeNode: any;
  resetPasswordFlag: boolean = false;
  resetPasswordForm: FormGroup;
  hidePassword: boolean = true;
  hideConfirmPassword: boolean = true;
  breadcrumbs: any[] = [];

  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData['animation'];
  }

  title = 'dcae-mod-fe';
  right: boolean = true;
  down: boolean = false;

  menu_tree(){
    this.right = !this.right;
    this.down = !this.down;
  }

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  redirectTo(link){
    window.open(link, "_blank");
  }

  redirectToAPIs() {
    window.open(`http://${environment.api_baseURL}:31001/swagger-ui.html#/`, "_blank");
  }

  constructor(private breakpointObserver: BreakpointObserver, private router: Router, public authService: AuthService,
              private bread: BreadcrumbService, private fb: FormBuilder, private userService: UserService) { 
    this.dataSource.data = TREE_DATA;     
  }

  ngOnInit() {
    this.resetPasswordForm = this.fb.group(
        {
          password: ['', [Validators.minLength(6)]],
          confirm_password: ''
        },
        {validators: [this.passwordValidator]}
      );

      //  Subscribe to breadcrumb changes
      this.bread.breadcrumbs$.subscribe( (crumbArray) => {this.breadcrumbs = crumbArray});
  }

  setCrumbs(component, action) {
    this.bread.setBreadcrumbs(component, action)
  }

  passwordValidator(group: FormGroup) {
    if(group.value.password === group.value.confirm_password){
      return null;
    } else {
      return {errMsg: 'Passwords do not match!'};
    }
  }

  private _transformer = (node: TreeNode, level: number) => {
    return {
      expandable: !!node.children && node.children.length > 0,
      name: node.name,
      level: level,
    };
  }

  tree_handler(name, treenode) {
    if (name == "MOD APIs") {
      window.open(`http://${environment.api_baseURL}:31001/swagger-ui.html#/`, "_blank");
    } else if (name == "MS Instances") {
      this.router.navigate(["ms-instances"]);
      this.activeNode = treenode;
    } else if(name == "Microservices") {
      this.router.navigate(["base-microservices"]);
      this.activeNode = treenode;
    } else if(name == "Blueprints") {
      this.router.navigate(["blueprints"]);
      this.activeNode = treenode;
    }
    
  }

  treeControl = new FlatTreeControl<ExampleFlatNode>(
    node => node.level, node => node.expandable);

  treeFlattener = new MatTreeFlattener(
    this._transformer, node => node.level, node => node.expandable, node => node.children);

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

  hasChild = (_: number, node: ExampleFlatNode) => node.expandable;

  showMsMenu = false;
  msMenuIconRight = true;
  msMenu() {
    this.showMsMenu = !this.showMsMenu
    this.msMenuIconRight = !this.msMenuIconRight
  }

  showUtilitiesMenu = false;
  utilitiesMenuIconRight = true;
  utilitiesMenu(){
    this.showUtilitiesMenu = !this.showUtilitiesMenu
    this.utilitiesMenuIconRight = !this.utilitiesMenuIconRight
  }

  onConfirm() {
    this.authService.reLoginMsg = false;
  }

  handleLogout() {
    this.showMsMenu = false
    this.authService.logout()
  }

  handleProfile() {
    console.log(this.authService.getUser().roles);
    this.resetPasswordFlag = true;
  }

  closeResetDialog() {
    this.resetPasswordForm.reset();
    this.resetPasswordFlag = false;
  }

  submitReset() {
    if(this.authService.getUser().roles.includes(Authority.ADMIN)){
      this.userService.editUser(this.authService.getUser().username, this.resetPasswordForm.value as User).subscribe(res=>{
        alert("Password reset successful. Please login in using the new credentials.");
        this.authService.logout(); 
    }, (err)=>{
      alert(err.error.message);
     
    });
    } else {
      this.userService.editProfile(this.authService.getUser().username, this.resetPasswordForm.value as User).subscribe(res=>{
        alert("Password reset successful. Please login in using the new credentials.");
        this.authService.logout();
      }, (err)=>{
        alert(err.error.message);
      });
    }  
     this.resetPasswordForm.reset();
     this.resetPasswordFlag = false;
  }

}
