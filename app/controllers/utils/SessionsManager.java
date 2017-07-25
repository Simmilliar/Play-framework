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

	// solved todo remove it

	public String registerSession(String userAgent, String email)
	{
		// solved todo figure out how to avoid using magic numbers
		long expirationDate = System.currentTimeMillis() + TOKEN_LIFETIME;

		String token = utils.hashString(userAgent + email + expirationDate, "");

		Session session = new Session();
		session.user = Ebean.find(Users.class, email);
		session.expirationDate = expirationDate;
		session.token = token;
		session.save();

		return token;
	}

	public void unregisterSession(String token)
	{
		Session session = Ebean.find(Session.class, token);
		if (session != null)
		{
			session.expirationDate = System.currentTimeMillis();
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
		else if (session.expirationDate <= System.currentTimeMillis())
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}