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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RadioButtonModule } from 'primeng/radiobutton';

import { CompSpecValidationComponent } from './comp-spec-validation.component';
import { FormsModule } from '@angular/forms';
import { MatCardModule, MatTooltipModule } from '@angular/material';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MessageService } from 'primeng/api';
import { JwtHelperService, JWT_OPTIONS } from '@auth0/angular-jwt';
import { Ng4LoadingSpinnerModule } from 'ng4-loading-spinner';

describe('CompSpecValidationComponent', () => {
  let component: CompSpecValidationComponent;
  let fixture: ComponentFixture<CompSpecValidationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CompSpecValidationComponent ],
      imports: [ 
        RadioButtonModule, 
        FormsModule, 
        MatCardModule,
        MatTooltipModule,
        ScrollPanelModule,
        HttpClientTestingModule,
        Ng4LoadingSpinnerModule
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
    fixture = TestBed.createComponent(CompSpecValidationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should have as shouldValidate 'false'`, () => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.shouldValidate).toEqual(false);
  });

  it(`should change shouldValidate to 'true'`, async(() => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    app.validateRadioButton()
    fixture.detectChanges();
    expect(app.shouldValidate).toEqual(true);
  }));

  it(`should have as shouldDownload 'false'`, () => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.shouldDownload).toEqual(false);
  });  

  it(`should change shouldDownload to 'true'`, async(() => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    app.downloadRadioButton()
    fixture.detectChanges();
    expect(app.shouldDownload).toEqual(true);
  }));

  it(`should set validation error message`, async(() => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    let mockSuccess = {
      status: 200
    }
    app.setSpecValidationMessage(mockSuccess)
    fixture.detectChanges();
    expect(app.validCompSpec).toEqual(true)
  }));

  it(`should set validation error message`, async(() => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;

    let mockError = {
      status: 400,
      error: {
        summary: 'Test',
        errors: [
          'error1',
          'error2'
        ]
      }
    }
    app.setSpecValidationMessage(mockError)
    fixture.detectChanges();
    expect(app.validCompSpec).toEqual(false)
  }));

  it(`should invalidate JSON structure`, async(() => {
    const fixture = TestBed.createComponent(CompSpecValidationComponent);
    const app = fixture.debugElement.componentInstance;
    let mockErrorJson = "test: 'test}"
    app.compSpecContent = mockErrorJson
    expect(() => app.validateJsonStructure()).toThrowError('JSON Structure error, quit!')  
  }));

});
