package controllers.utils;

import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionsManager
{
	public final long TOKEN_LIFETIME = TimeUnit.DAYS.toMillis(30);
	public final Utils utils;

	public final String AUTH_TYPE_PASSWORD = "password";
	public final String AUTH_TYPE_FACEBOOK = "facebook";
	public final String AUTH_TYPE_TWITTER = "twitter";

	@Inject
	public SessionsManager(Utils utils)
	{
		this.utils = utils;
	}

	public String registerSession(String authType, String userAgent, UUID userId)
	{
		long expirationDate = System.currentTimeMillis() + TOKEN_LIFETIME;

		String token = utils.hashString(userAgent + userId + expirationDate, "");

		Session session = new Session();
		session.setUser(Ebean.find(Users.class, userId));
		session.setExpirationDate(expirationDate);
		session.setToken(token);
		session.setAuthType(authType);
		session.save();

		return token;
	}

	public void unregisterSession(String token)
	{
		Session session = Ebean.find(Session.class, token);
		if (session != null)
		{
			session.setExpirationDate(System.currentTimeMillis());
			session.save();
		}
	}
}