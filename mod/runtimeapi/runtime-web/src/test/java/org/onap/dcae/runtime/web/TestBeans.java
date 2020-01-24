/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcae.runtime.web;

import org.junit.Test;

import org.onap.dcae.runtime.web.exception.ActionsNotDefinedException;
import org.onap.dcae.runtime.web.exception.MainGraphAlreadyExistException;
import org.onap.dcae.runtime.web.exception.MainGraphNotFoundException;
import org.onap.dcae.runtime.web.models.Action;
import org.onap.dcae.runtime.web.models.DashboardConfig;
import org.onap.dcae.runtime.web.models.DistributeGraphRequest;

public class TestBeans {
    @Test
    public void testBeans() {
	Action act = new Action();
	act.setCommand(act.getCommand());
	act.setTarget_graph_id(act.getTarget_graph_id());
	act.setPayload(act.getPayload());
        DashboardConfig dc = new DashboardConfig();
	dc.setUrl(dc.getUrl());
	dc.setUsername(dc.getUsername());
	dc.setPassword(dc.getPassword());
	DistributeGraphRequest dgr = new DistributeGraphRequest();
	dgr.setActions(dgr.getActions());
	new ActionsNotDefinedException("some message");
	new MainGraphNotFoundException();
	new MainGraphAlreadyExistException("some message");
    }
}
