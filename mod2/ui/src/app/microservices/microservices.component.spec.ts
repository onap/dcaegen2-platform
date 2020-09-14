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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { JwtHelperService, JWT_OPTIONS } from '@auth0/angular-jwt';
import { Ng4LoadingSpinnerModule } from 'ng4-loading-spinner';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { MsAddChangeComponent } from '../ms-add-change/ms-add-change.component';
import { MsInstanceAddComponent } from '../ms-instance-add/ms-instance-add.component';

import { MicroservicesComponent } from './microservices.component';

describe('MicroservicesComponent', () => {
  let component: MicroservicesComponent;
  let fixture: ComponentFixture<MicroservicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        MicroservicesComponent,
        MsAddChangeComponent,
        MsInstanceAddComponent,
      ],
      imports: [
        Ng4LoadingSpinnerModule,
        TableModule,
        MatMenuModule,
        ScrollPanelModule,
        ToastModule,
        DialogModule,
        DropdownModule,
        FormsModule,
        ReactiveFormsModule,
        ButtonModule,
        CalendarModule,
        HttpClientTestingModule,
        ToastModule,
        RouterTestingModule
      ],
      providers: [
        MessageService,
        { provide: JWT_OPTIONS, useValue: JWT_OPTIONS },
        JwtHelperService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MicroservicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should fill msInstances Object`, () => {
    const fixture = TestBed.createComponent(MicroservicesComponent);
    const app = fixture.debugElement.componentInstance;

    let mockMicroservice = [{
      id: 'testId1234',
      name: 'test-MS',
      tag: 'test-MS-tag',
      serviceName: 'testServiceName',
      type: 'testType',
      location: 'TestLocation',
      namespace: 'testNameSpace',
      status: 'testStatus',
      metadata: {
        createdBy: 'test',
        createdOn: '01-01-2020 12:00',
        updatedBy: 'test',
        updatedOn: '01-01-2020 12:00',
        notes: 'test',
        labels: ['test'],
      },
      msInstances: 'test'
    }]

    app.fillTable(mockMicroservice)

    expect(app.loadTable).toEqual(true);
    expect(app.msElements.length).toEqual(1);
  });

  it(`should set addOrChange to "Add"`, () => {
    const fixture = TestBed.createComponent(MicroservicesComponent);
    const app = fixture.debugElement.componentInstance;

    let mockRowData = null
    app.showAddChangeDialog(mockRowData)

    expect(app.addOrChange).toEqual('Add');
  });

  it(`should set addOrChange to "Change"`, () => {
    const fixture = TestBed.createComponent(MicroservicesComponent);
    const app = fixture.debugElement.componentInstance;

    let mockRowData = {field: 'test'}
    app.showAddChangeDialog(mockRowData)

    expect(app.addOrChange).toEqual('Change');
  });
});
