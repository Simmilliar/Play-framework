package controllers.utils;

import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SessionsManager
{
	public final long TOKEN_LIFETIME = TimeUnit.DAYS.toMillis(30);
	public final Utils utils;

	@Inject
	public SessionsManager(Utils utils)
	{
		this.utils = utils;
	}

	public String registerSession(String userAgent, String email)
	{
		long expirationDate = System.currentTimeMillis() + TOKEN_LIFETIME;

		String token = utils.hashString(userAgent + email + expirationDate, "");

		Session session = new Session();
		session.setUser(Ebean.find(Users.class, email));
		session.setExpirationDate(expirationDate);
		session.setToken(token);
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

	public boolean checkSession(String token)
	{
		Session session = Ebean.find(Session.class, token);
		if (session == null)
		{
			return false;
		}
		else if (session.getExpirationDate() <= System.currentTimeMillis())
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}