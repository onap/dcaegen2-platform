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
import static org.junit.Assert.assertTrue;

import java.io.File;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.vcc.inventorycollector.util.Constants;
import com.att.vcc.inventorycollector.util.Util;
import com.google.inject.internal.util.StackTraceElements;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TestUtil {
	
	@InjectMocks
	Util util = new Util();

	@Test
	public void testIsMobility() {
		assertTrue(util.isMobility(Constants.VSERVER_ENTITY));
		assertTrue(util.isMobility(Constants.GENERIC_VNF_ENTITY));
		assertFalse(util.isMobility(Constants.PNF_ENTITY));
	}

	@Test
	public void testIsGamma() {
		assertTrue(util.isGamma(Constants.VCE_ENTITY));
		assertFalse(util.isGamma(Constants.VSERVER_ENTITY));
	}
	
	@Test
	public void testIsVMvFW() {
		assertTrue(util.isVMvFW("JNXvFW"));
		assertTrue(util.isVMvFW("FRWL"));
		assertTrue(util.isVMvFW("FPDG"));
		assertFalse(util.isVMvFW("VPMS"));
	}
	
	@Test
	public void testIsTrinity() {
		assertTrue(util.isTrinity("basx"));
		assertTrue(util.isTrinity("bdbl"));
		assertTrue(util.isTrinity("bdbr"));
		assertTrue(util.isTrinity("bdbx"));
		assertTrue(util.isTrinity("bmsc"));
		assertTrue(util.isTrinity("bmsi"));
		assertTrue(util.isTrinity("bmsx"));
		assertTrue(util.isTrinity("bnfm"));
		assertTrue(util.isTrinity("bpsr"));
		assertTrue(util.isTrinity("bpsx"));
		assertTrue(util.isTrinity("bsgm"));
		assertTrue(util.isTrinity("bums"));
		assertTrue(util.isTrinity("bxsd"));
		assertTrue(util.isTrinity("bxsp"));
		assertTrue(util.isTrinity("bxss"));
		assertTrue(util.isTrinity("bnsx"));
		assertTrue(util.isTrinity("bpsd"));
		assertTrue(util.isTrinity("buss"));
		assertTrue(util.isTrinity("bxsa"));
		assertTrue(util.isTrinity("dbhx"));
		assertTrue(util.isTrinity("tsbg"));
		assertTrue(util.isTrinity("dbkx"));
		assertTrue(util.isTrinity("dbpx"));
		assertTrue(util.isTrinity("dbzx"));
		assertTrue(util.isTrinity("ctsf"));
		assertTrue(util.isTrinity("buvs"));
		assertTrue(util.isTrinity("asbg"));
		assertTrue(util.isTrinity("nsbg"));
		assertTrue(util.isTrinity("qsbg"));
		assertTrue(util.isTrinity("rsbg"));
		assertTrue(util.isTrinity("tsbg"));
		assertTrue(util.isTrinity("tsdb"));
		assertTrue(util.isTrinity("tece"));
		assertTrue(util.isTrinity("bpsm"));
		assertTrue(util.isTrinity("rarf"));
		assertTrue(util.isTrinity("bxsc"));
		assertTrue(util.isTrinity("bxsl"));
		assertTrue(util.isTrinity("bxst"));
		assertTrue(util.isTrinity("bxsi"));
		assertTrue(util.isTrinity("bxsn"));
		assertTrue(util.isTrinity("bdbr"));
		assertTrue(util.isTrinity("ccdb"));
		assertTrue(util.isTrinity("ccfx"));
		assertTrue(util.isTrinity("mrtf"));
		assertTrue(util.isTrinity("csdb"));
		assertTrue(util.isTrinity("bslf"));
		assertTrue(util.isTrinity("bulf"));
		assertTrue(util.isTrinity("bnsf"));
		assertTrue(util.isTrinity("bcdf"));
		assertTrue(util.isTrinity("bclf"));
		assertTrue(util.isTrinity("bxsf"));
		assertTrue(util.isTrinity("bndf"));
		assertTrue(util.isTrinity("bnmf"));
		assertTrue(util.isTrinity("bhzf"));
		assertTrue(util.isTrinity("bpbf"));
		assertTrue(util.isTrinity("bpaf"));
		assertTrue(util.isTrinity("bmif"));
		assertTrue(util.isTrinity("bprf"));
		assertTrue(util.isTrinity("bxmf"));
		assertTrue(util.isTrinity("bxaf"));
		assertTrue(util.isTrinity("dbkf"));
		assertTrue(util.isTrinity("dbqf"));
		assertTrue(util.isTrinity("dbjf"));
		assertTrue(util.isTrinity("dbqx"));
		assertTrue(util.isTrinity("dbjx"));
		assertTrue(util.isTrinity("bfsa"));
		assertTrue(util.isTrinity("bfnm"));
		assertFalse(util.isTrinity("dbcx"));
		assertFalse(util.isTrinity("JNXvFW"));
		assertFalse(util.isTrinity("enpx"));
		assertFalse(util.isTrinity(Constants.PNF_ENTITY));
	}

	@Test
	public void testIsVUSP() {
		assertTrue(util.isVUSP("dbcx"));
		assertTrue(util.isVUSP("dbdx"));
		assertTrue(util.isVUSP("dbux"));
		assertTrue(util.isVUSP("comx"));
		assertTrue(util.isVUSP("dbgx"));
		assertTrue(util.isVUSP("dbsx"));
		assertTrue(util.isVUSP("dbrx"));
		assertTrue(util.isVUSP("rarf"));
		assertTrue(util.isVUSP("dbax"));
		assertTrue(util.isVUSP("sflb"));
		assertTrue(util.isVUSP("ibcx"));
		assertTrue(util.isVUSP("iwfx"));
		assertTrue(util.isVUSP("tsbc"));
		assertTrue(util.isVUSP("coms"));
		assertTrue(util.isVUSP("mrfv"));
		assertTrue(util.isVUSP("mrtf"));
		assertTrue(util.isVUSP("enpx"));
		assertTrue(util.isVUSP("srec"));
		assertTrue(util.isVUSP("srei"));
		assertTrue(util.isVUSP("srev"));
		assertTrue(util.isVUSP("dbfh"));
		assertFalse(util.isVUSP("bndf"));
		assertFalse(util.isVUSP("dbjx"));
		assertFalse(util.isVUSP("JNXvFW"));
		assertFalse(util.isVUSP(Constants.PNF_ENTITY));
		assertFalse(util.isVUSP("VPMS"));
	}
	
	@Test
	public void testSafeFileName() {
		assertEquals("testFileName.dat", Util.safeFileName("testFileName.dat"));
	}
	
	@Test(expected = RuntimeException.class)
	public void testSafeFileNameForBadData() {
		Util.safeFileName("testFileName..dat");
	}
	
	

}
