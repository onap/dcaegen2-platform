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
import { MatMenuModule, MatTooltipModule } from '@angular/material';
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
import { CompSpecAddComponent } from '../comp-spec-add/comp-spec-add.component';
import { MsInstanceAddComponent } from '../ms-instance-add/ms-instance-add.component';

import { MsInstancesComponent } from './msInstances.component';

describe('MsInstancesComponent', () => {
  let component: MsInstancesComponent;
  let fixture: ComponentFixture<MsInstancesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        MsInstancesComponent,
        CompSpecAddComponent,
        MsInstanceAddComponent
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
        RouterTestingModule,
        MatTooltipModule
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
    fixture = TestBed.createComponent(MsInstancesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should fill msInstances Object`, () => {
    const fixture = TestBed.createComponent(MsInstancesComponent);
    const app = fixture.debugElement.componentInstance;

    let mockMsInstance = [{
      id: 'testId1234',
      name: 'test-MS',
      release: '2008',
      version: '1.0.0',
      status: 'New',
      msInfo: {
        id: 'testBaseMsId1234',
        name: 'test Base Ms',
        tag: 'test-MS-tag',
      },
      metadata: {
        scrumLead: 'test',
        scrumLeadId: 'testId',
        systemsEngineer: 'test',
        systemsEngineerId: 'testId',
        developer: 'test',
        developerId: 'testId',
        pstDueDate: '01-01-2020 12:00',
        pstDueIteration: '1.1',
        eteDueDate: '01-01-2020 12:00',
        eteDueIteration: '1.1',
        createdBy: 'test',
        createdOn: '01-01-2020 12:00',
        updatedBy: 'test',
        updatedOn: '01-01-2020 12:00',
        notes: 'test',
        labels: ['test'],
      },
      activeSpec: 'test'
    }]

    app.fillTable(mockMsInstance)

    expect(app.loadTable).toEqual(true);
    expect(app.msInstances.length).toEqual(1);
  });
});
