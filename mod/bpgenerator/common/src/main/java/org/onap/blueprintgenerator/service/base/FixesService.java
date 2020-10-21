/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package org.onap.blueprintgenerator.service.base;

import org.onap.blueprintgenerator.exception.FixesException;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAp and DCAE Blueprint Applications
 * Service: For Blueprint Quotes Fixes
 */

@Service
public class FixesService {

	public void fixDcaeSingleQuotes(File file) {
		List<String> lines = new ArrayList<>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			for (String line = br.readLine(); line != null; line = br.readLine()){
				if(line.contains("'")) {
					line = line.replaceAll("'\\{", "{");
					line = line.replaceAll("}'", "}");
					line = line.replaceAll("'\\[", "[");
					line = line.replaceAll("]'", "]");
					line = line.replaceAll("'''''", "'");
					line = line.replaceAll("'''", "'");
					line = line.replaceAll("'''", "");
					line = line.replaceAll("''\\{", "'{");
					line = line.replaceAll("}''", "}'");
					line = line.replaceAll("''\\[", "'[");
					line = line.replaceAll("]''", "]'");
					line = line.replaceAll("\"''", "'");
					line = line.replaceAll("''\"", "'");
				}
				if(line.contains("get_input") || line.contains("get_secret") || line.contains("envs"))
					line = line.replaceAll("'", "");

				lines.add(line);
			}

			fr.close();
			br.close();

			FileWriter fw = new FileWriter(file);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for(String s: lines) {
				out.println();
				out.write(s);
				out.flush();
			}
			out.close();
			fw.close();
		} catch (Exception e) {
			throw new FixesException("Unable to Fix Single Quotes in Final DCAE Blueprint", e);
		}
	}

	public String fixStringQuotes(String string) {
		String sLines[] = string.split("\n");
		String ret = "";
		for(String line: sLines) {
			if(line.contains("get_input") || line.contains("get_secret") || ((line.contains("concat") || line.contains("default: ") || line.contains("description") || line.contains("dmaap") || line.contains(".\"'")) && line.contains("'")))
				line = line.replaceAll("'", "");

			if(line.contains("'")) {
				line = line.replaceAll("'\\{", "{");
				line = line.replaceAll("}'", "}");
				line = line.replaceAll("'\\[", "[");
				line = line.replaceAll("]'", "]");
				line = line.replaceAll("'''''", "'");
				line = line.replaceAll("'''", "'");
				line = line.replaceAll("'''", "");
				line = line.replaceAll("''\\{", "'{");
				line = line.replaceAll("}''", "}'");
				line = line.replaceAll("''\\[", "'[");
				line = line.replaceAll("]''", "]'");
				line = line.replaceAll("\"''", "'");
				line = line.replaceAll("''\"", "'");
			}
			ret = ret + "\n" + line;
		}
		return ret;
	}

	public void fixOnapSingleQuotes(File file)  {
		List<String> lines = new ArrayList<>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			for (String line = br.readLine(); line != null; line = br.readLine()){
				if(line.contains("'")) {
					line = line.replace("'", "");
				}
				if(line.contains("\"\"") && (line.contains("m") || line.contains("M"))) {
					line = line.replaceAll("\"\"", "\"");
				}
				lines.add(line);

			}
			fr.close();
			br.close();
			FileWriter fw = new FileWriter(file);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			for(String s: lines) {
				out.println();
				out.write(s);
				out.flush();
			}

			out.close();
			fw.close();

		} catch (Exception e) {
			throw new FixesException("Unable to Fix Single Quotes in final ONAP Blueprint", e);
		}
	}

	private String ensureNoSingleQuotes(String line) {
		if ((line.contains("concat") || line.contains("default: ") || line.contains("description") || line.contains("dmaap") || line.contains(".\"'")) && line.contains("'"))
			return line.replace("'", "");
		else
			return line;
	}

	public String applyFixes(String bp) {
		List<String> lines = new ArrayList<>();
		String[] linesPre = bp.split("\n");
		for (String line : linesPre) {
			lines.add(ensureNoSingleQuotes(line));
		}
		return String.join("\n", lines);
	}
	
}
