package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.MailerUtils;
import controllers.utils.SessionsUtils;
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

	private SessionsUtils mockSessionsUtils;

	@Override
	protected Application provideApplication() {
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);
		UsersRepository mockUsersRepository = mock(UsersRepository.class);
		mockSessionsUtils = mock(SessionsUtils.class);

		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.overrides(bind(MailerUtils.class).toInstance(mock(MailerUtils.class)))
				.overrides(bind(SessionsUtils.class).toInstance(mockSessionsUtils))
				.build();
	}

	@Test
	public void redirect_authorized_user_to_home_page_on_authorization_page_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.authorization().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void redirect_authorized_user_to_home_page_on_authorizing_post_request() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accept_authorized_user_on_logout_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.logout().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsUtils, atLeast(1)).unregisterSession(anyString());
	}

	@Test
	public void accept_unauthorized_user_on_authorization_page_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.AuthorizationController.authorization().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accept_unauthorized_user_on_authorizing_post_request() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void redirect_unauthorized_user_to_home_page_on_logout_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.AuthorizationController.logout().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsUtils, never()).unregisterSession(anyString());
	}
}