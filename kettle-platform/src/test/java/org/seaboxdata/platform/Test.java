package org.seaboxdata.platform;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;

import org.pentaho.di.core.encryption.Encr;
import org.seaboxdata.systemmng.utils.task.KettleEncr;

public class Test {
	public static void main(String[] args) throws UnsupportedEncodingException {
		//System.out.println(KettleEncr.encryptPassword("1qaz@WSX"));

		//System.out.println(KettleEncr.decryptPasswd("2be98afc86aa7f2e4cb79ce71da9fa6d4"));
		
		 System.out.println(decryptPassword("70d1f7dbf95894c6d1fd388c53fad38f99"));
		 
	}
	
	private static final int RADIX = 16;
	  private static final String SEED = "0933910847463829827159347601486730416058";

	public static final String decryptPassword( String encrypted ) {
	    if ( encrypted == null ) {
	      return "";
	    }
	    if ( encrypted.length() == 0 ) {
	      return "";
	    }

	    BigInteger bi_confuse = new BigInteger( SEED );

	    try {
	      BigInteger bi_r1 = new BigInteger( encrypted, RADIX );
	      BigInteger bi_r0 = bi_r1.xor( bi_confuse );

	      return new String( bi_r0.toByteArray() );
	    } catch ( Exception e ) {
	      return "";
	    }
	  }
}
