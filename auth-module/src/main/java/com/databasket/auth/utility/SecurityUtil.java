package com.databasket.auth.utility;

import java.util.Random;

public class SecurityUtil {

	public static String generatePassword(){
		String someWord="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz@#$!";
	    char[] password = new char[8];
	    Random rand = new Random(System.nanoTime());
	    for (int i = 0; i < 8; i++) {
	        password[i] = someWord.toCharArray()[rand.nextInt(someWord.toCharArray().length)];
	    }
	    return new String(password);
	}
}
