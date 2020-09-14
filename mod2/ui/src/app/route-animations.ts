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

import { trigger, transition, style, query, group, animateChild, animate, keyframes } from '@angular/animations'

export const slider =
    trigger('routeAnimations', [
        //iframe interactions
        transition('home => iframe', singleSlideOut('left')),
        transition('ms-instances => iframe', singleSlideOut('top')),
        transition('iframe => home', singleSlideIn('left')),
        transition('iframe => ms-instances', singleSlideIn('top')),
        transition('base-ms => iframe', singleSlideOut('top')),
        transition('iframe => ms-base', singleSlideIn('top')),
        transition('BPs => iframe', singleSlideOut('top')),
        transition('iframe => BPs', singleSlideIn('top')),

        //home interactions
        transition('home => ms-instances', doubleSlide('top', 'left')),
        transition('ms-instances => home', doubleSlide('left', 'top')),
        transition('home => base-ms', doubleSlide('top', 'left')),
        transition('base-ms => home', doubleSlide('left', 'top')),
        transition('home => BPs', doubleSlide('top', 'left')),
        transition('BPs => home', doubleSlide('left', 'top')),

        //table interactions
        transition('ms-instances => base-ms', doubleSlide('top', 'top')),
        transition('base-ms => ms-instances', doubleSlide('top', 'top')),
        transition('BPs => ms-instances', doubleSlide('top', 'top')),
        transition('ms-instances => BPs', doubleSlide('top', 'top')),
        transition('base-ms => BPs', doubleSlide('top', 'top')),
        transition('BPs => base-ms', doubleSlide('top', 'top')),

        //initial animations
        transition('* => home', singleSlideIn('left')),
        transition('* => ms-instances', singleSlideIn('top')),
        transition('* => base-ms', singleSlideIn('top')),
        transition('* => BPs', singleSlideIn('top'))
        
    ])

function singleSlideIn(direction) {
    const optional = { optional: true };
    return [
        query(':enter, :leave', [
            style({
                position: 'absolute',
                top: 0,
                [direction]: 0,
                width: '100%'
            })
        ], optional),
        query(':enter', [
            style({ [direction]: '100%' })
        ]),
        group([
            query(':leave', [
                animate('0ms', style({ [direction]: '100%' }))
            ], optional),
            query(':enter', [
                animate('900ms 0.4s ease', style({ [direction]: '0%' }))
            ])
        ])
    ];
}

function singleSlideOut(direction) {
    const optional = { optional: true };
    return [
        query(':enter, :leave', [
            style({
                position: 'absolute',
                top: 0,
                [direction]: 0,
                width: '100%'
            })
        ], optional),
        query(':enter', [
            style({ [direction]: '100%' })
        ]),
        group([
            query(':leave', [
                animate('600ms ease', style({ [direction]: '100%' }))
            ], optional)
        ])
    ];
}

function doubleSlide(directionIn, directionOut) {
    const optional = { optional: true };
    return [
        query(':enter, :leave', [
            style({
                position: 'absolute',
                top: 0,
                [directionOut]: 0,
                width: '100%'
            })
        ], optional),
        query(':enter', [
            style({ [directionIn]: '100%' })
        ]),
        group([
            query(':leave', [
                animate('600ms ease', style({ [directionOut]: '100%' }))
            ], optional),
            query(':enter', [
                animate('900ms 0.2s ease', style({ [directionIn]: '0%' }))
            ])
        ])
    ];
}