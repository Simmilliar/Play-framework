package controllers;

import com.google.inject.ImplementedBy;
import models.Session;

@ImplementedBy(SessionRepositoryImpl.class)
public interface SessionRepository
{
	Session findByToken(String sessionToken);
	void saveSession(Session session);
}