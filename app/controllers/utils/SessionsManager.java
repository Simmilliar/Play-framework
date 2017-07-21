package controllers.utils;

import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;
import play.mvc.Http;

import java.util.concurrent.TimeUnit;

public class SessionsManager
{
	public static final long TOKEN_LIFETIME = TimeUnit.DAYS.toSeconds(30);

	//todo instead of using this method everywhere I'd create global listener that check if user authorized or not, with list of pages that are allowed for non authorized user
	public static boolean userAuthorized(Http.Request request)
	{
		return request.cookies().get("session_token") != null &&
				SessionsManager.checkSession(request.cookies().get("session_token").value());
	}

	public static String registerSession(String userAgent, String email)
	{
		// todo why convert to seconds?
		long timestamp = System.currentTimeMillis() / 1000L + TOKEN_LIFETIME;

		String token = Utils.hashString(userAgent + email + timestamp);

		Session session = new Session();
		session.user = Ebean.find(Users.class, email);
		session.expirationDate = timestamp;
		session.token = token;
		session.save();

		return token;
	}

	public static void unregisterSession(String token)
	{
		if (checkSession(token))
		{
			Ebean.delete(Ebean.find(Session.class, token));
		}
	}

	public static boolean checkSession(String token)
	{
		Session session = Ebean.find(Session.class, token);
		if (session == null)
		{
			return false;
		}
		else if (session.expirationDate <= System.currentTimeMillis() / 1000L)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}