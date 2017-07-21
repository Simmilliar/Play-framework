package controllers.utils;

import org.apache.commons.codec.binary.Base64;
import play.mvc.Http;

import java.security.MessageDigest;
import java.time.Duration;

public class Utils
{

	public static final String REGEX_NAME = "^[\\p{L}\\s'.-]+$";
	public static final String REGEX_EMAIL = "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	// solved todo url address should not be hardcoded
	//ok
	public static final String EMAIL_CONFIRMATION = "To complete your registration you need to confirm your e-mail " +
			"address by following this link: http://%s/emailconfirm?key=%s";
	// solved todo url address should not be hardcoded
	//ok
	public static final String EMAIL_PASSWORD_CHANGE = "You can change your password by following this link: " +
			"http://%s/changepassword?key=%s\nIf you don't want to do this, just ignore this e-mail.";

	public static String hashString(String str)
	{
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

	public void setNotification(Http.Response response, String notification, String host)
	{
		response.setCookie(Http.Cookie.builder("notif", new String(Base64.encodeBase64(notification.getBytes())))
				.withMaxAge(Duration.ofSeconds(60))
				.withPath("/")
				.withDomain(host)	// solved todo is this parameter important?
									//yes, it is
				.withSecure(false)
				.withHttpOnly(true)
				.withSameSite(Http.Cookie.SameSite.STRICT)
				.build()
		);
	}

	public String getNotification(Http.Request request)
	{
		Http.Cookie notif = request.cookies().get("notif");
		String notification = "";
		if (notif != null)
		{
			notification = new String(java.util.Base64.getDecoder().decode(notif.value()));
		}
		return notification;
	}
}