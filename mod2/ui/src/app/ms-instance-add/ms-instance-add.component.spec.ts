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

import { DatePipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { JwtHelperService, JWT_OPTIONS } from '@auth0/angular-jwt';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';

import { MsInstanceAddComponent } from './ms-instance-add.component';

describe('MsInstanceAddComponent', () => {
  let component: MsInstanceAddComponent;
  let fixture: ComponentFixture<MsInstanceAddComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MsInstanceAddComponent],
      imports: [
        DialogModule,
        DropdownModule,
        ToastModule,
        FormsModule,
        ReactiveFormsModule,
        ButtonModule,
        HttpClientTestingModule,
        RouterTestingModule,
        CalendarModule,
      ],
      providers: [
        MessageService,
        DatePipe,
        { provide: JWT_OPTIONS, useValue: JWT_OPTIONS },
        JwtHelperService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MsInstanceAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
