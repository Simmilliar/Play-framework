package controllers.utils;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import models.Session;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionsUtils
{
	public final long TOKEN_LIFETIME = TimeUnit.DAYS.toMillis(30);
	private final Utils utils;
	private final SessionRepository sessionRepository;
	private final UsersRepository usersRepository;

	public final String AUTH_TYPE_PASSWORD = "password";
	public final String AUTH_TYPE_FACEBOOK = "facebook";
	public final String AUTH_TYPE_TWITTER = "twitter";
	public final String AUTH_TYPE_API = "api";

	@Inject
	public SessionsUtils(Utils utils, SessionRepository sessionRepository, UsersRepository usersRepository)
	{
		this.utils = utils;
		this.sessionRepository = sessionRepository;
		this.usersRepository = usersRepository;
	}

	public String registerSession(String authType, UUID userId)
	{
		long expirationDate = System.currentTimeMillis() + TOKEN_LIFETIME;

		String token = utils.hashString("" + System.currentTimeMillis() + userId + expirationDate, "");

		Session session = new Session();
		session.setUser(usersRepository.findById(userId));
		session.setExpirationDate(expirationDate);
		session.setToken(token);
		session.setAuthType(authType);
		sessionRepository.saveSession(session);

		return token;
	}

	public void unregisterSession(String token)
	{
		Session session = sessionRepository.findByToken(token);
		if (session != null)
		{
			session.setExpirationDate(System.currentTimeMillis());
			sessionRepository.saveSession(session);
		}
	}
}