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
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { BlueprintsComponent } from './blueprints.component';

describe('BlueprintsComponent', () => {
  let component: BlueprintsComponent;
  let fixture: ComponentFixture<BlueprintsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BlueprintsComponent],
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
        HttpClientTestingModule,
        ToastModule,
        RouterTestingModule,
        MatTooltipModule,
        BrowserAnimationsModule
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
    fixture = TestBed.createComponent(BlueprintsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(`should set states`, async(() => {
    const fixture = TestBed.createComponent(BlueprintsComponent);
    const app = fixture.debugElement.componentInstance;
    let mockStates = [
      'state1', 
      'state2'
    ]
    app.setMenuStates(mockStates)
    fixture.detectChanges();
    expect(app.states).toEqual([ ]);
  }));

  it(`should not enable action buttons`, async(() => {
    const fixture = TestBed.createComponent(BlueprintsComponent);
    const app = fixture.debugElement.componentInstance;
    
    app.selectedBPs = []
    app.enableButtonCheck()
    fixture.detectChanges();

    expect(app.canDownload).toEqual(false);
    expect(app.canUpdate).toEqual(false);
    expect(app.canDelete).toEqual(false);
  }));

  it(`should enable download/update buttons but not delete`, async(() => {
    const fixture = TestBed.createComponent(BlueprintsComponent);
    const app = fixture.debugElement.componentInstance;

    app.selectedBPs = [{status: 'TEST'}]
    app.enableButtonCheck()
    fixture.detectChanges();

    expect(app.canDownload).toEqual(true);
    expect(app.canUpdate).toEqual(true);
    expect(app.canDelete).toEqual(false);
  }));

  it(`should enable download/update buttons but not delete`, async(() => {
    const fixture = TestBed.createComponent(BlueprintsComponent);
    const app = fixture.debugElement.componentInstance;

    app.selectedBPs = [{ status: 'IN_DEV' }]
    app.enableButtonCheck()
    fixture.detectChanges();

    expect(app.canDownload).toEqual(true);
    expect(app.canUpdate).toEqual(true);
    expect(app.canDelete).toEqual(true);
  }));

  it(`should enable download/update buttons but not delete`, async(() => {
    const fixture = TestBed.createComponent(BlueprintsComponent);
    const app = fixture.debugElement.componentInstance;

    let mockBpToView = {
      tag: 'test-tag',
      type: 'k8s',
      instanceRelease: '2008',
      version: '1',
      content: 'test'
    }

    app.viewBpContent(mockBpToView)
    fixture.detectChanges();

    expect(app.BpFileNameForDownload).toEqual('test-tag_k8s_2008_1');
    expect(app.BpContentToView).toEqual('test');
    expect(app.showBpContentDialog).toEqual(true);
  }));
  
});


