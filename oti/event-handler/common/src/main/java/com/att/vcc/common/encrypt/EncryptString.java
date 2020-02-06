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

package com.att.vcc.common.encrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;


public class EncryptString{

	static Cipher cipher;
        static byte[] key;
        static byte[] ivspec;
	static SecretKey encKey ;
	static IvParameterSpec ivSpec;
	static boolean initialised = false;

	static void init ()
	{
	   try {
			//Key is generated from password cssecret
			String keyStr = "6F7E1ECC9DF67BEFFBC21ED9A1BCA44B"; 
			byte[] key = javax.xml.bind.DatatypeConverter.parseHexBinary(keyStr);

			String ivStr = "3F512AAA648274A7161E4F40EE093FBD";
			ivspec = javax.xml.bind.DatatypeConverter.parseHexBinary(ivStr);
			ivSpec = new  IvParameterSpec(ivspec);
			
			encKey = new SecretKeySpec(key, "AES");

			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			initialised = true;
	   }
	   catch (Exception ex )
	   {
		System.err.println("Exception while initializing the Cipher");
		ex.printStackTrace();
	   }
	
		
	}


	public static String encrypt (String inputString) 
	{
		if ( !initialised)
			init();
		long start = System.currentTimeMillis();
		String encryptedString = null;
		try {
			if ( inputString == null ) 
			    return null;	

		        cipher.init(Cipher.ENCRYPT_MODE, encKey, ivSpec);

		        byte[] stringtoencrypt = inputString.getBytes();
			//System.out.println("Lenght of string to be encrypted="+stringtoencrypt.length);
		        byte[] encrypted = cipher.doFinal(stringtoencrypt);
		        //System.out.println("encrypted string: " + asHex(encrypted));
		        //System.out.println("encrypted length="+encrypted.length);
			encryptedString  = new Base64().encode(encrypted);
		}
		catch (Exception ex)
		{
		   System.err.println("Exception while Encrypting : "+ex.toString());
		   ex.printStackTrace();
		}
		long timetaken = System.currentTimeMillis()-start;
		//System.out.println("Time taken to encrypt="+timetaken);
		return encryptedString;
	}

	public static String decrypt ( String encryptedString )
	{
		if ( !initialised)
			init();
		long start = System.currentTimeMillis();
		if ( encryptedString == null )
			return null;
	
		String originalString = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, encKey, ivSpec );
			byte[] decodedEncryptedBytes = new Base64().decode(encryptedString);
	        	byte[] original = cipher.doFinal(decodedEncryptedBytes);
			originalString = new String(original);
			//System.out.println("Original string: " + originalString );

		}
		catch (Exception x )
		{
		   	System.err.println("Exception while decrypting :"+x.toString());
			x.printStackTrace();
		}
		long timetaken = System.currentTimeMillis()-start;
		//System.out.println("Time taken to decrypt="+timetaken);
		return originalString;
	}
			
	
	public static String asHex (byte buf[]) {
	      StringBuffer strbuf = new StringBuffer(buf.length * 2);
	      int i;

	      for (i = 0; i < buf.length; i++) {
	       if (((int) buf[i] & 0xff) < 0x10)
		    strbuf.append("0");

	       strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
	      }

	      return strbuf.toString();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if ( args.length <2 )
   		{
		    System.out.println("Invalid arguments. Usage : java EncryptString <-e|-d> <string> ");
		    System.exit(1);
		   
		}
		if ( args[0].equals("-e") )
		   System.out.println( EncryptString.encrypt( args[1]));
	        else if ( args[0].equals("-d"))	
		    System.out.println( EncryptString.decrypt( args[1]));
		else
		    System.out.println("Invalid arguments. Usage : java EncryptString <-e|-d> <string> ");
	

	
	}


}


