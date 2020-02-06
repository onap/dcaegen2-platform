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

/**
 * This class is used to fix Critical and High vulnerabilities issues
 * The approach is recommended by Fortify Scan
 * To create a whitelist of characters that are allowed by each application
 * 
 */

public class ValidationData {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ValidationData obj = new ValidationData();

		obj.test2();
		obj.test3();
	}
	
	
	/**
	 * Similar to the regular expressions:
	 * name.matches("[a-zA-Z0-9]*")
	 * 
	 * @param aString
	 * @return
	 */
    public static String cleanString(String aString) {

       if (aString == null) return null;

       String cleanString = "";

       for (int i = 0; i < aString.length(); ++i) {
           cleanString += cleanCharForAlphaNumeric(aString.charAt(i));
       }

       return cleanString;
   }
    
    public static String cleanPathString(String aString) {

        if (aString == null) return null;

        String cleanString = "";

        for (int i = 0; i < aString.length(); ++i) {
            cleanString += cleanCharForPath(aString.charAt(i));
        }

        return cleanString;
    }
    
    public static String cleanCmdString(String aString) {

        if (aString == null) return null;

        String cleanString = "";
 	   
       for (int i = 0; i < aString.length(); ++i) {
	       	String s = "" + aString.charAt(i);
 	       	if ( "&".equalsIgnoreCase(s) ) {
	       		cleanString += s;
	       	} else {
	       		cleanString += cleanCommandStr(aString.charAt(i));
	       	}
       } 	   

       return cleanString;
    }
    
    private static char cleanCharForAlphaNumeric(char aChar) {
 	   
        // 0 - 9
        for (int i = 48; i < 58; ++i) {
             if (aChar == i) return (char) i;
        }

        // 'A' - 'Z'
        for (int i = 65; i < 91; ++i) {
            if (aChar == i) return (char) i;
        }

        // 'a' - 'z'
        for (int i = 97; i < 123; ++i) {
            if (aChar == i) return (char) i;
        }
        
        return '%';
    }
        

   private static char cleanCharForPath(char aChar) {
	   
      // 0 - 9
      for (int i = 48; i < 58; ++i) {
           if (aChar == i) return (char) i;
      }

      // 'A' - 'Z'
      for (int i = 65; i < 91; ++i) {
          if (aChar == i) return (char) i;
      }

      // 'a' - 'z'
      for (int i = 97; i < 123; ++i) {
          if (aChar == i) return (char) i;
      }
      
      // other valid characters
       switch (aChar) {
           case '/':
               return '/';

           case '.':
               return '.';

           case '-':
               return '-';

           case '_':
               return '_';

           case ' ':
               return ' ';
               
           case ':':
               return ':';
       }

       return '\\';
   }
   
   
   private static char cleanCommandStr(char aChar) {
	   
	      // 0 - 9
	      for (int i = 48; i < 58; ++i) {
	           if (aChar == i) return (char) i;
	      }

	      // 'A' - 'Z'
	      for (int i = 65; i < 91; ++i) {
	          if (aChar == i) return (char) i;
	      }

	      // 'a' - 'z'
	      for (int i = 97; i < 123; ++i) {
	          if (aChar == i) return (char) i;
	      }
	      
	      // other valid characters
	       switch (aChar) {
	           case '/':
	               return '/';

	           case '.':
	               return '.';

	           case '-':
	               return '-';

	           case '_':
	               return '_';

	           case ' ':
	               return ' ';
	               
	           case ':':
	               return ':';

	           case '>':
	        	   return '>';
	        	   
	       }

	       return '\\';
	   }

   	
	public void test2() {
		// TC2
		// testing command string
		String cmdStr = "sh " + System.getenv("DTI") + "/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1";
		String retStr = cleanCmdString(cmdStr);
		
		System.out.println("cmdStr = " + cmdStr);
		System.out.println("retStr = " + retStr);
		
		// testing results
		// cmdStr = sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1
		// retStr = sh null/bin/driver-vecTopo.sh > /opt/logs/DCAE/dti/run_driver-vecTopo.log 2>&1
	}
	

	public void test3() {
		// TC2
		// testing command string
		String path = "/temp_config/complex_data.txt";
		String retStr = cleanPathString(path);
		
		System.out.println("path   = " + path);
		System.out.println("retStr = " + retStr);
		
		// testing results

	}	
	
}