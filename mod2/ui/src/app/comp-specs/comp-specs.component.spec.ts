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

import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule, MatTooltipModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { JwtHelperService, JWT_OPTIONS } from '@auth0/angular-jwt';
import { Ng4LoadingSpinnerModule } from 'ng4-loading-spinner';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';

import { CompSpecsComponent } from './comp-specs.component';

describe('CompSpecsComponent', () => {
  let component: CompSpecsComponent;
  let fixture: ComponentFixture<CompSpecsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        CompSpecsComponent
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
        HttpClientModule,
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
    fixture = TestBed.createComponent(CompSpecsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should fill csElements Object`, () => {
    const fixture = TestBed.createComponent(CompSpecsComponent);
    const app = fixture.debugElement.componentInstance;

    let mockCsElement = [{
      id: 'testId1234',
      name: 'test-MS',
      type: 'k8s',
      specContent: '',
      policyJson: 'test',
      status: 'New',
      msInstanceInfo: {
        release: '2008',
        name: 'test Ms',
      },
      metadata: {
        createdBy: 'test',
        createdOn: '01-01-2020 12:00',
        updatedBy: 'test',
        updatedOn: '01-01-2020 12:00',
        notes: 'test',
        labels: ['test'],
      }
    }]

    app.fillTable(mockCsElement)

    expect(app.loadTable).toEqual(true);
    expect(app.csElements.length).toEqual(1);
  });

  it(`should set spec content to view`, () => {
    const fixture = TestBed.createComponent(CompSpecsComponent);
    const app = fixture.debugElement.componentInstance;
    let mockData = {
      specContent: 'test'
    }
    app.showViewCsDialog(mockData)
    expect(app.showViewCs).toEqual(true);
    expect(app.specContentToView).toEqual('test');    
  });

  it(`should set policy json content to view`, () => {
    const fixture = TestBed.createComponent(CompSpecsComponent);
    const app = fixture.debugElement.componentInstance;
    let mockData = {
      policyJson: 'test'
    }
    app.showViewPolicyDialog(mockData)
    expect(app.showViewPolicy).toEqual(true);
    expect(app.policyJsonToView).toEqual('test');
  });  
});

