package controllers;

import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import models.Users;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

public class ApiAuthorizationTest extends WithApplication
{
	@Override
	protected Application provideApplication()
	{
		return new GuiceApplicationBuilder().build();
	}

	private Users buildTestUser(String name, String email, String password, boolean confirmed)
	{
		Users user = new Users();
		user.setUserId(UUID.randomUUID());
		user.setName(name);
		user.setEmail(email);
		user.setAvatarUrl(routes.Assets.versioned(new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)).url());
		user.setFacebookId(-1L * System.currentTimeMillis());
		user.setTwitterId(-1L * System.currentTimeMillis());
		user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
		user.setPasswordHash(new Utils().hashString(password, user.getPasswordSalt()));
		user.setConfirmed(confirmed);
		String confirmationKey = UUID.randomUUID().toString();
		user.setConfirmationKeyHash(new Utils().hashString(confirmationKey, confirmationKey));
		user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
		return user;
	}

	@Before
	public void setup()
	{
	}

	@Test
	public void testEmpty()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("")).thenReturn(null);
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("", "");
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"email\":[\"Invalid e-mail address.\"]}", contentAsString(result));
	}

	@Test
	public void testInvalidEmail()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("incorrect@email")).thenReturn(null);
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("incorrect@email", "somepassword");
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"email\":[\"Invalid e-mail address.\"]}", contentAsString(result));
	}

	@Test
	public void testValidMailNotRegistered()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("valid@email.com")).thenReturn(null);
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("valid@email.com", "somepassword");
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"email\":[\"Unregistered user.\"]}", contentAsString(result));
	}

	@Test
	public void testValidMailNotConfirmed()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("not-confirmed-email@example.com")).thenReturn(
				buildTestUser("Not confirmed", "not-confirmed-email@example.com",
						"not confirmed", false));
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("not-confirmed-email@example.com", "not confirmed");
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"email\":[\"Unregistered user.\"]}", contentAsString(result));
	}

	@Test
	public void testWrongPassword()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("registered-email@example.com")).thenReturn(
				buildTestUser("Registered", "registered-email@example.com",
						"registered", true));
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("registered-email@example.com", "wrongpassword");
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"password\":[\"Wrong password.\"]}", contentAsString(result));
	}

	@Test
	public void testAllCorrect()
	{
		UsersRepository usersRepository = mock(UsersRepository.class);
		when(usersRepository.findByEmail("registered-email@example.com")).thenReturn(
				buildTestUser("Registered", "registered-email@example.com",
						"registered", true));
		SessionRepository sessionRepository = mock(SessionRepository.class);

		Result result = new ApiController(usersRepository, sessionRepository,
				new SessionsManager(new Utils(), sessionRepository, usersRepository), new Utils())
				.authorize("registered-email@example.com", "registered");
		assertEquals(OK, result.status());
		Logger.debug(contentAsString(result));
		assertTrue(contentAsString(result).matches("^\"([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)\"$"));
	}

	@Test
	public void testUnauthorizeInvalid()
	{
		/*Result result = new ApiController(usersRepository, sessionRepository, new SessionsManager(new Utils()), new Utils())
				.unauthorize("" + System.currentTimeMillis());
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("{\"session_token\":[\"Invalid session token.\"]}", contentAsString(result));*/
	}

	@Test
	public void testUnauthorizeValid()
	{
		/*Result result = new ApiController(usersRepository, sessionRepository, new SessionsManager(new Utils()), new Utils())
				.unauthorize(sessionToken);
		assertEquals(OK, result.status());
		assertEquals("", contentAsString(result));*/
	}
}