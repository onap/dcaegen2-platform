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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PathLocationStrategy, LocationStrategy} from '@angular/common';

import { SharedModule } from './shared/shared-module';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HomeComponent } from './home/home.component';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTreeModule } from '@angular/material/tree';
import { MatCardModule } from '@angular/material/card';
import { MaterialElevationDirective } from './material-elevation.directive';
import { MatProgressSpinnerModule, MatTooltipModule, MatMenuModule } from '@angular/material';
import { OnboardingToolsComponent, SafePipe } from './onboarding-tools/onboarding-tools.component';
import { CompSpecsComponent } from './comp-specs/comp-specs.component';
import { MatTableExporterModule } from 'mat-table-exporter';
import { TableModule } from 'primeng/table';
import { MsInstancesComponent } from './msInstances/msInstances.component';
import { ButtonModule } from 'primeng/button';
import { SidebarModule } from 'primeng/sidebar';
import { MenuModule } from 'primeng/menu';
import { ToolbarModule } from 'primeng/toolbar';
import { PanelMenuModule } from 'primeng/panelmenu';
import { CardModule } from 'primeng/card';
import { LoginComponent } from './login/login.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './register/register.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { JwtInterceptorService } from './services/jwt-interceptor.service';
import { JwtModule } from '@auth0/angular-jwt';
import { UserManagementComponent } from './user-management/user-management.component';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { Ng4LoadingSpinnerModule } from 'ng4-loading-spinner';
import { PasswordModule } from 'primeng/password';
import { TooltipModule } from 'primeng/tooltip';
import { AccordionModule } from 'primeng/accordion';
import { SplitButtonModule } from 'primeng/splitbutton';
import { DropdownModule } from 'primeng/dropdown';
import { FileUploadModule } from 'primeng/fileupload';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MatExpansionModule } from '@angular/material/expansion';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SelectButtonModule } from 'primeng/selectbutton';
import { MessageService } from 'primeng/api';
import { MsAddChangeComponent } from './ms-add-change/ms-add-change.component';
import { MicroservicesComponent } from './microservices/microservices.component';
import { PaginatorModule } from 'primeng/paginator';
import { ScrollPanelModule } from 'primeng/scrollpanel'; 
import { CalendarModule } from 'primeng/calendar';
import { BlueprintsComponent } from './blueprints/blueprints.component';
import { CompSpecAddComponent } from './comp-spec-add/comp-spec-add.component';
import { MsInstanceAddComponent } from './ms-instance-add/ms-instance-add.component';
import {MultiSelectModule} from 'primeng/multiselect';
import {CheckboxModule} from 'primeng/checkbox';
import { InputSwitchModule } from 'primeng/inputswitch';
import { CompSpecValidationComponent } from './comp-spec-validation/comp-spec-validation.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MaterialElevationDirective,
    SafePipe,
    OnboardingToolsComponent,
    CompSpecsComponent,
    MsInstancesComponent,
    LoginComponent,
    RegisterComponent,
    ResetPasswordComponent,
    UserManagementComponent,
    MsAddChangeComponent,
    MicroservicesComponent,
    BlueprintsComponent,
    CompSpecAddComponent,
    MsInstanceAddComponent,
    CompSpecValidationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    SharedModule,
    HttpClientModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatTreeModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableExporterModule,
    TableModule, 
    ButtonModule,
    SidebarModule,
    MenuModule,
    ToolbarModule,
    PanelMenuModule,
    FormsModule,
    ReactiveFormsModule,
    CardModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: ()=>localStorage.getItem('jwt')
      }
    }),
    DialogModule,
    ToastModule,
    Ng4LoadingSpinnerModule,
    TooltipModule,
    AccordionModule, 
    SplitButtonModule,
    DropdownModule,
    FileUploadModule,
    InputTextareaModule,
    MatExpansionModule,
    PasswordModule,
    RadioButtonModule,
    SelectButtonModule,
    MatTooltipModule,
    PaginatorModule,
    ScrollPanelModule,
    MatMenuModule,
    CalendarModule,
    MultiSelectModule,
    CheckboxModule,
    InputSwitchModule
  ],
  providers: [AuthGuard, RoleGuard, {
    provide: HTTP_INTERCEPTORS,
    useClass: JwtInterceptorService,
    multi: true
  }, Location, { provide: LocationStrategy, useClass: PathLocationStrategy }, MessageService],
  bootstrap: [AppComponent]
})
export class AppModule { }
