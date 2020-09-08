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

import { NgModule } from '@angular/core';
import { Routes, RouterModule, NavigationStart, NavigationEnd, Route} from '@angular/router';
import { HomeComponent } from './home/home.component';
import { OnboardingToolsComponent } from './onboarding-tools/onboarding-tools.component';
import { CompSpecsComponent } from './comp-specs/comp-specs.component';
import { MsInstancesComponent } from './msInstances/msInstances.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { UserManagementComponent } from './user-management/user-management.component';
import { LoginGuard } from './guards/login.guard';
import { MicroservicesComponent } from './microservices/microservices.component';
import { BlueprintsComponent } from './blueprints/blueprints.component';
import { CompSpecValidationComponent } from './comp-spec-validation/comp-spec-validation.component';

const routes: Routes = [
  { path: '',  component: HomeComponent, data: { animation: 'home' }, canActivate:[LoginGuard]},
  { path: 'home', component: HomeComponent, data: { animation: 'home' }, canActivate:[AuthGuard] },
  { path: 'login', component: LoginComponent, canActivate:[LoginGuard]},
  { path: 'register', component: RegisterComponent, canActivate:[AuthGuard, RoleGuard] },
  { path: 'reset-password', component: ResetPasswordComponent, canActivate:[AuthGuard]},
  { path: 'users', component: UserManagementComponent, canActivate:[AuthGuard, RoleGuard]},
  { path: 'OnboardingTools', component: OnboardingToolsComponent, data: { animation: 'iframe' }, canActivate:[AuthGuard] },
  { path: 'CompSpecs', component: CompSpecsComponent, data: { animation: 'CompSpecs' }, canActivate:[AuthGuard]},
  { path: 'ms-instances', component: MsInstancesComponent, data: { animation: 'ms-instances' }, canActivate:[AuthGuard] },
  { path: 'base-microservices', component: MicroservicesComponent, data: { animation: 'base-ms' }, canActivate: [AuthGuard] },
  { path: 'blueprints', component: BlueprintsComponent, data: { animation: 'BPs' }, canActivate: [AuthGuard] },
  { path: 'spec-validator', component: CompSpecValidationComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
