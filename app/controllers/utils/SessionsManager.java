package controllers.utils;

import io.ebean.Ebean;
import models.data.Session;
import models.data.User;
import play.mvc.Http;

import java.util.concurrent.TimeUnit;

public class SessionsManager
{
	public static final long TOKEN_LIFETIME = TimeUnit.DAYS.toSeconds(30);

	public static boolean userAuthorized(Http.Request request)
	{
		return request.cookies().get("session_token") != null &&
				SessionsManager.checkSession(request.cookies().get("session_token").value());
	}

	public static String registerSession(String userAgent, String email)
	{
		long timestamp = System.currentTimeMillis() / 1000L + TOKEN_LIFETIME;

		String token = Utils.hashString(userAgent + email + timestamp);

		Session session = new Session();
		session.user = Ebean.find(User.class, email);
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
			Ebean.delete(session);
			return false;
		}
		else
		{
			return true;
		}
	}
}