package controllers.utils;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	public static final String DEFAULT_AVATAR_ASSET = "images/default_avatar.jpg";


	private final String REGEX_NAME = "^[\\p{L}\\s'.-]+$";
	private final String REGEX_EMAIL = "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	private final String REGEX_UUID = "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})";

	public boolean isNameValid(String name){
		return name.matches(REGEX_NAME);
	}

	public boolean isEmailValid(String email){
		return email.matches(REGEX_EMAIL);
	}

	public boolean isUUIDValid(String uuid){
		return uuid.matches(REGEX_UUID);
	}

	public String fetchUUID(String str) {
		Matcher mailMatcher = Pattern.compile(REGEX_UUID).matcher(str);
		return mailMatcher.find() ? mailMatcher.group(1) : null;
	}

	public static final String EMAIL_CONFIRMATION = "To complete your registration you need to confirm your e-mail address by following this link: %s";
	public static final String EMAIL_PASSWORD_CHANGE = "You can change your password by following this link: %s\nIf you don't want to do this, just ignore this e-mail.";

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