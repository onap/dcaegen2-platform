// ============LICENSE_START=======================================================
// Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
// ================================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============LICENSE_END=========================================================

package com.att.vcc.inventroycollector.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.att.vcc.inventorycollector.util.ValidationData;

@RunWith(MockitoJUnitRunner.class)
public class TestValidationData {
	
	@InjectMocks
	ValidationData validationData = new ValidationData();
	
	@Test
	public void testCleanString() {
		assertNull(ValidationData.cleanString(null));
		assertEquals("TestCleanString123", ValidationData.cleanString("TestCleanString123"));
		assertNotEquals("TestCleanString123", ValidationData.cleanString("TestCleanString"));
	}
	
	@Test
	public void testCleanPathString() {
		assertNull(ValidationData.cleanPathString(null));
		assertEquals("/temp_config/complex_data.txt", ValidationData.cleanPathString("/temp_config/complex_data.txt"));
		assertNotEquals("/temp_config/complex_data.txt", ValidationData.cleanPathString("/temp_config/"));
	}
	
	@Test
	public void testCleanCmdString() {
		assertNull(ValidationData.cleanCmdString(null));
		assertEquals("sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1", ValidationData.cleanCmdString("sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1"));
		assertNotEquals("sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1", ValidationData.cleanCmdString("sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo"));
	}
}
