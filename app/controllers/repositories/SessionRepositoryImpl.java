package controllers.repositories;

import io.ebean.Ebean;
import models.Session;

public class SessionRepositoryImpl implements SessionRepository
{
	@Override
	public Session findByToken(String sessionToken)
	{
		return Ebean.find(Session.class, sessionToken);
	}

	@Override
	public void saveSession(Session session)
	{
		session.save();
	}
}
