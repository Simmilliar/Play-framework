package controllers.utils;

import java.security.MessageDigest;

public class Utils
{
	public String hashString(String str, String salt)
	{
		str = new StringBuilder(str).insert(str.length() / 2, salt).toString();

		String hash = "";
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes("UTF-8"));
			byte[] digest = md.digest();
			hash = String.format("%064x", new java.math.BigInteger(1, digest));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hash;
	}
}