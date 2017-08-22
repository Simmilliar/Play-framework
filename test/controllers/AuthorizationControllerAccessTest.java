package controllers;

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

public class AuthorizationControllerAccessTest extends WithApplication {

	private SessionRepository mockSessionRepository;
	private Session mockSession;
	private UsersRepository mockUsersRepository;
	private SessionsManager mockSessionsManager;

	@Override
	protected Application provideApplication() {
		mockSessionRepository = mock(SessionRepository.class);
		mockSession = mock(Session.class);
		mockUsersRepository = mock(UsersRepository.class);
		mockSessionsManager = mock(SessionsManager.class);

		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.overrides(bind(MailerService.class).toInstance(mock(MailerService.class)))
				.overrides(bind(SessionsManager.class).toInstance(mockSessionsManager))
				.build();
	}

	@Test
	public void accessAuthorizationAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.authorization().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessAuthorizeAuthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessLogoutAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.logout().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsManager, atLeast(1)).unregisterSession(anyString());
	}

	@Test
	public void accessAuthorizationUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.AuthorizationController.authorization().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accessAuthorizeUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void accessLogoutUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.AuthorizationController.logout().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsManager, never()).unregisterSession(anyString());
	}
}