package controllers;

import io.ebean.Ebean;
import models.data.Sessions;
import models.data.User;

import java.util.concurrent.TimeUnit;

public class SessionsManager {

	static final long TOKEN_LIFETIME = TimeUnit.DAYS.toSeconds(30);

	static String registerSession(String userAgent, String email) {
		long timestamp = System.currentTimeMillis() / 1000L + TOKEN_LIFETIME;

		String token = Utils.hashString(userAgent + email + timestamp);

		Sessions sessions = new Sessions();
		sessions.user = Ebean.find(User.class, email);
		sessions.expirationDate = timestamp;
		sessions.token = token;
		sessions.save();

		return token;
	}

	static void unregisterSession(String token) {
		if (checkSession(token)) {
			Ebean.delete(Ebean.find(Sessions.class, token));
		}
	}

	static boolean checkSession(String token) {
		Sessions session = Ebean.find(Sessions.class, token);
		if (session == null) {
			return false;
		} else if (session.expirationDate <= System.currentTimeMillis() / 1000L) {
			Ebean.delete(session);
			return false;
		} else {
			return true;
		}
	}
}
