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

package com.att.vcc.inventorycollector.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Util {

	public boolean isMobility(String entityType) {
		return (entityType.equalsIgnoreCase(Constants.VSERVER_ENTITY)
				|| entityType.equalsIgnoreCase(Constants.GENERIC_VNF_ENTITY)) ? true : false;
	}

	public boolean isGamma(String entityType) {
		return (entityType.equalsIgnoreCase(Constants.VCE_ENTITY)) ? true : false;
	}

	public boolean isVMvFW(String nodeType) {
		if (nodeType.equalsIgnoreCase("JNXvFW") || nodeType.equalsIgnoreCase("FRWL")
				|| nodeType.equalsIgnoreCase("FPDG"))
			return true;
		return false;
	}

	public boolean isTrinity(String nodeType) {
		if (nodeType.equalsIgnoreCase("basx") || nodeType.equalsIgnoreCase("bdbl") || nodeType.equalsIgnoreCase("bdbr")
				|| nodeType.equalsIgnoreCase("bdbx") || nodeType.equalsIgnoreCase("bmsc")
				|| nodeType.equalsIgnoreCase("bmsi") || nodeType.equalsIgnoreCase("bmsx")
				|| nodeType.equalsIgnoreCase("bnfm") || nodeType.equalsIgnoreCase("bpsr")
				|| nodeType.equalsIgnoreCase("bpsx") || nodeType.equalsIgnoreCase("bsgm")
				|| nodeType.equalsIgnoreCase("bums") || nodeType.equalsIgnoreCase("bxsd")
				|| nodeType.equalsIgnoreCase("bxsp") || nodeType.equalsIgnoreCase("bxss")
				|| nodeType.equalsIgnoreCase("bnsx") || nodeType.equalsIgnoreCase("bpsd")
				|| nodeType.equalsIgnoreCase("buss") || nodeType.equalsIgnoreCase("bxsa")
				|| nodeType.equalsIgnoreCase("dbhx") || nodeType.equalsIgnoreCase("tsbg")
				|| nodeType.equalsIgnoreCase("dbkx") || nodeType.equalsIgnoreCase("dbpx")
				|| nodeType.equalsIgnoreCase("dbzx") || nodeType.equalsIgnoreCase("ctsf")
				|| nodeType.equalsIgnoreCase("buvs") || nodeType.equalsIgnoreCase("asbg")
				|| nodeType.equalsIgnoreCase("nsbg") || nodeType.equalsIgnoreCase("qsbg")
				|| nodeType.equalsIgnoreCase("rsbg") || nodeType.equalsIgnoreCase("tsbg")
				|| nodeType.equalsIgnoreCase("tsdb") || nodeType.equalsIgnoreCase("tece")
				|| nodeType.equalsIgnoreCase("bpsm") || nodeType.equalsIgnoreCase("rarf")
				|| nodeType.equalsIgnoreCase("bxsc") || nodeType.equalsIgnoreCase("bxsl")
				|| nodeType.equalsIgnoreCase("bxst") || nodeType.equalsIgnoreCase("bxsi")
				|| nodeType.equalsIgnoreCase("bxsn") || nodeType.equalsIgnoreCase("bdbr")
				|| nodeType.equalsIgnoreCase("ccdb") || nodeType.equalsIgnoreCase("ccfx")
				|| nodeType.equalsIgnoreCase("mrtf") || nodeType.equalsIgnoreCase("csdb")
				|| nodeType.equalsIgnoreCase("bslf") || nodeType.equalsIgnoreCase("bulf")
				|| nodeType.equalsIgnoreCase("bnsf") || nodeType.equalsIgnoreCase("bcdf")
				|| nodeType.equalsIgnoreCase("bclf") || nodeType.equalsIgnoreCase("bxsf")
				|| nodeType.equalsIgnoreCase("bndf") || nodeType.equalsIgnoreCase("bnmf")
				|| nodeType.equalsIgnoreCase("bhzf") || nodeType.equalsIgnoreCase("bpbf")
				|| nodeType.equalsIgnoreCase("bpaf") || nodeType.equalsIgnoreCase("bmif")
				|| nodeType.equalsIgnoreCase("bprf") || nodeType.equalsIgnoreCase("bxmf")
				|| nodeType.equalsIgnoreCase("bxaf") || nodeType.equalsIgnoreCase("dbkf")
				|| nodeType.equalsIgnoreCase("dbqf") || nodeType.equalsIgnoreCase("dbjf")
				|| nodeType.equalsIgnoreCase("dbqx") || nodeType.equalsIgnoreCase("dbjx")
				|| nodeType.equalsIgnoreCase("bfsa") || nodeType.equalsIgnoreCase("bfnm") )
			return true;
		return false;
	}

	public boolean isVUSP(String nodeType) {
		if (nodeType.equalsIgnoreCase("dbcx") || nodeType.equalsIgnoreCase("dbdx") || nodeType.equalsIgnoreCase("dbux")
				|| nodeType.equalsIgnoreCase("comx") || nodeType.equalsIgnoreCase("dbgx")
				|| nodeType.equalsIgnoreCase("dbsx") || nodeType.equalsIgnoreCase("dbrx")
				|| nodeType.equalsIgnoreCase("rarf") || nodeType.equalsIgnoreCase("dbax")
				|| nodeType.equalsIgnoreCase("sflb") || nodeType.equalsIgnoreCase("ibcx")
				|| nodeType.equalsIgnoreCase("iwfx") || nodeType.equalsIgnoreCase("tsbc")
				|| nodeType.equalsIgnoreCase("coms") || nodeType.equalsIgnoreCase("mrfv")
				|| nodeType.equalsIgnoreCase("mrtf") || nodeType.equalsIgnoreCase("enpx")
				|| nodeType.equalsIgnoreCase("srec") || nodeType.equalsIgnoreCase("srei")
				|| nodeType.equalsIgnoreCase("srev") || nodeType.equalsIgnoreCase("dbfh") )
			return true;
		return false;
	}

	public static String errorStacktoString(StackTraceElement[] st) {
		StringBuilder sb = new StringBuilder("ERROR STACK:\n");

		for (int i = 0; i < st.length; i++) {
			sb.append(st[i].toString());
			sb.append("\n");
		}

		return sb.toString();
	}

	public static String errorStackToString(Exception e) {
		return errorStacktoString(e.getStackTrace());
	}

	public static File createSafeFile(File dir, String fname) {
		String fname2 = dir.getAbsolutePath() + "/" + fname;
		return new File(safeFileName(fname2));
	}

	public static String safeFileName(String file) {
		// creating file with safer creation.
		if (file.contains("..")) {
			throw new RuntimeException("File name contain ..: " + file);
		}

		String safeNm = null;

		ValidationData cp = new ValidationData();
		String str = cp.cleanPathString(file);
		if (str == null || str.isEmpty() || str.contains("%")) {
			throw new RuntimeException("Invalid file name! " + file);
		} else {
			safeNm = str;
		}

		return safeNm;
	}

	private static File safeFile(File file) {
		// creating file with safer creation.
		if (file.getAbsolutePath().contains(".."))
			throw new RuntimeException("File name contain ..: " + file.getAbsolutePath());
		return file;
	}

	public static void closeFileInputStream(FileInputStream fis) {
		if (fis != null) {
			try {
				fis.close();
			} catch (IOException e) {
				throw new RuntimeException("Error calling closeFileInputStream!" + e.getMessage());
			}
		}
	}

}
