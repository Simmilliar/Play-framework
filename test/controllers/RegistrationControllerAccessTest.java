package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.MailerService;
import controllers.utils.SessionsManager;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class RegistrationControllerAccessTest extends WithApplication {

	private final SessionRepository mockSessionRepository = mock(SessionRepository.class);
	private final Session mockSession = mock(Session.class);
	private final UsersRepository mockUsersRepository = mock(UsersRepository.class);

	@Override
	protected Application provideApplication() {
		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.overrides(bind(MailerService.class).toInstance(mock(MailerService.class)))
				.overrides(bind(SessionsManager.class).toInstance(mock(SessionsManager.class)))
				.build();
	}

	@Test
	public void accessRegistrationAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.RegistrationController.registration().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessRegisterAuthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.RegistrationController.register().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessConfirmEmailAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.RegistrationController.confirmEmail("").url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockUsersRepository, never()).findUnconfirmedByConfirmationKey(anyString());
	}

	@Test
	public void accessRegistrationUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.RegistrationController.registration().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accessRegisterUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void accessConfirmEmailUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.RegistrationController.confirmEmail("").url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockUsersRepository, atLeast(1)).findUnconfirmedByConfirmationKey(anyString());
	}
}