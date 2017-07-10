package controllers;

import java.security.MessageDigest;

public class Utils {
	public static String hashString (String str) {
		String hash = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes("UTF-8"));
			byte[] digest = md.digest();
			hash = String.format("%064x", new java.math.BigInteger(1, digest));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}
}