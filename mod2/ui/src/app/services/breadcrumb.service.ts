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

import { Injectable } from '@angular/core';
import { Observable, of, Subject, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class BreadcrumbService {

    breadcrumbs: any[] = [];

    breadcrumbs$: Subject<any[]> = new BehaviorSubject([]);
    
    constructor() {
        this.setBreadcrumbs('', "reset");
    }

    setBreadcrumbs(component: string, action: string) {
        if (action == "reset") {
            this.breadcrumbs = [];
            this.breadcrumbs.push({ page: 'Home', link: '/home' });
        }

        //  If the breadcrumb item is clicked, remove evwrything to the right of the clicked item
        if (action == "crumbClicked") {
            const pos = this.breadcrumbs.map(function(crumb) { return crumb.page }).indexOf(component);
            for (1; this.breadcrumbs.length -1 -pos; 1) {
                this.breadcrumbs.pop()
            }
        } else {    // Add the component that was selected
            if (component == 'Microservices') {
                this.breadcrumbs.push({ page: 'Microservices', link: '/base-microservices' });
            } else if (component == 'MS Instances') {
                this.breadcrumbs.push({ page: 'MS Instances', link: '/ms-instances' });
            } else if (component == 'Blueprints') {
                this.breadcrumbs.push({ page: 'Blueprints', link: '/blueprints' });
            } else if (component == 'Component Specs') {
                this.breadcrumbs.push({ page: 'Component Specs', link: '/CompSpecs' });
            } else if (component == 'User Management') {
                this.breadcrumbs.push({ page: 'User Management', link: '/users' });
            } else if (component == 'Onboarding Tools') {
                this.breadcrumbs.push({ page: 'Onboarding Tools', link: '/OnboardingTools' });
            } else if (component == 'Spec Validator') {
                this.breadcrumbs.push({ page: 'Spec Validator', link: '/spec-validator' });
            }
        }
        this.notifySubscriber()
    }

    
    notifySubscriber() {
        this.breadcrumbs$.next(this.breadcrumbs);
    }
}

