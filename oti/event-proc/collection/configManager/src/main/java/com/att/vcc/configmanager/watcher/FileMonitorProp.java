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

package com.att.vcc.configmanager.watcher;

import java.util.*;

public class FileMonitorProp
{
	private PropertyResourceBundle monProp;

	public FileMonitorProp()
	{
		try
		{
			monProp = (PropertyResourceBundle)PropertyResourceBundle.getBundle("FileMonitor", Locale.getDefault());
		}
		catch (MissingResourceException e) { ; }
	}

	public Enumeration<String> getKeys()
	{
		return monProp.getKeys();	
	}

	public String getValue(String name)
	{
		String value = null;
		try
		{
			value = monProp.getString(name);
		}
		catch (MissingResourceException e) {;}
		return value;
	}
}
