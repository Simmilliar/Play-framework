package controllers;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.Session;
import models.Users;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import tyrex.services.UUID;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ApiUserslistTest extends WithApplication
{
	@Override
	protected Application provideApplication()
	{
		return new GuiceApplicationBuilder().build();
	}

	private Users registerTestUser(String name, String email, String password, boolean confirmed)
	{
		Users user = new Users();
		user.setName(name);
		user.setEmail(email);
		user.setAvatarUrl(routes.Assets.versioned(new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)).url());
		user.setFacebookId(-1L * System.currentTimeMillis());
		user.setTwitterId(-1L * System.currentTimeMillis());
		user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
		user.setPasswordHash(new Utils().hashString(password, user.getPasswordSalt()));
		user.setConfirmed(confirmed);
		String confirmationKey = UUID.create();
		user.setConfirmationKeyHash(new Utils().hashString(confirmationKey, confirmationKey));
		user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
		user.save();
		return user;
	}

	private String sessionToken;

	@Before
	public void setup()
	{
/*
		Users user = registerTestUser("Registered User 1", "registered-email-1@example.com", "registered", true);
		registerTestUser("Registered User 2", "registered-email-2@example.com", "registered", true);
		registerTestUser("Not Confirmed User", "not-confirmed-email@example.com", "not confirmed", false);

		SessionsManager sessionsManager = new SessionsManager(new Utils());
		sessionToken = sessionsManager.registerSession(
				sessionsManager.AUTH_TYPE_API,
				"Some user agent", user.getUserId()
		);
*/
	}

	@Test
	public void testUnauthorized()
	{
		/*Result result = new ApiController(usersRepository, sessionRepository, new SessionsManager(new Utils()), new Utils())
				.usersList("" + System.currentTimeMillis());
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"session_token\":[\"Invalid session token.\"]}", contentAsString(result));*/
	}

	@Test
	public void testAuthorized()
	{
		/*Result result = new ApiController(usersRepository, sessionRepository, new SessionsManager(new Utils()), new Utils())
				.usersList(sessionToken);
		assertEquals(OK, result.status());
		assertTrue(contentAsString(result).matches("\\[\\{\"userId\":\"[a-z0-9-]+\",\"name\":\"Registered User 1\",\"email\":\"registered-email-1@example.com\",\"avatarUrl\":\"/assets/images/default_avatar.jpg\"\\},\\{\"userId\":\"[a-z0-9-]+\",\"name\":\"Registered User 2\",\"email\":\"registered-email-2@example.com\",\"avatarUrl\":\"/assets/images/default_avatar.jpg\"\\}\\]"));*/
	}

	@After
	public void clear()
	{
		for (Session session : Ebean.find(Session.class).findList())
		{
			session.delete();
		}
		for (Users user : Ebean.find(Users.class).findList())
		{
			user.delete();
		}
	}
}